package com.smarttravel.modules.booking.scheduler;

import com.smarttravel.modules.booking.service.BookingExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background periodic scheduler scanning for unpaid expired bookings and triggering seat releases.
 */
@Component
@ConditionalOnProperty(prefix = "smarttravel.booking.expiration", name = "enabled", havingValue = "true")
public class BookingExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingExpirationScheduler.class);

    private final BookingExpirationService expirationService;

    public BookingExpirationScheduler(BookingExpirationService expirationService) {
        this.expirationService = expirationService;
    }

    @Scheduled(fixedDelayString = "${smarttravel.booking.expiration.fixed-delay-ms:60000}")
    public void runBookingExpiration() {
        log.debug("Executing scheduled booking expiration check...");
        try {
            int expiredCount = expirationService.expireOverdueBookings();
            if (expiredCount > 0) {
                log.info("Scheduled expiration run finished: {} bookings expired", expiredCount);
            }
        } catch (Exception ex) {
            log.error("Unexpected error in BookingExpirationScheduler", ex);
        }
    }
}
