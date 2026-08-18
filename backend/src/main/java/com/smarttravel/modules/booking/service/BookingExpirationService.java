package com.smarttravel.modules.booking.service;

/**
 * Service managing the atomic expiration of overdue, unpaid flight bookings and seat inventory release.
 */
public interface BookingExpirationService {

    /**
     * Scans for and atomically expires all unpaid bookings whose expiration deadline has passed.
     * Releases reserved cabin seats for every successfully expired booking.
     *
     * @return Number of bookings successfully transitioned to EXPIRED
     */
    int expireOverdueBookings();

    /**
     * Atomically expires a single specified booking if it is PENDING and overdue.
     *
     * @param bookingId Booking MongoDB ID
     * @return true if successfully expired and seats released, false otherwise
     */
    boolean expireBooking(String bookingId);
}
