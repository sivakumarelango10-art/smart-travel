package com.smarttravel.modules.ticket.mapper;

import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.ticket.dto.PassengerTicketResponse;
import com.smarttravel.modules.ticket.dto.TicketResponse;
import com.smarttravel.modules.ticket.model.PassengerTicketInfo;
import com.smarttravel.modules.ticket.model.Ticket;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Ticket entity models and API response DTOs.
 */
@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        List<PassengerTicketResponse> passengerResponses = ticket.getPassengers() != null
                ? ticket.getPassengers().stream().map(this::toPassengerResponse).collect(Collectors.toList())
                : new ArrayList<>();

        String pdfUrl = ticket.getId() != null ? "/api/v1/tickets/" + ticket.getId() + "/pdf" : null;

        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .bookingId(ticket.getBookingId())
                .bookingReference(ticket.getBookingReference())
                .userId(ticket.getUserId())
                .userEmail(ticket.getUserEmail())
                .flightId(ticket.getFlightId())
                .flightNumber(ticket.getFlightNumber())
                .airline(ticket.getAirline())
                .airlineCode(ticket.getAirlineCode())
                .aircraftModel(ticket.getAircraftModel())
                .departureAirport(toAirportDto(ticket.getDepartureAirport()))
                .arrivalAirport(toAirportDto(ticket.getArrivalAirport()))
                .departureTime(ticket.getDepartureTime())
                .arrivalTime(ticket.getArrivalTime())
                .durationMinutes(ticket.getDurationMinutes())
                .cabinClass(ticket.getCabinClass())
                .passengerCount(ticket.getPassengerCount())
                .passengers(passengerResponses)
                .fareBreakdown(ticket.getFareBreakdown())
                .totalAmount(ticket.getTotalAmount())
                .currency(ticket.getCurrency())
                .status(ticket.getStatus())
                .paymentId(ticket.getPaymentId())
                .razorpayPaymentId(ticket.getRazorpayPaymentId())
                .issuedAt(ticket.getIssuedAt())
                .cancelledAt(ticket.getCancelledAt())
                .cancellationReason(ticket.getCancellationReason())
                .pdfDownloadUrl(pdfUrl)
                .build();
    }

    public PassengerTicketResponse toPassengerResponse(PassengerTicketInfo passenger) {
        if (passenger == null) {
            return null;
        }
        return PassengerTicketResponse.builder()
                .title(passenger.getTitle())
                .firstName(passenger.getFirstName())
                .lastName(passenger.getLastName())
                .dateOfBirth(passenger.getDateOfBirth())
                .gender(passenger.getGender())
                .nationality(passenger.getNationality())
                .seatNumber(passenger.getSeatNumber())
                .eTicketNumber(passenger.getETicketNumber())
                .build();
    }

    public AirportDto toAirportDto(AirportInfo info) {
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
