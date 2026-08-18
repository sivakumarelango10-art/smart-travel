package com.smarttravel.modules.flight.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

@Schema(description = "Flight Simulation Start / Reconfiguration Payload")
public class SimulationStartRequest {

    @Schema(description = "Speed multiplier (e.g. 60 = 1 simulated hour in 1 real minute)", example = "60")
    @Min(value = 1, message = "Speed multiplier must be at least 1")
    private Integer speedMultiplier;

    @Schema(description = "Probability of encountering a flight delay (0.0 to 1.0)", example = "0.25")
    @DecimalMin(value = "0.0", message = "Delay probability must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Delay probability must be between 0.0 and 1.0")
    private Double delayProbability;

    @Schema(description = "Minimum simulated delay in minutes", example = "15")
    @Min(value = 1, message = "Minimum delay must be at least 1 minute")
    private Integer minDelayMinutes;

    @Schema(description = "Maximum simulated delay in minutes", example = "120")
    @Min(value = 1, message = "Maximum delay must be at least 1 minute")
    private Integer maxDelayMinutes;

    public SimulationStartRequest() {
    }

    public SimulationStartRequest(Integer speedMultiplier, Double delayProbability, Integer minDelayMinutes, Integer maxDelayMinutes) {
        this.speedMultiplier = speedMultiplier;
        this.delayProbability = delayProbability;
        this.minDelayMinutes = minDelayMinutes;
        this.maxDelayMinutes = maxDelayMinutes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer speedMultiplier;
        private Double delayProbability;
        private Integer minDelayMinutes;
        private Integer maxDelayMinutes;

        public Builder speedMultiplier(Integer speedMultiplier) {
            this.speedMultiplier = speedMultiplier;
            return this;
        }

        public Builder delayProbability(Double delayProbability) {
            this.delayProbability = delayProbability;
            return this;
        }

        public Builder minDelayMinutes(Integer minDelayMinutes) {
            this.minDelayMinutes = minDelayMinutes;
            return this;
        }

        public Builder maxDelayMinutes(Integer maxDelayMinutes) {
            this.maxDelayMinutes = maxDelayMinutes;
            return this;
        }

        public SimulationStartRequest build() {
            return new SimulationStartRequest(speedMultiplier, delayProbability, minDelayMinutes, maxDelayMinutes);
        }
    }

    public Integer getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(Integer speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public Double getDelayProbability() {
        return delayProbability;
    }

    public void setDelayProbability(Double delayProbability) {
        this.delayProbability = delayProbability;
    }

    public Integer getMinDelayMinutes() {
        return minDelayMinutes;
    }

    public void setMinDelayMinutes(Integer minDelayMinutes) {
        this.minDelayMinutes = minDelayMinutes;
    }

    public Integer getMaxDelayMinutes() {
        return maxDelayMinutes;
    }

    public void setMaxDelayMinutes(Integer maxDelayMinutes) {
        this.maxDelayMinutes = maxDelayMinutes;
    }
}
