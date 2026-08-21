package com.smarttravel.modules.flight.requirement1;

import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.flight.simulation.engine.FlightSimulationEngine;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationConfig;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationEvent;
import com.smarttravel.modules.flight.simulation.random.RandomProvider;
import com.smarttravel.modules.flight.simulation.repository.FlightSimulationConfigRepository;
import com.smarttravel.modules.flight.websocket.FlightStatusEvent;
import com.smarttravel.modules.flight.websocket.FlightStatusWebSocketPublisher;
import com.smarttravel.modules.notification.service.WebPushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Requirement #1 - Test Group B: Simulation Engine
 * Verifies that the simulation engine handles status transitions, schedule recalculations,
 * ETA propagation, terminal state guards, and error resilience.
 */
@ExtendWith(MockitoExtension.class)
class FlightSimulationEngineAuditTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightService flightService;

    @Mock
    private FlightSimulationConfigRepository configRepository;

    @Mock
    private RandomProvider randomProvider;

    @Mock
    private FlightStatusWebSocketPublisher webSocketPublisher;

    @Mock
    private WebPushService webPushService;

    @InjectMocks
    private FlightSimulationEngine simulationEngine;

    private Flight flight;
    private FlightSimulationConfig config;
    private Instant departureTime;
    private Instant arrivalTime;

    @BeforeEach
    void setUp() {
        departureTime = Instant.now().plus(4, ChronoUnit.HOURS);
        arrivalTime = departureTime.plus(2, ChronoUnit.HOURS);

        flight = Flight.builder()
                .id("fl-audit-101")
                .flightNumber("AI-302")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .status(FlightStatus.SCHEDULED)
                .active(true)
                .build();

        config = FlightSimulationConfig.builder()
                .id("cfg-audit-101")
                .flightId("fl-audit-101")
                .flightNumber("AI-302")
                .enabled(true)
                .currentStatus(FlightStatus.SCHEDULED)
                .delayProbability(0.30)
                .minDelayMinutes(15)
                .maxDelayMinutes(60)
                .completed(false)
                .build();
    }

    @Test
    @DisplayName("11. Simulation engine changes flight status")
    void testSimulationEngineChangesFlightStatus() {
        when(flightRepository.findById("fl-audit-101")).thenReturn(Optional.of(flight));
        when(randomProvider.nextDouble()).thenReturn(0.50); // No delay -> BOARDING

        FlightResponse resp = FlightResponse.builder()
                .id("fl-audit-101")
                .flightNumber("AI-302")
                .status(FlightStatus.BOARDING)
                .build();
        when(flightService.updateFlightStatus(eq("fl-audit-101"), any(FlightStatusUpdateRequest.class))).thenReturn(resp);

        Optional<FlightSimulationEvent> event = simulationEngine.stepSimulation(config);

        assertThat(event).isPresent();
        assertThat(event.get().getNewStatus()).isEqualTo(FlightStatus.BOARDING);
    }

    @Test
    @DisplayName("12. Simulation engine persists the new status")
    void testSimulationEnginePersistsNewStatus() {
        when(flightRepository.findById("fl-audit-101")).thenReturn(Optional.of(flight));
        when(randomProvider.nextDouble()).thenReturn(0.80);

        FlightResponse resp = FlightResponse.builder()
                .id("fl-audit-101")
                .flightNumber("AI-302")
                .status(FlightStatus.BOARDING)
                .build();
        when(flightService.updateFlightStatus(eq("fl-audit-101"), any(FlightStatusUpdateRequest.class))).thenReturn(resp);

        simulationEngine.stepSimulation(config);

        verify(flightService).updateFlightStatus(eq("fl-audit-101"), any(FlightStatusUpdateRequest.class));
        verify(configRepository).save(config);
        assertThat(config.getCurrentStatus()).isEqualTo(FlightStatus.BOARDING);
    }

    @Test
    @DisplayName("13. Simulation engine updates dynamic ETA when delayed")
    void testSimulationEngineUpdatesEtaWhenDelayed() {
        when(flightRepository.findById("fl-audit-101")).thenReturn(Optional.of(flight));
        when(randomProvider.nextDouble()).thenReturn(0.10); // Delay triggered
        when(randomProvider.nextInt(15, 60)).thenReturn(45);
        when(randomProvider.getRandomDelayReason()).thenReturn("Heavy monsoon downpour");

        Instant expectedEta = arrivalTime.plus(45, ChronoUnit.MINUTES);
        Instant expectedRevDep = departureTime.plus(45, ChronoUnit.MINUTES);

        FlightResponse resp = FlightResponse.builder()
                .id("fl-audit-101")
                .flightNumber("AI-302")
                .status(FlightStatus.DELAYED)
                .delayMinutes(45)
                .delayReason("Heavy monsoon downpour")
                .revisedDepartureTime(expectedRevDep)
                .estimatedArrival(expectedEta)
                .build();
        when(flightService.updateFlightStatus(eq("fl-audit-101"), any(FlightStatusUpdateRequest.class))).thenReturn(resp);

        Optional<FlightSimulationEvent> event = simulationEngine.stepSimulation(config);

        assertThat(event).isPresent();
        assertThat(event.get().getEstimatedArrival()).isEqualTo(expectedEta);
        assertThat(event.get().getRevisedDepartureTime()).isEqualTo(expectedRevDep);

        ArgumentCaptor<FlightStatusUpdateRequest> captor = ArgumentCaptor.forClass(FlightStatusUpdateRequest.class);
        verify(flightService).updateFlightStatus(eq("fl-audit-101"), captor.capture());
        assertThat(captor.getValue().getEstimatedArrival()).isEqualTo(expectedEta);
    }

    @Test
    @DisplayName("14. Simulation engine updates departure/arrival schedule when required")
    void testSimulationEngineUpdatesSchedule() {
        when(flightRepository.findById("fl-audit-101")).thenReturn(Optional.of(flight));
        when(randomProvider.nextDouble()).thenReturn(0.05); // Delay triggered
        when(randomProvider.nextInt(15, 60)).thenReturn(30);
        when(randomProvider.getRandomDelayReason()).thenReturn("Late incoming aircraft turnaround");

        FlightResponse resp = FlightResponse.builder()
                .id("fl-audit-101")
                .flightNumber("AI-302")
                .status(FlightStatus.DELAYED)
                .delayMinutes(30)
                .delayReason("Late incoming aircraft turnaround")
                .revisedDepartureTime(departureTime.plus(30, ChronoUnit.MINUTES))
                .estimatedArrival(arrivalTime.plus(30, ChronoUnit.MINUTES))
                .build();
        when(flightService.updateFlightStatus(eq("fl-audit-101"), any(FlightStatusUpdateRequest.class))).thenReturn(resp);

        simulationEngine.stepSimulation(config);

        ArgumentCaptor<FlightStatusUpdateRequest> captor = ArgumentCaptor.forClass(FlightStatusUpdateRequest.class);
        verify(flightService).updateFlightStatus(eq("fl-audit-101"), captor.capture());
        assertThat(captor.getValue().getRevisedDepartureTime()).isEqualTo(departureTime.plus(30, ChronoUnit.MINUTES));
    }

    @Test
    @DisplayName("15. Simulation engine generates realistic status transitions (DEPARTED -> ARRIVED)")
    void testRealisticStatusTransitionToArrived() {
        flight.setStatus(FlightStatus.DEPARTED);
        config.setCurrentStatus(FlightStatus.DEPARTED);

        when(flightRepository.findById("fl-audit-101")).thenReturn(Optional.of(flight));

        FlightResponse resp = FlightResponse.builder()
                .id("fl-audit-101")
                .flightNumber("AI-302")
                .status(FlightStatus.ARRIVED)
                .build();
        when(flightService.updateFlightStatus(eq("fl-audit-101"), any(FlightStatusUpdateRequest.class))).thenReturn(resp);

        Optional<FlightSimulationEvent> event = simulationEngine.stepSimulation(config);

        assertThat(event).isPresent();
        assertThat(event.get().getNewStatus()).isEqualTo(FlightStatus.ARRIVED);
        assertThat(config.isCompleted()).isTrue();
        assertThat(config.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("16. Simulation does not produce events when disabled or completed")
    void testSimulationDisabledProducesNoEvents() {
        config.setEnabled(false);
        Optional<FlightSimulationEvent> event1 = simulationEngine.stepSimulation(config);
        assertThat(event1).isEmpty();

        config.setEnabled(true);
        config.setCompleted(true);
        Optional<FlightSimulationEvent> event2 = simulationEngine.stepSimulation(config);
        assertThat(event2).isEmpty();

        verifyNoInteractions(flightService);
    }

    @Test
    @DisplayName("17. Simulation handles a flight that does not exist")
    void testSimulationHandlesNonExistentFlight() {
        when(flightRepository.findById("fl-audit-101")).thenReturn(Optional.empty());

        Optional<FlightSimulationEvent> event = simulationEngine.stepSimulation(config);

        assertThat(event).isEmpty();
        assertThat(config.isCompleted()).isTrue();
        assertThat(config.isEnabled()).isFalse();
        verify(configRepository).save(config);
        verifyNoInteractions(flightService);
    }

    @Test
    @DisplayName("18. Simulation handles terminal/cancelled flights correctly without state overwrite")
    void testSimulationHandlesCancelledFlights() {
        flight.setStatus(FlightStatus.CANCELLED);
        config.setCurrentStatus(FlightStatus.CANCELLED);

        when(flightRepository.findById("fl-audit-101")).thenReturn(Optional.of(flight));

        Optional<FlightSimulationEvent> event = simulationEngine.stepSimulation(config);

        assertThat(event).isEmpty();
        assertThat(config.isCompleted()).isTrue();
        assertThat(config.isEnabled()).isFalse();
        verify(configRepository).save(config);
        verify(flightService, never()).updateFlightStatus(any(), any());
    }
}
