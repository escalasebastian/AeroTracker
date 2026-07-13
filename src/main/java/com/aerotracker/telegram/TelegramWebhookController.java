package com.aerotracker.telegram;

import com.aerotracker.telegram.dto.TelegramUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(TelegramWebhookController.class);

    private final TelegramCommandContextFactory contextFactory;
    private final TelegramCommandDispatcher commandDispatcher;
    private final TelegramClient telegramClient;

    public TelegramWebhookController(TelegramCommandContextFactory contextFactory,
                                     TelegramCommandDispatcher commandDispatcher,
                                     TelegramClient telegramClient) {
        this.contextFactory = contextFactory;
        this.commandDispatcher = commandDispatcher;
        this.telegramClient = telegramClient;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveUpdate(@RequestBody TelegramUpdate telegramUpdate) {
        // Try to build a valid command context; if present, process it cleanly
        contextFactory.from(telegramUpdate)
                .ifPresent(this::processCommand);

        // Important: Telegram requires an immediate HTTP 200 OK response to avoid retry loops
        return ResponseEntity.ok().build();
    }

    private void processCommand(TelegramCommandContext context) {
        try {
            // 1. Dispatch command to the appropriate handler
            String responseText = commandDispatcher.dispatch(context);

            logger.info(">>> TELEGRAM RESPONSE (Chat {}):\n{}", context.chatId(), responseText);

            // 2. Send response back to the user via Telegram API
            telegramClient.sendMessage(context.chatId(), responseText);
        } catch (Exception e) {
            logger.error("Failed to process Telegram command for chatId {}: {}", context.chatId(), e.getMessage(), e);
        }
    }
}