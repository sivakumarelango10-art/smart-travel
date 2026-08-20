package com.smarttravel.modules.booking.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.common.security.UserPrincipal;
import com.smarttravel.modules.booking.dto.BookingCancelRequest;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Customer Flight Bookings and Reservations.
 */
@RestController
@RequestMapping({"/api/v1/bookings", "/v1/bookings", "/api/bookings"})
@Tag(name = "Booking", description = "Flight Booking Creation, User History, PNR Lookup, and Cancellation APIs")
@SecurityRequirement(name = "BearerAuth")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(
            summary = "Create Flight Booking & Reserve Seats",
            description = "Atomically reserves cabin seat inventory, snapshots price and fare breakdown, generates a unique PNR, and creates a confirmed booking."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Booking created and seats reserved successfully", content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or non-bookable flight"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found or inactive"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Insufficient seat availability in selected cabin")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        String userEmail = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUserEmail().orElse("user@smarttravel.com");

        BookingResponse response = bookingService.createBooking(request, userId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", response));
    }

    @GetMapping({"", "/my-bookings"})
    @Operation(
            summary = "Get Authenticated User's Bookings",
            description = "Retrieves a paginated list of bookings owned by the authenticated traveler."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User bookings retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required")
    })
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getUserBookings(
            @Parameter(description = "Optional Booking Status filter", example = "CONFIRMED")
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.smarttravel.modules.booking.model.BookingStatus status,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");

        PageResponse<BookingResponse> response = bookingService.getUserBookings(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("User bookings retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Booking by ID",
            description = "Retrieves booking details by MongoDB ID for the authenticated owner."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found or access denied")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @Parameter(description = "Booking MongoDB ID", example = "66c1e101f1a2b3c4d5e6f801")
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");

        BookingResponse response = bookingService.getBookingById(id, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }

    @GetMapping("/reference/{bookingReference}")
    @Operation(
            summary = "Get Booking by PNR Reference",
            description = "Retrieves booking details by unique PNR reference for the authenticated owner."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found or access denied")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(
            @Parameter(description = "PNR Booking Reference", example = "ST8K4P2Q")
            @PathVariable String bookingReference,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");

        BookingResponse response = bookingService.getBookingByReference(bookingReference, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel Booking & Release Seats",
            description = "Cancels a confirmed booking, transitions status to CANCELLED, and atomically releases reserved seats back to the original cabin inventory."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking cancelled and seats released successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found or access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Booking is already in a terminal status (e.g. CANCELLED)")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @Parameter(description = "Booking MongoDB ID", example = "66c1e101f1a2b3c4d5e6f801")
            @PathVariable String id,
            @RequestBody(required = false) BookingCancelRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");

        BookingResponse response = bookingService.cancelBooking(id, request, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }
}
