package com.smarttravel.modules.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Safe local development and test implementation of SmsNotificationProvider.
 */
@Component
@ConditionalOnMissingBean(name = "customSmsNotificationProvider")
public class LoggingSmsNotificationProvider implements SmsNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsNotificationProvider.class);

    @Override
    public String sendSms(String phoneNumber, String message) {
        String messageId = "msg_sms_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        log.info("[SMS OUTBOUND] [ID: {}] To: '{}' | Text: '{}'",
                messageId, phoneNumber != null ? phoneNumber : "N/A", message);
        return messageId;
    }
}
