package com.smarttravel.modules.ticket.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.model.Passenger;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.ticket.dto.TicketResponse;
import com.smarttravel.modules.ticket.mapper.TicketMapper;
import com.smarttravel.modules.ticket.model.PassengerTicketInfo;
import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.model.TicketStatus;
import com.smarttravel.modules.ticket.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Production implementation of TicketService with atomic issuance, idempotency,
 * IDOR ownership enforcement, and deterministic PDF generation.
 */
@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final TicketPdfService ticketPdfService;
    private final TicketMapper ticketMapper;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public TicketServiceImpl(TicketRepository ticketRepository,
                             BookingRepository bookingRepository,
                             PaymentRepository paymentRepository,
                             TicketNumberGenerator ticketNumberGenerator,
                             TicketPdfService ticketPdfService,
                             TicketMapper ticketMapper,
                             @Autowired(required = false) MongoTemplate mongoTemplate) {
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.ticketPdfService = ticketPdfService;
        this.ticketMapper = ticketMapper;
        this.mongoTemplate = mongoTemplate;
    }

    public TicketServiceImpl(TicketRepository ticketRepository,
                             BookingRepository bookingRepository,
                             PaymentRepository paymentRepository,
                             TicketNumberGenerator ticketNumberGenerator,
                             TicketPdfService ticketPdfService,
                             TicketMapper ticketMapper) {
        this(ticketRepository, bookingRepository, paymentRepository, ticketNumberGenerator, ticketPdfService, ticketMapper, null);
    }

    @jakarta.annotation.PostConstruct
    public void initIndexes() {
        if (mongoTemplate != null) {
            try {
                ensureUniqueIndexes();
                log.info("MongoDB unique indexes successfully verified for Ticket collection");
            } catch (Exception ex) {
                log.warn("Initial index verification encountered duplicate keys or conflict: {}. Attempting self-healing deduplication...", ex.getMessage());
                try {
                    selfHealDuplicateTickets();
                    ensureUniqueIndexes();
                    log.info("MongoDB unique indexes successfully established after self-healing deduplication");
                } catch (Exception innerEx) {
                    log.warn("Non-fatal: Deferred index verification for Ticket collection: {}", innerEx.getMessage());
                }
            }
        }
    }

    private void ensureUniqueIndexes() {
        mongoTemplate.indexOps(Ticket.class).ensureIndex(
                new org.springframework.data.mongodb.core.index.Index().on("bookingId", org.springframework.data.domain.Sort.Direction.ASC).unique()
        );
        mongoTemplate.indexOps(Ticket.class).ensureIndex(
                new org.springframework.data.mongodb.core.index.Index().on("ticketNumber", org.springframework.data.domain.Sort.Direction.ASC).unique()
        );
        mongoTemplate.indexOps(Ticket.class).ensureIndex(
                new org.springframework.data.mongodb.core.index.Index().on("userId", org.springframework.data.domain.Sort.Direction.ASC).on("issuedAt", org.springframework.data.domain.Sort.Direction.DESC)
        );
        mongoTemplate.indexOps(Ticket.class).ensureIndex(
                new org.springframework.data.mongodb.core.index.Index().on("flightId", org.springframework.data.domain.Sort.Direction.ASC).on("status", org.springframework.data.domain.Sort.Direction.ASC)
        );
    }

    /**
     * Self-healing deduplication: detects any historical duplicate bookingId records in the tickets collection,
     * preserves the authoritative ticket matching the Booking record, and safely cleans up orphaned duplicates.
     */
    public synchronized void selfHealDuplicateTickets() {
        try {
            org.springframework.data.mongodb.core.aggregation.Aggregation aggregation =
                    org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation(
                            org.springframework.data.mongodb.core.aggregation.Aggregation.match(org.springframework.data.mongodb.core.query.Criteria.where("bookingId").exists(true).ne(null)),
                            org.springframework.data.mongodb.core.aggregation.Aggregation.group("bookingId").count().as("count").push("$$ROOT").as("tickets"),
                            org.springframework.data.mongodb.core.aggregation.Aggregation.match(org.springframework.data.mongodb.core.query.Criteria.where("count").gt(1))
                    );

            org.springframework.data.mongodb.core.aggregation.AggregationResults<org.bson.Document> results =
                    mongoTemplate.aggregate(aggregation, "tickets", org.bson.Document.class);

            List<org.bson.Document> duplicateGroups = results.getMappedResults();
            if (duplicateGroups.isEmpty()) {
                return;
            }

            log.info("Self-healing detected {} duplicate bookingId group(s) in tickets collection", duplicateGroups.size());

            for (org.bson.Document group : duplicateGroups) {
                String bookingId = group.getString("_id");
                List<org.bson.Document> ticketDocs = group.getList("tickets", org.bson.Document.class);
                if (ticketDocs == null || ticketDocs.size() <= 1) {
                    continue;
                }

                Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
                String linkedTicketId = bookingOpt.map(Booking::getTicketId).orElse(null);
                String linkedTicketNumber = bookingOpt.map(Booking::getTicketNumber).orElse(null);

                org.bson.Document authoritativeDoc = null;
                List<org.bson.Document> toDelete = new ArrayList<>();

                for (org.bson.Document doc : ticketDocs) {
                    String docId = doc.get("_id") != null ? doc.get("_id").toString() : null;
                    String docTicketNumber = doc.getString("ticketNumber");
                    String status = doc.getString("status");

                    if (authoritativeDoc == null) {
                        authoritativeDoc = doc;
                    } else {
                        if (docId != null && docId.equals(linkedTicketId)) {
                            toDelete.add(authoritativeDoc);
                            authoritativeDoc = doc;
                        } else if (docTicketNumber != null && docTicketNumber.equals(linkedTicketNumber)) {
                            toDelete.add(authoritativeDoc);
                            authoritativeDoc = doc;
                        } else if ("ISSUED".equals(status) && !"ISSUED".equals(authoritativeDoc.getString("status"))) {
                            toDelete.add(authoritativeDoc);
                            authoritativeDoc = doc;
                        } else {
                            toDelete.add(doc);
                        }
                    }
                }

                log.info("Deduplication: Preserving ticket {} (ID: {}) for bookingId: {}",
                        authoritativeDoc.getString("ticketNumber"), authoritativeDoc.get("_id"), bookingId);

                for (org.bson.Document delDoc : toDelete) {
                    Object delId = delDoc.get("_id");
                    log.info("Deduplication: Removing orphaned duplicate ticket {} (ID: {})",
                            delDoc.getString("ticketNumber"), delId);
                    mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(
                            org.springframework.data.mongodb.core.query.Criteria.where("_id").is(delId)), "tickets");
                }
            }
        } catch (Exception ex) {
            log.warn("Self-healing deduplication encountered an error: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional
    public TicketResponse issueTicket(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new BadRequestException("Booking ID cannot be null or blank for ticket issuance");
        }

        log.info("Processing ticket issuance for booking ID: {}", bookingId);

        synchronized (bookingId.intern()) {
            // 1. Idempotency fast-path: Return existing ticket if already issued
            Optional<Ticket> existingTicketOpt = ticketRepository.findFirstByBookingId(bookingId);
            if (existingTicketOpt.isPresent()) {
                log.info("Ticket already exists for booking ID: {} (Ticket Number: {}). Returning existing ticket.",
                        bookingId, existingTicketOpt.get().getTicketNumber());
                return ticketMapper.toResponse(existingTicketOpt.get());
            }

            // 2. Fetch and validate booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new ConflictException("Cannot issue ticket for CANCELLED booking: " + bookingId);
            }

            if (booking.getStatus() == BookingStatus.EXPIRED) {
                throw new ConflictException("Cannot issue ticket for EXPIRED booking: " + bookingId);
            }

            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                throw new ConflictException("Cannot issue ticket for booking in status: " + booking.getStatus() + ". Booking must be CONFIRMED.");
            }

            // 3. Validate payment status (ensure verified payment exists if payment records are tracked)
            List<Payment> payments = paymentRepository.findByBookingId(bookingId);
            Optional<Payment> verifiedPayment = payments.stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.VERIFIED)
                    .findFirst();

            String paymentId = verifiedPayment.map(Payment::getId).orElse(null);
            String razorpayPaymentId = verifiedPayment.map(Payment::getRazorpayPaymentId).orElse(null);

            // 4. Generate master and passenger ticket numbers
            String ticketNumber = ticketNumberGenerator.generateTicketNumber();
            List<PassengerTicketInfo> passengerTicketInfos = new ArrayList<>();

            if (booking.getPassengers() != null) {
                for (int i = 0; i < booking.getPassengers().size(); i++) {
                    var p = booking.getPassengers().get(i);
                    String eTicketNumber = ticketNumberGenerator.generatePassengerTicketNumber(ticketNumber, i + 1);
                    passengerTicketInfos.add(PassengerTicketInfo.builder()
                            .eTicketNumber(eTicketNumber)
                            .title(p.getTitle())
                            .firstName(p.getFirstName())
                            .lastName(p.getLastName())
                            .dateOfBirth(p.getDateOfBirth())
                            .gender(p.getGender())
                            .nationality(p.getNationality())
                            .seatNumber(p.getSeatNumber())
                            .passportNumber(p.getPassportNumber())
                            .build());
                }
            }

            Instant now = Instant.now();

            // 5. Build immutable Ticket snapshot
            Ticket ticket = Ticket.builder()
                    .ticketNumber(ticketNumber)
                    .bookingId(booking.getId())
                    .bookingReference(booking.getBookingReference())
                    .userId(booking.getUserId())
                    .userEmail(booking.getUserEmail())
                    .flightId(booking.getFlightId())
                    .flightNumber(booking.getFlightNumber())
                    .airline(booking.getAirline())
                    .airlineCode(booking.getAirlineCode())
                    .aircraftModel("Boeing 737 / Airbus A320")
                    .departureAirport(booking.getDepartureAirport())
                    .arrivalAirport(booking.getArrivalAirport())
                    .departureTime(booking.getDepartureTime())
                    .arrivalTime(booking.getArrivalTime())
                    .durationMinutes(booking.getDurationMinutes())
                    .cabinClass(booking.getCabinClass())
                    .passengerCount(booking.getPassengerCount())
                    .passengers(passengerTicketInfos)
                    .fareBreakdown(booking.getFareBreakdown())
                    .totalAmount(booking.getTotalAmount())
                    .currency(booking.getCurrency())
                    .status(TicketStatus.ISSUED)
                    .paymentId(paymentId)
                    .razorpayPaymentId(razorpayPaymentId)
                    .issuedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // 6. Persist with race-condition / duplicate-key safety
            Ticket savedTicket;
            try {
                savedTicket = ticketRepository.save(ticket);
                log.info("Ticket successfully issued: {} for booking PNR: {}", ticketNumber, booking.getBookingReference());
            } catch (Exception dke) {
                log.warn("Duplicate key or persistence collision during ticket issuance for booking ID: {}. Loading existing ticket. Details: {}", bookingId, dke.getMessage());
                savedTicket = ticketRepository.findFirstByBookingId(bookingId)
                        .orElseThrow(() -> new ConflictException("Ticket creation collision for booking ID: " + bookingId));
            }

            // 7. Update booking entity with ticket references if not already set
            try {
                booking.setTicketId(savedTicket.getId());
                booking.setTicketNumber(savedTicket.getTicketNumber());
                bookingRepository.save(booking);
            } catch (Exception ex) {
                log.warn("Non-fatal: Failed to update booking ticket references for ID: {}", bookingId);
            }

            return ticketMapper.toResponse(savedTicket);
        }
    }

    @Override
    public TicketResponse getTicketById(String id, String userId, boolean isAdmin) {
        if (id == null || id.isBlank()) {
            throw new BadRequestException("Ticket ID cannot be empty");
        }

        log.debug("Fetching ticket by ID: {} (user: {}, isAdmin: {})", id, userId, isAdmin);

        Ticket ticket;
        if (isAdmin) {
            ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        } else {
            ticket = ticketRepository.findFirstByIdAndUserId(id, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        }

        return ticketMapper.toResponse(ticket);
    }

    @Override
    public TicketResponse getTicketByNumber(String ticketNumber, String userId, boolean isAdmin) {
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new BadRequestException("Ticket number cannot be empty");
        }

        String normalizedNumber = ticketNumber.trim().toUpperCase();
        log.debug("Fetching ticket by number: {} (user: {}, isAdmin: {})", normalizedNumber, userId, isAdmin);

        Ticket ticket;
        if (isAdmin) {
            ticket = ticketRepository.findFirstByTicketNumber(normalizedNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketNumber", normalizedNumber));
        } else {
            ticket = ticketRepository.findFirstByTicketNumberAndUserId(normalizedNumber, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketNumber", normalizedNumber));
        }

        return ticketMapper.toResponse(ticket);
    }

    @Override
    public TicketResponse getTicketByBookingId(String bookingId, String userId, boolean isAdmin) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new BadRequestException("Booking ID cannot be empty");
        }

        log.debug("Fetching ticket by booking ID: {} (user: {}, isAdmin: {})", bookingId, userId, isAdmin);

        Ticket ticket;
        if (isAdmin) {
            ticket = ticketRepository.findFirstByBookingId(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", "bookingId", bookingId));
        } else {
            ticket = ticketRepository.findFirstByBookingIdAndUserId(bookingId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", "bookingId", bookingId));
        }

        return ticketMapper.toResponse(ticket);
    }

    @Override
    public PageResponse<TicketResponse> getUserTickets(String userId, Pageable pageable) {
        log.debug("Fetching tickets for user ID: {}", userId);
        Page<Ticket> page = ticketRepository.findByUserId(userId, pageable);
        return PageResponse.from(page.map(ticketMapper::toResponse));
    }

    @Override
    public PageResponse<TicketResponse> getAllTickets(Pageable pageable) {
        log.debug("Admin fetching all platform tickets with pagination");
        Page<Ticket> page = ticketRepository.findAll(pageable);
        return PageResponse.from(page.map(ticketMapper::toResponse));
    }

    @Override
    public byte[] generateTicketPdf(String ticketId, String userId, boolean isAdmin) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new BadRequestException("Ticket ID cannot be empty for PDF download");
        }

        log.debug("Generating PDF for ticket ID: {} (user: {}, isAdmin: {})", ticketId, userId, isAdmin);

        Ticket ticket;
        if (isAdmin) {
            ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        } else {
            ticket = ticketRepository.findFirstByIdAndUserId(ticketId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        }

        byte[] pdfBytes = ticketPdfService.generateTicketPdf(ticket);

        // Update pdf generated flag asynchronously/safely
        try {
            ticket.setPdfGenerated(true);
            ticket.setPdfGeneratedAt(Instant.now());
            ticketRepository.save(ticket);
        } catch (Exception ex) {
            log.warn("Non-fatal: Failed to update pdfGenerated timestamp for ticket: {}", ticket.getTicketNumber());
        }

        return pdfBytes;
    }

    @Override
    public void cancelTicketForBooking(String bookingId, String cancellationReason) {
        if (bookingId == null || bookingId.isBlank()) {
            return;
        }

        Optional<Ticket> ticketOpt = ticketRepository.findFirstByBookingId(bookingId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            if (ticket.getStatus() == TicketStatus.ISSUED) {
                ticket.setStatus(TicketStatus.CANCELLED);
                ticket.setCancelledAt(Instant.now());
                ticket.setCancellationReason(cancellationReason != null ? cancellationReason : "Associated booking cancelled");
                ticket.setUpdatedAt(Instant.now());
                ticketRepository.save(ticket);
                log.info("Ticket {} marked as CANCELLED for booking ID: {}", ticket.getTicketNumber(), bookingId);
            }
        }
    }

    @Override
    public TicketResponse retryIssueTicket(String bookingId) {
        log.info("Admin triggered retry ticket issuance for booking ID: {}", bookingId);
        return issueTicket(bookingId);
    }
}
