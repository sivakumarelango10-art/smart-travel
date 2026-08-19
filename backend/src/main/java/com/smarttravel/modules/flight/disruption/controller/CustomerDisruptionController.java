package com.smarttravel.modules.flight.disruption.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.common.security.UserPrincipal;
import com.smarttravel.modules.flight.disruption.dto.FlightDisruptionDto;
import com.smarttravel.modules.flight.disruption.dto.FlightOperationalStatusResponse;
import com.smarttravel.modules.flight.disruption.service.FlightDisruptionService;
import com.smarttravel.modules.payment.refund.dto.RefundResponse;
import com.smarttravel.modules.payment.refund.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller exposing customer-facing disruption queries and booking refund status.
 */
@RestController
@RequestMapping({"/api/v1", "/v1", "/api"})
@Tag(name = "Customer Flight Status & Disruptions", description = "Customer operational flight status queries and disruption history")
public class CustomerDisruptionController {

    private final FlightDisruptionService disruptionService;
    private final RefundService refundService;

    public CustomerDisruptionController(FlightDisruptionService disruptionService,
                                        RefundService refundService) {
        this.disruptionService = disruptionService;
        this.refundService = refundService;
    }

    @GetMapping("/flights/{id}/operational-status")
    @Operation(summary = "Get Flight Operational Status", description = "Retrieves live operational status, gate, terminal, delays, and disruptions")
    public ResponseEntity<ApiResponse<FlightOperationalStatusResponse>> getFlightOperationalStatus(@PathVariable String id) {
        FlightOperationalStatusResponse response = disruptionService.getFlightOperationalStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Flight operational status retrieved", response));
    }

    @GetMapping("/bookings/{bookingId}/disruptions")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get Booking Disruptions", description = "Retrieves disruption history for customer booking with strict ownership validation")
    public ResponseEntity<ApiResponse<List<FlightDisruptionDto>>> getBookingDisruptions(
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        boolean isAdmin = principal != null && principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<FlightDisruptionDto> response = disruptionService.getDisruptionsForBooking(bookingId, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Booking disruptions retrieved successfully", response));
    }

    @GetMapping("/bookings/{bookingId}/refund")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get Booking Refund Status", description = "Retrieves refund transaction details for customer booking with strict ownership validation")
    public ResponseEntity<ApiResponse<RefundResponse>> getBookingRefund(
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        boolean isAdmin = principal != null && principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        RefundResponse response = refundService.getRefundByBookingId(bookingId, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Booking refund retrieved successfully", response));
    }
}
