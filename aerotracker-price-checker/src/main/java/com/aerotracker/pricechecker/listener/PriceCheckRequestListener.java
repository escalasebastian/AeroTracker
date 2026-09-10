package com.aerotracker.pricechecker.listener;

import com.aerotracker.common.event.PriceAlertEvent;
import com.aerotracker.common.event.PriceCheckEvent;
import com.aerotracker.common.provider.FlightPriceProvider;
import com.aerotracker.common.provider.FlightPriceRequest;
import com.aerotracker.common.provider.FlightPriceResponse;
import com.aerotracker.entity.FlightPriceHistory;
import com.aerotracker.entity.Route;
import com.aerotracker.pricechecker.config.RabbitMQConfig;
import com.aerotracker.repository.FlightPriceHistoryRepository;
import com.aerotracker.repository.RouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PriceCheckRequestListener {

    private static final Logger log = LoggerFactory.getLogger(PriceCheckRequestListener.class);

    /**
     * The platform quotes every price in euros. Providers are asked for EUR explicitly,
     * which lets a cached price be reused without storing a currency alongside it.
     */
    private static final String CURRENCY = "EUR";

    private final RabbitTemplate rabbitTemplate;
    private final FlightPriceProvider flightPriceProvider;
    private final FlightPriceHistoryRepository historyRepository;
    private final RouteRepository routeRepository;
    private final long cacheTtlHours;
    private final long monthlyCallBudget;

    /**
     * When each route last failed to price. A failed call records no history row, so without
     * this a route the provider keeps rejecting (an unknown airport code, say) would be retried
     * on every scheduler cycle and quietly drain the quota. Held in memory on purpose: losing it
     * on restart just grants one extra retry.
     */
    private final Map<Long, LocalDateTime> lastFailureByRoute = new ConcurrentHashMap<>();

    public PriceCheckRequestListener(RabbitTemplate rabbitTemplate,
                                     FlightPriceProvider flightPriceProvider,
                                     FlightPriceHistoryRepository historyRepository,
                                     RouteRepository routeRepository,
                                     @Value("${aerotracker.price-cache.ttl-hours:24}") long cacheTtlHours,
                                     @Value("${aerotracker.price-cache.monthly-call-budget:200}") long monthlyCallBudget) {
        this.rabbitTemplate = rabbitTemplate;
        this.flightPriceProvider = flightPriceProvider;
        this.historyRepository = historyRepository;
        this.routeRepository = routeRepository;
        this.cacheTtlHours = cacheTtlHours;
        this.monthlyCallBudget = monthlyCallBudget;
    }

    @RabbitListener(queues = RabbitMQConfig.PRICE_CHECK_REQUESTS_QUEUE)
    @Transactional
    public void handlePriceCheckRequest(PriceCheckEvent request) {
        log.info("🔍 Received price check request for Route ID: {} ({} to {})",
                request.getRouteId(), request.getOrigin(), request.getDestination());

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + request.getRouteId()));

        // A flight that has already departed can no longer be priced. Providers reject the request,
        // and since a failed call records nothing, the route would otherwise be retried against
        // the paid API on every scheduler cycle indefinitely.
        if (LocalDate.parse(request.getDepartureDate()).isBefore(LocalDate.now())) {
            log.info("Route {} departed on {}. Skipping it without querying the price provider.",
                    route.getId(), request.getDepartureDate());
            return;
        }

        BigDecimal currentPrice = fetchFreshPrice(request, route);

        // Alerts are only evaluated against newly fetched prices. Re-evaluating a cached price on
        // every scheduler cycle would resend the same alert every few minutes.
        if (currentPrice == null) {
            log.info("No new price for route {} this cycle. No alerts evaluated.", route.getId());
            return;
        }

        // Process the "backpack" of subscribers
        String routeDescription = request.getOrigin() + "-" + request.getDestination();
        Map<Long, BigDecimal> subscriptions = request.getSubscriptions();

        if (subscriptions == null || subscriptions.isEmpty()) {
            log.info("No subscribers for this route.");
            return;
        }

        int alertsSent = 0;
        for (Map.Entry<Long, BigDecimal> entry : subscriptions.entrySet()) {
            Long chatId = entry.getKey();
            BigDecimal targetPrice = entry.getValue();

            // Check if the current price is less than or equal to the target price
            if (currentPrice.compareTo(targetPrice) <= 0) {
                PriceAlertEvent alertEvent = new PriceAlertEvent(chatId, routeDescription, currentPrice.doubleValue(), CURRENCY);
                rabbitTemplate.convertAndSend("price-alerts-queue", alertEvent);
                alertsSent++;
            }
        }

        log.info("Finished processing route. Sent {} alerts out of {} total subscribers.", alertsSent, subscriptions.size());
    }

    /**
     * Fetches a new price from the provider, or returns null when no request should be made.
     * <p>
     * A route priced within the cache window is not queried again. The scheduler wakes up every
     * few minutes, but flight prices do not move meaningfully at that rate and the real provider
     * is capped at a few hundred calls per month, so only a stale or missing price is worth a
     * real request. This is what keeps an arbitrary number of tracked routes affordable.
     */
    private BigDecimal fetchFreshPrice(PriceCheckEvent request, Route route) {
        Optional<FlightPriceHistory> lastKnown =
                historyRepository.findTopByRouteIdOrderByCheckedAtDesc(route.getId());

        LocalDateTime staleBefore = LocalDateTime.now().minusHours(cacheTtlHours);
        if (lastKnown.isPresent() && lastKnown.get().getCheckedAt().isAfter(staleBefore)) {
            FlightPriceHistory cached = lastKnown.get();
            log.info("♻️ Route {} was priced at {} ({} {}). No provider call needed.",
                    route.getId(), cached.getCheckedAt(), cached.getPrice(), CURRENCY);
            return null;
        }

        LocalDateTime lastFailure = lastFailureByRoute.get(route.getId());
        if (lastFailure != null && lastFailure.isAfter(staleBefore)) {
            log.info("Route {} failed to price at {}. Not retrying until the cache window passes.",
                    route.getId(), lastFailure);
            return null;
        }

        // The cap on distinct tracked routes bounds steady-state usage, but routes can be added
        // and dropped freely, and each new one costs a call. This is the hard stop that keeps a
        // burst of churn from exhausting the monthly quota.
        long callsThisMonth = historyRepository.countByCheckedAtAfter(startOfMonth());
        if (callsThisMonth >= monthlyCallBudget) {
            log.warn("Monthly price-provider budget spent ({}/{}). Route {} is not refreshed this cycle.",
                    callsThisMonth, monthlyCallBudget, route.getId());
            return null;
        }

        try {
            FlightPriceRequest apiRequest = new FlightPriceRequest(
                    request.getOrigin(),
                    request.getDestination(),
                    LocalDate.parse(request.getDepartureDate()),
                    request.getReturnDate() != null ? LocalDate.parse(request.getReturnDate()) : null
            );

            FlightPriceResponse response = flightPriceProvider.getFlightPrice(apiRequest);
            BigDecimal price = response.price();
            log.info("✅ Price found: {} {}", price, response.currency());

            historyRepository.save(new FlightPriceHistory(route, price));
            lastFailureByRoute.remove(route.getId());
            return price;

        } catch (Exception e) {
            // A provider outage or an exhausted quota must not break the pipeline: log it, back
            // off for a cache window, and try again later.
            log.error("Price provider failed for route {}: {}", route.getId(), e.getMessage());
            lastFailureByRoute.put(route.getId(), LocalDateTime.now());
            return null;
        }
    }

    private static LocalDateTime startOfMonth() {
        return LocalDateTime.now()
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay();
    }
}
