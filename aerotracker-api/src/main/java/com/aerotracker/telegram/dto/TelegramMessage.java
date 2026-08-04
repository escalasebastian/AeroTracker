package com.aerotracker.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramMessage(
        @JsonProperty("message_id") Long messageId,
        TelegramUser from,
        TelegramChat chat,
        String text
) {
}