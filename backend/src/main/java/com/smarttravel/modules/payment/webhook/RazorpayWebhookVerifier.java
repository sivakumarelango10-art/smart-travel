package com.smarttravel.modules.payment.webhook;

import com.smarttravel.modules.payment.config.RazorpayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Dedicated cryptographic verifier for Razorpay Webhook HMAC-SHA256 signatures.
 * Validates signatures against raw payload bytes using constant-time comparison.
 */
@Component
public class RazorpayWebhookVerifier {

    private static final Logger log = LoggerFactory.getLogger(RazorpayWebhookVerifier.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final RazorpayProperties razorpayProperties;

    public RazorpayWebhookVerifier(RazorpayProperties razorpayProperties) {
        this.razorpayProperties = razorpayProperties;
    }

    /**
     * Verifies the authenticity of an incoming Razorpay webhook request.
     *
     * @param rawPayloadBytes Raw HTTP request body bytes as received by the server
     * @param signatureHeader Value of the X-Razorpay-Signature HTTP header
     * @return true if the signature matches the calculated HMAC-SHA256 hash, false otherwise
     */
    public boolean verifyWebhookSignature(byte[] rawPayloadBytes, String signatureHeader) {
        if (rawPayloadBytes == null || rawPayloadBytes.length == 0) {
            log.warn("Webhook signature verification failed: empty payload bytes received");
            return false;
        }

        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("Webhook signature verification failed: missing or empty signature header");
            return false;
        }

        String secret = resolveWebhookSecret();
        if (secret == null || secret.isBlank()) {
            log.warn("Webhook signature verification failed: no webhook secret configured");
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);

            byte[] hashBytes = mac.doFinal(rawPayloadBytes);
            String expectedSignature = HexFormat.of().formatHex(hashBytes);

            String actualSignature = signatureHeader.trim().toLowerCase();
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    actualSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Cryptographic error occurred during webhook signature verification", e);
            return false;
        }
    }

    /**
     * Helper to compute a SHA-256 hash of raw payload bytes for audit tracking.
     */
    public String calculatePayloadHash(byte[] rawPayloadBytes) {
        if (rawPayloadBytes == null || rawPayloadBytes.length == 0) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPayloadBytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error computing payload SHA-256 hash", e);
            return "";
        }
    }

    private String resolveWebhookSecret() {
        if (razorpayProperties.getWebhookSecret() != null && !razorpayProperties.getWebhookSecret().isBlank()) {
            return razorpayProperties.getWebhookSecret();
        }
        if (razorpayProperties.getKeySecret() != null && !razorpayProperties.getKeySecret().isBlank()) {
            log.warn("RAZORPAY_WEBHOOK_SECRET is not configured; falling back to RAZORPAY_KEY_SECRET for webhook verification.");
            return razorpayProperties.getKeySecret();
        }
        // In dev / test environments when Razorpay live gateway is disabled (mock mode), permit test signature validation
        if (!razorpayProperties.isEnabled()) {
            return "smarttravel_dev_secret_key";
        }
        log.error("Neither RAZORPAY_WEBHOOK_SECRET nor RAZORPAY_KEY_SECRET is configured in live production mode. Webhook verification will fail.");
        return null;
    }
}
