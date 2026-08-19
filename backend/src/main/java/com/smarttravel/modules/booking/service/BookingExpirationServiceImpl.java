package com.smarttravel.modules.booking.service;

import com.mongodb.client.result.UpdateResult;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.payment.service.PaymentStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Implementation of BookingExpirationService with atomic conditional state transitions,
 * compensating seat releases, and individual failure isolation.
 */
@Service
public class BookingExpirationServiceImpl implements BookingExpirationService {

    private static final Logger log = LoggerFactory.getLogger(BookingExpirationServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final FlightInventoryReservationService reservationService;
    private final PaymentRepository paymentRepository;
    private final PaymentStateMachine paymentStateMachine;
    private final MongoTemplate mongoTemplate;
    private final com.smarttravel.modules.flight.service.SeatMapService seatMapService;

    @org.springframework.beans.factory.annotation.Autowired
    public BookingExpirationServiceImpl(BookingRepository bookingRepository,
                                        FlightInventoryReservationService reservationService,
                                        PaymentRepository paymentRepository,
                                        PaymentStateMachine paymentStateMachine,
                                        MongoTemplate mongoTemplate,
                                        @org.springframework.beans.factory.annotation.Autowired(required = false) @org.springframework.context.annotation.Lazy com.smarttravel.modules.flight.service.SeatMapService seatMapService) {
        this.bookingRepository = bookingRepository;
        this.reservationService = reservationService;
        this.paymentRepository = paymentRepository;
        this.paymentStateMachine = paymentStateMachine;
        this.mongoTemplate = mongoTemplate;
        this.seatMapService = seatMapService;
    }

    public BookingExpirationServiceImpl(BookingRepository bookingRepository,
                                        FlightInventoryReservationService reservationService,
                                        PaymentRepository paymentRepository,
                                        PaymentStateMachine paymentStateMachine,
                                        MongoTemplate mongoTemplate) {
        this(bookingRepository, reservationService, paymentRepository, paymentStateMachine, mongoTemplate, null);
    }

    @Override
    public int expireOverdueBookings() {
        Instant now = Instant.now();
        List<Booking> overdueBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING, now);

        if (overdueBookings.isEmpty()) {
            return 0;
        }

        log.info("Found {} overdue PENDING bookings eligible for expiration", overdueBookings.size());
        int expiredCount = 0;

        for (Booking booking : overdueBookings) {
            try {
                boolean expired = expireSingleBooking(booking);
                if (expired) {
                    expiredCount++;
                }
            } catch (Exception ex) {
                log.error("Failed to process expiration for booking ID: {}. Continuing with remaining bookings.", booking.getId(), ex);
            }
        }

        log.info("Successfully expired {} out of {} overdue bookings", expiredCount, overdueBookings.size());
        return expiredCount;
    }

    @Override
    public boolean expireBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            return false;
        }
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return false;
        }
        return expireSingleBooking(booking);
    }

    private boolean expireSingleBooking(Booking booking) {
        // 1. Atomic conditional transition PENDING -> EXPIRED
        Query query = Query.query(
                Criteria.where("_id").is(booking.getId())
                        .and("status").is(BookingStatus.PENDING)
        );

        Update update = new Update()
                .set("status", BookingStatus.EXPIRED)
                .set("updatedAt", Instant.now());

        UpdateResult result = mongoTemplate.updateFirst(query, update, Booking.class);

        if (result.getModifiedCount() == 0) {
            // Concurrent confirmation or cancellation won the race
            log.info("Booking ID: {} is no longer in PENDING state (concurrently updated). Skipping seat release.", booking.getId());
            return false;
        }

        // 2. Atomically release reserved cabin seats back to inventory
        boolean seatsReleased = reservationService.releaseSeats(
                booking.getFlightId(),
                booking.getCabinClass(),
                booking.getPassengerCount()
        );

        if (seatsReleased) {
            log.info("Booking ID: {} (PNR: {}) EXPIRED. Released {} seats in cabin {} for flight ID: {}",
                    booking.getId(), booking.getBookingReference(), booking.getPassengerCount(), booking.getCabinClass(), booking.getFlightId());
        } else {
            log.error("Failed to release seats for expired booking ID: {}, flight ID: {}, cabin: {}",
                    booking.getId(), booking.getFlightId(), booking.getCabinClass());
        }

        // Release physical seats
        if (seatMapService != null) {
            try {
                seatMapService.releaseSeats(booking.getId());
            } catch (Exception ex) {
                log.warn("Non-fatal: Error releasing physical seats for expired booking ID: {}", booking.getId(), ex);
            }
        }

        // 3. Mark active payments for this booking as EXPIRED
        try {
            List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
            for (Payment p : payments) {
                if (paymentStateMachine.isValidTransition(p.getPaymentStatus(), PaymentStatus.EXPIRED)) {
                    p.setPaymentStatus(PaymentStatus.EXPIRED);
                    p.setFailureReason("Booking expired due to payment timeout");
                    p.setUpdatedAt(Instant.now());
                    paymentRepository.save(p);
                }
            }
        } catch (Exception ex) {
            log.warn("Non-fatal: Error updating payment status to EXPIRED for booking ID: {}", booking.getId(), ex);
        }

        return true;
    }
}
