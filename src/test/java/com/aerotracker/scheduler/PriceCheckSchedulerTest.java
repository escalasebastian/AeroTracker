package com.aerotracker.scheduler;

import com.aerotracker.service.PriceMonitoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the PriceCheckScheduler.
 * Ensures that the scheduling wrapper correctly delegates execution to the business service.
 */
@ExtendWith(MockitoExtension.class)
class PriceCheckSchedulerTest {

    // Mock the underlying service to isolate the scheduler's behavior
    @Mock
    private PriceMonitoringService priceMonitoringService;

    @InjectMocks
    private PriceCheckScheduler priceCheckScheduler;

    @Test
    void shouldCallPriceMonitoringServiceWhenScheduledTaskRuns() {
        // Arrange
        // No setup is required since we only need to verify interaction with the mock

        // Act
        // Simulate the Spring framework invoking the @Scheduled method
        priceCheckScheduler.scheduledPriceCheck();

        // Assert
        // Verify the scheduler fulfills its single responsibility: delegating to the service
        verify(priceMonitoringService, times(1)).checkAllSubscriptions();
    }
}