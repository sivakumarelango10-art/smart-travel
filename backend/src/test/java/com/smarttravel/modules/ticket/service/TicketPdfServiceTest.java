package com.smarttravel.modules.ticket.service;

import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.ticket.model.PassengerTicketInfo;
import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.model.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TicketPdfService Unit Tests")
class TicketPdfServiceTest {

    private TicketPdfServiceImpl pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new TicketPdfServiceImpl();
    }

    @Test
    @DisplayName("Should generate valid binary PDF for complete ticket snapshot")
    void shouldGenerateValidPdfDocument() {
        AirportInfo dep = new AirportInfo("DEL", "Indira Gandhi International", "Delhi", "India", "3", "42B");
        AirportInfo arr = new AirportInfo("BOM", "Chhatrapati Shivaji Maharaj", "Mumbai", "India", "2", "18A");

        PassengerTicketInfo p1 = PassengerTicketInfo.builder()
                .title("Ms")
                .firstName("Sarah")
                .lastName("Connor")
                .dateOfBirth(LocalDate.of(1990, 5, 20))
                .gender("FEMALE")
                .nationality("Indian")
                .seatNumber("14A")
                .eTicketNumber("ST-TEST12345678-01")
                .build();

        FareBreakdownDto fare = FareBreakdownDto.builder()
                .baseFare(new BigDecimal("4500.00"))
                .taxes(new BigDecimal("750.00"))
                .fees(new BigDecimal("500.00"))
                .totalAmount(new BigDecimal("5750.00"))
                .build();

        Ticket ticket = Ticket.builder()
                .id("tkt-001")
                .ticketNumber("ST-TEST12345678")
                .bookingId("bk-001")
                .bookingReference("ST8K4P2Q")
                .userId("usr-001")
                .userEmail("sarah@example.com")
                .flightId("fl-001")
                .flightNumber("ST-302")
                .airline("SmartAir Express")
                .airlineCode("SE")
                .aircraftModel("Boeing 737 MAX 8")
                .departureAirport(dep)
                .arrivalAirport(arr)
                .departureTime(Instant.parse("2026-09-01T06:00:00Z"))
                .arrivalTime(Instant.parse("2026-09-01T08:15:00Z"))
                .durationMinutes(135)
                .cabinClass(CabinClass.ECONOMY)
                .passengerCount(1)
                .passengers(List.of(p1))
                .fareBreakdown(fare)
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .status(TicketStatus.ISSUED)
                .paymentId("pay-001")
                .razorpayPaymentId("pay_RZP123456")
                .issuedAt(Instant.parse("2026-08-18T18:00:00Z"))
                .build();

        byte[] pdfBytes = pdfService.generateTicketPdf(ticket);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(1000);

        // Verify PDF Magic header: %PDF-
        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 10), StandardCharsets.US_ASCII);
        assertThat(header).startsWith("%PDF-");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ticket is null")
    void shouldThrowExceptionWhenTicketIsNull() {
        assertThatThrownBy(() -> pdfService.generateTicketPdf(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ticket cannot be null");
    }
}
