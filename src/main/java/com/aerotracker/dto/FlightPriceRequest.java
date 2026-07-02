package com.aerotracker.dto;

import java.time.LocalDate;

public record FlightPriceRequest(
        String origin,
        String destination,
        LocalDate departureDate,
        LocalDate returnDate // nullable
) {
}
