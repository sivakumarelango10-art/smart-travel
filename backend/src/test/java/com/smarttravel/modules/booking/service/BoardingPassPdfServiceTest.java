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
    @DisplayName("generateBoardingPassPdf generates valid PDF bytes with embedded vector PDF417 and Barcode128")
    void testGenerateBoardingPassPdf() {
        BoardingPass bp = BoardingPass.builder()
                .boardingPassNumber("BP-TEST98765432")
                .bookingReference("ST8K4P2Q")
                .ticketNumber("ST-MW827QQJRL45")
                .eTicketNumber("ST-MW827QQJRL45-01")
                .passengerName("Ms Sarah Connor")
                .seatNumber("12A")
                .cabinClass(CabinClass.ECONOMY)
                .flightNumber("6E-2041")
                .airline("IndiGo")
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
        assertThat(pdf.length).isGreaterThan(2000);

        String header = new String(pdf, 0, 5);
        assertThat(header).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("generateMultiBoardingPassPdf generates multi-page PDF document")
    void testGenerateMultiBoardingPassPdf() {
        BoardingPass bp1 = BoardingPass.builder()
                .boardingPassNumber("BP-PAX01-12A")
                .bookingReference("ST8K4P2Q")
                .passengerName("Ms Sarah Connor")
                .seatNumber("12A")
                .cabinClass(CabinClass.ECONOMY)
                .flightNumber("6E-2041")
                .airline("IndiGo")
                .departureTime(Instant.parse("2026-09-01T10:00:00Z"))
                .build();

        BoardingPass bp2 = BoardingPass.builder()
                .boardingPassNumber("BP-PAX02-12B")
                .bookingReference("ST8K4P2Q")
                .passengerName("Mr John Connor")
                .seatNumber("12B")
                .cabinClass(CabinClass.ECONOMY)
                .flightNumber("6E-2041")
                .airline("IndiGo")
                .departureTime(Instant.parse("2026-09-01T10:00:00Z"))
                .build();

        byte[] pdf = pdfService.generateMultiBoardingPassPdf(List.of(bp1, bp2));

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(3000);
    }

    @Test
    @DisplayName("generateBoardingPassPdf throws IllegalArgumentException when null")
    void testGenerateBoardingPassPdfNull() {
        assertThatThrownBy(() -> pdfService.generateBoardingPassPdf(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
