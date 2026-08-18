package com.smarttravel.modules.booking.repository;

import com.smarttravel.modules.flight.model.CabinClass;

/**
 * Repository interface for atomic database-level cabin seat inventory deduction and release.
 */
public interface FlightInventoryReservationRepository {

    /**
     * Atomically reserves the requested number of seats in the specified cabin tier.
     * Guarantees concurrency safety without race conditions or overselling.
     *
     * @param flightId   The Flight MongoDB ID
     * @param cabinClass The target cabin tier
     * @param seatCount  Number of seats to reserve (1 to 9)
     * @return true if atomic reservation succeeded; false if insufficient availability, inactive, or invalid flight
     */
    boolean reserveCabinSeats(String flightId, CabinClass cabinClass, int seatCount);

    /**
     * Atomically releases previously reserved seats back into the specified cabin inventory.
     *
     * @param flightId   The Flight MongoDB ID
     * @param cabinClass The target cabin tier
     * @param seatCount  Number of seats to release back
     * @return true if atomic release succeeded; false otherwise
     */
    boolean releaseCabinSeats(String flightId, CabinClass cabinClass, int seatCount);
}
