package com.smarttravel.modules.flight.requirement1;

import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.tracking.controller.FlightTrackingController;
import com.smarttravel.modules.flight.tracking.dto.TrackedFlightResponse;
import com.smarttravel.modules.flight.tracking.service.FlightTrackingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Requirement #1 - Test Group F: Frontend REST API Endpoint Audit
 * Verifies track, untrack, list tracked flights, and status inquiry endpoints with authentication and authorization.
 */
@WebMvcTest(FlightTrackingController.class)
@AutoConfigureMockMvc
class FlightTrackingControllerApiAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightTrackingService flightTrackingService;

    @MockBean
    private com.smarttravel.common.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.smarttravel.common.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user-tracker-01")
    @DisplayName("53. Frontend retrieves tracked flights successfully via GET /v1/flights/tracked")
    void testGetTrackedFlightsEndpoint() throws Exception {
        TrackedFlightResponse resp = TrackedFlightResponse.builder()
                .id("tf-001")
                .flightId("flight-101")
                .flightNumber("6E-101")
                .route("DEL → BOM")
                .currentStatus(FlightStatus.DELAYED)
                .delayMinutes(45)
                .delayReason("Fog at origin")
                .scheduledDeparture(Instant.parse("2026-11-01T08:00:00Z"))
                .revisedDeparture(Instant.parse("2026-11-01T08:45:00Z"))
                .scheduledArrival(Instant.parse("2026-11-01T10:15:00Z"))
                .estimatedArrival(Instant.parse("2026-11-01T11:00:00Z"))
                .active(true)
                .build();

        when(flightTrackingService.getTrackedFlights("user-tracker-01")).thenReturn(List.of(resp));

        mockMvc.perform(get("/v1/flights/tracked")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].flightNumber").value("6E-101"))
                .andExpect(jsonPath("$.data[0].currentStatus").value("DELAYED"))
                .andExpect(jsonPath("$.data[0].delayMinutes").value(45))
                .andExpect(jsonPath("$.data[0].delayReason").value("Fog at origin"));
    }

    @Test
    @WithMockUser(username = "user-tracker-01")
    @DisplayName("54. Frontend tracks a flight via POST /v1/flights/{flightId}/track")
    void testTrackFlightEndpoint() throws Exception {
        TrackedFlightResponse resp = TrackedFlightResponse.builder()
                .id("tf-002")
                .flightId("flight-202")
                .flightNumber("AI-202")
                .route("BOM → BLR")
                .currentStatus(FlightStatus.SCHEDULED)
                .active(true)
                .build();

        when(flightTrackingService.trackFlight("flight-202", "user-tracker-01")).thenReturn(resp);

        mockMvc.perform(post("/v1/flights/flight-202/track")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flightNumber").value("AI-202"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @WithMockUser(username = "user-tracker-01")
    @DisplayName("55. Frontend untracks a flight via DELETE /v1/flights/{flightId}/track")
    void testUntrackFlightEndpoint() throws Exception {
        doNothing().when(flightTrackingService).untrackFlight("flight-202", "user-tracker-01");

        mockMvc.perform(delete("/v1/flights/flight-202/track")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(flightTrackingService).untrackFlight("flight-202", "user-tracker-01");
    }

    @Test
    @WithMockUser(username = "user-tracker-01")
    @DisplayName("56. Frontend checks tracking status via GET /v1/flights/{flightId}/track/status")
    void testIsTrackingStatusEndpoint() throws Exception {
        when(flightTrackingService.isTracking("flight-202", "user-tracker-01")).thenReturn(true);

        mockMvc.perform(get("/v1/flights/flight-202/track/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("Unauthenticated tracking access is rejected with 401 Unauthorized")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/v1/flights/tracked"))
                .andExpect(status().isUnauthorized());
    }
}
