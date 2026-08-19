package com.smarttravel.modules.flight.simulation.service;

import com.smarttravel.common.exception.BadRequestException;

import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.simulation.config.FlightSimulationProperties;
import com.smarttravel.modules.flight.simulation.dto.SimulationStartRequest;
import com.smarttravel.modules.flight.simulation.dto.SimulationStatusResponse;
import com.smarttravel.modules.flight.simulation.engine.FlightSimulationEngine;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationConfig;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationEvent;
import com.smarttravel.modules.flight.simulation.repository.FlightSimulationConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightSimulationServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightSimulationConfigRepository configRepository;

    @Mock
    private FlightSimulationEngine simulationEngine;

    @Spy
    private FlightSimulationProperties properties = new FlightSimulationProperties();

    @InjectMocks
    private FlightSimulationServiceImpl simulationService;

    private Flight sampleFlight;
    private FlightSimulationConfig sampleConfig;

    @BeforeEach
    void setUp() {
        sampleFlight = Flight.builder()
                .id("fl-1")
                .flightNumber("AI-101")
                .status(FlightStatus.SCHEDULED)
                .active(true)
                .build();

        sampleConfig = FlightSimulationConfig.builder()
                .id("cfg-1")
                .flightId("fl-1")
                .flightNumber("AI-101")
                .enabled(true)
                .currentStatus(FlightStatus.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("startSimulation creates and activates simulation config")
    void testStartSimulationSuccess() {
        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(sampleFlight));
        when(configRepository.findByFlightId("fl-1")).thenReturn(Optional.empty());
        when(configRepository.save(any(FlightSimulationConfig.class))).thenAnswer(i -> {
            FlightSimulationConfig c = i.getArgument(0);
            c.setId("generated-cfg-id");
            return c;
        });

        SimulationStartRequest req = SimulationStartRequest.builder()
                .speedMultiplier(120)
                .delayProbability(0.30)
                .minDelayMinutes(20)
                .maxDelayMinutes(60)
                .build();

        SimulationStatusResponse response = simulationService.startSimulation("fl-1", req);

        assertNotNull(response);
        assertEquals("fl-1", response.getFlightId());
        assertEquals("AI-101", response.getFlightNumber());
        assertTrue(response.isEnabled());
        assertEquals(120, response.getSpeedMultiplier());
        assertEquals(0.30, response.getDelayProbability());
        verify(configRepository).save(any(FlightSimulationConfig.class));
    }

    @Test
    @DisplayName("startSimulation rejects starting simulation on terminal flight")
    void testStartSimulationTerminalFlightRejection() {
        sampleFlight.setStatus(FlightStatus.ARRIVED);
        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(sampleFlight));

        assertThrows(BadRequestException.class, () -> simulationService.startSimulation("fl-1", null));
    }

    @Test
    @DisplayName("stopSimulation deactivates running simulation")
    void testStopSimulationSuccess() {
        when(configRepository.findByFlightId("fl-1")).thenReturn(Optional.of(sampleConfig));
        when(configRepository.save(any(FlightSimulationConfig.class))).thenAnswer(i -> i.getArgument(0));
        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(sampleFlight));

        SimulationStatusResponse response = simulationService.stopSimulation("fl-1");

        assertNotNull(response);
        assertFalse(response.isEnabled());
        verify(configRepository).save(sampleConfig);
    }

    @Test
    @DisplayName("getSimulationStatus returns current status")
    void testGetSimulationStatusSuccess() {
        when(configRepository.findByFlightId("fl-1")).thenReturn(Optional.of(sampleConfig));
        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(sampleFlight));

        SimulationStatusResponse response = simulationService.getSimulationStatus("fl-1");

        assertNotNull(response);
        assertEquals("cfg-1", response.getSimulationId());
        assertEquals("AI-101", response.getFlightNumber());
        assertEquals(FlightStatus.SCHEDULED, response.getCurrentStatus());
    }

    @Test
    @DisplayName("stepSimulation delegates step to simulation engine")
    void testStepSimulationDelegation() {
        when(configRepository.findByFlightId("fl-1")).thenReturn(Optional.of(sampleConfig));
        FlightSimulationEvent event = FlightSimulationEvent.builder()
                .flightNumber("AI-101")
                .previousStatus(FlightStatus.SCHEDULED)
                .newStatus(FlightStatus.BOARDING)
                .build();
        when(simulationEngine.stepSimulation(sampleConfig)).thenReturn(Optional.of(event));

        Optional<FlightSimulationEvent> result = simulationService.stepSimulation("fl-1");

        assertTrue(result.isPresent());
        assertEquals(FlightStatus.BOARDING, result.get().getNewStatus());
        verify(simulationEngine).stepSimulation(sampleConfig);
    }

    @Test
    @DisplayName("getActiveSimulations returns list of active responses")
    void testGetActiveSimulations() {
        when(configRepository.findByEnabledTrueAndCompletedFalse()).thenReturn(List.of(sampleConfig));
        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(sampleFlight));

        List<SimulationStatusResponse> responses = simulationService.getActiveSimulations();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("AI-101", responses.get(0).getFlightNumber());
    }
}
