package com.smarttravel.modules.booking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for booking lifecycle and payment timeout policies.
 */
@Configuration
@ConfigurationProperties(prefix = "smarttravel.booking")
public class BookingProperties {

    private int paymentTimeoutMinutes = 15;
    private Expiration expiration = new Expiration();

    public BookingProperties() {
    }

    public BookingProperties(int paymentTimeoutMinutes, Expiration expiration) {
        this.paymentTimeoutMinutes = paymentTimeoutMinutes;
        this.expiration = expiration != null ? expiration : new Expiration();
    }

    public int getPaymentTimeoutMinutes() {
        return paymentTimeoutMinutes;
    }

    public void setPaymentTimeoutMinutes(int paymentTimeoutMinutes) {
        this.paymentTimeoutMinutes = paymentTimeoutMinutes;
    }

    public Expiration getExpiration() {
        return expiration;
    }

    public void setExpiration(Expiration expiration) {
        this.expiration = expiration;
    }

    public static class Expiration {
        private boolean enabled = false;
        private long fixedDelayMs = 60000L;

        public Expiration() {
        }

        public Expiration(boolean enabled, long fixedDelayMs) {
            this.enabled = enabled;
            this.fixedDelayMs = fixedDelayMs;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getFixedDelayMs() {
            return fixedDelayMs;
        }

        public void setFixedDelayMs(long fixedDelayMs) {
            this.fixedDelayMs = fixedDelayMs;
        }
    }
}
