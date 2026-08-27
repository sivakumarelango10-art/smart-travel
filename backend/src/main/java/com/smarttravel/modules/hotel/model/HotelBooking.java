package com.smarttravel.modules.hotel.model;

import com.smarttravel.modules.booking.model.BookingStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * MongoDB Document entity representing a Hotel Room Reservation.
 */
@Document(collection = "hotel_bookings")
@CompoundIndexes({
        @CompoundIndex(name = "hotel_booking_user_created_idx", def = "{'userId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "hotel_booking_hotel_status_idx", def = "{'hotelId': 1, 'status': 1}"),
        @CompoundIndex(name = "hotel_booking_reference_idx", def = "{'bookingReference': 1}", unique = true)
})
public class HotelBooking {

    @Id
    private String id;

    @Indexed(unique = true)
    private String bookingReference;

    @Indexed
    private String userId;

    private String userEmail;

    @Indexed
    private String hotelId;

    private String hotelName;

    private String hotelCity;

    private String hotelAddress;

    private String hotelImageUrl;

    private String roomTypeId;

    private String roomTypeName;

    private RoomCategory roomCategory;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private int nights;

    private int guestCount;

    private int roomCount;

    private String primaryGuestName;

    private String primaryGuestEmail;

    private String primaryGuestPhone;

    private String specialRequests;

    private BigDecimal nightlyRate;

    private BigDecimal baseAmount;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal totalAmount;

    private String currency = "INR";

    private BookingStatus status = BookingStatus.CONFIRMED;

    private String paymentId;

    private String paymentStatus = "COMPLETED";

    private String cancellationPolicy;

    private Instant cancelledAt;

    private String cancellationReason;

    private BigDecimal refundAmount = BigDecimal.ZERO;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public HotelBooking() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final HotelBooking b = new HotelBooking();

        public Builder id(String id) { b.id = id; return this; }
        public Builder bookingReference(String bookingReference) { b.bookingReference = bookingReference; return this; }
        public Builder userId(String userId) { b.userId = userId; return this; }
        public Builder userEmail(String userEmail) { b.userEmail = userEmail; return this; }
        public Builder hotelId(String hotelId) { b.hotelId = hotelId; return this; }
        public Builder hotelName(String hotelName) { b.hotelName = hotelName; return this; }
        public Builder hotelCity(String hotelCity) { b.hotelCity = hotelCity; return this; }
        public Builder hotelAddress(String hotelAddress) { b.hotelAddress = hotelAddress; return this; }
        public Builder hotelImageUrl(String hotelImageUrl) { b.hotelImageUrl = hotelImageUrl; return this; }
        public Builder roomTypeId(String roomTypeId) { b.roomTypeId = roomTypeId; return this; }
        public Builder roomTypeName(String roomTypeName) { b.roomTypeName = roomTypeName; return this; }
        public Builder roomCategory(RoomCategory roomCategory) { b.roomCategory = roomCategory; return this; }
        public Builder checkInDate(LocalDate checkInDate) { b.checkInDate = checkInDate; return this; }
        public Builder checkOutDate(LocalDate checkOutDate) { b.checkOutDate = checkOutDate; return this; }
        public Builder nights(int nights) { b.nights = nights; return this; }
        public Builder guestCount(int guestCount) { b.guestCount = guestCount; return this; }
        public Builder roomCount(int roomCount) { b.roomCount = roomCount; return this; }
        public Builder primaryGuestName(String primaryGuestName) { b.primaryGuestName = primaryGuestName; return this; }
        public Builder primaryGuestEmail(String primaryGuestEmail) { b.primaryGuestEmail = primaryGuestEmail; return this; }
        public Builder primaryGuestPhone(String primaryGuestPhone) { b.primaryGuestPhone = primaryGuestPhone; return this; }
        public Builder specialRequests(String specialRequests) { b.specialRequests = specialRequests; return this; }
        public Builder nightlyRate(BigDecimal nightlyRate) { b.nightlyRate = nightlyRate; return this; }
        public Builder baseAmount(BigDecimal baseAmount) { b.baseAmount = baseAmount; return this; }
        public Builder taxAmount(BigDecimal taxAmount) { b.taxAmount = taxAmount; return this; }
        public Builder discountAmount(BigDecimal discountAmount) { b.discountAmount = discountAmount; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { b.totalAmount = totalAmount; return this; }
        public Builder currency(String currency) { b.currency = currency; return this; }
        public Builder status(BookingStatus status) { b.status = status; return this; }
        public Builder paymentId(String paymentId) { b.paymentId = paymentId; return this; }
        public Builder paymentStatus(String paymentStatus) { b.paymentStatus = paymentStatus; return this; }
        public Builder cancellationPolicy(String cancellationPolicy) { b.cancellationPolicy = cancellationPolicy; return this; }
        public Builder cancelledAt(Instant cancelledAt) { b.cancelledAt = cancelledAt; return this; }
        public Builder cancellationReason(String cancellationReason) { b.cancellationReason = cancellationReason; return this; }
        public Builder refundAmount(BigDecimal refundAmount) { b.refundAmount = refundAmount; return this; }
        public Builder createdAt(Instant createdAt) { b.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { b.updatedAt = updatedAt; return this; }

        public HotelBooking build() {
            return b;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getHotelCity() { return hotelCity; }
    public void setHotelCity(String hotelCity) { this.hotelCity = hotelCity; }

    public String getHotelAddress() { return hotelAddress; }
    public void setHotelAddress(String hotelAddress) { this.hotelAddress = hotelAddress; }

    public String getHotelImageUrl() { return hotelImageUrl; }
    public void setHotelImageUrl(String hotelImageUrl) { this.hotelImageUrl = hotelImageUrl; }

    public String getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(String roomTypeId) { this.roomTypeId = roomTypeId; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public RoomCategory getRoomCategory() { return roomCategory; }
    public void setRoomCategory(RoomCategory roomCategory) { this.roomCategory = roomCategory; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public int getNights() { return nights; }
    public void setNights(int nights) { this.nights = nights; }

    public int getGuestCount() { return guestCount; }
    public void setGuestCount(int guestCount) { this.guestCount = guestCount; }

    public int getRoomCount() { return roomCount; }
    public void setRoomCount(int roomCount) { this.roomCount = roomCount; }

    public String getPrimaryGuestName() { return primaryGuestName; }
    public void setPrimaryGuestName(String primaryGuestName) { this.primaryGuestName = primaryGuestName; }

    public String getPrimaryGuestEmail() { return primaryGuestEmail; }
    public void setPrimaryGuestEmail(String primaryGuestEmail) { this.primaryGuestEmail = primaryGuestEmail; }

    public String getPrimaryGuestPhone() { return primaryGuestPhone; }
    public void setPrimaryGuestPhone(String primaryGuestPhone) { this.primaryGuestPhone = primaryGuestPhone; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public BigDecimal getNightlyRate() { return nightlyRate; }
    public void setNightlyRate(BigDecimal nightlyRate) { this.nightlyRate = nightlyRate; }

    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(String cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }

    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotelBooking that = (HotelBooking) o;
        return Objects.equals(id, that.id) || Objects.equals(bookingReference, that.bookingReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bookingReference);
    }
}
