package com.smarttravel.modules.flight.repository;

import java.time.Instant;


/**
 * Custom repository interface for atomic MongoDB seat operations.
 */
public interface SeatRepositoryCustom {

    boolean atomicHoldSeat(String flightId, String seatNumber, String bookingId, String bookingReference, Instant expiresAt);

    boolean atomicConfirmSeat(String flightId, String seatNumber, String bookingId);

    long confirmSeatsForBooking(String bookingId);

    long releaseSeatsForBooking(String bookingId);

    long releaseExpiredSeatHolds(Instant now);
}
