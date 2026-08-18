package com.smarttravel.modules.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload to initiate a Razorpay payment order for an existing booking.
 * Note: Payment amounts are strictly determined by the server from the booking's fare snapshot.
 */
@Schema(description = "Payment Order Creation Request Payload")
public class PaymentOrderCreateRequest {

    @NotBlank(message = "Booking ID is required")
    @Schema(description = "MongoDB ID of the booking to be paid", example = "66c1e101f1a2b3c4d5e6f801", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bookingId;

    @Schema(description = "Optional transaction description or user notes", example = "Flight to Mumbai payment")
    private String notes;

    public PaymentOrderCreateRequest() {
    }

    public PaymentOrderCreateRequest(String bookingId, String notes) {
        this.bookingId = bookingId;
        this.notes = notes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String bookingId;
        private String notes;

        public Builder bookingId(String bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public PaymentOrderCreateRequest build() {
            return new PaymentOrderCreateRequest(bookingId, notes);
        }
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
