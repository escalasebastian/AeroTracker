package com.aerotracker.common.provider;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FlightPriceResponse(
        String origin,
        String destination,
        LocalDate departureDate,
        LocalDate returnDate, // nullable
        BigDecimal price,
        String currency
) {
}
