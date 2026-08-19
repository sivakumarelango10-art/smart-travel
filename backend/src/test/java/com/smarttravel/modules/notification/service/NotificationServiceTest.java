package com.smarttravel.modules.notification.service;

import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.notification.dto.NotificationResponse;
import com.smarttravel.modules.notification.dto.NotificationSendRequest;
import com.smarttravel.modules.notification.model.Notification;
import com.smarttravel.modules.notification.model.NotificationChannel;
import com.smarttravel.modules.notification.model.NotificationStatus;
import com.smarttravel.modules.notification.model.NotificationType;
import com.smarttravel.modules.notification.provider.EmailNotificationProvider;
import com.smarttravel.modules.notification.provider.PushNotificationProvider;
import com.smarttravel.modules.notification.provider.SmsNotificationProvider;
import com.smarttravel.modules.notification.provider.WhatsAppNotificationProvider;
import com.smarttravel.modules.notification.repository.NotificationRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailNotificationProvider emailProvider;

    @Mock
    private SmsNotificationProvider smsProvider;

    @Mock
    private WhatsAppNotificationProvider whatsAppProvider;

    @Mock
    private PushNotificationProvider pushProvider;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("Should send email notification and persist record with composite idempotency key")
    void shouldSendEmailNotificationSuccessfully() {
        NotificationSendRequest req = NotificationSendRequest.builder()
                .userId("user-1")
                .bookingId("book-1")
                .flightId("flight-1")
                .notificationType(NotificationType.FLIGHT_CANCELLED)
                .channel(NotificationChannel.EMAIL)
                .recipient("sarah@smarttravel.com")
                .subject("Flight Cancelled")
                .content("Your flight has been cancelled")
                .eventId("evt-101")
                .build();

        when(notificationRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(emailProvider.sendEmail(any(), any(), any())).thenReturn("msg-email-123");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            if (n.getId() == null) n.setId("notif-1");
            return n;
        });

        NotificationResponse res = notificationService.sendNotification(req);

        assertThat(res).isNotNull();
        assertThat(res.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(res.getSubject()).isEqualTo("Flight Cancelled");

        verify(emailProvider).sendEmail(eq("sarah@smarttravel.com"), eq("Flight Cancelled"), any());
    }

    @Test
    @DisplayName("Should suppress duplicate notification using composite idempotency key")
    void shouldSuppressDuplicateNotification() {
        Notification existing = Notification.builder()
                .id("notif-existing")
                .userId("user-1")
                .status(NotificationStatus.SENT)
                .subject("Existing Notification")
                .build();

        NotificationSendRequest req = NotificationSendRequest.builder()
                .userId("user-1")
                .flightId("flight-1")
                .notificationType(NotificationType.FLIGHT_CANCELLED)
                .channel(NotificationChannel.EMAIL)
                .subject("Flight Cancelled")
                .content("Body")
                .eventId("evt-101")
                .build();

        when(notificationRepository.findByIdempotencyKey(any())).thenReturn(Optional.of(existing));

        NotificationResponse res = notificationService.sendNotification(req);

        assertThat(res.getId()).isEqualTo("notif-existing");
        verifyNoInteractions(emailProvider);
    }

    @Test
    @DisplayName("Should enforce strict ownership when marking notification as read (IDOR protection)")
    void shouldEnforceIdorOnMarkAsRead() {
        Notification notification = Notification.builder()
                .id("notif-1")
                .userId("user-owner")
                .read(false)
                .build();

        when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(notification));

        // Unauthorized user attempts to mark as read
        assertThatThrownBy(() -> notificationService.markAsRead("notif-1", "user-attacker", false))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
