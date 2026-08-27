package com.smarttravel.modules.hotel.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.common.security.UserPrincipal;
import com.smarttravel.modules.hotel.dto.HotelBookingDto;
import com.smarttravel.modules.hotel.service.HotelBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/hotels", "/v1/hotels", "/api/hotels"})
@Tag(name = "Hotel Bookings", description = "Hotel Room Reservations, Pricing, and Booking Management APIs")
public class HotelBookingController {

    private final HotelBookingService hotelBookingService;

    public HotelBookingController(HotelBookingService hotelBookingService) {
        this.hotelBookingService = hotelBookingService;
    }

    @Operation(summary = "Calculate authoritative stay price and tax breakdown (Public)")
    @PostMapping("/pricing/calculate")
    public ResponseEntity<ApiResponse<HotelBookingDto.PriceCalculateResponse>> calculatePrice(
            @Valid @RequestBody HotelBookingDto.PriceCalculateRequest request) {
        HotelBookingDto.PriceCalculateResponse response = hotelBookingService.calculatePrice(request);
        return ResponseEntity.ok(ApiResponse.success("Price calculated successfully", response));
    }

    @Operation(summary = "Create a Hotel Room Reservation (Requires Authentication)")
    @PostMapping("/bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HotelBookingDto.HotelBookingResponse>> createBooking(
            @Valid @RequestBody HotelBookingDto.CreateHotelBookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        String userEmail = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUserEmail().orElse("traveler@smarttravel.com");

        HotelBookingDto.HotelBookingResponse response = hotelBookingService.createBooking(request, userId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hotel reservation confirmed successfully", response));
    }

    @Operation(summary = "Get Authenticated User's Hotel Bookings")
    @GetMapping("/bookings/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<HotelBookingDto.HotelBookingResponse>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<HotelBookingDto.HotelBookingResponse> bookings = hotelBookingService.getUserBookings(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Hotel bookings retrieved successfully", bookings));
    }

    @Operation(summary = "Get Hotel Booking Details by ID")
    @GetMapping("/bookings/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HotelBookingDto.HotelBookingResponse>> getBookingById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        HotelBookingDto.HotelBookingResponse booking = hotelBookingService.getBookingById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Hotel booking retrieved successfully", booking));
    }

    @Operation(summary = "Get Hotel Booking Details by PNR Reference")
    @GetMapping("/bookings/reference/{reference}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HotelBookingDto.HotelBookingResponse>> getBookingByReference(
            @PathVariable String reference,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        HotelBookingDto.HotelBookingResponse booking = hotelBookingService.getBookingByReference(reference, userId);
        return ResponseEntity.ok(ApiResponse.success("Hotel booking retrieved successfully", booking));
    }

    @Operation(summary = "Cancel Hotel Reservation with Automated Refund Calculation")
    @PostMapping("/bookings/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HotelBookingDto.HotelBookingResponse>> cancelBooking(
            @PathVariable String id,
            @RequestBody(required = false) HotelBookingDto.CancelHotelBookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        String reason = request != null ? request.cancellationReason() : "Traveler requested cancellation";
        HotelBookingDto.HotelBookingResponse cancelled = hotelBookingService.cancelBooking(id, userId, reason);
        return ResponseEntity.ok(ApiResponse.success("Hotel reservation cancelled successfully", cancelled));
    }

    @Operation(summary = "Preview Refund Amount for a Hotel Booking")
    @GetMapping("/bookings/{id}/refund-preview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HotelBookingDto.HotelRefundCalculation>> getRefundPreview(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        HotelBookingDto.HotelRefundCalculation preview = hotelBookingService.calculateRefund(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Refund preview calculated", preview));
    }
}
