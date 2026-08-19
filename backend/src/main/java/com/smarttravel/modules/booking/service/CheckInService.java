package com.smarttravel.modules.booking.service;

import com.smarttravel.modules.booking.dto.BoardingPassResponse;
import com.smarttravel.modules.booking.dto.CheckInRequest;
import com.smarttravel.modules.booking.dto.CheckInResponse;

import java.util.List;

/**
 * Service managing passenger online check-in and boarding pass lifecycle.
 */
public interface CheckInService {

    CheckInResponse performCheckIn(String bookingId, CheckInRequest request, String userId, boolean isAdmin);

    CheckInResponse getCheckInByBookingId(String bookingId, String userId, boolean isAdmin);

    List<BoardingPassResponse> getBoardingPasses(String bookingId, String userId, boolean isAdmin);

    byte[] getBoardingPassPdf(String bookingId, String userId, boolean isAdmin);
}
