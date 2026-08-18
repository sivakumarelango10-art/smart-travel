package com.smarttravel.modules.flight.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
import com.smarttravel.modules.flight.dto.FlightUpdateRequest;
import com.smarttravel.modules.flight.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/flights")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Flight Operations", description = "Privileged flight catalog management and status operations (Admin only)")
@SecurityRequirement(name = "BearerAuth")
public class AdminFlightController {

    private final FlightService flightService;

    public AdminFlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping
    @Operation(summary = "Create Flight", description = "Creates a new flight in the catalog. Duration is calculated server-side.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Flight created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Flight number already exists")
    })
    public ResponseEntity<ApiResponse<FlightResponse>> createFlight(
            @Valid @RequestBody FlightCreateRequest request) {
        FlightResponse response = flightService.createFlight(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Flight created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Flight", description = "Updates an existing flight's details and recalculates duration if times changed.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flight updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found")
    })
    public ResponseEntity<ApiResponse<FlightResponse>> updateFlight(
            @Parameter(description = "Flight MongoDB ObjectId", example = "66c1e101f1a2b3c4d5e6f702")
            @PathVariable String id,
            @Valid @RequestBody FlightUpdateRequest request) {
        FlightResponse response = flightService.updateFlight(id, request);
        return ResponseEntity.ok(ApiResponse.success("Flight updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Flight", description = "Deactivates / soft-deletes a flight from public search.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flight deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteFlight(
            @Parameter(description = "Flight MongoDB ObjectId", example = "66c1e101f1a2b3c4d5e6f702")
            @PathVariable String id) {
        flightService.deleteFlight(id);
        return ResponseEntity.ok(ApiResponse.success("Flight deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Flight Status", description = "Manually updates the operational status of a flight (Admin only).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flight status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found")
    })
    public ResponseEntity<ApiResponse<FlightResponse>> updateFlightStatus(
            @Parameter(description = "Flight MongoDB ObjectId", example = "66c1e101f1a2b3c4d5e6f702")
            @PathVariable String id,
            @Valid @RequestBody FlightStatusUpdateRequest request) {
        FlightResponse response = flightService.updateFlightStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Flight status updated successfully", response));
    }
}
