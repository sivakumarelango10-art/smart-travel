package com.smarttravel.modules.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Safe local development and test implementation of EmailNotificationProvider.
 * Logs email dispatches without initiating external network calls.
 */
@Component
@ConditionalOnMissingBean(name = "customEmailNotificationProvider")
public class LoggingEmailNotificationProvider implements EmailNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailNotificationProvider.class);

    @Override
    public String sendEmail(String recipient, String subject, String body) {
        String messageId = "msg_email_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        log.info("[EMAIL OUTBOUND] [ID: {}] To: '{}' | Subject: '{}' | Body length: {} chars",
                messageId, recipient != null ? recipient : "N/A", subject, body != null ? body.length() : 0);
        return messageId;
    }
}
