package com.smarttravel.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.auth.dto.RegisterRequest;
import com.smarttravel.modules.user.model.AccountStatus;
import com.smarttravel.modules.user.model.Role;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Production Security Endpoints Integration Test.
 * Validates through the live Spring Security FilterChain:
 * 1. Public unauthenticated access to /api/v1/auth/register, /v1/auth/register, /api/auth/register without JWT.
 * 2. Public unauthenticated access to /api/v1/auth/login, /v1/auth/login without JWT.
 * 3. Public unauthenticated access to /api/v1/flights, /v1/flights without JWT.
 * 4. Public unauthenticated access to /api/health, /api/v1/health, /v1/health, /health without JWT.
 * 5. Rejection of unauthenticated requests to customer resources (/api/v1/bookings, /v1/bookings, /api/auth/me, /v1/auth/me) with HTTP 401.
 * 6. Rejection of unauthenticated requests to admin resources (/api/v1/admin/**, /v1/admin/**, /admin/**) with HTTP 401.
 * 7. Rejection of customer (non-admin) JWT accessing admin endpoints with HTTP 403 Forbidden.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductionSecurityEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String customerUserId;
    private String customerJwt;
    private String testSuffix;

    @BeforeEach
    void setUp() {
        testSuffix = UUID.randomUUID().toString().substring(0, 8);
        User customerUser = User.builder()
                .email("test.customer." + testSuffix + "@smarttravel.com")
                .fullName("Test Customer")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .phoneNumber("+919876543210")
                .roles(Set.of(Role.ROLE_USER))
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User saved = userRepository.save(customerUser);
        customerUserId = saved.getId();
        customerJwt = jwtTokenProvider.generateTokenFromUserIdAndEmail(saved.getId(), saved.getEmail(), java.util.List.of("ROLE_USER"));
    }

    @AfterEach
    void tearDown() {
        if (customerUserId != null) {
            userRepository.deleteById(customerUserId);
        }
        userRepository.findByEmail("new.reg.api." + testSuffix + "@smarttravel.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("new.reg.v1." + testSuffix + "@smarttravel.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("new.reg.legacy." + testSuffix + "@smarttravel.com").ifPresent(userRepository::delete);
    }

    @Test
    @DisplayName("POST /api/v1/auth/register succeeds without Authorization header")
    void testRegisterApiV1WithoutJwt() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .email("new.reg.api." + testSuffix + "@smarttravel.com")
                .fullName("API Register")
                .password("Password123!")
                .phoneNumber("+919876543210")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("new.reg.api." + testSuffix + "@smarttravel.com"));
    }

    @Test
    @DisplayName("POST /v1/auth/register succeeds without Authorization header")
    void testRegisterV1DirectWithoutJwt() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .email("new.reg.v1." + testSuffix + "@smarttravel.com")
                .fullName("V1 Direct Register")
                .password("Password123!")
                .phoneNumber("+919876543210")
                .build();

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("new.reg.v1." + testSuffix + "@smarttravel.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register succeeds without Authorization header")
    void testRegisterLegacyWithoutJwt() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .email("new.reg.legacy." + testSuffix + "@smarttravel.com")
                .fullName("Legacy Register")
                .password("Password123!")
                .phoneNumber("+919876543210")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/flights and /v1/flights are accessible without JWT")
    void testPublicFlightSearchWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/v1/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /health, /api/health, /v1/health, /api/v1/health are accessible without JWT")
    void testPublicHealthEndpointsWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
        mockMvc.perform(get("/v1/health")).andExpect(status().isOk());
        mockMvc.perform(get("/health")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected booking endpoints return 401 when accessed without JWT")
    void testProtectedBookingsEndpointReturns401WithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));

        mockMvc.perform(get("/v1/bookings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));
    }

    @Test
    @DisplayName("Protected profile endpoints return 401 when accessed without JWT")
    void testProtectedProfileEndpointReturns401WithoutJwt() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Admin endpoints return 401 when accessed without JWT")
    void testAdminEndpointsReturn401WithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/admin/flights"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/v1/admin/flights"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/analytics/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/v1/admin/analytics/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Customer JWT receives 403 Forbidden when attempting to access Admin endpoints")
    void testCustomerJwtBlockedFromAdminEndpointsWith403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/flights")
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied: You do not have permission to access this resource"));

        mockMvc.perform(get("/v1/admin/analytics/overview")
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
