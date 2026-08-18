package com.smarttravel.modules.payment;

import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingExpirationService;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.payment.dto.PaymentOrderCreateRequest;
import com.smarttravel.modules.payment.dto.PaymentOrderResponse;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.payment.service.PaymentReconciliationService;
import com.smarttravel.modules.payment.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentVsExpirationConcurrencyIntegrationTest {

    @Autowired
    private PaymentReconciliationService reconciliationService;

    @Autowired
    private BookingExpirationService expirationService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    private String flightId;
    private String bookingId;
    private String paymentId;
    private String razorpayOrderId;

    @BeforeEach
    void setUp() {
        AirportDto del = AirportDto.builder().code("DEL").name("Delhi Airport").city("Delhi").country("India").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("Mumbai Airport").city("Mumbai").country("India").build();

        CabinInventoryDto econ = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(100)
                .basePrice(new BigDecimal("5000.00"))
                .taxAmount(new BigDecimal("600.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5750.00"))
                .build();

        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber("TEST-RACE-" + System.currentTimeMillis())
                .airline("SmartAir Race")
                .airlineCode("SR")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.now().plusSeconds(172800))
                .arrivalTime(Instant.now().plusSeconds(180000))
                .aircraftModel("A321neo")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(100)
                .availableSeats(100)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econ))
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse flightRes = flightService.createFlight(flightReq);
        flightId = flightRes.getId();

        PassengerDto p1 = PassengerDto.builder().title("Mr").firstName("John").lastName("Doe").dateOfBirth(LocalDate.of(1990, 1, 1)).gender("MALE").nationality("Indian").build();
        PassengerDto p2 = PassengerDto.builder().title("Mrs").firstName("Jane").lastName("Doe").dateOfBirth(LocalDate.of(1992, 2, 2)).gender("FEMALE").nationality("Indian").build();

        BookingCreateRequest bkgReq = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(p1, p2))
                .build();

        BookingResponse bkgRes = bookingService.createBooking(bkgReq, "user-race", "race@smarttravel.com");
        bookingId = bkgRes.getId();

        // 2 seats reserved -> 98 available
        Flight f1 = flightRepository.findById(flightId).orElseThrow();
        assertThat(f1.getAvailableSeats()).isEqualTo(98);

        // Set booking to PENDING and overdue
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.PENDING);
        booking.setExpiresAt(Instant.now().minusSeconds(10));
        bookingRepository.save(booking);

        // Create payment order (total = 5750 * 2 = 11500 INR = 1150000 paise)
        PaymentOrderCreateRequest orderReq = PaymentOrderCreateRequest.builder().bookingId(bookingId).build();
        PaymentOrderResponse orderRes = paymentService.createPaymentOrder(orderReq, "user-race", "race@smarttravel.com");
        paymentId = orderRes.getPaymentId();
        razorpayOrderId = orderRes.getRazorpayOrderId();
    }

    @AfterEach
    void tearDown() {
        if (paymentId != null) {
            try {
                paymentRepository.deleteById(paymentId);
            } catch (Exception ignored) {
            }
        }
        if (bookingId != null) {
            try {
                bookingRepository.deleteById(bookingId);
            } catch (Exception ignored) {
            }
        }
        if (flightId != null) {
            try {
                flightRepository.deleteById(flightId);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("Concurrency Race: Simultaneous Payment Reconcile vs Booking Expiration results in consistent state")
    void testConcurrentPaymentVsExpiration() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);

        AtomicReference<Payment> paymentResultRef = new AtomicReference<>();
        AtomicReference<Integer> expireResultRef = new AtomicReference<>();

        // Thread 1: Payment Reconciliation (Webhook)
        executor.submit(() -> {
            try {
                startLatch.await();
                Payment p = reconciliationService.reconcilePaymentSuccess(razorpayOrderId, "pay_race_123", 1150000L, "INR", "payment.captured");
                paymentResultRef.set(p);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                finishLatch.countDown();
            }
        });

        // Thread 2: Booking Expiration
        executor.submit(() -> {
            try {
                startLatch.await();
                int count = expirationService.expireOverdueBookings();
                expireResultRef.set(count);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                finishLatch.countDown();
            }
        });

        // Fire both threads simultaneously
        startLatch.countDown();
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        executor.shutdown();

        // Query final DB states
        Booking finalBooking = bookingRepository.findById(bookingId).orElseThrow();
        Payment finalPayment = paymentRepository.findById(paymentId).orElseThrow();
        Flight finalFlight = flightRepository.findById(flightId).orElseThrow();

        // Exactly one valid state must prevail:
        if (finalBooking.getStatus() == BookingStatus.CONFIRMED) {
            // Payment won the race: Booking is CONFIRMED, Payment is VERIFIED, seats remain reserved (98)
            assertThat(finalPayment.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);
            assertThat(finalFlight.getAvailableSeats()).isEqualTo(98);
        } else if (finalBooking.getStatus() == BookingStatus.EXPIRED) {
            // Expiration won the race: Booking is EXPIRED, seats are safely released (100)
            assertThat(finalFlight.getAvailableSeats()).isEqualTo(100);
            if (finalPayment.getFailureReason() != null) {
                assertThat(finalPayment.getFailureReason()).matches("(?s).*(LATE_PAYMENT_CONFLICT|Booking expired).*");
            }
        } else {
            org.junit.jupiter.api.Assertions.fail("Inconsistent booking state: " + finalBooking.getStatus());
        }
    }
}
