package com.smarttravel.modules.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Safe local development and test implementation of WhatsAppNotificationProvider.
 */
@Component
@ConditionalOnMissingBean(name = "customWhatsAppNotificationProvider")
public class LoggingWhatsAppNotificationProvider implements WhatsAppNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingWhatsAppNotificationProvider.class);

    @Override
    public String sendWhatsApp(String phoneNumber, String message) {
        String messageId = "msg_wa_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        log.info("[WHATSAPP OUTBOUND] [ID: {}] To: '{}' | Message: '{}'",
                messageId, phoneNumber != null ? phoneNumber : "N/A", message);
        return messageId;
    }
}
