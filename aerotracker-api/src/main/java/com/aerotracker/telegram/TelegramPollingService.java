package com.aerotracker.telegram;

import com.aerotracker.telegram.dto.TelegramUpdate;
import com.aerotracker.telegram.dto.TelegramUpdatesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelegramPollingService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramPollingService.class);

    private final TelegramClient telegramClient;
    private final TelegramCommandContextFactory contextFactory;
    private final TelegramCommandDispatcher commandDispatcher;
    private final TelegramProperties properties;

    private Long lastUpdateId = 0L;

    public TelegramPollingService(TelegramClient telegramClient,
                                  TelegramCommandContextFactory contextFactory,
                                  TelegramCommandDispatcher commandDispatcher,
                                  TelegramProperties properties) {
        this.telegramClient = telegramClient;
        this.contextFactory = contextFactory;
        this.commandDispatcher = commandDispatcher;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${telegram.polling.interval:1000}")
    public void pollUpdates() {
        try {
            Long offset = lastUpdateId == 0L ? null : lastUpdateId + 1;
            TelegramUpdatesResponse response = telegramClient.getUpdates(offset, properties.pollingTimeout());

            if (response != null && response.ok() && response.result() != null) {
                List<TelegramUpdate> updates = response.result();
                for (TelegramUpdate update : updates) {
                    processUpdate(update);
                    if (update.updateId() != null) {
                        lastUpdateId = Math.max(lastUpdateId, update.updateId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error polling Telegram updates: {}", e.getMessage());
        }
    }

    private void processUpdate(TelegramUpdate telegramUpdate) {
        contextFactory.from(telegramUpdate)
                .ifPresent(context -> {
                    try {
                        String responseText = commandDispatcher.dispatch(context);
                        logger.info(">>> TELEGRAM RESPONSE (Chat {}):\n{}", context.chatId(), responseText);
                        telegramClient.sendMessage(context.chatId(), responseText);
                    } catch (Exception e) {
                        logger.error("Failed to process Telegram command for chatId {}: {}", context.chatId(), e.getMessage(), e);
                    }
                });
    }
}
