package com.aerotracker.repository;

import com.aerotracker.entity.FlightPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FlightPriceHistoryRepository extends JpaRepository<FlightPriceHistory, Long> {

    /**
     * Finds the most recently saved price for a specific route.
     * SELECT * FROM flight_price_history WHERE route_id = ? ORDER BY checked_at DESC LIMIT 1
     */
    Optional<FlightPriceHistory> findTopByRouteIdOrderByCheckedAtDesc(Long routeId);

    /**
     * Counts price records written since the given instant.
     * <p>
     * A record is only ever written after a successful call to the external price provider —
     * reusing a cached price writes nothing — so this doubles as the ledger of real API calls
     * and lets the service stay inside its monthly quota.
     */
    long countByCheckedAtAfter(LocalDateTime timestamp);

}