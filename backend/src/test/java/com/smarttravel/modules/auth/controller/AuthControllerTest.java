package com.smarttravel.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.DuplicateResourceException;
import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.common.exception.UnauthorizedException;
import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.auth.dto.AuthResponse;
import com.smarttravel.modules.auth.dto.LoginRequest;
import com.smarttravel.modules.auth.dto.RegisterRequest;
import com.smarttravel.modules.auth.dto.UserResponse;
import com.smarttravel.modules.auth.dto.UserSummaryDto;
import com.smarttravel.modules.auth.service.AuthService;
import com.smarttravel.modules.user.model.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/auth/register should create user and return 201 Created")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Traveler")
                .email("john.traveler@smarttravel.com")
                .phoneNumber("+919876543210")
                .password("Travel2026!Secure")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id("usr_998877")
                .fullName("John Traveler")
                .email("john.traveler@smarttravel.com")
                .phoneNumber("+919876543210")
                .roles(List.of("ROLE_USER"))
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.id").value("usr_998877"))
                .andExpect(jsonPath("$.data.fullName").value("John Traveler"))
                .andExpect(jsonPath("$.data.email").value("john.traveler@smarttravel.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register with invalid email should return 400 Bad Request")
    void testRegisterInvalidEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Traveler")
                .email("invalid-email-format")
                .password("Travel2026!Secure")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("POST /api/auth/register with duplicate email should return 409 Conflict")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Traveler")
                .email("duplicate@smarttravel.com")
                .password("Travel2026!Secure")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("User", "email", "duplicate@smarttravel.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User already exists with email: 'duplicate@smarttravel.com'"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 200 OK with JWT token and user summary")
    void testLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("john.traveler@smarttravel.com")
                .password("Travel2026!Secure")
                .build();

        UserSummaryDto summary = UserSummaryDto.builder()
                .id("usr_998877")
                .fullName("John Traveler")
                .email("john.traveler@smarttravel.com")
                .roles(List.of("ROLE_USER"))
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("eyJhbGciOiJIUzUxMiJ9.sampleJwtToken")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(summary)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("eyJhbGciOiJIUzUxMiJ9.sampleJwtToken"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(86400000L))
                .andExpect(jsonPath("$.data.user.id").value("usr_998877"))
                .andExpect(jsonPath("$.data.user.fullName").value("John Traveler"))
                .andExpect(jsonPath("$.data.user.email").value("john.traveler@smarttravel.com"))
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login with wrong credentials should return 401 Unauthorized")
    void testLoginUnauthorized() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("john.traveler@smarttravel.com")
                .password("WrongPassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("GET /api/auth/me should return current user profile")
    void testGetCurrentUser() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id("usr_998877")
                .fullName("John Traveler")
                .email("john.traveler@smarttravel.com")
                .phoneNumber("+919876543210")
                .roles(List.of("ROLE_USER"))
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();

        when(authService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("usr_998877"))
                .andExpect(jsonPath("$.data.fullName").value("John Traveler"))
                .andExpect(jsonPath("$.data.email").value("john.traveler@smarttravel.com"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }
}
