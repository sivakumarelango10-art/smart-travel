package com.smarttravel.modules.flight.tracking.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.flight.tracking.dto.TrackedFlightResponse;
import com.smarttravel.modules.flight.tracking.service.FlightTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user flight tracking subscriptions.
 * Allows authenticated users to track/untrack flights and view live status.
 */
@RestController
@RequestMapping("/v1/flights")
@Tag(name = "Flight Tracking", description = "User flight tracking and live status subscriptions")
public class FlightTrackingController {

    private final FlightTrackingService flightTrackingService;

    public FlightTrackingController(FlightTrackingService flightTrackingService) {
        this.flightTrackingService = flightTrackingService;
    }

    @Operation(summary = "Track a flight", description = "Subscribe to live status updates for a specific flight")
    @PostMapping("/{flightId}/track")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TrackedFlightResponse>> trackFlight(
            @PathVariable String flightId,
            Authentication authentication) {
        String userId = authentication.getName();
        TrackedFlightResponse response = flightTrackingService.trackFlight(flightId, userId);
        return ResponseEntity.ok(ApiResponse.success("Flight tracking activated", response));
    }

    @Operation(summary = "Stop tracking a flight")
    @DeleteMapping("/{flightId}/track")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> untrackFlight(
            @PathVariable String flightId,
            Authentication authentication) {
        String userId = authentication.getName();
        flightTrackingService.untrackFlight(flightId, userId);
        return ResponseEntity.ok(ApiResponse.success("Flight tracking deactivated"));
    }

    @Operation(summary = "List all tracked flights", description = "Returns all actively tracked flights with live operational status")
    @GetMapping("/tracked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TrackedFlightResponse>>> getTrackedFlights(
            Authentication authentication) {
        String userId = authentication.getName();
        List<TrackedFlightResponse> tracked = flightTrackingService.getTrackedFlights(userId);
        return ResponseEntity.ok(ApiResponse.success("Tracked flights retrieved", tracked));
    }

    @Operation(summary = "Check if a flight is being tracked")
    @GetMapping("/{flightId}/track/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> isTracking(
            @PathVariable String flightId,
            Authentication authentication) {
        String userId = authentication.getName();
        boolean tracking = flightTrackingService.isTracking(flightId, userId);
        return ResponseEntity.ok(ApiResponse.success("Tracking status retrieved", tracking));
    }
}
