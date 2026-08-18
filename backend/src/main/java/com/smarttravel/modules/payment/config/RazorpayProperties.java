package com.smarttravel.modules.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Razorpay payment integration.
 */
@Configuration
@ConfigurationProperties(prefix = "smarttravel.payment.razorpay")
public class RazorpayProperties {

    private boolean enabled = false;
    private String keyId = "";
    private String keySecret = "";
    private String webhookSecret = "";
    private String currency = "INR";

    public RazorpayProperties() {
    }

    public RazorpayProperties(boolean enabled, String keyId, String keySecret, String webhookSecret, String currency) {
        this.enabled = enabled;
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;
        this.currency = currency != null ? currency : "INR";
    }

    public RazorpayProperties(boolean enabled, String keyId, String keySecret, String currency) {
        this(enabled, keyId, keySecret, "", currency);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getKeySecret() {
        return keySecret;
    }

    public void setKeySecret(String keySecret) {
        this.keySecret = keySecret;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
