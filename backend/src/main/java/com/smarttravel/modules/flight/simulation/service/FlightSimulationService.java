package com.smarttravel.modules.flight.simulation.service;

import com.smarttravel.modules.flight.simulation.dto.SimulationStartRequest;
import com.smarttravel.modules.flight.simulation.dto.SimulationStatusResponse;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationEvent;

import java.util.List;
import java.util.Optional;

public interface FlightSimulationService {

    SimulationStatusResponse startSimulation(String flightId, SimulationStartRequest request);

    SimulationStatusResponse stopSimulation(String flightId);

    SimulationStatusResponse getSimulationStatus(String flightId);

    Optional<FlightSimulationEvent> stepSimulation(String flightId);

    List<SimulationStatusResponse> getActiveSimulations();
}
