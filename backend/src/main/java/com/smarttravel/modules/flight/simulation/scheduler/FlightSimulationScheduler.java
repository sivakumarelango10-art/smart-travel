package com.smarttravel.modules.flight.simulation.scheduler;

import com.smarttravel.modules.flight.simulation.config.FlightSimulationProperties;
import com.smarttravel.modules.flight.simulation.engine.FlightSimulationEngine;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationConfig;
import com.smarttravel.modules.flight.simulation.repository.FlightSimulationConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Background scheduler periodically triggering active flight simulations.
 * Incorporates concurrent execution locks to prevent double-processing or race conditions.
 */
@Component
public class FlightSimulationScheduler {

    private static final Logger log = LoggerFactory.getLogger(FlightSimulationScheduler.class);

    private final FlightSimulationProperties properties;
    private final FlightSimulationConfigRepository configRepository;
    private final FlightSimulationEngine simulationEngine;
    private final Set<String> processingLocks = ConcurrentHashMap.newKeySet();

    public FlightSimulationScheduler(FlightSimulationProperties properties,
                                     FlightSimulationConfigRepository configRepository,
                                     FlightSimulationEngine simulationEngine) {
        this.properties = properties;
        this.configRepository = configRepository;
        this.simulationEngine = simulationEngine;
    }

    @Scheduled(fixedDelayString = "${smarttravel.flight.simulation.interval-ms:5000}")
    public void executeSimulationCycle() {
        if (!properties.isEnabled()) {
            return;
        }

        List<FlightSimulationConfig> activeSimulations = configRepository.findByEnabledTrueAndCompletedFalse();
        if (activeSimulations.isEmpty()) {
            return;
        }

        log.debug("Executing simulation cycle for {} active flight(s)", activeSimulations.size());

        for (FlightSimulationConfig config : activeSimulations) {
            String flightId = config.getFlightId();
            if (processingLocks.add(flightId)) {
                try {
                    simulationEngine.stepSimulation(config);
                } catch (Exception e) {
                    log.error("Simulation error on flight {}: {}", config.getFlightNumber(), e.getMessage());
                } finally {
                    processingLocks.remove(flightId);
                }
            } else {
                log.debug("Skipping flight {} as it is currently being processed by another worker", config.getFlightNumber());
            }
        }
    }
}
