package com.aerotracker.notification.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PRICE_ALERTS_QUEUE = "price-alerts-queue";

    @Bean
    public Queue priceAlertsQueue() {
        // The 'true' flag means the queue is durable (survives broker restarts)
        return new Queue(PRICE_ALERTS_QUEUE, true);
    }
}