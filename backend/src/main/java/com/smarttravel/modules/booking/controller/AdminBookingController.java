package com.smarttravel.modules.booking.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.booking.dto.BookingCancelRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Administrator Booking Operations.
 */
@RestController
@RequestMapping("/api/v1/admin/bookings")
@Tag(name = "Admin Booking", description = "Administrator Booking Inspection and Forced Cancellation APIs")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @Operation(
            summary = "Get All System Bookings (Admin)",
            description = "Retrieves a paginated list of all bookings across all travelers."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ROLE_ADMIN authority required")
    })
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getAllBookings(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<BookingResponse> response = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(ApiResponse.success("All bookings retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Any Booking by ID (Admin)",
            description = "Retrieves any flight booking by its MongoDB ID with administrative access."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ROLE_ADMIN authority required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @Parameter(description = "Booking ID", example = "66c1e101f1a2b3c4d5e6f801")
            @PathVariable String id) {
        BookingResponse response = bookingService.getBookingById(id, null, true);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }

    @GetMapping("/reference/{bookingReference}")
    @Operation(
            summary = "Get Any Booking by PNR (Admin)",
            description = "Retrieves any flight booking by its PNR reference with administrative access."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ROLE_ADMIN authority required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(
            @Parameter(description = "PNR Reference", example = "ST8K4P2Q")
            @PathVariable String bookingReference) {
        BookingResponse response = bookingService.getBookingByReference(bookingReference, null, true);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Administrative Booking Cancellation",
            description = "Administratively cancels a booking and releases reserved cabin seats."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking cancelled by admin successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ROLE_ADMIN authority required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Booking is already cancelled")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @Parameter(description = "Booking ID", example = "66c1e101f1a2b3c4d5e6f801")
            @PathVariable String id,
            @RequestBody(required = false) BookingCancelRequest request) {
        BookingResponse response = bookingService.cancelBooking(id, request, null, true);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled administratively", response));
    }
}
