package com.smarttravel.modules.ticket;

import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.payment.dto.PaymentOrderCreateRequest;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.service.PaymentReconciliationService;
import com.smarttravel.modules.payment.service.PaymentService;
import com.smarttravel.modules.ticket.dto.TicketResponse;
import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.model.TicketStatus;
import com.smarttravel.modules.ticket.repository.TicketRepository;
import com.smarttravel.modules.ticket.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Ticket Integration Test (Full Flow)")
class TicketIntegrationTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentReconciliationService reconciliationService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Complete Ticketing Flow: Flight -> Booking -> Payment -> Webhook Reconciliation -> Ticket Issuance -> Retrieval")
    void testCompleteTicketingLifecycle() {
        String testSuffix = String.valueOf(System.currentTimeMillis());

        // 1. Create Flight
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
                .flightNumber("TEST-TKT-" + testSuffix)
                .airline("SmartTravel Wings")
                .airlineCode("SW")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.now().plusSeconds(86400))
                .arrivalTime(Instant.now().plusSeconds(93600))
                .aircraftModel("A320neo")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(100)
                .availableSeats(100)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econ))
                .status(FlightStatus.SCHEDULED)
                .build();
        FlightResponse flight = flightService.createFlight(flightReq);

        // 2. Create Booking
        PassengerDto p = PassengerDto.builder()
                .title("Ms")
                .firstName("Sarah")
                .lastName("Connor")
                .dateOfBirth(LocalDate.of(1995, 3, 15))
                .gender("FEMALE")
                .nationality("Indian")
                .build();
        BookingCreateRequest bookingReq = BookingCreateRequest.builder()
                .flightId(flight.getId())
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(p))
                .build();
        BookingResponse booking = bookingService.createBooking(bookingReq, "user-sarah", "sarah@smarttravel.com");

        // 3. Initiate Payment
        PaymentOrderCreateRequest orderReq = new PaymentOrderCreateRequest(booking.getId(), "Flight payment");
        var orderResponse = paymentService.createPaymentOrder(orderReq, "user-sarah", "sarah@smarttravel.com");
        String rzpOrderId = orderResponse.getRazorpayOrderId();

        // 4. Reconcile Payment Success (simulating webhook delivery)
        reconciliationService.reconcilePaymentSuccess(rzpOrderId, "pay_tkt_test_" + testSuffix, 575000L, "INR", "payment.captured");

        // 5. Verify Booking is CONFIRMED
        Booking confirmedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(confirmedBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        // 6. Verify Ticket was automatically issued downstream
        Optional<Ticket> ticketOpt = ticketRepository.findFirstByBookingId(booking.getId());
        assertThat(ticketOpt).isPresent();

        Ticket ticket = ticketOpt.get();
        assertThat(ticket.getTicketNumber()).startsWith("ST-");
        assertThat(ticket.getBookingReference()).isEqualTo(booking.getBookingReference());
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ISSUED);
        assertThat(ticket.getPassengers()).hasSize(1);
        assertThat(ticket.getPassengers().get(0).getFirstName()).isEqualTo("Sarah");
        assertThat(ticket.getPassengers().get(0).getETicketNumber()).isNotNull();

        // 7. Verify Ticket Retrieval via Service
        TicketResponse ticketResponse = ticketService.getTicketById(ticket.getId(), "user-sarah", false);
        assertThat(ticketResponse).isNotNull();
        assertThat(ticketResponse.getTicketNumber()).isEqualTo(ticket.getTicketNumber());

        // 8. Verify PDF generation from ticket snapshot
        byte[] pdfBytes = ticketService.generateTicketPdf(ticket.getId(), "user-sarah", false);
        assertThat(pdfBytes).isNotNull();
        assertThat(new String(pdfBytes, 0, 5)).isEqualTo("%PDF-");
    }
}
