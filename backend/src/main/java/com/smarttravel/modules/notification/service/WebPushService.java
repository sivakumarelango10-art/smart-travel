package com.smarttravel.modules.notification.service;

import com.smarttravel.modules.notification.dto.PushSubscriptionRequest;
import com.smarttravel.modules.notification.dto.WebPushPayload;
import com.smarttravel.modules.notification.model.PushSubscription;

import java.util.List;

public interface WebPushService {

    PushSubscription registerSubscription(String userId, PushSubscriptionRequest request);

    void removeSubscription(String userId, String endpoint);

    String getVapidPublicKey();

    void sendPushToUser(String userId, WebPushPayload payload);

    void sendPushForFlight(String flightId, String title, String body, String url, String eventType);

    List<PushSubscription> getUserSubscriptions(String userId);
}
