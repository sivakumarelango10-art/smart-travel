package com.smarttravel.modules.booking.mapper;

import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.Passenger;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.model.AirportInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mapper component converting between Booking domain entities and DTOs.
 */
@Component
public class BookingMapper {

    private final com.smarttravel.modules.ticket.repository.TicketRepository ticketRepository;
    private final com.smarttravel.modules.booking.repository.CheckInRepository checkInRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public BookingMapper(
            @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.ticket.repository.TicketRepository ticketRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.booking.repository.CheckInRepository checkInRepository) {
        this.ticketRepository = ticketRepository;
        this.checkInRepository = checkInRepository;
    }

    public BookingMapper() {
        this(null, null);
    }

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

        String ticketId = booking.getTicketId();
        String ticketNumber = booking.getTicketNumber();
        if ((ticketId == null || ticketId.isBlank()) && ticketRepository != null && booking.getId() != null) {
            try {
                com.smarttravel.modules.ticket.model.Ticket ticket = ticketRepository.findFirstByBookingId(booking.getId()).orElse(null);
                if (ticket != null) {
                    ticketId = ticket.getId();
                    ticketNumber = ticket.getTicketNumber();
                }
            } catch (Exception ignored) {}
        }

        boolean checkedIn = booking.isCheckedIn();
        String checkInNumber = booking.getCheckInNumber();
        java.time.Instant checkedInAt = booking.getCheckedInAt();
        if (!checkedIn && checkInRepository != null && booking.getId() != null) {
            try {
                com.smarttravel.modules.booking.model.CheckIn ci = checkInRepository.findByBookingId(booking.getId()).orElse(null);
                if (ci != null) {
                    checkedIn = true;
                    checkInNumber = ci.getCheckInNumber();
                    checkedInAt = ci.getCheckedInAt();
                }
            } catch (Exception ignored) {}
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .userEmail(booking.getUserEmail())
                .flightId(booking.getFlightId())
                .flightNumber(booking.getFlightNumber())
                .airline(booking.getAirline())
                .airlineCode(booking.getAirlineCode())
                .departureAirport(toAirportDto(booking.getDepartureAirport()))
                .arrivalAirport(toAirportDto(booking.getArrivalAirport()))
                .departureTime(booking.getDepartureTime())
                .arrivalTime(booking.getArrivalTime())
                .durationMinutes(booking.getDurationMinutes())
                .cabinClass(booking.getCabinClass())
                .passengerCount(booking.getPassengerCount())
                .passengers(toPassengerDtoList(booking.getPassengers()))
                .fareBreakdown(booking.getFareBreakdown())
                .totalAmount(booking.getTotalAmount())
                .couponCode(booking.getCouponCode())
                .discountAmount(booking.getDiscountAmount())
                .currency(booking.getCurrency())
                .status(booking.getStatus())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .expiresAt(booking.getExpiresAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .ticketId(ticketId)
                .ticketNumber(ticketNumber)
                .checkedIn(checkedIn)
                .checkInNumber(checkInNumber)
                .checkedInAt(checkedInAt)
                .build();
    }

    public Passenger toEntity(PassengerDto dto) {
        if (dto == null) {
            return null;
        }

        return Passenger.builder()
                .passengerId(UUID.randomUUID().toString())
                .title(dto.getTitle() != null ? dto.getTitle().trim() : null)
                .firstName(dto.getFirstName() != null ? dto.getFirstName().trim() : null)
                .lastName(dto.getLastName() != null ? dto.getLastName().trim() : null)
                .dateOfBirth(dto.getDateOfBirth() != null ? dto.getDateOfBirth() : LocalDate.of(1995, 1, 1))
                .gender(dto.getGender() != null ? dto.getGender().trim().toUpperCase() : null)
                .nationality(dto.getNationality() != null ? dto.getNationality().trim() : null)
                .passportNumber(dto.getPassportNumber() != null ? dto.getPassportNumber().trim().toUpperCase() : null)
                .seatNumber(dto.getSeatNumber() != null ? dto.getSeatNumber().trim().toUpperCase() : null)
                .build();
    }

    public List<Passenger> toEntityList(List<PassengerDto> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        return dtos.stream().map(this::toEntity).toList();
    }

    public PassengerDto toPassengerDto(Passenger entity) {
        if (entity == null) {
            return null;
        }

        return PassengerDto.builder()
                .title(entity.getTitle())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .nationality(entity.getNationality())
                .passportNumber(entity.getPassportNumber())
                .seatNumber(entity.getSeatNumber())
                .build();
    }

    public List<PassengerDto> toPassengerDtoList(List<Passenger> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::toPassengerDto).toList();
    }

    private AirportDto toAirportDto(AirportInfo info) {
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
}
