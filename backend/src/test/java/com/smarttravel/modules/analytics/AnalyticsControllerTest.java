package com.smarttravel.modules.analytics;

import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.modules.analytics.controller.AnalyticsController;
import com.smarttravel.modules.analytics.dto.*;
import com.smarttravel.modules.analytics.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.RequestIdFilter;
import com.smarttravel.common.security.SecurityConfig;

/**
 * MockMvc security tests for AnalyticsController.
 * Verifies RBAC: unauthenticated → 401, customer → 403, admin → 200.
 * Uses @WebMvcTest with Spring Security enabled.
 */
@WebMvcTest(controllers = AnalyticsController.class,
        excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class})
@AutoConfigureMockMvc
@Import({
        SecurityConfig.class,
        RequestIdFilter.class,
        JwtAuthenticationFilter.class,
        GlobalExceptionHandler.class
})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private com.smarttravel.common.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.smarttravel.common.security.CustomUserDetailsService customUserDetailsService;

    private OverviewAnalyticsResponse sampleOverview;
    private RevenueAnalyticsResponse sampleRevenue;
    private BookingAnalyticsResponse sampleBookings;
    private FlightAnalyticsResponse sampleFlights;
    private SeatAnalyticsResponse sampleSeats;
    private PaymentAnalyticsResponse samplePayments;
    private CustomerAnalyticsResponse sampleCustomers;

    @BeforeEach
    void setUp() {
        sampleOverview = OverviewAnalyticsResponse.builder()
                .totalBookings(100L)
                .confirmedBookings(80L)
                .totalGrossRevenue(BigDecimal.valueOf(500000))
                .totalNetRevenue(BigDecimal.valueOf(480000))
                .totalRefundedAmount(BigDecimal.valueOf(20000))
                .generatedAt(Instant.now())
                .build();

        sampleRevenue = RevenueAnalyticsResponse.builder()
                .grossRevenue(BigDecimal.valueOf(100000))
                .netRevenue(BigDecimal.valueOf(95000))
                .refundedAmount(BigDecimal.valueOf(5000))
                .successfulPaymentCount(50L)
                .trend(Collections.emptyList())
                .period("last30days")
                .build();

        sampleBookings = BookingAnalyticsResponse.builder()
                .totalBookings(50L)
                .confirmedBookings(40L)
                .confirmationRate(BigDecimal.valueOf(80))
                .trend(Collections.emptyList())
                .period("last30days")
                .build();

        sampleFlights = FlightAnalyticsResponse.builder()
                .totalFlights(20L)
                .activeFlights(15L)
                .topByRevenue(Collections.emptyList())
                .topByBookings(Collections.emptyList())
                .topByOccupancy(Collections.emptyList())
                .leastUtilized(Collections.emptyList())
                .build();

        sampleSeats = SeatAnalyticsResponse.builder()
                .totalSeats(2000L)
                .bookedSeats(1200L)
                .availableSeats(700L)
                .heldSeats(100L)
                .overallOccupancyPercentage(BigDecimal.valueOf(60))
                .cabinUtilization(Collections.emptyList())
                .build();

        samplePayments = PaymentAnalyticsResponse.builder()
                .totalPayments(100L)
                .successfulPayments(85L)
                .failedPayments(15L)
                .paymentSuccessRate(BigDecimal.valueOf(85))
                .trend(Collections.emptyList())
                .period("last30days")
                .build();

        sampleCustomers = CustomerAnalyticsResponse.builder()
                .totalCustomers(500L)
                .activeCustomers(450L)
                .customersWithBookings(300L)
                .trend(Collections.emptyList())
                .period("last30days")
                .build();

        when(analyticsService.getOverview()).thenReturn(sampleOverview);
        when(analyticsService.getRevenueAnalytics(any())).thenReturn(sampleRevenue);
        when(analyticsService.getBookingAnalytics(any())).thenReturn(sampleBookings);
        when(analyticsService.getFlightAnalytics(any())).thenReturn(sampleFlights);
        when(analyticsService.getSeatAnalytics()).thenReturn(sampleSeats);
        when(analyticsService.getPaymentAnalytics(any())).thenReturn(samplePayments);
        when(analyticsService.getCustomerAnalytics(any())).thenReturn(sampleCustomers);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UNAUTHENTICATED → 401
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Unauthenticated request to /overview returns 401")
    void unauthenticated_overview_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/overview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated request to /revenue returns 401")
    void unauthenticated_revenue_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/revenue"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated request to /bookings returns 401")
    void unauthenticated_bookings_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/bookings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated request to /flights returns 401")
    void unauthenticated_flights_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/flights"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated request to /seats returns 401")
    void unauthenticated_seats_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/seats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated request to /payments returns 401")
    void unauthenticated_payments_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/payments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated request to /customers returns 401")
    void unauthenticated_customers_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/customers"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CUSTOMER (ROLE_CUSTOMER) → 403
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Customer accessing /overview returns 403")
    @WithMockUser(roles = "CUSTOMER")
    void customer_overview_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/overview"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer accessing /revenue returns 403")
    @WithMockUser(roles = "CUSTOMER")
    void customer_revenue_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/revenue"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer accessing /bookings returns 403")
    @WithMockUser(roles = "CUSTOMER")
    void customer_bookings_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/bookings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer accessing /flights returns 403")
    @WithMockUser(roles = "CUSTOMER")
    void customer_flights_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/flights"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer accessing /seats returns 403")
    @WithMockUser(roles = "CUSTOMER")
    void customer_seats_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/seats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer accessing /payments returns 403")
    @WithMockUser(roles = "CUSTOMER")
    void customer_payments_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/payments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer accessing /customers returns 403")
    @WithMockUser(roles = "CUSTOMER")
    void customer_customers_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/customers"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN (ROLE_ADMIN) → 200
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Admin accessing /overview returns 200 with data")
    @WithMockUser(roles = "ADMIN")
    void admin_overview_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalBookings").value(100))
                .andExpect(jsonPath("$.data.confirmedBookings").value(80));
    }

    @Test
    @DisplayName("Admin accessing /revenue returns 200 with trend data")
    @WithMockUser(roles = "ADMIN")
    void admin_revenue_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/revenue")
                        .param("period", "last30days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.grossRevenue").value(100000));
    }

    @Test
    @DisplayName("Admin accessing /bookings returns 200")
    @WithMockUser(roles = "ADMIN")
    void admin_bookings_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/bookings")
                        .param("period", "last7days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalBookings").value(50));
    }

    @Test
    @DisplayName("Admin accessing /flights returns 200")
    @WithMockUser(roles = "ADMIN")
    void admin_flights_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/flights")
                        .param("period", "thisMonth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalFlights").value(20));
    }

    @Test
    @DisplayName("Admin accessing /seats returns 200")
    @WithMockUser(roles = "ADMIN")
    void admin_seats_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSeats").value(2000));
    }

    @Test
    @DisplayName("Admin accessing /payments returns 200")
    @WithMockUser(roles = "ADMIN")
    void admin_payments_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/payments")
                        .param("period", "today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentSuccessRate").value(85));
    }

    @Test
    @DisplayName("Admin accessing /customers returns 200 without PII")
    @WithMockUser(roles = "ADMIN")
    void admin_customers_returns200_withoutPII() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/customers")
                        .param("period", "last30days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCustomers").value(500))
                // Confirm no email/phone fields in response
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.phone").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Invalid period parameter returns 400")
    @WithMockUser(roles = "ADMIN")
    void invalidPeriod_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/revenue")
                        .param("period", "invalidPeriod"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Admin accessing /revenue with yesterday period returns 200")
    @WithMockUser(roles = "ADMIN")
    void admin_revenue_yesterday_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/revenue")
                        .param("period", "yesterday"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin accessing /revenue with lastMonth period returns 200")
    @WithMockUser(roles = "ADMIN")
    void admin_revenue_lastMonth_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/revenue")
                        .param("period", "lastMonth"))
                .andExpect(status().isOk());
    }
}
