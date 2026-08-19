package com.smarttravel.modules.flight.dto;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.SeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Data Transfer Object for public seat availability without exposing passenger PII.
 */
@Schema(description = "Seat Details Response")
public class SeatDto {

    @Schema(description = "Seat Number", example = "12A")
    private String seatNumber;

    @Schema(description = "Row Number", example = "12")
    private int rowNumber;

    @Schema(description = "Column Letter", example = "A")
    private String column;

    @Schema(description = "Cabin Class", example = "ECONOMY")
    private CabinClass cabinClass;

    @Schema(description = "Seat Status", example = "AVAILABLE")
    private SeatStatus status;

    @Schema(description = "Additional Price Adjustment", example = "350.00")
    private BigDecimal priceAdjustment;

    public SeatDto() {
    }

    public SeatDto(String seatNumber, int rowNumber, String column, CabinClass cabinClass,
                   SeatStatus status, BigDecimal priceAdjustment) {
        this.seatNumber = seatNumber;
        this.rowNumber = rowNumber;
        this.column = column;
        this.cabinClass = cabinClass;
        this.status = status;
        this.priceAdjustment = priceAdjustment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }

    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }

    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public BigDecimal getPriceAdjustment() { return priceAdjustment; }
    public void setPriceAdjustment(BigDecimal priceAdjustment) { this.priceAdjustment = priceAdjustment; }

    public static class Builder {
        private String seatNumber;
        private int rowNumber;
        private String column;
        private CabinClass cabinClass;
        private SeatStatus status;
        private BigDecimal priceAdjustment;

        public Builder seatNumber(String seatNumber) { this.seatNumber = seatNumber; return this; }
        public Builder rowNumber(int rowNumber) { this.rowNumber = rowNumber; return this; }
        public Builder column(String column) { this.column = column; return this; }
        public Builder cabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; return this; }
        public Builder status(SeatStatus status) { this.status = status; return this; }
        public Builder priceAdjustment(BigDecimal priceAdjustment) { this.priceAdjustment = priceAdjustment; return this; }

        public SeatDto build() {
            return new SeatDto(seatNumber, rowNumber, column, cabinClass, status, priceAdjustment);
        }
    }
}
