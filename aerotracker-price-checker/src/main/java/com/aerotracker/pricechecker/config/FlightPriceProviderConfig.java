package com.aerotracker.pricechecker.config;

import com.aerotracker.common.provider.FlightPriceProvider;
import com.aerotracker.common.provider.MockFlightPriceProvider;
import com.aerotracker.pricechecker.provider.SerpApiFlightPriceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chooses the price source for the tracking pipeline.
 * <p>
 * Wiring is explicit rather than annotation-driven because two implementations exist and
 * each service picks its own: the API service always answers /price with simulated data,
 * while this service prices tracked routes against a real provider whenever a key is set.
 */
@Configuration
public class FlightPriceProviderConfig {

    private static final Logger log = LoggerFactory.getLogger(FlightPriceProviderConfig.class);

    @Bean
    public FlightPriceProvider flightPriceProvider(@Value("${serpapi.api-key:}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("No SerpApi key configured (serpapi.api-key). Falling back to simulated prices: "
                    + "alerts will NOT reflect real fares.");
            return new MockFlightPriceProvider();
        }

        log.info("SerpApi key detected. Tracked routes will be priced against real Google Flights data.");
        return new SerpApiFlightPriceProvider(apiKey);
    }
}
