package com.smarttravel.modules.hotel.service;

import com.smarttravel.modules.hotel.dto.HotelBookingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HotelBookingService {

    HotelBookingDto.PriceCalculateResponse calculatePrice(HotelBookingDto.PriceCalculateRequest request);

    HotelBookingDto.HotelBookingResponse createBooking(HotelBookingDto.CreateHotelBookingRequest request, String userId, String userEmail);

    Page<HotelBookingDto.HotelBookingResponse> getUserBookings(String userId, Pageable pageable);

    HotelBookingDto.HotelBookingResponse getBookingById(String bookingId, String userId);

    HotelBookingDto.HotelBookingResponse getBookingByReference(String reference, String userId);

    HotelBookingDto.HotelBookingResponse cancelBooking(String bookingId, String userId, String reason);

    HotelBookingDto.HotelRefundCalculation calculateRefund(String bookingId, String userId);
}
