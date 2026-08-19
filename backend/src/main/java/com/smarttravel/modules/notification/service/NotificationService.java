package com.smarttravel.modules.notification.service;

import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.notification.dto.NotificationResponse;
import com.smarttravel.modules.notification.dto.NotificationSendRequest;
import com.smarttravel.modules.notification.dto.UnreadCountResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service orchestrating customer notification persistence, dispatch, idempotency, and retrieval.
 */
public interface NotificationService {

    /**
     * Idempotently sends and persists a customer notification.
     *
     * @param request Payload containing recipient, type, subject, body, and idempotency eventId
     * @return Created or existing notification details
     */
    NotificationResponse sendNotification(NotificationSendRequest request);

    /**
     * Retrieves paginated notifications for a customer.
     *
     * @param userId   Customer user ID
     * @param pageable Pagination parameters
     * @return Paginated notifications
     */
    PageResponse<NotificationResponse> getUserNotifications(String userId, Pageable pageable);

    /**
     * Returns count of unread notifications for a customer.
     *
     * @param userId Customer user ID
     * @return Unread notification count
     */
    UnreadCountResponse getUnreadCount(String userId);

    /**
     * Marks a notification as read with strict ownership validation.
     *
     * @param notificationId Notification MongoDB ID
     * @param userId         Customer user ID
     * @param isAdmin        Whether requester is admin
     * @return Updated notification details
     */
    NotificationResponse markAsRead(String notificationId, String userId, boolean isAdmin);

    /**
     * Retries delivery for a failed notification (Admin only).
     *
     * @param notificationId Notification MongoDB ID
     * @return Updated notification details
     */
    NotificationResponse retryNotification(String notificationId);
}
