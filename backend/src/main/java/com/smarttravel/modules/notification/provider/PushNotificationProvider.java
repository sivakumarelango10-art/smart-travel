package com.smarttravel.modules.notification.provider;

/**
 * Provider interface for mobile push notifications (FCM / APNs).
 */
public interface PushNotificationProvider {

    /**
     * Sends a push notification to a customer device.
     *
     * @param deviceToken Target device push token
     * @param title       Notification title
     * @param body        Notification body
     * @return Unique provider message identifier
     */
    String sendPush(String deviceToken, String title, String body);
}
