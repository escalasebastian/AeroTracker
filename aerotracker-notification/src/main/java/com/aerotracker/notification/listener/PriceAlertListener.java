package com.aerotracker.notification.listener;

import com.aerotracker.notification.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PriceAlertListener {

    private static final Logger log = LoggerFactory.getLogger(PriceAlertListener.class);

    @RabbitListener(queues = RabbitMQConfig.PRICE_ALERTS_QUEUE)
    public void receiveMessage(String message) {
        log.info("🔔 NEW ALERT RECEIVED: {}", message);
        // TODO: Call Telegram API to notify the user.
        // For now, we just print the message to verify the setup.
    }
}