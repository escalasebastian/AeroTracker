package com.aerotracker.scheduler.job;

import com.aerotracker.common.event.PriceCheckEvent;
import com.aerotracker.entity.Route;
import com.aerotracker.entity.Subscription;
import com.aerotracker.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PriceCheckJob {
    private static final Logger log = LoggerFactory.getLogger(PriceCheckJob.class);
    private final RabbitTemplate rabbitTemplate;
    private final SubscriptionRepository subscriptionRepository;

    public PriceCheckJob(RabbitTemplate rabbitTemplate, SubscriptionRepository subscriptionRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Scheduled(fixedRateString = "${aerotracker.scheduler.interval-ms:30000}")
    public void triggerPriceChecks() {
        log.info("⏰ Scheduler waking up... Reading active subscriptions from Database.");

        List<Subscription> activeSubs = subscriptionRepository.findAllActiveWithRouteAndUser();

        if (activeSubs.isEmpty()) {
            log.info("No active subscriptions found. Going back to sleep.");
            return;
        }

        // Group subscriptions by Route to avoid hitting the Flight API multiple times for the same flight
        Map<Route, List<Subscription>> subsByRoute = activeSubs.stream()
                .collect(Collectors.groupingBy(Subscription::getRoute));

        log.info("Found {} active routes to check.", subsByRoute.size());

        for (Map.Entry<Route, List<Subscription>> entry : subsByRoute.entrySet()) {
            Route route = entry.getKey();
            List<Subscription> subscribers = entry.getValue();

            // Build the map of Chat ID -> Target Price
            Map<Long, BigDecimal> subscriberMap = new HashMap<>();
            for (Subscription sub : subscribers) {
                Long chatId = sub.getUser().getTelegramUserId();
                BigDecimal targetPrice = sub.getTargetPrice();
                subscriberMap.put(chatId, targetPrice);
            }

            // Create the real stateless Event
            PriceCheckEvent event = new PriceCheckEvent(
                    route.getId(),
                    route.getOrigin(),
                    route.getDestination(),
                    route.getDepartureDate().toString(),
                    route.getReturnDate() != null ? route.getReturnDate().toString() : null,
                    subscriberMap
            );

            log.info("Sending PriceCheckEvent for route {}-{} with {} subscribers.", route.getOrigin(), route.getDestination(), subscriberMap.size());
            
            rabbitTemplate.convertAndSend("price-check-requests-queue", event);
        }
    }
}