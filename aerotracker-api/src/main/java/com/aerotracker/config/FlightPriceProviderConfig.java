package com.aerotracker.config;

import com.aerotracker.common.provider.FlightPriceProvider;
import com.aerotracker.common.provider.MockFlightPriceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Price source backing the manual /price command.
 * <p>
 * Deliberately simulated: /price is open to anyone chatting with the bot, so pointing it at a
 * metered flight API would let a handful of curious users exhaust the monthly quota that the
 * tracking pipeline depends on. Real fares are reserved for /track, whose call volume is
 * bounded by the number of tracked routes and the price cache.
 */
@Configuration
public class FlightPriceProviderConfig {

    @Bean
    public FlightPriceProvider flightPriceProvider() {
        return new MockFlightPriceProvider();
    }
}
