package com.smarttravel.modules.ticket;

import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.ticket.model.PassengerTicketInfo;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Ticket PDF Integration Test")
class TicketPdfIntegrationTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    @DisplayName("Should generate and stream binary PDF document from stored database ticket")
    void shouldGeneratePdfFromDatabaseTicket() {
        String testSuffix = String.valueOf(System.currentTimeMillis());

        AirportInfo dep = new AirportInfo("DEL", "Indira Gandhi International Airport", "Delhi", "India", "3", "42B");
        AirportInfo arr = new AirportInfo("BOM", "Chhatrapati Shivaji Maharaj Airport", "Mumbai", "India", "2", "18A");

        PassengerTicketInfo p = PassengerTicketInfo.builder()
                .title("Dr")
                .firstName("Robert")
                .lastName("Neville")
                .dateOfBirth(LocalDate.of(1980, 11, 10))
                .gender("MALE")
                .nationality("Indian")
                .seatNumber("12F")
                .eTicketNumber("ST-PDFTEST-" + testSuffix + "-01")
                .build();

        FareBreakdownDto fare = FareBreakdownDto.builder()
                .baseFare(new BigDecimal("5000.00"))
                .taxes(new BigDecimal("800.00"))
                .fees(new BigDecimal("350.00"))
                .totalAmount(new BigDecimal("6150.00"))
                .build();

        Ticket ticket = Ticket.builder()
                .ticketNumber("ST-PDFTEST-" + testSuffix)
                .bookingId("bk_pdf_" + testSuffix)
                .bookingReference("PDF" + testSuffix.substring(Math.max(0, testSuffix.length() - 5)))
                .userId("user_pdf_" + testSuffix)
                .userEmail("robert@example.com")
                .flightId("fl_pdf_" + testSuffix)
                .flightNumber("ST-505")
                .airline("SmartAir")
                .airlineCode("SE")
                .aircraftModel("Airbus A321neo")
                .departureAirport(dep)
                .arrivalAirport(arr)
                .departureTime(Instant.now().plusSeconds(86400))
                .arrivalTime(Instant.now().plusSeconds(93600))
                .durationMinutes(120)
                .cabinClass(CabinClass.BUSINESS)
                .passengerCount(1)
                .passengers(List.of(p))
                .fareBreakdown(fare)
                .totalAmount(new BigDecimal("6150.00"))
                .currency("INR")
                .status(TicketStatus.ISSUED)
                .paymentId("pay_pdf_" + testSuffix)
                .razorpayPaymentId("pay_live_pdf_" + testSuffix)
                .issuedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        byte[] pdfBytes = ticketService.generateTicketPdf(savedTicket.getId(), savedTicket.getUserId(), false);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(1500);

        // Verify PDF Header Magic Number
        String magicHeader = new String(pdfBytes, 0, 5);
        assertThat(magicHeader).isEqualTo("%PDF-");
    }
}
