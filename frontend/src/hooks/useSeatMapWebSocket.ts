import { useEffect, useRef, useState } from 'react';
import { SeatMapUpdateEvent } from '../types/api';
import { flightStatusWebSocketManager } from '../services/flightStatusWebSocketManager';

interface UseSeatMapWebSocketProps {
  flightId?: string;
  onSeatUpdate?: (event: SeatMapUpdateEvent) => void;
  enabled?: boolean;
}

/**
 * React hook for consuming real-time flight seat map availability updates.
 * Leverages the singleton WebSocketManager to subscribe to /topic/seat-map/{flightId}.
 */
export function useSeatMapWebSocket({
  flightId,
  onSeatUpdate,
  enabled = true,
}: UseSeatMapWebSocketProps) {
  const [latestSeatEvent, setLatestSeatEvent] = useState<SeatMapUpdateEvent | null>(null);

  const onSeatUpdateRef = useRef(onSeatUpdate);
  useEffect(() => {
    onSeatUpdateRef.current = onSeatUpdate;
  }, [onSeatUpdate]);

  useEffect(() => {
    if (!enabled || !flightId) {
      return;
    }

    const handleUpdate = (event: SeatMapUpdateEvent) => {
      setLatestSeatEvent(event);
      if (onSeatUpdateRef.current) {
        onSeatUpdateRef.current(event);
      }
    };

    const unsubscribe = flightStatusWebSocketManager.subscribeSeatMap(flightId, handleUpdate);

    return () => {
      unsubscribe();
    };
  }, [flightId, enabled]);

  return {
    latestSeatEvent,
  };
}
