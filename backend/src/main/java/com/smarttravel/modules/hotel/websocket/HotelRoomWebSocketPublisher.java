package com.smarttravel.modules.hotel.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket publisher for broadcasting hotel room inventory updates.
 * Broadcasts to /topic/hotels/{hotelId}/rooms whenever a room is held, booked, or released.
 */
@Component
public class HotelRoomWebSocketPublisher {

    private static final Logger log = LoggerFactory.getLogger(HotelRoomWebSocketPublisher.class);
    private static final String TOPIC_PREFIX = "/topic/hotels/";
    private static final String TOPIC_SUFFIX = "/rooms";

    private final SimpMessagingTemplate messagingTemplate;

    public HotelRoomWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Publishes a room availability event to /topic/hotels/{hotelId}/rooms.
     *
     * @param event The room availability event payload
     */
    public void publishRoomUpdate(RoomAvailabilityEvent event) {
        if (event == null || event.getHotelId() == null) {
            return;
        }

        String destination = TOPIC_PREFIX + event.getHotelId() + TOPIC_SUFFIX;
        try {
            log.debug("Publishing room update to {}: roomTypeId={}, availableRooms={}, action={}",
                    destination, event.getRoomTypeId(), event.getAvailableRooms(), event.getAction());
            messagingTemplate.convertAndSend(destination, event);
        } catch (Exception ex) {
            log.warn("Failed to publish hotel room WebSocket event for hotel {}: {}",
                    event.getHotelId(), ex.getMessage());
        }
    }
}
