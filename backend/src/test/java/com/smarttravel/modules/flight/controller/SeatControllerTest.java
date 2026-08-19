package com.smarttravel.modules.flight.controller;

import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.flight.dto.SeatDto;
import com.smarttravel.modules.flight.dto.SeatMapResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.SeatStatus;
import com.smarttravel.modules.flight.service.SeatMapService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatController.class)
@AutoConfigureMockMvc(addFilters = false)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatMapService seatMapService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/v1/flights/{flightId}/seat-map returns 200 with layout")
    void testGetFlightSeatMap() throws Exception {
        SeatDto seat = SeatDto.builder().seatNumber("12A").rowNumber(12).column("A")
                .cabinClass(CabinClass.ECONOMY).status(SeatStatus.AVAILABLE).priceAdjustment(BigDecimal.ZERO).build();

        SeatMapResponse response = SeatMapResponse.builder()
                .flightId("fl-100")
                .flightNumber("ST-101")
                .aircraftModel("Boeing 737 MAX 8")
                .totalSeats(1)
                .availableSeatsCount(1)
                .seats(List.of(seat))
                .cabinSeats(Map.of(CabinClass.ECONOMY, List.of(seat)))
                .build();

        when(seatMapService.getFlightSeatMap("fl-100")).thenReturn(response);

        mockMvc.perform(get("/api/v1/flights/fl-100/seat-map")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flightNumber").value("ST-101"))
                .andExpect(jsonPath("$.data.totalSeats").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/flights/{flightId}/seats returns 200 with seat list")
    void testGetSeats() throws Exception {
        SeatDto seat = SeatDto.builder().seatNumber("12A").rowNumber(12).column("A")
                .cabinClass(CabinClass.ECONOMY).status(SeatStatus.AVAILABLE).priceAdjustment(BigDecimal.ZERO).build();

        when(seatMapService.getSeatsForFlight(eq("fl-100"), eq(CabinClass.ECONOMY))).thenReturn(List.of(seat));

        mockMvc.perform(get("/api/v1/flights/fl-100/seats?cabinClass=ECONOMY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].seatNumber").value("12A"));
    }
}
