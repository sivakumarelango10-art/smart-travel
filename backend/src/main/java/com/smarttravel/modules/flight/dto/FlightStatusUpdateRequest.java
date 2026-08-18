package com.smarttravel.modules.flight.dto;

import com.smarttravel.modules.flight.model.FlightStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "Admin Flight Status Update Payload")
public class FlightStatusUpdateRequest {

    @Schema(description = "New flight status", example = "DELAYED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Flight status is required")
    private FlightStatus status;

    @Schema(description = "Delay duration in minutes (required when status is DELAYED, must be non-negative)", example = "45")
    @Min(value = 0, message = "Delay minutes must be non-negative")
    private Integer delayMinutes;

    @Schema(description = "Reason for delay (required when status is DELAYED)", example = "Weather conditions at destination")
    private String delayReason;

    @Schema(description = "Revised UTC departure timestamp (optional, calculated automatically if omitted)", example = "2026-08-20T19:15:00Z")
    private Instant revisedDepartureTime;

    @Schema(description = "Estimated UTC arrival timestamp (optional, calculated automatically if omitted)", example = "2026-08-20T21:30:00Z")
    private Instant estimatedArrival;

    public FlightStatusUpdateRequest() {
    }

    public FlightStatusUpdateRequest(FlightStatus status) {
        this.status = status;
    }

    public FlightStatusUpdateRequest(FlightStatus status, Integer delayMinutes, String delayReason,
                                     Instant revisedDepartureTime, Instant estimatedArrival) {
        this.status = status;
        this.delayMinutes = delayMinutes;
        this.delayReason = delayReason;
        this.revisedDepartureTime = revisedDepartureTime;
        this.estimatedArrival = estimatedArrival;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private FlightStatus status;
        private Integer delayMinutes;
        private String delayReason;
        private Instant revisedDepartureTime;
        private Instant estimatedArrival;

        public Builder status(FlightStatus status) {
            this.status = status;
            return this;
        }

        public Builder delayMinutes(Integer delayMinutes) {
            this.delayMinutes = delayMinutes;
            return this;
        }

        public Builder delayReason(String delayReason) {
            this.delayReason = delayReason;
            return this;
        }

        public Builder revisedDepartureTime(Instant revisedDepartureTime) {
            this.revisedDepartureTime = revisedDepartureTime;
            return this;
        }

        public Builder estimatedArrival(Instant estimatedArrival) {
            this.estimatedArrival = estimatedArrival;
            return this;
        }

        public FlightStatusUpdateRequest build() {
            return new FlightStatusUpdateRequest(status, delayMinutes, delayReason, revisedDepartureTime, estimatedArrival);
        }
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public Integer getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public String getDelayReason() {
        return delayReason;
    }

    public void setDelayReason(String delayReason) {
        this.delayReason = delayReason;
    }

    public Instant getRevisedDepartureTime() {
        return revisedDepartureTime;
    }

    public void setRevisedDepartureTime(Instant revisedDepartureTime) {
        this.revisedDepartureTime = revisedDepartureTime;
    }

    public Instant getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(Instant estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }
}
