package com.smarttravel.modules.flight.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/flights", "/v1/flights", "/api/flights"})
@Tag(name = "Flights", description = "Public Flight Search, Details, and Catalog APIs")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    @Operation(summary = "Search Flights", description = "Multi-criteria flight search supporting origin, destination, departure date, airline, cabin class, passenger count (1-9), price range, time windows, sorting, and pagination.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flights retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search parameters (e.g. identical route, invalid passenger count, past date, or negative price)")
    })
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> searchFlights(
            @ParameterObject FlightSearchCriteria criteria) {
        PageResponse<FlightResponse> results = flightService.searchFlights(criteria);
        return ResponseEntity.ok(ApiResponse.success("Flights retrieved successfully", results));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Flight by ID", description = "Retrieves full customer-facing details and cabin availability for a specific active flight by its database ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flight found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found or inactive")
    })
    public ResponseEntity<ApiResponse<FlightResponse>> getFlightById(
            @Parameter(description = "Flight MongoDB ObjectId", example = "66c1e101f1a2b3c4d5e6f702")
            @PathVariable String id) {
        FlightResponse response = flightService.getFlightById(id);
        return ResponseEntity.ok(ApiResponse.success("Flight retrieved successfully", response));
    }

    @GetMapping("/number/{flightNumber}")
    @Operation(summary = "Get Flight by Flight Number", description = "Retrieves flight schedule and details using its unique IATA flight number (e.g. AI-101) with automatic whitespace trimming and case normalization.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flight found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found or inactive")
    })
    public ResponseEntity<ApiResponse<FlightResponse>> getFlightByFlightNumber(
            @Parameter(description = "IATA Flight number", example = "AI-101")
            @PathVariable String flightNumber) {
        FlightResponse response = flightService.getFlightByFlightNumber(flightNumber);
        return ResponseEntity.ok(ApiResponse.success("Flight retrieved successfully", response));
    }
}
