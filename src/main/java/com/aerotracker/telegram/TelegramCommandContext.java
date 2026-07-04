package com.aerotracker.telegram;

import java.util.Locale;

public record TelegramCommandContext(
        Long chatId,
        String rawText,
        String command,
        String[] tokens,
        Locale locale
) {
}