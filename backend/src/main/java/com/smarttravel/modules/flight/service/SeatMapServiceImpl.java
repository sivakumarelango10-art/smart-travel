package com.smarttravel.modules.flight.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.flight.config.AircraftSeatLayout;
import com.smarttravel.modules.flight.dto.SeatDto;
import com.smarttravel.modules.flight.dto.SeatMapResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.Seat;
import com.smarttravel.modules.flight.model.SeatStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of SeatMapService managing aircraft seating layouts and atomic reservation operations.
 */
@Service
public class SeatMapServiceImpl implements SeatMapService {

    private static final Logger log = LoggerFactory.getLogger(SeatMapServiceImpl.class);

    private final SeatRepository seatRepository;
    private final FlightRepository flightRepository;
    private final AircraftSeatLayout aircraftSeatLayout;

    public SeatMapServiceImpl(SeatRepository seatRepository,
                              FlightRepository flightRepository,
                              AircraftSeatLayout aircraftSeatLayout) {
        this.seatRepository = seatRepository;
        this.flightRepository = flightRepository;
        this.aircraftSeatLayout = aircraftSeatLayout;
    }

    @Override
    public SeatMapResponse getFlightSeatMap(String flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        ensureSeatsInitialized(flight);

        List<Seat> seats = seatRepository.findByFlightIdOrderByRowNumberAscColumnAsc(flightId);
        List<SeatDto> seatDtos = seats.stream().map(this::toDto).collect(Collectors.toList());

        int availableCount = (int) seats.stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE || (s.getStatus() == SeatStatus.HELD && s.getExpiresAt() != null && s.getExpiresAt().isBefore(Instant.now())))
                .count();

        Map<CabinClass, List<SeatDto>> cabinSeats = new LinkedHashMap<>();
        for (CabinClass cc : CabinClass.values()) {
            List<SeatDto> list = seatDtos.stream()
                    .filter(s -> s.getCabinClass() == cc)
                    .collect(Collectors.toList());
            if (!list.isEmpty()) {
                cabinSeats.put(cc, list);
            }
        }

        return SeatMapResponse.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .aircraftModel(flight.getAircraftModel())
                .totalSeats(seats.size())
                .availableSeatsCount(availableCount)
                .seats(seatDtos)
                .cabinSeats(cabinSeats)
                .build();
    }

    @Override
    public List<SeatDto> getSeatsForFlight(String flightId, CabinClass cabinClass) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        ensureSeatsInitialized(flight);

        List<Seat> seats = cabinClass != null
                ? seatRepository.findByFlightIdAndCabinClassOrderByRowNumberAscColumnAsc(flightId, cabinClass)
                : seatRepository.findByFlightIdOrderByRowNumberAscColumnAsc(flightId);

        return seats.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public synchronized void initializeSeatsForFlight(Flight flight) {
        if (seatRepository.existsByFlightId(flight.getId())) {
            return;
        }
        log.info("Generating physical seat map for flight ID: {}, Number: {}, Model: {}",
                flight.getId(), flight.getFlightNumber(), flight.getAircraftModel());

        Set<CabinClass> supportedCabins = flight.getCabinClasses();
        int totalSeats = flight.getTotalSeats();
        List<Seat> seats = aircraftSeatLayout.generateSeatsForFlight(
                flight.getId(), flight.getFlightNumber(), flight.getAircraftModel(), supportedCabins, totalSeats);

        if (!seats.isEmpty()) {
            seatRepository.saveAll(seats);
            log.info("Successfully persisted {} seats for flight ID: {}", seats.size(), flight.getId());
        }
    }

    @Override
    public boolean holdSeats(String flightId, CabinClass cabinClass, List<String> seatNumbers,
                             String bookingId, String bookingReference, Instant expiresAt) {
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            return true;
        }

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));
        ensureSeatsInitialized(flight);

        List<String> successfullyHeld = new ArrayList<>();

        for (String seatNumber : seatNumbers) {
            Seat seat = seatRepository.findByFlightIdAndSeatNumber(flightId, seatNumber)
                    .orElseThrow(() -> new BadRequestException("Seat " + seatNumber + " does not exist on flight " + flight.getFlightNumber()));

            if (cabinClass != null && seat.getCabinClass() != cabinClass) {
                log.warn("Seat {} belongs to cabin {}, but booking requested cabin {}", seatNumber, seat.getCabinClass(), cabinClass);
                rollbackHeldSeats(flightId, successfullyHeld);
                throw new BadRequestException("Seat " + seatNumber + " belongs to cabin " + seat.getCabinClass() + ", not " + cabinClass);
            }

            if (seat.getStatus() == SeatStatus.BLOCKED) {
                log.warn("Seat {} is BLOCKED on flight {}", seatNumber, flightId);
                rollbackHeldSeats(flightId, successfullyHeld);
                throw new ConflictException("Seat " + seatNumber + " is blocked and cannot be reserved");
            }

            boolean held = seatRepository.atomicHoldSeat(flightId, seatNumber, bookingId, bookingReference, expiresAt);
            if (!held) {
                log.warn("Atomic hold failed for seat {} on flight {}", seatNumber, flightId);
                rollbackHeldSeats(flightId, successfullyHeld);
                throw new ConflictException("Seat " + seatNumber + " is no longer available");
            }

            successfullyHeld.add(seatNumber);
        }

        return true;
    }

    @Override
    public void confirmSeats(String bookingId) {
        seatRepository.confirmSeatsForBooking(bookingId);
    }

    @Override
    public void releaseSeats(String bookingId) {
        seatRepository.releaseSeatsForBooking(bookingId);
    }

    @Override
    public void releaseExpiredHolds() {
        seatRepository.releaseExpiredSeatHolds(Instant.now());
    }

    private void ensureSeatsInitialized(Flight flight) {
        if (!seatRepository.existsByFlightId(flight.getId())) {
            initializeSeatsForFlight(flight);
        }
    }

    private void rollbackHeldSeats(String flightId, List<String> seatNumbers) {
        if (seatNumbers == null || seatNumbers.isEmpty()) return;
        for (String seatNumber : seatNumbers) {
            try {
                Seat seat = seatRepository.findByFlightIdAndSeatNumber(flightId, seatNumber).orElse(null);
                if (seat != null && seat.getStatus() == SeatStatus.HELD) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setBookingId(null);
                    seat.setBookingReference(null);
                    seat.setHeldAt(null);
                    seat.setExpiresAt(null);
                    seat.setUpdatedAt(Instant.now());
                    seatRepository.save(seat);
                }
            } catch (Exception ex) {
                log.error("Error during compensating seat release for seat: {} on flight: {}", seatNumber, flightId, ex);
            }
        }
    }

    private SeatDto toDto(Seat seat) {
        SeatStatus effectiveStatus = seat.getStatus();
        if (effectiveStatus == SeatStatus.HELD && seat.getExpiresAt() != null && seat.getExpiresAt().isBefore(Instant.now())) {
            effectiveStatus = SeatStatus.AVAILABLE;
        }

        return SeatDto.builder()
                .seatNumber(seat.getSeatNumber())
                .rowNumber(seat.getRowNumber())
                .column(seat.getColumn())
                .cabinClass(seat.getCabinClass())
                .status(effectiveStatus)
                .priceAdjustment(seat.getPriceAdjustment())
                .build();
    }
}
