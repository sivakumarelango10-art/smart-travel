package com.smarttravel.modules.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Safe local development and test implementation of PushNotificationProvider.
 */
@Component
@ConditionalOnMissingBean(name = "customPushNotificationProvider")
public class LoggingPushNotificationProvider implements PushNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingPushNotificationProvider.class);

    @Override
    public String sendPush(String deviceToken, String title, String body) {
        String messageId = "msg_push_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        log.info("[PUSH OUTBOUND] [ID: {}] Token: '{}' | Title: '{}' | Body: '{}'",
                messageId, deviceToken != null ? deviceToken : "N/A", title, body);
        return messageId;
    }
}
