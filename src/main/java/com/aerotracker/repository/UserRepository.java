package com.aerotracker.repository;

import com.aerotracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique Telegram user ID.
     * Spring Data JPA automatically generates the SQL query:
     * SELECT * FROM users WHERE telegram_user_id = ?
     *
     * @param telegramUserId The unique identifier from the Telegram webhook update
     * @return An Optional containing the user if found, or Optional.empty() if not registered
     */
    Optional<User> findByTelegramUserId(Long telegramUserId);
}