package com.smarttravel.modules.flight.websocket;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.SeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * WebSocket event payload broadcast to /topic/seat-map/{flightId}
 * whenever seat availability or status changes in real-time.
 */
@Schema(description = "Real-time Seat Map Update Event")
public class SeatMapUpdateEvent {

    @Schema(description = "Flight MongoDB ID", example = "66c1e101f1a2b3c4d5e6f701")
    private String flightId;

    @Schema(description = "List of affected seat numbers", example = "[\"12A\", \"12B\"]")
    private List<String> seatNumbers;

    @Schema(description = "New seat status", example = "HELD")
    private SeatStatus status;

    @Schema(description = "Cabin class", example = "ECONOMY")
    private CabinClass cabinClass;

    @Schema(description = "Price adjustment for seat if applicable")
    private BigDecimal priceAdjustment;

    @Schema(description = "Action type: HELD, CONFIRMED, RELEASED, EXPIRED")
    private String action;

    @Schema(description = "Timestamp of the update event")
    private Instant timestamp;

    public SeatMapUpdateEvent() {
    }

    public SeatMapUpdateEvent(String flightId, List<String> seatNumbers, SeatStatus status,
                              CabinClass cabinClass, BigDecimal priceAdjustment, String action, Instant timestamp) {
        this.flightId = flightId;
        this.seatNumbers = seatNumbers;
        this.status = status;
        this.cabinClass = cabinClass;
        this.priceAdjustment = priceAdjustment;
        this.action = action;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String flightId;
        private List<String> seatNumbers;
        private SeatStatus status;
        private CabinClass cabinClass;
        private BigDecimal priceAdjustment;
        private String action;
        private Instant timestamp = Instant.now();

        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder seatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers; return this; }
        public Builder status(SeatStatus status) { this.status = status; return this; }
        public Builder cabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; return this; }
        public Builder priceAdjustment(BigDecimal priceAdjustment) { this.priceAdjustment = priceAdjustment; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public SeatMapUpdateEvent build() {
            return new SeatMapUpdateEvent(flightId, seatNumbers, status, cabinClass, priceAdjustment, action, timestamp);
        }
    }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public List<String> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers; }

    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }

    public BigDecimal getPriceAdjustment() { return priceAdjustment; }
    public void setPriceAdjustment(BigDecimal priceAdjustment) { this.priceAdjustment = priceAdjustment; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
