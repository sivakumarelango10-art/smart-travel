package com.smarttravel.modules.flight.disruption.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.flight.disruption.dto.FlightCancelRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightGateChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightOperationalStatusResponse;
import com.smarttravel.modules.flight.disruption.dto.FlightScheduleChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightTerminalChangeRequest;
import com.smarttravel.modules.flight.disruption.service.FlightDisruptionService;
import com.smarttravel.modules.flight.impact.dto.FlightImpactSummaryDto;
import com.smarttravel.modules.flight.impact.service.FlightImpactService;
import com.smarttravel.modules.flight.model.FlightStatus;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminFlightOperationsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminFlightOperationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightDisruptionService disruptionService;

    @MockBean
    private FlightImpactService flightImpactService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Admin can reschedule flight successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldRescheduleFlightSuccessfully() throws Exception {
        Instant newDep = Instant.now().plus(12, ChronoUnit.HOURS);
        Instant newArr = Instant.now().plus(14, ChronoUnit.HOURS);
        FlightScheduleChangeRequest req = new FlightScheduleChangeRequest(newDep, newArr, "Runway inspection", "Detail");

        FlightOperationalStatusResponse res = FlightOperationalStatusResponse.builder()
                .flightId("flight-1")
                .flightNumber("ST-101")
                .revisedDepartureTime(newDep)
                .estimatedArrivalTime(newArr)
                .status(FlightStatus.SCHEDULED)
                .build();

        when(disruptionService.rescheduleFlight(eq("flight-1"), any(), any())).thenReturn(res);

        mockMvc.perform(patch("/api/v1/admin/flights/flight-1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flightNumber").value("ST-101"));
    }

    @Test
    @DisplayName("Admin can cancel flight successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldCancelFlightSuccessfully() throws Exception {
        FlightCancelRequest req = new FlightCancelRequest("Severe storm", "Safe ops not possible", true);

        FlightOperationalStatusResponse res = FlightOperationalStatusResponse.builder()
                .flightId("flight-1")
                .status(FlightStatus.CANCELLED)
                .build();

        when(disruptionService.cancelFlight(eq("flight-1"), any(), any())).thenReturn(res);

        mockMvc.perform(patch("/api/v1/admin/flights/flight-1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Admin can update gate")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateGateSuccessfully() throws Exception {
        FlightGateChangeRequest req = new FlightGateChangeRequest("18A", "Stand reassigned");

        FlightOperationalStatusResponse res = FlightOperationalStatusResponse.builder()
                .flightId("flight-1")
                .gate("18A")
                .build();

        when(disruptionService.updateGate(eq("flight-1"), any(), any())).thenReturn(res);

        mockMvc.perform(patch("/api/v1/admin/flights/flight-1/gate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gate").value("18A"));
    }

    @Test
    @DisplayName("Admin can query customer impact summary")
    @WithMockUser(roles = "ADMIN")
    void shouldGetDisruptionImpact() throws Exception {
        FlightImpactSummaryDto summary = FlightImpactSummaryDto.builder()
                .flightId("flight-1")
                .totalAffectedBookings(5)
                .totalAffectedPassengers(8)
                .build();

        when(flightImpactService.getDisruptionImpactSummary("flight-1")).thenReturn(summary);

        mockMvc.perform(get("/api/v1/admin/flights/flight-1/impact"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAffectedBookings").value(5))
                .andExpect(jsonPath("$.data.totalAffectedPassengers").value(8));
    }
}
