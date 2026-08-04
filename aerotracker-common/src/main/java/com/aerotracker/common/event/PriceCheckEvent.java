package com.aerotracker.common.event;

import java.math.BigDecimal;
import java.util.Map;

public class PriceCheckEvent {

    private Long routeId;
    private String origin;
    private String destination;
    private String departureDate;
    private String returnDate; // Can be null for one-way flights
    
    // Key: Telegram Chat ID, Value: Target Price
    private Map<Long, BigDecimal> subscriptions;

    // Default constructor is REQUIRED by Jackson (Spring's JSON converter) for deserialization
    public PriceCheckEvent() {
    }

    public PriceCheckEvent(Long routeId, String origin, String destination, String departureDate, String returnDate, Map<Long, BigDecimal> subscriptions) {
        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.subscriptions = subscriptions;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
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

    public Map<Long, BigDecimal> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Map<Long, BigDecimal> subscriptions) {
        this.subscriptions = subscriptions;
    }
}