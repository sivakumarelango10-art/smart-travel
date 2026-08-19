package com.smarttravel.modules.booking.service;

import com.smarttravel.modules.booking.model.BoardingPass;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoardingPassPdfServiceTest {

    private final BoardingPassPdfService pdfService = new BoardingPassPdfServiceImpl();

    @Test
    @DisplayName("generateBoardingPassPdf generates valid PDF bytes with %PDF- header")
    void testGenerateBoardingPassPdf() {
        BoardingPass bp = BoardingPass.builder()
                .boardingPassNumber("BP-TEST98765432")
                .bookingReference("ST8K4P2Q")
                .ticketNumber("ST-MW827QQJRL45")
                .eTicketNumber("ST-MW827QQJRL45-01")
                .passengerName("Ms Sarah Connor")
                .seatNumber("12A")
                .cabinClass(CabinClass.ECONOMY)
                .flightNumber("ST-101")
                .airline("SmartTravel Airways")
                .departureAirport(AirportInfo.builder().code("DEL").name("Indira Gandhi Intl").city("Delhi").country("India").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").name("Chhatrapati Shivaji Intl").city("Mumbai").country("India").build())
                .departureTime(Instant.parse("2026-09-01T10:00:00Z"))
                .arrivalTime(Instant.parse("2026-09-01T12:00:00Z"))
                .boardingTime(Instant.parse("2026-09-01T09:15:00Z"))
                .gate("Gate 12")
                .terminal("T3")
                .boardingGroup("Group 1")
                .issuedAt(Instant.now())
                .build();

        byte[] pdf = pdfService.generateBoardingPassPdf(bp);

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(1000);

        String header = new String(pdf, 0, 5);
        assertThat(header).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("generateBoardingPassPdf throws IllegalArgumentException when null")
    void testGenerateBoardingPassPdfNull() {
        assertThatThrownBy(() -> pdfService.generateBoardingPassPdf(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
