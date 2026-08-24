package com.smarttravel.modules.flight.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket publisher for broadcasting real-time aircraft seat status changes.
 * Broadcasts to /topic/seat-map/{flightId} whenever seats are held, confirmed, or released.
 */
@Component
public class SeatMapWebSocketPublisher {

    private static final Logger log = LoggerFactory.getLogger(SeatMapWebSocketPublisher.class);
    private static final String TOPIC_PREFIX = "/topic/seat-map/";

    private final SimpMessagingTemplate messagingTemplate;

    public SeatMapWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Publishes a seat map update event to /topic/seat-map/{flightId}.
     *
     * @param event The seat map update event payload
     */
    public void publishSeatUpdate(SeatMapUpdateEvent event) {
        if (event == null || event.getFlightId() == null) {
            return;
        }

        String destination = TOPIC_PREFIX + event.getFlightId();
        try {
            log.debug("Publishing seat map update to {}: seats={}, status={}, action={}",
                    destination, event.getSeatNumbers(), event.getStatus(), event.getAction());
            messagingTemplate.convertAndSend(destination, event);
        } catch (Exception ex) {
            log.warn("Failed to publish seat map WebSocket event for flight {}: {}",
                    event.getFlightId(), ex.getMessage());
        }
    }
}
