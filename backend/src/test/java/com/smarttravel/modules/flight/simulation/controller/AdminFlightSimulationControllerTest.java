package com.smarttravel.modules.flight.simulation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.simulation.dto.SimulationStartRequest;
import com.smarttravel.modules.flight.simulation.dto.SimulationStatusResponse;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationEvent;
import com.smarttravel.modules.flight.simulation.service.FlightSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminFlightSimulationController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminFlightSimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FlightSimulationService simulationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private SimulationStatusResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = SimulationStatusResponse.builder()
                .simulationId("cfg-101")
                .flightId("fl-101")
                .flightNumber("AI-101")
                .enabled(true)
                .currentStatus(FlightStatus.SCHEDULED)
                .speedMultiplier(60)
                .delayProbability(0.25)
                .completed(false)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/admin/flight-simulation/{flightId}/start returns 200 OK")
    void testStartSimulation() throws Exception {
        SimulationStartRequest req = SimulationStartRequest.builder()
                .speedMultiplier(60)
                .delayProbability(0.20)
                .build();

        when(simulationService.startSimulation(eq("fl-101"), any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/admin/flight-simulation/fl-101/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flightNumber").value("AI-101"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/admin/flight-simulation/{flightId}/stop returns 200 OK")
    void testStopSimulation() throws Exception {
        sampleResponse.setEnabled(false);
        when(simulationService.stopSimulation("fl-101")).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/admin/flight-simulation/fl-101/stop")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/admin/flight-simulation/{flightId} returns 200 OK")
    void testGetSimulationStatus() throws Exception {
        when(simulationService.getSimulationStatus("fl-101")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/admin/flight-simulation/fl-101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flightId").value("fl-101"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/flight-simulation/{flightId}/step returns 200 OK")
    void testStepSimulation() throws Exception {
        FlightSimulationEvent event = FlightSimulationEvent.builder()
                .flightNumber("AI-101")
                .previousStatus(FlightStatus.SCHEDULED)
                .newStatus(FlightStatus.BOARDING)
                .build();

        when(simulationService.stepSimulation("fl-101")).thenReturn(Optional.of(event));

        mockMvc.perform(post("/api/v1/admin/flight-simulation/fl-101/step")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.newStatus").value("BOARDING"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/flight-simulation returns list of active simulations")
    void testGetActiveSimulations() throws Exception {
        when(simulationService.getActiveSimulations()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/admin/flight-simulation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].flightNumber").value("AI-101"));
    }
}
