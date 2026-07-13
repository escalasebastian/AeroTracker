package com.aerotracker.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Represents a unique flight route between an origin and destination on specific dates.
 * This entity is normalized so multiple user subscriptions can share and track the same route.
 */
@Entity
@Table(
        name = "routes",
        // Unique constraint: We should never have two identical rows for the same flight route/dates
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_route_flight_details",
                        columnNames = {"origin", "destination", "departure_date", "return_date"}
                )
        }
)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin", nullable = false, length = 3)
    private String origin;

    @Column(name = "destination", nullable = false, length = 3)
    private String destination;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    /**
     * Optional return date for round-trip flights. Null for one-way flights.
     */
    @Column(name = "return_date")
    private LocalDate returnDate;

    // 1. JPA no-args constructor
    protected Route() {
    }

    // 2. Convenience constructor for creating new routes
    public Route(String origin, String destination, LocalDate departureDate, LocalDate returnDate) {
        this.origin = origin.toUpperCase();
        this.destination = destination.toUpperCase();
        this.departureDate = departureDate;
        this.returnDate = returnDate;
    }

    public Long getId() {
        return id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin != null ? origin.toUpperCase() : null;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination != null ? destination.toUpperCase() : null;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}