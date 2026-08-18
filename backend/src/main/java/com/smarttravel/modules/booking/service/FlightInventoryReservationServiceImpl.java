package com.smarttravel.modules.booking.service;

import com.smarttravel.modules.booking.repository.FlightInventoryReservationRepository;
import com.smarttravel.modules.flight.model.CabinClass;
import org.springframework.stereotype.Service;

/**
 * Implementation of FlightInventoryReservationService delegating to atomic repository operations.
 */
@Service
public class FlightInventoryReservationServiceImpl implements FlightInventoryReservationService {

    private final FlightInventoryReservationRepository reservationRepository;

    public FlightInventoryReservationServiceImpl(FlightInventoryReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public boolean reserveSeats(String flightId, CabinClass cabinClass, int seatCount) {
        return reservationRepository.reserveCabinSeats(flightId, cabinClass, seatCount);
    }

    @Override
    public boolean releaseSeats(String flightId, CabinClass cabinClass, int seatCount) {
        return reservationRepository.releaseCabinSeats(flightId, cabinClass, seatCount);
    }
}
