package com.smarttravel.modules.flight.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes real-time flight status events to STOMP WebSocket topic subscribers.
 * Called by the simulation engine whenever a flight's operational status changes.
 */
@Component
public class FlightStatusWebSocketPublisher {

    private static final Logger log = LoggerFactory.getLogger(FlightStatusWebSocketPublisher.class);
    private static final String TOPIC_PREFIX = "/topic/flight-status/";

    private final SimpMessagingTemplate messagingTemplate;

    public FlightStatusWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcasts a flight status event to all subscribers of the flight's topic.
     *
     * @param event The flight status event payload
     */
    public void publish(FlightStatusEvent event) {
        if (event == null || event.getFlightId() == null) {
            log.warn("Attempted to publish null or incomplete FlightStatusEvent");
            return;
        }

        String topic = TOPIC_PREFIX + event.getFlightId();
        try {
            messagingTemplate.convertAndSend(topic, event);
            log.info("Published flight status event to {}: {} → {} (delay: {}min)",
                    topic, event.getPreviousStatus(), event.getStatus(), event.getDelayMinutes());
        } catch (Exception ex) {
            // WebSocket publication failure must never break the simulation loop
            log.error("Failed to publish WebSocket event for flight {}: {}", event.getFlightId(), ex.getMessage());
        }
    }
}
