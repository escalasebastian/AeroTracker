package com.aerotracker.provider;

import com.aerotracker.dto.FlightPriceRequest;
import com.aerotracker.dto.FlightPriceResponse;

public interface FlightPriceProvider {

    /**
     * Obtain flight price based on the request parameters
     */
    FlightPriceResponse getFlightPrice(FlightPriceRequest request);
}
