package com.smarttravel.modules.booking.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.booking.config.CheckInProperties;
import com.smarttravel.modules.booking.dto.BoardingPassResponse;
import com.smarttravel.modules.booking.dto.CheckInRequest;
import com.smarttravel.modules.booking.dto.CheckInResponse;
import com.smarttravel.modules.booking.dto.PassengerCheckInResponse;
import com.smarttravel.modules.booking.model.BoardingPass;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.model.CheckIn;
import com.smarttravel.modules.booking.model.CheckInStatus;
import com.smarttravel.modules.booking.model.Passenger;
import com.smarttravel.modules.booking.model.PassengerCheckInInfo;
import com.smarttravel.modules.booking.repository.BoardingPassRepository;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.repository.CheckInRepository;
import com.smarttravel.modules.flight.dto.SeatDto;
import com.smarttravel.modules.flight.model.SeatStatus;
import com.smarttravel.modules.flight.service.SeatMapService;
import com.smarttravel.modules.ticket.model.PassengerTicketInfo;
import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.repository.TicketRepository;
import com.smarttravel.modules.ticket.service.TicketNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of CheckInService with idempotency, check-in window validation,
 * automatic seat assignment, and boarding pass generation.
 */
@Service
public class CheckInServiceImpl implements CheckInService {

    private static final Logger log = LoggerFactory.getLogger(CheckInServiceImpl.class);

    private final CheckInRepository checkInRepository;
    private final BoardingPassRepository boardingPassRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final SeatMapService seatMapService;
    private final BoardingPassPdfService boardingPassPdfService;
    private final CheckInProperties checkInProperties;
    private final TicketNumberGenerator numberGenerator;

    public CheckInServiceImpl(CheckInRepository checkInRepository,
                              BoardingPassRepository boardingPassRepository,
                              BookingRepository bookingRepository,
                              TicketRepository ticketRepository,
                              SeatMapService seatMapService,
                              BoardingPassPdfService boardingPassPdfService,
                              CheckInProperties checkInProperties,
                              TicketNumberGenerator numberGenerator) {
        this.checkInRepository = checkInRepository;
        this.boardingPassRepository = boardingPassRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.seatMapService = seatMapService;
        this.boardingPassPdfService = boardingPassPdfService;
        this.checkInProperties = checkInProperties;
        this.numberGenerator = numberGenerator;
    }

    @Override
    public CheckInResponse performCheckIn(String bookingId, CheckInRequest request, String userId, boolean isAdmin) {
        log.info("Processing check-in for booking ID: {} (user: {}, isAdmin: {})", bookingId, userId, isAdmin);

        // 1. Fetch booking with strict ownership validation (404 for unauthorized access)
        Booking booking = fetchBookingWithOwnership(bookingId, userId, isAdmin);

        // 2. Validate booking status is CONFIRMED
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException("Check-in is not permitted for booking in status: " + booking.getStatus());
        }

        // 3. Validate issued Ticket exists
        Ticket ticket = ticketRepository.findFirstByBookingId(booking.getId())
                .orElseThrow(() -> new ConflictException("Cannot check in: E-Ticket has not been issued for this booking."));

        // 4. Validate Check-in Window
        validateCheckInWindow(booking.getDepartureTime());

        // 5. Idempotent check-in execution (synchronized per booking ID)
        synchronized (bookingId.intern()) {
            CheckIn existingCheckIn = checkInRepository.findByBookingId(bookingId).orElse(null);
            if (existingCheckIn != null) {
                log.info("Booking ID: {} is already checked in (Check-in No: {}). Returning existing boarding passes.",
                        bookingId, existingCheckIn.getCheckInNumber());
                return mapToCheckInResponse(existingCheckIn);
            }

            // 6. Assign seats and generate boarding passes
            Instant now = Instant.now();
            String checkInNumber = "CI-" + numberGenerator.generateTicketNumber().replace("ST-", "");
            List<BoardingPass> boardingPasses = new ArrayList<>();
            List<PassengerCheckInInfo> checkInPassengers = new ArrayList<>();

            List<Passenger> passengers = booking.getPassengers();
            Map<String, String> requestedSeats = request != null && request.getPassengerSeats() != null
                    ? request.getPassengerSeats()
                    : Map.of();

            List<SeatDto> availableSeats = seatMapService.getSeatsForFlight(booking.getFlightId(), booking.getCabinClass())
                    .stream()
                    .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                    .collect(Collectors.toList());

            int seatIndex = 0;

            for (int i = 0; i < passengers.size(); i++) {
                Passenger p = passengers.get(i);
                String seat = p.getSeatNumber();

                // Check if seat specified in request
                if (seat == null || seat.isBlank()) {
                    if (requestedSeats.containsKey(p.getFirstName())) {
                        seat = requestedSeats.get(p.getFirstName());
                    } else if (requestedSeats.containsKey(String.valueOf(i))) {
                        seat = requestedSeats.get(String.valueOf(i));
                    }
                }

                // Fallback to first available seat in cabin or default row
                if (seat == null || seat.isBlank()) {
                    if (seatIndex < availableSeats.size()) {
                        seat = availableSeats.get(seatIndex++).getSeatNumber();
                    } else {
                        seat = (10 + i) + "A";
                    }
                    p.setSeatNumber(seat);
                }

                // Find passenger's eTicketNumber from Ticket snapshot
                String eTicketNumber = ticket.getPassengers() != null && i < ticket.getPassengers().size()
                        ? ticket.getPassengers().get(i).getETicketNumber()
                        : ticket.getTicketNumber() + "-" + String.format("%02d", i + 1);

                String bpNumber = "BP-" + numberGenerator.generateTicketNumber().replace("ST-", "");

                Instant boardingTime = booking.getDepartureTime() != null
                        ? booking.getDepartureTime().minus(Duration.ofMinutes(45))
                        : now.plus(Duration.ofHours(1));

                BoardingPass bp = BoardingPass.builder()
                        .boardingPassNumber(bpNumber)
                        .checkInId(checkInNumber)
                        .bookingId(booking.getId())
                        .bookingReference(booking.getBookingReference())
                        .userId(booking.getUserId())
                        .ticketNumber(ticket.getTicketNumber())
                        .eTicketNumber(eTicketNumber)
                        .passengerName(p.getTitle() + " " + p.getFirstName() + " " + p.getLastName())
                        .seatNumber(seat)
                        .cabinClass(booking.getCabinClass())
                        .flightNumber(booking.getFlightNumber())
                        .airline(booking.getAirline())
                        .airlineCode(booking.getAirlineCode())
                        .departureAirport(booking.getDepartureAirport())
                        .arrivalAirport(booking.getArrivalAirport())
                        .departureTime(booking.getDepartureTime())
                        .arrivalTime(booking.getArrivalTime())
                        .boardingGroup("Group " + (booking.getCabinClass() != null && booking.getCabinClass().name().contains("BUSINESS") ? "1" : "2"))
                        .gate("Gate " + ((Math.abs(booking.getFlightNumber().hashCode()) % 15) + 1))
                        .terminal("T" + ((Math.abs(booking.getFlightNumber().hashCode()) % 3) + 1))
                        .boardingTime(boardingTime)
                        .barcodeData(bpNumber)
                        .issuedAt(now)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                boardingPasses.add(bp);

                checkInPassengers.add(PassengerCheckInInfo.builder()
                        .passengerId(p.getPassengerId())
                        .title(p.getTitle())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .seatNumber(seat)
                        .cabinClass(booking.getCabinClass())
                        .eTicketNumber(eTicketNumber)
                        .boardingPassNumber(bpNumber)
                        .build());
            }

            // Persist boarding passes and update booking passenger seats
            boardingPassRepository.saveAll(boardingPasses);
            bookingRepository.save(booking);

            CheckIn checkIn = CheckIn.builder()
                    .checkInNumber(checkInNumber)
                    .bookingId(booking.getId())
                    .bookingReference(booking.getBookingReference())
                    .userId(booking.getUserId())
                    .flightId(booking.getFlightId())
                    .flightNumber(booking.getFlightNumber())
                    .passengers(checkInPassengers)
                    .status(CheckInStatus.COMPLETED)
                    .checkedInAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            CheckIn savedCheckIn = checkInRepository.save(checkIn);
            log.info("Check-in completed successfully for booking ID: {} (Check-In No: {})", bookingId, checkInNumber);

            return mapToCheckInResponse(savedCheckIn);
        }
    }

    @Override
    public CheckInResponse getCheckInByBookingId(String bookingId, String userId, boolean isAdmin) {
        Booking booking = fetchBookingWithOwnership(bookingId, userId, isAdmin);

        CheckIn checkIn = checkInRepository.findByBookingId(booking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CheckIn", "bookingId", bookingId));

        return mapToCheckInResponse(checkIn);
    }

    @Override
    public List<BoardingPassResponse> getBoardingPasses(String bookingId, String userId, boolean isAdmin) {
        Booking booking = fetchBookingWithOwnership(bookingId, userId, isAdmin);

        List<BoardingPass> passes = boardingPassRepository.findByBookingId(booking.getId());
        if (passes.isEmpty()) {
            throw new ResourceNotFoundException("BoardingPass", "bookingId", bookingId);
        }

        return passes.stream().map(this::mapToBoardingPassResponse).collect(Collectors.toList());
    }

    @Override
    public byte[] getBoardingPassPdf(String bookingId, String userId, boolean isAdmin) {
        Booking booking = fetchBookingWithOwnership(bookingId, userId, isAdmin);

        List<BoardingPass> passes = boardingPassRepository.findByBookingId(booking.getId());
        if (passes.isEmpty()) {
            throw new ResourceNotFoundException("BoardingPass", "bookingId", bookingId);
        }

        return boardingPassPdfService.generateMultiBoardingPassPdf(passes);
    }

    private Booking fetchBookingWithOwnership(String bookingId, String userId, boolean isAdmin) {
        if (isAdmin) {
            return bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        } else {
            return bookingRepository.findByIdAndUserId(bookingId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        }
    }

    private void validateCheckInWindow(Instant departureTime) {
        if (departureTime == null) {
            return;
        }

        if (checkInProperties != null && !checkInProperties.isEnabled()) {
            throw new ConflictException("Online check-in is currently disabled by system administrator.");
        }

        Instant now = Instant.now();
        int openHours = checkInProperties != null ? checkInProperties.getOpeningHoursBeforeDeparture() : 24;
        int closeMins = checkInProperties != null ? checkInProperties.getClosingMinutesBeforeDeparture() : 60;

        Instant windowStart = departureTime.minus(Duration.ofHours(openHours));
        Instant windowEnd = departureTime.minus(Duration.ofMinutes(closeMins));

        if (now.isBefore(windowStart)) {
            throw new ConflictException("Online check-in opens " + openHours + " hours before flight departure. Check-in opens at " + windowStart);
        }

        if (now.isAfter(windowEnd)) {
            throw new ConflictException("Online check-in closed " + closeMins + " minutes before flight departure. Please proceed to the airport counter.");
        }
    }

    private CheckInResponse mapToCheckInResponse(CheckIn checkIn) {
        List<PassengerCheckInResponse> passengerResponses = checkIn.getPassengers().stream()
                .map(p -> PassengerCheckInResponse.builder()
                        .title(p.getTitle())
                        .passengerName(p.getTitle() + " " + p.getFirstName() + " " + p.getLastName())
                        .seatNumber(p.getSeatNumber())
                        .cabinClass(p.getCabinClass())
                        .eTicketNumber(p.getETicketNumber())
                        .boardingPassNumber(p.getBoardingPassNumber())
                        .build())
                .collect(Collectors.toList());

        return CheckInResponse.builder()
                .id(checkIn.getId())
                .checkInNumber(checkIn.getCheckInNumber())
                .bookingId(checkIn.getBookingId())
                .bookingReference(checkIn.getBookingReference())
                .flightNumber(checkIn.getFlightNumber())
                .status(checkIn.getStatus())
                .checkedInAt(checkIn.getCheckedInAt())
                .passengers(passengerResponses)
                .build();
    }

    private BoardingPassResponse mapToBoardingPassResponse(BoardingPass bp) {
        return BoardingPassResponse.builder()
                .id(bp.getId())
                .boardingPassNumber(bp.getBoardingPassNumber())
                .bookingReference(bp.getBookingReference())
                .ticketNumber(bp.getTicketNumber())
                .eTicketNumber(bp.getETicketNumber())
                .passengerName(bp.getPassengerName())
                .seatNumber(bp.getSeatNumber())
                .cabinClass(bp.getCabinClass())
                .flightNumber(bp.getFlightNumber())
                .airline(bp.getAirline())
                .departureAirport(bp.getDepartureAirport())
                .arrivalAirport(bp.getArrivalAirport())
                .departureTime(bp.getDepartureTime())
                .arrivalTime(bp.getArrivalTime())
                .boardingGroup(bp.getBoardingGroup())
                .gate(bp.getGate())
                .terminal(bp.getTerminal())
                .boardingTime(bp.getBoardingTime())
                .issuedAt(bp.getIssuedAt())
                .build();
    }
}
