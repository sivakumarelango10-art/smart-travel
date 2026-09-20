package com.smarttravel.modules.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.modules.payment.config.RazorpayProperties;
import com.smarttravel.modules.payment.gateway.RazorpayPaymentGateway;
import org.junit.jupiter.api.BeforeEach;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RazorpayStandardCheckoutController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RazorpayStandardCheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RazorpayProperties razorpayProperties;

    @MockBean
    private RazorpayPaymentGateway paymentGateway;

    @MockBean
    private com.smarttravel.common.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.smarttravel.common.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.smarttravel.common.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String TEST_KEY_ID = "rzp_test_TdmwlBNwLKKPnN";
    private static final String TEST_KEY_SECRET = "BsKfWsuT3TMxdLf5zE7CJISs";

    @BeforeEach
    void setUp() {
        when(razorpayProperties.getKeyId()).thenReturn(TEST_KEY_ID);
        when(razorpayProperties.getKeySecret()).thenReturn(TEST_KEY_SECRET);
        when(razorpayProperties.getCurrency()).thenReturn("INR");
    }

    @Test
    @DisplayName("Create Order: rejects amount less than 100 paise")
    void testCreateOrderUnderMinimumAmount() throws Exception {
        Map<String, Object> req = Map.of(
                "amount", 50, // Less than 100 paise
                "currency", "INR"
        );

        mockMvc.perform(post("/api/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid amount. Minimum amount is 100 paise (1 INR)."));
    }

    @Test
    @DisplayName("Create Order: successfully returns order_id, amount, and currency")
    void testCreateOrderSuccess() throws Exception {
        Map<String, Object> req = Map.of(
                "amount", 50000, // 500 INR
                "currency", "INR",
                "receipt", "rcpt_12345"
        );

        mockMvc.perform(post("/api/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.order_id").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(50000))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.key_id").value(TEST_KEY_ID));
    }

    @Test
    @DisplayName("Verify Signature: rejects request with missing fields")
    void testVerifyPaymentMissingFields() throws Exception {
        Map<String, String> req = Map.of(
                "razorpay_order_id", "order_123"
                // Missing payment ID and signature
        );

        mockMvc.perform(post("/api/verify-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Missing required fields: razorpay_order_id, razorpay_payment_id, razorpay_signature"));
    }

    @Test
    @DisplayName("Verify Signature: successfully verifies authentic HMAC-SHA256 signature")
    void testVerifyPaymentValidHmacSignature() throws Exception {
        String orderId = "order_rzp_999";
        String paymentId = "pay_rzp_888";

        // Calculate HMAC-SHA256(orderId + "|" + paymentId, secret)
        String payload = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(TEST_KEY_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        String validSignature = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));

        when(paymentGateway.verifyPaymentSignature(orderId, paymentId, validSignature)).thenReturn(false);

        Map<String, String> req = Map.of(
                "razorpay_order_id", orderId,
                "razorpay_payment_id", paymentId,
                "razorpay_signature", validSignature
        );

        mockMvc.perform(post("/api/verify-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment verified successfully"))
                .andExpect(jsonPath("$.order_id").value(orderId))
                .andExpect(jsonPath("$.payment_id").value(paymentId));
    }

    @Test
    @DisplayName("Verify Signature: rejects mismatched signature")
    void testVerifyPaymentMismatchedSignature() throws Exception {
        String orderId = "order_rzp_999";
        String paymentId = "pay_rzp_888";
        String badSignature = "invalid_tampered_signature_hex";

        when(paymentGateway.verifyPaymentSignature(orderId, paymentId, badSignature)).thenReturn(false);

        Map<String, String> req = Map.of(
                "razorpay_order_id", orderId,
                "razorpay_payment_id", paymentId,
                "razorpay_signature", badSignature
        );

        mockMvc.perform(post("/api/verify-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Payment signature verification failed. Mismatch between expected and received signature."));
    }

    @Test
    @DisplayName("Get Config: returns public key ID")
    void testGetRazorpayConfig() throws Exception {
        mockMvc.perform(get("/api/razorpay/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key_id").value(TEST_KEY_ID))
                .andExpect(jsonPath("$.currency").value("INR"));
    }
}
