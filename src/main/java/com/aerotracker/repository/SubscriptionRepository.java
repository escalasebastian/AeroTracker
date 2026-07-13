package com.aerotracker.repository;

import com.aerotracker.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<Subscription> findByUserTelegramUserIdAndActiveTrue(Long telegramUserId);

    /**
     * Checks if a user is already actively subscribing to a specific route.
     * Used to prevent duplicate alerts.
     */
    Optional<Subscription> findByUserIdAndRouteIdAndActiveTrue(Long userId, Long routeId);
}