package com.smarttravel.modules.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.UnauthorizedException;
import com.smarttravel.modules.auth.dto.GoogleTokenPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleTokenVerifierTest {

    private GoogleTokenVerifierImpl verifier;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        verifier = new GoogleTokenVerifierImpl(objectMapper);
        ReflectionTestUtils.setField(verifier, "configuredClientId", "test-google-client-id.apps.googleusercontent.com");
    }

    private String createMockJwt(Map<String, Object> claims) {
        try {
            String header = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    objectMapper.writeValueAsString(Map.of("alg", "RS256", "typ", "JWT")).getBytes(StandardCharsets.UTF_8)
            );
            String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    objectMapper.writeValueAsString(claims).getBytes(StandardCharsets.UTF_8)
            );
            String signature = Base64.getUrlEncoder().withoutPadding().encodeToString("mock-signature".getBytes(StandardCharsets.UTF_8));
            return header + "." + payload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("1. Valid Google credential with correct issuer, audience, subject and email passes verification")
    void testValidGoogleCredential() {
        long exp = Instant.now().plusSeconds(3600).getEpochSecond();
        String token = createMockJwt(Map.of(
                "iss", "https://accounts.google.com",
                "sub", "109876543210987654321",
                "email", "traveler.google@gmail.com",
                "email_verified", true,
                "name", "Rahul Google",
                "given_name", "Rahul",
                "family_name", "Google",
                "picture", "https://lh3.googleusercontent.com/a/photo.jpg",
                "aud", "test-google-client-id.apps.googleusercontent.com",
                "exp", exp
        ));

        GoogleTokenPayload payload = verifier.verify(token);

        assertNotNull(payload);
        assertEquals("109876543210987654321", payload.subject());
        assertEquals("traveler.google@gmail.com", payload.email());
        assertTrue(payload.emailVerified());
        assertEquals("Rahul Google", payload.name());
        assertEquals("https://lh3.googleusercontent.com/a/photo.jpg", payload.pictureUrl());
    }

    @Test
    @DisplayName("2. Missing / null credential throws UnauthorizedException")
    void testMissingCredential() {
        assertThrows(UnauthorizedException.class, () -> verifier.verify(null));
        assertThrows(UnauthorizedException.class, () -> verifier.verify("   "));
    }

    @Test
    @DisplayName("3. Expired Google token throws UnauthorizedException")
    void testExpiredGoogleToken() {
        long exp = Instant.now().minusSeconds(3600).getEpochSecond();
        String token = createMockJwt(Map.of(
                "iss", "https://accounts.google.com",
                "sub", "10987654321",
                "email", "expired@gmail.com",
                "aud", "test-google-client-id.apps.googleusercontent.com",
                "exp", exp
        ));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> verifier.verify(token));
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("4. Wrong audience throws UnauthorizedException")
    void testWrongAudience() {
        long exp = Instant.now().plusSeconds(3600).getEpochSecond();
        String token = createMockJwt(Map.of(
                "iss", "https://accounts.google.com",
                "sub", "10987654321",
                "email", "wrongaud@gmail.com",
                "aud", "malicious-app-client-id.apps.googleusercontent.com",
                "exp", exp
        ));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> verifier.verify(token));
        assertTrue(ex.getMessage().contains("audience mismatch"));
    }

    @Test
    @DisplayName("5. Wrong issuer throws UnauthorizedException")
    void testWrongIssuer() {
        long exp = Instant.now().plusSeconds(3600).getEpochSecond();
        String token = createMockJwt(Map.of(
                "iss", "https://evil-identity-provider.com",
                "sub", "10987654321",
                "email", "imposter@gmail.com",
                "aud", "test-google-client-id.apps.googleusercontent.com",
                "exp", exp
        ));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> verifier.verify(token));
        assertTrue(ex.getMessage().contains("issuer"));
    }

    @Test
    @DisplayName("6. Missing subject or email throws UnauthorizedException")
    void testMissingClaims() {
        long exp = Instant.now().plusSeconds(3600).getEpochSecond();
        String tokenNoSub = createMockJwt(Map.of(
                "iss", "https://accounts.google.com",
                "email", "nosub@gmail.com",
                "aud", "test-google-client-id.apps.googleusercontent.com",
                "exp", exp
        ));

        assertThrows(UnauthorizedException.class, () -> verifier.verify(tokenNoSub));
    }
}
