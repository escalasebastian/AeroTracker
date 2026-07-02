package com.aerotracker.telegram.dto;

public record TelegramUser(
        Long id,
        boolean is_bot,
        String first_name,
        String last_name,
        String username,
        String language_code
) {
}
