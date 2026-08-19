package com.smarttravel.modules.payment.gateway;

import com.smarttravel.modules.payment.gateway.dto.RazorpayOrderDto;

import java.util.Map;

/**
 * Gateway abstraction for Razorpay payment processing and signature verification.
 */
public interface RazorpayPaymentGateway {

    /**
     * Creates an order with the Razorpay payment gateway.
     *
     * @param receipt       Unique internal reference / receipt string
     * @param amountInPaise Payable amount in smallest currency unit (paise)
     * @param currency      ISO currency code (e.g. INR)
     * @param notes         Key-value metadata dictionary
     * @return Created Razorpay order details
     */
    RazorpayOrderDto createOrder(String receipt, long amountInPaise, String currency, Map<String, String> notes);

    /**
     * Verifies the cryptographic HMAC-SHA256 signature returned by Razorpay Checkout.
     *
     * @param orderId   Razorpay Order ID
     * @param paymentId Razorpay Payment ID
     * @param signature Cryptographic hex signature from frontend
     * @return True if signature is cryptographically valid, false otherwise
     */
    boolean verifyPaymentSignature(String orderId, String paymentId, String signature);

    /**
     * Initiates a refund for a previously captured Razorpay payment.
     *
     * @param paymentId     Razorpay Payment ID
     * @param amountInPaise Refund amount in paise
     * @param reason        Refund reason / note
     * @return Created Razorpay refund details
     */
    com.smarttravel.modules.payment.gateway.dto.RazorpayRefundDto refundPayment(String paymentId, long amountInPaise, String reason);
}


