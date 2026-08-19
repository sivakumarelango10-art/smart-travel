package com.smarttravel.modules.ticket;

import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
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
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.ticket.dto.TicketResponse;
import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.repository.TicketRepository;
import com.smarttravel.modules.ticket.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Ticket Issuance Concurrency Integration Test")
class TicketIssuanceConcurrencyIntegrationTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    @DisplayName("10 Concurrent Issuance Requests for Same Booking -> Exactly 1 Ticket Created")
    void testConcurrentTicketIssuanceSafety() throws InterruptedException {
        String testSuffix = String.valueOf(System.currentTimeMillis());

        // 1. Create Flight
        AirportDto del = AirportDto.builder().code("DEL").name("Delhi Airport").city("Delhi").country("India").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("Mumbai Airport").city("Mumbai").country("India").build();

        CabinInventoryDto econ = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(50)
                .availableSeats(50)
                .basePrice(new BigDecimal("4500.00"))
                .taxAmount(new BigDecimal("500.00"))
                .feeAmount(new BigDecimal("175.00"))
                .totalPrice(new BigDecimal("5175.00"))
                .build();

        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber("CONCUR-TKT-" + testSuffix)
                .airline("Concurrency Express")
                .airlineCode("CE")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.now().plusSeconds(86400))
                .arrivalTime(Instant.now().plusSeconds(93600))
                .aircraftModel("Boeing 737")
                .basePrice(new BigDecimal("4500.00"))
                .totalSeats(50)
                .availableSeats(50)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econ))
                .status(FlightStatus.SCHEDULED)
                .build();
        FlightResponse flight = flightService.createFlight(flightReq);

        // 2. Create Booking
        PassengerDto p = PassengerDto.builder()
                .title("Mr")
                .firstName("Alex")
                .lastName("Murphy")
                .dateOfBirth(LocalDate.of(1985, 8, 20))
                .gender("MALE")
                .nationality("Indian")
                .build();
        BookingCreateRequest bookingReq = BookingCreateRequest.builder()
                .flightId(flight.getId())
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(p))
                .build();
        BookingResponse booking = bookingService.createBooking(bookingReq, "user-alex", "alex@smarttravel.com");

        // Manually confirm booking & record verified payment for concurrency test setup
        Booking bookingEntity = bookingRepository.findById(booking.getId()).orElseThrow();
        bookingEntity.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(bookingEntity);

        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .userId("user-alex")
                .amount(new BigDecimal("5175.00"))
                .paymentStatus(PaymentStatus.VERIFIED)
                .razorpayPaymentId("pay_concur_" + testSuffix)
                .createdAt(Instant.now())
                .build();
        paymentRepository.save(payment);

        // 3. Launch 10 concurrent threads issuing ticket for this booking
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        List<TicketResponse> responses = Collections.synchronizedList(new ArrayList<>());
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    TicketResponse res = ticketService.issueTicket(booking.getId());
                    responses.add(res);
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // trigger all threads simultaneously
        boolean completed = finishLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(exceptions).isEmpty();
        assertThat(responses).hasSize(threadCount);

        // 4. Verify that all threads got the EXACT SAME ticket number
        String firstTicketNumber = responses.get(0).getTicketNumber();
        assertThat(firstTicketNumber).isNotNull();
        for (TicketResponse res : responses) {
            assertThat(res.getTicketNumber()).isEqualTo(firstTicketNumber);
            assertThat(res.getBookingId()).isEqualTo(booking.getId());
        }

        // 5. Verify database has EXACTLY ONE ticket record for this bookingId
        List<Ticket> dbTickets = ticketRepository.findAll().stream()
                .filter(t -> booking.getId().equals(t.getBookingId()))
                .toList();

        assertThat(dbTickets).hasSize(1);
        assertThat(dbTickets.get(0).getTicketNumber()).isEqualTo(firstTicketNumber);
    }
}
