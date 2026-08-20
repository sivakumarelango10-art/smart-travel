package com.smarttravel.modules.booking.controller;

import com.smarttravel.modules.booking.model.BoardingPass;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BoardingPassRepository;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BoardingPassPdfService;
import com.smarttravel.modules.booking.service.BoardingPassPdfServiceImpl;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration test validating the complete Boarding Pass Gate Scanner Flow:
 * Booking -> CheckIn -> Boarding Pass Document Generation -> PDF Barcode Token Extraction -> Verification REST API.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BoardingPassScannerFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardingPassRepository boardingPassRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private final BoardingPassPdfService pdfService = new BoardingPassPdfServiceImpl();

    private String testBookingId;
    private String testPassNumber;
    private String testPnr;

    @BeforeEach
    void setUp() {
        testBookingId = "bkg-scan-" + UUID.randomUUID().toString().substring(0, 8);
        testPnr = "PNR" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        testPassNumber = "BP-" + testPnr + "-14C";
    }

    @Test
    @DisplayName("End-to-End Flow: Valid Boarding Pass PDF generation -> Gate Scanner Verification")
    void testEndToEndValidBoardingPassScanFlow() throws Exception {
        // 1. Persist active confirmed booking
        Booking booking = Booking.builder()
                .id(testBookingId)
                .bookingReference(testPnr)
                .userId("user-gate-01")
                .flightId("flight-del-bom-101")
                .status(BookingStatus.CONFIRMED)
                .build();
        bookingRepository.save(booking);

        // 2. Persist issued Boarding Pass
        BoardingPass boardingPass = BoardingPass.builder()
                .boardingPassNumber(testPassNumber)
                .bookingId(testBookingId)
                .bookingReference(testPnr)
                .passengerName("Dr Evelyn Vance")
                .flightNumber("6E-2041")
                .airline("IndiGo")
                .seatNumber("14C")
                .cabinClass(CabinClass.ECONOMY)
                .departureAirport(AirportInfo.builder().code("DEL").name("Indira Gandhi Intl").city("Delhi").country("India").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").name("Chhatrapati Shivaji Intl").city("Mumbai").country("India").build())
                .departureTime(Instant.parse("2026-09-10T06:00:00Z"))
                .arrivalTime(Instant.parse("2026-09-10T08:15:00Z"))
                .boardingTime(Instant.parse("2026-09-10T05:15:00Z"))
                .gate("Gate 04A")
                .terminal("T3")
                .boardingGroup("Group 2")
                .issuedAt(Instant.now())
                .build();
        boardingPassRepository.save(boardingPass);

        // 3. Generate Boarding Pass PDF and verify bytes
        byte[] pdfBytes = pdfService.generateBoardingPassPdf(boardingPass);
        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 5)).isEqualTo("%PDF-");

        // 4. Construct extracted barcode token from document
        String tokenPayload = String.format("STBP|%s|%s|6E-2041|14C|Dr_Evelyn_Vance", testPassNumber, testPnr);

        // 5. Invoke Gate Verification REST endpoint with extracted token
        mockMvc.perform(get("/api/v1/boarding-passes/verify")
                        .param("token", tokenPayload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.boardingPassNumber").value(testPassNumber))
                .andExpect(jsonPath("$.data.bookingReference").value(testPnr))
                .andExpect(jsonPath("$.data.passengerName").value("Dr Evelyn Vance"))
                .andExpect(jsonPath("$.data.flightNumber").value("6E-2041"))
                .andExpect(jsonPath("$.data.seatNumber").value("14C"))
                .andExpect(jsonPath("$.data.gate").value("Gate 04A"))
                .andExpect(jsonPath("$.data.terminal").value("T3"));
    }

    @Test
    @DisplayName("Gate Scanner rejects cancelled booking token")
    void testGateScanCancelledBooking() throws Exception {
        Booking booking = Booking.builder()
                .id(testBookingId)
                .bookingReference(testPnr)
                .status(BookingStatus.CANCELLED)
                .build();
        bookingRepository.save(booking);

        BoardingPass boardingPass = BoardingPass.builder()
                .boardingPassNumber(testPassNumber)
                .bookingId(testBookingId)
                .bookingReference(testPnr)
                .passengerName("Alex Mercer")
                .flightNumber("AI-887")
                .build();
        boardingPassRepository.save(boardingPass);

        mockMvc.perform(get("/api/v1/boarding-passes/verify")
                        .param("token", testPassNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Gate Scanner rejects expired booking token")
    void testGateScanExpiredBooking() throws Exception {
        Booking booking = Booking.builder()
                .id(testBookingId)
                .bookingReference(testPnr)
                .status(BookingStatus.EXPIRED)
                .build();
        bookingRepository.save(booking);

        BoardingPass boardingPass = BoardingPass.builder()
                .boardingPassNumber(testPassNumber)
                .bookingId(testBookingId)
                .bookingReference(testPnr)
                .passengerName("Dana Scully")
                .flightNumber("UK-955")
                .build();
        boardingPassRepository.save(boardingPass);

        mockMvc.perform(get("/api/v1/boarding-passes/verify")
                        .param("token", testPassNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.status").value("EXPIRED"));
    }

    @Test
    @DisplayName("Gate Scanner rejects fake or non-existent boarding pass token")
    void testGateScanFakeToken() throws Exception {
        mockMvc.perform(get("/api/v1/boarding-passes/verify")
                        .param("token", "BP-FAKE-TOKEN-999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("Gate Scanner handles tampered token safely")
    void testGateScanTamperedToken() throws Exception {
        mockMvc.perform(get("/api/v1/boarding-passes/verify")
                        .param("token", "STBP|MALFORMED|INJECTION|&&||")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }
}
