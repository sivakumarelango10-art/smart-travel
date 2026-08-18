package com.smarttravel.modules.booking.service;

import com.mongodb.client.result.UpdateResult;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.flight.model.CabinClass;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingExpirationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightInventoryReservationService reservationService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private PaymentStateMachine paymentStateMachine;
    private BookingExpirationService expirationService;

    private Booking overdueBooking;
    private Payment activePayment;

    @BeforeEach
    void setUp() {
        paymentStateMachine = new PaymentStateMachine();

        expirationService = new BookingExpirationServiceImpl(
                bookingRepository,
                reservationService,
                paymentRepository,
                paymentStateMachine,
                mongoTemplate
        );

        overdueBooking = Booking.builder()
                .id("bk-overdue-1")
                .bookingReference("ST8K4P2Q")
                .userId("user-1")
                .flightId("fl-100")
                .cabinClass(CabinClass.ECONOMY)
                .passengerCount(2)
                .status(BookingStatus.PENDING)
                .expiresAt(Instant.now().minusSeconds(300))
                .build();

        activePayment = Payment.builder()
                .id("pay-1")
                .bookingId("bk-overdue-1")
                .paymentStatus(PaymentStatus.ORDER_CREATED)
                .build();
    }

    @Test
    @DisplayName("Expire Overdue Bookings: Atomically transitions PENDING to EXPIRED, releases seats, and marks payment EXPIRED")
    void testExpireOverdueBookings_Success() {
        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(Instant.class)))
                .thenReturn(List.of(overdueBooking));

        UpdateResult successResult = mock(UpdateResult.class);
        when(successResult.getModifiedCount()).thenReturn(1L);
        when(mongoTemplate.updateFirst(any(Query.class), any(org.springframework.data.mongodb.core.query.UpdateDefinition.class), eq(Booking.class)))
                .thenReturn(successResult);

        when(reservationService.releaseSeats("fl-100", CabinClass.ECONOMY, 2)).thenReturn(true);
        when(paymentRepository.findByBookingId("bk-overdue-1")).thenReturn(List.of(activePayment));

        int expiredCount = expirationService.expireOverdueBookings();

        assertThat(expiredCount).isEqualTo(1);
        verify(reservationService).releaseSeats("fl-100", CabinClass.ECONOMY, 2);
        verify(paymentRepository).save(activePayment);
        assertThat(activePayment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
    }

    @Test
    @DisplayName("Concurrency Race: If concurrent payment confirmed the booking (modifiedCount = 0), do NOT release seats")
    void testExpireOverdueBookings_RaceConditionLost() {
        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(Instant.class)))
                .thenReturn(List.of(overdueBooking));

        UpdateResult noModResult = mock(UpdateResult.class);
        when(noModResult.getModifiedCount()).thenReturn(0L);
        when(mongoTemplate.updateFirst(any(Query.class), any(org.springframework.data.mongodb.core.query.UpdateDefinition.class), eq(Booking.class)))
                .thenReturn(noModResult);

        int expiredCount = expirationService.expireOverdueBookings();

        assertThat(expiredCount).isEqualTo(0);
        // Seats must NOT be released!
        verify(reservationService, never()).releaseSeats(any(), any(), org.mockito.ArgumentMatchers.anyInt());
        verify(paymentRepository, never()).findByBookingId(any());
    }

    @Test
    @DisplayName("Expire Overdue Bookings: No overdue bookings returns 0 without DB updates")
    void testExpireOverdueBookings_NoneFound() {
        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(Instant.class)))
                .thenReturn(List.of());

        int expiredCount = expirationService.expireOverdueBookings();

        assertThat(expiredCount).isEqualTo(0);
        verify(mongoTemplate, never()).updateFirst(any(Query.class), any(org.springframework.data.mongodb.core.query.UpdateDefinition.class), eq(Booking.class));
    }

    @Test
    @DisplayName("Fault Isolation: Failure on one booking does not abort remaining bookings")
    void testExpireOverdueBookings_FaultIsolation() {
        Booking booking2 = Booking.builder()
                .id("bk-overdue-2")
                .flightId("fl-200")
                .cabinClass(CabinClass.BUSINESS)
                .passengerCount(1)
                .status(BookingStatus.PENDING)
                .build();

        when(bookingRepository.findByStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(Instant.class)))
                .thenReturn(List.of(overdueBooking, booking2));

        UpdateResult successResult = mock(UpdateResult.class);
        when(successResult.getModifiedCount()).thenReturn(1L);

        // First booking throws DB exception on update, second succeeds
        when(mongoTemplate.updateFirst(any(Query.class), any(org.springframework.data.mongodb.core.query.UpdateDefinition.class), eq(Booking.class)))
                .thenThrow(new RuntimeException("DB glitch"))
                .thenReturn(successResult);

        when(reservationService.releaseSeats("fl-200", CabinClass.BUSINESS, 1)).thenReturn(true);
        when(paymentRepository.findByBookingId("bk-overdue-2")).thenReturn(List.of());

        int expiredCount = expirationService.expireOverdueBookings();

        assertThat(expiredCount).isEqualTo(1);
        verify(reservationService).releaseSeats("fl-200", CabinClass.BUSINESS, 1);
    }
}
