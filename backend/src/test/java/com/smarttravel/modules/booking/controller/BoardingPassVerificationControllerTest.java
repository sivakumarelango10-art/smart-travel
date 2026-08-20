package com.smarttravel.modules.booking.controller;

import com.smarttravel.modules.booking.model.BoardingPass;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BoardingPassRepository;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.flight.model.CabinClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BoardingPassVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardingPassRepository boardingPassRepository;

    @MockBean
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("GET /api/v1/boarding-passes/verify returns valid true for active confirmed boarding pass")
    void testVerifyBoardingPassSuccess() throws Exception {
        String bpNumber = "BP-ST8K4P2Q-10F";
        BoardingPass bp = BoardingPass.builder()
                .boardingPassNumber(bpNumber)
                .bookingReference("ST8K4P2Q")
                .bookingId("booking-101")
                .passengerName("Sarah Connor")
                .flightNumber("6E-2041")
                .airline("IndiGo")
                .seatNumber("10F")
                .cabinClass(CabinClass.ECONOMY)
                .gate("Gate 08")
                .terminal("T3")
                .boardingGroup("Group 1")
                .build();

        Booking booking = Booking.builder()
                .id("booking-101")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(boardingPassRepository.findByBoardingPassNumber(eq(bpNumber))).thenReturn(Optional.of(bp));
        when(bookingRepository.findById(eq("booking-101"))).thenReturn(Optional.of(booking));

        mockMvc.perform(get("/api/v1/boarding-passes/verify")
                        .param("token", "STBP|" + bpNumber + "|ST8K4P2Q|6E-2041|10F|Sarah_Connor")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.passengerName").value("Sarah Connor"))
                .andExpect(jsonPath("$.data.seatNumber").value("10F"))
                .andExpect(jsonPath("$.data.gate").value("Gate 08"));
    }

    @Test
    @DisplayName("GET /api/v1/boarding-passes/verify returns valid false for cancelled booking")
    void testVerifyBoardingPassCancelled() throws Exception {
        String bpNumber = "BP-CANCELLED-01";
        BoardingPass bp = BoardingPass.builder()
                .boardingPassNumber(bpNumber)
                .bookingReference("ST8K4P2Q")
                .bookingId("booking-cancelled")
                .passengerName("Sarah Connor")
                .flightNumber("6E-2041")
                .build();

        Booking booking = Booking.builder()
                .id("booking-cancelled")
                .status(BookingStatus.CANCELLED)
                .build();

        when(boardingPassRepository.findByBoardingPassNumber(eq(bpNumber))).thenReturn(Optional.of(bp));
        when(bookingRepository.findById(eq("booking-cancelled"))).thenReturn(Optional.of(booking));

        mockMvc.perform(get("/api/v1/boarding-passes/verify")
                        .param("token", bpNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("GET /api/v1/boarding-passes/verify returns bad request for missing or blank token")
    void testVerifyBoardingPassMissingToken() throws Exception {
        mockMvc.perform(get("/api/v1/boarding-passes/verify")
                        .param("token", "   ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/boarding-passes/verify returns valid false for unknown/unrecognized token")
    void testVerifyBoardingPassUnrecognizedToken() throws Exception {
        when(boardingPassRepository.findByBoardingPassNumber(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/boarding-passes/verify")
                        .param("token", "BP-NON-EXISTENT-999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }
}
