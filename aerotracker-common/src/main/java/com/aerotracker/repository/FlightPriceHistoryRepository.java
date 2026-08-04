package com.aerotracker.repository;

import com.aerotracker.entity.FlightPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlightPriceHistoryRepository extends JpaRepository<FlightPriceHistory, Long> {

    /**
     * Finds the most recently saved price for a specific route.
     * SELECT * FROM flight_price_history WHERE route_id = ? ORDER BY checked_at DESC LIMIT 1
     */
    Optional<FlightPriceHistory> findTopByRouteIdOrderByCheckedAtDesc(Long routeId);

}