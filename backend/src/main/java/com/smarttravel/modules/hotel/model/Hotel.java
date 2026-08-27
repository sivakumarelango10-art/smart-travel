package com.smarttravel.modules.hotel.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document representing a hotel in the SmartTravel catalog.
 */
@Document(collection = "hotels")
@CompoundIndexes({
        @CompoundIndex(name = "hotel_city_star_idx", def = "{'address.city': 1, 'starRating': 1, 'active': 1}"),
        @CompoundIndex(name = "hotel_name_city_idx", def = "{'name': 1, 'address.city': 1}"),
        @CompoundIndex(name = "hotel_airport_idx", def = "{'nearestAirportCode': 1, 'active': 1}")
})
public class Hotel {

    @Id
    private String id;

    private String name;

    private HotelAddress address;

    /** IATA code of nearest airport, for airport hotel search */
    @Indexed
    private String nearestAirportCode;

    private int starRating;

    private String description;

    private BigDecimal baseNightlyRate;

    private String currency = "INR";

    private List<String> amenities = new ArrayList<>();

    private List<String> imageUrls = new ArrayList<>();

    private HotelContactInfo contactInfo;

    private double averageRating = 0.0;

    private int totalReviews = 0;

    private boolean active = true;

    /** 360 virtual tour metadata */
    private VirtualTour virtualTour;

    /** Room types embedded or linked */
    private List<RoomType> roomTypes = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Hotel() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Hotel r = new Hotel();
        public Builder id(String v) { r.id = v; return this; }
        public Builder name(String v) { r.name = v; return this; }
        public Builder address(HotelAddress v) { r.address = v; return this; }
        public Builder nearestAirportCode(String v) { r.nearestAirportCode = v; return this; }
        public Builder starRating(int v) { r.starRating = v; return this; }
        public Builder description(String v) { r.description = v; return this; }
        public Builder baseNightlyRate(BigDecimal v) { r.baseNightlyRate = v; return this; }
        public Builder currency(String v) { r.currency = v; return this; }
        public Builder amenities(List<String> v) { r.amenities = v; return this; }
        public Builder imageUrls(List<String> v) { r.imageUrls = v; return this; }
        public Builder contactInfo(HotelContactInfo v) { r.contactInfo = v; return this; }
        public Builder averageRating(double v) { r.averageRating = v; return this; }
        public Builder totalReviews(int v) { r.totalReviews = v; return this; }
        public Builder active(boolean v) { r.active = v; return this; }
        public Builder virtualTour(VirtualTour v) { r.virtualTour = v; return this; }
        public Builder roomTypes(List<RoomType> v) { r.roomTypes = v; return this; }
        public Hotel build() { return r; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public HotelAddress getAddress() { return address; }
    public void setAddress(HotelAddress address) { this.address = address; }
    public String getNearestAirportCode() { return nearestAirportCode; }
    public void setNearestAirportCode(String nearestAirportCode) { this.nearestAirportCode = nearestAirportCode; }
    public int getStarRating() { return starRating; }
    public void setStarRating(int starRating) { this.starRating = starRating; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getBaseNightlyRate() { return baseNightlyRate; }
    public void setBaseNightlyRate(BigDecimal baseNightlyRate) { this.baseNightlyRate = baseNightlyRate; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public HotelContactInfo getContactInfo() { return contactInfo; }
    public void setContactInfo(HotelContactInfo contactInfo) { this.contactInfo = contactInfo; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public VirtualTour getVirtualTour() { return virtualTour; }
    public void setVirtualTour(VirtualTour virtualTour) { this.virtualTour = virtualTour; }
    public List<RoomType> getRoomTypes() { return roomTypes; }
    public void setRoomTypes(List<RoomType> roomTypes) { this.roomTypes = roomTypes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
