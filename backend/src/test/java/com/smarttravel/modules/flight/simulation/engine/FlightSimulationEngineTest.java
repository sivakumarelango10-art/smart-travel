package com.smarttravel.modules.flight.simulation.engine;

import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationConfig;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationEvent;
import com.smarttravel.modules.flight.simulation.random.RandomProvider;
import com.smarttravel.modules.flight.simulation.repository.FlightSimulationConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightSimulationEngineTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightService flightService;

    @Mock
    private FlightSimulationConfigRepository configRepository;

    @Mock
    private RandomProvider randomProvider;

    @InjectMocks
    private FlightSimulationEngine simulationEngine;

    private Flight sampleFlight;
    private FlightSimulationConfig sampleConfig;
    private Instant departureTime;
    private Instant arrivalTime;

    @BeforeEach
    void setUp() {
        departureTime = Instant.now().plus(2, ChronoUnit.DAYS);
        arrivalTime = departureTime.plus(2, ChronoUnit.HOURS);

        sampleFlight = Flight.builder()
                .id("fl-100")
                .flightNumber("SIM-101")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .durationMinutes(120)
                .status(FlightStatus.SCHEDULED)
                .active(true)
                .build();

        sampleConfig = FlightSimulationConfig.builder()
                .id("cfg-100")
                .flightId("fl-100")
                .flightNumber("SIM-101")
                .enabled(true)
                .currentStatus(FlightStatus.SCHEDULED)
                .delayProbability(0.25)
                .minDelayMinutes(15)
                .maxDelayMinutes(90)
                .completed(false)
                .build();
    }

    @Nested
    @DisplayName("Normal Lifecycle Transitions (No Delays)")
    class NormalLifecycleTests {

        @Test
        @DisplayName("1. SCHEDULED -> BOARDING when delay not triggered")
        void testScheduledToBoarding() {
            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));
            when(randomProvider.nextDouble()).thenReturn(0.80); // 0.80 > 0.25 -> no delay

            FlightResponse boardingResp = FlightResponse.builder()
                    .id("fl-100")
                    .flightNumber("SIM-101")
                    .status(FlightStatus.BOARDING)
                    .build();
            when(flightService.updateFlightStatus(eq("fl-100"), any(FlightStatusUpdateRequest.class)))
                    .thenReturn(boardingResp);

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isPresent());
            FlightSimulationEvent event = eventOpt.get();
            assertEquals(FlightStatus.SCHEDULED, event.getPreviousStatus());
            assertEquals(FlightStatus.BOARDING, event.getNewStatus());
            assertEquals(FlightStatus.BOARDING, sampleConfig.getCurrentStatus());
            assertFalse(sampleConfig.isCompleted());
            verify(configRepository).save(sampleConfig);
        }

        @Test
        @DisplayName("2. BOARDING -> ON_TIME when delay not triggered")
        void testBoardingToOnTime() {
            sampleFlight.setStatus(FlightStatus.BOARDING);
            sampleConfig.setCurrentStatus(FlightStatus.BOARDING);

            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));
            when(randomProvider.nextDouble()).thenReturn(0.50); // no delay

            FlightResponse onTimeResp = FlightResponse.builder()
                    .id("fl-100")
                    .flightNumber("SIM-101")
                    .status(FlightStatus.ON_TIME)
                    .build();
            when(flightService.updateFlightStatus(eq("fl-100"), any(FlightStatusUpdateRequest.class)))
                    .thenReturn(onTimeResp);

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isPresent());
            assertEquals(FlightStatus.ON_TIME, eventOpt.get().getNewStatus());
        }

        @Test
        @DisplayName("3. ON_TIME -> DEPARTED")
        void testOnTimeToDeparted() {
            sampleFlight.setStatus(FlightStatus.ON_TIME);
            sampleConfig.setCurrentStatus(FlightStatus.ON_TIME);

            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));

            FlightResponse departedResp = FlightResponse.builder()
                    .id("fl-100")
                    .flightNumber("SIM-101")
                    .status(FlightStatus.DEPARTED)
                    .build();
            when(flightService.updateFlightStatus(eq("fl-100"), any(FlightStatusUpdateRequest.class)))
                    .thenReturn(departedResp);

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isPresent());
            assertEquals(FlightStatus.DEPARTED, eventOpt.get().getNewStatus());
        }

        @Test
        @DisplayName("4. DEPARTED -> ARRIVED (completes simulation)")
        void testDepartedToArrived() {
            sampleFlight.setStatus(FlightStatus.DEPARTED);
            sampleConfig.setCurrentStatus(FlightStatus.DEPARTED);

            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));

            FlightResponse arrivedResp = FlightResponse.builder()
                    .id("fl-100")
                    .flightNumber("SIM-101")
                    .status(FlightStatus.ARRIVED)
                    .build();
            when(flightService.updateFlightStatus(eq("fl-100"), any(FlightStatusUpdateRequest.class)))
                    .thenReturn(arrivedResp);

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isPresent());
            assertEquals(FlightStatus.ARRIVED, eventOpt.get().getNewStatus());
            assertTrue(sampleConfig.isCompleted());
            assertFalse(sampleConfig.isEnabled());
        }
    }

    @Nested
    @DisplayName("Simulated Delay Flows")
    class DelayLifecycleTests {

        @Test
        @DisplayName("5. SCHEDULED -> DELAYED when probability matches")
        void testScheduledToDelayed() {
            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));
            when(randomProvider.nextDouble()).thenReturn(0.10); // 0.10 < 0.25 -> delay triggered
            when(randomProvider.nextInt(15, 90)).thenReturn(45);
            when(randomProvider.getRandomDelayReason()).thenReturn("Air traffic control congestion");

            FlightResponse delayedResp = FlightResponse.builder()
                    .id("fl-100")
                    .flightNumber("SIM-101")
                    .status(FlightStatus.DELAYED)
                    .delayMinutes(45)
                    .delayReason("Air traffic control congestion")
                    .revisedDepartureTime(departureTime.plus(45, ChronoUnit.MINUTES))
                    .estimatedArrival(arrivalTime.plus(45, ChronoUnit.MINUTES))
                    .build();
            when(flightService.updateFlightStatus(eq("fl-100"), any(FlightStatusUpdateRequest.class)))
                    .thenReturn(delayedResp);

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isPresent());
            FlightSimulationEvent event = eventOpt.get();
            assertEquals(FlightStatus.DELAYED, event.getNewStatus());
            assertEquals(45, event.getDelayMinutes());
            assertEquals("Air traffic control congestion", event.getDelayReason());
            assertEquals(departureTime.plus(45, ChronoUnit.MINUTES), event.getRevisedDepartureTime());
            assertEquals(arrivalTime.plus(45, ChronoUnit.MINUTES), event.getEstimatedArrival());

            ArgumentCaptor<FlightStatusUpdateRequest> reqCaptor = ArgumentCaptor.forClass(FlightStatusUpdateRequest.class);
            verify(flightService).updateFlightStatus(eq("fl-100"), reqCaptor.capture());
            FlightStatusUpdateRequest req = reqCaptor.getValue();
            assertEquals(FlightStatus.DELAYED, req.getStatus());
            assertEquals(45, req.getDelayMinutes());
            assertEquals(departureTime.plus(45, ChronoUnit.MINUTES), req.getRevisedDepartureTime());
            assertEquals(arrivalTime.plus(45, ChronoUnit.MINUTES), req.getEstimatedArrival());
        }

        @Test
        @DisplayName("6. DELAYED -> BOARDING transition")
        void testDelayedToBoarding() {
            sampleFlight.setStatus(FlightStatus.DELAYED);
            sampleFlight.setDelayMinutes(45);
            sampleConfig.setCurrentStatus(FlightStatus.DELAYED);

            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));

            FlightResponse boardingResp = FlightResponse.builder()
                    .id("fl-100")
                    .flightNumber("SIM-101")
                    .status(FlightStatus.BOARDING)
                    .delayMinutes(45)
                    .build();
            when(flightService.updateFlightStatus(eq("fl-100"), any(FlightStatusUpdateRequest.class)))
                    .thenReturn(boardingResp);

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isPresent());
            assertEquals(FlightStatus.BOARDING, eventOpt.get().getNewStatus());
        }

        @Test
        @DisplayName("7. BOARDING -> DELAYED when newly delayed at gate")
        void testBoardingToDelayed() {
            sampleFlight.setStatus(FlightStatus.BOARDING);
            sampleFlight.setDelayMinutes(0); // Not delayed earlier
            sampleConfig.setCurrentStatus(FlightStatus.BOARDING);

            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));
            when(randomProvider.nextDouble()).thenReturn(0.05); // delay triggered
            when(randomProvider.nextInt(15, 90)).thenReturn(30);
            when(randomProvider.getRandomDelayReason()).thenReturn("Technical inspection and maintenance");

            FlightResponse delayedResp = FlightResponse.builder()
                    .id("fl-100")
                    .flightNumber("SIM-101")
                    .status(FlightStatus.DELAYED)
                    .delayMinutes(30)
                    .delayReason("Technical inspection and maintenance")
                    .build();
            when(flightService.updateFlightStatus(eq("fl-100"), any(FlightStatusUpdateRequest.class)))
                    .thenReturn(delayedResp);

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isPresent());
            assertEquals(FlightStatus.DELAYED, eventOpt.get().getNewStatus());
            assertEquals(30, eventOpt.get().getDelayMinutes());
        }

        @Test
        @DisplayName("8. Delayed BOARDING -> DEPARTED")
        void testDelayedBoardingToDeparted() {
            sampleFlight.setStatus(FlightStatus.BOARDING);
            sampleFlight.setDelayMinutes(45); // Was previously delayed
            sampleConfig.setCurrentStatus(FlightStatus.BOARDING);

            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));

            FlightResponse departedResp = FlightResponse.builder()
                    .id("fl-100")
                    .flightNumber("SIM-101")
                    .status(FlightStatus.DEPARTED)
                    .delayMinutes(45)
                    .build();
            when(flightService.updateFlightStatus(eq("fl-100"), any(FlightStatusUpdateRequest.class)))
                    .thenReturn(departedResp);

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isPresent());
            assertEquals(FlightStatus.DEPARTED, eventOpt.get().getNewStatus());
        }
    }

    @Nested
    @DisplayName("Terminal States & Inactivity Protection")
    class TerminalStateTests {

        @Test
        @DisplayName("14. ARRIVED cannot transition and completes simulation")
        void testArrivedCannotTransition() {
            sampleFlight.setStatus(FlightStatus.ARRIVED);
            sampleConfig.setCurrentStatus(FlightStatus.ARRIVED);

            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isEmpty());
            assertTrue(sampleConfig.isCompleted());
            assertFalse(sampleConfig.isEnabled());
            verify(flightService, never()).updateFlightStatus(any(), any());
        }

        @Test
        @DisplayName("15. CANCELLED cannot transition and completes simulation")
        void testCancelledCannotTransition() {
            sampleFlight.setStatus(FlightStatus.CANCELLED);
            sampleConfig.setCurrentStatus(FlightStatus.CANCELLED);

            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isEmpty());
            assertTrue(sampleConfig.isCompleted());
            assertFalse(sampleConfig.isEnabled());
            verify(flightService, never()).updateFlightStatus(any(), any());
        }

        @Test
        @DisplayName("16. DIVERTED cannot transition and completes simulation")
        void testDivertedCannotTransition() {
            sampleFlight.setStatus(FlightStatus.DIVERTED);
            sampleConfig.setCurrentStatus(FlightStatus.DIVERTED);

            when(flightRepository.findById("fl-100")).thenReturn(Optional.of(sampleFlight));

            Optional<FlightSimulationEvent> eventOpt = simulationEngine.stepSimulation(sampleConfig);

            assertTrue(eventOpt.isEmpty());
            assertTrue(sampleConfig.isCompleted());
            assertFalse(sampleConfig.isEnabled());
            verify(flightService, never()).updateFlightStatus(any(), any());
        }
    }
}
