package com.smarttravel.modules.flight.simulation.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.flight.simulation.dto.SimulationStartRequest;
import com.smarttravel.modules.flight.simulation.dto.SimulationStatusResponse;
import com.smarttravel.modules.flight.simulation.model.FlightSimulationEvent;
import com.smarttravel.modules.flight.simulation.service.FlightSimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/api/v1/admin/flight-simulation", "/v1/admin/flight-simulation", "/api/admin/flight-simulation", "/admin/flight-simulation"})
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Flight Simulation", description = "Mock real-time flight status simulation and testing engine")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminFlightSimulationController {

    private final FlightSimulationService simulationService;

    public AdminFlightSimulationController(FlightSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/{flightId}/start")
    @Operation(summary = "Start Flight Simulation", description = "Enables and starts the mock status transition simulation for a flight (Admin only).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Simulation started successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Flight in terminal state or invalid configuration"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found")
    })
    public ResponseEntity<ApiResponse<SimulationStatusResponse>> startSimulation(
            @Parameter(description = "Flight MongoDB ObjectId", example = "66c1e101f1a2b3c4d5e6f702")
            @PathVariable String flightId,
            @Valid @RequestBody(required = false) SimulationStartRequest request) {
        SimulationStatusResponse response = simulationService.startSimulation(flightId, request);
        return ResponseEntity.ok(ApiResponse.success("Flight simulation started successfully", response));
    }

    @PostMapping("/{flightId}/stop")
    @Operation(summary = "Stop Flight Simulation", description = "Deactivates the mock status transition simulation for a flight (Admin only).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Simulation stopped successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight simulation not found")
    })
    public ResponseEntity<ApiResponse<SimulationStatusResponse>> stopSimulation(
            @Parameter(description = "Flight MongoDB ObjectId", example = "66c1e101f1a2b3c4d5e6f702")
            @PathVariable String flightId) {
        SimulationStatusResponse response = simulationService.stopSimulation(flightId);
        return ResponseEntity.ok(ApiResponse.success("Flight simulation stopped successfully", response));
    }

    @GetMapping("/{flightId}")
    @Operation(summary = "Get Simulation Status", description = "Retrieves the current simulation state and progress for a flight (Admin only).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Simulation status retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight simulation not found")
    })
    public ResponseEntity<ApiResponse<SimulationStatusResponse>> getSimulationStatus(
            @Parameter(description = "Flight MongoDB ObjectId", example = "66c1e101f1a2b3c4d5e6f702")
            @PathVariable String flightId) {
        SimulationStatusResponse response = simulationService.getSimulationStatus(flightId);
        return ResponseEntity.ok(ApiResponse.success("Simulation status retrieved successfully", response));
    }

    @PostMapping("/{flightId}/step")
    @Operation(summary = "Step Flight Simulation", description = "Manually advances the simulation by exactly one transition cycle (Admin only).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Simulation stepped"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight simulation not found")
    })
    public ResponseEntity<ApiResponse<FlightSimulationEvent>> stepSimulation(
            @Parameter(description = "Flight MongoDB ObjectId", example = "66c1e101f1a2b3c4d5e6f702")
            @PathVariable String flightId) {
        Optional<FlightSimulationEvent> event = simulationService.stepSimulation(flightId);
        return ResponseEntity.ok(ApiResponse.success("Simulation stepped successfully", event.orElse(null)));
    }

    @GetMapping
    @Operation(summary = "Get All Active Simulations", description = "Retrieves all currently running flight simulations (Admin only).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active simulations retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponse<List<SimulationStatusResponse>>> getActiveSimulations() {
        List<SimulationStatusResponse> responses = simulationService.getActiveSimulations();
        return ResponseEntity.ok(ApiResponse.success("Active simulations retrieved successfully", responses));
    }
}
