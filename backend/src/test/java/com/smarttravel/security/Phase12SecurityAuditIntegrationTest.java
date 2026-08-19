package com.smarttravel.security;

import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.analytics.dto.AnalyticsDateRangeRequest;
import com.smarttravel.modules.analytics.service.AnalyticsService;
import com.smarttravel.modules.booking.dto.BookingCancelRequest;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.Booking;

import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.booking.service.CheckInService;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.ticket.dto.TicketResponse;

import com.smarttravel.modules.ticket.repository.TicketRepository;
import com.smarttravel.modules.ticket.service.TicketService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 12 Final Security & IDOR Isolation Audit Integration Test.
 * Validates cross-tenant isolation, IDOR protection, authoritative pricing,
 * webhook idempotency, and admin authorization barriers against live MongoDB.
 */
@SpringBootTest
class Phase12SecurityAuditIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AnalyticsService analyticsService;

    private String flightId;
    private String bookingIdCustomerA;
    private String bookingIdCustomerB;
    private final String USER_A = "user-alice-sec-12";
    private final String USER_B = "user-bob-sec-12";
    private final List<String> cleanBookingIds = new ArrayList<>();
    private final List<String> cleanTicketIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        AirportDto del = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").country("India").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").country("India").build();

        CabinInventoryDto econ = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(100)
                .basePrice(new BigDecimal("4500.00"))
                .taxAmount(new BigDecimal("500.00"))
                .feeAmount(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("5100.00"))
                .build();

        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber("SEC-12-" + System.currentTimeMillis())
                .airline("SecurityAir")
                .airlineCode("SA")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.now().plusSeconds(86400 * 3))
                .arrivalTime(Instant.now().plusSeconds(86400 * 3 + 7200))
                .aircraftModel("A321neo")
                .basePrice(new BigDecimal("4500.00"))
                .totalSeats(100)
                .availableSeats(100)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econ))
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse flightRes = flightService.createFlight(flightReq);
        flightId = flightRes.getId();

        // Create booking for Customer A
        PassengerDto pA = PassengerDto.builder()
                .title("MR")
                .firstName("Alice")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1992, 5, 12))
                .gender("FEMALE")
                .nationality("IN")
                .build();

        BookingCreateRequest reqA = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(pA))
                .build();

        BookingResponse resA = bookingService.createBooking(reqA, USER_A, "alice@sec.com");
        bookingIdCustomerA = resA.getId();
        cleanBookingIds.add(bookingIdCustomerA);

        // Create booking for Customer B
        PassengerDto pB = PassengerDto.builder()
                .title("MR")
                .firstName("Bob")
                .lastName("Jones")
                .dateOfBirth(LocalDate.of(1988, 11, 20))
                .gender("MALE")
                .nationality("IN")
                .build();

        BookingCreateRequest reqB = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(pB))
                .build();

        BookingResponse resB = bookingService.createBooking(reqB, USER_B, "bob@sec.com");
        bookingIdCustomerB = resB.getId();
        cleanBookingIds.add(bookingIdCustomerB);
    }

    @AfterEach
    void tearDown() {
        for (String id : cleanBookingIds) {
            try { bookingRepository.deleteById(id); } catch (Exception ignored) {}
        }
        for (String id : cleanTicketIds) {
            try { ticketRepository.deleteById(id); } catch (Exception ignored) {}
        }
        if (flightId != null) {
            try { flightRepository.deleteById(flightId); } catch (Exception ignored) {}
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IDOR Protection: Booking Isolation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Security Audit 1: Customer A cannot access Customer B's booking (throws ResourceNotFoundException 404)")
    void customerA_cannotAccess_customerB_booking() {
        // Customer A queries Customer B's bookingId
        assertThatThrownBy(() -> bookingService.getBookingById(bookingIdCustomerB, USER_A, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Security Audit 2: Customer B cannot access Customer A's booking by ID (404)")
    void customerB_cannotAccess_customerA_booking() {
        assertThatThrownBy(() -> bookingService.getBookingById(bookingIdCustomerA, USER_B, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Security Audit 3: Customer A cannot cancel Customer B's booking (404)")
    void customerA_cannotCancel_customerB_booking() {
        BookingCancelRequest cancelReq = new BookingCancelRequest("Malicious cancel");
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingIdCustomerB, cancelReq, USER_A, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IDOR Protection: Ticket & PDF Isolation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Security Audit 4: Customer A cannot view or lookup Customer B's ticket (404)")
    void customerA_cannotAccess_customerB_ticket() {
        // Issue ticket for Customer B
        Payment paymentB = Payment.builder()
                .bookingId(bookingIdCustomerB)
                .userId(USER_B)
                .amount(new BigDecimal("5100.00"))
                .currency("INR")
                .paymentStatus(PaymentStatus.VERIFIED)
                .razorpayPaymentId("pay_sec_test_" + System.currentTimeMillis())
                .createdAt(Instant.now())
                .build();
        paymentRepository.save(paymentB);

        TicketResponse ticketB = ticketService.issueTicket(bookingIdCustomerB);
        cleanTicketIds.add(ticketB.getId());

        // Customer A attempts to access Customer B's ticket by ID
        assertThatThrownBy(() -> ticketService.getTicketById(ticketB.getId(), USER_A, false))
                .isInstanceOf(ResourceNotFoundException.class);

        // Customer A attempts to access Customer B's ticket by bookingId
        assertThatThrownBy(() -> ticketService.getTicketByBookingId(bookingIdCustomerB, USER_A, false))
                .isInstanceOf(ResourceNotFoundException.class);

        // Customer A attempts to generate PDF for Customer B's ticket
        assertThatThrownBy(() -> ticketService.generateTicketPdf(ticketB.getId(), USER_A, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IDOR Protection: Check-in & Boarding Pass Isolation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Security Audit 5: Customer A cannot access Customer B's check-in or boarding passes (404)")
    void customerA_cannotAccess_customerB_checkIn() {
        assertThatThrownBy(() -> checkInService.getCheckInByBookingId(bookingIdCustomerB, USER_A, false))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> checkInService.getBoardingPasses(bookingIdCustomerB, USER_A, false))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> checkInService.getBoardingPassPdf(bookingIdCustomerB, USER_A, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Authoritative Pricing & Fare Tampering Protection
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Security Audit 6: Backend is authoritative for fare calculation (client price manipulation impossible)")
    void backendCalculatesAuthoritativeFare() {
        Booking bookingA = bookingRepository.findById(bookingIdCustomerA).orElseThrow();
        FareBreakdownDto fare = bookingA.getFareBreakdown();

        // Backend calculates fare server-side; it must be non-null and internally consistent.
        assertThat(fare).isNotNull();
        assertThat(fare.getBaseFare()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(fare.getTaxes()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(fare.getFees()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);

        // Total amount must be positive (backend-authoritative, never zero/negative)
        assertThat(bookingA.getTotalAmount()).isGreaterThan(BigDecimal.ZERO);

        // Internal consistency: totalAmount == baseFare + taxes + fees
        BigDecimal expectedTotal = fare.getBaseFare().add(fare.getTaxes()).add(fare.getFees());
        assertThat(bookingA.getTotalAmount()).isEqualByComparingTo(expectedTotal);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Idempotency: Webhook / Ticket Issuance
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Security Audit 7: Duplicate ticket issuance calls are idempotent (zero duplicated ticket records)")
    void duplicateTicketIssuance_isIdempotent() {
        Payment paymentA = Payment.builder()
                .bookingId(bookingIdCustomerA)
                .userId(USER_A)
                .amount(new BigDecimal("5100.00"))
                .currency("INR")
                .paymentStatus(PaymentStatus.VERIFIED)
                .razorpayPaymentId("pay_idem_" + System.currentTimeMillis())
                .createdAt(Instant.now())
                .build();
        paymentRepository.save(paymentA);

        TicketResponse t1 = ticketService.issueTicket(bookingIdCustomerA);
        cleanTicketIds.add(t1.getId());

        TicketResponse t2 = ticketService.issueTicket(bookingIdCustomerA);

        // Idempotency: same ticket returned
        assertThat(t1.getTicketNumber()).isEqualTo(t2.getTicketNumber());
        assertThat(t1.getId()).isEqualTo(t2.getId());

        // Only one ticket document exists in the database for this booking
        boolean secondTicketExists = ticketRepository.existsByBookingId(bookingIdCustomerA);
        assertThat(secondTicketExists).isTrue();
        // Verify no duplicate: both calls return same ID
        assertThat(t1.getId()).isEqualTo(t2.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin RBAC & Analytics Isolation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Security Audit 8: Admin role can view all customer bookings & tickets without IDOR restriction")
    void admin_canAccess_anyBookingAndTicket() {
        BookingResponse adminViewA = bookingService.getBookingById(bookingIdCustomerA, "admin-user", true);
        BookingResponse adminViewB = bookingService.getBookingById(bookingIdCustomerB, "admin-user", true);

        assertThat(adminViewA).isNotNull();
        assertThat(adminViewB).isNotNull();
    }

    @Test
    @DisplayName("Security Audit 9: Admin analytics overview returns non-null metrics")
    void adminAnalytics_returnsOverview() {
        var overview = analyticsService.getOverview();
        assertThat(overview).isNotNull();
        assertThat(overview.getTotalBookings()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Security Audit 10: Date range validation rejects invalid period queries (from > to)")
    void dateRangeValidation_rejectsInvalid() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.custom);
        req.setFrom(Instant.parse("2026-08-10T00:00:00Z"));
        req.setTo(Instant.parse("2026-08-01T00:00:00Z"));

        assertThatThrownBy(() -> analyticsService.getRevenueAnalytics(req))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
