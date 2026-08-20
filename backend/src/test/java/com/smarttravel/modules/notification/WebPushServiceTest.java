package com.smarttravel.modules.notification;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.flight.tracking.model.TrackedFlight;
import com.smarttravel.modules.flight.tracking.repository.TrackedFlightRepository;
import com.smarttravel.modules.notification.dto.PushSubscriptionRequest;
import com.smarttravel.modules.notification.dto.WebPushPayload;
import com.smarttravel.modules.notification.model.PushSubscription;
import com.smarttravel.modules.notification.repository.PushSubscriptionRepository;
import com.smarttravel.modules.notification.service.WebPushService;
import com.smarttravel.modules.notification.service.WebPushServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebPushServiceTest {

    @Mock
    private PushSubscriptionRepository subscriptionRepository;

    @Mock
    private TrackedFlightRepository trackedFlightRepository;

    private WebPushService webPushService;

    @BeforeEach
    void setUp() {
        webPushService = new WebPushServiceImpl(subscriptionRepository, trackedFlightRepository);
    }

    @Test
    @DisplayName("Should register a new push subscription successfully")
    void registerSubscription_New_Success() {
        when(subscriptionRepository.findByUserIdAndEndpoint("user-1", "https://fcm.googleapis.com/fcm/send/sub-1"))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(PushSubscription.class))).thenAnswer(i -> {
            PushSubscription s = i.getArgument(0);
            s.setId("sub-id-1");
            return s;
        });

        PushSubscriptionRequest request = new PushSubscriptionRequest(
                "https://fcm.googleapis.com/fcm/send/sub-1",
                "p256dh-key-base64",
                "auth-key-base64",
                "Mozilla/5.0 Chrome"
        );

        PushSubscription result = webPushService.registerSubscription("user-1", request);

        assertThat(result.getId()).isEqualTo("sub-id-1");
        assertThat(result.getEndpoint()).isEqualTo("https://fcm.googleapis.com/fcm/send/sub-1");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should update existing subscription if endpoint is already registered")
    void registerSubscription_Existing_Updates() {
        PushSubscription existing = PushSubscription.builder()
                .id("sub-id-existing")
                .userId("user-1")
                .endpoint("https://fcm.googleapis.com/fcm/send/sub-1")
                .p256dhKey("old-key")
                .authKey("old-auth")
                .build();

        when(subscriptionRepository.findByUserIdAndEndpoint("user-1", "https://fcm.googleapis.com/fcm/send/sub-1"))
                .thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(PushSubscription.class))).thenAnswer(i -> i.getArgument(0));

        PushSubscriptionRequest request = new PushSubscriptionRequest(
                "https://fcm.googleapis.com/fcm/send/sub-1",
                "new-p256dh-key",
                "new-auth-key",
                "Mozilla/5.0 Firefox"
        );

        PushSubscription updated = webPushService.registerSubscription("user-1", request);

        assertThat(updated.getP256dhKey()).isEqualTo("new-p256dh-key");
        assertThat(updated.getAuthKey()).isEqualTo("new-auth-key");
    }

    @Test
    @DisplayName("Should remove subscription when unsubscribing")
    void removeSubscription_Success() {
        webPushService.removeSubscription("user-1", "https://fcm.googleapis.com/fcm/send/sub-1");
        verify(subscriptionRepository, times(1))
                .deleteByUserIdAndEndpoint("user-1", "https://fcm.googleapis.com/fcm/send/sub-1");
    }

    @Test
    @DisplayName("Should filter out non-critical push events and dispatch only critical ones")
    void sendPushForFlight_EventFiltering() {
        TrackedFlight tf = TrackedFlight.builder()
                .userId("user-tracker")
                .flightId("fl-100")
                .flightNumber("AI-101")
                .build();

        when(trackedFlightRepository.findByFlightIdAndActiveTrue("fl-100")).thenReturn(List.of(tf));
        when(subscriptionRepository.findByUserIdAndActiveTrue("user-tracker")).thenReturn(List.of(
                PushSubscription.builder().userId("user-tracker").endpoint("https://push.example.com").build()
        ));

        // Critical event: DELAYED -> should dispatch
        webPushService.sendPushForFlight("fl-100", "Flight Delayed", "30 mins late", "/tracked-flights", "DELAYED");
        verify(trackedFlightRepository, times(1)).findByFlightIdAndActiveTrue("fl-100");

        // Non-critical event: ON_TIME -> should be skipped
        webPushService.sendPushForFlight("fl-100", "Flight On Time", "Flight is on time", "/tracked-flights", "ON_TIME");
        verify(trackedFlightRepository, times(1)).findByFlightIdAndActiveTrue("fl-100"); // Not called a second time
    }

    @Test
    @DisplayName("Should return valid VAPID public key")
    void getVapidPublicKey_ReturnsKey() {
        String key = webPushService.getVapidPublicKey();
        assertThat(key).isNotBlank();
    }
}
