package com.smarttravel.modules.flight.simulation.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ResourceNotFoundException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class FlightSimulationServiceImpl implements FlightSimulationService {

    private static final Logger log = LoggerFactory.getLogger(FlightSimulationServiceImpl.class);

    private final FlightRepository flightRepository;
    private final FlightSimulationConfigRepository configRepository;
    private final FlightSimulationEngine simulationEngine;
    private final FlightSimulationProperties properties;

    public FlightSimulationServiceImpl(FlightRepository flightRepository,
                                      FlightSimulationConfigRepository configRepository,
                                      FlightSimulationEngine simulationEngine,
                                      FlightSimulationProperties properties) {
        this.flightRepository = flightRepository;
        this.configRepository = configRepository;
        this.simulationEngine = simulationEngine;
        this.properties = properties;
    }

    @Override
    @Transactional
    public SimulationStatusResponse startSimulation(String flightId, SimulationStartRequest request) {
        log.info("Admin starting flight simulation for flight ID: {}", flightId);

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        if (flight.getStatus() == FlightStatus.ARRIVED ||
                flight.getStatus() == FlightStatus.CANCELLED ||
                flight.getStatus() == FlightStatus.DIVERTED) {
            throw new BadRequestException("Cannot start simulation for flight in terminal state: " + flight.getStatus());
        }

        int speed = (request != null && request.getSpeedMultiplier() != null)
                ? request.getSpeedMultiplier()
                : properties.getDefaultSpeed();

        double delayProb = (request != null && request.getDelayProbability() != null)
                ? request.getDelayProbability()
                : properties.getDelayProbability();

        int minDelay = (request != null && request.getMinDelayMinutes() != null)
                ? request.getMinDelayMinutes()
                : properties.getMinDelayMinutes();

        int maxDelay = (request != null && request.getMaxDelayMinutes() != null)
                ? request.getMaxDelayMinutes()
                : properties.getMaxDelayMinutes();

        FlightSimulationConfig config = configRepository.findByFlightId(flightId)
                .orElseGet(() -> FlightSimulationConfig.builder()
                        .flightId(flight.getId())
                        .flightNumber(flight.getFlightNumber())
                        .build());

        config.setEnabled(true);
        config.setCompleted(false);
        config.setCurrentStatus(flight.getStatus());
        config.setSpeedMultiplier(speed);
        config.setDelayProbability(delayProb);
        config.setMinDelayMinutes(minDelay);
        config.setMaxDelayMinutes(maxDelay);
        config.setStartTime(Instant.now());

        FlightSimulationConfig savedConfig = configRepository.save(config);
        log.info("Simulation activated for flight {} ({}) with speed multiplier {}x",
                flight.getFlightNumber(), flightId, speed);

        return toStatusResponse(savedConfig, flight);
    }

    @Override
    @Transactional
    public SimulationStatusResponse stopSimulation(String flightId) {
        log.info("Admin stopping flight simulation for flight ID: {}", flightId);

        FlightSimulationConfig config = configRepository.findByFlightId(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("FlightSimulation", "flightId", flightId));

        config.setEnabled(false);
        FlightSimulationConfig savedConfig = configRepository.save(config);

        Flight flight = flightRepository.findById(flightId).orElse(null);
        log.info("Simulation stopped for flight ID: {}", flightId);

        return toStatusResponse(savedConfig, flight);
    }

    @Override
    public SimulationStatusResponse getSimulationStatus(String flightId) {
        FlightSimulationConfig config = configRepository.findByFlightId(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("FlightSimulation", "flightId", flightId));

        Flight flight = flightRepository.findById(flightId).orElse(null);
        return toStatusResponse(config, flight);
    }

    @Override
    public Optional<FlightSimulationEvent> stepSimulation(String flightId) {
        FlightSimulationConfig config = configRepository.findByFlightId(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("FlightSimulation", "flightId", flightId));

        return simulationEngine.stepSimulation(config);
    }

    @Override
    public List<SimulationStatusResponse> getActiveSimulations() {
        List<FlightSimulationConfig> configs = configRepository.findByEnabledTrueAndCompletedFalse();
        return configs.stream()
                .map(cfg -> {
                    Flight f = flightRepository.findById(cfg.getFlightId()).orElse(null);
                    return toStatusResponse(cfg, f);
                })
                .toList();
    }

    private SimulationStatusResponse toStatusResponse(FlightSimulationConfig config, Flight flight) {
        Integer delayMinutes = (flight != null) ? flight.getDelayMinutes() : null;
        String delayReason = (flight != null) ? flight.getDelayReason() : null;
        Instant revisedDep = (flight != null) ? flight.getRevisedDepartureTime() : null;
        Instant estArr = (flight != null) ? flight.getEstimatedArrival() : null;
        FlightStatus currentStatus = (flight != null) ? flight.getStatus() : config.getCurrentStatus();

        return SimulationStatusResponse.builder()
                .simulationId(config.getId())
                .flightId(config.getFlightId())
                .flightNumber(config.getFlightNumber())
                .enabled(config.isEnabled())
                .currentStatus(currentStatus)
                .speedMultiplier(config.getSpeedMultiplier())
                .delayProbability(config.getDelayProbability())
                .delayMinutes(delayMinutes)
                .delayReason(delayReason)
                .revisedDepartureTime(revisedDep)
                .estimatedArrival(estArr)
                .startTime(config.getStartTime())
                .lastTransitionAt(config.getLastTransitionAt())
                .completed(config.isCompleted())
                .build();
    }
}
