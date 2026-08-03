package com.aerotracker.service;

import com.aerotracker.dto.FlightPriceRequest;
import com.aerotracker.dto.FlightPriceResponse;
import com.aerotracker.entity.FlightPriceHistory;
import com.aerotracker.entity.Route;
import com.aerotracker.entity.Subscription;
import com.aerotracker.provider.FlightPriceProvider;
import com.aerotracker.repository.FlightPriceHistoryRepository;
import com.aerotracker.repository.SubscriptionRepository;
import com.aerotracker.telegram.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core business service for Phase 3.
 * Periodically checks prices and triggers alerts.
 */
@Service
public class PriceMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(PriceMonitoringService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final FlightPriceProvider flightPriceProvider;
    private final FlightPriceHistoryRepository flightPriceHistoryRepository;
    private final TelegramClient telegramClient;
    private final MessageSource messageSource;

    // Dependency Injection via constructor
    public PriceMonitoringService(
            SubscriptionRepository subscriptionRepository,
            FlightPriceProvider flightPriceProvider,
            FlightPriceHistoryRepository flightPriceHistoryRepository,
            TelegramClient telegramClient,
            MessageSource messageSource) {
        this.subscriptionRepository = subscriptionRepository;
        this.flightPriceProvider = flightPriceProvider;
        this.flightPriceHistoryRepository = flightPriceHistoryRepository;
        this.telegramClient = telegramClient;
        this.messageSource = messageSource;
    }

    /**
     * Entry point. This is NOT @Transactional
     * We don't want to hold a DB connection open while making external API calls.
     */
    public void checkAllSubscriptions() {
        List<Subscription> activeSubs = subscriptionRepository.findAllActiveWithRouteAndUser();

        if (activeSubs.isEmpty()) {
            log.info("No active subscriptions to check at the moment.");
            return;
        }

        // DEDUPLICATION is totally safe because Route has equals() based on ID
        Map<Route, List<Subscription>> subsByRoute = activeSubs.stream()
                .collect(Collectors.groupingBy(Subscription::getRoute));
        log.info("Checking prices for {} unique routes...", subsByRoute.size());
        for (Map.Entry<Route, List<Subscription>> entry : subsByRoute.entrySet()) {
            processRoute(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Orchestrates the process for a single route: query API, save history, send alerts.
     */
    private void processRoute(Route route, List<Subscription> subscribers) {
        try {
            // 1. Query external API and save snapshot in DB (small transaction)
            FlightPriceResponse response = queryAndSavePrice(route);
            BigDecimal currentPrice = response.price();
            log.info("Checked Route {} -> {}: Current price is {} {}",
                    route.getOrigin(), route.getDestination(), currentPrice, response.currency());

            // 2. Compare against user targets and send alerts (no DB transaction needed here)
            for (Subscription sub : subscribers) {
                if (currentPrice.compareTo(sub.getTargetPrice()) <= 0) {
                    sendPriceAlert(sub, currentPrice, response.currency());
                }
            }
        } catch (Exception e) {
            log.error("Failed to check price for route ID: " + route.getId(), e);
        }
    }

    /**
     * Isolated transaction just for saving to the database.
     * Transactions should be as short as possible.
     */
    @Transactional
    protected FlightPriceResponse queryAndSavePrice(Route route) {
        FlightPriceRequest request = new FlightPriceRequest(
                route.getOrigin(),
                route.getDestination(),
                route.getDepartureDate(),
                route.getReturnDate()
        );

        FlightPriceResponse response = flightPriceProvider.getFlightPrice(request);

        FlightPriceHistory history = new FlightPriceHistory(route, response.price());
        flightPriceHistoryRepository.save(history);

        return response;
    }

    private void sendPriceAlert(Subscription sub, BigDecimal currentPrice, String currency) {
        Route route = sub.getRoute();

        Locale locale = Locale.ENGLISH; // Default

        String returnLine = "";
        if (route.getReturnDate() != null) {
            returnLine = messageSource.getMessage(
                    "telegram.alert.price-drop.return-line",
                    new Object[]{route.getReturnDate()},
                    locale
            );
        }

        String message = messageSource.getMessage(
                "telegram.alert.price-drop",
                new Object[]{
                        route.getOrigin(),
                        route.getDestination(),
                        route.getDepartureDate(),
                        returnLine, // {3}, optional
                        currentPrice,
                        currency,
                        sub.getTargetPrice()
                },
                locale
        );
        telegramClient.sendMessage(sub.getUser().getTelegramUserId(), message);

        log.info("Sent alert to Telegram User {} for Route {}", sub.getUser().getTelegramUserId(), route.getId());
    }
}