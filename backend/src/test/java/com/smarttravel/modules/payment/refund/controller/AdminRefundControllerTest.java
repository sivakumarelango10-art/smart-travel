package com.smarttravel.modules.payment.refund.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.security.CustomUserDetailsService;

import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.payment.refund.dto.RefundEligibilityResponse;
import com.smarttravel.modules.payment.refund.dto.RefundProcessRequest;
import com.smarttravel.modules.payment.refund.dto.RefundResponse;
import com.smarttravel.modules.payment.refund.model.RefundReason;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import com.smarttravel.modules.payment.refund.service.RefundEligibilityService;
import com.smarttravel.modules.payment.refund.service.RefundService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRefundController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminRefundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefundService refundService;

    @MockBean
    private RefundEligibilityService refundEligibilityService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Admin can process payment refund")
    @WithMockUser(roles = "ADMIN")
    void shouldProcessRefundSuccessfully() throws Exception {
        RefundProcessRequest req = new RefundProcessRequest(RefundReason.FLIGHT_CANCELLED, "Operational cancellation");
        RefundResponse res = RefundResponse.builder()
                .id("rfnd-1")
                .refundNumber("RF-123456789012")
                .amount(BigDecimal.valueOf(5190.00))
                .status(RefundStatus.COMPLETED)
                .build();

        when(refundService.processRefund(eq("pay-1"), any(), any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/admin/refunds/pay-1/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.refundNumber").value("RF-123456789012"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Admin can check refund eligibility")
    @WithMockUser(roles = "ADMIN")
    void shouldCheckRefundEligibility() throws Exception {
        RefundEligibilityResponse res = RefundEligibilityResponse.builder()
                .paymentId("pay-1")
                .eligible(true)
                .refundableAmount(BigDecimal.valueOf(5190.00))
                .build();

        when(refundEligibilityService.checkPaymentRefundEligibility(eq("pay-1"), any())).thenReturn(res);

        mockMvc.perform(get("/api/v1/admin/refunds/pay-1/eligibility"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(true));
    }
}
