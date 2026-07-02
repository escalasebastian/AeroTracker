package com.aerotracker.telegram;

import com.aerotracker.telegram.dto.TelegramCommandContext;
import com.aerotracker.telegram.dto.TelegramUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(TelegramWebhookController.class);

    private final TelegramCommandService commandService;
    private final TelegramClient telegramClient;

    public TelegramWebhookController(TelegramCommandService commandService, TelegramClient telegramClient) {
        this.commandService = commandService;
        this.telegramClient = telegramClient;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveUpdate(@RequestBody TelegramUpdate telegramUpdate) {
        // Validate that the message contains text
        if (telegramUpdate != null && telegramUpdate.message() != null && telegramUpdate.message().text() != null) {
            Long chatId = telegramUpdate.message().chat().id();
            String messageText = telegramUpdate.message().text();
            
            // Extract Locale from user language_code
            Locale locale = Locale.ENGLISH; // Default fallback
            if (telegramUpdate.message().from() != null && telegramUpdate.message().from().language_code() != null) {
                locale = Locale.forLanguageTag(telegramUpdate.message().from().language_code());
            }

            // Parse command and tokens
            String trimmedText = messageText.trim();
            String[] tokens = trimmedText.split("\\s+");
            String command = tokens[0];

            TelegramCommandContext context = new TelegramCommandContext(
                    chatId,
                    messageText,
                    command,
                    tokens,
                    locale
            );

            try {
                // 1. Process the command and generate the response
                String responseText = commandService.executeCommand(context);
                // 2. Send response back to the user in Telegram
                telegramClient.sendMessage(chatId, responseText);
            } catch (Exception e) {
                logger.error("Failed to process or send Telegram message for chatId {}: {}", chatId, e.getMessage(), e);
            }
        }
        // Important: Telegram requires us to return an HTTP 200 OK state immediately.
        // If we don't (or if it takes too long), Telegram will retry sending the message repeatedly.
        return ResponseEntity.ok().build();
    }
}
