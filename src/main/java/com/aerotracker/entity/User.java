package com.aerotracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique Telegram User ID received from the webhook.
     * Marked as unique to prevent duplicate accounts for the same Telegram user.
     */
    @Column(name = "telegram_user_id", unique = true, nullable = false)
    private Long telegramUserId;

    @Column(name = "username")
    private String username;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 1. JPA requires a no-args constructor (protected or public)
    protected User() {
    }

    // 2. Convenience constructor for creating new users in our business logic
    public User(Long telegramUserId, String username) {
        this.telegramUserId = telegramUserId;
        this.username = username;
    }

    /**
     * JPA Lifecycle callback: Automatically sets the creation timestamp
     * right before Hibernate executes the SQL INSERT statement.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(Long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}