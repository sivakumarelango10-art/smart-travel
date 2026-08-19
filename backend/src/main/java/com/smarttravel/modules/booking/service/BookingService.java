package com.smarttravel.modules.booking.service;

import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.booking.dto.BookingCancelRequest;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service managing the complete flight booking lifecycle and seat reservation.
 */
public interface BookingService {

    /**
     * Creates a new booking with atomic cabin seat deduction and price snapshotting.
     *
     * @param request   Booking creation payload
     * @param userId    Authenticated user's ID
     * @param userEmail Authenticated user's email
     * @return Customer-facing booking response
     */
    BookingResponse createBooking(BookingCreateRequest request, String userId, String userEmail);

    /**
     * Retrieves a booking by its MongoDB ID, enforcing user ownership or admin access.
     *
     * @param id      Booking ID
     * @param userId  Requesting user's ID
     * @param isAdmin True if caller has ROLE_ADMIN
     * @return Booking details
     */
    BookingResponse getBookingById(String id, String userId, boolean isAdmin);

    /**
     * Retrieves a booking by its unique PNR / booking reference.
     *
     * @param reference PNR reference code (e.g. ST8K4P2Q)
     * @param userId    Requesting user's ID
     * @param isAdmin   True if caller has ROLE_ADMIN
     * @return Booking details
     */
    BookingResponse getBookingByReference(String reference, String userId, boolean isAdmin);

    /**
     * Retrieves paginated bookings for the authenticated user with optional status filter.
     *
     * @param userId   User ID
     * @param status   Optional BookingStatus filter
     * @param pageable Pagination parameters
     * @return Page of booking responses
     */
    PageResponse<BookingResponse> getUserBookings(String userId, com.smarttravel.modules.booking.model.BookingStatus status, Pageable pageable);

    default PageResponse<BookingResponse> getUserBookings(String userId, Pageable pageable) {
        return getUserBookings(userId, null, pageable);
    }

    /**
     * Retrieves paginated bookings across all users with optional status filter (Admin operation).
     *
     * @param status   Optional BookingStatus filter
     * @param pageable Pagination parameters
     * @return Page of all booking responses
     */
    PageResponse<BookingResponse> getAllBookings(com.smarttravel.modules.booking.model.BookingStatus status, Pageable pageable);

    default PageResponse<BookingResponse> getAllBookings(Pageable pageable) {
        return getAllBookings(null, pageable);
    }

    /**
     * Cancels a booking, updates its status to CANCELLED, and releases reserved cabin seats atomically.
     *
     * @param id      Booking ID
     * @param request Cancellation request payload
     * @param userId  Requesting user's ID
     * @param isAdmin True if caller has ROLE_ADMIN
     * @return Updated booking response
     */
    BookingResponse cancelBooking(String id, BookingCancelRequest request, String userId, boolean isAdmin);
}
