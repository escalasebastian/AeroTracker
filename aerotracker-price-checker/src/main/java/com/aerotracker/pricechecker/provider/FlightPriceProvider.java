package com.aerotracker.pricechecker.provider;

import com.aerotracker.pricechecker.dto.FlightPriceRequest;
import com.aerotracker.pricechecker.dto.FlightPriceResponse;

public interface FlightPriceProvider {

    /**
     * Obtain flight price based on the request parameters
     */
    FlightPriceResponse getFlightPrice(FlightPriceRequest request);
}
