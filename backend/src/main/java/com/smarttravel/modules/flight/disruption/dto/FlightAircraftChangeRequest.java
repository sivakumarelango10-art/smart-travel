package com.smarttravel.modules.flight.disruption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Flight Aircraft Swap Request Payload")
public class FlightAircraftChangeRequest {

    @Schema(description = "New aircraft model", requiredMode = Schema.RequiredMode.REQUIRED, example = "Airbus A320neo")
    @NotBlank(message = "New aircraft model is required")
    private String aircraftModel;

    @Schema(description = "Operational reason for equipment change", example = "Technical maintenance on scheduled Boeing 737")
    private String reason;

    @Schema(description = "Force change even if minor cabin configuration differences exist", defaultValue = "false")
    private boolean force;

    public FlightAircraftChangeRequest() {
    }

    public FlightAircraftChangeRequest(String aircraftModel, String reason, boolean force) {
        this.aircraftModel = aircraftModel;
        this.reason = reason;
        this.force = force;
    }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public boolean isForce() { return force; }
    public void setForce(boolean force) { this.force = force; }
}
