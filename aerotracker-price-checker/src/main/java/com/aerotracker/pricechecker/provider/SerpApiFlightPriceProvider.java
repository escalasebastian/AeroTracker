package com.aerotracker.pricechecker.provider;

import com.aerotracker.common.provider.FlightPriceProvider;
import com.aerotracker.common.provider.FlightPriceRequest;
import com.aerotracker.common.provider.FlightPriceResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Queries real flight prices from Google Flights through SerpApi.
 * <p>
 * The free plan allows a few hundred searches per month, so callers are expected to
 * rate-limit themselves: the price checker only reaches this provider when its cached
 * price for a route has gone stale.
 */
public class SerpApiFlightPriceProvider implements FlightPriceProvider {

    private static final Logger log = LoggerFactory.getLogger(SerpApiFlightPriceProvider.class);

    private static final String BASE_URL = "https://serpapi.com/search.json";
    private static final String CURRENCY = "EUR";

    /** Google Flights search types, as defined by the SerpApi engine. */
    private static final String TYPE_ROUND_TRIP = "1";
    private static final String TYPE_ONE_WAY = "2";

    private final RestClient restClient;
    private final String apiKey;

    public SerpApiFlightPriceProvider(String apiKey) {
        this.apiKey = apiKey;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(20));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public boolean isMetered() {
        return true;
    }

    @Override
    public FlightPriceResponse getFlightPrice(FlightPriceRequest request) {
        boolean roundTrip = request.returnDate() != null;

        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("engine", "google_flights")
                .queryParam("departure_id", request.origin().toUpperCase(Locale.ROOT))
                .queryParam("arrival_id", request.destination().toUpperCase(Locale.ROOT))
                .queryParam("outbound_date", request.departureDate().toString())
                .queryParam("type", roundTrip ? TYPE_ROUND_TRIP : TYPE_ONE_WAY)
                .queryParam("currency", CURRENCY)
                .queryParam("hl", "en")
                .queryParam("api_key", apiKey);

        if (roundTrip) {
            uri.queryParam("return_date", request.returnDate().toString());
        }

        // Deliberately logged without the URI: it carries the API key.
        log.info("Querying SerpApi for {} -> {} on {}{}",
                request.origin(), request.destination(), request.departureDate(),
                roundTrip ? " returning " + request.returnDate() : " (one way)");

        URI endpoint = uri.build().toUri();
        JsonNode body = restClient.get()
                .uri(endpoint)
                .retrieve()
                .body(JsonNode.class);

        if (body == null) {
            throw new IllegalStateException("SerpApi returned an empty response");
        }

        JsonNode error = body.path("error");
        if (!error.isMissingNode() && !error.isNull()) {
            // Covers an exhausted quota and an invalid key, among others.
            throw new IllegalStateException("SerpApi reported an error: " + error.asText());
        }

        BigDecimal price = lowestPrice(body);
        if (price == null) {
            throw new IllegalStateException(
                    "SerpApi returned no price for " + request.origin() + "-" + request.destination());
        }

        return new FlightPriceResponse(
                request.origin().toUpperCase(Locale.ROOT),
                request.destination().toUpperCase(Locale.ROOT),
                request.departureDate(),
                request.returnDate(),
                price,
                CURRENCY
        );
    }

    /**
     * Picks the cheapest quoted itinerary. SerpApi splits results between a curated
     * "best_flights" list and the remaining "other_flights", and either may be absent,
     * so both are scanned before falling back to the summary in "price_insights".
     */
    private BigDecimal lowestPrice(JsonNode body) {
        BigDecimal lowest = null;

        for (String section : List.of("best_flights", "other_flights")) {
            JsonNode itineraries = body.path(section);
            if (!itineraries.isArray()) {
                continue;
            }
            for (JsonNode itinerary : itineraries) {
                JsonNode price = itinerary.path("price");
                if (price.isNumber()) {
                    BigDecimal candidate = price.decimalValue();
                    if (lowest == null || candidate.compareTo(lowest) < 0) {
                        lowest = candidate;
                    }
                }
            }
        }

        if (lowest == null) {
            JsonNode insight = body.path("price_insights").path("lowest_price");
            if (insight.isNumber()) {
                lowest = insight.decimalValue();
            }
        }

        return lowest;
    }
}
