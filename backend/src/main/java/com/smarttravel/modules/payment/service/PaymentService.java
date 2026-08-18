package com.smarttravel.modules.payment.service;

import com.smarttravel.modules.payment.dto.PaymentOrderCreateRequest;
import com.smarttravel.modules.payment.dto.PaymentOrderResponse;
import com.smarttravel.modules.payment.dto.PaymentResponse;
import com.smarttravel.modules.payment.dto.PaymentVerificationRequest;

/**
 * Service managing payment order creation, cryptographic verification, and booking confirmation.
 */
public interface PaymentService {

    /**
     * Creates a Razorpay payment order for an existing authenticated user's booking.
     * The payable amount is strictly read from the booking's immutable fare snapshot.
     *
     * @param request   Order creation request containing booking ID
     * @param userId    Authenticated user's ID
     * @param userEmail Authenticated user's email
     * @return PaymentOrderResponse with Razorpay Order ID for frontend checkout
     */
    PaymentOrderResponse createPaymentOrder(PaymentOrderCreateRequest request, String userId, String userEmail);

    /**
     * Verifies the cryptographic HMAC-SHA256 signature returned by Razorpay Checkout.
     * On successful verification, transitions Payment to VERIFIED and Booking to CONFIRMED.
     *
     * @param request   Signature verification request payload
     * @param userId    Authenticated user's ID
     * @param userEmail Authenticated user's email
     * @return Verified PaymentResponse
     */
    PaymentResponse verifyPayment(PaymentVerificationRequest request, String userId, String userEmail);

    /**
     * Retrieves payment details by payment MongoDB ID with ownership enforcement.
     *
     * @param paymentId Payment document ID
     * @param userId    Requesting user ID
     * @param isAdmin   True if caller has ROLE_ADMIN
     * @return Payment details
     */
    PaymentResponse getPaymentById(String paymentId, String userId, boolean isAdmin);

    /**
     * Retrieves payment details by associated booking ID with ownership enforcement.
     *
     * @param bookingId Booking MongoDB ID
     * @param userId    Requesting user ID
     * @param isAdmin   True if caller has ROLE_ADMIN
     * @return Payment details
     */
    PaymentResponse getPaymentByBookingId(String bookingId, String userId, boolean isAdmin);
}
