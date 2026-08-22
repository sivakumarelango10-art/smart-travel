package com.smarttravel.modules.flight.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Server-side configuration properties for Aviationstack API integration.
 * The API key is securely loaded from environment variables (AVIATIONSTACK_API_KEY)
 * and is never exposed in client payloads, logs, or git.
 */
@Component
@ConfigurationProperties(prefix = "smarttravel.flight.aviationstack")
public class AviationstackProperties {

    /**
     * Aviationstack Access Key (server-side secret).
     */
    private String apiKey = "";

    /**
     * Base URL for the Aviationstack API.
     */
    private String baseUrl = "https://api.aviationstack.com";

    /**
     * Active provider mode: "MOCK" or "AVIATIONSTACK".
     */
    private String provider = "MOCK";

    /**
     * Flight search response caching TTL in seconds.
     * Default: 180 seconds (3 minutes) to protect the monthly free tier budget.
     */
    private int searchCacheSeconds = 180;

    /**
     * Single flight live status response caching TTL in seconds.
     * Default: 60 seconds (1 minute).
     */
    private int flightCacheSeconds = 60;

    /**
     * Monthly request quota guard to prevent quota exhaustion.
     * Default: 100 requests per calendar month.
     */
    private int monthlyRequestLimit = 100;

    /**
     * HTTP connect timeout in milliseconds.
     */
    private int connectTimeoutMs = 5000;

    /**
     * HTTP read timeout in milliseconds.
     */
    private int readTimeoutMs = 5000;

    /**
     * Enable/disable external live calls. If false, falls back to mock/cached data.
     */
    private boolean enabled = true;

    public AviationstackProperties() {
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl.trim() : "https://api.aviationstack.com";
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider != null ? provider.trim().toUpperCase() : "MOCK";
    }

    public int getSearchCacheSeconds() {
        return searchCacheSeconds;
    }

    public void setSearchCacheSeconds(int searchCacheSeconds) {
        this.searchCacheSeconds = Math.max(5, searchCacheSeconds);
    }

    public int getFlightCacheSeconds() {
        return flightCacheSeconds;
    }

    public void setFlightCacheSeconds(int flightCacheSeconds) {
        this.flightCacheSeconds = Math.max(5, flightCacheSeconds);
    }

    public int getMonthlyRequestLimit() {
        return monthlyRequestLimit;
    }

    public void setMonthlyRequestLimit(int monthlyRequestLimit) {
        this.monthlyRequestLimit = Math.max(1, monthlyRequestLimit);
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAviationstackMode() {
        return apiKey != null && !apiKey.isBlank() && !"MOCK".equalsIgnoreCase(provider);
    }
}
