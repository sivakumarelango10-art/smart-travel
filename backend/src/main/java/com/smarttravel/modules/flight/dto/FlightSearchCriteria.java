package com.smarttravel.modules.flight.dto;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.FlightStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Multi-Criteria Flight Search Parameters")
public class FlightSearchCriteria {

    @Schema(description = "Origin airport code or city name", example = "DEL")
    private String origin;

    @Schema(description = "Destination airport code or city name", example = "BOM")
    private String destination;

    @Schema(description = "Departure date (YYYY-MM-DD)", example = "2026-08-20")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;

    @Schema(description = "Filter by airline name", example = "Air India")
    private String airline;

    @Schema(description = "Filter by cabin class", example = "ECONOMY")
    private CabinClass cabinClass;

    @Schema(description = "Minimum base price", example = "3000.00")
    private BigDecimal minPrice;

    @Schema(description = "Maximum base price", example = "10000.00")
    private BigDecimal maxPrice;

    @Schema(description = "Filter by flight status", example = "SCHEDULED")
    private FlightStatus status;

    @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private int size = 20;

    @Schema(description = "Sort field (e.g. 'departureTime', 'basePrice', 'durationMinutes')", example = "departureTime", defaultValue = "departureTime")
    private String sortBy = "departureTime";

    @Schema(description = "Sort direction ('asc' or 'desc')", example = "asc", defaultValue = "asc")
    private String sortDirection = "asc";

    public FlightSearchCriteria() {
    }

    public FlightSearchCriteria(String origin, String destination, LocalDate departureDate,
                                String airline, CabinClass cabinClass, BigDecimal minPrice,
                                BigDecimal maxPrice, FlightStatus status,
                                int page, int size, String sortBy, String sortDirection) {
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.airline = airline;
        this.cabinClass = cabinClass;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.status = status;
        this.page = page;
        this.size = size > 0 ? size : 20;
        this.sortBy = sortBy != null && !sortBy.isBlank() ? sortBy : "departureTime";
        this.sortDirection = sortDirection != null && !sortDirection.isBlank() ? sortDirection : "asc";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String origin;
        private String destination;
        private LocalDate departureDate;
        private String airline;
        private CabinClass cabinClass;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private FlightStatus status;
        private int page = 0;
        private int size = 20;
        private String sortBy = "departureTime";
        private String sortDirection = "asc";

        public Builder origin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder destination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder departureDate(LocalDate departureDate) {
            this.departureDate = departureDate;
            return this;
        }

        public Builder airline(String airline) {
            this.airline = airline;
            return this;
        }

        public Builder cabinClass(CabinClass cabinClass) {
            this.cabinClass = cabinClass;
            return this;
        }

        public Builder minPrice(BigDecimal minPrice) {
            this.minPrice = minPrice;
            return this;
        }

        public Builder maxPrice(BigDecimal maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        public Builder status(FlightStatus status) {
            this.status = status;
            return this;
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder sortDirection(String sortDirection) {
            this.sortDirection = sortDirection;
            return this;
        }

        public FlightSearchCriteria build() {
            return new FlightSearchCriteria(origin, destination, departureDate, airline,
                    cabinClass, minPrice, maxPrice, status, page, size, sortBy, sortDirection);
        }
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
