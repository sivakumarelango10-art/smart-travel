package com.smarttravel.modules.ticket.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.common.security.UserPrincipal;
import com.smarttravel.modules.ticket.dto.TicketResponse;
import com.smarttravel.modules.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public User REST controller for electronic flight tickets and PDF downloads.
 */
@RestController
@RequestMapping({"/api/tickets", "/api/v1/tickets", "/v1/tickets"})
@Tag(name = "Tickets", description = "User E-Ticket Retrieval, History, and PDF Download Endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    @Operation(summary = "Get Authenticated User's Tickets", description = "Retrieves paginated list of tickets issued to the authenticated user.")
    public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> getUserTickets(
            @PageableDefault(size = 20, sort = "issuedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        PageResponse<TicketResponse> response = ticketService.getUserTickets(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("User tickets retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Ticket by ID", description = "Retrieves ticket details by internal ID with strict IDOR ownership checks.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket details"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found or unauthorized")
    })
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(
            @Parameter(description = "Ticket MongoDB ID", required = true) @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        TicketResponse response = ticketService.getTicketById(id, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", response));
    }

    @GetMapping("/number/{ticketNumber}")
    @Operation(summary = "Get Ticket by Ticket Number", description = "Retrieves ticket by public ticket number (e.g., ST-8K4P2Q7X9Y1Z).")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByNumber(
            @Parameter(description = "Public Ticket Number", required = true) @PathVariable String ticketNumber,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        TicketResponse response = ticketService.getTicketByNumber(ticketNumber, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", response));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get Ticket by Booking ID", description = "Retrieves the issued ticket associated with a booking ID.")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByBookingId(
            @Parameter(description = "Booking ID", required = true) @PathVariable String bookingId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        TicketResponse response = ticketService.getTicketByBookingId(bookingId, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", response));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download Ticket PDF", description = "Streams a deterministic binary PDF document for the specified ticket.")
    public ResponseEntity<byte[]> downloadTicketPdf(
            @Parameter(description = "Ticket ID", required = true) @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");

        TicketResponse ticket = ticketService.getTicketById(id, userId, false);
        byte[] pdfBytes = ticketService.generateTicketPdf(id, userId, false);

        String filename = "SmartTravel-Ticket-" + ticket.getTicketNumber() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0")
                .body(pdfBytes);
    }
}
