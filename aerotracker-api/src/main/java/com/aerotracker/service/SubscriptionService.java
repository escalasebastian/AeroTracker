package com.aerotracker.service;

import com.aerotracker.entity.Route;
import com.aerotracker.entity.Subscription;
import com.aerotracker.entity.User;
import com.aerotracker.exception.RouteLimitExceededException;
import com.aerotracker.repository.RouteRepository;
import com.aerotracker.repository.SubscriptionRepository;
import com.aerotracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Core business domain service managing flight tracking subscriptions.
 * Coordinates User registration, Route normalization, and ACID transactions.
 */
@Service
public class SubscriptionService {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final long maxActiveRoutes;

    public SubscriptionService(UserRepository userRepository,
                               RouteRepository routeRepository,
                               SubscriptionRepository subscriptionRepository,
                               @Value("${aerotracker.tracking.max-active-routes:8}") long maxActiveRoutes) {
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.maxActiveRoutes = maxActiveRoutes;
    }

    /**
     * Tracks a flight route for a user with a desired target price.
     * Everything runs inside a single database ACID transaction.
     *
     * @return The created or updated Subscription entity
     */
    @Transactional
    public Subscription trackFlight(Long telegramUserId,
                                    String username,
                                    String origin,
                                    String destination,
                                    LocalDate departureDate,
                                    LocalDate returnDate,
                                    BigDecimal targetPrice) {

        // 1. Find existing User or create & persist a new one automatically
        User user = userRepository.findByTelegramUserId(telegramUserId)
                .orElseGet(() -> userRepository.save(new User(telegramUserId, username)));

        // 2. Find existing normalized Route or create & persist a new one
        Route route;
        if (returnDate != null) {
            route = routeRepository.findByOriginAndDestinationAndDepartureDateAndReturnDate(
                    origin, destination, departureDate, returnDate
            ).orElseGet(() -> routeRepository.save(new Route(origin, destination, departureDate, returnDate)));
        } else {
            route = routeRepository.findByOriginAndDestinationAndDepartureDateAndReturnDateIsNull(
                    origin, destination, departureDate
            ).orElseGet(() -> routeRepository.save(new Route(origin, destination, departureDate, null)));
        }

        // 3. Guard the price provider quota. Every distinct monitored route is priced separately,
        // so only a route nobody is tracking yet counts against the limit. Because this method is
        // transactional, rejecting here also rolls back a Route that was just inserted above.
        if (!subscriptionRepository.existsByRouteIdAndActiveTrue(route.getId())
                && subscriptionRepository.countDistinctActiveRoutes() >= maxActiveRoutes) {
            throw new RouteLimitExceededException(maxActiveRoutes);
        }

        // 4. Reuse the user's subscription to this route if one exists, active or cancelled.
        // /untrack only deactivates rows and (user_id, route_id) is unique, so inserting a new row
        // after a cancellation would break that constraint.
        Optional<Subscription> existingSubscription = subscriptionRepository
                .findByUserIdAndRouteId(user.getId(), route.getId());

        if (existingSubscription.isPresent()) {
            // Update the target price, and reactivate the alert if it had been cancelled
            Subscription sub = existingSubscription.get();
            sub.setTargetPrice(targetPrice);
            sub.setActive(true);
            return subscriptionRepository.save(sub);
        }

        // Otherwise, create and persist a brand new tracking subscription
        Subscription newSubscription = new Subscription(user, route, targetPrice);
        return subscriptionRepository.save(newSubscription);
    }

    /**
     * Retrieves all active tracking subscriptions for a specific Telegram user.
     * Uses our custom property traversal query method from SubscriptionRepository.
     * Notice readOnly = true: Optimizes Hibernate performance by disabling dirty checking.
     */
    @Transactional(readOnly = true)
    public List<Subscription> getActiveSubscriptions(Long telegramUserId) {
        return subscriptionRepository.findByUserTelegramUserIdAndActiveTrue(telegramUserId);
    }

    /**
     * Deactivates a tracking subscription (soft delete).
     *
     * @return true if successfully untracked, false if subscription not found or unauthorized
     */
    @Transactional
    public boolean untrackFlight(Long telegramUserId, Long subscriptionId) {
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);

        if (subscriptionOpt.isPresent()) {
            Subscription sub = subscriptionOpt.get();
            // Security & ownership check: Ensure the subscription actually belongs to the requesting user!
            if (sub.getUser().getTelegramUserId().equals(telegramUserId) && sub.getActive()) {
                sub.setActive(false); // Soft delete: Keep record in DB for historical analytics, but mark inactive
                subscriptionRepository.save(sub);
                return true;
            }
        }
        return false;
    }
}