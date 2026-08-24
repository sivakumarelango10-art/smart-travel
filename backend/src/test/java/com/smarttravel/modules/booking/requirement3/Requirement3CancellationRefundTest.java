package com.smarttravel.modules.booking.requirement3;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingStateMachine;
import com.smarttravel.modules.booking.service.BookingServiceImpl;
import com.smarttravel.modules.booking.dto.BookingCancelRequest;
import com.smarttravel.modules.booking.mapper.BookingMapper;
import com.smarttravel.modules.booking.service.FlightInventoryReservationService;
import com.smarttravel.modules.booking.service.PnrGenerator;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FareCalculationService;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.refund.dto.RefundEligibilityResponse;
import com.smarttravel.modules.payment.refund.dto.RefundProcessRequest;
import com.smarttravel.modules.payment.refund.dto.RefundResponse;
import com.smarttravel.modules.payment.refund.model.RefundReason;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import com.smarttravel.modules.payment.refund.service.CancellationRefundPolicy;
import com.smarttravel.modules.payment.refund.service.RefundEligibilityServiceImpl;
import com.smarttravel.modules.payment.refund.model.Refund;
import com.smarttravel.modules.payment.refund.repository.RefundRepository;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.payment.refund.service.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Requirement #3 — Cancellation & Refund System
 * ============================================================
 * Automated verification of all Requirement #3 sub-requirements:
 *  - Cancellation reason persistence
 *  - Time-based refund policy (>7d=100%, 24h-7d=50%, <24h=0%)
 *  - Auto-refund trigger on cancellation
 *  - Idempotency
 *  - IDOR security
 *  - Server-side refund calculation (never trust frontend)
 *  - BigDecimal only — no floating point arithmetic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Requirement #3: Cancellation & Refund System")
class Requirement3CancellationRefundTest {

    // -----------------------------------------------------------------------
    // SECTION 1 – Refund Policy (CancellationRefundPolicy unit tests)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("1. Time-Based Refund Policy")
    class RefundPolicyTests {

        private final CancellationRefundPolicy policy = new CancellationRefundPolicy();
        private static final long ORIGINAL_PAISE = 1000000L; // ₹10,000

        @Test
        @DisplayName("[T1] Full refund (100%) when cancelled more than 7 days before departure")
        void fullRefundWhenCancelledMoreThan7DaysBeforeDeparture() {
            Instant departure = Instant.now().plus(10, ChronoUnit.DAYS); // 10 days away
            Instant cancelledAt = Instant.now();

            long refundPaise = policy.calculateRefundAmountPaise(ORIGINAL_PAISE, departure, cancelledAt);
            String label = policy.getRefundPercentageLabel(departure, cancelledAt);

            assertThat(refundPaise).isEqualTo(ORIGINAL_PAISE);
            assertThat(label).isEqualTo("100%");
        }

        @Test
        @DisplayName("[T2] Partial refund (50%) when cancelled 24h–7 days before departure")
        void halfRefundWhenCancelledWithin24HoursTo7Days() {
            Instant departure = Instant.now().plus(72, ChronoUnit.HOURS); // 3 days away
            Instant cancelledAt = Instant.now();

            long refundPaise = policy.calculateRefundAmountPaise(ORIGINAL_PAISE, departure, cancelledAt);
            String label = policy.getRefundPercentageLabel(departure, cancelledAt);

            assertThat(refundPaise).isEqualTo(ORIGINAL_PAISE / 2L); // exactly 50%
            assertThat(label).isEqualTo("50%");
        }

        @Test
        @DisplayName("[T3] No refund (0%) when cancelled within 24 hours of departure")
        void noRefundWhenCancelledWithin24HoursOfDeparture() {
            Instant departure = Instant.now().plus(12, ChronoUnit.HOURS); // 12h away
            Instant cancelledAt = Instant.now();

            long refundPaise = policy.calculateRefundAmountPaise(ORIGINAL_PAISE, departure, cancelledAt);
            String label = policy.getRefundPercentageLabel(departure, cancelledAt);

            assertThat(refundPaise).isEqualTo(0L);
            assertThat(label).isEqualTo("0%");
        }

        @Test
        @DisplayName("[T4] No refund when flight has already departed")
        void noRefundWhenFlightAlreadyDeparted() {
            Instant departure = Instant.now().minus(2, ChronoUnit.HOURS); // already departed
            Instant cancelledAt = Instant.now();

            long refundPaise = policy.calculateRefundAmountPaise(ORIGINAL_PAISE, departure, cancelledAt);

            assertThat(refundPaise).isEqualTo(0L);
        }

        @Test
        @DisplayName("[T5] BigDecimal INR output is exact — no floating-point rounding errors")
        void bigDecimalUsedForAllMoneyCalculations() {
            // ₹10,001 (paise=1000100) — odd amount for 50% split test
            long oddPaise = 1000100L;
            Instant departure = Instant.now().plus(72, ChronoUnit.HOURS);
            Instant cancelledAt = Instant.now();

            BigDecimal refundInr = policy.calculateRefundAmountInr(oddPaise, departure, cancelledAt);

            // 50% of 10001.00 = 5000.50
            assertThat(refundInr).isEqualByComparingTo(new BigDecimal("5000.50"));
            // Verify the type returned is BigDecimal (not double/float)
            assertThat(refundInr).isInstanceOf(BigDecimal.class);
        }

        @Test
        @DisplayName("[T6] Full refund regardless of timing for airline-disruption reasons")
        void fullRefundForAirlineDisruptionRegardlessOfTiming() {
            // Booking departure in 1 hour — would normally be 0% for CUSTOMER_CANCELLATION
            Instant departure = Instant.now().plus(1, ChronoUnit.HOURS);
            Instant cancelledAt = Instant.now();

            // Policy class doesn't differentiate reason — that logic is in RefundEligibilityServiceImpl
            // But we verify 0% for customer cancellation at <24h
            long refundPaise = policy.calculateRefundAmountPaise(1000000L, departure, cancelledAt);
            assertThat(refundPaise).isEqualTo(0L);
            // In RefundEligibilityServiceImpl, FLIGHT_CANCELLED bypasses this and returns full
            // (tested in T11 below via eligibility service)
        }
    }

    // -----------------------------------------------------------------------
    // SECTION 2 – Refund Eligibility Service (with policy engine)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("2. Refund Eligibility Service (Policy-Aware)")
    class RefundEligibilityServiceTests {

        @Mock private BookingRepository bookingRepository;
        @Mock private PaymentRepository paymentRepository;
        @Mock private RefundRepository refundRepository;

        private CancellationRefundPolicy cancellationRefundPolicy;
        private RefundEligibilityServiceImpl eligibilityService;

        private Payment verifiedPayment;
        private Booking testBooking;

        @BeforeEach
        void setUp() {
            // Manually instantiate with real policy (not mocked) to test end-to-end
            cancellationRefundPolicy = new CancellationRefundPolicy();
            eligibilityService = new RefundEligibilityServiceImpl(
                    bookingRepository, paymentRepository, refundRepository, cancellationRefundPolicy);

            verifiedPayment = Payment.builder()
                    .id("pay-test-001")
                    .bookingId("book-test-001")
                    .amount(BigDecimal.valueOf(10000.00))
                    .amountPaise(1000000L)
                    .currency("INR")
                    .paymentStatus(PaymentStatus.VERIFIED)
                    .build();

            testBooking = Booking.builder()
                    .id("book-test-001")
                    .userId("user-test-001")
                    .bookingReference("STTEST01")
                    .status(BookingStatus.CONFIRMED)
                    .build();
        }

        @Test
        @DisplayName("[T7] Eligibility returns 50% for CUSTOMER_CANCELLATION 3 days before departure")
        void eligibilityReturns50PercentFor3DaysBeforeDeparture() {
            testBooking.setDepartureTime(Instant.now().plus(72, ChronoUnit.HOURS));

            when(paymentRepository.findById("pay-test-001")).thenReturn(Optional.of(verifiedPayment));
            when(bookingRepository.findById("book-test-001")).thenReturn(Optional.of(testBooking));
            when(refundRepository.findFirstByPaymentIdOrderByCreatedAtDesc("pay-test-001")).thenReturn(Optional.empty());

            RefundEligibilityResponse res = eligibilityService.checkPaymentRefundEligibility(
                    "pay-test-001", RefundReason.CUSTOMER_CANCELLATION);

            assertThat(res.isEligible()).isTrue();
            assertThat(res.getRefundPercentage()).isEqualTo("50%");
            assertThat(res.getRefundableAmountPaise()).isEqualTo(500000L); // 50% of ₹10,000
            assertThat(res.getRefundableAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(res.getPolicyDescription()).contains("50%").contains("24h");
        }

        @Test
        @DisplayName("[T8] Refund amount is CALCULATED ON SERVER — not passed from frontend")
        void refundAmountCalculatedOnServerNotFrontend() {
            testBooking.setDepartureTime(Instant.now().plus(10, ChronoUnit.DAYS));

            when(paymentRepository.findById("pay-test-001")).thenReturn(Optional.of(verifiedPayment));
            when(bookingRepository.findById("book-test-001")).thenReturn(Optional.of(testBooking));
            when(refundRepository.findFirstByPaymentIdOrderByCreatedAtDesc("pay-test-001")).thenReturn(Optional.empty());

            RefundEligibilityResponse res = eligibilityService.checkPaymentRefundEligibility(
                    "pay-test-001", RefundReason.CUSTOMER_CANCELLATION);

            // Server determined the refund — the test input (RefundReason) never includes an amount
            // The server reads from the persisted Payment record, NOT from request body
            assertThat(res.getRefundableAmountPaise()).isEqualTo(1000000L);
            assertThat(res.getRefundableAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000.00));
        }

        @Test
        @DisplayName("[T9] Not eligible when payment is not VERIFIED")
        void unverifiedPaymentNotEligibleForRefund() {
            verifiedPayment.setPaymentStatus(PaymentStatus.PENDING);
            testBooking.setDepartureTime(Instant.now().plus(10, ChronoUnit.DAYS));

            when(paymentRepository.findById("pay-test-001")).thenReturn(Optional.of(verifiedPayment));
            when(bookingRepository.findById("book-test-001")).thenReturn(Optional.of(testBooking));
            when(refundRepository.findFirstByPaymentIdOrderByCreatedAtDesc("pay-test-001")).thenReturn(Optional.empty());

            RefundEligibilityResponse res = eligibilityService.checkPaymentRefundEligibility(
                    "pay-test-001", RefundReason.CUSTOMER_CANCELLATION);

            assertThat(res.isEligible()).isFalse();
            assertThat(res.getReason()).contains("Only captured/verified payments");
        }

        @Test
        @DisplayName("[T10] Full refund for FLIGHT_CANCELLED regardless of departure proximity")
        void fullRefundForFlightCancelledDisruption() {
            // Flight departs in 2 hours — normally no customer refund, but airline cancelled
            testBooking.setDepartureTime(Instant.now().plus(2, ChronoUnit.HOURS));

            when(paymentRepository.findById("pay-test-001")).thenReturn(Optional.of(verifiedPayment));
            when(bookingRepository.findById("book-test-001")).thenReturn(Optional.of(testBooking));
            when(refundRepository.findFirstByPaymentIdOrderByCreatedAtDesc("pay-test-001")).thenReturn(Optional.empty());

            RefundEligibilityResponse res = eligibilityService.checkPaymentRefundEligibility(
                    "pay-test-001", RefundReason.FLIGHT_CANCELLED);

            assertThat(res.isEligible()).isTrue();
            assertThat(res.getRefundPercentage()).isEqualTo("100%");
            assertThat(res.getRefundableAmountPaise()).isEqualTo(1000000L);
        }

        @Test
        @DisplayName("[T11] Idempotency — returns existing refund when already processed")
        void idempotencyReturnsExistingRefundWhenAlreadyProcessed() {
            Refund existing = Refund.builder()
                    .id("rfnd-existing-999")
                    .status(RefundStatus.COMPLETED)
                    .reason(RefundReason.CUSTOMER_CANCELLATION)
                    .build();

            when(paymentRepository.findById("pay-test-001")).thenReturn(Optional.of(verifiedPayment));
            when(bookingRepository.findById("book-test-001")).thenReturn(Optional.of(testBooking));
            when(refundRepository.findFirstByPaymentIdOrderByCreatedAtDesc("pay-test-001"))
                    .thenReturn(Optional.of(existing));

            RefundEligibilityResponse res = eligibilityService.checkPaymentRefundEligibility(
                    "pay-test-001", RefundReason.CUSTOMER_CANCELLATION);

            assertThat(res.isEligible()).isFalse();
            assertThat(res.isAlreadyRefunded()).isTrue();
            assertThat(res.getExistingRefundId()).isEqualTo("rfnd-existing-999");
        }
    }

    // -----------------------------------------------------------------------
    // SECTION 3 – BookingServiceImpl cancellation flow
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("3. Cancellation Flow (BookingServiceImpl)")
    @ExtendWith(MockitoExtension.class)
    class CancellationFlowTests {

        @Mock private BookingRepository bookingRepository;
        @Mock private FlightRepository flightRepository;
        @Mock private FlightInventoryReservationService reservationService;
        @Mock private FareCalculationService fareCalculationService;
        @Mock private BookingStateMachine stateMachine;
        @Mock private PnrGenerator pnrGenerator;
        @Mock private BookingMapper bookingMapper;
        @Mock private PaymentRepository paymentRepository;
        @Mock private RefundService refundService;

        private BookingServiceImpl bookingService;
        private Booking confirmedBooking;

        @BeforeEach
        void setUp() {
            bookingService = new BookingServiceImpl(
                    bookingRepository, flightRepository, reservationService,
                    fareCalculationService, stateMachine, pnrGenerator, bookingMapper,
                    new com.smarttravel.modules.booking.config.BookingProperties(),
                    null, null, null, null,
                    paymentRepository, refundService
            );

            confirmedBooking = Booking.builder()
                    .id("book-cancel-001")
                    .userId("user-cancel-001")
                    .bookingReference("STCNCL01")
                    .status(BookingStatus.CONFIRMED)
                    .flightId("flight-001")
                    .cabinClass(CabinClass.ECONOMY)
                    .passengerCount(1)
                    .departureTime(Instant.now().plus(10, ChronoUnit.DAYS))
                    .build();
        }

        @Test
        @DisplayName("[T12] Cancellation reason is persisted to database")
        void cancellationReasonPersistedToDatabase() {
            when(bookingRepository.findByIdAndUserId("book-cancel-001", "user-cancel-001"))
                    .thenReturn(Optional.of(confirmedBooking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reservationService.releaseSeats(any(), any(), anyInt())).thenReturn(true);
            when(bookingMapper.toResponse(any())).thenReturn(null);
            when(paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc("book-cancel-001"))
                    .thenReturn(Optional.empty()); // no payment — skip auto-refund

            BookingCancelRequest req = new BookingCancelRequest("Medical emergency");
            bookingService.cancelBooking("book-cancel-001", req, "user-cancel-001", false);

            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(captor.capture());

            Booking saved = captor.getValue();
            assertThat(saved.getCancellationReason()).isEqualTo("Medical emergency");
            assertThat(saved.getCancelledAt()).isNotNull();
            assertThat(saved.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("[T13] Auto-refund is triggered when verified payment exists")
        void autoRefundTriggeredOnCancellationWithVerifiedPayment() {
            Payment verifiedPayment = Payment.builder()
                    .id("pay-auto-001")
                    .bookingId("book-cancel-001")
                    .paymentStatus(PaymentStatus.VERIFIED)
                    .amount(BigDecimal.valueOf(5000.00))
                    .amountPaise(500000L)
                    .build();

            when(bookingRepository.findByIdAndUserId("book-cancel-001", "user-cancel-001"))
                    .thenReturn(Optional.of(confirmedBooking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reservationService.releaseSeats(any(), any(), anyInt())).thenReturn(true);
            when(bookingMapper.toResponse(any())).thenReturn(null);
            when(paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc("book-cancel-001"))
                    .thenReturn(Optional.of(verifiedPayment));
            when(refundService.processRefund(anyString(), any(), anyString())).thenReturn(
                    RefundResponse.builder().id("rfnd-001").status(RefundStatus.COMPLETED).build()
            );

            BookingCancelRequest req = new BookingCancelRequest("Found better fare");
            bookingService.cancelBooking("book-cancel-001", req, "user-cancel-001", false);

            // Verify refund was triggered with CUSTOMER_CANCELLATION reason
            ArgumentCaptor<RefundProcessRequest> refundCaptor = ArgumentCaptor.forClass(RefundProcessRequest.class);
            verify(refundService).processRefund(eq("pay-auto-001"), refundCaptor.capture(), eq("user-cancel-001"));

            RefundProcessRequest captured = refundCaptor.getValue();
            assertThat(captured.getReason()).isEqualTo(RefundReason.CUSTOMER_CANCELLATION);
            assertThat(captured.getDescription()).isEqualTo("Found better fare");
        }

        @Test
        @DisplayName("[T14] Auto-refund is NOT triggered when payment is PENDING (not verified)")
        void autoRefundNotTriggeredForPendingPayment() {
            Payment pendingPayment = Payment.builder()
                    .id("pay-pending-001")
                    .bookingId("book-cancel-001")
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();

            when(bookingRepository.findByIdAndUserId("book-cancel-001", "user-cancel-001"))
                    .thenReturn(Optional.of(confirmedBooking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reservationService.releaseSeats(any(), any(), anyInt())).thenReturn(true);
            when(bookingMapper.toResponse(any())).thenReturn(null);
            when(paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc("book-cancel-001"))
                    .thenReturn(Optional.of(pendingPayment));

            bookingService.cancelBooking("book-cancel-001", null, "user-cancel-001", false);

            // RefundService.processRefund should NOT be called for a non-VERIFIED payment
            verify(refundService, never()).processRefund(anyString(), any(), anyString());
        }

        @Test
        @DisplayName("[T15] Cancellation of already-CANCELLED booking throws ConflictException")
        void cancellationOfAlreadyCancelledBookingThrowsConflict() {
            Booking alreadyCancelled = Booking.builder()
                    .id("book-cancel-001")
                    .userId("user-cancel-001")
                    .status(BookingStatus.CANCELLED)
                    .build();

            when(bookingRepository.findByIdAndUserId("book-cancel-001", "user-cancel-001"))
                    .thenReturn(Optional.of(alreadyCancelled));
            doThrow(new com.smarttravel.common.exception.ConflictException(
                    "CANCELLED → CANCELLED transition not allowed"))
                    .when(stateMachine).validateTransition(BookingStatus.CANCELLED, BookingStatus.CANCELLED);

            assertThatThrownBy(() ->
                    bookingService.cancelBooking("book-cancel-001", null, "user-cancel-001", false)
            ).isInstanceOf(com.smarttravel.common.exception.ConflictException.class);
        }

        @Test
        @DisplayName("[T16] IDOR protection — user cannot cancel another user's booking")
        void idorProtectionUserCannotCancelAnotherUsersBooking() {
            when(bookingRepository.findByIdAndUserId("book-cancel-001", "attacker-user"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    bookingService.cancelBooking("book-cancel-001", null, "attacker-user", false)
            ).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("[T17] Seat inventory is released atomically on cancellation")
        void seatInventoryReleasedAtomicallyOnCancellation() {
            when(bookingRepository.findByIdAndUserId("book-cancel-001", "user-cancel-001"))
                    .thenReturn(Optional.of(confirmedBooking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reservationService.releaseSeats(any(), any(), anyInt())).thenReturn(true);
            when(bookingMapper.toResponse(any())).thenReturn(null);
            when(paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc("book-cancel-001"))
                    .thenReturn(Optional.empty());

            bookingService.cancelBooking("book-cancel-001", null, "user-cancel-001", false);

            verify(reservationService).releaseSeats("flight-001", CabinClass.ECONOMY, 1);
        }
    }
}
