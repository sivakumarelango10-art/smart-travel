package com.smarttravel.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.modules.auth.controller.AuthController;
import com.smarttravel.modules.auth.dto.UserResponse;
import com.smarttravel.modules.auth.service.AuthService;
import com.smarttravel.modules.flight.controller.AdminFlightController;
import com.smarttravel.modules.flight.controller.FlightController;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.health.controller.HealthController;
import com.smarttravel.modules.user.model.AccountStatus;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@Import({
        SecurityConfig.class,
        RequestIdFilter.class,
        JwtAuthenticationFilter.class,
        SecurityAccessTest.ProtectedSampleController.class,
        HealthController.class,
        AuthController.class,
        FlightController.class,
        AdminFlightController.class
})
class SecurityAccessTest {

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
    private com.smarttravel.modules.flight.simulation.service.FlightSimulationService flightSimulationService;

    @MockBean
    private com.smarttravel.modules.booking.service.BookingService bookingService;

    @MockBean
    private com.smarttravel.modules.payment.service.PaymentService paymentService;

    @MockBean
    private com.smarttravel.modules.payment.webhook.service.PaymentWebhookService paymentWebhookService;

    @MockBean
    private com.smarttravel.modules.ticket.service.TicketService ticketService;

    @MockBean
    private com.smarttravel.modules.flight.service.SeatMapService seatMapService;

    @MockBean
    private com.smarttravel.modules.booking.service.CheckInService checkInService;

    @MockBean
    private com.smarttravel.modules.flight.disruption.service.FlightDisruptionService flightDisruptionService;

    @MockBean
    private com.smarttravel.modules.flight.impact.service.FlightImpactService flightImpactService;

    @MockBean
    private com.smarttravel.modules.payment.refund.service.RefundService refundService;

    @MockBean
    private com.smarttravel.modules.payment.refund.service.RefundEligibilityService refundEligibilityService;

    @MockBean
    private com.smarttravel.modules.notification.service.NotificationService notificationService;

    @MockBean
    private com.smarttravel.modules.analytics.service.AnalyticsService analyticsService;

    @Autowired
    private ObjectMapper objectMapper;


    @RestController
    public static class ProtectedSampleController {

        @GetMapping("/api/v1/protected/user-data")
        public String getProtectedData() {
            return "Secret Data for Authenticated User";
        }

        @GetMapping("/api/v1/admin/dashboard")
        @PreAuthorize("hasRole('ADMIN')")
        public String getAdminDashboard() {
            return "Admin Dashboard Metrics";
        }
    }

    @Test
    @DisplayName("Public endpoint /api/health should be accessible without authentication")
    void testPublicHealthEndpoint() throws Exception {
        Document pingDoc = new Document("ok", 1.0);
        when(mongoTemplate.executeCommand(any(Document.class))).thenReturn(pingDoc);

        mockMvc.perform(get("/api/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Public endpoint /api/v1/flights should be accessible without authentication")
    void testPublicFlightSearch() throws Exception {
        mockMvc.perform(get("/api/v1/flights")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected endpoint without credentials should return 401 Unauthorized")
    void testProtectedEndpointUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/protected/user-data")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Admin mutation endpoint without credentials should return 401 Unauthorized")
    void testAdminFlightMutationUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/admin/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "traveler@smarttravel.com", roles = {"USER"})
    @DisplayName("USER role accessing ADMIN flight mutation should return 403 Forbidden")
    void testUserRoleForbiddenForAdminFlightMutation() throws Exception {
        mockMvc.perform(post("/api/v1/admin/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "traveler@smarttravel.com", roles = {"USER"})
    @DisplayName("Protected endpoint with valid user authentication should return 200 OK")
    void testProtectedEndpointAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/protected/user-data")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/api/auth/me without token should return 401 Unauthorized")
    void testAuthMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "traveler@smarttravel.com", roles = {"USER"})
    @DisplayName("/api/auth/me with valid authentication should return 200 OK")
    void testAuthMeWithToken() throws Exception {
        UserResponse response = UserResponse.builder()
                .id("usr-123")
                .fullName("Traveler Bob")
                .email("traveler@smarttravel.com")
                .roles(List.of("ROLE_USER"))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(authService.getCurrentUser()).thenReturn(response);

        mockMvc.perform(get("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("usr-123"));
    }

    @Test
    @WithMockUser(username = "traveler@smarttravel.com", roles = {"USER"})
    @DisplayName("USER role accessing ADMIN endpoint should return 403 Forbidden")
    void testUserRoleForbiddenForAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "admin@smarttravel.com", roles = {"ADMIN"})
    @DisplayName("ADMIN role accessing ADMIN endpoint should return 200 OK")
    void testAdminRoleAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
