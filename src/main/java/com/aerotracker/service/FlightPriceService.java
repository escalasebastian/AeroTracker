package com.aerotracker.service;

import com.aerotracker.dto.FlightPriceRequest;
import com.aerotracker.dto.FlightPriceResponse;
import com.aerotracker.provider.FlightPriceProvider;
import org.springframework.stereotype.Service;

@Service
public class FlightPriceService {
    private final FlightPriceProvider priceProvider;

    public FlightPriceService(FlightPriceProvider priceProvider) {
        this.priceProvider = priceProvider;
    }

    public FlightPriceResponse getPrice(FlightPriceRequest request) {
        // Business logic
        if (request.returnDate() != null && request.returnDate().isBefore(request.departureDate())) {
            throw new IllegalArgumentException("The return date cannot be earlier than the departure date.");
        }

        return priceProvider.getFlightPrice(request);
    }
}
