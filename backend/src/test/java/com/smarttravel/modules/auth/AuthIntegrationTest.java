package com.smarttravel.modules.auth;

import com.smarttravel.SmartTravelApplication;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.auth.dto.AuthResponse;
import com.smarttravel.modules.auth.dto.LoginRequest;
import com.smarttravel.modules.auth.dto.RegisterRequest;
import com.smarttravel.modules.auth.dto.UserResponse;
import com.smarttravel.modules.auth.service.AuthService;
import com.smarttravel.modules.user.model.AccountStatus;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuthIntegrationTest {

    static {
        SmartTravelApplication.loadDotenv();
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_EMAIL = "integration.tester@smarttravel.com";

    @BeforeEach
    @AfterEach
    void cleanup() {
        userRepository.findByNormalizedEmail(TEST_EMAIL).ifPresent(userRepository::delete);
    }

    @Test
    @DisplayName("20. End-to-end integration: Register, verify MongoDB persistence with hashed password, and login with JWT generation")
    void testEndToEndAuthFlow() {
        RegisterRequest registerReq = RegisterRequest.builder()
                .fullName("Integration Tester")
                .email(TEST_EMAIL)
                .phoneNumber("+919876543299")
                .password("Travel2026!Secure")
                .build();

        UserResponse registered = authService.register(registerReq);
        assertNotNull(registered.getId());
        assertEquals("Integration Tester", registered.getFullName());
        assertEquals(TEST_EMAIL, registered.getEmail());
        assertEquals(AccountStatus.ACTIVE, registered.getAccountStatus());

        // Verify document persisted in MongoDB
        Optional<User> savedUserOpt = userRepository.findByNormalizedEmail(TEST_EMAIL);
        assertTrue(savedUserOpt.isPresent());
        User savedUser = savedUserOpt.get();
        assertEquals("Integration Tester", savedUser.getFullName());
        assertEquals(TEST_EMAIL, savedUser.getNormalizedEmail());
        assertFalse(savedUser.getPasswordHash().contains("Travel2026!Secure"));
        assertTrue(passwordEncoder.matches("Travel2026!Secure", savedUser.getPasswordHash()));

        // Login with uppercase/mixed email to verify case-insensitive normalization
        LoginRequest loginReq = LoginRequest.builder()
                .email("INTEGRATION.TESTER@SmartTravel.com")
                .password("Travel2026!Secure")
                .build();

        AuthResponse authResp = authService.login(loginReq);
        assertNotNull(authResp.getAccessToken());
        assertEquals("Bearer", authResp.getTokenType());
        assertTrue(jwtTokenProvider.validateToken(authResp.getAccessToken()));
        assertEquals(savedUser.getId(), jwtTokenProvider.getUserIdFromToken(authResp.getAccessToken()));
        assertEquals(savedUser.getEmail(), jwtTokenProvider.getEmailFromToken(authResp.getAccessToken()));
    }
}
