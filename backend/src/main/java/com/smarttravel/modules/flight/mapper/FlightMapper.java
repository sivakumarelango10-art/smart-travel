package com.smarttravel.modules.flight.mapper;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.CabinSelectionResponse;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightUpdateRequest;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.service.FareCalculationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapper utility for Flight entities, CabinInventories, and DTO conversions.
 */
public final class FlightMapper {

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.12");
    private static final BigDecimal DEFAULT_FEE = new BigDecimal("150.00");

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

        List<CabinInventory> cabinInventories = new ArrayList<>();
        if (request.getCabinInventories() != null && !request.getCabinInventories().isEmpty()) {
            for (CabinInventoryDto dto : request.getCabinInventories()) {
                cabinInventories.add(toCabinInventory(dto));
            }
        } else {
            // Generate default cabin inventory based on basePrice and seats
            BigDecimal tax = request.getBasePrice().multiply(DEFAULT_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = request.getBasePrice().add(tax).add(DEFAULT_FEE);
            for (CabinClass cc : cabinClasses) {
                cabinInventories.add(CabinInventory.builder()
                        .cabinClass(cc)
                        .totalSeats(request.getTotalSeats())
                        .availableSeats(availableSeats)
                        .basePrice(request.getBasePrice())
                        .taxAmount(tax)
                        .feeAmount(DEFAULT_FEE)
                        .totalPrice(total)
                        .build());
            }
        }

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
                .cabinInventories(cabinInventories)
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
        if (request.getCabinInventories() != null && !request.getCabinInventories().isEmpty()) {
            List<CabinInventory> inventories = request.getCabinInventories().stream()
                    .map(FlightMapper::toCabinInventory)
                    .toList();
            flight.setCabinInventories(new ArrayList<>(inventories));
        }
        if (request.getStatus() != null) {
            flight.setStatus(request.getStatus());
        }
        if (request.getActive() != null) {
            flight.setActive(request.getActive());
        }
    }

    public static FlightResponse toResponse(Flight flight) {
        return toResponse(flight, null, 1, null);
    }

    public static FlightResponse toResponse(Flight flight, CabinClass selectedCabinClass, int passengerCount, FareCalculationService fareCalculationService) {
        if (flight == null) {
            return null;
        }

        List<CabinInventoryDto> inventoryDtos = new ArrayList<>();
        if (flight.getCabinInventories() != null && !flight.getCabinInventories().isEmpty()) {
            for (CabinInventory inv : flight.getCabinInventories()) {
                inventoryDtos.add(toCabinInventoryDto(inv));
            }
        } else {
            // Fallback for legacy documents
            BigDecimal base = flight.getBasePrice() != null ? flight.getBasePrice() : new BigDecimal("5000.00");
            BigDecimal tax = base.multiply(DEFAULT_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = base.add(tax).add(DEFAULT_FEE);
            inventoryDtos.add(CabinInventoryDto.builder()
                    .cabinClass(CabinClass.ECONOMY)
                    .totalSeats(flight.getTotalSeats())
                    .availableSeats(flight.getAvailableSeats())
                    .basePrice(base)
                    .taxAmount(tax)
                    .feeAmount(DEFAULT_FEE)
                    .totalPrice(total)
                    .build());
        }

        CabinSelectionResponse selectedCabin = null;
        if (selectedCabinClass != null && fareCalculationService != null) {
            CabinInventory matched = null;
            if (flight.getCabinInventories() != null) {
                matched = flight.getCabinInventories().stream()
                        .filter(i -> i.getCabinClass() == selectedCabinClass)
                        .findFirst()
                        .orElse(null);
            }
            if (matched == null && !inventoryDtos.isEmpty()) {
                CabinInventoryDto dto = inventoryDtos.get(0);
                matched = toCabinInventory(dto);
            }
            if (matched != null) {
                selectedCabin = fareCalculationService.buildCabinSelectionResponse(matched, passengerCount);
            }
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
                .cabinInventories(inventoryDtos)
                .selectedCabin(selectedCabin)
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

    public static CabinInventory toCabinInventory(CabinInventoryDto dto) {
        if (dto == null) {
            return null;
        }
        BigDecimal tax = dto.getTaxAmount() != null ? dto.getTaxAmount() : dto.getBasePrice().multiply(DEFAULT_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = dto.getFeeAmount() != null ? dto.getFeeAmount() : DEFAULT_FEE;
        BigDecimal total = dto.getTotalPrice() != null ? dto.getTotalPrice() : dto.getBasePrice().add(tax).add(fee);

        return CabinInventory.builder()
                .cabinClass(dto.getCabinClass() != null ? dto.getCabinClass() : CabinClass.ECONOMY)
                .totalSeats(dto.getTotalSeats())
                .availableSeats(dto.getAvailableSeats())
                .basePrice(dto.getBasePrice())
                .taxAmount(tax)
                .feeAmount(fee)
                .totalPrice(total)
                .build();
    }

    public static CabinInventoryDto toCabinInventoryDto(CabinInventory model) {
        if (model == null) {
            return null;
        }
        return CabinInventoryDto.builder()
                .cabinClass(model.getCabinClass())
                .totalSeats(model.getTotalSeats())
                .availableSeats(model.getAvailableSeats())
                .basePrice(model.getBasePrice())
                .taxAmount(model.getTaxAmount())
                .feeAmount(model.getFeeAmount())
                .totalPrice(model.getTotalPrice())
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
