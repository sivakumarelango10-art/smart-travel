package com.smarttravel.modules.payment.webhook.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.payment.webhook.model.PaymentWebhookEvent;
import com.smarttravel.modules.payment.webhook.service.PaymentWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller receiving server-to-server webhook events from Razorpay.
 * Authenticated cryptographically via the X-Razorpay-Signature HTTP header.
 */
@RestController
@RequestMapping({"/api/v1/payments/webhook", "/v1/payments/webhook", "/api/payments/webhook"})
@Tag(name = "Payment Webhook", description = "Server-to-server endpoint for Razorpay asynchronous payment events")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    public PaymentWebhookController(PaymentWebhookService paymentWebhookService) {
        this.paymentWebhookService = paymentWebhookService;
    }

    @PostMapping
    @Operation(
            summary = "Razorpay Webhook Callback",
            description = "Receives asynchronous payment and order events directly from Razorpay servers. Cryptographically verified via HMAC-SHA256 signature against the raw payload."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Webhook received and reconciled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid webhook signature or malformed JSON payload")
    })
    public ResponseEntity<ApiResponse<PaymentWebhookEvent>> handleRazorpayWebhook(
            @RequestBody byte[] rawPayloadBytes,
            @Parameter(description = "HMAC-SHA256 signature header computed with webhook secret", required = true)
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signatureHeader) {

        PaymentWebhookEvent processedEvent = paymentWebhookService.handleWebhook(rawPayloadBytes, signatureHeader);
        return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully", processedEvent));
    }
}
