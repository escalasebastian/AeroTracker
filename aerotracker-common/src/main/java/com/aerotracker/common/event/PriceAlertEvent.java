package com.aerotracker.common.event;

public class PriceAlertEvent {

    private Long chatId;
    private String routeDescription; // Example: "MAD-AMS"
    private double currentPrice;
    private String currency; // Example: "EUR"

    // Default constructor REQUIRED by Jackson
    public PriceAlertEvent() {
    }

    public PriceAlertEvent(Long chatId, String routeDescription, double currentPrice, String currency) {
        this.chatId = chatId;
        this.routeDescription = routeDescription;
        this.currentPrice = currentPrice;
        this.currency = currency;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getRouteDescription() {
        return routeDescription;
    }

    public void setRouteDescription(String routeDescription) {
        this.routeDescription = routeDescription;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}