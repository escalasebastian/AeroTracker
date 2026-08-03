package com.aerotracker.pricechecker.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Entry queue
    public static final String PRICE_CHECK_REQUESTS_QUEUE = "price-check-requests-queue";

    @Bean
    public Queue priceCheckRequestsQueue() {
        return new Queue(PRICE_CHECK_REQUESTS_QUEUE, true);
    }
}