package com.smarttravel.modules.notification.provider;

/**
 * Provider interface for outbound SMS delivery.
 */
public interface SmsNotificationProvider {

    /**
     * Sends an SMS notification.
     *
     * @param phoneNumber Recipient mobile number in E.164 format
     * @param message     SMS text content
     * @return Unique provider message identifier
     */
    String sendSms(String phoneNumber, String message);
}
