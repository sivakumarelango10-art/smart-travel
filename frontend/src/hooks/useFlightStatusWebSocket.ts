import { useEffect, useRef, useState, useCallback } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL, WS_BASE_URL } from '../config/constants';
import { FlightStatusEvent } from '../types/api';

interface UseFlightStatusWebSocketProps {
  flightId?: string;
  onStatusUpdate?: (event: FlightStatusEvent) => void;
  enabled?: boolean;
}

export function useFlightStatusWebSocket({
  flightId,
  onStatusUpdate,
  enabled = true,
}: UseFlightStatusWebSocketProps) {
  const [isConnected, setIsConnected] = useState(false);
  const [latestEvent, setLatestEvent] = useState<FlightStatusEvent | null>(null);
  const [connectionError, setConnectionError] = useState<string | null>(null);
  const clientRef = useRef<Client | null>(null);

  const getWsUrl = useCallback(() => {
    if (WS_BASE_URL) return `${WS_BASE_URL}/ws`;
    if (API_BASE_URL && API_BASE_URL !== '/api') {
      return `${API_BASE_URL}/ws`;
    }
    // Default localhost or relative URL
    return 'http://localhost:8080/ws';
  }, []);

  useEffect(() => {
    if (!enabled || !flightId) {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
        setIsConnected(false);
      }
      return;
    }

    const wsUrl = getWsUrl();

    const stompClient = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: () => {
        // quiet debug in production
      },
      onConnect: () => {
        setIsConnected(true);
        setConnectionError(null);

        // Subscribe to flight status topic
        const topic = `/topic/flight-status/${flightId}`;
        stompClient.subscribe(topic, (message: IMessage) => {
          try {
            const event: FlightStatusEvent = JSON.parse(message.body);
            setLatestEvent(event);
            if (onStatusUpdate) {
              onStatusUpdate(event);
            }
          } catch (err) {
            console.error('Failed to parse flight status WebSocket message', err);
          }
        });
      },
      onDisconnect: () => {
        setIsConnected(false);
      },
      onStompError: (frame) => {
        console.warn('STOMP error:', frame.headers['message']);
        setConnectionError(frame.headers['message'] || 'WebSocket STOMP error');
      },
      onWebSocketError: (event) => {
        console.warn('WebSocket connection warning:', event);
      },
    });

    stompClient.activate();
    clientRef.current = stompClient;

    return () => {
      if (stompClient.active) {
        stompClient.deactivate();
      }
      clientRef.current = null;
      setIsConnected(false);
    };
  }, [flightId, enabled, getWsUrl, onStatusUpdate]);

  return {
    isConnected,
    latestEvent,
    connectionError,
  };
}
