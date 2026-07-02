package com.aerotracker.telegram.dto;

public record TelegramMessage(
        Long message_id,
        TelegramUser from,
        TelegramChat chat,
        String text
) {
}