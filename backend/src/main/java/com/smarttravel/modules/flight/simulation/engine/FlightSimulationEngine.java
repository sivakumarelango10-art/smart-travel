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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/**
 * Deterministic simulation decision logic engine.
 * Computes next flight operational status and invokes domain services without bypassing validation.
 */
@Component
public class FlightSimulationEngine {

    private static final Logger log = LoggerFactory.getLogger(FlightSimulationEngine.class);

    private final FlightRepository flightRepository;
    private final FlightService flightService;
    private final FlightSimulationConfigRepository configRepository;
    private final RandomProvider randomProvider;

    public FlightSimulationEngine(FlightRepository flightRepository,
                                  FlightService flightService,
                                  FlightSimulationConfigRepository configRepository,
                                  RandomProvider randomProvider) {
        this.flightRepository = flightRepository;
        this.flightService = flightService;
        this.configRepository = configRepository;
        this.randomProvider = randomProvider;
    }

    /**
     * Executes one deterministic step of the flight status simulation.
     *
     * @param config The simulation configuration to advance.
     * @return Optional containing the generated FlightSimulationEvent, or empty if terminal/inactive.
     */
    public Optional<FlightSimulationEvent> stepSimulation(FlightSimulationConfig config) {
        if (config == null || !config.isEnabled() || config.isCompleted()) {
            return Optional.empty();
        }

        Optional<Flight> flightOpt = flightRepository.findById(config.getFlightId());
        if (flightOpt.isEmpty()) {
            log.warn("Simulation target flight ID '{}' not found. Terminating simulation.", config.getFlightId());
            config.setCompleted(true);
            config.setEnabled(false);
            configRepository.save(config);
            return Optional.empty();
        }

        Flight flight = flightOpt.get();
        FlightStatus currentStatus = flight.getStatus();

        // 1. Guard against terminal states
        if (currentStatus == FlightStatus.ARRIVED ||
                currentStatus == FlightStatus.CANCELLED ||
                currentStatus == FlightStatus.DIVERTED) {
            log.info("Flight {} is in terminal status {}. Completing simulation.", flight.getFlightNumber(), currentStatus);
            config.setCurrentStatus(currentStatus);
            config.setCompleted(true);
            config.setEnabled(false);
            configRepository.save(config);
            return Optional.empty();
        }

        // 2. Determine Next Status & Delay Properties
        FlightStatus nextStatus;
        Integer delayMinutes = null;
        String delayReason = null;
        Instant revisedDeparture = null;
        Instant estimatedArrival = null;

        switch (currentStatus) {
            case SCHEDULED -> {
                if (shouldTriggerDelay(config)) {
                    nextStatus = FlightStatus.DELAYED;
                    delayMinutes = calculateDelayMinutes(config);
                    delayReason = randomProvider.getRandomDelayReason();
                } else {
                    nextStatus = FlightStatus.BOARDING;
                }
            }
            case DELAYED -> {
                nextStatus = FlightStatus.BOARDING;
            }
            case BOARDING -> {
                boolean previouslyDelayed = flight.getDelayMinutes() != null && flight.getDelayMinutes() > 0;
                if (!previouslyDelayed && shouldTriggerDelay(config)) {
                    nextStatus = FlightStatus.DELAYED;
                    delayMinutes = calculateDelayMinutes(config);
                    delayReason = randomProvider.getRandomDelayReason();
                } else if (previouslyDelayed) {
                    nextStatus = FlightStatus.DEPARTED;
                } else {
                    nextStatus = FlightStatus.ON_TIME;
                }
            }
            case ON_TIME -> {
                nextStatus = FlightStatus.DEPARTED;
            }
            case DEPARTED -> {
                nextStatus = FlightStatus.ARRIVED;
            }
            default -> {
                log.warn("Unrecognized current flight status: {}. Stopping simulation.", currentStatus);
                config.setCompleted(true);
                config.setEnabled(false);
                configRepository.save(config);
                return Optional.empty();
            }
        }

        // 3. Compute revised timestamps if delayed
        if (nextStatus == FlightStatus.DELAYED) {
            int delayMin = delayMinutes != null ? delayMinutes : 30;
            revisedDeparture = flight.getDepartureTime().plus(delayMin, ChronoUnit.MINUTES);
            estimatedArrival = flight.getArrivalTime().plus(delayMin, ChronoUnit.MINUTES);
        }

        // 4. Update via Domain Service (enforcing validation & history audit)
        FlightStatusUpdateRequest updateReq = FlightStatusUpdateRequest.builder()
                .status(nextStatus)
                .delayMinutes(delayMinutes)
                .delayReason(delayReason)
                .revisedDepartureTime(revisedDeparture)
                .estimatedArrival(estimatedArrival)
                .build();

        FlightResponse updatedFlight = flightService.updateFlightStatus(flight.getId(), updateReq);

        // 5. Produce Simulation Event
        FlightSimulationEvent event = FlightSimulationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .simulationId(config.getId())
                .flightId(updatedFlight.getId())
                .flightNumber(updatedFlight.getFlightNumber())
                .previousStatus(currentStatus)
                .newStatus(updatedFlight.getStatus())
                .delayMinutes(updatedFlight.getDelayMinutes())
                .delayReason(updatedFlight.getDelayReason())
                .revisedDepartureTime(updatedFlight.getRevisedDepartureTime())
                .estimatedArrival(updatedFlight.getEstimatedArrival())
                .eventTime(Instant.now())
                .build();

        // 6. Update Simulation State
        config.setCurrentStatus(updatedFlight.getStatus());
        config.setLastTransitionAt(Instant.now());
        if (updatedFlight.getStatus() == FlightStatus.ARRIVED ||
                updatedFlight.getStatus() == FlightStatus.CANCELLED ||
                updatedFlight.getStatus() == FlightStatus.DIVERTED) {
            config.setCompleted(true);
            config.setEnabled(false);
            log.info("Simulated flight {} reached terminal status: {}. Simulation completed.",
                    updatedFlight.getFlightNumber(), updatedFlight.getStatus());
        }

        configRepository.save(config);
        log.info("Simulation transitioned flight {} from {} to {}",
                updatedFlight.getFlightNumber(), currentStatus, updatedFlight.getStatus());

        return Optional.of(event);
    }

    private boolean shouldTriggerDelay(FlightSimulationConfig config) {
        return randomProvider.nextDouble() < config.getDelayProbability();
    }

    private int calculateDelayMinutes(FlightSimulationConfig config) {
        return randomProvider.nextInt(config.getMinDelayMinutes(), config.getMaxDelayMinutes());
    }
}
