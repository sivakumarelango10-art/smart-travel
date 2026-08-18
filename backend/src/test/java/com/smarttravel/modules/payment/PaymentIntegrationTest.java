package com.smarttravel.modules.payment;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
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
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.payment.dto.PaymentOrderCreateRequest;
import com.smarttravel.modules.payment.dto.PaymentOrderResponse;
import com.smarttravel.modules.payment.dto.PaymentResponse;
import com.smarttravel.modules.payment.dto.PaymentVerificationRequest;
import com.smarttravel.modules.payment.gateway.RazorpayPaymentGateway;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.payment.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class PaymentIntegrationTest {

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

    @MockBean
    private RazorpayPaymentGateway razorpayGateway;

    private String flightId;
    private String bookingId;
    private final List<String> createdPaymentIds = new ArrayList<>();
    private final List<String> createdBookingIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        AirportDto del = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").country("India").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").country("India").build();

        CabinInventoryDto econ = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(100)
                .basePrice(new BigDecimal("5000.00"))
                .taxAmount(new BigDecimal("600.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5750.00"))
                .build();

        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber("TEST-PAY-" + System.currentTimeMillis())
                .airline("SmartAir")
                .airlineCode("SA")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.now().plusSeconds(172800))
                .arrivalTime(Instant.now().plusSeconds(180000))
                .aircraftModel("A321neo")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(100)
                .availableSeats(100)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econ))
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse flightRes = flightService.createFlight(flightReq);
        flightId = flightRes.getId();

        PassengerDto p = PassengerDto.builder()
                .title("Mr")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender("MALE")
                .nationality("Indian")
                .build();

        BookingCreateRequest bkgReq = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(p))
                .build();

        BookingResponse bkgRes = bookingService.createBooking(bkgReq, "user-alice", "alice@smarttravel.com");
        bookingId = bkgRes.getId();
        createdBookingIds.add(bookingId);

        // Explicitly set booking to PENDING for payment workflow testing
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);
    }

    @AfterEach
    void tearDown() {
        for (String id : createdPaymentIds) {
            try {
                paymentRepository.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        for (String id : createdBookingIds) {
            try {
                bookingRepository.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        if (flightId != null) {
            try {
                flightRepository.deleteById(flightId);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("End-to-End Payment Flow: Create Order -> Verify Signature -> Booking CONFIRMED -> Idempotent Replay")
    void testEndToEndPaymentFlow() {
        // 1. Mock Gateway Order Creation
        com.smarttravel.modules.payment.gateway.dto.RazorpayOrderDto mockOrder =
                com.smarttravel.modules.payment.gateway.dto.RazorpayOrderDto.builder()
                        .id("order_mock_12345")
                        .amount(575000L)
                        .currency("INR")
                        .receipt("ST-TEST-RECEIPT")
                        .status("created")
                        .build();

        when(razorpayGateway.createOrder(anyString(), org.mockito.ArgumentMatchers.anyLong(), anyString(), org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(mockOrder);

        // 2. Create Payment Order
        PaymentOrderCreateRequest orderReq = PaymentOrderCreateRequest.builder()
                .bookingId(bookingId)
                .notes("Flight ticket payment")
                .build();

        PaymentOrderResponse orderRes = paymentService.createPaymentOrder(orderReq, "user-alice", "alice@smarttravel.com");
        createdPaymentIds.add(orderRes.getPaymentId());

        assertThat(orderRes.getRazorpayOrderId()).isEqualTo("order_mock_12345");
        assertThat(orderRes.getAmount()).isEqualTo(575000L);
        assertThat(orderRes.getAmountInRupees()).isEqualByComparingTo("5750.00");

        // 3. Verify Payment Record in MongoDB Atlas
        Payment savedPayment = paymentRepository.findById(orderRes.getPaymentId()).orElseThrow();
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.ORDER_CREATED);
        assertThat(savedPayment.getUserId()).isEqualTo("user-alice");

        // 4. Verify Signature with valid mock
        when(razorpayGateway.verifyPaymentSignature("order_mock_12345", "pay_mock_9999", "sig_valid_hex"))
                .thenReturn(true);

        PaymentVerificationRequest verifyReq = PaymentVerificationRequest.builder()
                .razorpayOrderId("order_mock_12345")
                .razorpayPaymentId("pay_mock_9999")
                .razorpaySignature("sig_valid_hex")
                .build();

        PaymentResponse verifyRes = paymentService.verifyPayment(verifyReq, "user-alice", "alice@smarttravel.com");
        assertThat(verifyRes.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);
        assertThat(verifyRes.getRazorpayPaymentId()).isEqualTo("pay_mock_9999");
        assertThat(verifyRes.getVerifiedAt()).isNotNull();

        // 5. Assert Booking Transitioned to CONFIRMED in MongoDB
        Booking confirmedBooking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(confirmedBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        // 6. Test Idempotency: Replay verify request
        PaymentResponse replayRes = paymentService.verifyPayment(verifyReq, "user-alice", "alice@smarttravel.com");
        assertThat(replayRes.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);
    }

    @Test
    @DisplayName("Payment Verification Failure: Invalid signature marks payment FAILED and throws BadRequestException")
    void testPaymentVerification_InvalidSignature() {
        com.smarttravel.modules.payment.gateway.dto.RazorpayOrderDto mockOrder =
                com.smarttravel.modules.payment.gateway.dto.RazorpayOrderDto.builder()
                        .id("order_mock_fail_1")
                        .amount(575000L)
                        .currency("INR")
                        .receipt("ST-FAIL")
                        .status("created")
                        .build();

        when(razorpayGateway.createOrder(anyString(), org.mockito.ArgumentMatchers.anyLong(), anyString(), org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(mockOrder);

        PaymentOrderCreateRequest orderReq = PaymentOrderCreateRequest.builder().bookingId(bookingId).build();
        PaymentOrderResponse orderRes = paymentService.createPaymentOrder(orderReq, "user-alice", "alice@smarttravel.com");
        createdPaymentIds.add(orderRes.getPaymentId());

        when(razorpayGateway.verifyPaymentSignature("order_mock_fail_1", "pay_bad", "bad_sig"))
                .thenReturn(false);

        PaymentVerificationRequest verifyReq = PaymentVerificationRequest.builder()
                .razorpayOrderId("order_mock_fail_1")
                .razorpayPaymentId("pay_bad")
                .razorpaySignature("bad_sig")
                .build();

        assertThrows(BadRequestException.class, () ->
                paymentService.verifyPayment(verifyReq, "user-alice", "alice@smarttravel.com")
        );

        Payment failedPayment = paymentRepository.findById(orderRes.getPaymentId()).orElseThrow();
        assertThat(failedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(failedPayment.getFailureReason()).isEqualTo("Invalid signature verification");

        // Booking remains PENDING
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    @DisplayName("Ownership Security: User Bob cannot access or pay User Alice's booking")
    void testOwnershipSecurity() {
        PaymentOrderCreateRequest orderReq = PaymentOrderCreateRequest.builder().bookingId(bookingId).build();

        assertThrows(ResourceNotFoundException.class, () ->
                paymentService.createPaymentOrder(orderReq, "user-bob", "bob@smarttravel.com")
        );
    }
}
