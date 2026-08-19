package com.smarttravel.modules.booking.service;

import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.payment.service.PaymentStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerReliabilityTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightInventoryReservationService reservationService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStateMachine paymentStateMachine;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private com.smarttravel.modules.flight.service.SeatMapService seatMapService;

    private BookingExpirationServiceImpl expirationService;

    @BeforeEach
    void setUp() {
        expirationService = new BookingExpirationServiceImpl(
                bookingRepository,
                reservationService,
                paymentRepository,
                paymentStateMachine,
                mongoTemplate,
                seatMapService
        );
    }

    @Test
    @DisplayName("Should isolate single booking failure and continue expiring remaining overdue bookings")
    void shouldIsolateSingleBookingFailure() {
        Booking booking1 = Booking.builder()
                .id("bkg-fail-1")
                .flightId("flt-1")
                .cabinClass(com.smarttravel.modules.flight.model.CabinClass.ECONOMY)
                .passengerCount(1)
                .status(BookingStatus.PENDING)
                .expiresAt(Instant.now().minusSeconds(100))
                .build();

        Booking booking2 = Booking.builder()
                .id("bkg-success-2")
                .flightId("flt-2")
                .cabinClass(com.smarttravel.modules.flight.model.CabinClass.ECONOMY)
                .passengerCount(1)
                .status(BookingStatus.PENDING)
                .expiresAt(Instant.now().minusSeconds(100))
                .build();

        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(Instant.class)))
                .thenReturn(List.of(booking1, booking2));

        // Booking 1 throws an unexpected database exception during update
        when(mongoTemplate.updateFirst(argThat(query -> query != null && query.toString().contains("bkg-fail-1")), any(Update.class), eq(Booking.class)))
                .thenThrow(new RuntimeException("Simulated transient MongoDB network failure"));

        // Booking 2 succeeds
        com.mongodb.client.result.UpdateResult updateResult = mock(com.mongodb.client.result.UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(1L);
        when(mongoTemplate.updateFirst(argThat(query -> query != null && query.toString().contains("bkg-success-2")), any(Update.class), eq(Booking.class)))
                .thenReturn(updateResult);

        int expired = expirationService.expireOverdueBookings();

        // One booking succeeded despite the first failing
        assertThat(expired).isEqualTo(1);
        verify(reservationService, times(1)).releaseSeats(eq("flt-2"), eq(com.smarttravel.modules.flight.model.CabinClass.ECONOMY), eq(1));
    }
}
