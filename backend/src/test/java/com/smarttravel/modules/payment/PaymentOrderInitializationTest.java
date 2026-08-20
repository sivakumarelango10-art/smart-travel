package com.smarttravel.modules.payment;

import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.payment.dto.PaymentOrderCreateRequest;
import com.smarttravel.modules.payment.dto.PaymentOrderResponse;
import com.smarttravel.modules.payment.dto.PaymentResponse;
import com.smarttravel.modules.payment.dto.PaymentVerificationRequest;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.payment.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PaymentOrderInitializationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    private String flightId;
    private final String userId = "user-init-test";
    private final String userEmail = "init_test@smarttravel.com";
    private final List<String> createdBookingIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        AirportDto del = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").country("India").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").country("India").build();

        CabinInventoryDto econ = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(100)
                .basePrice(new BigDecimal("4000.00"))
                .taxAmount(new BigDecimal("480.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("4630.00"))
                .build();

        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber("PAY-INIT-" + System.currentTimeMillis())
                .airline("Payment Airways")
                .airlineCode("PA")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.now().plusSeconds(86400 * 3))
                .arrivalTime(Instant.now().plusSeconds(86400 * 3 + 7200))
                .aircraftModel("Boeing 737 MAX 8")
                .basePrice(new BigDecimal("4000.00"))
                .totalSeats(100)
                .availableSeats(100)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econ))
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse flight = flightService.createFlight(flightReq);
        this.flightId = flight.getId();
    }

    @AfterEach
    void tearDown() {
        createdBookingIds.forEach(id -> {
            paymentRepository.findByBookingId(id).forEach(p -> paymentRepository.deleteById(p.getId()));
            bookingRepository.deleteById(id);
        });
        if (flightId != null) {
            flightRepository.deleteById(flightId);
        }
    }

    private BookingResponse createTestBooking(String user) {
        PassengerDto pax = PassengerDto.builder()
                .title("Mr")
                .firstName("John")
                .lastName("Doe")
                .gender("MALE")
                .nationality("Indian")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        BookingCreateRequest bkgReq = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(pax))
                .build();

        BookingResponse bkg = bookingService.createBooking(bkgReq, user, userEmail);
        createdBookingIds.add(bkg.getId());
        return bkg;
    }

    @Test
    @DisplayName("1. Authenticated user can initialize payment order with exact paise calculation")
    void testCreatePaymentOrder_Success() {
        BookingResponse booking = createTestBooking(userId);

        PaymentOrderCreateRequest req = new PaymentOrderCreateRequest(booking.getId(), "Payment note");
        PaymentOrderResponse orderRes = paymentService.createPaymentOrder(req, userId, userEmail);

        assertThat(orderRes).isNotNull();
        assertThat(orderRes.getRazorpayOrderId()).startsWith("order_");
        assertThat(orderRes.getBookingId()).isEqualTo(booking.getId());
        assertThat(orderRes.getBookingReference()).isEqualTo(booking.getBookingReference());
        // Amount check: ₹4630.00 -> 463000 paise
        long expectedPaise = booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValueExact();
        assertThat(orderRes.getAmount()).isEqualTo(expectedPaise);
        assertThat(orderRes.getAmountInRupees()).isEqualByComparingTo(booking.getTotalAmount());
        assertThat(orderRes.getKeyId()).isNotNull();
    }

    @Test
    @DisplayName("2. Lookup by PNR booking reference succeeds seamlessly")
    void testCreatePaymentOrder_ByPnrReference() {
        BookingResponse booking = createTestBooking(userId);

        // Pass PNR instead of MongoDB ObjectId
        PaymentOrderCreateRequest req = new PaymentOrderCreateRequest(booking.getBookingReference(), "PNR test");
        PaymentOrderResponse orderRes = paymentService.createPaymentOrder(req, userId, userEmail);

        assertThat(orderRes).isNotNull();
        assertThat(orderRes.getBookingReference()).isEqualTo(booking.getBookingReference());
        assertThat(orderRes.getRazorpayOrderId()).isNotBlank();
    }

    @Test
    @DisplayName("3. User cannot initialize payment for another user's booking")
    void testCreatePaymentOrder_UnauthorizedOtherUser() {
        BookingResponse booking = createTestBooking(userId);

        PaymentOrderCreateRequest req = new PaymentOrderCreateRequest(booking.getId(), "Hacker note");
        assertThrows(ResourceNotFoundException.class, () ->
                paymentService.createPaymentOrder(req, "other-user-999", "other@smarttravel.com"));
    }

    @Test
    @DisplayName("4. Invalid booking ID throws ResourceNotFoundException")
    void testCreatePaymentOrder_InvalidBookingId() {
        PaymentOrderCreateRequest req = new PaymentOrderCreateRequest("invalid-bkg-id-9999", "Invalid");
        assertThrows(ResourceNotFoundException.class, () ->
                paymentService.createPaymentOrder(req, userId, userEmail));
    }

    @Test
    @DisplayName("5. Idempotent order creation returns existing active order")
    void testCreatePaymentOrder_Idempotency() {
        BookingResponse booking = createTestBooking(userId);

        PaymentOrderCreateRequest req = new PaymentOrderCreateRequest(booking.getId(), "First note");
        PaymentOrderResponse firstOrder = paymentService.createPaymentOrder(req, userId, userEmail);

        PaymentOrderResponse secondOrder = paymentService.createPaymentOrder(req, userId, userEmail);

        assertThat(secondOrder.getRazorpayOrderId()).isEqualTo(firstOrder.getRazorpayOrderId());
        assertThat(secondOrder.getPaymentId()).isEqualTo(firstOrder.getPaymentId());
    }

    @Test
    @DisplayName("6. Cryptographic signature verification and booking confirmation")
    void testVerifyPayment_Success() {
        BookingResponse booking = createTestBooking(userId);

        PaymentOrderCreateRequest req = new PaymentOrderCreateRequest(booking.getId(), "Verification test");
        PaymentOrderResponse orderRes = paymentService.createPaymentOrder(req, userId, userEmail);

        // Verify with simulated signature
        PaymentVerificationRequest verifyReq = new PaymentVerificationRequest(
                orderRes.getRazorpayOrderId(),
                "pay_test_" + System.currentTimeMillis(),
                "sim_sig_" + System.currentTimeMillis()
        );

        PaymentResponse paymentRes = paymentService.verifyPayment(verifyReq, userId, userEmail);
        assertThat(paymentRes.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);

        BookingResponse updatedBooking = bookingService.getBookingById(booking.getId(), userId, false);
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }
}
