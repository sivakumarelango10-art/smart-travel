package com.smarttravel.modules.flight.dto;

import com.smarttravel.modules.flight.model.CabinClass;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details and Calculated Multi-Passenger Fare for the Selected Cabin Tier")
public class CabinSelectionResponse {

    @Schema(description = "Selected cabin class tier", example = "ECONOMY")
    private CabinClass cabinClass;

    @Schema(description = "Available seats in this cabin", example = "42")
    private int availableSeats;

    @Schema(description = "Single passenger fare breakdown")
    private FareBreakdownDto singlePassengerFare;

    @Schema(description = "Total fare breakdown scaled for all requested passengers")
    private FareBreakdownDto totalFare;

    public CabinSelectionResponse() {
    }

    public CabinSelectionResponse(CabinClass cabinClass, int availableSeats,
                                  FareBreakdownDto singlePassengerFare, FareBreakdownDto totalFare) {
        this.cabinClass = cabinClass;
        this.availableSeats = availableSeats;
        this.singlePassengerFare = singlePassengerFare;
        this.totalFare = totalFare;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CabinClass cabinClass = CabinClass.ECONOMY;
        private int availableSeats;
        private FareBreakdownDto singlePassengerFare;
        private FareBreakdownDto totalFare;

        public Builder cabinClass(CabinClass cabinClass) {
            this.cabinClass = cabinClass;
            return this;
        }

        public Builder availableSeats(int availableSeats) {
            this.availableSeats = availableSeats;
            return this;
        }

        public Builder singlePassengerFare(FareBreakdownDto singlePassengerFare) {
            this.singlePassengerFare = singlePassengerFare;
            return this;
        }

        public Builder totalFare(FareBreakdownDto totalFare) {
            this.totalFare = totalFare;
            return this;
        }

        public CabinSelectionResponse build() {
            return new CabinSelectionResponse(cabinClass, availableSeats, singlePassengerFare, totalFare);
        }
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public FareBreakdownDto getSinglePassengerFare() {
        return singlePassengerFare;
    }

    public void setSinglePassengerFare(FareBreakdownDto singlePassengerFare) {
        this.singlePassengerFare = singlePassengerFare;
    }

    public FareBreakdownDto getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(FareBreakdownDto totalFare) {
        this.totalFare = totalFare;
    }
}
