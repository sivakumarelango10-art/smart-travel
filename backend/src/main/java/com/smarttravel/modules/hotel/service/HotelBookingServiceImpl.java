package com.smarttravel.modules.hotel.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ForbiddenException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.hotel.dto.HotelBookingDto;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.HotelBooking;
import com.smarttravel.modules.hotel.model.RoomType;
import com.smarttravel.modules.hotel.repository.HotelBookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class HotelBookingServiceImpl implements HotelBookingService {

    private static final Logger log = LoggerFactory.getLogger(HotelBookingServiceImpl.class);
    private static final BigDecimal TAX_RATE = new BigDecimal("0.12"); // 12% GST
    private static final String PNR_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final HotelBookingRepository hotelBookingRepository;
    private final HotelService hotelService;

    public HotelBookingServiceImpl(HotelBookingRepository hotelBookingRepository,
                                   HotelService hotelService) {
        this.hotelBookingRepository = hotelBookingRepository;
        this.hotelService = hotelService;
    }

    @Override
    public HotelBookingDto.PriceCalculateResponse calculatePrice(HotelBookingDto.PriceCalculateRequest request) {
        validateDates(request.checkInDate(), request.checkOutDate());

        Hotel hotel = hotelService.getHotelById(request.hotelId());
        RoomType room = findRoom(hotel, request.roomTypeId());

        int nights = (int) ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        int roomCount = Math.max(1, request.roomCount());

        BigDecimal nightlyRate = room.getNightlyRate() != null
                ? room.getNightlyRate()
                : (hotel.getBaseNightlyRate() != null ? hotel.getBaseNightlyRate() : BigDecimal.ZERO);
        BigDecimal baseAmount = nightlyRate.multiply(BigDecimal.valueOf((long) nights * roomCount)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = baseAmount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = calculateDiscount(request.couponCode(), baseAmount);
        BigDecimal totalAmount = baseAmount.add(taxAmount).subtract(discountAmount).max(BigDecimal.ZERO);

        boolean isAvailable = room.getAvailableRooms() >= roomCount;

        return new HotelBookingDto.PriceCalculateResponse(
                hotel.getId(),
                hotel.getName(),
                room.getId(),
                room.getName(),
                room.getCategory(),
                request.checkInDate(),
                request.checkOutDate(),
                nights,
                request.guestCount(),
                roomCount,
                nightlyRate,
                baseAmount,
                taxAmount,
                TAX_RATE.multiply(BigDecimal.valueOf(100)),
                discountAmount,
                totalAmount,
                hotel.getCurrency() != null ? hotel.getCurrency() : "INR",
                "Free cancellation up to 7 days before check-in (100% refund); 50% refund 24h–7 days; Non-refundable within 24h.",
                isAvailable,
                room.getAvailableRooms()
        );
    }

    @Override
    @Transactional
    public HotelBookingDto.HotelBookingResponse createBooking(HotelBookingDto.CreateHotelBookingRequest request, String userId, String userEmail) {
        validateDates(request.checkInDate(), request.checkOutDate());

        Hotel hotel = hotelService.getHotelById(request.hotelId());
        RoomType room = findRoom(hotel, request.roomTypeId());

        int roomCount = Math.max(1, request.roomCount());
        if (room.getAvailableRooms() < roomCount) {
            throw new BadRequestException("Selected room category is sold out or has insufficient inventory");
        }

        int nights = (int) ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        BigDecimal nightlyRate = room.getNightlyRate() != null
                ? room.getNightlyRate()
                : (hotel.getBaseNightlyRate() != null ? hotel.getBaseNightlyRate() : BigDecimal.ZERO);
        BigDecimal baseAmount = nightlyRate.multiply(BigDecimal.valueOf((long) nights * roomCount)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = baseAmount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = calculateDiscount(request.couponCode(), baseAmount);
        BigDecimal totalAmount = baseAmount.add(taxAmount).subtract(discountAmount).max(BigDecimal.ZERO);

        // Atomically hold room inventory
        hotelService.holdRoom(hotel.getId(), room.getId(), roomCount);

        String reference = generateBookingReference();
        String primaryImage = (hotel.getImageUrls() != null && !hotel.getImageUrls().isEmpty())
                ? hotel.getImageUrls().get(0) : "";

        HotelBooking booking = HotelBooking.builder()
                .id("hbk-" + UUID.randomUUID().toString().substring(0, 8))
                .bookingReference(reference)
                .userId(userId)
                .userEmail(userEmail)
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .hotelCity(hotel.getAddress() != null ? hotel.getAddress().getCity() : "")
                .hotelAddress(hotel.getAddress() != null ? hotel.getAddress().getLine1() : "")
                .hotelImageUrl(primaryImage)
                .roomTypeId(room.getId())
                .roomTypeName(room.getName())
                .roomCategory(room.getCategory())
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .nights(nights)
                .guestCount(request.guestCount())
                .roomCount(roomCount)
                .primaryGuestName(request.primaryGuestName())
                .primaryGuestEmail(request.primaryGuestEmail())
                .primaryGuestPhone(request.primaryGuestPhone())
                .specialRequests(request.specialRequests())
                .nightlyRate(nightlyRate)
                .baseAmount(baseAmount)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .currency(hotel.getCurrency() != null ? hotel.getCurrency() : "INR")
                .status(BookingStatus.CONFIRMED)
                .paymentId("PAY-HTL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paymentStatus("COMPLETED")
                .cancellationPolicy("Free cancellation up to 7 days before check-in (100% refund); 50% refund 24h–7 days; Non-refundable within 24h.")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        HotelBooking saved = hotelBookingRepository.save(booking);
        log.info("Hotel booking created successfully: reference={}, hotel={}, total={}", reference, hotel.getName(), totalAmount);

        return mapToResponse(saved);
    }

    @Override
    public Page<HotelBookingDto.HotelBookingResponse> getUserBookings(String userId, Pageable pageable) {
        return hotelBookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public HotelBookingDto.HotelBookingResponse getBookingById(String bookingId, String userId) {
        HotelBooking booking = hotelBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelBooking", "id", bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new ForbiddenException("Access denied: You do not have permission to view this reservation");
        }
        return mapToResponse(booking);
    }

    @Override
    public HotelBookingDto.HotelBookingResponse getBookingByReference(String reference, String userId) {
        HotelBooking booking = hotelBookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("HotelBooking", "reference", reference));

        if (!booking.getUserId().equals(userId)) {
            throw new ForbiddenException("Access denied: You do not have permission to view this reservation");
        }
        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public HotelBookingDto.HotelBookingResponse cancelBooking(String bookingId, String userId, String reason) {
        HotelBooking booking = hotelBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelBooking", "id", bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new ForbiddenException("Access denied: You do not have permission to cancel this reservation");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        HotelBookingDto.HotelRefundCalculation refund = calculateRefundInternal(booking);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.setCancellationReason(reason != null && !reason.isBlank() ? reason : "Traveler requested cancellation");
        booking.setRefundAmount(refund.refundAmount());
        booking.setUpdatedAt(Instant.now());

        // Restore room inventory
        try {
            hotelService.releaseRoom(booking.getHotelId(), booking.getRoomTypeId(), booking.getRoomCount());
        } catch (Exception e) {
            log.warn("Failed to release room inventory upon cancellation: {}", e.getMessage());
        }

        HotelBooking updated = hotelBookingRepository.save(booking);
        log.info("Hotel reservation cancelled: ref={}, refundAmount={}", booking.getBookingReference(), refund.refundAmount());
        return mapToResponse(updated);
    }

    @Override
    public HotelBookingDto.HotelRefundCalculation calculateRefund(String bookingId, String userId) {
        HotelBooking booking = hotelBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelBooking", "id", bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new ForbiddenException("Access denied: You do not have permission to view refund for this reservation");
        }
        return calculateRefundInternal(booking);
    }

    // ── Internal Helpers ─────────────────────────────────────────────────────────

    private HotelBookingDto.HotelRefundCalculation calculateRefundInternal(HotelBooking booking) {
        LocalDate today = LocalDate.now();
        long daysUntilCheckIn = ChronoUnit.DAYS.between(today, booking.getCheckInDate());

        BigDecimal total = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal refundAmount;
        int percentage;
        String policy;
        String timeline;

        if (daysUntilCheckIn >= 7) {
            percentage = 100;
            refundAmount = total;
            policy = "100% Full Refund (> 7 days before check-in)";
            timeline = "Refund will be credited to original payment method within 3–5 business days.";
        } else if (daysUntilCheckIn >= 1) {
            percentage = 50;
            refundAmount = total.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
            policy = "50% Partial Refund (24h–7 days before check-in)";
            timeline = "Refund of 50% fare will be credited within 3–5 business days.";
        } else {
            percentage = 0;
            refundAmount = BigDecimal.ZERO;
            policy = "Non-refundable (< 24h before check-in)";
            timeline = "No refund is applicable for cancellations within 24 hours of check-in.";
        }

        return new HotelBookingDto.HotelRefundCalculation(
                booking.getBookingReference(),
                total,
                refundAmount,
                percentage,
                policy,
                timeline
        );
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BadRequestException("Check-in and check-out dates are required");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in date cannot be in the past");
        }
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new BadRequestException("Check-out date must be at least 1 day after check-in date");
        }
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > 60) {
            throw new BadRequestException("Reservations cannot exceed 60 consecutive nights");
        }
    }

    private RoomType findRoom(Hotel hotel, String roomTypeId) {
        if (hotel.getRoomTypes() == null || hotel.getRoomTypes().isEmpty()) {
            throw new ResourceNotFoundException("RoomType", "id", roomTypeId);
        }
        return hotel.getRoomTypes().stream()
                .filter(r -> r.getId().equalsIgnoreCase(roomTypeId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", "id", roomTypeId));
    }

    private BigDecimal calculateDiscount(String couponCode, BigDecimal baseAmount) {
        if (couponCode == null || couponCode.isBlank()) return BigDecimal.ZERO;
        String code = couponCode.trim().toUpperCase();
        if (code.equals("SMARTSTAY20") || code.equals("HOTEL20")) {
            return baseAmount.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        }
        if (code.equals("SMARTFLY25") || code.equals("FESTIVE1500")) {
            return new BigDecimal("1500.00").min(baseAmount);
        }
        return BigDecimal.ZERO;
    }

    private String generateBookingReference() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder("HTL-");
            for (int i = 0; i < 6; i++) {
                sb.append(PNR_CHARS.charAt(RANDOM.nextInt(PNR_CHARS.length())));
            }
            String ref = sb.toString();
            if (!hotelBookingRepository.existsByBookingReference(ref)) {
                return ref;
            }
        }
        return "HTL-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private HotelBookingDto.HotelBookingResponse mapToResponse(HotelBooking b) {
        return new HotelBookingDto.HotelBookingResponse(
                b.getId(),
                b.getBookingReference(),
                b.getUserId(),
                b.getUserEmail(),
                b.getHotelId(),
                b.getHotelName(),
                b.getHotelCity(),
                b.getHotelAddress(),
                b.getHotelImageUrl(),
                b.getRoomTypeId(),
                b.getRoomTypeName(),
                b.getRoomCategory(),
                b.getCheckInDate(),
                b.getCheckOutDate(),
                b.getNights(),
                b.getGuestCount(),
                b.getRoomCount(),
                b.getPrimaryGuestName(),
                b.getPrimaryGuestEmail(),
                b.getPrimaryGuestPhone(),
                b.getSpecialRequests(),
                b.getNightlyRate(),
                b.getBaseAmount(),
                b.getTaxAmount(),
                b.getDiscountAmount(),
                b.getTotalAmount(),
                b.getCurrency(),
                b.getStatus(),
                b.getPaymentId(),
                b.getPaymentStatus(),
                b.getCancellationPolicy(),
                b.getCancelledAt(),
                b.getCancellationReason(),
                b.getRefundAmount(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}
