package com.smarttravel.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "dGhpcy1pcy1hLXNhbXBsZS01MTItYml0LXNlY3JldC1rZXktZm9yLXVzZS13aXRoLWpqd3Qtc21hcnR0cmF2ZWwtYXBwbGljYXRpb24tZGV2ZWxvcG1lbnQtdGVzdGluZw==";
    private static final long TEST_EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Generate and validate valid JWT token with claims")
    void testGenerateAndValidateToken() {
        String userId = "user_123456";
        String email = "traveler@smarttravel.com";
        List<String> roles = List.of("ROLE_USER");

        String token = jwtTokenProvider.generateTokenFromUserIdAndEmail(userId, email, roles);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        assertEquals(TEST_EXPIRATION, jwtTokenProvider.getJwtExpirationMs());
    }

    @Test
    @DisplayName("Expired JWT token should fail validation")
    void testExpiredToken() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredProvider, "jwtExpirationMs", -1000L); // Expired 1 second ago

        String expiredToken = expiredProvider.generateTokenFromUserIdAndEmail(
                "user_expired", "expired@smarttravel.com", List.of("ROLE_USER")
        );

        assertFalse(expiredProvider.validateToken(expiredToken));
    }

    @Test
    @DisplayName("Invalid or tampered token should fail validation")
    void testInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzUxMiJ9.tamperedPayload.invalidSignature";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Empty or null token should fail validation")
    void testEmptyToken() {
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken(null));
    }
}
