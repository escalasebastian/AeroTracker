package com.aerotracker.common.event;

public class PriceCheckEvent {

    private Long chatId;
    private String origin;
    private String destination;
    private String departureDate;
    private String returnDate; // Can be null for one-way flights

    // Default constructor is REQUIRED by Jackson (Spring's JSON converter) for deserialization
    public PriceCheckEvent() {
    }

    public PriceCheckEvent(Long chatId, String origin, String destination, String departureDate, String returnDate) {
        this.chatId = chatId;
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }
}