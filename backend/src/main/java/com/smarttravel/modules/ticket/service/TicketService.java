package com.smarttravel.modules.ticket.service;

import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.ticket.dto.TicketResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service managing the lifecycle, issuance, retrieval, and PDF generation of flight tickets.
 */
public interface TicketService {

    /**
     * Issues an electronic ticket for a confirmed flight booking with verified payment.
     * Operation is strictly idempotent; if a ticket already exists for the booking, it returns the existing ticket.
     *
     * @param bookingId Booking MongoDB ID
     * @return Generated or existing Ticket response DTO
     */
    TicketResponse issueTicket(String bookingId);

    /**
     * Fetches a ticket by internal MongoDB ID with ownership/IDOR enforcement.
     *
     * @param id Ticket MongoDB ID
     * @param userId Authenticated user ID
     * @param isAdmin True if caller has ROLE_ADMIN
     * @return Ticket response DTO
     */
    TicketResponse getTicketById(String id, String userId, boolean isAdmin);

    /**
     * Fetches a ticket by public ticket number (e.g., ST-8K4P2Q7X9Y1Z) with ownership enforcement.
     *
     * @param ticketNumber Public ticket number
     * @param userId Authenticated user ID
     * @param isAdmin True if caller has ROLE_ADMIN
     * @return Ticket response DTO
     */
    TicketResponse getTicketByNumber(String ticketNumber, String userId, boolean isAdmin);

    /**
     * Fetches the ticket associated with a specific booking ID with ownership enforcement.
     *
     * @param bookingId Booking MongoDB ID
     * @param userId Authenticated user ID
     * @param isAdmin True if caller has ROLE_ADMIN
     * @return Ticket response DTO
     */
    TicketResponse getTicketByBookingId(String bookingId, String userId, boolean isAdmin);

    /**
     * Fetches paginated tickets belonging to the specified user.
     *
     * @param userId Authenticated user ID
     * @param pageable Pagination and sorting specifications
     * @return Paginated ticket response
     */
    PageResponse<TicketResponse> getUserTickets(String userId, Pageable pageable);

    /**
     * Admin method to fetch all issued tickets in the platform with pagination.
     *
     * @param pageable Pagination and sorting specifications
     * @return Paginated ticket response
     */
    PageResponse<TicketResponse> getAllTickets(Pageable pageable);

    /**
     * Generates and returns binary PDF bytes for the specified ticket.
     *
     * @param ticketId Ticket MongoDB ID
     * @param userId Authenticated user ID
     * @param isAdmin True if caller has ROLE_ADMIN
     * @return Raw PDF document bytes
     */
    byte[] generateTicketPdf(String ticketId, String userId, boolean isAdmin);

    /**
     * Synchronously marks the ticket associated with a booking as CANCELLED.
     *
     * @param bookingId Booking MongoDB ID
     * @param cancellationReason Reason for cancellation
     */
    void cancelTicketForBooking(String bookingId, String cancellationReason);

    /**
     * Admin retry mechanism to issue a ticket for a confirmed booking if initial issuance failed.
     *
     * @param bookingId Booking MongoDB ID
     * @return Ticket response DTO
     */
    TicketResponse retryIssueTicket(String bookingId);
}
