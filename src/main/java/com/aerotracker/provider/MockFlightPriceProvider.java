package com.aerotracker.provider;

import com.aerotracker.dto.FlightPriceRequest;
import com.aerotracker.dto.FlightPriceResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component
public class MockFlightPriceProvider implements FlightPriceProvider {

    private final Random random = new Random();

    @Override
    public FlightPriceResponse getFlightPrice(FlightPriceRequest request) {
        // We create a random mock price
        double randomPrice = 50.0 + (500.0 * random.nextDouble());

        BigDecimal price = BigDecimal.valueOf(randomPrice)
                .setScale(2, RoundingMode.HALF_UP);

        return new FlightPriceResponse(
                request.origin().toUpperCase(),
                request.destination().toUpperCase(),
                request.departureDate(),
                request.returnDate(),
                price,
                "EUR"
        );
    }
}
