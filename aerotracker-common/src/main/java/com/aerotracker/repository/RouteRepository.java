package com.aerotracker.repository;

import com.aerotracker.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    /**
     * Finds an existing route for a round-trip flight (where returnDate is NOT null).
     * SELECT * FROM routes WHERE origin = ? AND destination = ? AND departure_date = ? AND return_date = ?
     */
    Optional<Route> findByOriginAndDestinationAndDepartureDateAndReturnDate(
            String origin,
            String destination,
            LocalDate departureDate,
            LocalDate returnDate
    );

    /**
     * Finds an existing route for a one-way flight where returnDate is null.
     * 'IsNull' keyword at the end of the method name!
     * SELECT * FROM routes WHERE origin = ? AND destination = ? AND departure_date = ? AND return_date IS NULL
     */
    Optional<Route> findByOriginAndDestinationAndDepartureDateAndReturnDateIsNull(
            String origin,
            String destination,
            LocalDate departureDate
    );
}