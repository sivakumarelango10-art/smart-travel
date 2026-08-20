package com.smarttravel.common.security;

import com.smarttravel.modules.auth.controller.AuthController;
import com.smarttravel.modules.auth.service.AuthService;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.booking.service.CheckInService;
import com.smarttravel.modules.flight.controller.FlightController;
import com.smarttravel.modules.flight.disruption.service.FlightDisruptionService;
import com.smarttravel.modules.flight.impact.service.FlightImpactService;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.flight.service.SeatMapService;
import com.smarttravel.modules.flight.simulation.service.FlightSimulationService;
import com.smarttravel.modules.health.controller.HealthController;
import com.smarttravel.modules.notification.service.NotificationService;
import com.smarttravel.modules.payment.refund.service.RefundEligibilityService;
import com.smarttravel.modules.payment.refund.service.RefundService;
import com.smarttravel.modules.payment.service.PaymentService;
import com.smarttravel.modules.payment.webhook.service.PaymentWebhookService;
import com.smarttravel.modules.ticket.service.TicketService;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {
                HealthController.class,
                AuthController.class,
                FlightController.class
        },
        excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class }
)
@Import({
        SecurityConfig.class,
        RequestIdFilter.class,
        JwtAuthenticationFilter.class,
        HealthController.class,
        AuthController.class,
        FlightController.class
})
@AutoConfigureMockMvc
class ProductionHardeningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private AuthService authService;

    @MockBean
    private FlightService flightService;

    @MockBean
    private FlightSimulationService flightSimulationService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentWebhookService paymentWebhookService;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private SeatMapService seatMapService;

    @MockBean
    private CheckInService checkInService;

    @MockBean
    private FlightDisruptionService flightDisruptionService;

    @MockBean
    private FlightImpactService flightImpactService;

    @MockBean
    private RefundService refundService;

    @MockBean
    private RefundEligibilityService refundEligibilityService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private com.smarttravel.modules.analytics.service.AnalyticsService analyticsService;

    @Test
    @DisplayName("Should include X-Request-ID and security headers in response")
    void shouldIncludeRequestIdAndSecurityHeaders() throws Exception {
        when(mongoTemplate.executeCommand(any(Document.class))).thenReturn(new Document("ok", 1.0));

        mockMvc.perform(get("/api/health")
                        .header("X-Request-ID", "trace-live-test-101"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-ID", "trace-live-test-101"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.database").value("CONNECTED"));
    }

    @Test
    @DisplayName("Should generate X-Request-ID when not supplied by client")
    void shouldGenerateRequestIdWhenMissing() throws Exception {
        when(mongoTemplate.executeCommand(any(Document.class))).thenReturn(new Document("ok", 1.0));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(header().string("X-Request-ID", notNullValue()));
    }

    @Test
    @DisplayName("Should return DEGRADED status in body and report DISCONNECTED when MongoDB ping fails")
    void shouldReturnDegradedWhenDatabaseDisconnected() throws Exception {
        when(mongoTemplate.executeCommand(any(Document.class)))
                .thenThrow(new RuntimeException("MongoDB ping failed"));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.database").value("DISCONNECTED"));
    }

    @Test
    @DisplayName("Should include requestId in unauthorized error responses")
    void shouldIncludeRequestIdInErrorResponse() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("X-Request-ID", "error-test-trace-999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Request-ID", "error-test-trace-999"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.requestId").value("error-test-trace-999"));
    }
}
