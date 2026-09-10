package com.aerotracker.common.provider;

/**
 * Abstraction over any source of flight prices.
 * <p>
 * Implementations are wired explicitly by each service rather than through
 * component scanning, so every service decides which source it uses:
 * the API service answers the manual /price command with the simulated
 * provider, while the price checker queries a real flight API.
 */
public interface FlightPriceProvider {

    /**
     * Obtain flight price based on the request parameters
     */
    FlightPriceResponse getFlightPrice(FlightPriceRequest request);

    /**
     * Whether each call consumes a paid or quota-limited external resource.
     * Callers use this to decide if a usage budget applies at all.
     */
    default boolean isMetered() {
        return false;
    }
}
