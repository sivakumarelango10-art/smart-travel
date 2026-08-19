package com.smarttravel.modules.flight.service;

import com.smarttravel.modules.flight.dto.SeatDto;
import com.smarttravel.modules.flight.dto.SeatMapResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for Flight Seat Maps, Availability, and Reservation Lifecycle.
 */
public interface SeatMapService {

    SeatMapResponse getFlightSeatMap(String flightId);

    List<SeatDto> getSeatsForFlight(String flightId, CabinClass cabinClass);

    void initializeSeatsForFlight(Flight flight);

    boolean holdSeats(String flightId, CabinClass cabinClass, List<String> seatNumbers,
                      String bookingId, String bookingReference, Instant expiresAt);

    void confirmSeats(String bookingId);

    void releaseSeats(String bookingId);

    void releaseExpiredHolds();
}
