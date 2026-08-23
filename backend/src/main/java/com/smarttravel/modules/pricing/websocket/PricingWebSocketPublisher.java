package com.smarttravel.modules.pricing.websocket;

import com.smarttravel.modules.pricing.event.DynamicPricingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes real-time pricing events to STOMP WebSocket topic subscribers.
 * Broadcasts to /topic/pricing/{flightId} whenever demand or pricing conditions shift.
 */
@Component
public class PricingWebSocketPublisher {

    private static final Logger log = LoggerFactory.getLogger(PricingWebSocketPublisher.class);
    private static final String TOPIC_PREFIX = "/topic/pricing/";

    private final SimpMessagingTemplate messagingTemplate;

    public PricingWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcasts a dynamic pricing event to all subscribers of the flight's pricing topic.
     *
     * @param event The dynamic pricing event payload
     */
    public void publish(DynamicPricingEvent event) {
        if (event == null || event.getFlightId() == null) {
            log.warn("Attempted to publish null or incomplete DynamicPricingEvent");
            return;
        }

        String topic = TOPIC_PREFIX + event.getFlightId();
        try {
            messagingTemplate.convertAndSend(topic, event);
            log.info("Published dynamic pricing event to {}: cabin={}, old=₹{}, new=₹{}, demand={}% ({})",
                    topic, event.getCabinClass(), event.getOldPrice(), event.getNewPrice(),
                    event.getDemandAdjustmentPercent(), event.getReason());
        } catch (Exception ex) {
            log.error("Failed to publish pricing WebSocket event for flight {}: {}", event.getFlightId(), ex.getMessage());
        }
    }
}
