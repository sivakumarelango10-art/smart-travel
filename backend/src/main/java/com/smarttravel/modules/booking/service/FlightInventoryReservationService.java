package com.smarttravel.modules.booking.service;

import com.smarttravel.modules.flight.model.CabinClass;

/**
 * Service managing atomic cabin inventory reservations and releases.
 */
public interface FlightInventoryReservationService {

    /**
     * Atomically reserves seats in the given flight and cabin.
     *
     * @param flightId   Flight ID
     * @param cabinClass Cabin class tier
     * @param seatCount  Number of seats to reserve
     * @return true if successful, false if insufficient availability or non-bookable
     */
    boolean reserveSeats(String flightId, CabinClass cabinClass, int seatCount);

    /**
     * Atomically releases seats back into the given flight and cabin inventory.
     *
     * @param flightId   Flight ID
     * @param cabinClass Cabin class tier
     * @param seatCount  Number of seats to release
     * @return true if successful, false otherwise
     */
    boolean releaseSeats(String flightId, CabinClass cabinClass, int seatCount);
}
