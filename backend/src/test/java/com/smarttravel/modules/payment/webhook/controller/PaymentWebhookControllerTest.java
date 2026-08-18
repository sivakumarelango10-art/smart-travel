package com.smarttravel.modules.payment.webhook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.payment.webhook.model.PaymentWebhookEvent;
import com.smarttravel.modules.payment.webhook.model.WebhookProcessingStatus;
import com.smarttravel.modules.payment.webhook.service.PaymentWebhookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentWebhookController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PaymentWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentWebhookService paymentWebhookService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/payments/webhook: Valid signature returns 200 OK")
    void testHandleWebhook_Success() throws Exception {
        String payload = "{\"event\":\"payment.captured\",\"payload\":{}}";

        PaymentWebhookEvent responseEvent = PaymentWebhookEvent.builder()
                .id("evt-100")
                .eventId("evt_rzp_123")
                .eventType("payment.captured")
                .processingStatus(WebhookProcessingStatus.PROCESSED)
                .receivedAt(Instant.now())
                .build();

        when(paymentWebhookService.handleWebhook(any(byte[].class), eq("valid_sig_hex")))
                .thenReturn(responseEvent);

        mockMvc.perform(post("/api/v1/payments/webhook")
                        .header("X-Razorpay-Signature", "valid_sig_hex")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("evt-100"))
                .andExpect(jsonPath("$.data.eventType").value("payment.captured"))
                .andExpect(jsonPath("$.data.processingStatus").value("PROCESSED"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/webhook: Invalid signature returns 400 Bad Request")
    void testHandleWebhook_InvalidSignature() throws Exception {
        String payload = "{\"event\":\"payment.captured\"}";

        when(paymentWebhookService.handleWebhook(any(byte[].class), eq("invalid_sig")))
                .thenThrow(new BadRequestException("Invalid webhook signature"));

        mockMvc.perform(post("/api/v1/payments/webhook")
                        .header("X-Razorpay-Signature", "invalid_sig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid webhook signature"));
    }
}
