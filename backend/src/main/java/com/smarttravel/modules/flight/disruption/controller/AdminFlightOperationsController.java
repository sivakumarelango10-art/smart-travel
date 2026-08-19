package com.smarttravel.modules.flight.disruption.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.common.security.UserPrincipal;
import com.smarttravel.modules.flight.disruption.dto.FlightAircraftChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightCancelRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightDisruptionDto;
import com.smarttravel.modules.flight.disruption.dto.FlightGateChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightOperationalStatusResponse;
import com.smarttravel.modules.flight.disruption.dto.FlightScheduleChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightTerminalChangeRequest;
import com.smarttravel.modules.flight.disruption.service.FlightDisruptionService;
import com.smarttravel.modules.flight.impact.dto.FlightImpactSummaryDto;
import com.smarttravel.modules.flight.impact.service.FlightImpactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing administrative flight operations and disruption lifecycle endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/flights")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Flight Operations & Disruptions", description = "Operational controls for schedule changes, cancellations, gates, terminals, and aircraft swaps")
@SecurityRequirement(name = "BearerAuth")
public class AdminFlightOperationsController {

    private final FlightDisruptionService disruptionService;
    private final FlightImpactService flightImpactService;

    public AdminFlightOperationsController(FlightDisruptionService disruptionService,
                                           FlightImpactService flightImpactService) {
        this.disruptionService = disruptionService;
        this.flightImpactService = flightImpactService;
    }

    @PatchMapping("/{id}/schedule")
    @Operation(summary = "Reschedule Flight", description = "Reschedules flight departure and arrival times while preserving immutable published schedule")
    public ResponseEntity<ApiResponse<FlightOperationalStatusResponse>> rescheduleFlight(
            @PathVariable String id,
            @Valid @RequestBody FlightScheduleChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String adminUser = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUsernameOrAnonymous();
        FlightOperationalStatusResponse response = disruptionService.rescheduleFlight(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success("Flight rescheduled successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel Flight", description = "Operationally cancels flight, transitions status via state machine, marks bookings, releases seats, and initiates auto-refunds")
    public ResponseEntity<ApiResponse<FlightOperationalStatusResponse>> cancelFlight(
            @PathVariable String id,
            @Valid @RequestBody FlightCancelRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String adminUser = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUsernameOrAnonymous();
        FlightOperationalStatusResponse response = disruptionService.cancelFlight(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success("Flight cancelled and operational workflows triggered", response));
    }

    @PatchMapping("/{id}/gate")
    @Operation(summary = "Update Gate", description = "Updates departure gate and dispatches customer alerts")
    public ResponseEntity<ApiResponse<FlightOperationalStatusResponse>> updateGate(
            @PathVariable String id,
            @Valid @RequestBody FlightGateChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String adminUser = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUsernameOrAnonymous();
        FlightOperationalStatusResponse response = disruptionService.updateGate(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success("Gate updated successfully", response));
    }

    @PatchMapping("/{id}/terminal")
    @Operation(summary = "Update Terminal", description = "Updates departure terminal and dispatches customer alerts")
    public ResponseEntity<ApiResponse<FlightOperationalStatusResponse>> updateTerminal(
            @PathVariable String id,
            @Valid @RequestBody FlightTerminalChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String adminUser = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUsernameOrAnonymous();
        FlightOperationalStatusResponse response = disruptionService.updateTerminal(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success("Terminal updated successfully", response));
    }

    @PatchMapping("/{id}/aircraft")
    @Operation(summary = "Swap Aircraft", description = "Changes operating aircraft with cabin seat layout compatibility checks")
    public ResponseEntity<ApiResponse<FlightOperationalStatusResponse>> changeAircraft(
            @PathVariable String id,
            @Valid @RequestBody FlightAircraftChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String adminUser = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUsernameOrAnonymous();
        FlightOperationalStatusResponse response = disruptionService.changeAircraft(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success("Aircraft updated successfully", response));
    }

    @GetMapping("/{id}/disruptions")
    @Operation(summary = "Get Flight Disruption History", description = "Retrieves paginated history of operational disruptions for a flight")
    public ResponseEntity<ApiResponse<PageResponse<FlightDisruptionDto>>> getFlightDisruptions(
            @PathVariable String id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<FlightDisruptionDto> response = disruptionService.getFlightDisruptions(id, pageable);
        return ResponseEntity.ok(ApiResponse.success("Flight disruptions retrieved successfully", response));
    }

    @GetMapping("/{id}/impact")
    @Operation(summary = "Assess Customer Impact", description = "Returns metrics on affected bookings, passengers, and check-in counts")
    public ResponseEntity<ApiResponse<FlightImpactSummaryDto>> getDisruptionImpact(
            @PathVariable String id) {
        FlightImpactSummaryDto response = flightImpactService.getDisruptionImpactSummary(id);
        return ResponseEntity.ok(ApiResponse.success("Disruption impact summary retrieved", response));
    }

    @PostMapping("/disruptions/{disruptionId}/resolve")
    @Operation(summary = "Resolve Disruption", description = "Marks an active disruption as resolved")
    public ResponseEntity<ApiResponse<FlightDisruptionDto>> resolveDisruption(
            @PathVariable String disruptionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String adminUser = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUsernameOrAnonymous();
        FlightDisruptionDto response = disruptionService.resolveDisruption(disruptionId, adminUser);
        return ResponseEntity.ok(ApiResponse.success("Disruption marked as resolved", response));
    }
}
