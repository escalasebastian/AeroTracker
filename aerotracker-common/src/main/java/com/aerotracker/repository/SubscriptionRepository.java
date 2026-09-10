package com.aerotracker.repository;

import com.aerotracker.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Finds all active subscriptions for a given user ID.
     * SELECT * FROM subscriptions WHERE user_id = ? AND active = true
     */
    List<Subscription> findByUserIdAndActiveTrue(Long userId);

    /**
     * Finds all active subscriptions directly by the Telegram User ID!
     * 'UserTelegramUserId' Spring Data navigates from Subscription -> User -> telegramUserId
     * It automatically generates an SQL JOIN under the hood:
     * SELECT s.* FROM subscriptions s JOIN users u ON s.user_id = u.id WHERE u.telegram_user_id = ? AND s.active = true
     */
    // The route is fetched in the same query because callers read it after the transaction has
    // closed, where touching a lazy proxy throws LazyInitializationException. The explicit order
    // matters too: /untrack addresses alerts by their position in /list, so it must be stable.
    @Query("SELECT s FROM Subscription s JOIN FETCH s.route "
            + "WHERE s.user.telegramUserId = :telegramUserId AND s.active = true ORDER BY s.id")
    List<Subscription> findByUserTelegramUserIdAndActiveTrue(@Param("telegramUserId") Long telegramUserId);

    /**
     * Checks if a user is already actively subscribing to a specific route.
     * Used to prevent duplicate alerts.
     */
    Optional<Subscription> findByUserIdAndRouteIdAndActiveTrue(Long userId, Long routeId);

    /**
     * Finds all active subscriptions across ALL users.
     * Uses JOIN FETCH to eagerly load the associated Route and User entities in a single SQL query.
     * This prevents the "N+1 select problem" when the scheduler iterates through subscriptions.
     */
    @Query("SELECT s FROM Subscription s JOIN FETCH s.route JOIN FETCH s.user WHERE s.active = true")
    List<Subscription> findAllActiveWithRouteAndUser();

    /**
     * Checks whether anyone is already tracking this route.
     * A route that is already being monitored costs no additional price lookups, so extra
     * subscribers may always join it.
     */
    boolean existsByRouteIdAndActiveTrue(Long routeId);

    /**
     * Counts how many distinct routes are currently being monitored.
     * This is the figure that drives external price-API usage, since every distinct route is
     * priced independently while all of its subscribers share that single lookup.
     */
    @Query("SELECT COUNT(DISTINCT s.route.id) FROM Subscription s WHERE s.active = true")
    long countDistinctActiveRoutes();
}