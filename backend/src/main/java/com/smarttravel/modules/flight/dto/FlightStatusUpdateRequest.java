package com.smarttravel.modules.flight.dto;

import com.smarttravel.modules.flight.model.FlightStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Admin Flight Status Update Payload")
public class FlightStatusUpdateRequest {

    @Schema(description = "New flight status", example = "DELAYED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Flight status is required")
    private FlightStatus status;

    public FlightStatusUpdateRequest() {
    }

    public FlightStatusUpdateRequest(FlightStatus status) {
        this.status = status;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }
}
