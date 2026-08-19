package com.smarttravel.modules.payment.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.common.security.UserPrincipal;
import com.smarttravel.modules.payment.dto.PaymentOrderCreateRequest;
import com.smarttravel.modules.payment.dto.PaymentOrderResponse;
import com.smarttravel.modules.payment.dto.PaymentResponse;
import com.smarttravel.modules.payment.dto.PaymentVerificationRequest;
import com.smarttravel.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Controller for payment order creation, cryptographic verification, and transaction retrieval.
 */
@RestController
@RequestMapping({"/api/v1/payments", "/v1/payments", "/api/payments"})
@Tag(name = "Payment Management", description = "Endpoints for Razorpay order generation and signature verification")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Create Razorpay Payment Order",
            description = "Creates an order on Razorpay for the specified booking. The payable amount is calculated server-side from the booking's fare snapshot."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payment order created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or non-payable amount"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found or not owned by authenticated user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Booking is already paid, cancelled, or expired")
    })
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createPaymentOrder(
            @Valid @RequestBody PaymentOrderCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        String userEmail = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUserEmail().orElse("user@smarttravel.com");

        PaymentOrderResponse response = paymentService.createPaymentOrder(request, userId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment order created successfully", response));
    }

    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Verify Razorpay Payment Signature",
            description = "Cryptographically verifies the HMAC-SHA256 signature returned by Razorpay Checkout. Transitions booking to CONFIRMED on success."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment signature verified and booking confirmed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Signature verification failed or invalid parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment order or booking not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Associated booking is cancelled or expired")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        String userEmail = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUserEmail().orElse("user@smarttravel.com");

        PaymentResponse response = paymentService.verifyPayment(request, userId, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", response));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Payment by ID",
            description = "Retrieves payment details by payment MongoDB ID with strict user ownership enforcement."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment record not found or access denied")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @Parameter(description = "Payment MongoDB ID", example = "66c1e101f1a2b3c4d5e6f901")
            @PathVariable String paymentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");

        PaymentResponse response = paymentService.getPaymentById(paymentId, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Payment by Booking ID",
            description = "Retrieves payment details associated with a booking ID for the authenticated owner."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment record not found or access denied")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBookingId(
            @Parameter(description = "Booking MongoDB ID", example = "66c1e101f1a2b3c4d5e6f801")
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");

        PaymentResponse response = paymentService.getPaymentByBookingId(bookingId, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }
}
