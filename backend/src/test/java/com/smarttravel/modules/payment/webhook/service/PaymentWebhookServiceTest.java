package com.smarttravel.modules.payment.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.service.PaymentReconciliationService;
import com.smarttravel.modules.payment.webhook.RazorpayWebhookVerifier;
import com.smarttravel.modules.payment.webhook.model.PaymentWebhookEvent;
import com.smarttravel.modules.payment.webhook.model.WebhookProcessingStatus;
import com.smarttravel.modules.payment.webhook.repository.PaymentWebhookEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceTest {

    @Mock
    private RazorpayWebhookVerifier webhookVerifier;

    @Mock
    private PaymentWebhookEventRepository webhookEventRepository;

    @Mock
    private PaymentReconciliationService reconciliationService;

    private ObjectMapper objectMapper;
    private PaymentWebhookService webhookService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        webhookService = new PaymentWebhookServiceImpl(
                webhookVerifier,
                webhookEventRepository,
                reconciliationService,
                objectMapper
        );
    }

    @Test
    @DisplayName("Handle Webhook: Invalid signature throws BadRequestException")
    void testHandleWebhook_InvalidSignature() {
        byte[] payload = "{}".getBytes(StandardCharsets.UTF_8);
        when(webhookVerifier.verifyWebhookSignature(payload, "bad_sig")).thenReturn(false);

        assertThatThrownBy(() -> webhookService.handleWebhook(payload, "bad_sig"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid webhook signature");
    }

    @Test
    @DisplayName("Handle Webhook: payment.captured event reconciles payment and records PROCESSED event")
    void testHandleWebhook_PaymentCaptured_Success() {
        String json = """
                {
                  "event_id": "evt_12345",
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_98765",
                        "order_id": "order_54321",
                        "amount": 575000,
                        "currency": "INR",
                        "status": "captured"
                      }
                    }
                  }
                }
                """;
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        when(webhookVerifier.verifyWebhookSignature(payload, "valid_sig")).thenReturn(true);
        when(webhookVerifier.calculatePayloadHash(payload)).thenReturn("abc123hash");
        when(webhookEventRepository.findByEventId("evt_12345")).thenReturn(Optional.empty());
        when(webhookEventRepository.findFirstByRazorpayOrderIdAndEventTypeAndProcessingStatus(
                "order_54321", "payment.captured", WebhookProcessingStatus.PROCESSED)).thenReturn(Optional.empty());
        when(webhookEventRepository.save(any(PaymentWebhookEvent.class))).thenAnswer(i -> i.getArgument(0));

        PaymentWebhookEvent event = webhookService.handleWebhook(payload, "valid_sig");

        assertThat(event).isNotNull();
        assertThat(event.getProcessingStatus()).isEqualTo(WebhookProcessingStatus.PROCESSED);
        assertThat(event.getEventId()).isEqualTo("evt_12345");
        assertThat(event.getEventType()).isEqualTo("payment.captured");
        assertThat(event.getRazorpayOrderId()).isEqualTo("order_54321");
        assertThat(event.getRazorpayPaymentId()).isEqualTo("pay_98765");

        verify(reconciliationService).reconcilePaymentSuccess(
                eq("order_54321"), eq("pay_98765"), eq(575000L), eq("INR"), eq("payment.captured"));
    }

    @Test
    @DisplayName("Handle Webhook: payment.failed event reconciles failure and records PROCESSED event")
    void testHandleWebhook_PaymentFailed_Success() {
        String json = """
                {
                  "event_id": "evt_fail_1",
                  "event": "payment.failed",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_fail_123",
                        "order_id": "order_54321",
                        "amount": 575000,
                        "currency": "INR",
                        "error_description": "Bank system timeout"
                      }
                    }
                  }
                }
                """;
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        when(webhookVerifier.verifyWebhookSignature(payload, "valid_sig")).thenReturn(true);
        when(webhookEventRepository.findByEventId("evt_fail_1")).thenReturn(Optional.empty());
        when(webhookEventRepository.save(any(PaymentWebhookEvent.class))).thenAnswer(i -> i.getArgument(0));

        PaymentWebhookEvent event = webhookService.handleWebhook(payload, "valid_sig");

        assertThat(event).isNotNull();
        assertThat(event.getProcessingStatus()).isEqualTo(WebhookProcessingStatus.PROCESSED);
        verify(reconciliationService).reconcilePaymentFailure(eq("order_54321"), eq("pay_fail_123"), eq("Bank system timeout"));
    }

    @Test
    @DisplayName("Handle Webhook: Duplicate event ID returns existing record without repeating reconciliation")
    void testHandleWebhook_DuplicateEventId() {
        String json = """
                {
                  "event_id": "evt_dup_999",
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_98765",
                        "order_id": "order_54321"
                      }
                    }
                  }
                }
                """;
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        PaymentWebhookEvent existing = PaymentWebhookEvent.builder()
                .eventId("evt_dup_999")
                .eventType("payment.captured")
                .processingStatus(WebhookProcessingStatus.PROCESSED)
                .build();

        when(webhookVerifier.verifyWebhookSignature(payload, "valid_sig")).thenReturn(true);
        when(webhookEventRepository.findByEventId("evt_dup_999")).thenReturn(Optional.of(existing));

        PaymentWebhookEvent event = webhookService.handleWebhook(payload, "valid_sig");

        assertThat(event).isEqualTo(existing);
        verify(reconciliationService, never()).reconcilePaymentSuccess(anyString(), anyString(), anyLong(), anyString(), anyString());
        verify(webhookEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Handle Webhook: Unhandled event type is recorded as IGNORED without error")
    void testHandleWebhook_UnhandledEvent() {
        String json = """
                {
                  "event_id": "evt_refund_1",
                  "event": "refund.processed",
                  "payload": {}
                }
                """;
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        when(webhookVerifier.verifyWebhookSignature(payload, "valid_sig")).thenReturn(true);
        when(webhookEventRepository.save(any(PaymentWebhookEvent.class))).thenAnswer(i -> i.getArgument(0));

        PaymentWebhookEvent event = webhookService.handleWebhook(payload, "valid_sig");

        assertThat(event).isNotNull();
        assertThat(event.getProcessingStatus()).isEqualTo(WebhookProcessingStatus.IGNORED);
        verify(reconciliationService, never()).reconcilePaymentSuccess(anyString(), anyString(), anyLong(), anyString(), anyString());
    }
}
