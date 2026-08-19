package com.smarttravel.modules.flight.disruption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Flight Cancellation Operational Request Payload")
public class FlightCancelRequest {

    @Schema(description = "Operational cancellation reason", requiredMode = Schema.RequiredMode.REQUIRED, example = "Severe weather conditions at departure airport")
    @NotBlank(message = "Cancellation reason is required")
    private String reason;

    @Schema(description = "Detailed explanation of cancellation", example = "Cyclone warning issued; all flight operations suspended for 12 hours")
    private String description;

    @Schema(description = "Whether to automatically trigger eligible customer refunds", defaultValue = "true")
    private boolean autoRefund = true;

    public FlightCancelRequest() {
    }

    public FlightCancelRequest(String reason, String description, boolean autoRefund) {
        this.reason = reason;
        this.description = description;
        this.autoRefund = autoRefund;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isAutoRefund() { return autoRefund; }
    public void setAutoRefund(boolean autoRefund) { this.autoRefund = autoRefund; }
}
