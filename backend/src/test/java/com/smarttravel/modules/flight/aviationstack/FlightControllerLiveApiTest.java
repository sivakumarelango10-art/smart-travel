package com.smarttravel.modules.flight.aviationstack;

import com.smarttravel.modules.flight.controller.FlightController;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot;
import com.smarttravel.modules.flight.service.FlightService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FlightController.class)
@AutoConfigureMockMvc(addFilters = false)
class FlightControllerLiveApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    @MockBean
    private com.smarttravel.common.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.smarttravel.common.security.CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("1. GET /api/v1/flights/live/{flightNumber} returns live status snapshot with data provenance")
    void testGetLiveFlightStatusEndpoint() throws Exception {
        FlightStatusSnapshot snapshot = new FlightStatusSnapshot(
                "AI-101",
                FlightStatus.ON_TIME,
                0,
                null,
                Instant.parse("2026-08-21T10:00:00Z"),
                Instant.parse("2026-08-21T12:15:00Z"),
                "Gate 7",
                "T3",
                "AVIATIONSTACK"
        );

        when(flightService.getLiveFlightStatus(eq("AI-101"))).thenReturn(snapshot);

        mockMvc.perform(get("/api/v1/flights/live/AI-101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flightNumber").value("AI-101"))
                .andExpect(jsonPath("$.data.status").value("ON_TIME"))
                .andExpect(jsonPath("$.data.gate").value("Gate 7"))
                .andExpect(jsonPath("$.data.terminal").value("T3"))
                .andExpect(jsonPath("$.data.updatedSource").value("AVIATIONSTACK"));
    }
}
