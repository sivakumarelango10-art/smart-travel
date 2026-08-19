package com.smarttravel.modules.flight.disruption;

import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.flight.disruption.dto.FlightCancelRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightOperationalStatusResponse;
import com.smarttravel.modules.flight.disruption.dto.FlightScheduleChangeRequest;
import com.smarttravel.modules.flight.disruption.service.FlightDisruptionService;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.SeatMapResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.model.SeatStatus;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.flight.service.SeatMapService;
import com.smarttravel.modules.notification.model.NotificationType;
import com.smarttravel.modules.notification.service.NotificationService;
import com.smarttravel.modules.payment.dto.PaymentOrderCreateRequest;
import com.smarttravel.modules.payment.dto.PaymentOrderResponse;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.refund.dto.RefundResponse;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import com.smarttravel.modules.payment.refund.service.RefundService;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlightDisruptionIntegrationTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightDisruptionService disruptionService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundService refundService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SeatMapService seatMapService;

    @Test
    @DisplayName("End-to-End Operational Lifecycle: Flight Reschedule -> Cancel -> Auto Refund -> Passenger Notification -> Seat Release")
    void testEndToEndDisruptionFlow() {
        long timestamp = System.currentTimeMillis();
        String flightNumber = "ST-DISRUPT-" + timestamp;
        Instant depTime = Instant.now().plus(12, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);
        Instant arrTime = Instant.now().plus(14, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);

        // 1. Create Flight
        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber(flightNumber)
                .airline("SmartTravel Disruption Air")
                .airlineCode("SDA")
                .departureAirport(new AirportDto("DEL", "Delhi Airport", "Delhi", "India", "T3", "Gate 10"))
                .arrivalAirport(new AirportDto("BOM", "Mumbai Airport", "Mumbai", "India", "T2", "Gate 5"))
                .departureTime(depTime)
                .arrivalTime(arrTime)
                .aircraftModel("Boeing 737 MAX 8")
                .basePrice(BigDecimal.valueOf(4500.00))
                .totalSeats(150)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(
                        new CabinInventoryDto(CabinClass.ECONOMY, 150, 150, BigDecimal.valueOf(4500.00),
                                BigDecimal.valueOf(540.00), BigDecimal.valueOf(150.00), BigDecimal.valueOf(5190.00))
                ))
                .build();

        FlightResponse flight = flightService.createFlight(flightReq);
        String flightId = flight.getId();

        // 2. Create Booking with Seat 12A
        String userId = "user_disrupt_" + timestamp;
        PassengerDto pax = PassengerDto.builder()
                .title("Ms")
                .firstName("Sarah")
                .lastName("Connor")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .gender("FEMALE")
                .nationality("Indian")
                .passportNumber("P1234567")
                .seatNumber("12A")
                .build();

        BookingCreateRequest bookingReq = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(pax))
                .build();

        BookingResponse booking = bookingService.createBooking(bookingReq, userId, "sarah@smarttravel.com");
        String bookingId = booking.getId();

        // Verify Seat is HELD
        SeatMapResponse seatMap1 = seatMapService.getFlightSeatMap(flightId);
        assertThat(seatMap1.getSeats().stream().filter(s -> "12A".equals(s.getSeatNumber())).findFirst().get().getStatus())
                .isEqualTo(SeatStatus.HELD);

        // 3. Payment Capture & Verification
        PaymentOrderCreateRequest orderReq = PaymentOrderCreateRequest.builder()
                .bookingId(bookingId)
                .notes("Integration test payment")
                .build();
        PaymentOrderResponse order = paymentService.createPaymentOrder(orderReq, userId, "sarah@smarttravel.com");

        Payment payment = paymentRepository.findById(order.getPaymentId()).orElseThrow();
        payment.setPaymentStatus(PaymentStatus.VERIFIED);
        payment.setRazorpayPaymentId("pay_rzp_" + timestamp);
        paymentRepository.save(payment);

        Booking b = bookingRepository.findById(bookingId).orElseThrow();
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b);
        seatMapService.confirmSeats(bookingId);

        // 4. Admin Reschedules Flight
        Instant newDep = depTime.plus(2, ChronoUnit.HOURS);
        Instant newArr = arrTime.plus(2, ChronoUnit.HOURS);
        FlightScheduleChangeRequest schedReq = new FlightScheduleChangeRequest(newDep, newArr, "Air traffic congestion", "Slot revised");
        FlightOperationalStatusResponse schedRes = disruptionService.rescheduleFlight(flightId, schedReq, "admin_ops");

        assertThat(schedRes.getRevisedDepartureTime()).isEqualTo(newDep);
        assertThat(schedRes.getScheduledDepartureTime()).isEqualTo(depTime); // Published schedule preserved!

        // 5. Admin Cancels Flight with Auto-Refund
        FlightCancelRequest cancelReq = new FlightCancelRequest("Severe Thunderstorm Ground Stop", "All departures halted", true);
        FlightOperationalStatusResponse cancelRes = disruptionService.cancelFlight(flightId, cancelReq, "admin_ops");

        assertThat(cancelRes.getStatus()).isEqualTo(FlightStatus.CANCELLED);

        // 6. Verify Booking is marked CANCELLED
        BookingResponse cancelledBooking = bookingService.getBookingById(bookingId, userId, false);
        assertThat(cancelledBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        // 7. Verify Physical Seat 12A is released back to AVAILABLE
        SeatMapResponse seatMapAfterCancel = seatMapService.getFlightSeatMap(flightId);
        assertThat(seatMapAfterCancel.getSeats().stream().filter(s -> "12A".equals(s.getSeatNumber())).findFirst().get().getStatus())
                .isEqualTo(SeatStatus.AVAILABLE);

        // 8. Verify Refund was generated
        RefundResponse refund = refundService.getRefundByBookingId(bookingId, userId, false);
        assertThat(refund).isNotNull();
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);

        // 9. Verify Notifications recorded
        var notifs = notificationService.getUserNotifications(userId, PageRequest.of(0, 10));
        assertThat(notifs.getContent()).isNotEmpty();
        assertThat(notifs.getContent().stream().anyMatch(n -> n.getNotificationType() == NotificationType.FLIGHT_CANCELLED)).isTrue();
    }
}
