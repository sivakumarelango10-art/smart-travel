package com.smarttravel.modules.hotel.websocket;

import com.smarttravel.modules.hotel.model.RoomCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * WebSocket event payload broadcast to /topic/hotels/{hotelId}/rooms
 * whenever hotel room availability or hold state changes.
 */
@Schema(description = "Real-time Hotel Room Availability Update Event")
public class RoomAvailabilityEvent {

    @Schema(description = "Hotel MongoDB ID", example = "66c1e101f1a2b3c4d5e6f750")
    private String hotelId;

    @Schema(description = "Room Type ID", example = "rt-deluxe-01")
    private String roomTypeId;

    @Schema(description = "Room Type Name", example = "Deluxe Room")
    private String roomTypeName;

    @Schema(description = "Room Category", example = "DELUXE")
    private RoomCategory category;

    @Schema(description = "Updated count of available rooms", example = "3")
    private int availableRooms;

    @Schema(description = "Total room inventory", example = "10")
    private int totalRooms;

    @Schema(description = "Nightly rate for room", example = "4500.00")
    private BigDecimal nightlyRate;

    @Schema(description = "Action: HELD, RELEASED, BOOKED")
    private String action;

    @Schema(description = "Timestamp of the event")
    private Instant timestamp;

    public RoomAvailabilityEvent() {
    }

    public RoomAvailabilityEvent(String hotelId, String roomTypeId, String roomTypeName,
                                 RoomCategory category, int availableRooms, int totalRooms,
                                 BigDecimal nightlyRate, String action, Instant timestamp) {
        this.hotelId = hotelId;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.category = category;
        this.availableRooms = availableRooms;
        this.totalRooms = totalRooms;
        this.nightlyRate = nightlyRate;
        this.action = action;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String hotelId;
        private String roomTypeId;
        private String roomTypeName;
        private RoomCategory category;
        private int availableRooms;
        private int totalRooms;
        private BigDecimal nightlyRate;
        private String action;
        private Instant timestamp = Instant.now();

        public Builder hotelId(String hotelId) { this.hotelId = hotelId; return this; }
        public Builder roomTypeId(String roomTypeId) { this.roomTypeId = roomTypeId; return this; }
        public Builder roomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; return this; }
        public Builder category(RoomCategory category) { this.category = category; return this; }
        public Builder availableRooms(int availableRooms) { this.availableRooms = availableRooms; return this; }
        public Builder totalRooms(int totalRooms) { this.totalRooms = totalRooms; return this; }
        public Builder nightlyRate(BigDecimal nightlyRate) { this.nightlyRate = nightlyRate; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public RoomAvailabilityEvent build() {
            return new RoomAvailabilityEvent(hotelId, roomTypeId, roomTypeName, category, availableRooms, totalRooms, nightlyRate, action, timestamp);
        }
    }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(String roomTypeId) { this.roomTypeId = roomTypeId; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public RoomCategory getCategory() { return category; }
    public void setCategory(RoomCategory category) { this.category = category; }

    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public BigDecimal getNightlyRate() { return nightlyRate; }
    public void setNightlyRate(BigDecimal nightlyRate) { this.nightlyRate = nightlyRate; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
