package com.smarttravel.modules.flight.requirement1;

import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingStateMachine;
import com.smarttravel.modules.flight.disruption.dto.FlightCancelRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightScheduleChangeRequest;
import com.smarttravel.modules.flight.disruption.model.FlightDisruption;
import com.smarttravel.modules.flight.disruption.repository.FlightDisruptionRepository;
import com.smarttravel.modules.flight.disruption.service.FlightDisruptionServiceImpl;
import com.smarttravel.modules.flight.impact.service.FlightImpactService;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.FlightStatusHistoryRepository;
import com.smarttravel.modules.flight.service.FlightStateMachine;
import com.smarttravel.modules.flight.service.SeatMapService;
import com.smarttravel.modules.notification.dto.NotificationSendRequest;
import com.smarttravel.modules.notification.model.Notification;
import com.smarttravel.modules.notification.model.NotificationChannel;
import com.smarttravel.modules.notification.model.NotificationType;
import com.smarttravel.modules.notification.provider.EmailNotificationProvider;
import com.smarttravel.modules.notification.provider.PushNotificationProvider;
import com.smarttravel.modules.notification.provider.SmsNotificationProvider;
import com.smarttravel.modules.notification.provider.WhatsAppNotificationProvider;
import com.smarttravel.modules.notification.repository.NotificationRepository;
import com.smarttravel.modules.notification.service.NotificationServiceImpl;
import com.smarttravel.modules.payment.refund.service.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Requirement #1 - Test Group E: Notifications & Multi-Channel Dispatch Audit
 * Verifies that operational changes (delays, reschedules, cancellations, gate/terminal changes)
 * dispatch targeted notifications containing flight number, schedule, reason, and enforce idempotency.
 */
@ExtendWith(MockitoExtension.class)
class FlightDisruptionNotificationAuditTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightDisruptionRepository disruptionRepository;

    @Mock
    private FlightStatusHistoryRepository statusHistoryRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightStateMachine flightStateMachine;

    @Mock
    private BookingStateMachine bookingStateMachine;

    @Mock
    private FlightImpactService flightImpactService;

    @Mock
    private RefundService refundService;

    @Mock
    private NotificationServiceImpl notificationService;

    @Mock
    private SeatMapService seatMapService;

    @InjectMocks
    private FlightDisruptionServiceImpl disruptionService;

    private Flight testFlight;
    private Booking confirmedBooking;

    @BeforeEach
    void setUp() {
        testFlight = Flight.builder()
                .id("fl-notif-100")
                .flightNumber("6E-555")
                .airline("IndiGo")
                .status(FlightStatus.SCHEDULED)
                .departureAirport(AirportInfo.builder().code("DEL").city("Delhi").terminal("T3").gate("Gate 02").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").city("Mumbai").terminal("T2").build())
                .departureTime(Instant.parse("2026-11-01T10:00:00Z"))
                .arrivalTime(Instant.parse("2026-11-01T12:15:00Z"))
                .active(true)
                .build();

        confirmedBooking = Booking.builder()
                .id("bkg-notif-01")
                .userId("user-passenger-01")
                .flightId("fl-notif-100")
                .bookingReference("PNR555NOTIF")
                .status(BookingStatus.CONFIRMED)
                .build();
    }

    @Test
    @DisplayName("42-43. Reschedule / departure-time change creates a targeted notification with schedule details")
    void testRescheduleFlightCreatesNotification() {
        Instant newDep = testFlight.getDepartureTime().plus(90, ChronoUnit.MINUTES);
        Instant newArr = testFlight.getArrivalTime().plus(90, ChronoUnit.MINUTES);

        when(flightRepository.findById("fl-notif-100")).thenReturn(Optional.of(testFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));
        when(disruptionRepository.save(any(FlightDisruption.class))).thenAnswer(i -> {
            FlightDisruption d = i.getArgument(0);
            d.setId("disr-001");
            return d;
        });
        when(bookingRepository.findByFlightId("fl-notif-100")).thenReturn(List.of(confirmedBooking));

        FlightScheduleChangeRequest req = new FlightScheduleChangeRequest(
                newDep,
                newArr,
                "Severe air traffic flow management hold",
                "ATFM slot delay"
        );

        disruptionService.rescheduleFlight("fl-notif-100", req, "admin-ops");

        ArgumentCaptor<NotificationSendRequest> notifCaptor = ArgumentCaptor.forClass(NotificationSendRequest.class);
        verify(notificationService).sendNotification(notifCaptor.capture());

        NotificationSendRequest notif = notifCaptor.getValue();
        assertThat(notif.getUserId()).isEqualTo("user-passenger-01");
        assertThat(notif.getFlightId()).isEqualTo("fl-notif-100");
        assertThat(notif.getNotificationType()).isEqualTo(NotificationType.FLIGHT_RESCHEDULED);
        assertThat(notif.getSubject()).contains("6E-555");
        assertThat(notif.getContent()).contains("Severe air traffic flow management hold");
        assertThat(notif.getContent()).contains(newDep.toString());
    }

    @Test
    @DisplayName("46-50. Cancellation event creates notification belonging to confirmed user with reason")
    void testCancellationNotificationContentAndOwnership() {
        when(flightRepository.findById("fl-notif-100")).thenReturn(Optional.of(testFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));
        when(disruptionRepository.save(any(FlightDisruption.class))).thenAnswer(i -> {
            FlightDisruption d = i.getArgument(0);
            d.setId("disr-002");
            return d;
        });
        when(bookingRepository.findByFlightId("fl-notif-100")).thenReturn(List.of(confirmedBooking));

        FlightCancelRequest cancelReq = new FlightCancelRequest(
                "Adverse cyclone weather warnings at destination",
                "Weather cancellation",
                false
        );

        disruptionService.cancelFlight("fl-notif-100", cancelReq, "admin-ops");

        ArgumentCaptor<NotificationSendRequest> notifCaptor = ArgumentCaptor.forClass(NotificationSendRequest.class);
        verify(notificationService).sendNotification(notifCaptor.capture());

        NotificationSendRequest notif = notifCaptor.getValue();
        assertThat(notif.getUserId()).isEqualTo("user-passenger-01");
        assertThat(notif.getNotificationType()).isEqualTo(NotificationType.FLIGHT_CANCELLED);
        assertThat(notif.getSubject()).contains("6E-555");
        assertThat(notif.getContent()).contains("Adverse cyclone weather warnings at destination");
    }

    @Test
    @DisplayName("51-52. Duplicate notification events are suppressed by composite idempotency key")
    void testNotificationIdempotencyKeySuppression() {
        NotificationRepository mockNotifRepo = mock(NotificationRepository.class);
        EmailNotificationProvider emailProv = mock(EmailNotificationProvider.class);
        SmsNotificationProvider smsProv = mock(SmsNotificationProvider.class);
        WhatsAppNotificationProvider waProv = mock(WhatsAppNotificationProvider.class);
        PushNotificationProvider pushProv = mock(PushNotificationProvider.class);

        NotificationServiceImpl service = new NotificationServiceImpl(
                mockNotifRepo, emailProv, smsProv, waProv, pushProv
        );

        NotificationSendRequest req = NotificationSendRequest.builder()
                .userId("user-1")
                .flightId("fl-100")
                .eventId("evt-delay-01")
                .notificationType(NotificationType.FLIGHT_DELAYED)
                .channel(NotificationChannel.EMAIL)
                .recipient("user1@example.com")
                .subject("Flight Delay Notice")
                .content("Your flight is delayed by 45m")
                .build();

        String expectedIdempotencyKey = "fl-100:evt-delay-01:user-1:FLIGHT_DELAYED:EMAIL";

        Notification existingNotif = Notification.builder()
                .id("notif-existing-1")
                .idempotencyKey(expectedIdempotencyKey)
                .userId("user-1")
                .build();

        when(mockNotifRepo.findByIdempotencyKey(expectedIdempotencyKey)).thenReturn(Optional.of(existingNotif));

        service.sendNotification(req);

        // When existing notification is found, no dispatch to email provider is made
        verify(emailProv, never()).sendEmail(any(), any(), any());
        verify(mockNotifRepo, never()).save(any(Notification.class));
    }
}
