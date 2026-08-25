package com.smarttravel.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.common.exception.UnauthorizedException;
import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.auth.dto.AuthResponse;
import com.smarttravel.modules.auth.dto.GoogleLoginRequest;
import com.smarttravel.modules.auth.dto.UserSummaryDto;
import com.smarttravel.modules.auth.service.AuthService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GoogleAuthControllerTest {

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
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("12. POST /v1/auth/google with valid credential returns 200 OK and AuthResponse")
    void testGoogleLoginSuccess() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest("mock-valid-google-credential", true);

        UserSummaryDto userSummary = UserSummaryDto.builder()
                .id("usr_google_123")
                .email("google.traveler@gmail.com")
                .fullName("Google Traveler")
                .roles(List.of("ROLE_USER"))
                .accountStatus("ACTIVE")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("mock-jwt-access-token-12345")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userSummary)
                .build();

        when(authService.authenticateWithGoogle(any(GoogleLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-access-token-12345"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("google.traveler@gmail.com"));
    }

    @Test
    @DisplayName("13. POST /v1/auth/google with blank credential returns 400 Bad Request")
    void testGoogleLoginValidationFailure() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest("", true);

        mockMvc.perform(post("/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("14. POST /v1/auth/google with invalid Google ID token returns 401 Unauthorized")
    void testGoogleLoginUnauthorized() throws Exception {
        GoogleLoginRequest request = new GoogleLoginRequest("invalid-id-token", true);

        when(authService.authenticateWithGoogle(any(GoogleLoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid Google token signature."));

        mockMvc.perform(post("/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Google token signature."));
    }
}
