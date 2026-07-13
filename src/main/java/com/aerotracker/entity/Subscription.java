package com.aerotracker.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a tracking subscription created by a User for a specific flight Route.
 * Links User and Route via Foreign Keys and holds tracking preferences like target price.
 */
@Entity
@Table(
        name = "subscriptions",
        // Prevent duplicate subscriptions: A user can only subscribe once to the exact same route
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subscription_user_route",
                        columnNames = {"user_id", "route_id"}
                )
        }
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-One relationship to User.
     * FetchType.LAZY is mandatory here to prevent N+1 query performance bottlenecks.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Many-to-One relationship to Route.
     * Multiple users can subscribe to the same normalized Route.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    /**
     * The maximum price the user is willing to pay.
     * When the actual price drops below this value, an alert should be triggered.
     */
    @Column(name = "target_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal targetPrice;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 1. JPA no-args constructor
    protected Subscription() {
    }

    // 2. Convenience constructor for creating new tracking subscriptions
    public Subscription(User user, Route route, BigDecimal targetPrice) {
        this.user = user;
        this.route = route;
        this.targetPrice = targetPrice;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.active == null) {
            this.active = true;
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}