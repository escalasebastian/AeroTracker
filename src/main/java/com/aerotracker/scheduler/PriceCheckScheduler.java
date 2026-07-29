package com.aerotracker.scheduler;

import com.aerotracker.service.PriceMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Executes the price monitoring process automatically on a schedule.
 */
@Component
public class PriceCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceCheckScheduler.class);

    private final PriceMonitoringService priceMonitoringService;

    public PriceCheckScheduler(PriceMonitoringService priceMonitoringService) {
        this.priceMonitoringService = priceMonitoringService;
    }

    /**
     * Runs periodically based on the interval configured in application.yaml.
     * Default fallback is 30 minutes (1800000 milliseconds).
     */
    @Scheduled(fixedRateString = "${aerotracker.scheduler.interval-ms:1800000}")
    public void scheduledPriceCheck() {
        log.info("--- Starting scheduled price check ---");
        priceMonitoringService.checkAllSubscriptions();
        log.info("--- Scheduled price check completed ---");
    }
}