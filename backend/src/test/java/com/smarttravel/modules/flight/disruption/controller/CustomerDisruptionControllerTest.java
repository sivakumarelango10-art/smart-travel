package com.smarttravel.modules.flight.disruption.controller;

import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.flight.disruption.dto.FlightDisruptionDto;
import com.smarttravel.modules.flight.disruption.dto.FlightOperationalStatusResponse;
import com.smarttravel.modules.flight.disruption.model.DisruptionType;
import com.smarttravel.modules.flight.disruption.service.FlightDisruptionService;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.payment.refund.dto.RefundResponse;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import com.smarttravel.modules.payment.refund.service.RefundService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerDisruptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerDisruptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightDisruptionService disruptionService;

    @MockBean
    private RefundService refundService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Public can query flight operational status")
    void shouldGetFlightOperationalStatus() throws Exception {
        FlightOperationalStatusResponse res = FlightOperationalStatusResponse.builder()
                .flightId("fl-101")
                .flightNumber("ST-101")
                .status(FlightStatus.DELAYED)
                .delayMinutes(35)
                .gate("12A")
                .build();

        when(disruptionService.getFlightOperationalStatus("fl-101")).thenReturn(res);

        mockMvc.perform(get("/api/v1/flights/fl-101/operational-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flightNumber").value("ST-101"))
                .andExpect(jsonPath("$.data.delayMinutes").value(35));
    }

    @Test
    @DisplayName("Customer can query disruption history for their booking")
    @WithMockUser(username = "sarah@smarttravel.com")
    void shouldGetBookingDisruptions() throws Exception {
        FlightDisruptionDto dto = FlightDisruptionDto.builder()
                .id("disrupt-1")
                .disruptionType(DisruptionType.GATE_CHANGE)
                .newGate("15C")
                .build();

        when(disruptionService.getDisruptionsForBooking(eq("bk-101"), any(), eq(false)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/bookings/bk-101/disruptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].newGate").value("15C"));
    }

    @Test
    @DisplayName("Customer can query refund status for their booking")
    @WithMockUser(username = "sarah@smarttravel.com")
    void shouldGetBookingRefund() throws Exception {
        RefundResponse refund = RefundResponse.builder()
                .id("rfnd-101")
                .refundNumber("RF-123456789012")
                .status(RefundStatus.COMPLETED)
                .build();

        when(refundService.getRefundByBookingId(eq("bk-101"), any(), eq(false)))
                .thenReturn(refund);

        mockMvc.perform(get("/api/v1/bookings/bk-101/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.refundNumber").value("RF-123456789012"));
    }
}
