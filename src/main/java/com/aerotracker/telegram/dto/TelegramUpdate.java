package com.aerotracker.telegram.dto;

public record TelegramUpdate(
        Long update_id,
        TelegramMessage message
) {
}
