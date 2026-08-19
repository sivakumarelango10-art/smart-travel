package com.smarttravel.modules.ticket.service;

import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.model.Passenger;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.ticket.dto.TicketResponse;
import com.smarttravel.modules.ticket.mapper.TicketMapper;
import com.smarttravel.modules.ticket.model.PassengerTicketInfo;
import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.model.TicketStatus;
import com.smarttravel.modules.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService Unit Tests")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TicketNumberGenerator ticketNumberGenerator;

    @Mock
    private TicketPdfService ticketPdfService;

    private final TicketMapper ticketMapper = new TicketMapper();

    private TicketService ticketService;

    private Booking confirmedBooking;
    private Payment verifiedPayment;
    private Ticket existingTicket;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(
                ticketRepository,
                bookingRepository,
                paymentRepository,
                ticketNumberGenerator,
                ticketPdfService,
                ticketMapper
        );

        AirportInfo dep = AirportInfo.builder().code("DEL").name("Delhi Airport").city("Delhi").country("India").build();
        AirportInfo arr = AirportInfo.builder().code("BOM").name("Mumbai Airport").city("Mumbai").country("India").build();

        Passenger passenger = Passenger.builder()
                .title("Ms")
                .firstName("Sarah")
                .lastName("Connor")
                .dateOfBirth(LocalDate.of(1995, 3, 15))
                .gender("FEMALE")
                .nationality("Indian")
                .seatNumber("12A")
                .build();

        FareBreakdownDto fare = FareBreakdownDto.builder()
                .baseFare(new BigDecimal("5000.00"))
                .taxes(new BigDecimal("600.00"))
                .fees(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .passengerCount(1)
                .build();

        confirmedBooking = Booking.builder()
                .id("bk-001")
                .bookingReference("ST8K4P2Q")
                .userId("usr-sarah")
                .userEmail("sarah@smarttravel.com")
                .flightId("fl-001")
                .flightNumber("AI-202")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(dep)
                .arrivalAirport(arr)
                .departureTime(Instant.now().plusSeconds(86400))
                .arrivalTime(Instant.now().plusSeconds(93600))
                .durationMinutes(120)
                .cabinClass(CabinClass.ECONOMY)
                .passengerCount(1)
                .passengers(List.of(passenger))
                .fareBreakdown(fare)
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .status(BookingStatus.CONFIRMED)
                .createdAt(Instant.now())
                .build();

        verifiedPayment = Payment.builder()
                .id("pay-001")
                .bookingId("bk-001")
                .userId("usr-sarah")
                .amount(new BigDecimal("5750.00"))
                .amountPaise(575000L)
                .currency("INR")
                .paymentStatus(PaymentStatus.VERIFIED)
                .razorpayPaymentId("pay_live_test_123")
                .build();

        PassengerTicketInfo tktPax = PassengerTicketInfo.builder()
                .eTicketNumber("ST-8K4P2Q7X9Y1Z-01")
                .title("Ms")
                .firstName("Sarah")
                .lastName("Connor")
                .dateOfBirth(LocalDate.of(1995, 3, 15))
                .gender("FEMALE")
                .nationality("Indian")
                .seatNumber("12A")
                .build();

        existingTicket = Ticket.builder()
                .id("tkt-001")
                .ticketNumber("ST-8K4P2Q7X9Y1Z")
                .bookingId("bk-001")
                .bookingReference("ST8K4P2Q")
                .userId("usr-sarah")
                .userEmail("sarah@smarttravel.com")
                .flightId("fl-001")
                .flightNumber("AI-202")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(dep)
                .arrivalAirport(arr)
                .departureTime(confirmedBooking.getDepartureTime())
                .arrivalTime(confirmedBooking.getArrivalTime())
                .durationMinutes(120)
                .cabinClass(CabinClass.ECONOMY)
                .passengerCount(1)
                .passengers(List.of(tktPax))
                .fareBreakdown(fare)
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .status(TicketStatus.ISSUED)
                .paymentId("pay-001")
                .razorpayPaymentId("pay_live_test_123")
                .issuedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully issue a new ticket for confirmed booking with verified payment")
    void shouldIssueNewTicketSuccessfully() {
        when(ticketRepository.findFirstByBookingId("bk-001")).thenReturn(Optional.empty());
        when(bookingRepository.findById("bk-001")).thenReturn(Optional.of(confirmedBooking));
        when(paymentRepository.findByBookingId("bk-001")).thenReturn(List.of(verifiedPayment));
        when(ticketNumberGenerator.generateTicketNumber()).thenReturn("ST-8K4P2Q7X9Y1Z");
        when(ticketNumberGenerator.generatePassengerTicketNumber(eq("ST-8K4P2Q7X9Y1Z"), eq(1))).thenReturn("ST-8K4P2Q7X9Y1Z-01");
        when(ticketRepository.save(any(Ticket.class))).thenReturn(existingTicket);

        TicketResponse response = ticketService.issueTicket("bk-001");

        assertThat(response).isNotNull();
        assertThat(response.getTicketNumber()).isEqualTo("ST-8K4P2Q7X9Y1Z");
        assertThat(response.getBookingReference()).isEqualTo("ST8K4P2Q");
        assertThat(response.getStatus()).isEqualTo(TicketStatus.ISSUED);
        assertThat(response.getPassengers()).hasSize(1);
        assertThat(response.getPassengers().get(0).getETicketNumber()).isEqualTo("ST-8K4P2Q7X9Y1Z-01");
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should return existing ticket when issueTicket is invoked idempotently")
    void shouldReturnExistingTicketIdempotently() {
        when(ticketRepository.findFirstByBookingId("bk-001")).thenReturn(Optional.of(existingTicket));

        TicketResponse response = ticketService.issueTicket("bk-001");

        assertThat(response).isNotNull();
        assertThat(response.getTicketNumber()).isEqualTo("ST-8K4P2Q7X9Y1Z");
        verify(bookingRepository, never()).findById(any());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject ticket issuance if booking is in PENDING status")
    void shouldRejectIssuanceForPendingBooking() {
        confirmedBooking.setStatus(BookingStatus.PENDING);
        when(ticketRepository.findFirstByBookingId("bk-001")).thenReturn(Optional.empty());
        when(bookingRepository.findById("bk-001")).thenReturn(Optional.of(confirmedBooking));

        assertThatThrownBy(() -> ticketService.issueTicket("bk-001"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Booking must be CONFIRMED");
    }

    @Test
    @DisplayName("Should reject ticket issuance if booking is CANCELLED")
    void shouldRejectIssuanceForCancelledBooking() {
        confirmedBooking.setStatus(BookingStatus.CANCELLED);
        when(ticketRepository.findFirstByBookingId("bk-001")).thenReturn(Optional.empty());
        when(bookingRepository.findById("bk-001")).thenReturn(Optional.of(confirmedBooking));

        assertThatThrownBy(() -> ticketService.issueTicket("bk-001"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot issue ticket for CANCELLED booking");
    }

    @Test
    @DisplayName("Should fetch ticket by ID with IDOR protection for user")
    void shouldFetchTicketByIdWithOwnership() {
        when(ticketRepository.findFirstByIdAndUserId("tkt-001", "usr-sarah")).thenReturn(Optional.of(existingTicket));

        TicketResponse response = ticketService.getTicketById("tkt-001", "usr-sarah", false);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("tkt-001");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when unauthorized user attempts to access ticket")
    void shouldThrowNotFoundOnUnauthorizedAccess() {
        when(ticketRepository.findFirstByIdAndUserId("tkt-001", "usr-hacker")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketById("tkt-001", "usr-hacker", false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should allow admin to access any ticket by ID")
    void shouldAllowAdminToAccessAnyTicket() {
        when(ticketRepository.findById("tkt-001")).thenReturn(Optional.of(existingTicket));

        TicketResponse response = ticketService.getTicketById("tkt-001", null, true);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("tkt-001");
    }

    @Test
    @DisplayName("Should cancel ticket when booking is cancelled")
    void shouldCancelTicketForBooking() {
        when(ticketRepository.findFirstByBookingId("bk-001")).thenReturn(Optional.of(existingTicket));

        ticketService.cancelTicketForBooking("bk-001", "Passenger requested cancellation");

        assertThat(existingTicket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(existingTicket.getCancellationReason()).isEqualTo("Passenger requested cancellation");
        assertThat(existingTicket.getCancelledAt()).isNotNull();
        verify(ticketRepository, times(1)).save(existingTicket);
    }
}
