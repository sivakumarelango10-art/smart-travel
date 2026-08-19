package com.smarttravel.modules.flight.disruption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "Flight Reschedule Request Payload")
public class FlightScheduleChangeRequest {

    @Schema(description = "New revised UTC departure time", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08-25T14:30:00Z")
    @NotNull(message = "New departure time is required")
    private Instant newDepartureTime;

    @Schema(description = "New revised UTC arrival time", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08-25T16:45:00Z")
    @NotNull(message = "New arrival time is required")
    private Instant newArrivalTime;

    @Schema(description = "Primary operational reason for schedule change", requiredMode = Schema.RequiredMode.REQUIRED, example = "Air Traffic Control slot revision")
    @NotBlank(message = "Reason is required")
    private String reason;

    @Schema(description = "Additional context or notes for passenger notification", example = "Runway maintenance at departure airport")
    private String description;

    public FlightScheduleChangeRequest() {
    }

    public FlightScheduleChangeRequest(Instant newDepartureTime, Instant newArrivalTime, String reason, String description) {
        this.newDepartureTime = newDepartureTime;
        this.newArrivalTime = newArrivalTime;
        this.reason = reason;
        this.description = description;
    }

    public Instant getNewDepartureTime() { return newDepartureTime; }
    public void setNewDepartureTime(Instant newDepartureTime) { this.newDepartureTime = newDepartureTime; }

    public Instant getNewArrivalTime() { return newArrivalTime; }
    public void setNewArrivalTime(Instant newArrivalTime) { this.newArrivalTime = newArrivalTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
