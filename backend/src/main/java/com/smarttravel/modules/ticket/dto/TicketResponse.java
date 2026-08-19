package com.smarttravel.modules.ticket.dto;

import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.ticket.model.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Public response DTO representing an issued electronic flight ticket.
 */
@Schema(description = "Issued Electronic Flight Ticket Response")
public class TicketResponse {

    @Schema(description = "Internal Ticket ID", example = "66c1e101f1a2b3c4d5e6fa01")
    private String id;

    @Schema(description = "Public Ticket Number", example = "ST-8K4P2Q7X9Y1Z")
    private String ticketNumber;

    @Schema(description = "Associated Booking ID", example = "66c1e101f1a2b3c4d5e6f801")
    private String bookingId;

    @Schema(description = "PNR Booking Reference", example = "ST8K4P2Q")
    private String bookingReference;

    @Schema(description = "User ID", example = "66c1e101f1a2b3c4d5e6f701")
    private String userId;

    @Schema(description = "User Email", example = "passenger@smarttravel.com")
    private String userEmail;

    @Schema(description = "Flight ID", example = "66c1e101f1a2b3c4d5e6f601")
    private String flightId;

    @Schema(description = "Flight Number", example = "ST-302")
    private String flightNumber;

    @Schema(description = "Airline Name", example = "SmartAir Express")
    private String airline;

    @Schema(description = "IATA/ICAO Airline Code", example = "SE")
    private String airlineCode;

    @Schema(description = "Aircraft Model", example = "Boeing 737 MAX 8")
    private String aircraftModel;

    @Schema(description = "Departure Airport Snapshot")
    private AirportDto departureAirport;

    @Schema(description = "Arrival Airport Snapshot")
    private AirportDto arrivalAirport;

    @Schema(description = "Scheduled Departure Time", example = "2026-09-01T06:00:00Z")
    private Instant departureTime;

    @Schema(description = "Scheduled Arrival Time", example = "2026-09-01T08:15:00Z")
    private Instant arrivalTime;

    @Schema(description = "Duration in minutes", example = "135")
    private Integer durationMinutes;

    @Schema(description = "Cabin Class", example = "ECONOMY")
    private CabinClass cabinClass;

    @Schema(description = "Passenger Count", example = "2")
    private int passengerCount;

    @Schema(description = "Passengers list on this ticket")
    private List<PassengerTicketResponse> passengers = new ArrayList<>();

    @Schema(description = "Fare Breakdown Snapshot")
    private FareBreakdownDto fareBreakdown;

    @Schema(description = "Total Amount Paid", example = "11500.00")
    private BigDecimal totalAmount;

    @Schema(description = "Currency", example = "INR")
    private String currency;

    @Schema(description = "Ticket Status", example = "ISSUED")
    private TicketStatus status;

    @Schema(description = "Payment ID", example = "66c1e101f1a2b3c4d5e6f901")
    private String paymentId;

    @Schema(description = "Razorpay Payment ID", example = "pay_N1234567890abc")
    private String razorpayPaymentId;

    @Schema(description = "Ticket Issue Timestamp", example = "2026-08-18T18:00:00Z")
    private Instant issuedAt;

    @Schema(description = "Ticket Cancellation Timestamp if cancelled")
    private Instant cancelledAt;

    @Schema(description = "Cancellation Reason if cancelled")
    private String cancellationReason;

    @Schema(description = "PDF Download URL endpoint", example = "/api/v1/tickets/66c1e101f1a2b3c4d5e6fa01/pdf")
    private String pdfDownloadUrl;

    public TicketResponse() {
    }

    public TicketResponse(String id, String ticketNumber, String bookingId, String bookingReference,
                          String userId, String userEmail, String flightId, String flightNumber,
                          String airline, String airlineCode, String aircraftModel,
                          AirportDto departureAirport, AirportDto arrivalAirport,
                          Instant departureTime, Instant arrivalTime, Integer durationMinutes,
                          CabinClass cabinClass, int passengerCount, List<PassengerTicketResponse> passengers,
                          FareBreakdownDto fareBreakdown, BigDecimal totalAmount, String currency,
                          TicketStatus status, String paymentId, String razorpayPaymentId,
                          Instant issuedAt, Instant cancelledAt, String cancellationReason, String pdfDownloadUrl) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.userEmail = userEmail;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.airlineCode = airlineCode;
        this.aircraftModel = aircraftModel;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.durationMinutes = durationMinutes;
        this.cabinClass = cabinClass;
        this.passengerCount = passengerCount;
        this.passengers = passengers != null ? passengers : new ArrayList<>();
        this.fareBreakdown = fareBreakdown;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.status = status;
        this.paymentId = paymentId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.issuedAt = issuedAt;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.pdfDownloadUrl = pdfDownloadUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String ticketNumber;
        private String bookingId;
        private String bookingReference;
        private String userId;
        private String userEmail;
        private String flightId;
        private String flightNumber;
        private String airline;
        private String airlineCode;
        private String aircraftModel;
        private AirportDto departureAirport;
        private AirportDto arrivalAirport;
        private Instant departureTime;
        private Instant arrivalTime;
        private Integer durationMinutes;
        private CabinClass cabinClass;
        private int passengerCount;
        private List<PassengerTicketResponse> passengers = new ArrayList<>();
        private FareBreakdownDto fareBreakdown;
        private BigDecimal totalAmount;
        private String currency;
        private TicketStatus status;
        private String paymentId;
        private String razorpayPaymentId;
        private Instant issuedAt;
        private Instant cancelledAt;
        private String cancellationReason;
        private String pdfDownloadUrl;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder ticketNumber(String ticketNumber) {
            this.ticketNumber = ticketNumber;
            return this;
        }

        public Builder bookingId(String bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        public Builder bookingReference(String bookingReference) {
            this.bookingReference = bookingReference;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public Builder flightId(String flightId) {
            this.flightId = flightId;
            return this;
        }

        public Builder flightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
            return this;
        }

        public Builder airline(String airline) {
            this.airline = airline;
            return this;
        }

        public Builder airlineCode(String airlineCode) {
            this.airlineCode = airlineCode;
            return this;
        }

        public Builder aircraftModel(String aircraftModel) {
            this.aircraftModel = aircraftModel;
            return this;
        }

        public Builder departureAirport(AirportDto departureAirport) {
            this.departureAirport = departureAirport;
            return this;
        }

        public Builder arrivalAirport(AirportDto arrivalAirport) {
            this.arrivalAirport = arrivalAirport;
            return this;
        }

        public Builder departureTime(Instant departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        public Builder arrivalTime(Instant arrivalTime) {
            this.arrivalTime = arrivalTime;
            return this;
        }

        public Builder durationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public Builder cabinClass(CabinClass cabinClass) {
            this.cabinClass = cabinClass;
            return this;
        }

        public Builder passengerCount(int passengerCount) {
            this.passengerCount = passengerCount;
            return this;
        }

        public Builder passengers(List<PassengerTicketResponse> passengers) {
            this.passengers = passengers != null ? passengers : new ArrayList<>();
            return this;
        }

        public Builder fareBreakdown(FareBreakdownDto fareBreakdown) {
            this.fareBreakdown = fareBreakdown;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder status(TicketStatus status) {
            this.status = status;
            return this;
        }

        public Builder paymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder razorpayPaymentId(String razorpayPaymentId) {
            this.razorpayPaymentId = razorpayPaymentId;
            return this;
        }

        public Builder issuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public Builder cancelledAt(Instant cancelledAt) {
            this.cancelledAt = cancelledAt;
            return this;
        }

        public Builder cancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
            return this;
        }

        public Builder pdfDownloadUrl(String pdfDownloadUrl) {
            this.pdfDownloadUrl = pdfDownloadUrl;
            return this;
        }

        public TicketResponse build() {
            return new TicketResponse(id, ticketNumber, bookingId, bookingReference, userId, userEmail,
                    flightId, flightNumber, airline, airlineCode, aircraftModel,
                    departureAirport, arrivalAirport, departureTime, arrivalTime,
                    durationMinutes, cabinClass, passengerCount, passengers,
                    fareBreakdown, totalAmount, currency, status, paymentId,
                    razorpayPaymentId, issuedAt, cancelledAt, cancellationReason, pdfDownloadUrl);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public String getAircraftModel() {
        return aircraftModel;
    }

    public void setAircraftModel(String aircraftModel) {
        this.aircraftModel = aircraftModel;
    }

    public AirportDto getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(AirportDto departureAirport) {
        this.departureAirport = departureAirport;
    }

    public AirportDto getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(AirportDto arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public Instant getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Instant departureTime) {
        this.departureTime = departureTime;
    }

    public Instant getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Instant arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }

    public List<PassengerTicketResponse> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerTicketResponse> passengers) {
        this.passengers = passengers != null ? passengers : new ArrayList<>();
    }

    public FareBreakdownDto getFareBreakdown() {
        return fareBreakdown;
    }

    public void setFareBreakdown(FareBreakdownDto fareBreakdown) {
        this.fareBreakdown = fareBreakdown;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getPdfDownloadUrl() {
        return pdfDownloadUrl;
    }

    public void setPdfDownloadUrl(String pdfDownloadUrl) {
        this.pdfDownloadUrl = pdfDownloadUrl;
    }
}
