package com.smarttravel.modules.ticket.repository;

import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Optional<Ticket> findByBookingId(String bookingId);

    Optional<Ticket> findByBookingReference(String bookingReference);

    Optional<Ticket> findByIdAndUserId(String id, String userId);

    Optional<Ticket> findByTicketNumberAndUserId(String ticketNumber, String userId);

    Optional<Ticket> findByBookingIdAndUserId(String bookingId, String userId);

    Optional<Ticket> findFirstByTicketNumber(String ticketNumber);

    Optional<Ticket> findFirstByBookingId(String bookingId);

    Optional<Ticket> findFirstByBookingReference(String bookingReference);

    Optional<Ticket> findFirstByIdAndUserId(String id, String userId);

    Optional<Ticket> findFirstByTicketNumberAndUserId(String ticketNumber, String userId);

    Optional<Ticket> findFirstByBookingIdAndUserId(String bookingId, String userId);

    Page<Ticket> findByUserId(String userId, Pageable pageable);

    Page<Ticket> findByFlightId(String flightId, Pageable pageable);

    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    List<Ticket> findByFlightIdAndStatus(String flightId, TicketStatus status);

    boolean existsByTicketNumber(String ticketNumber);

    boolean existsByBookingId(String bookingId);
}
