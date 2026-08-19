package com.smarttravel.modules.flight.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.flight.dto.SeatDto;
import com.smarttravel.modules.flight.dto.SeatMapResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.service.SeatMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for flight seat maps and availability.
 */
@RestController
@RequestMapping("/api/v1/flights")
@Tag(name = "Seats", description = "Aircraft Seat Maps, Seat Availability, and Cabin Layout APIs")
public class SeatController {

    private final SeatMapService seatMapService;

    public SeatController(SeatMapService seatMapService) {
        this.seatMapService = seatMapService;
    }

    @GetMapping("/{flightId}/seat-map")
    @Operation(
            summary = "Get Complete Aircraft Seat Map",
            description = "Retrieves full aircraft layout with seats grouped by cabin class and real-time availability."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Seat map retrieved successfully", content = @Content(schema = @Schema(implementation = SeatMapResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found")
    })
    public ResponseEntity<ApiResponse<SeatMapResponse>> getFlightSeatMap(
            @Parameter(description = "Flight MongoDB ID", example = "66c1e101f1a2b3c4d5e6f701")
            @PathVariable String flightId) {
        SeatMapResponse response = seatMapService.getFlightSeatMap(flightId);
        return ResponseEntity.ok(ApiResponse.success("Flight seat map retrieved successfully", response));
    }

    @GetMapping("/{flightId}/seats")
    @Operation(
            summary = "Get Flight Seats List",
            description = "Retrieves a flat list of seats on a flight, optionally filtered by cabin class."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Seats retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found")
    })
    public ResponseEntity<ApiResponse<List<SeatDto>>> getSeats(
            @Parameter(description = "Flight MongoDB ID", example = "66c1e101f1a2b3c4d5e6f701")
            @PathVariable String flightId,
            @Parameter(description = "Optional Cabin Class filter", example = "ECONOMY")
            @RequestParam(required = false) CabinClass cabinClass) {
        List<SeatDto> seats = seatMapService.getSeatsForFlight(flightId, cabinClass);
        return ResponseEntity.ok(ApiResponse.success("Flight seats retrieved successfully", seats));
    }
}
