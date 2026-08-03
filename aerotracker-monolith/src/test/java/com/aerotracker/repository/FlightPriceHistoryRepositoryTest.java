package com.aerotracker.repository;

import com.aerotracker.entity.FlightPriceHistory;
import com.aerotracker.entity.Route;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for FlightPriceHistoryRepository.
 *
 * @DataJpaTest boots a lightweight in-memory database (H2), runs Flyway migrations,
 * and configures Spring Data JPA for testing repository layer in isolation.
 */
@DataJpaTest
class FlightPriceHistoryRepositoryTest {

    @Autowired
    private FlightPriceHistoryRepository historyRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Test
    void shouldFindTheMostRecentPriceForARoute() throws InterruptedException {
        // Arrange
        Route route = new Route("MAD", "AMS", LocalDate.now().plusDays(10), null);
        route = routeRepository.save(route);

        FlightPriceHistory oldPrice = new FlightPriceHistory(route, new BigDecimal("150.00"));
        historyRepository.save(oldPrice);

        // Brief pause to ensure the 'checkedAt' timestamp of the new price is strictly greater
        Thread.sleep(100);

        FlightPriceHistory newPrice = new FlightPriceHistory(route, new BigDecimal("120.00"));
        historyRepository.save(newPrice);

        // Act
        Optional<FlightPriceHistory> result = historyRepository.findTopByRouteIdOrderByCheckedAtDesc(route.getId());

        // Assert
        assertTrue(result.isPresent(), "Should find a price history record for the route");
        assertEquals(new BigDecimal("120.00"), result.get().getPrice(),
                "Should return the most recent price (120.00), not the older one");
    }
}