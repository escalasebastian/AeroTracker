package com.aerotracker.common.provider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Simulated price source. Produces a plausible, route-stable price without
 * calling any external API, so it consumes no third-party quota.
 * <p>
 * Deliberately not annotated with @Component: each service declares it as an
 * explicit bean when it wants it, which keeps the price checker free to wire a
 * real provider instead without any bean ambiguity.
 */
public class MockFlightPriceProvider implements FlightPriceProvider {

    private final Random random = new Random();

    @Override
    public FlightPriceResponse getFlightPrice(FlightPriceRequest request) {

        // 1. Calculate a stable "base price" for this specific route using a hash of the origin and destination.
        // This ensures MAD-AMS always hovers around the same average price.
        int hash = Math.abs((request.origin() + request.destination()).hashCode());
        double basePrice = 80.0 + (hash % 200); // Base price between 80€ and 280€

        double finalPrice;

        // 2. Simulate market behavior and price drops
        int chance = random.nextInt(100);

        if (chance < 20) {
            // 20% chance --> FLASH SALE! Price drops by 30% - 50%.
            // This sudden drop is what will trigger our Telegram alerts during testing.
            double dropPercentage = 0.30 + (0.20 * random.nextDouble());
            finalPrice = basePrice * (1.0 - dropPercentage);
        } else {
            // 80% chance --> Normal, boring market fluctuation (+- 5% of base price)
            double fluctuation = 0.95 + (0.10 * random.nextDouble());
            finalPrice = basePrice * fluctuation;
        }

        BigDecimal price = BigDecimal.valueOf(finalPrice)
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
