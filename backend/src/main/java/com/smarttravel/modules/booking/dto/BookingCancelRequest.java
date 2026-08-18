package com.smarttravel.modules.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request payload for cancelling a flight booking.
 */
@Schema(description = "Booking Cancellation Request")
public class BookingCancelRequest {

    @Schema(description = "Optional reason for cancellation", example = "Change of travel plans")
    @Size(max = 255, message = "Cancellation reason must not exceed 255 characters")
    private String reason;

    public BookingCancelRequest() {
    }

    public BookingCancelRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
