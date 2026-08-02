package com.aerotracker.service;

import com.aerotracker.dto.FlightPriceRequest;
import com.aerotracker.dto.FlightPriceResponse;
import com.aerotracker.entity.FlightPriceHistory;
import com.aerotracker.entity.Route;
import com.aerotracker.entity.Subscription;
import com.aerotracker.entity.User;
import com.aerotracker.provider.FlightPriceProvider;
import com.aerotracker.repository.FlightPriceHistoryRepository;
import com.aerotracker.repository.SubscriptionRepository;
import com.aerotracker.telegram.TelegramClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyLong;

/**
 * Unit tests for PriceMonitoringService.
 *
 * @ExtendWith(MockitoExtension.class) enables Mockito without loading the whole Spring Boot application.
 */
@ExtendWith(MockitoExtension.class)
class PriceMonitoringServiceTest {

    // --- MOCKS (Fake Dependencies) ---
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private FlightPriceProvider flightPriceProvider;

    @Mock
    private FlightPriceHistoryRepository flightPriceHistoryRepository;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private MessageSource messageSource;

    // --- CLASS UNDER TEST ---
    // Mockito will automatically inject the fake dependencies above into this real instance.
    @InjectMocks
    private PriceMonitoringService priceMonitoringService;


    // --- SCENARIO 1: No active subscriptions ---
    @Test
    void shouldDoNothingWhenNoActiveSubscriptionsExist() {
        // Arrange: Tell the mock repository to return an empty list
        when(subscriptionRepository.findAllActiveWithRouteAndUser())
                .thenReturn(Collections.emptyList());

        // Act: Run the scheduler logic
        priceMonitoringService.checkAllSubscriptions();

        // Assert: Verify that the provider was NEVER called (since there's nothing to check)
        verify(flightPriceProvider, never()).getFlightPrice(any());
        verify(flightPriceHistoryRepository, never()).save(any());
        verify(telegramClient, never()).sendMessage(anyLong(), anyString());
    }


    // --- SCENARIO 2: Price drops below target -> Send Alert ---
    @Test
    void shouldSendAlertWhenPriceDropsBelowTarget() {
        // Arrange
        User user = new User(12345L, "sebas");
        Route route = new Route("MAD", "AMS", LocalDate.now().plusDays(10), null);
        // User wants to pay maximum 150 euros
        Subscription subscription = new Subscription(user, route, new BigDecimal("150.00"));

        when(subscriptionRepository.findAllActiveWithRouteAndUser())
                .thenReturn(List.of(subscription));

        when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenReturn("MOCK ALERT TEXT");

        // Simulate a cheap flight from the provider (100 euros)
        FlightPriceResponse cheapResponse = new FlightPriceResponse(
                "MAD", "AMS", route.getDepartureDate(), null, new BigDecimal("100.00"), "EUR"
        );
        when(flightPriceProvider.getFlightPrice(any(FlightPriceRequest.class)))
                .thenReturn(cheapResponse);

        // Act
        priceMonitoringService.checkAllSubscriptions();

        // Assert
        // 1. Verify history was saved
        verify(flightPriceHistoryRepository, times(1)).save(any(FlightPriceHistory.class));

        // 2. Verify Telegram alert WAS sent because 100 <= 150
        verify(telegramClient, times(1)).sendMessage(eq(12345L), eq("MOCK ALERT TEXT"));
    }


    // --- SCENARIO 3: Price is too high -> No Alert ---
    @Test
    void shouldNotSendAlertWhenPriceIsAboveTarget() {
        // Arrange
        User user = new User(12345L, "sebas");
        Route route = new Route("MAD", "AMS", LocalDate.now().plusDays(10), null);
        // User wants to pay maximum 150 euros
        Subscription subscription = new Subscription(user, route, new BigDecimal("150.00"));

        when(subscriptionRepository.findAllActiveWithRouteAndUser())
                .thenReturn(List.of(subscription));

        // Simulate an expensive flight from the provider (200 euros)
        FlightPriceResponse expensiveResponse = new FlightPriceResponse(
                "MAD", "AMS", route.getDepartureDate(), null, new BigDecimal("200.00"), "EUR"
        );
        when(flightPriceProvider.getFlightPrice(any(FlightPriceRequest.class)))
                .thenReturn(expensiveResponse);

        // Act
        priceMonitoringService.checkAllSubscriptions();

        // Assert
        // 1. Verify history was still saved (we always track prices)
        verify(flightPriceHistoryRepository, times(1)).save(any(FlightPriceHistory.class));

        // 2. Verify Telegram alert was NEVER sent because 200 > 150
        verify(telegramClient, never()).sendMessage(anyLong(), anyString());
    }
}