package com.smarttravel.modules.hotel.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Embedded room type within a hotel.
 * Tracks availability using availableRooms / totalRooms.
 */
public class RoomType {

    private String id; // unique within the hotel
    private String name; // e.g. "Deluxe Room", "Junior Suite", "Premium Suite"
    private RoomCategory category;
    private String description;

    private int totalRooms;
    private int availableRooms;

    private int maxOccupancy;
    private String bedType; // e.g. "King", "Twin", "Double"
    private int sizeInSqFt;

    private BigDecimal nightlyRate;
    private BigDecimal taxAmount;
    private BigDecimal totalNightlyRate;

    private String currency = "INR";

    private List<String> amenities = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();

    private boolean breakfastIncluded = false;
    private boolean refundable = true;

    /** 360 room-specific virtual tour metadata */
    private VirtualTour virtualTour;

    public RoomType() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RoomType r = new RoomType();
        public Builder id(String v) { r.id = v; return this; }
        public Builder name(String v) { r.name = v; return this; }
        public Builder category(RoomCategory v) { r.category = v; return this; }
        public Builder description(String v) { r.description = v; return this; }
        public Builder totalRooms(int v) { r.totalRooms = v; return this; }
        public Builder availableRooms(int v) { r.availableRooms = v; return this; }
        public Builder maxOccupancy(int v) { r.maxOccupancy = v; return this; }
        public Builder bedType(String v) { r.bedType = v; return this; }
        public Builder sizeInSqFt(int v) { r.sizeInSqFt = v; return this; }
        public Builder nightlyRate(BigDecimal v) { r.nightlyRate = v; return this; }
        public Builder taxAmount(BigDecimal v) { r.taxAmount = v; return this; }
        public Builder totalNightlyRate(BigDecimal v) { r.totalNightlyRate = v; return this; }
        public Builder currency(String v) { r.currency = v; return this; }
        public Builder amenities(List<String> v) { r.amenities = v; return this; }
        public Builder imageUrls(List<String> v) { r.imageUrls = v; return this; }
        public Builder breakfastIncluded(boolean v) { r.breakfastIncluded = v; return this; }
        public Builder refundable(boolean v) { r.refundable = v; return this; }
        public Builder virtualTour(VirtualTour v) { r.virtualTour = v; return this; }
        public RoomType build() { return r; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public RoomCategory getCategory() { return category; }
    public void setCategory(RoomCategory category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
    public int getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(int maxOccupancy) { this.maxOccupancy = maxOccupancy; }
    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }
    public int getSizeInSqFt() { return sizeInSqFt; }
    public void setSizeInSqFt(int sizeInSqFt) { this.sizeInSqFt = sizeInSqFt; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
    public void setNightlyRate(BigDecimal nightlyRate) { this.nightlyRate = nightlyRate; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalNightlyRate() { return totalNightlyRate; }
    public void setTotalNightlyRate(BigDecimal totalNightlyRate) { this.totalNightlyRate = totalNightlyRate; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public boolean isBreakfastIncluded() { return breakfastIncluded; }
    public void setBreakfastIncluded(boolean breakfastIncluded) { this.breakfastIncluded = breakfastIncluded; }
    public boolean isRefundable() { return refundable; }
    public void setRefundable(boolean refundable) { this.refundable = refundable; }
    public VirtualTour getVirtualTour() { return virtualTour; }
    public void setVirtualTour(VirtualTour virtualTour) { this.virtualTour = virtualTour; }
}
