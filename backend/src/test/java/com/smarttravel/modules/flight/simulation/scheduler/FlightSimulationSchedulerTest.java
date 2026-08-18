package com.smarttravel.modules.flight.simulation.scheduler;

import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.simulation.config.FlightSimulationProperties;
import com.smarttravel.modules.flight.simulation.engine.FlightSimulationEngine;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationConfig;
import com.smarttravel.modules.flight.simulation.repository.FlightSimulationConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightSimulationSchedulerTest {

    @Mock
    private FlightSimulationProperties properties;

    @Mock
    private FlightSimulationConfigRepository configRepository;

    @Mock
    private FlightSimulationEngine simulationEngine;

    @InjectMocks
    private FlightSimulationScheduler scheduler;

    private FlightSimulationConfig config1;
    private FlightSimulationConfig config2;

    @BeforeEach
    void setUp() {
        config1 = FlightSimulationConfig.builder()
                .id("cfg-1")
                .flightId("fl-1")
                .flightNumber("AI-101")
                .enabled(true)
                .currentStatus(FlightStatus.SCHEDULED)
                .completed(false)
                .build();

        config2 = FlightSimulationConfig.builder()
                .id("cfg-2")
                .flightId("fl-2")
                .flightNumber("6E-202")
                .enabled(true)
                .currentStatus(FlightStatus.BOARDING)
                .completed(false)
                .build();
    }

    @Test
    @DisplayName("17 & 22. Scheduler triggers simulation for multiple active flights independently")
    void testActiveSimulationsProcessed() {
        when(properties.isEnabled()).thenReturn(true);
        when(configRepository.findByEnabledTrueAndCompletedFalse()).thenReturn(List.of(config1, config2));

        scheduler.executeSimulationCycle();

        verify(simulationEngine, times(1)).stepSimulation(config1);
        verify(simulationEngine, times(1)).stepSimulation(config2);
    }

    @Test
    @DisplayName("18 & 19. Scheduler does nothing when simulation is disabled globally")
    void testSchedulerDisabledGlobally() {
        when(properties.isEnabled()).thenReturn(false);

        scheduler.executeSimulationCycle();

        verify(configRepository, never()).findByEnabledTrueAndCompletedFalse();
        verify(simulationEngine, never()).stepSimulation(any());
    }

    @Test
    @DisplayName("Scheduler handles empty active simulations gracefully")
    void testSchedulerNoActiveFlights() {
        when(properties.isEnabled()).thenReturn(true);
        when(configRepository.findByEnabledTrueAndCompletedFalse()).thenReturn(List.of());

        scheduler.executeSimulationCycle();

        verify(simulationEngine, never()).stepSimulation(any());
    }

    @Test
    @DisplayName("21. Error on one flight is isolated and does not stop other flights")
    void testSimulationErrorIsolation() {
        when(properties.isEnabled()).thenReturn(true);
        when(configRepository.findByEnabledTrueAndCompletedFalse()).thenReturn(List.of(config1, config2));
        when(simulationEngine.stepSimulation(config1)).thenThrow(new RuntimeException("Database timeout on flight 1"));

        scheduler.executeSimulationCycle();

        verify(simulationEngine).stepSimulation(config1);
        verify(simulationEngine).stepSimulation(config2);
    }
}
