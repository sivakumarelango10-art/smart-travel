package com.smarttravel.modules.ticket;

import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.ticket.dto.TicketResponse;
import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.model.TicketStatus;
import com.smarttravel.modules.ticket.repository.TicketRepository;
import com.smarttravel.modules.ticket.service.TicketService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TicketUniquenessAndConcurrencyIntegrationTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightService flightService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String flightId;
    private String bookingId;
    private final List<String> cleanBookingIds = new ArrayList<>();
    private final List<String> cleanTicketIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        AirportDto del = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").country("India").build();
        AirportDto blr = AirportDto.builder().code("BLR").name("Kempegowda Int Airport").city("Bangalore").country("India").build();

        CabinInventoryDto econ = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(50)
                .availableSeats(50)
                .basePrice(new BigDecimal("3500.00"))
                .build();

        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber("TICKET-UNIQ-" + System.currentTimeMillis())
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(del)
                .arrivalAirport(blr)
                .departureTime(Instant.now().plus(4, ChronoUnit.DAYS))
                .arrivalTime(Instant.now().plus(4, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS))
                .aircraftModel("A320neo")
                .basePrice(new BigDecimal("3500.00"))
                .totalSeats(50)
                .availableSeats(50)
                .cabinClasses(java.util.Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econ))
                .status(com.smarttravel.modules.flight.model.FlightStatus.SCHEDULED)
                .build();

        FlightResponse flight = flightService.createFlight(flightReq);
        flightId = flight.getId();

        PassengerDto p = PassengerDto.builder()
                .title("Mr")
                .firstName("Unique")
                .lastName("Passenger")
                .dateOfBirth(LocalDate.of(1992, 5, 10))
                .gender("MALE")
                .nationality("Indian")
                .build();

        BookingCreateRequest bookingReq = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(p))
                .build();

        var booking = bookingService.createBooking(bookingReq, "user-ticket-test", "user@tickettest.com");
        bookingId = booking.getId();
        cleanBookingIds.add(bookingId);

        // Confirm booking and create verified payment
        Booking b = bookingRepository.findById(bookingId).orElseThrow();
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b);

        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .userId("user-ticket-test")
                .amount(new BigDecimal("3500.00"))
                .currency("INR")
                .paymentStatus(PaymentStatus.VERIFIED)
                .razorpayPaymentId("pay_uniq_" + UUID.randomUUID().toString().substring(0, 8))
                .createdAt(Instant.now())
                .build();
        paymentRepository.save(payment);
    }

    @AfterEach
    void tearDown() {
        for (String bId : cleanBookingIds) {
            ticketRepository.findFirstByBookingId(bId).ifPresent(t -> ticketRepository.deleteById(t.getId()));
            bookingRepository.deleteById(bId);
            paymentRepository.findByBookingId(bId).forEach(p -> paymentRepository.deleteById(p.getId()));
        }
        for (String tId : cleanTicketIds) {
            ticketRepository.deleteById(tId);
        }
    }

    @Test
    @DisplayName("MongoDB Ticket collection has verified unique index on bookingId")
    void testUniqueIndexExistsOnBookingId() {
        List<IndexInfo> indexInfoList = mongoTemplate.indexOps("tickets").getIndexInfo();
        boolean hasUniqueBookingIdIndex = indexInfoList.stream()
                .anyMatch(idx -> idx.isUnique() && idx.getIndexFields().stream().anyMatch(f -> "bookingId".equals(f.getKey())));

        assertTrue(hasUniqueBookingIdIndex, "Unique index on bookingId must exist in tickets collection");
    }

    @Test
    @DisplayName("Direct database duplicate insert for same bookingId is rejected by MongoDB unique constraint")
    void testDatabaseDuplicateConstraintEnforced() {
        // Issue valid ticket
        TicketResponse first = ticketService.issueTicket(bookingId);
        assertNotNull(first.getTicketNumber());

        // Attempt direct duplicate insertion bypassing service layer
        Ticket duplicate = Ticket.builder()
                .ticketNumber("ST-DUP-" + System.currentTimeMillis())
                .bookingId(bookingId)
                .userId("user-ticket-test")
                .flightId(flightId)
                .status(TicketStatus.ISSUED)
                .issuedAt(Instant.now())
                .build();

        assertThrows(DuplicateKeyException.class, () -> mongoTemplate.insert(duplicate, "tickets"));
    }

    @Test
    @DisplayName("Concurrent ticket issuance requests result in exactly ONE ticket issued idempotently")
    void testConcurrentTicketIssuanceIdempotency() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<TicketResponse>> tasks = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            tasks.add(() -> ticketService.issueTicket(bookingId));
        }

        List<Future<TicketResponse>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        List<String> ticketNumbers = new ArrayList<>();
        for (Future<TicketResponse> future : futures) {
            TicketResponse res = future.get();
            assertNotNull(res);
            assertNotNull(res.getTicketNumber());
            ticketNumbers.add(res.getTicketNumber());
        }

        // All threads must receive the EXACT SAME ticket number
        String expectedNumber = ticketNumbers.getFirst();
        for (String num : ticketNumbers) {
            assertEquals(expectedNumber, num, "All concurrent callers must receive the identical ticket number");
        }

        // Database must contain strictly 1 ticket for this bookingId
        long ticketCount = mongoTemplate.getCollection("tickets")
                .countDocuments(new org.bson.Document("bookingId", bookingId));
        assertEquals(1, ticketCount, "Database must hold exactly 1 ticket for the bookingId");
    }
}
