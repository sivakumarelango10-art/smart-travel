package com.smarttravel.modules.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.payment.dto.PaymentOrderCreateRequest;
import com.smarttravel.modules.payment.dto.PaymentOrderResponse;
import com.smarttravel.modules.payment.dto.PaymentResponse;
import com.smarttravel.modules.payment.dto.PaymentVerificationRequest;
import com.smarttravel.modules.payment.model.PaymentMethod;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.service.PaymentService;
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

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/payments/orders: Success returns 201 Created")
    void testCreatePaymentOrder_Success() throws Exception {
        PaymentOrderCreateRequest request = PaymentOrderCreateRequest.builder()
                .bookingId("bk-100")
                .notes("Flight booking payment")
                .build();

        PaymentOrderResponse response = PaymentOrderResponse.builder()
                .paymentId("pay-100")
                .razorpayOrderId("order_rzp_123")
                .razorpayKeyId("rzp_test_key123")
                .amount(1710000L)
                .amountInRupees(new BigDecimal("17100.00"))
                .currency("INR")
                .bookingId("bk-100")
                .bookingReference("ST8K4P2Q")
                .status(PaymentStatus.ORDER_CREATED)
                .build();

        when(paymentService.createPaymentOrder(any(PaymentOrderCreateRequest.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentId").value("pay-100"))
                .andExpect(jsonPath("$.data.razorpayOrderId").value("order_rzp_123"))
                .andExpect(jsonPath("$.data.amount").value(1710000))
                .andExpect(jsonPath("$.data.amountInRupees").value(17100.00))
                .andExpect(jsonPath("$.data.bookingReference").value("ST8K4P2Q"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/orders: Missing bookingId returns 400 Bad Request")
    void testCreatePaymentOrder_MissingBookingId() throws Exception {
        PaymentOrderCreateRequest request = PaymentOrderCreateRequest.builder().bookingId("").build();

        mockMvc.perform(post("/api/v1/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/payments/orders: Cancelled booking returns 409 Conflict")
    void testCreatePaymentOrder_Conflict() throws Exception {
        PaymentOrderCreateRequest request = PaymentOrderCreateRequest.builder().bookingId("bk-cancelled").build();

        when(paymentService.createPaymentOrder(any(PaymentOrderCreateRequest.class), any(), any()))
                .thenThrow(new ConflictException("Cannot create payment for a cancelled booking"));

        mockMvc.perform(post("/api/v1/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot create payment for a cancelled booking"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/verify: Success returns 200 OK")
    void testVerifyPayment_Success() throws Exception {
        PaymentVerificationRequest request = PaymentVerificationRequest.builder()
                .razorpayOrderId("order_rzp_123")
                .razorpayPaymentId("pay_rzp_987")
                .razorpaySignature("valid_sig_hex")
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .id("pay-100")
                .bookingId("bk-100")
                .bookingReference("ST8K4P2Q")
                .userId("user-1")
                .razorpayOrderId("order_rzp_123")
                .razorpayPaymentId("pay_rzp_987")
                .amount(new BigDecimal("17100.00"))
                .amountPaise(1710000L)
                .currency("INR")
                .paymentStatus(PaymentStatus.VERIFIED)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .verifiedAt(Instant.now())
                .build();

        when(paymentService.verifyPayment(any(PaymentVerificationRequest.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.razorpayPaymentId").value("pay_rzp_987"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/verify: Invalid signature returns 400 Bad Request")
    void testVerifyPayment_InvalidSignature() throws Exception {
        PaymentVerificationRequest request = PaymentVerificationRequest.builder()
                .razorpayOrderId("order_rzp_123")
                .razorpayPaymentId("pay_rzp_987")
                .razorpaySignature("invalid_sig")
                .build();

        when(paymentService.verifyPayment(any(PaymentVerificationRequest.class), any(), any()))
                .thenThrow(new BadRequestException("Payment signature verification failed"));

        mockMvc.perform(post("/api/v1/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Payment signature verification failed"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/{paymentId}: Success returns 200 OK")
    void testGetPaymentById_Success() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .id("pay-100")
                .bookingId("bk-100")
                .bookingReference("ST8K4P2Q")
                .amount(new BigDecimal("17100.00"))
                .paymentStatus(PaymentStatus.ORDER_CREATED)
                .build();

        when(paymentService.getPaymentById(eq("pay-100"), any(), eq(false)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/pay-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("pay-100"))
                .andExpect(jsonPath("$.data.bookingReference").value("ST8K4P2Q"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/{paymentId}: Not found returns 404")
    void testGetPaymentById_NotFound() throws Exception {
        when(paymentService.getPaymentById(eq("pay-unknown"), any(), eq(false)))
                .thenThrow(new ResourceNotFoundException("Payment", "id", "pay-unknown"));

        mockMvc.perform(get("/api/v1/payments/pay-unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/booking/{bookingId}: Success returns 200 OK")
    void testGetPaymentByBookingId_Success() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .id("pay-100")
                .bookingId("bk-100")
                .paymentStatus(PaymentStatus.VERIFIED)
                .build();

        when(paymentService.getPaymentByBookingId(eq("bk-100"), any(), eq(false)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/booking/bk-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("pay-100"))
                .andExpect(jsonPath("$.data.bookingId").value("bk-100"));
    }
}
