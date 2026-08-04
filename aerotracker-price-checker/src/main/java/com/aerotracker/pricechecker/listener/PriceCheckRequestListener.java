package com.aerotracker.pricechecker.listener;

import com.aerotracker.common.event.PriceAlertEvent;
import com.aerotracker.common.event.PriceCheckEvent;
import com.aerotracker.pricechecker.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PriceCheckRequestListener {

    private static final Logger log = LoggerFactory.getLogger(PriceCheckRequestListener.class);
    private final RabbitTemplate rabbitTemplate;

    public PriceCheckRequestListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // We receive the JSON object directly, automatically deserialized by Jackson
    @RabbitListener(queues = RabbitMQConfig.PRICE_CHECK_REQUESTS_QUEUE)
    public void handlePriceCheckRequest(PriceCheckEvent request) {
        log.info("🔍 Received price check request for: {} to {}", request.getOrigin(), request.getDestination());

        // Simulate a 2-second delay for querying the external API
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("✅ Price found. Sending alert to Notification Service...");

        // Create the outgoing event, preserving the original Chat ID so the Notification Service knows who to message
        String routeDescription = request.getOrigin() + "-" + request.getDestination();
        PriceAlertEvent alertEvent = new PriceAlertEvent(request.getChatId(), routeDescription, 50.0, "EUR");

        // Publish the event to the next queue
        rabbitTemplate.convertAndSend("price-alerts-queue", alertEvent);
    }
}