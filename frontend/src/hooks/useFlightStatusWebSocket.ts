import { useEffect, useRef, useState } from 'react';
import { FlightStatusEvent } from '../types/api';
import { flightStatusWebSocketManager } from '../services/flightStatusWebSocketManager';

interface UseFlightStatusWebSocketProps {
  flightId?: string;
  onStatusUpdate?: (event: FlightStatusEvent) => void;
  enabled?: boolean;
}

/**
 * React hook for consuming live flight status updates.
 * Leverages the singleton FlightStatusWebSocketManager to multiplex multiple
 * flight subscriptions over a single shared STOMP/WebSocket connection.
 */
export function useFlightStatusWebSocket({
  flightId,
  onStatusUpdate,
  enabled = true,
}: UseFlightStatusWebSocketProps) {
  const [isConnected, setIsConnected] = useState(flightStatusWebSocketManager.isConnected());
  const [latestEvent, setLatestEvent] = useState<FlightStatusEvent | null>(null);
  const [connectionError, setConnectionError] = useState<string | null>(
    flightStatusWebSocketManager.getConnectionError()
  );

  const onStatusUpdateRef = useRef(onStatusUpdate);
  useEffect(() => {
    onStatusUpdateRef.current = onStatusUpdate;
  }, [onStatusUpdate]);

  // Listen to shared connection state changes
  useEffect(() => {
    const unsubConnection = flightStatusWebSocketManager.addConnectionListener((connected, error) => {
      setIsConnected(connected);
      setConnectionError(error);
    });

    return () => {
      unsubConnection();
    };
  }, []);

  // Subscribe to specific flight topic updates
  useEffect(() => {
    if (!enabled || !flightId) {
      return;
    }

    const handleUpdate = (event: FlightStatusEvent) => {
      setLatestEvent(event);
      if (onStatusUpdateRef.current) {
        onStatusUpdateRef.current(event);
      }
    };

    const unsubscribe = flightStatusWebSocketManager.subscribe(flightId, handleUpdate);

    return () => {
      unsubscribe();
    };
  }, [flightId, enabled]);

  return {
    isConnected,
    latestEvent,
    connectionError,
  };
}
