package com.aerotracker.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks the historical prices for a specific flight route over time.
 */
@Entity
@Table(name = "flight_price_history")
public class FlightPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We link the price to the Route, not the Users Subscription.
    // If 10 users track the same route, we only need to store 1 price record per check.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "checked_at", nullable = false, updatable = false)
    private LocalDateTime checkedAt;

    // JPA requires a no-args constructor
    protected FlightPriceHistory() {
    }

    public FlightPriceHistory(Route route, BigDecimal price) {
        this.route = route;
        this.price = price;
    }

    // Automatically sets the timestamp right before inserting into the database
    @PrePersist
    protected void onCreate() {
        if (this.checkedAt == null) {
            this.checkedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }
}