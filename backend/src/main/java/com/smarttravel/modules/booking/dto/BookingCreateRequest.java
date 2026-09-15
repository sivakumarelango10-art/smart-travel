package com.smarttravel.modules.booking.dto;

import com.smarttravel.modules.flight.model.CabinClass;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for creating a new flight booking and reserving cabin seat inventory.
 */
@Schema(description = "Flight Booking Creation Payload")
public class BookingCreateRequest {

    @Schema(description = "Flight MongoDB ObjectId", example = "66c1e101f1a2b3c4d5e6f702")
    @NotBlank(message = "Flight ID is required")
    private String flightId;

    @Schema(description = "Selected cabin class tier", example = "ECONOMY")
    @NotNull(message = "Cabin class is required")
    private CabinClass cabinClass;

    @Schema(description = "List of passenger details (1 to 9 passengers)")
    @NotEmpty(message = "Passenger list must not be empty")
    @Size(min = 1, max = 9, message = "A booking must contain between 1 and 9 passengers")
    @Valid
    private List<PassengerDto> passengers;

    @Schema(description = "Optional active Price Freeze ID to apply locked fare snapshot", example = "66c1e101f1a2b3c4d5e6f703")
    private String priceFreezeId;

    @Schema(description = "Optional promotional coupon code applied at checkout", example = "SMARTFLY25")
    private String couponCode;

    @Schema(description = "Optional pre-calculated discount amount", example = "1500.00")
    private java.math.BigDecimal discountAmount;

    public BookingCreateRequest() {
    }

    public BookingCreateRequest(String flightId, CabinClass cabinClass, List<PassengerDto> passengers) {
        this.flightId = flightId;
        this.cabinClass = cabinClass;
        this.passengers = passengers;
    }

    public BookingCreateRequest(String flightId, CabinClass cabinClass, List<PassengerDto> passengers, String priceFreezeId) {
        this.flightId = flightId;
        this.cabinClass = cabinClass;
        this.passengers = passengers;
        this.priceFreezeId = priceFreezeId;
    }

    public BookingCreateRequest(String flightId, CabinClass cabinClass, List<PassengerDto> passengers,
                                String priceFreezeId, String couponCode, java.math.BigDecimal discountAmount) {
        this.flightId = flightId;
        this.cabinClass = cabinClass;
        this.passengers = passengers;
        this.priceFreezeId = priceFreezeId;
        this.couponCode = couponCode;
        this.discountAmount = discountAmount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String flightId;
        private CabinClass cabinClass;
        private List<PassengerDto> passengers;
        private String priceFreezeId;
        private String couponCode;
        private java.math.BigDecimal discountAmount;

        public Builder flightId(String flightId) {
            this.flightId = flightId;
            return this;
        }

        public Builder cabinClass(CabinClass cabinClass) {
            this.cabinClass = cabinClass;
            return this;
        }

        public Builder passengers(List<PassengerDto> passengers) {
            this.passengers = passengers;
            return this;
        }

        public Builder priceFreezeId(String priceFreezeId) {
            this.priceFreezeId = priceFreezeId;
            return this;
        }

        public Builder couponCode(String couponCode) {
            this.couponCode = couponCode;
            return this;
        }

        public Builder discountAmount(java.math.BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public BookingCreateRequest build() {
            return new BookingCreateRequest(flightId, cabinClass, passengers, priceFreezeId, couponCode, discountAmount);
        }
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public List<PassengerDto> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerDto> passengers) {
        this.passengers = passengers;
    }

    public String getPriceFreezeId() {
        return priceFreezeId;
    }

    public void setPriceFreezeId(String priceFreezeId) {
        this.priceFreezeId = priceFreezeId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public java.math.BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(java.math.BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}
