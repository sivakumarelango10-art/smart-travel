package com.smarttravel.modules.payment.webhook;

import com.smarttravel.modules.payment.config.RazorpayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class RazorpayWebhookVerifierTest {

    private RazorpayWebhookVerifier verifier;
    private RazorpayProperties properties;

    private static final String WEBHOOK_SECRET = "whsec_test_secret_1234567890";
    private static final String SAMPLE_PAYLOAD = "{\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_123\",\"amount\":575000}}}}";

    @BeforeEach
    void setUp() {
        properties = new RazorpayProperties(true, "rzp_key", "rzp_secret", WEBHOOK_SECRET, "INR");
        verifier = new RazorpayWebhookVerifier(properties);
    }

    private String computeHmacSha256(byte[] data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(data));
    }

    @Test
    @DisplayName("Valid HMAC-SHA256 signature against raw payload returns true")
    void testVerifyWebhookSignature_Valid() throws Exception {
        byte[] payloadBytes = SAMPLE_PAYLOAD.getBytes(StandardCharsets.UTF_8);
        String validSignature = computeHmacSha256(payloadBytes, WEBHOOK_SECRET);

        boolean result = verifier.verifyWebhookSignature(payloadBytes, validSignature);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Tampered/modified payload bytes return false")
    void testVerifyWebhookSignature_ModifiedPayload() throws Exception {
        byte[] originalBytes = SAMPLE_PAYLOAD.getBytes(StandardCharsets.UTF_8);
        String validSignature = computeHmacSha256(originalBytes, WEBHOOK_SECRET);

        byte[] tamperedBytes = (SAMPLE_PAYLOAD + " ").getBytes(StandardCharsets.UTF_8);
        boolean result = verifier.verifyWebhookSignature(tamperedBytes, validSignature);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Wrong signature returns false")
    void testVerifyWebhookSignature_WrongSignature() {
        byte[] payloadBytes = SAMPLE_PAYLOAD.getBytes(StandardCharsets.UTF_8);
        String badSignature = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

        boolean result = verifier.verifyWebhookSignature(payloadBytes, badSignature);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Empty or null parameters return false without throwing exceptions")
    void testVerifyWebhookSignature_NullOrEmpty() {
        byte[] payloadBytes = SAMPLE_PAYLOAD.getBytes(StandardCharsets.UTF_8);

        assertThat(verifier.verifyWebhookSignature(null, "some_sig")).isFalse();
        assertThat(verifier.verifyWebhookSignature(new byte[0], "some_sig")).isFalse();
        assertThat(verifier.verifyWebhookSignature(payloadBytes, null)).isFalse();
        assertThat(verifier.verifyWebhookSignature(payloadBytes, "")).isFalse();
        assertThat(verifier.verifyWebhookSignature(payloadBytes, "   ")).isFalse();
    }

    @Test
    @DisplayName("Missing secret configuration returns false safely")
    void testVerifyWebhookSignature_MissingSecret() {
        properties.setWebhookSecret("");
        properties.setKeySecret("");
        byte[] payloadBytes = SAMPLE_PAYLOAD.getBytes(StandardCharsets.UTF_8);

        assertThat(verifier.verifyWebhookSignature(payloadBytes, "sig")).isFalse();
    }

    @Test
    @DisplayName("Payload hash computation calculates correct SHA-256")
    void testCalculatePayloadHash() {
        byte[] payloadBytes = SAMPLE_PAYLOAD.getBytes(StandardCharsets.UTF_8);
        String hash = verifier.calculatePayloadHash(payloadBytes);

        assertThat(hash).isNotBlank();
        assertThat(hash).hasSize(64);
        assertThat(verifier.calculatePayloadHash(null)).isEmpty();
        assertThat(verifier.calculatePayloadHash(new byte[0])).isEmpty();
    }
}
