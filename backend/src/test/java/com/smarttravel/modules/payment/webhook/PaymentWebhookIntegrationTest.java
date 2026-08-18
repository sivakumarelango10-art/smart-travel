package com.smarttravel.modules.payment.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.smarttravel.modules.payment.config.RazorpayProperties;
import com.smarttravel.modules.payment.dto.PaymentOrderCreateRequest;
import com.smarttravel.modules.payment.dto.PaymentOrderResponse;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import com.smarttravel.modules.payment.service.PaymentService;
import com.smarttravel.modules.payment.webhook.model.PaymentWebhookEvent;
import com.smarttravel.modules.payment.webhook.model.WebhookProcessingStatus;
import com.smarttravel.modules.payment.webhook.repository.PaymentWebhookEventRepository;
import com.smarttravel.modules.payment.webhook.service.PaymentWebhookService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentWebhookIntegrationTest {

    @Autowired
    private PaymentWebhookService paymentWebhookService;

    @Autowired
    private PaymentWebhookEventRepository webhookEventRepository;

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

    @Autowired
    private RazorpayProperties razorpayProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private String flightId;
    private String bookingId;
    private String paymentId;
    private String razorpayOrderId;
    private final List<String> createdWebhookEventIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 1. Create Flight
        AirportDto del = AirportDto.builder().code("DEL").name("Delhi Airport").city("Delhi").country("India").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("Mumbai Airport").city("Mumbai").country("India").build();

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
                .flightNumber("TEST-WH-" + System.currentTimeMillis())
                .airline("SmartAir Webhook")
                .airlineCode("SW")
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

        // 2. Create Booking
        PassengerDto p = PassengerDto.builder()
                .title("Ms")
                .firstName("Sarah")
                .lastName("Connor")
                .dateOfBirth(LocalDate.of(1992, 3, 10))
                .gender("FEMALE")
                .nationality("Indian")
                .build();

        BookingCreateRequest bkgReq = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(p))
                .build();

        BookingResponse bkgRes = bookingService.createBooking(bkgReq, "user-sarah", "sarah@smarttravel.com");
        bookingId = bkgRes.getId();

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        // 3. Create Payment Order
        PaymentOrderCreateRequest orderReq = PaymentOrderCreateRequest.builder()
                .bookingId(bookingId)
                .notes("Webhook test payment")
                .build();

        PaymentOrderResponse orderRes = paymentService.createPaymentOrder(orderReq, "user-sarah", "sarah@smarttravel.com");
        paymentId = orderRes.getPaymentId();
        razorpayOrderId = orderRes.getRazorpayOrderId();
    }

    @AfterEach
    void tearDown() {
        for (String id : createdWebhookEventIds) {
            try {
                webhookEventRepository.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        if (paymentId != null) {
            try {
                paymentRepository.deleteById(paymentId);
            } catch (Exception ignored) {
            }
        }
        if (bookingId != null) {
            try {
                bookingRepository.deleteById(bookingId);
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

    private String calculateSignature(byte[] payload) throws Exception {
        String secret = razorpayProperties.getWebhookSecret();
        if (secret == null || secret.isBlank()) {
            secret = razorpayProperties.getKeySecret();
        }
        if (secret == null || secret.isBlank()) {
            secret = "smarttravel_dev_secret_key";
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(payload));
    }

    @Test
    @DisplayName("End-to-End Webhook: payment.captured event updates Payment to VERIFIED and Booking to CONFIRMED")
    void testWebhookPaymentCaptured_EndToEnd() throws Exception {
        String eventId = "evt_it_" + System.currentTimeMillis();
        String jsonPayload = String.format("""
                {
                  "event_id": "%s",
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_it_captured_99",
                        "order_id": "%s",
                        "amount": 575000,
                        "currency": "INR",
                        "status": "captured"
                      }
                    }
                  }
                }
                """, eventId, razorpayOrderId);

        byte[] payloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
        String signature = calculateSignature(payloadBytes);

        // 1. Dispatch webhook
        PaymentWebhookEvent resultEvent = paymentWebhookService.handleWebhook(payloadBytes, signature);
        createdWebhookEventIds.add(resultEvent.getId());

        assertThat(resultEvent.getProcessingStatus()).isEqualTo(WebhookProcessingStatus.PROCESSED);
        assertThat(resultEvent.getRazorpayOrderId()).isEqualTo(razorpayOrderId);
        assertThat(resultEvent.getRazorpayPaymentId()).isEqualTo("pay_it_captured_99");

        // 2. Assert Payment updated in MongoDB Atlas
        Payment updatedPayment = paymentRepository.findById(paymentId).orElseThrow();
        assertThat(updatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);
        assertThat(updatedPayment.getRazorpayPaymentId()).isEqualTo("pay_it_captured_99");
        assertThat(updatedPayment.getVerifiedAt()).isNotNull();

        // 3. Assert Booking updated in MongoDB Atlas
        Booking updatedBooking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        // 4. Test Webhook Idempotency: Replay identical webhook
        PaymentWebhookEvent replayEvent = paymentWebhookService.handleWebhook(payloadBytes, signature);
        assertThat(replayEvent.getId()).isEqualTo(resultEvent.getId());
        assertThat(replayEvent.getProcessingStatus()).isEqualTo(WebhookProcessingStatus.PROCESSED);
    }
}
