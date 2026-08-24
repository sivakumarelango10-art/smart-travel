import { useEffect, useRef, useState } from 'react';
import { RoomAvailabilityEvent } from '../types/api';
import { flightStatusWebSocketManager } from '../services/flightStatusWebSocketManager';

interface UseHotelRoomWebSocketProps {
  hotelId?: string;
  onRoomUpdate?: (event: RoomAvailabilityEvent) => void;
  enabled?: boolean;
}

/**
 * React hook for consuming real-time hotel room availability updates.
 * Leverages the singleton WebSocketManager to subscribe to /topic/hotels/{hotelId}/rooms.
 */
export function useHotelRoomWebSocket({
  hotelId,
  onRoomUpdate,
  enabled = true,
}: UseHotelRoomWebSocketProps) {
  const [latestRoomEvent, setLatestRoomEvent] = useState<RoomAvailabilityEvent | null>(null);

  const onRoomUpdateRef = useRef(onRoomUpdate);
  useEffect(() => {
    onRoomUpdateRef.current = onRoomUpdate;
  }, [onRoomUpdate]);

  useEffect(() => {
    if (!enabled || !hotelId) {
      return;
    }

    const handleUpdate = (event: RoomAvailabilityEvent) => {
      setLatestRoomEvent(event);
      if (onRoomUpdateRef.current) {
        onRoomUpdateRef.current(event);
      }
    };

    const unsubscribe = flightStatusWebSocketManager.subscribeHotelRooms(hotelId, handleUpdate);

    return () => {
      unsubscribe();
    };
  }, [hotelId, enabled]);

  return {
    latestRoomEvent,
  };
}
