package com.smarttravel.modules.ticket.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.response.PageResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative REST controller for inspecting tickets, global pagination, and manual issuance retries.
 */
@RestController
@RequestMapping({"/api/admin/tickets", "/api/v1/admin/tickets", "/v1/admin/tickets", "/admin/tickets"})
@Tag(name = "Admin Tickets", description = "Administrator Ticket Management and Retry APIs (Requires ROLE_ADMIN)")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTicketController {

    private final TicketService ticketService;

    public AdminTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    @Operation(summary = "Get All Issued Tickets", description = "Returns paginated list of all platform tickets with sorting.")
    public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> getAllTickets(
            @PageableDefault(size = 20, sort = "issuedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<TicketResponse> response = ticketService.getAllTickets(pageable);
        return ResponseEntity.ok(ApiResponse.success("All platform tickets retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Admin Get Ticket by ID", description = "Retrieves any ticket by internal ID.")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(
            @Parameter(description = "Ticket ID", required = true) @PathVariable String id) {
        TicketResponse response = ticketService.getTicketById(id, null, true);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", response));
    }

    @GetMapping("/number/{ticketNumber}")
    @Operation(summary = "Admin Get Ticket by Ticket Number", description = "Retrieves any ticket by public ticket number.")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByNumber(
            @Parameter(description = "Public Ticket Number", required = true) @PathVariable String ticketNumber) {
        TicketResponse response = ticketService.getTicketByNumber(ticketNumber, null, true);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", response));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Admin Get Ticket by Booking ID", description = "Retrieves ticket associated with a booking ID.")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByBookingId(
            @Parameter(description = "Booking ID", required = true) @PathVariable String bookingId) {
        TicketResponse response = ticketService.getTicketByBookingId(bookingId, null, true);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", response));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Admin Download Ticket PDF", description = "Admin endpoint to stream PDF document for any ticket.")
    public ResponseEntity<byte[]> downloadTicketPdf(
            @Parameter(description = "Ticket ID", required = true) @PathVariable String id) {
        TicketResponse ticket = ticketService.getTicketById(id, null, true);
        byte[] pdfBytes = ticketService.generateTicketPdf(id, null, true);
        String filename = "SmartTravel-Admin-Ticket-" + ticket.getTicketNumber() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }

    @PostMapping("/{bookingId}/issue")
    @Operation(summary = "Admin Retry Ticket Issuance", description = "Manually triggers/retries ticket issuance for a confirmed booking with verified payment.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket successfully issued or retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Booking not confirmed or in conflicting status")
    })
    public ResponseEntity<ApiResponse<TicketResponse>> retryIssueTicket(
            @Parameter(description = "Booking ID", required = true) @PathVariable String bookingId) {
        TicketResponse response = ticketService.retryIssueTicket(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Ticket successfully issued", response));
    }
}
