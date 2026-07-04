package com.aerotracker.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUpdate(
        @JsonProperty("update_id") Long updateId,
        TelegramMessage message
) {
}
