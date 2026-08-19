package com.smarttravel.modules.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.booking.dto.BoardingPassResponse;
import com.smarttravel.modules.booking.dto.CheckInResponse;
import com.smarttravel.modules.booking.dto.PassengerCheckInResponse;
import com.smarttravel.modules.booking.model.CheckInStatus;
import com.smarttravel.modules.booking.service.CheckInService;
import com.smarttravel.modules.flight.model.CabinClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckInController.class)
@AutoConfigureMockMvc(addFilters = false)
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckInService checkInService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;


    @Test
    @DisplayName("POST /api/v1/bookings/{bookingId}/check-in returns 201 with CheckInResponse")
    void testPerformCheckIn() throws Exception {
        PassengerCheckInResponse p = PassengerCheckInResponse.builder()
                .title("Ms")
                .passengerName("Sarah Connor")
                .seatNumber("12A")
                .cabinClass(CabinClass.ECONOMY)
                .eTicketNumber("ST-MW827QQJRL45-01")
                .boardingPassNumber("BP-TEST98765432")
                .build();

        CheckInResponse response = CheckInResponse.builder()
                .id("ci-100")
                .checkInNumber("CI-TEST98765432")
                .bookingId("bk-100")
                .bookingReference("ST8K4P2Q")
                .flightNumber("ST-101")
                .status(CheckInStatus.COMPLETED)
                .checkedInAt(Instant.now())
                .passengers(List.of(p))
                .build();

        when(checkInService.performCheckIn(eq("bk-100"), any(), anyString(), anyBoolean()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/bookings/bk-100/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.checkInNumber").value("CI-TEST98765432"))
                .andExpect(jsonPath("$.data.passengers[0].seatNumber").value("12A"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{bookingId}/boarding-pass returns 200 with list of boarding passes")
    void testGetBoardingPasses() throws Exception {
        BoardingPassResponse bp = BoardingPassResponse.builder()
                .id("bp-100")
                .boardingPassNumber("BP-TEST98765432")
                .bookingReference("ST8K4P2Q")
                .passengerName("Sarah Connor")
                .seatNumber("12A")
                .cabinClass(CabinClass.ECONOMY)
                .flightNumber("ST-101")
                .build();

        when(checkInService.getBoardingPasses(eq("bk-100"), anyString(), anyBoolean()))
                .thenReturn(List.of(bp));

        mockMvc.perform(get("/api/v1/bookings/bk-100/boarding-pass")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].boardingPassNumber").value("BP-TEST98765432"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{bookingId}/boarding-pass/pdf returns PDF binary stream")
    void testDownloadBoardingPassPdf() throws Exception {
        byte[] fakePdf = "%PDF-1.4 Fake PDF Content".getBytes();

        when(checkInService.getBoardingPassPdf(eq("bk-100"), anyString(), anyBoolean()))
                .thenReturn(fakePdf);

        mockMvc.perform(get("/api/v1/bookings/bk-100/boarding-pass/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("SmartTravel-BoardingPass-bk-100.pdf")));
    }
}
