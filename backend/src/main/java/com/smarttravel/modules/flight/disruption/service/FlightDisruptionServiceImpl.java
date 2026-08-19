package com.smarttravel.modules.flight.disruption.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingStateMachine;
import com.smarttravel.modules.flight.config.AircraftSeatLayout;
import com.smarttravel.modules.flight.disruption.dto.FlightAircraftChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightCancelRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightDisruptionDto;
import com.smarttravel.modules.flight.disruption.dto.FlightGateChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightOperationalStatusResponse;
import com.smarttravel.modules.flight.disruption.dto.FlightScheduleChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightTerminalChangeRequest;
import com.smarttravel.modules.flight.disruption.model.DisruptionStatus;
import com.smarttravel.modules.flight.disruption.model.DisruptionType;
import com.smarttravel.modules.flight.disruption.model.FlightDisruption;
import com.smarttravel.modules.flight.disruption.repository.FlightDisruptionRepository;
import com.smarttravel.modules.flight.impact.service.FlightImpactService;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.model.FlightStatusHistory;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.FlightStatusHistoryRepository;
import com.smarttravel.modules.flight.service.FlightStateMachine;
import com.smarttravel.modules.flight.service.SeatMapService;

import com.smarttravel.modules.notification.dto.NotificationSendRequest;
import com.smarttravel.modules.notification.model.NotificationChannel;
import com.smarttravel.modules.notification.model.NotificationType;
import com.smarttravel.modules.notification.service.NotificationService;
import com.smarttravel.modules.payment.refund.model.RefundReason;
import com.smarttravel.modules.payment.refund.service.RefundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Production-ready implementation of FlightDisruptionService providing flight operations,
 * disruption history logging, customer alerts, and automated compensation handling.
 */
@Service
public class FlightDisruptionServiceImpl implements FlightDisruptionService {

    private static final Logger log = LoggerFactory.getLogger(FlightDisruptionServiceImpl.class);

    private final FlightRepository flightRepository;
    private final FlightDisruptionRepository disruptionRepository;
    private final FlightStatusHistoryRepository statusHistoryRepository;
    private final BookingRepository bookingRepository;
    private final FlightStateMachine flightStateMachine;
    private final BookingStateMachine bookingStateMachine;
    private final FlightImpactService flightImpactService;
    private final RefundService refundService;
    private final NotificationService notificationService;
    private final SeatMapService seatMapService;

    public FlightDisruptionServiceImpl(FlightRepository flightRepository,
                                      FlightDisruptionRepository disruptionRepository,
                                      FlightStatusHistoryRepository statusHistoryRepository,
                                      BookingRepository bookingRepository,
                                      FlightStateMachine flightStateMachine,
                                      BookingStateMachine bookingStateMachine,
                                      FlightImpactService flightImpactService,
                                      RefundService refundService,
                                      NotificationService notificationService,
                                      SeatMapService seatMapService) {
        this.flightRepository = flightRepository;
        this.disruptionRepository = disruptionRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.bookingRepository = bookingRepository;
        this.flightStateMachine = flightStateMachine;
        this.bookingStateMachine = bookingStateMachine;
        this.flightImpactService = flightImpactService;
        this.refundService = refundService;
        this.notificationService = notificationService;
        this.seatMapService = seatMapService;
    }

    @Override
    @Transactional
    public FlightOperationalStatusResponse rescheduleFlight(String flightId, FlightScheduleChangeRequest request, String adminUser) {
        log.info("Admin '{}' rescheduling flight ID: {} (New Dep: {}, New Arr: {})",
                adminUser, flightId, request.getNewDepartureTime(), request.getNewArrivalTime());

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        if (!request.getNewDepartureTime().isBefore(request.getNewArrivalTime())) {
            throw new BadRequestException("New arrival time must be strictly after new departure time");
        }

        Instant previousDep = flight.getRevisedDepartureTime() != null ? flight.getRevisedDepartureTime() : flight.getDepartureTime();
        Instant previousArr = flight.getEstimatedArrival() != null ? flight.getEstimatedArrival() : flight.getArrivalTime();

        // 1. Update Flight Operational Fields (Preserve original published schedule immutable)
        flight.setRevisedDepartureTime(request.getNewDepartureTime());
        flight.setEstimatedArrival(request.getNewArrivalTime());
        flight.setOperationalNotes(request.getDescription());
        flight.setLastOperationalUpdate(Instant.now());
        Flight savedFlight = flightRepository.save(flight);

        // 2. Persist Disruption Record
        FlightDisruption disruption = FlightDisruption.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .disruptionType(DisruptionType.RESCHEDULE)
                .reason(request.getReason())
                .description(request.getDescription())
                .previousDepartureTime(previousDep)
                .newDepartureTime(request.getNewDepartureTime())
                .previousArrivalTime(previousArr)
                .newArrivalTime(request.getNewArrivalTime())
                .status(DisruptionStatus.ACTIVE)
                .createdBy(adminUser)
                .createdAt(Instant.now())
                .build();
        disruption = disruptionRepository.save(disruption);

        // 3. Notify Affected Confirmed Passengers
        notifyAffectedPassengers(savedFlight, disruption, NotificationType.FLIGHT_RESCHEDULED,
                String.format("Flight %s Rescheduled: New Departure at %s", savedFlight.getFlightNumber(), request.getNewDepartureTime()),
                String.format("Dear Passenger,\n\nYour flight %s has been rescheduled.\nOriginal Departure: %s\nNew Revised Departure: %s\nReason: %s\n\nThank you for choosing SmartTravel.",
                        savedFlight.getFlightNumber(), previousDep, request.getNewDepartureTime(), request.getReason()));

        return buildOperationalStatusResponse(savedFlight);
    }

    @Override
    @Transactional
    public synchronized FlightOperationalStatusResponse cancelFlight(String flightId, FlightCancelRequest request, String adminUser) {
        log.info("Admin '{}' cancelling flight ID: {} (Reason: {})", adminUser, flightId, request.getReason());

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        // Idempotency: If flight is already cancelled, return operational status
        if (flight.getStatus() == FlightStatus.CANCELLED) {
            log.info("Flight ID: {} is already cancelled. Returning current operational status.", flightId);
            return buildOperationalStatusResponse(flight);
        }

        // 1. Validate State Machine Transition
        flightStateMachine.validateTransition(flight.getStatus(), FlightStatus.CANCELLED);

        // 2. Update Flight to CANCELLED
        flight.setStatus(FlightStatus.CANCELLED);
        flight.setDelayReason(request.getReason());
        flight.setOperationalNotes(request.getDescription());
        flight.setLastOperationalUpdate(Instant.now());
        flight.setLastStatusUpdated(Instant.now());
        Flight savedFlight = flightRepository.save(flight);

        // Record status history audit
        FlightStatusHistory history = FlightStatusHistory.builder()
                .flightId(savedFlight.getId())
                .flightNumber(savedFlight.getFlightNumber())
                .previousStatus(flight.getStatus())
                .newStatus(FlightStatus.CANCELLED)
                .delayMinutes(savedFlight.getDelayMinutes())
                .delayReason(request.getReason())
                .changedBy(adminUser)
                .changedAt(Instant.now())
                .build();
        statusHistoryRepository.save(history);

        // 3. Persist Disruption Record
        FlightDisruption disruption = FlightDisruption.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .disruptionType(DisruptionType.CANCELLATION)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(DisruptionStatus.ACTIVE)
                .createdBy(adminUser)
                .createdAt(Instant.now())
                .build();
        disruption = disruptionRepository.save(disruption);

        // 4. Update Affected Bookings and Release Seats
        List<Booking> affectedBookings = bookingRepository.findByFlightId(flightId);
        for (Booking booking : affectedBookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PENDING) {
                try {
                    bookingStateMachine.validateTransition(booking.getStatus(), BookingStatus.CANCELLED);
                    booking.setStatus(BookingStatus.CANCELLED);
                    booking.setCancellationReason("Flight cancelled by airline: " + request.getReason());
                    booking.setCancelledAt(Instant.now());
                    bookingRepository.save(booking);

                    // Release physical seats
                    seatMapService.releaseSeats(booking.getId());
                } catch (Exception ex) {
                    log.warn("Error updating booking ID: {} on flight cancellation: {}", booking.getId(), ex.getMessage());
                }
            }
        }

        // 5. Trigger Eligible Auto-Refunds if requested
        if (request.isAutoRefund()) {
            refundService.processDisruptionRefundsForFlight(flightId, RefundReason.FLIGHT_CANCELLED, adminUser);
        }

        // 6. Notify Affected Passengers
        notifyAffectedPassengers(savedFlight, disruption, NotificationType.FLIGHT_CANCELLED,
                String.format("Important: Flight %s Cancelled", savedFlight.getFlightNumber()),
                String.format("Dear Passenger,\n\nWe regret to inform you that flight %s has been cancelled.\nReason: %s\nEligible refunds are being processed automatically to your payment method.\n\nSmartTravel Customer Support.",
                        savedFlight.getFlightNumber(), request.getReason()));

        return buildOperationalStatusResponse(savedFlight);
    }

    @Override
    @Transactional
    public FlightOperationalStatusResponse updateGate(String flightId, FlightGateChangeRequest request, String adminUser) {
        log.info("Admin '{}' updating gate for flight ID: {} to '{}'", adminUser, flightId, request.getGate());

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        AirportInfo dep = flight.getDepartureAirport();
        String previousGate = dep != null ? dep.getGate() : null;
        String newGate = request.getGate().trim();

        // Idempotent guard
        if (newGate.equalsIgnoreCase(previousGate)) {
            return buildOperationalStatusResponse(flight);
        }

        if (dep != null) {
            dep.setGate(newGate);
        }
        flight.setLastOperationalUpdate(Instant.now());
        Flight savedFlight = flightRepository.save(flight);

        // Persist Disruption Record
        FlightDisruption disruption = FlightDisruption.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .disruptionType(DisruptionType.GATE_CHANGE)
                .reason(request.getReason() != null ? request.getReason() : "Gate reassignment")
                .previousGate(previousGate)
                .newGate(newGate)
                .status(DisruptionStatus.ACTIVE)
                .createdBy(adminUser)
                .createdAt(Instant.now())
                .build();
        disruption = disruptionRepository.save(disruption);

        // Alert Passengers
        notifyAffectedPassengers(savedFlight, disruption, NotificationType.GATE_CHANGED,
                String.format("Gate Change: Flight %s now departing from %s", savedFlight.getFlightNumber(), newGate),
                String.format("Dear Passenger,\n\nPlease note the departure gate for flight %s has changed from %s to %s.",
                        savedFlight.getFlightNumber(), previousGate != null ? previousGate : "TBD", newGate));

        return buildOperationalStatusResponse(savedFlight);
    }

    @Override
    @Transactional
    public FlightOperationalStatusResponse updateTerminal(String flightId, FlightTerminalChangeRequest request, String adminUser) {
        log.info("Admin '{}' updating terminal for flight ID: {} to '{}'", adminUser, flightId, request.getTerminal());

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        AirportInfo dep = flight.getDepartureAirport();
        String previousTerminal = dep != null ? dep.getTerminal() : null;
        String newTerminal = request.getTerminal().trim();

        // Idempotent guard
        if (newTerminal.equalsIgnoreCase(previousTerminal)) {
            return buildOperationalStatusResponse(flight);
        }

        if (dep != null) {
            dep.setTerminal(newTerminal);
        }
        flight.setLastOperationalUpdate(Instant.now());
        Flight savedFlight = flightRepository.save(flight);

        // Persist Disruption Record
        FlightDisruption disruption = FlightDisruption.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .disruptionType(DisruptionType.TERMINAL_CHANGE)
                .reason(request.getReason() != null ? request.getReason() : "Terminal reassignment")
                .previousTerminal(previousTerminal)
                .newTerminal(newTerminal)
                .status(DisruptionStatus.ACTIVE)
                .createdBy(adminUser)
                .createdAt(Instant.now())
                .build();
        disruption = disruptionRepository.save(disruption);

        // Alert Passengers
        notifyAffectedPassengers(savedFlight, disruption, NotificationType.TERMINAL_CHANGED,
                String.format("Terminal Change: Flight %s now departing from %s", savedFlight.getFlightNumber(), newTerminal),
                String.format("Dear Passenger,\n\nPlease note the departure terminal for flight %s has changed from %s to %s.",
                        savedFlight.getFlightNumber(), previousTerminal != null ? previousTerminal : "TBD", newTerminal));

        return buildOperationalStatusResponse(savedFlight);
    }

    @Override
    @Transactional
    public FlightOperationalStatusResponse changeAircraft(String flightId, FlightAircraftChangeRequest request, String adminUser) {
        log.info("Admin '{}' swapping aircraft for flight ID: {} to '{}'", adminUser, flightId, request.getAircraftModel());

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        String previousModel = flight.getAircraftModel();
        String newModel = request.getAircraftModel().trim();

        if (newModel.equalsIgnoreCase(previousModel)) {
            return buildOperationalStatusResponse(flight);
        }

        // Validate Seat Compatibility
        int currentCapacity = flight.getTotalSeats();
        int newCapacity = AircraftSeatLayout.getTotalSeatCapacity(newModel);

        if (!request.isForce() && newCapacity < currentCapacity) {
            int bookedCount = flightImpactService.getAffectedConfirmedBookings(flightId).size();
            if (bookedCount > newCapacity) {
                throw new BadRequestException(String.format("Cannot swap to %s (Capacity: %d): Exceeds current confirmed booking count (%d)",
                        newModel, newCapacity, bookedCount));
            }
        }

        flight.setAircraftModel(newModel);
        flight.setLastOperationalUpdate(Instant.now());
        Flight savedFlight = flightRepository.save(flight);

        // Persist Disruption Record
        FlightDisruption disruption = FlightDisruption.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .disruptionType(DisruptionType.AIRCRAFT_CHANGE)
                .reason(request.getReason() != null ? request.getReason() : "Equipment swap")
                .previousAircraftModel(previousModel)
                .newAircraftModel(newModel)
                .status(DisruptionStatus.ACTIVE)
                .createdBy(adminUser)
                .createdAt(Instant.now())
                .build();
        disruption = disruptionRepository.save(disruption);

        // Alert Passengers
        notifyAffectedPassengers(savedFlight, disruption, NotificationType.AIRCRAFT_CHANGED,
                String.format("Aircraft Change Notice for Flight %s", savedFlight.getFlightNumber()),
                String.format("Dear Passenger,\n\nPlease note the operating aircraft for flight %s has been updated to %s.",
                        savedFlight.getFlightNumber(), newModel));

        return buildOperationalStatusResponse(savedFlight);
    }

    @Override
    public FlightDisruptionDto resolveDisruption(String disruptionId, String adminUser) {
        FlightDisruption disruption = disruptionRepository.findById(disruptionId)
                .orElseThrow(() -> new ResourceNotFoundException("FlightDisruption", "id", disruptionId));

        disruption.setStatus(DisruptionStatus.RESOLVED);
        disruption.setResolvedAt(Instant.now());
        disruption.setResolvedBy(adminUser);
        disruption = disruptionRepository.save(disruption);

        return toDto(disruption);
    }

    @Override
    public FlightOperationalStatusResponse getFlightOperationalStatus(String flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));
        return buildOperationalStatusResponse(flight);
    }

    @Override
    public PageResponse<FlightDisruptionDto> getFlightDisruptions(String flightId, Pageable pageable) {
        Page<FlightDisruption> page = disruptionRepository.findByFlightId(flightId, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Override
    public List<FlightDisruptionDto> getDisruptionsForBooking(String bookingId, String userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!isAdmin && !booking.getUserId().equals(userId)) {
            // Strict IDOR protection
            throw new ResourceNotFoundException("Booking", "id", bookingId);
        }

        List<FlightDisruption> disruptions = disruptionRepository.findByFlightIdOrderByCreatedAtDesc(booking.getFlightId());
        return disruptions.stream().map(this::toDto).collect(Collectors.toList());
    }

    private void notifyAffectedPassengers(Flight flight, FlightDisruption disruption,
                                         NotificationType type, String subject, String body) {
        List<Booking> bookings = bookingRepository.findByFlightId(flight.getId());
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED || type == NotificationType.FLIGHT_CANCELLED) {
                try {
                    notificationService.sendNotification(NotificationSendRequest.builder()
                            .userId(booking.getUserId())
                            .bookingId(booking.getId())
                            .flightId(flight.getId())
                            .notificationType(type)
                            .channel(NotificationChannel.EMAIL)
                            .subject(subject)
                            .content(body)
                            .eventId("disruption_" + disruption.getId())
                            .build());
                } catch (Exception ex) {
                    log.warn("Failed to dispatch disruption notification to user {}: {}", booking.getUserId(), ex.getMessage());
                }
            }
        }
    }

    private FlightOperationalStatusResponse buildOperationalStatusResponse(Flight flight) {
        List<FlightDisruption> disruptions = disruptionRepository.findByFlightIdOrderByCreatedAtDesc(flight.getId());
        List<FlightDisruptionDto> disruptionDtos = disruptions.stream().map(this::toDto).collect(Collectors.toList());

        AirportInfo dep = flight.getDepartureAirport();
        String gate = dep != null ? dep.getGate() : null;
        String terminal = dep != null ? dep.getTerminal() : null;

        return FlightOperationalStatusResponse.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .scheduledDepartureTime(flight.getDepartureTime())
                .scheduledArrivalTime(flight.getArrivalTime())
                .revisedDepartureTime(flight.getRevisedDepartureTime())
                .estimatedArrivalTime(flight.getEstimatedArrival())
                .status(flight.getStatus())
                .delayMinutes(flight.getDelayMinutes())
                .delayReason(flight.getDelayReason())
                .gate(gate)
                .terminal(terminal)
                .aircraftModel(flight.getAircraftModel())
                .operationalNotes(flight.getOperationalNotes())
                .lastOperationalUpdate(flight.getLastOperationalUpdate())
                .disruptions(disruptionDtos)
                .build();
    }

    private FlightDisruptionDto toDto(FlightDisruption d) {
        return FlightDisruptionDto.builder()
                .id(d.getId())
                .flightId(d.getFlightId())
                .flightNumber(d.getFlightNumber())
                .disruptionType(d.getDisruptionType())
                .reason(d.getReason())
                .description(d.getDescription())
                .previousDepartureTime(d.getPreviousDepartureTime())
                .newDepartureTime(d.getNewDepartureTime())
                .previousArrivalTime(d.getPreviousArrivalTime())
                .newArrivalTime(d.getNewArrivalTime())
                .previousGate(d.getPreviousGate())
                .newGate(d.getNewGate())
                .previousTerminal(d.getPreviousTerminal())
                .newTerminal(d.getNewTerminal())
                .previousAircraftModel(d.getPreviousAircraftModel())
                .newAircraftModel(d.getNewAircraftModel())
                .status(d.getStatus())
                .createdBy(d.getCreatedBy())
                .createdAt(d.getCreatedAt())
                .resolvedAt(d.getResolvedAt())
                .build();
    }
}
