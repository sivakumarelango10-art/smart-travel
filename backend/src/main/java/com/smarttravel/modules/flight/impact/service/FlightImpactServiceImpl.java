package com.smarttravel.modules.flight.impact.service;

import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.repository.CheckInRepository;
import com.smarttravel.modules.flight.impact.dto.FlightImpactSummaryDto;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of FlightImpactService calculating affected passenger metrics and isolating confirmed bookings.
 */
@Service
public class FlightImpactServiceImpl implements FlightImpactService {

    private static final Logger log = LoggerFactory.getLogger(FlightImpactServiceImpl.class);

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final CheckInRepository checkInRepository;

    public FlightImpactServiceImpl(FlightRepository flightRepository,
                                   BookingRepository bookingRepository,
                                   CheckInRepository checkInRepository) {
        this.flightRepository = flightRepository;
        this.bookingRepository = bookingRepository;
        this.checkInRepository = checkInRepository;
    }

    @Override
    public FlightImpactSummaryDto getDisruptionImpactSummary(String flightId) {
        log.debug("Assessing disruption impact summary for flight ID: {}", flightId);

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        List<Booking> allBookings = bookingRepository.findByFlightId(flightId);

        int confirmedCount = 0;
        int checkedInCount = 0;
        int pendingCount = 0;
        int totalPassengers = 0;
        List<String> affectedIds = new ArrayList<>();

        for (Booking booking : allBookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                confirmedCount++;
                int paxCount = booking.getPassengers() != null ? booking.getPassengers().size() : 1;
                totalPassengers += paxCount;
                affectedIds.add(booking.getId());

                if (checkInRepository.existsByBookingId(booking.getId())) {
                    checkedInCount++;
                }
            } else if (booking.getStatus() == BookingStatus.PENDING) {
                pendingCount++;
            }
        }

        return FlightImpactSummaryDto.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .totalAffectedBookings(confirmedCount)
                .totalAffectedPassengers(totalPassengers)
                .confirmedBookingsCount(confirmedCount)
                .checkedInBookingsCount(checkedInCount)
                .pendingBookingsCount(pendingCount)
                .affectedBookingIds(affectedIds)
                .build();
    }

    @Override
    public List<Booking> getAffectedConfirmedBookings(String flightId) {
        return bookingRepository.findByFlightIdAndStatus(flightId, BookingStatus.CONFIRMED);
    }
}
