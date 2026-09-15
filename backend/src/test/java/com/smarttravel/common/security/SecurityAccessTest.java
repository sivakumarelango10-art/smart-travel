package com.smarttravel.common.security;


import com.smarttravel.modules.analytics.service.AnalyticsService;
import com.smarttravel.modules.auth.controller.AuthController;
import com.smarttravel.modules.auth.dto.UserResponse;
import com.smarttravel.modules.auth.service.AuthService;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.booking.service.CheckInService;
import com.smarttravel.modules.flight.controller.AdminFlightController;
import com.smarttravel.modules.flight.controller.FlightController;
import com.smarttravel.modules.flight.disruption.service.FlightDisruptionService;
import com.smarttravel.modules.flight.impact.service.FlightImpactService;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.flight.service.SeatMapService;
import com.smarttravel.modules.flight.simulation.service.FlightSimulationService;
import com.smarttravel.modules.flight.tracking.service.FlightTrackingService;
import com.smarttravel.modules.health.controller.HealthController;
import com.smarttravel.modules.hotel.controller.HotelController;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.RoomCategory;
import com.smarttravel.modules.hotel.model.RoomType;
import com.smarttravel.modules.hotel.service.HotelService;
import com.smarttravel.modules.notification.service.NotificationService;
import com.smarttravel.modules.payment.refund.service.RefundEligibilityService;
import com.smarttravel.modules.payment.refund.service.RefundService;
import com.smarttravel.modules.payment.service.PaymentService;
import com.smarttravel.modules.payment.webhook.service.PaymentWebhookService;
import com.smarttravel.modules.pricing.service.DynamicPricingService;
import com.smarttravel.modules.pricing.service.PriceFreezeService;
import com.smarttravel.modules.recommendation.service.RecommendationService;
import com.smarttravel.modules.review.service.ReviewService;
import com.smarttravel.modules.ticket.service.TicketService;
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

@WebMvcTest(
        controllers = {
                SecurityAccessTest.ProtectedSampleController.class,
                HealthController.class,
                AuthController.class,
                FlightController.class,
                AdminFlightController.class,
                HotelController.class
        },
        excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class }
)
@Import({
        SecurityConfig.class,
        RequestIdFilter.class,
        JwtAuthenticationFilter.class,
        SecurityAccessTest.ProtectedSampleController.class,
        HealthController.class,
        AuthController.class,
        FlightController.class,
        AdminFlightController.class,
        HotelController.class
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
    private AnalyticsService analyticsService;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private DynamicPricingService dynamicPricingService;

    @MockBean
    private PriceFreezeService priceFreezeService;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private RecommendationService recommendationService;

    @MockBean
    private FlightTrackingService flightTrackingService;



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

    @Test
    @DisplayName("Anonymous user can access GET /api/v1/hotels/htl-hyd-01 without authentication")
    void testAnonymousAccessToHotelDetails() throws Exception {
        Hotel sample = Hotel.builder()
                .id("htl-hyd-01")
                .name("Taj Falaknuma Palace")
                .nearestAirportCode("HYD")
                .active(true)
                .build();

        when(hotelService.getHotelById("htl-hyd-01")).thenReturn(sample);

        mockMvc.perform(get("/api/v1/hotels/htl-hyd-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("htl-hyd-01"))
                .andExpect(jsonPath("$.data.name").value("Taj Falaknuma Palace"));
    }

    @Test
    @DisplayName("Anonymous user can access GET /api/v1/hotels search without authentication")
    void testAnonymousAccessToHotelSearch() throws Exception {
        when(hotelService.searchHotels(any(), any(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/hotels")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Anonymous user can access GET /api/v1/hotels/htl-hyd-01/rooms without authentication")
    void testAnonymousAccessToHotelRooms() throws Exception {
        RoomType room = RoomType.builder()
                .id("rm-01")
                .name("Palace Room")
                .category(RoomCategory.DELUXE)
                .build();

        when(hotelService.getRoomTypes("htl-hyd-01")).thenReturn(List.of(room));

        mockMvc.perform(get("/api/v1/hotels/htl-hyd-01/rooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("rm-01"));
    }

    @Test
    @DisplayName("Anonymous user attempting POST /api/v1/hotels/htl-hyd-01/hold is rejected with 401 Unauthorized")
    void testAnonymousHoldRoomRejected() throws Exception {
        mockMvc.perform(post("/api/v1/hotels/htl-hyd-01/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomTypeId\":\"rm-01\",\"quantity\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
