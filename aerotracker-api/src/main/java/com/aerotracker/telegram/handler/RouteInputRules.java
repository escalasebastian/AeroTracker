package com.aerotracker.telegram.handler;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Input rules shared by the commands that take a route, checked before anything is priced or stored.
 * <p>
 * The bot is open to anyone, so malformed input must be rejected with a clear message instead of
 * reaching the price providers, where it would waste metered calls, or the database, where it would
 * break a column constraint and surface as an unexpected error.
 * Each method returns the key of the message describing the problem, or empty when the input is valid.
 */
final class RouteInputRules {

    // IATA airport codes are exactly three letters; routes store them upper-cased in VARCHAR(3) columns
    private static final Pattern AIRPORT_CODE = Pattern.compile("[A-Za-z]{3}");

    // Far above any real fare and well within the NUMERIC(10, 2) target_price column
    private static final BigDecimal MAX_TARGET_PRICE = new BigDecimal("100000");

    private RouteInputRules() {
    }

    static Optional<String> checkAirports(String origin, String destination) {
        if (!AIRPORT_CODE.matcher(origin).matches() || !AIRPORT_CODE.matcher(destination).matches()) {
            return Optional.of("telegram.error.invalid-airport");
        }
        if (origin.equalsIgnoreCase(destination)) {
            return Optional.of("telegram.error.same-airport");
        }
        return Optional.empty();
    }

    static Optional<String> checkTargetPrice(BigDecimal targetPrice) {
        if (targetPrice.signum() <= 0
                || targetPrice.compareTo(MAX_TARGET_PRICE) > 0
                || targetPrice.stripTrailingZeros().scale() > 2) {
            return Optional.of("telegram.track.invalid-target-price");
        }
        return Optional.empty();
    }
}
