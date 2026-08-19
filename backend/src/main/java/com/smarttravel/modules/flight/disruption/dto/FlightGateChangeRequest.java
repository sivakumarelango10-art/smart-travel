package com.smarttravel.modules.flight.disruption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Flight Gate Change Request Payload")
public class FlightGateChangeRequest {

    @Schema(description = "New departure gate", requiredMode = Schema.RequiredMode.REQUIRED, example = "Gate 14B")
    @NotBlank(message = "New gate is required")
    private String gate;

    @Schema(description = "Operational reason", example = "Aircraft parked at remote stand")
    private String reason;

    public FlightGateChangeRequest() {
    }

    public FlightGateChangeRequest(String gate, String reason) {
        this.gate = gate;
        this.reason = reason;
    }

    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
