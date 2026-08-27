package com.smarttravel.modules.hotel.dto;

import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.hotel.model.RoomCategory;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class HotelBookingDto {

    public record PriceCalculateRequest(
            @NotBlank(message = "Hotel ID is required")
            String hotelId,

            @NotBlank(message = "Room Type ID is required")
            String roomTypeId,

            @NotNull(message = "Check-in date is required")
            @FutureOrPresent(message = "Check-in date cannot be in the past")
            LocalDate checkInDate,

            @NotNull(message = "Check-out date is required")
            LocalDate checkOutDate,

            @Min(value = 1, message = "Guest count must be at least 1")
            int guestCount,

            @Min(value = 1, message = "Room count must be at least 1")
            int roomCount,

            String couponCode
    ) {}

    public record PriceCalculateResponse(
            String hotelId,
            String hotelName,
            String roomTypeId,
            String roomTypeName,
            RoomCategory roomCategory,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int nights,
            int guestCount,
            int roomCount,
            BigDecimal nightlyRate,
            BigDecimal baseAmount,
            BigDecimal taxAmount,
            BigDecimal taxRatePercentage,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            String currency,
            String cancellationPolicy,
            boolean isAvailable,
            int availableRooms
    ) {}

    public record CreateHotelBookingRequest(
            @NotBlank(message = "Hotel ID is required")
            String hotelId,

            @NotBlank(message = "Room Type ID is required")
            String roomTypeId,

            @NotNull(message = "Check-in date is required")
            @FutureOrPresent(message = "Check-in date cannot be in the past")
            LocalDate checkInDate,

            @NotNull(message = "Check-out date is required")
            LocalDate checkOutDate,

            @Min(value = 1, message = "Guest count must be at least 1")
            int guestCount,

            @Min(value = 1, message = "Room count must be at least 1")
            int roomCount,

            @NotBlank(message = "Primary guest name is required")
            String primaryGuestName,

            @NotBlank(message = "Primary guest email is required")
            String primaryGuestEmail,

            String primaryGuestPhone,

            String specialRequests,

            String couponCode,

            String paymentMethod
    ) {}

    public record HotelBookingResponse(
            String id,
            String bookingReference,
            String userId,
            String userEmail,
            String hotelId,
            String hotelName,
            String hotelCity,
            String hotelAddress,
            String hotelImageUrl,
            String roomTypeId,
            String roomTypeName,
            RoomCategory roomCategory,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int nights,
            int guestCount,
            int roomCount,
            String primaryGuestName,
            String primaryGuestEmail,
            String primaryGuestPhone,
            String specialRequests,
            BigDecimal nightlyRate,
            BigDecimal baseAmount,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            String currency,
            BookingStatus status,
            String paymentId,
            String paymentStatus,
            String cancellationPolicy,
            Instant cancelledAt,
            String cancellationReason,
            BigDecimal refundAmount,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record CancelHotelBookingRequest(
            String cancellationReason
    ) {}

    public record HotelRefundCalculation(
            String bookingReference,
            BigDecimal originalAmount,
            BigDecimal refundAmount,
            int refundPercentage,
            String policyApplied,
            String timelineDescription
    ) {}
}
