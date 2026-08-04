package com.aerotracker.notification.listener;

import com.aerotracker.common.event.PriceAlertEvent;
import com.aerotracker.notification.config.RabbitMQConfig;
import com.aerotracker.notification.telegram.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PriceAlertListener {

    private static final Logger log = LoggerFactory.getLogger(PriceAlertListener.class);
    private final TelegramClient telegramClient;

    public PriceAlertListener(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @RabbitListener(queues = RabbitMQConfig.PRICE_ALERTS_QUEUE)
    public void receiveMessage(PriceAlertEvent alert) {
        log.info("🔔 NEW ALERT RECEIVED for chat {}: Route {}", alert.getChatId(), alert.getRouteDescription());

        String telegramText = String.format("✈️ Good news!\nThe flight %s has dropped to %.2f %s.",
                alert.getRouteDescription(), alert.getCurrentPrice(), alert.getCurrency());

        try {
            telegramClient.sendMessage(alert.getChatId(), telegramText);
            log.info("✅ Message successfully sent to Telegram!");
        } catch (Exception e) {
            log.error("❌ Failed to send message to Telegram for chat ID: {}", alert.getChatId(), e);
        }
    }
}