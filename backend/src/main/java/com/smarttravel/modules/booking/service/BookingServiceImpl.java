package com.smarttravel.modules.booking.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.booking.config.BookingProperties;
import com.smarttravel.modules.booking.dto.BookingCancelRequest;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.mapper.BookingMapper;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.model.Passenger;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FareCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core implementation of flight booking management with atomic seat reservation.
 */
@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private static final Set<FlightStatus> BOOKABLE_STATUSES = Set.of(
            FlightStatus.SCHEDULED,
            FlightStatus.BOARDING,
            FlightStatus.ON_TIME,
            FlightStatus.DELAYED
    );

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final FlightInventoryReservationService reservationService;
    private final FareCalculationService fareCalculationService;
    private final BookingStateMachine stateMachine;
    private final PnrGenerator pnrGenerator;
    private final BookingMapper bookingMapper;
    private final BookingProperties bookingProperties;
    private final com.smarttravel.modules.ticket.service.TicketService ticketService;
    private final com.smarttravel.modules.flight.service.SeatMapService seatMapService;
    private final com.smarttravel.modules.pricing.service.PriceFreezeService priceFreezeService;
    private final com.smarttravel.modules.pricing.service.DynamicPricingService dynamicPricingService;

    @org.springframework.beans.factory.annotation.Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              FlightRepository flightRepository,
                              FlightInventoryReservationService reservationService,
                              FareCalculationService fareCalculationService,
                              BookingStateMachine stateMachine,
                              PnrGenerator pnrGenerator,
                              BookingMapper bookingMapper,
                              BookingProperties bookingProperties,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.ticket.service.TicketService ticketService,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) @org.springframework.context.annotation.Lazy com.smarttravel.modules.flight.service.SeatMapService seatMapService,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) @org.springframework.context.annotation.Lazy com.smarttravel.modules.pricing.service.PriceFreezeService priceFreezeService,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) @org.springframework.context.annotation.Lazy com.smarttravel.modules.pricing.service.DynamicPricingService dynamicPricingService) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.reservationService = reservationService;
        this.fareCalculationService = fareCalculationService;
        this.stateMachine = stateMachine;
        this.pnrGenerator = pnrGenerator;
        this.bookingMapper = bookingMapper;
        this.bookingProperties = bookingProperties;
        this.ticketService = ticketService;
        this.seatMapService = seatMapService;
        this.priceFreezeService = priceFreezeService;
        this.dynamicPricingService = dynamicPricingService;
    }

    public BookingServiceImpl(BookingRepository bookingRepository,
                              FlightRepository flightRepository,
                              FlightInventoryReservationService reservationService,
                              FareCalculationService fareCalculationService,
                              BookingStateMachine stateMachine,
                              PnrGenerator pnrGenerator,
                              BookingMapper bookingMapper) {
        this(bookingRepository, flightRepository, reservationService, fareCalculationService, stateMachine, pnrGenerator, bookingMapper, new BookingProperties(), null, null, null, null);
    }

    @Override
    public BookingResponse createBooking(BookingCreateRequest request, String userId, String userEmail) {
        log.info("Initiating booking creation for user: {} on flight ID: {}", userId, request.getFlightId());

        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            throw new BadRequestException("Passenger list must not be empty");
        }
        int passengerCount = request.getPassengers().size();
        if (passengerCount > 9) {
            throw new BadRequestException("A maximum of 9 passengers can be booked in a single reservation");
        }

        CabinClass cabinClass = request.getCabinClass();
        if (cabinClass == null) {
            throw new BadRequestException("Cabin class is required");
        }

        // 1. Fetch flight and validate bookability
        Flight flight = flightRepository.findByIdAndActiveTrue(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", request.getFlightId()));

        if (!BOOKABLE_STATUSES.contains(flight.getStatus())) {
            throw new BadRequestException("Flight " + flight.getFlightNumber() + " is not available for booking in status: " + flight.getStatus());
        }

        if (flight.getDepartureTime() != null && flight.getDepartureTime().isBefore(Instant.now())) {
            throw new BadRequestException("Cannot book a flight whose departure time has already passed");
        }

        // Check for requested seat numbers
        List<String> requestedSeats = request.getPassengers().stream()
                .map(com.smarttravel.modules.booking.dto.PassengerDto::getSeatNumber)
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());

        // Validate no duplicate seat requests in this single booking
        Set<String> uniqueRequestedSeats = new java.util.HashSet<>(requestedSeats);
        if (uniqueRequestedSeats.size() < requestedSeats.size()) {
            throw new BadRequestException("Duplicate seat selection within the passenger list");
        }

        // 2. Determine base price for the selected cabin
        BigDecimal basePrice = resolveBasePrice(flight, cabinClass);

        // 3. Atomically reserve cabin inventory
        boolean reserved = reservationService.reserveSeats(flight.getId(), cabinClass, passengerCount);
        if (!reserved) {
            log.warn("Atomic seat reservation failed for flight: {}, cabin: {}, seats: {}", flight.getId(), cabinClass, passengerCount);
            throw new ConflictException("Insufficient seat availability for the selected cabin: " + cabinClass);
        }

        // 4. Calculate itemized price snapshot (or use active price freeze if provided)
        FareBreakdownDto fareSnapshot;
        if (request.getPriceFreezeId() != null && !request.getPriceFreezeId().isBlank() && priceFreezeService != null) {
            com.smarttravel.modules.pricing.model.PriceFreeze freeze =
                    priceFreezeService.getFreezeById(request.getPriceFreezeId(), userId);
            if (freeze.getStatus() != com.smarttravel.modules.pricing.model.PriceFreezeStatus.ACTIVE || freeze.isExpired()) {
                throw new BadRequestException("The specified price freeze is expired or no longer active");
            }
            if (!flight.getId().equals(freeze.getFlightId()) || cabinClass != freeze.getCabinClass()) {
                throw new BadRequestException("Price freeze does not match the requested flight or cabin class");
            }
            BigDecimal totalLocked = freeze.getLockedPricePerPassenger().multiply(BigDecimal.valueOf(passengerCount));
            BigDecimal baseLocked = freeze.getBasePriceAtFreeze() != null ? freeze.getBasePriceAtFreeze().multiply(BigDecimal.valueOf(passengerCount)) : totalLocked.multiply(new BigDecimal("0.8"));
            BigDecimal taxLocked = totalLocked.subtract(baseLocked);

            fareSnapshot = FareBreakdownDto.builder()
                    .baseFare(baseLocked)
                    .taxes(taxLocked)
                    .fees(BigDecimal.valueOf(150L * passengerCount))
                    .totalAmount(totalLocked)
                    .currency("INR")
                    .passengerCount(passengerCount)
                    .build();
            log.info("Applied locked price freeze {} (Total: ₹{}) for user {}", freeze.getId(), totalLocked, userId);
        } else {
            fareSnapshot = fareCalculationService.calculateFare(basePrice, cabinClass, passengerCount);
        }

        // 5. Generate unique PNR reference
        String pnr = generateUniquePnr();

        // 6. Map passenger entities
        List<Passenger> passengerEntities = bookingMapper.toEntityList(request.getPassengers());

        int timeoutMinutes = bookingProperties != null ? bookingProperties.getPaymentTimeoutMinutes() : 15;
        Instant now = Instant.now();
        Instant expiresAt = now.plus(java.time.Duration.ofMinutes(timeoutMinutes));

        String bookingId = new org.bson.types.ObjectId().toHexString();

        // Hold physical seats if requested (with compensating rollback)
        if (!requestedSeats.isEmpty() && seatMapService != null) {
            try {
                seatMapService.holdSeats(flight.getId(), cabinClass, requestedSeats, bookingId, pnr, expiresAt);
            } catch (Exception ex) {
                log.warn("Seat hold failed for flight ID: {}, seats: {}. Executing compensating cabin inventory release.", flight.getId(), requestedSeats, ex);
                reservationService.releaseSeats(flight.getId(), cabinClass, passengerCount);
                throw ex;
            }
        }

        // 7. Construct Booking entity
        Booking booking = Booking.builder()
                .id(bookingId)
                .bookingReference(pnr)
                .userId(userId)
                .userEmail(userEmail)
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .airlineCode(flight.getAirlineCode())
                .departureAirport(flight.getDepartureAirport())
                .arrivalAirport(flight.getArrivalAirport())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .durationMinutes(flight.getDurationMinutes())
                .cabinClass(cabinClass)
                .passengerCount(passengerCount)
                .passengers(passengerEntities)
                .fareBreakdown(fareSnapshot)
                .totalAmount(fareSnapshot.getTotalAmount())
                .currency(fareSnapshot.getCurrency())
                .status(BookingStatus.CONFIRMED)
                .expiresAt(expiresAt)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 8. Persist booking with compensating rollback on unexpected failure
        Booking savedBooking;
        try {
            savedBooking = bookingRepository.save(booking);
            log.info("Booking created successfully with PNR: {} and ID: {}", pnr, savedBooking.getId());

            // Mark price freeze as used if applied
            if (request.getPriceFreezeId() != null && !request.getPriceFreezeId().isBlank() && priceFreezeService != null) {
                try {
                    priceFreezeService.markAsUsed(request.getPriceFreezeId(), savedBooking.getId(), userId);
                } catch (Exception ex) {
                    log.warn("Failed to mark price freeze {} as used: {}", request.getPriceFreezeId(), ex.getMessage());
                }
            }

            // Broadcast real-time dynamic price update to topic subscribers
            if (dynamicPricingService != null) {
                try {
                    flightRepository.findById(flight.getId()).ifPresent(freshFlight -> {
                        if (freshFlight.getCabinInventories() != null) {
                            freshFlight.getCabinInventories().stream()
                                    .filter(ci -> ci.getCabinClass() == cabinClass)
                                    .findFirst()
                                    .ifPresent(ci -> dynamicPricingService.publishPriceUpdate(freshFlight, ci, null));
                        }
                    });
                } catch (Exception ex) {
                    log.warn("Failed to broadcast real-time pricing update after booking: {}", ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error("Failed to persist booking for flight ID: {}. Executing compensating seat release.", flight.getId(), ex);
            reservationService.releaseSeats(flight.getId(), cabinClass, passengerCount);
            if (!requestedSeats.isEmpty() && seatMapService != null) {
                seatMapService.releaseSeats(bookingId);
            }
            throw ex;
        }

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(String id, String userId, boolean isAdmin) {
        log.debug("Fetching booking by ID: {} (user: {}, isAdmin: {})", id, userId, isAdmin);
        if (id == null || id.trim().isEmpty()) {
            throw new BadRequestException("Booking identifier cannot be empty");
        }
        String cleanId = id.trim();
        Booking booking;
        if (isAdmin) {
            booking = bookingRepository.findById(cleanId)
                    .or(() -> bookingRepository.findByBookingReference(cleanId.toUpperCase()))
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", cleanId));
        } else {
            booking = bookingRepository.findByIdAndUserId(cleanId, userId)
                    .or(() -> bookingRepository.findByBookingReferenceAndUserId(cleanId.toUpperCase(), userId))
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", cleanId));
        }
        return bookingMapper.toResponse(booking);
    }

    @Override
    public BookingResponse getBookingByReference(String reference, String userId, boolean isAdmin) {
        if (reference == null || reference.trim().isEmpty()) {
            throw new BadRequestException("Booking reference cannot be empty");
        }
        String normalizedRef = reference.trim().toUpperCase();
        log.debug("Fetching booking by PNR: {} (user: {}, isAdmin: {})", normalizedRef, userId, isAdmin);

        Booking booking;
        if (isAdmin) {
            booking = bookingRepository.findByBookingReference(normalizedRef)
                    .or(() -> bookingRepository.findById(reference.trim()))
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingReference", normalizedRef));
        } else {
            booking = bookingRepository.findByBookingReferenceAndUserId(normalizedRef, userId)
                    .or(() -> bookingRepository.findByIdAndUserId(reference.trim(), userId))
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingReference", normalizedRef));
        }
        return bookingMapper.toResponse(booking);
    }

    @Override
    public PageResponse<BookingResponse> getUserBookings(String userId, BookingStatus status, Pageable pageable) {
        log.debug("Fetching bookings for user ID: {} (status: {})", userId, status);
        Page<Booking> page = status != null
                ? bookingRepository.findByUserIdAndStatus(userId, status, pageable)
                : bookingRepository.findByUserId(userId, pageable);
        return PageResponse.from(page.map(bookingMapper::toResponse));
    }

    @Override
    public PageResponse<BookingResponse> getAllBookings(BookingStatus status, Pageable pageable) {
        log.debug("Admin fetching all bookings with pagination (status: {})", status);
        Page<Booking> page = status != null
                ? bookingRepository.findByStatus(status, pageable)
                : bookingRepository.findAll(pageable);
        return PageResponse.from(page.map(bookingMapper::toResponse));
    }

    @Override
    public BookingResponse cancelBooking(String id, BookingCancelRequest request, String userId, boolean isAdmin) {
        log.info("Processing booking cancellation for ID: {} (user: {}, isAdmin: {})", id, userId, isAdmin);

        Booking booking;
        if (isAdmin) {
            booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        } else {
            booking = bookingRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        }

        // 1. Validate status transition
        stateMachine.validateTransition(booking.getStatus(), BookingStatus.CANCELLED);

        // 2. Mark booking as cancelled
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        String reason = (request != null && request.getReason() != null && !request.getReason().trim().isEmpty())
                ? request.getReason().trim()
                : (isAdmin ? "Cancelled by Administrator" : "Cancelled by Passenger");
        booking.setCancellationReason(reason);
        booking.setUpdatedAt(Instant.now());

        Booking updatedBooking = bookingRepository.save(booking);

        // 3. Atomically release reserved cabin seats
        boolean released = reservationService.releaseSeats(
                booking.getFlightId(),
                booking.getCabinClass(),
                booking.getPassengerCount()
        );

        if (!released) {
            log.error("Critical: Failed to release {} seat(s) in cabin {} for cancelled booking ID: {}",
                    booking.getPassengerCount(), booking.getCabinClass(), booking.getId());
        } else {
            log.info("Successfully released {} seat(s) in cabin {} for booking PNR: {}",
                    booking.getPassengerCount(), booking.getCabinClass(), booking.getBookingReference());
        }

        // 4. Release any physical seats assigned to this booking
        if (seatMapService != null) {
            try {
                seatMapService.releaseSeats(booking.getId());
            } catch (Exception ex) {
                log.warn("Non-fatal: Failed to release physical seats for booking ID: {}", booking.getId(), ex);
            }
        }

        // 5. Synchronously cancel any issued ticket
        if (ticketService != null) {
            try {
                ticketService.cancelTicketForBooking(booking.getId(), reason);
            } catch (Exception ex) {
                log.warn("Non-fatal: Failed to update ticket status to CANCELLED for booking ID: {}", booking.getId(), ex);
            }
        }

        return bookingMapper.toResponse(updatedBooking);
    }

    private BigDecimal resolveBasePrice(Flight flight, CabinClass cabinClass) {
        if (flight.getCabinInventories() != null && !flight.getCabinInventories().isEmpty()) {
            Optional<CabinInventory> inventoryOpt = flight.getCabinInventories().stream()
                    .filter(inv -> inv.getCabinClass() == cabinClass)
                    .findFirst();
            if (inventoryOpt.isPresent() && inventoryOpt.get().getBasePrice() != null) {
                return inventoryOpt.get().getBasePrice();
            }
        }
        if (flight.getBasePrice() != null) {
            return flight.getBasePrice();
        }
        throw new BadRequestException("Flight has no base price configured for cabin: " + cabinClass);
    }

    private String generateUniquePnr() {
        for (int i = 0; i < 5; i++) {
            String candidate = pnrGenerator.generatePnr();
            if (!bookingRepository.existsByBookingReference(candidate)) {
                return candidate;
            }
        }
        return pnrGenerator.generatePnr();
    }
}
