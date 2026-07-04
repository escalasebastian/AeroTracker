package com.aerotracker.telegram;

import com.aerotracker.telegram.dto.TelegramUpdate;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class TelegramCommandContextFactory {

    /**
     * Attempts to convert an incoming TelegramUpdate into a TelegramCommandContext.
     *
     * @param update The DTO received from the Telegram webhook.
     * @return Optional containing the command context if the message contains valid text;
     * Optional.empty() if null or if it lacks text (e.g., stickers, photos, audio).
     */
    public Optional<TelegramCommandContext> from(TelegramUpdate update) {
        // 1. Safety validation: if there is no message or no text, it cannot be processed as a command
        if (update == null || update.message() == null || update.message().text() == null) {
            return Optional.empty();
        }

        Long chatId = update.message().chat().id();
        String messageText = update.message().text();

        // 2. Extract Locale from languageCode (using the new camelCase getter)
        Locale locale = Locale.ENGLISH; // Default fallback language
        if (update.message().from() != null && update.message().from().languageCode() != null) {
            locale = Locale.forLanguageTag(update.message().from().languageCode());
        }

        // 3. Tokenize and clean up the message text
        String trimmedText = messageText.trim();
        String[] tokens = trimmedText.split("\\s+");
        String command = tokens[0];

        // 4. Build the immutable command context
        TelegramCommandContext context = new TelegramCommandContext(
                chatId,
                messageText,
                command,
                tokens,
                locale
        );

        return Optional.of(context);
    }
}