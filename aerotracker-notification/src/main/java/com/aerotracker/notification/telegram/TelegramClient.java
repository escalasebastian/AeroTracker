package com.aerotracker.notification.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TelegramClient {
    private final RestClient restClient;

    public TelegramClient(TelegramProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + properties.botToken())
                .build();
    }

    public void sendMessage(Long chatId, String text) {
        SendMessageRequest requestBody = new SendMessageRequest(chatId, text, "Markdown");
        restClient.post()
                .uri("/sendMessage")
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
    }

    private record SendMessageRequest(
            @JsonProperty("chat_id") Long chatId,
            String text,
            @JsonProperty("parse_mode") String parseMode
    ) {
    }
}
