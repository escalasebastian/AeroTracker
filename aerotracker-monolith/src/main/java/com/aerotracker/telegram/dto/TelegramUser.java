package com.aerotracker.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUser(
        Long id,
        @JsonProperty("is_bot") boolean isBot,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        @JsonProperty("language_code") String languageCode
) {
}
