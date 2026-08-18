package com.smarttravel.modules.flight.mapper;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightUpdateRequest;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Mapper utility for Flight entities and DTO conversions.
 */
public final class FlightMapper {

    private FlightMapper() {
    }

    public static Flight toEntity(FlightCreateRequest request) {
        if (request == null) {
            return null;
        }

        validateFlightTimes(request.getDepartureTime(), request.getArrivalTime());
        int duration = (int) Duration.between(request.getDepartureTime(), request.getArrivalTime()).toMinutes();

        Set<CabinClass> cabinClasses = request.getCabinClasses();
        if (cabinClasses == null || cabinClasses.isEmpty()) {
            cabinClasses = Set.of(CabinClass.ECONOMY);
        }

        int availableSeats = request.getAvailableSeats() != null ? request.getAvailableSeats() : request.getTotalSeats();

        return Flight.builder()
                .flightNumber(request.getFlightNumber().toUpperCase().trim())
                .airline(request.getAirline().trim())
                .airlineCode(request.getAirlineCode().toUpperCase().trim())
                .departureAirport(toAirportInfo(request.getDepartureAirport()))
                .arrivalAirport(toAirportInfo(request.getArrivalAirport()))
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .durationMinutes(duration)
                .aircraftModel(request.getAircraftModel().trim())
                .basePrice(request.getBasePrice())
                .totalSeats(request.getTotalSeats())
                .availableSeats(availableSeats)
                .cabinClasses(new HashSet<>(cabinClasses))
                .status(request.getStatus() != null ? request.getStatus() : FlightStatus.SCHEDULED)
                .active(true)
                .build();
    }

    public static void updateEntity(Flight flight, FlightUpdateRequest request) {
        if (flight == null || request == null) {
            return;
        }

        if (request.getAirline() != null && !request.getAirline().isBlank()) {
            flight.setAirline(request.getAirline().trim());
        }
        if (request.getAirlineCode() != null && !request.getAirlineCode().isBlank()) {
            flight.setAirlineCode(request.getAirlineCode().toUpperCase().trim());
        }
        if (request.getDepartureAirport() != null) {
            flight.setDepartureAirport(toAirportInfo(request.getDepartureAirport()));
        }
        if (request.getArrivalAirport() != null) {
            flight.setArrivalAirport(toAirportInfo(request.getArrivalAirport()));
        }

        Instant newDeparture = request.getDepartureTime() != null ? request.getDepartureTime() : flight.getDepartureTime();
        Instant newArrival = request.getArrivalTime() != null ? request.getArrivalTime() : flight.getArrivalTime();

        if (request.getDepartureTime() != null || request.getArrivalTime() != null) {
            validateFlightTimes(newDeparture, newArrival);
            flight.setDepartureTime(newDeparture);
            flight.setArrivalTime(newArrival);
            flight.setDurationMinutes((int) Duration.between(newDeparture, newArrival).toMinutes());
        }

        if (request.getAircraftModel() != null && !request.getAircraftModel().isBlank()) {
            flight.setAircraftModel(request.getAircraftModel().trim());
        }
        if (request.getBasePrice() != null) {
            flight.setBasePrice(request.getBasePrice());
        }
        if (request.getTotalSeats() != null) {
            flight.setTotalSeats(request.getTotalSeats());
        }
        if (request.getAvailableSeats() != null) {
            flight.setAvailableSeats(request.getAvailableSeats());
        }
        if (request.getCabinClasses() != null && !request.getCabinClasses().isEmpty()) {
            flight.setCabinClasses(new HashSet<>(request.getCabinClasses()));
        }
        if (request.getStatus() != null) {
            flight.setStatus(request.getStatus());
        }
        if (request.getActive() != null) {
            flight.setActive(request.getActive());
        }
    }

    public static FlightResponse toResponse(Flight flight) {
        if (flight == null) {
            return null;
        }

        return FlightResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .airlineCode(flight.getAirlineCode())
                .departureAirport(toAirportDto(flight.getDepartureAirport()))
                .arrivalAirport(toAirportDto(flight.getArrivalAirport()))
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .durationMinutes(flight.getDurationMinutes())
                .aircraftModel(flight.getAircraftModel())
                .basePrice(flight.getBasePrice())
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .cabinClasses(flight.getCabinClasses() != null ? flight.getCabinClasses() : Collections.emptySet())
                .status(flight.getStatus())
                .delayMinutes(flight.getDelayMinutes())
                .delayReason(flight.getDelayReason())
                .revisedDepartureTime(flight.getRevisedDepartureTime())
                .estimatedArrival(flight.getEstimatedArrival())
                .lastStatusUpdated(flight.getLastStatusUpdated())
                .active(flight.isActive())
                .createdAt(flight.getCreatedAt())
                .updatedAt(flight.getUpdatedAt())
                .build();
    }

    public static AirportInfo toAirportInfo(AirportDto dto) {
        if (dto == null) {
            return null;
        }
        return AirportInfo.builder()
                .code(dto.getCode() != null ? dto.getCode().toUpperCase().trim() : null)
                .name(dto.getName() != null ? dto.getName().trim() : null)
                .city(dto.getCity() != null ? dto.getCity().trim() : null)
                .country(dto.getCountry() != null ? dto.getCountry().trim() : null)
                .terminal(dto.getTerminal() != null ? dto.getTerminal().trim() : null)
                .gate(dto.getGate() != null ? dto.getGate().trim() : null)
                .build();
    }

    public static AirportDto toAirportDto(AirportInfo info) {
        if (info == null) {
            return null;
        }
        return AirportDto.builder()
                .code(info.getCode())
                .name(info.getName())
                .city(info.getCity())
                .country(info.getCountry())
                .terminal(info.getTerminal())
                .gate(info.getGate())
                .build();
    }

    public static void validateFlightTimes(Instant departureTime, Instant arrivalTime) {
        if (departureTime == null || arrivalTime == null) {
            throw new BadRequestException("Departure time and arrival time must not be null");
        }
        if (!arrivalTime.isAfter(departureTime)) {
            throw new BadRequestException("Arrival time (" + arrivalTime + ") must be after departure time (" + departureTime + ")");
        }
        long minutes = Duration.between(departureTime, arrivalTime).toMinutes();
        if (minutes < 15) {
            throw new BadRequestException("Flight duration must be at least 15 minutes");
        }
    }
}
