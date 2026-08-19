package com.smarttravel.modules.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.Map;

/**
 * Request payload for online check-in.
 */
@Schema(description = "Online Check-In Request Payload")
public class CheckInRequest {

    @Schema(description = "Optional mapping of passenger first name or index to requested seat number", example = "{\"Sarah\": \"12A\"}")
    private Map<String, String> passengerSeats;

    public CheckInRequest() {
    }

    public CheckInRequest(Map<String, String> passengerSeats) {
        this.passengerSeats = passengerSeats;
    }

    public Map<String, String> getPassengerSeats() {
        return passengerSeats;
    }

    public void setPassengerSeats(Map<String, String> passengerSeats) {
        this.passengerSeats = passengerSeats;
    }
}
