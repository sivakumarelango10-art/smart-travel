package com.smarttravel.modules.payment.refund.dto;

import com.smarttravel.modules.payment.refund.model.RefundReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Admin Process Refund Request Payload")
public class RefundProcessRequest {

    @Schema(description = "Refund Ground / Reason", requiredMode = Schema.RequiredMode.REQUIRED, example = "FLIGHT_CANCELLED")
    @NotNull(message = "Refund reason is required")
    private RefundReason reason;

    @Schema(description = "Optional additional context or note for refund audit log", example = "Manual refund triggered by admin operations")
    private String description;

    public RefundProcessRequest() {
    }

    public RefundProcessRequest(RefundReason reason, String description) {
        this.reason = reason;
        this.description = description;
    }

    public RefundReason getReason() { return reason; }
    public void setReason(RefundReason reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
