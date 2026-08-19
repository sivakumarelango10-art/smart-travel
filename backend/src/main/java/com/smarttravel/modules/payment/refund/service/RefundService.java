package com.smarttravel.modules.payment.refund.service;

import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.payment.refund.dto.RefundProcessRequest;
import com.smarttravel.modules.payment.refund.dto.RefundResponse;
import com.smarttravel.modules.payment.refund.model.RefundReason;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service managing payment refunds, idempotency, gateway execution, and state transitions.
 */
public interface RefundService {

    /**
     * Initiates and processes a refund for a payment.
     *
     * @param paymentId Payment MongoDB ID
     * @param request   Refund request parameters
     * @param createdBy Admin / System initiator
     * @return Processed refund details
     */
    RefundResponse processRefund(String paymentId, RefundProcessRequest request, String createdBy);

    /**
     * Automatically triggers refunds for all eligible bookings associated with a flight disruption.
     *
     * @param flightId Flight MongoDB ID
     * @param reason   Refund ground / reason
     * @param initiatedBy Initiator identifier
     * @return List of generated refund responses
     */
    List<RefundResponse> processDisruptionRefundsForFlight(String flightId, RefundReason reason, String initiatedBy);

    /**
     * Retrieves refund details by refund ID.
     *
     * @param refundId Refund MongoDB ID
     * @param userId   Requester user ID
     * @param isAdmin  Whether requester is admin
     * @return Refund details
     */
    RefundResponse getRefundById(String refundId, String userId, boolean isAdmin);

    /**
     * Retrieves refund details for a booking.
     *
     * @param bookingId Booking MongoDB ID
     * @param userId    Requester user ID
     * @param isAdmin   Whether requester is admin
     * @return Refund details
     */
    RefundResponse getRefundByBookingId(String bookingId, String userId, boolean isAdmin);

    /**
     * Lists paginated refunds (Admin only).
     *
     * @param status   Optional status filter
     * @param pageable Pagination parameters
     * @return Paginated refunds
     */
    PageResponse<RefundResponse> getAllRefunds(RefundStatus status, Pageable pageable);
}
