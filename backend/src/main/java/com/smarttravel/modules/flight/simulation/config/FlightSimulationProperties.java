package com.smarttravel.modules.flight.simulation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for mock real-time flight status simulation.
 */
@Component
@ConfigurationProperties(prefix = "smarttravel.flight.simulation")
public class FlightSimulationProperties {

    private boolean enabled = false;
    private long intervalMs = 5000L;
    private int defaultSpeed = 60;
    private double delayProbability = 0.25;
    private int minDelayMinutes = 15;
    private int maxDelayMinutes = 120;

    public FlightSimulationProperties() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public int getDefaultSpeed() {
        return defaultSpeed;
    }

    public void setDefaultSpeed(int defaultSpeed) {
        this.defaultSpeed = defaultSpeed;
    }

    public double getDelayProbability() {
        return delayProbability;
    }

    public void setDelayProbability(double delayProbability) {
        this.delayProbability = delayProbability;
    }

    public int getMinDelayMinutes() {
        return minDelayMinutes;
    }

    public void setMinDelayMinutes(int minDelayMinutes) {
        this.minDelayMinutes = minDelayMinutes;
    }

    public int getMaxDelayMinutes() {
        return maxDelayMinutes;
    }

    public void setMaxDelayMinutes(int maxDelayMinutes) {
        this.maxDelayMinutes = maxDelayMinutes;
    }
}
