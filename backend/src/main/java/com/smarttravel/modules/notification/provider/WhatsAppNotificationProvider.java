package com.smarttravel.modules.notification.provider;

/**
 * Provider interface for outbound WhatsApp messaging.
 */
public interface WhatsAppNotificationProvider {

    /**
     * Sends a WhatsApp template or session message.
     *
     * @param phoneNumber Recipient WhatsApp phone number
     * @param message     Message body
     * @return Unique provider message identifier
     */
    String sendWhatsApp(String phoneNumber, String message);
}
