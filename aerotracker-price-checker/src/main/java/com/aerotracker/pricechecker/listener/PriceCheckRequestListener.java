package com.aerotracker.pricechecker.listener;

import com.aerotracker.common.event.PriceAlertEvent;
import com.aerotracker.common.event.PriceCheckEvent;
import com.aerotracker.entity.FlightPriceHistory;
import com.aerotracker.entity.Route;
import com.aerotracker.pricechecker.config.RabbitMQConfig;
import com.aerotracker.pricechecker.dto.FlightPriceRequest;
import com.aerotracker.pricechecker.dto.FlightPriceResponse;
import com.aerotracker.pricechecker.provider.FlightPriceProvider;
import com.aerotracker.repository.FlightPriceHistoryRepository;
import com.aerotracker.repository.RouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
public class PriceCheckRequestListener {

    private static final Logger log = LoggerFactory.getLogger(PriceCheckRequestListener.class);
    private final RabbitTemplate rabbitTemplate;
    private final FlightPriceProvider flightPriceProvider;
    private final FlightPriceHistoryRepository historyRepository;
    private final RouteRepository routeRepository;

    public PriceCheckRequestListener(RabbitTemplate rabbitTemplate,
                                     FlightPriceProvider flightPriceProvider,
                                     FlightPriceHistoryRepository historyRepository,
                                     RouteRepository routeRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.flightPriceProvider = flightPriceProvider;
        this.historyRepository = historyRepository;
        this.routeRepository = routeRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.PRICE_CHECK_REQUESTS_QUEUE)
    @Transactional
    public void handlePriceCheckRequest(PriceCheckEvent request) {
        log.info("🔍 Received price check request for Route ID: {} ({} to {})",
                request.getRouteId(), request.getOrigin(), request.getDestination());

        // 1. Query the external API (MockProvider for now)
        FlightPriceRequest apiRequest = new FlightPriceRequest(
                request.getOrigin(),
                request.getDestination(),
                LocalDate.parse(request.getDepartureDate()),
                request.getReturnDate() != null ? LocalDate.parse(request.getReturnDate()) : null
        );
        FlightPriceResponse response = flightPriceProvider.getFlightPrice(apiRequest);
        BigDecimal currentPrice = response.price();
        log.info("✅ Price found: {} {}", currentPrice, response.currency());

        // 2. Save the price history to the database
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + request.getRouteId()));

        FlightPriceHistory history = new FlightPriceHistory(route, currentPrice);
        historyRepository.save(history);

        // 3. Process the "backpack" of subscribers
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
                PriceAlertEvent alertEvent = new PriceAlertEvent(chatId, routeDescription, currentPrice.doubleValue(), response.currency());
                rabbitTemplate.convertAndSend("price-alerts-queue", alertEvent);
                alertsSent++;
            }
        }

        log.info("Finished processing route. Sent {} alerts out of {} total subscribers.", alertsSent, subscriptions.size());
    }
}