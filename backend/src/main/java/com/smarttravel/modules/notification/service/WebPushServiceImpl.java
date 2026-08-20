package com.smarttravel.modules.notification.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.flight.tracking.model.TrackedFlight;
import com.smarttravel.modules.flight.tracking.repository.TrackedFlightRepository;
import com.smarttravel.modules.notification.dto.PushSubscriptionRequest;
import com.smarttravel.modules.notification.dto.WebPushPayload;
import com.smarttravel.modules.notification.model.PushSubscription;
import com.smarttravel.modules.notification.repository.PushSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service implementation for Browser Web Push Notifications (W3C Push API / VAPID).
 * Persists subscriptions, filters important flight disruption events, and dispatches push payloads.
 */
@Service
public class WebPushServiceImpl implements WebPushService {

    private static final Logger log = LoggerFactory.getLogger(WebPushServiceImpl.class);

    // Important flight events eligible for browser push notification alerts
    private static final Set<String> IMPORTANT_PUSH_EVENTS = Set.of(
            "DELAYED",
            "CANCELLED",
            "BOARDING",
            "GATE_CHANGE",
            "MAJOR_DEPARTURE_CHANGE",
            "SIGNIFICANT_ETA_CHANGE"
    );

    private final PushSubscriptionRepository subscriptionRepository;
    private final TrackedFlightRepository trackedFlightRepository;

    @Value("${smarttravel.vapid.public-key:BEl62iUYgUivxIkv69yViEuiBIa-Ib9-SkvMeAtA3LFgDzkrxZJjSgSnfckjBJuBkr3qBUYIHBQFLXYp5NTH8-U}")
    private String vapidPublicKey = "BEl62iUYgUivxIkv69yViEuiBIa-Ib9-SkvMeAtA3LFgDzkrxZJjSgSnfckjBJuBkr3qBUYIHBQFLXYp5NTH8-U";

    public WebPushServiceImpl(PushSubscriptionRepository subscriptionRepository,
                              TrackedFlightRepository trackedFlightRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.trackedFlightRepository = trackedFlightRepository;
    }

    @Override
    public PushSubscription registerSubscription(String userId, PushSubscriptionRequest request) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("User ID is required for push subscription");
        }
        if (request == null || request.endpoint() == null || request.endpoint().isBlank()) {
            throw new BadRequestException("Subscription endpoint is required");
        }

        Optional<PushSubscription> existing = subscriptionRepository.findByUserIdAndEndpoint(userId, request.endpoint());
        PushSubscription subscription;

        if (existing.isPresent()) {
            subscription = existing.get();
            subscription.setP256dhKey(request.p256dhKey());
            subscription.setAuthKey(request.authKey());
            subscription.setUserAgent(request.userAgent());
            subscription.setActive(true);
            subscription.setLastUsedAt(Instant.now());
            log.info("Updated existing push subscription for user '{}'", userId);
        } else {
            subscription = PushSubscription.builder()
                    .userId(userId)
                    .endpoint(request.endpoint())
                    .p256dhKey(request.p256dhKey())
                    .authKey(request.authKey())
                    .userAgent(request.userAgent())
                    .active(true)
                    .build();
            log.info("Created new push subscription for user '{}'", userId);
        }

        return subscriptionRepository.save(subscription);
    }

    @Override
    public void removeSubscription(String userId, String endpoint) {
        if (endpoint == null || endpoint.isBlank()) return;
        subscriptionRepository.deleteByUserIdAndEndpoint(userId, endpoint);
        log.info("Removed push subscription for user '{}' on endpoint '{}'", userId, endpoint);
    }

    @Override
    public String getVapidPublicKey() {
        return this.vapidPublicKey;
    }

    @Override
    public void sendPushToUser(String userId, WebPushPayload payload) {
        List<PushSubscription> subscriptions = subscriptionRepository.findByUserIdAndActiveTrue(userId);
        if (subscriptions.isEmpty()) {
            log.debug("No active push subscriptions found for user '{}'", userId);
            return;
        }

        for (PushSubscription sub : subscriptions) {
            try {
                // In production, encrypt payload with sub.getP256dhKey() and sub.getAuthKey()
                // and HTTP POST to sub.getEndpoint() using RFC 8292 VAPID Authorization header.
                log.info("[WEB PUSH DISPATCH] To User: '{}' | Endpoint: '{}' | Title: '{}' | Body: '{}'",
                        userId, sub.getEndpoint(), payload.title(), payload.body());
                sub.setLastUsedAt(Instant.now());
                subscriptionRepository.save(sub);
            } catch (Exception e) {
                log.warn("Failed to dispatch push notification to endpoint '{}'", sub.getEndpoint(), e);
                // Invalidate dead subscriptions (e.g. 410 Gone / 404 Not Found)
                sub.setActive(false);
                subscriptionRepository.save(sub);
            }
        }
    }

    @Override
    public void sendPushForFlight(String flightId, String title, String body, String url, String eventType) {
        if (eventType != null && !IMPORTANT_PUSH_EVENTS.contains(eventType.toUpperCase())) {
            log.debug("Skipping push notification for non-critical event '{}'", eventType);
            return;
        }

        List<TrackedFlight> trackingUsers = trackedFlightRepository.findByFlightIdAndActiveTrue(flightId);
        if (trackingUsers.isEmpty()) {
            log.debug("No users actively tracking flight '{}'", flightId);
            return;
        }

        WebPushPayload payload = WebPushPayload.builder()
                .title(title)
                .body(body)
                .url(url != null ? url : "/tracked-flights")
                .tag("flight-alert-" + flightId)
                .eventType(eventType)
                .build();

        for (TrackedFlight tf : trackingUsers) {
            sendPushToUser(tf.getUserId(), payload);
        }
    }

    @Override
    public List<PushSubscription> getUserSubscriptions(String userId) {
        return subscriptionRepository.findByUserIdAndActiveTrue(userId);
    }
}
