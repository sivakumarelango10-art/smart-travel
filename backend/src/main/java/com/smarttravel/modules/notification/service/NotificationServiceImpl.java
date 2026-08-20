package com.smarttravel.modules.notification.service;

import com.smarttravel.common.response.PageResponse;
import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.notification.dto.NotificationResponse;
import com.smarttravel.modules.notification.dto.NotificationSendRequest;
import com.smarttravel.modules.notification.dto.UnreadCountResponse;
import com.smarttravel.modules.notification.model.Notification;
import com.smarttravel.modules.notification.model.NotificationChannel;
import com.smarttravel.modules.notification.model.NotificationStatus;
import com.smarttravel.modules.notification.provider.EmailNotificationProvider;
import com.smarttravel.modules.notification.provider.PushNotificationProvider;
import com.smarttravel.modules.notification.provider.SmsNotificationProvider;
import com.smarttravel.modules.notification.provider.WhatsAppNotificationProvider;
import com.smarttravel.modules.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Production-ready implementation of NotificationService supporting multi-channel delivery,
 * composite idempotency keys, bounded retries, and ownership validation.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final int MAX_RETRY_COUNT = 3;

    private final NotificationRepository notificationRepository;
    private final EmailNotificationProvider emailProvider;
    private final SmsNotificationProvider smsProvider;
    private final WhatsAppNotificationProvider whatsAppProvider;
    private final PushNotificationProvider pushProvider;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   EmailNotificationProvider emailProvider,
                                   SmsNotificationProvider smsProvider,
                                   WhatsAppNotificationProvider whatsAppProvider,
                                   PushNotificationProvider pushProvider) {
        this.notificationRepository = notificationRepository;
        this.emailProvider = emailProvider;
        this.smsProvider = smsProvider;
        this.whatsAppProvider = whatsAppProvider;
        this.pushProvider = pushProvider;
    }

    @Override
    public synchronized NotificationResponse sendNotification(NotificationSendRequest request) {
        if (request == null) {
            throw new BadRequestException("Notification send request must not be null");
        }

        NotificationChannel channel = request.getChannel() != null ? request.getChannel() : NotificationChannel.EMAIL;

        // Construct unique deterministic idempotency key
        String idempotencyKey = buildIdempotencyKey(
                request.getFlightId(),
                request.getEventId(),
                request.getUserId(),
                request.getNotificationType().name(),
                channel.name()
        );

        // 1. Check existing notification for idempotency
        Optional<Notification> existingOpt = notificationRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOpt.isPresent()) {
            log.info("Duplicate notification suppressed by idempotency key: {}", idempotencyKey);
            return toDto(existingOpt.get());
        }

        // 2. Build initial notification record
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .bookingId(request.getBookingId())
                .flightId(request.getFlightId())
                .notificationType(request.getNotificationType())
                .channel(channel)
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .idempotencyKey(idempotencyKey)
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .read(false)
                .build();

        Notification saved;
        try {
            saved = notificationRepository.save(notification);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            log.info("Concurrent insert caught by unique index for idempotencyKey: {}", idempotencyKey);
            Optional<Notification> existing = notificationRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                existing = notificationRepository.findByIdempotencyKey(idempotencyKey);
            }
            return toDto(existing.orElse(notification));
        }

        // 3. Dispatch through provider
        dispatchNotification(saved);
        saved = notificationRepository.save(saved);

        return toDto(saved);
    }

    @Override
    public PageResponse<NotificationResponse> getUserNotifications(String userId, Pageable pageable) {
        log.debug("Fetching notifications for user: {}", userId);
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Override
    public UnreadCountResponse getUnreadCount(String userId) {
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return new UnreadCountResponse(count);
    }

    @Override
    public NotificationResponse markAsRead(String notificationId, String userId, boolean isAdmin) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!isAdmin && !notification.getUserId().equals(userId)) {
            // Strict IDOR protection
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
            log.debug("Notification ID: {} marked as read for user: {}", notificationId, userId);
        }

        return toDto(notification);
    }

    @Override
    public NotificationResponse retryNotification(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (notification.getStatus() == NotificationStatus.SENT || notification.getStatus() == NotificationStatus.DELIVERED) {
            log.info("Notification ID: {} is already in status '{}', skipping retry", notificationId, notification.getStatus());
            return toDto(notification);
        }

        if (notification.getRetryCount() >= MAX_RETRY_COUNT) {
            throw new BadRequestException("Maximum retry count (" + MAX_RETRY_COUNT + ") exceeded for notification: " + notificationId);
        }

        notification.setRetryCount(notification.getRetryCount() + 1);
        dispatchNotification(notification);
        Notification saved = notificationRepository.save(notification);

        return toDto(saved);
    }

    private void dispatchNotification(Notification notification) {
        try {
            String messageId;
            switch (notification.getChannel()) {
                case SMS:
                    messageId = smsProvider.sendSms(notification.getRecipient(), notification.getContent());
                    break;
                case WHATSAPP:
                    messageId = whatsAppProvider.sendWhatsApp(notification.getRecipient(), notification.getContent());
                    break;
                case PUSH:
                    messageId = pushProvider.sendPush(notification.getRecipient(), notification.getSubject(), notification.getContent());
                    break;
                case EMAIL:
                default:
                    messageId = emailProvider.sendEmail(notification.getRecipient(), notification.getSubject(), notification.getContent());
                    break;
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setProviderMessageId(messageId);
            notification.setSentAt(Instant.now());
            notification.setFailureReason(null);
            log.info("Dispatched notification ID: {} via {} successfully (Provider ID: {})",
                    notification.getId(), notification.getChannel(), messageId);
        } catch (Exception ex) {
            log.error("Failed to dispatch notification ID: {} via {}: {}",
                    notification.getId(), notification.getChannel(), ex.getMessage(), ex);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(ex.getMessage());
        }
    }

    private String buildIdempotencyKey(String flightId, String eventId, String userId, String type, String channel) {
        return String.format("%s:%s:%s:%s:%s",
                flightId != null ? flightId : "none",
                eventId != null ? eventId : "none",
                userId != null ? userId : "none",
                type != null ? type : "unknown",
                channel != null ? channel : "email");
    }

    private NotificationResponse toDto(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .bookingId(notification.getBookingId())
                .flightId(notification.getFlightId())
                .notificationType(notification.getNotificationType())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .status(notification.getStatus())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }
}
