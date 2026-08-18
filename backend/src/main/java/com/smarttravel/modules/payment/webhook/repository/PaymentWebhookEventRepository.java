package com.smarttravel.modules.payment.webhook.repository;

import com.smarttravel.modules.payment.webhook.model.PaymentWebhookEvent;
import com.smarttravel.modules.payment.webhook.model.WebhookProcessingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for PaymentWebhookEvent entities.
 */
@Repository
public interface PaymentWebhookEventRepository extends MongoRepository<PaymentWebhookEvent, String> {

    Optional<PaymentWebhookEvent> findByEventId(String eventId);

    Optional<PaymentWebhookEvent> findFirstByRazorpayOrderIdAndEventTypeAndProcessingStatus(
            String razorpayOrderId, String eventType, WebhookProcessingStatus status);

    boolean existsByEventIdAndProcessingStatus(String eventId, WebhookProcessingStatus status);

    boolean existsByRazorpayOrderIdAndEventTypeAndProcessingStatus(
            String razorpayOrderId, String eventType, WebhookProcessingStatus status);
}
