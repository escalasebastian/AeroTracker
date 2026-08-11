package com.aerotracker.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.aerotracker.telegram.dto.TelegramUpdatesResponse;
@Component
public class TelegramClient {
    private final RestClient restClient;

    // Simplified constructor: we only inject properties
    // and build the RestClient using its static RestClient.builder()
    public TelegramClient(TelegramProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + properties.botToken())
                .build();
    }

    /**
     * Sends a text message to a specific Telegram chat.
     */
    public void sendMessage(Long chatId, String text) {
        SendMessageRequest requestBody = new SendMessageRequest(chatId, text, "Markdown");
        restClient.post()
                .uri("/sendMessage")
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Fetches updates from Telegram via Long Polling.
     */
    public TelegramUpdatesResponse getUpdates(Long offset, int timeout) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getUpdates")
                        .queryParam("offset", offset)
                        .queryParam("timeout", timeout)
                        .build())
                .retrieve()
                .body(TelegramUpdatesResponse.class);
    }

    private record SendMessageRequest(
            @JsonProperty("chat_id") Long chatId,
            String text,
            @JsonProperty("parse_mode") String parseMode
    ) {
    }
}
