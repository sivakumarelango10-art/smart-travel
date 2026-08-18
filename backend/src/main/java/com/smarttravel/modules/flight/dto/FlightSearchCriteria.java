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

    @Schema(description = "Number of passengers (1 to 9)", example = "2", defaultValue = "1")
    private Integer passengers = 1;

    @Schema(description = "Filter by airline name", example = "Air India")
    private String airline;

    @Schema(description = "Filter by cabin class", example = "ECONOMY")
    private CabinClass cabinClass;

    @Schema(description = "Minimum price", example = "3000.00")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price", example = "10000.00")
    private BigDecimal maxPrice;

    @Schema(description = "Departure time window filter", example = "MORNING")
    private DepartureTimeWindow departureTimeWindow;

    @Schema(description = "Filter by flight status", example = "SCHEDULED")
    private FlightStatus status;

    @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private int size = 20;

    @Schema(description = "Sort field or alias ('CHEAPEST', 'FASTEST', 'EARLIEST_DEPARTURE', 'LATEST_DEPARTURE', 'BEST', 'price', 'departureTime')", example = "CHEAPEST", defaultValue = "departureTime")
    private String sortBy = "departureTime";

    @Schema(description = "Sort direction ('asc' or 'desc')", example = "asc", defaultValue = "asc")
    private String sortDirection = "asc";

    public FlightSearchCriteria() {
    }

    public FlightSearchCriteria(String origin, String destination, LocalDate departureDate, Integer passengers,
                                String airline, CabinClass cabinClass, BigDecimal minPrice,
                                BigDecimal maxPrice, DepartureTimeWindow departureTimeWindow, FlightStatus status,
                                int page, int size, String sortBy, String sortDirection) {
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.passengers = passengers;
        this.airline = airline;
        this.cabinClass = cabinClass;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.departureTimeWindow = departureTimeWindow;
        this.status = status;
        this.page = Math.max(0, page);
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
        private Integer passengers = 1;
        private String airline;
        private CabinClass cabinClass;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private DepartureTimeWindow departureTimeWindow;
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

        public Builder passengers(Integer passengers) {
            this.passengers = passengers;
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

        public Builder departureTimeWindow(DepartureTimeWindow departureTimeWindow) {
            this.departureTimeWindow = departureTimeWindow;
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
            return new FlightSearchCriteria(origin, destination, departureDate, passengers, airline,
                    cabinClass, minPrice, maxPrice, departureTimeWindow, status, page, size, sortBy, sortDirection);
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

    public Integer getPassengers() {
        return passengers;
    }

    public void setPassengers(Integer passengers) {
        this.passengers = passengers;
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

    public DepartureTimeWindow getDepartureTimeWindow() {
        return departureTimeWindow;
    }

    public void setDepartureTimeWindow(DepartureTimeWindow departureTimeWindow) {
        this.departureTimeWindow = departureTimeWindow;
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
