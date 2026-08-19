package com.smarttravel.modules.notification.provider;

/**
 * Provider interface for outbound email delivery.
 */
public interface EmailNotificationProvider {

    /**
     * Sends an email notification.
     *
     * @param recipient Recipient email address
     * @param subject   Email subject line
     * @param body      Email HTML / text body
     * @return Unique provider message identifier
     */
    String sendEmail(String recipient, String subject, String body);
}
