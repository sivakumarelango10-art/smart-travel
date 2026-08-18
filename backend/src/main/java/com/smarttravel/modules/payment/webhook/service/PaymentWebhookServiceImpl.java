package com.smarttravel.modules.payment.webhook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.payment.service.PaymentReconciliationService;
import com.smarttravel.modules.payment.webhook.RazorpayWebhookVerifier;
import com.smarttravel.modules.payment.webhook.model.PaymentWebhookEvent;
import com.smarttravel.modules.payment.webhook.model.WebhookProcessingStatus;
import com.smarttravel.modules.payment.webhook.repository.PaymentWebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of PaymentWebhookService handling signature verification, JSON parsing,
 * event deduplication, and reconciliation dispatch.
 */
@Service
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookServiceImpl.class);

    private static final Set<String> SUCCESS_EVENTS = Set.of(
            "payment.captured",
            "payment.authorized",
            "order.paid"
    );

    private static final Set<String> FAILURE_EVENTS = Set.of(
            "payment.failed"
    );

    private final RazorpayWebhookVerifier webhookVerifier;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final PaymentReconciliationService reconciliationService;
    private final ObjectMapper objectMapper;

    public PaymentWebhookServiceImpl(RazorpayWebhookVerifier webhookVerifier,
                                     PaymentWebhookEventRepository webhookEventRepository,
                                     PaymentReconciliationService reconciliationService,
                                     ObjectMapper objectMapper) {
        this.webhookVerifier = webhookVerifier;
        this.webhookEventRepository = webhookEventRepository;
        this.reconciliationService = reconciliationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentWebhookEvent handleWebhook(byte[] rawPayloadBytes, String signatureHeader) {
        // 1. Authenticate signature against raw body bytes
        boolean isValid = webhookVerifier.verifyWebhookSignature(rawPayloadBytes, signatureHeader);
        if (!isValid) {
            log.warn("Unauthorized webhook request rejected: Invalid or missing X-Razorpay-Signature header");
            throw new BadRequestException("Invalid webhook signature");
        }

        String payloadHash = webhookVerifier.calculatePayloadHash(rawPayloadBytes);

        // 2. Parse JSON payload
        JsonNode root;
        try {
            root = objectMapper.readTree(rawPayloadBytes);
        } catch (Exception e) {
            log.error("Failed to parse incoming webhook payload as JSON", e);
            throw new BadRequestException("Malformed JSON payload in webhook request");
        }

        String eventId = root.has("event_id") ? root.path("event_id").asText(null) : root.path("id").asText(null);
        String eventType = root.path("event").asText("");

        // 3. Extract payment and order details
        String razorpayOrderId = null;
        String razorpayPaymentId = null;
        long amount = 0L;
        String currency = "INR";
        String errorDescription = null;

        if (eventType.startsWith("payment.")) {
            JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
            razorpayPaymentId = paymentEntity.path("id").asText(null);
            razorpayOrderId = paymentEntity.path("order_id").asText(null);
            amount = paymentEntity.path("amount").asLong(0L);
            currency = paymentEntity.path("currency").asText("INR");
            errorDescription = paymentEntity.path("error_description").asText(null);
        } else if (eventType.startsWith("order.")) {
            JsonNode orderEntity = root.path("payload").path("order").path("entity");
            razorpayOrderId = orderEntity.path("id").asText(null);
            amount = orderEntity.path("amount_paid").asLong(orderEntity.path("amount").asLong(0L));
            currency = orderEntity.path("currency").asText("INR");
        }

        log.info("Received Razorpay Webhook Event: '{}', Event ID: {}, Order ID: {}, Payment ID: {}",
                eventType, eventId, razorpayOrderId, razorpayPaymentId);

        // 4. Enforce Webhook Idempotency (Deduplication)
        if (eventId != null && !eventId.isBlank()) {
            Optional<PaymentWebhookEvent> existingEvent = webhookEventRepository.findByEventId(eventId);
            if (existingEvent.isPresent() && existingEvent.get().getProcessingStatus() == WebhookProcessingStatus.PROCESSED) {
                log.info("Webhook Event ID: {} already successfully processed. Returning existing record.", eventId);
                return existingEvent.get();
            }
        }

        if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
            Optional<PaymentWebhookEvent> existingOrderEvent =
                    webhookEventRepository.findFirstByRazorpayOrderIdAndEventTypeAndProcessingStatus(
                            razorpayOrderId, eventType, WebhookProcessingStatus.PROCESSED);
            if (existingOrderEvent.isPresent()) {
                log.info("Webhook event '{}' for Order ID: {} already processed. Returning existing record.", eventType, razorpayOrderId);
                return existingOrderEvent.get();
            }
        }

        // 5. Initialize Audit Document
        PaymentWebhookEvent auditEvent = PaymentWebhookEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .razorpayOrderId(razorpayOrderId)
                .razorpayPaymentId(razorpayPaymentId)
                .payloadHash(payloadHash)
                .receivedAt(Instant.now())
                .processingStatus(WebhookProcessingStatus.RECEIVED)
                .build();

        // 6. Dispatch based on event type
        try {
            if (SUCCESS_EVENTS.contains(eventType)) {
                reconciliationService.reconcilePaymentSuccess(razorpayOrderId, razorpayPaymentId, amount, currency, eventType);
                auditEvent.setProcessingStatus(WebhookProcessingStatus.PROCESSED);
                auditEvent.setProcessedAt(Instant.now());
            } else if (FAILURE_EVENTS.contains(eventType)) {
                reconciliationService.reconcilePaymentFailure(razorpayOrderId, razorpayPaymentId, errorDescription);
                auditEvent.setProcessingStatus(WebhookProcessingStatus.PROCESSED);
                auditEvent.setProcessedAt(Instant.now());
            } else {
                log.info("Ignoring unhandled Razorpay webhook event type: {}", eventType);
                auditEvent.setProcessingStatus(WebhookProcessingStatus.IGNORED);
                auditEvent.setProcessedAt(Instant.now());
            }
        } catch (Exception ex) {
            log.error("Error occurred while reconciling webhook event '{}' for order '{}'", eventType, razorpayOrderId, ex);
            auditEvent.setProcessingStatus(WebhookProcessingStatus.FAILED);
            auditEvent.setProcessingError(ex.getMessage());
            auditEvent.setProcessedAt(Instant.now());
        }

        return webhookEventRepository.save(auditEvent);
    }
}
