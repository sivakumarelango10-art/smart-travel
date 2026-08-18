package com.smarttravel.modules.booking.scheduler;

import com.smarttravel.modules.booking.service.BookingExpirationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingExpirationSchedulerTest {

    @Mock
    private BookingExpirationService expirationService;

    @InjectMocks
    private BookingExpirationScheduler scheduler;

    @Test
    @DisplayName("Scheduled run triggers expireOverdueBookings")
    void testRunBookingExpiration_Success() {
        when(expirationService.expireOverdueBookings()).thenReturn(5);

        scheduler.runBookingExpiration();

        verify(expirationService).expireOverdueBookings();
    }

    @Test
    @DisplayName("Scheduler catches and isolates unexpected exceptions gracefully")
    void testRunBookingExpiration_CatchesExceptions() {
        doThrow(new RuntimeException("Scheduler internal error")).when(expirationService).expireOverdueBookings();

        // Should not throw
        scheduler.runBookingExpiration();

        verify(expirationService).expireOverdueBookings();
    }
}
