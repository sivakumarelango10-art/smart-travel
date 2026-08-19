package com.smarttravel.modules.flight.disruption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Flight Terminal Change Request Payload")
public class FlightTerminalChangeRequest {

    @Schema(description = "New departure terminal", requiredMode = Schema.RequiredMode.REQUIRED, example = "T2")
    @NotBlank(message = "New terminal is required")
    private String terminal;

    @Schema(description = "Operational reason", example = "Terminal 3 undergoing scheduled sanitization")
    private String reason;

    public FlightTerminalChangeRequest() {
    }

    public FlightTerminalChangeRequest(String terminal, String reason) {
        this.terminal = terminal;
        this.reason = reason;
    }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
