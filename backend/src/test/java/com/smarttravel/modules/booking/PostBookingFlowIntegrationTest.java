package com.smarttravel.modules.booking;

import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.booking.dto.BoardingPassResponse;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.CheckInResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.model.CheckInStatus;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.booking.service.CheckInService;

import com.smarttravel.modules.flight.dto.SeatMapResponse;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.model.Seat;
import com.smarttravel.modules.flight.model.SeatStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.SeatRepository;
import com.smarttravel.modules.flight.service.SeatMapService;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.payment.service.PaymentReconciliationService;


import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.repository.TicketRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class PostBookingFlowIntegrationTest {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatMapService seatMapService;

    @Autowired
    private BookingService bookingService;


    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentReconciliationService paymentReconciliationService;


    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CheckInService checkInService;

    private String flightId;
    private final String userId = "user-flow-" + System.currentTimeMillis();
    private final String userEmail = "flow_" + System.currentTimeMillis() + "@smarttravel.com";

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        CabinInventory econInv = CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(150)
                .availableSeats(150)
                .basePrice(new BigDecimal("4500.00"))
                .taxAmount(new BigDecimal("540.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5190.00"))
                .build();

        Flight flight = Flight.builder()
                .flightNumber("FLOW-FL-" + ts)
                .airline("SmartTravel Express")
                .airlineCode("STE")
                .aircraftModel("Boeing 737 MAX 8")
                .totalSeats(150)
                .availableSeats(150)
                .basePrice(new BigDecimal("4500.00"))
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econInv))
                .status(FlightStatus.SCHEDULED)
                .departureAirport(AirportInfo.builder().code("DEL").name("Delhi").city("Delhi").country("India").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").name("Mumbai").city("Mumbai").country("India").build())
                .departureTime(Instant.now().plusSeconds(3600 * 12)) // 12 hours from now (in check-in window)
                .arrivalTime(Instant.now().plusSeconds(3600 * 14))
                .active(true)
                .build();

        Flight saved = flightRepository.save(flight);
        flightId = saved.getId();
    }

    @Test
    @DisplayName("Complete Post-Booking Flow: Seat Map -> Book with 12A -> Payment -> Auto Ticket -> Check-In -> Boarding Pass -> Cancel -> Seat Released")
    void testCompletePostBookingLifecycle() {
        // 1. Retrieve Seat Map
        SeatMapResponse seatMap = seatMapService.getFlightSeatMap(flightId);
        assertThat(seatMap).isNotNull();
        assertThat(seatMap.getSeats()).isNotEmpty();

        // 2. Create Booking with selected seat 12A
        PassengerDto passenger = new PassengerDto(
                "Ms", "Sarah", "Connor", LocalDate.of(1995, 5, 20),
                "FEMALE", "Indian", "P9876543", "12A"
        );

        BookingCreateRequest createReq = new BookingCreateRequest(flightId, CabinClass.ECONOMY, List.of(passenger));
        BookingResponse booking = bookingService.createBooking(createReq, userId, userEmail);

        assertThat(booking).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        String bookingId = booking.getId();

        // Verify Seat 12A is HELD
        Seat seat12A = seatRepository.findByFlightIdAndSeatNumber(flightId, "12A").orElseThrow();
        assertThat(seat12A.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(seat12A.getBookingId()).isEqualTo(bookingId);

        // 3. User My Bookings query with status filter
        PageResponse<BookingResponse> myBookings = bookingService.getUserBookings(userId, BookingStatus.CONFIRMED, PageRequest.of(0, 10));
        assertThat(myBookings.getContent()).hasSize(1);
        assertThat(myBookings.getContent().get(0).getId()).isEqualTo(bookingId);

        // 4. Create Payment and Reconcile Webhook
        String rzpOrderId = "order_flow_" + System.currentTimeMillis();
        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .bookingReference(booking.getBookingReference())
                .userId(userId)
                .razorpayOrderId(rzpOrderId)
                .amount(booking.getTotalAmount())
                .amountPaise(519000L)
                .currency("INR")
                .paymentStatus(PaymentStatus.ORDER_CREATED)
                .createdAt(Instant.now())
                .build();
        paymentRepository.save(payment);

        Payment reconciled = paymentReconciliationService.reconcilePaymentSuccess(
                rzpOrderId, "pay_flow_" + System.currentTimeMillis(), 519000L, "INR", "payment.captured"
        );
        assertThat(reconciled.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);

        // Verify Seat 12A is BOOKED
        seat12A = seatRepository.findByFlightIdAndSeatNumber(flightId, "12A").orElseThrow();
        assertThat(seat12A.getStatus()).isEqualTo(SeatStatus.BOOKED);

        // Verify Ticket is ISSUED
        Ticket ticket = ticketRepository.findFirstByBookingId(bookingId).orElseThrow();
        assertThat(ticket.getTicketNumber()).startsWith("ST-");

        // 5. Perform Check-In
        CheckInResponse checkIn = checkInService.performCheckIn(bookingId, null, userId, false);
        assertThat(checkIn).isNotNull();
        assertThat(checkIn.getStatus()).isEqualTo(CheckInStatus.COMPLETED);
        assertThat(checkIn.getPassengers().get(0).getSeatNumber()).isEqualTo("12A");
        assertThat(checkIn.getPassengers().get(0).getBoardingPassNumber()).startsWith("BP-");

        // 6. Retrieve Boarding Passes (JSON)
        List<BoardingPassResponse> passes = checkInService.getBoardingPasses(bookingId, userId, false);
        assertThat(passes).hasSize(1);
        assertThat(passes.get(0).getPassengerName()).isEqualTo("Ms Sarah Connor");
        assertThat(passes.get(0).getSeatNumber()).isEqualTo("12A");

        // 7. Download Boarding Pass (PDF)
        byte[] pdfBytes = checkInService.getBoardingPassPdf(bookingId, userId, false);
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(1500);
        String pdfHeader = new String(pdfBytes, 0, 5);
        assertThat(pdfHeader).isEqualTo("%PDF-");

        // 8. Cancel Booking -> Verify Seat 12A is released back to AVAILABLE
        BookingResponse cancelled = bookingService.cancelBooking(bookingId, null, userId, false);
        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        seat12A = seatRepository.findByFlightIdAndSeatNumber(flightId, "12A").orElseThrow();
        assertThat(seat12A.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seat12A.getBookingId()).isNull();
    }
}
