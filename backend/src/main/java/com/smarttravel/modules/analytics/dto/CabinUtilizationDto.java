package com.smarttravel.modules.analytics.dto;

import java.math.BigDecimal;

/**
 * Seat utilization for a single cabin class (no PII).
 */
public class CabinUtilizationDto {

    private String cabinClass;
    private long totalSeats;
    private long availableSeats;
    private long bookedSeats;
    private long heldSeats;
    private BigDecimal occupancyPercentage;

    public CabinUtilizationDto() {
    }

    public CabinUtilizationDto(String cabinClass, long totalSeats, long availableSeats,
                               long bookedSeats, long heldSeats, BigDecimal occupancyPercentage) {
        this.cabinClass = cabinClass;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.bookedSeats = bookedSeats;
        this.heldSeats = heldSeats;
        this.occupancyPercentage = occupancyPercentage;
    }

    public String getCabinClass() { return cabinClass; }
    public long getTotalSeats() { return totalSeats; }
    public long getAvailableSeats() { return availableSeats; }
    public long getBookedSeats() { return bookedSeats; }
    public long getHeldSeats() { return heldSeats; }
    public BigDecimal getOccupancyPercentage() { return occupancyPercentage; }
}
