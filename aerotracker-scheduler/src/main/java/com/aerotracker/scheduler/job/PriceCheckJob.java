package com.aerotracker.scheduler.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceCheckJob {
    private static final Logger log = LoggerFactory.getLogger(PriceCheckJob.class);
    private final RabbitTemplate rabbitTemplate;

    public PriceCheckJob(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // 10,000 milliseconds 10 seconds
    @Scheduled(fixedRate = 10000)
    public void triggerPriceChecks() {
        log.info("⏰ Scheduler waking up... Generating price check request.");

        // Mock route for testing. Later we will fetch active routes from the database.
        String route = "MAD-JFK 2026-12-25";

        // Send the message to the Price Checker's input queue
        rabbitTemplate.convertAndSend("price-check-requests-queue", route);
    }
}