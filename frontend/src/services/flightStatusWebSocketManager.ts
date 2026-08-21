import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL, WS_BASE_URL } from '../config/constants';
import { FlightStatusEvent } from '../types/api';

type FlightStatusCallback = (event: FlightStatusEvent) => void;
type ConnectionStateListener = (connected: boolean, error: string | null) => void;

/**
 * Singleton FlightStatusWebSocketManager
 * Manages ONE single shared STOMP/WebSocket connection for the entire application,
 * multiplexing multiple flight topic subscriptions (/topic/flight-status/{flightId})
 * and routing live updates to local component subscribers.
 */
class FlightStatusWebSocketManager {
  private client: Client | null = null;
  private connected: boolean = false;
  private connectionError: string | null = null;

  // Active subscribers: Map<flightId, Set<callback>>
  private flightCallbacks: Map<string, Set<FlightStatusCallback>> = new Map();

  // Active STOMP broker subscriptions: Map<flightId, StompSubscription>
  private stompSubscriptions: Map<string, StompSubscription> = new Map();

  // Connection state change listeners
  private connectionListeners: Set<ConnectionStateListener> = new Set();

  // Duplicate event suppression cache (keeps last 200 unique event signatures)
  private processedEventSignatures: Set<string> = new Set();

  // Reconnect backoff state
  private reconnectAttempt: number = 0;
  private reconnectTimeoutId: any = null;
  private isExplicitlyDisconnected: boolean = false;

  private getWsUrl(): string {
    if (WS_BASE_URL) return `${WS_BASE_URL}/ws`;
    if (API_BASE_URL && API_BASE_URL !== '/api') {
      return `${API_BASE_URL}/ws`;
    }
    return 'http://localhost:8080/ws';
  }

  /**
   * Initializes and activates the shared STOMP connection if not already active.
   */
  public connect(): void {
    if (this.client && (this.connected || this.client.active)) {
      return;
    }

    this.isExplicitlyDisconnected = false;
    const wsUrl = this.getWsUrl();

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as any,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      reconnectDelay: 0, // Handled custom with bounded exponential backoff
      debug: () => {
        // quiet in production
      },
      onConnect: () => {
        this.connected = true;
        this.connectionError = null;
        this.reconnectAttempt = 0;
        if (this.reconnectTimeoutId) {
          clearTimeout(this.reconnectTimeoutId);
          this.reconnectTimeoutId = null;
        }

        // Restore all active flight subscriptions on reconnect
        this.restoreSubscriptions();
        this.notifyConnectionListeners();
      },
      onDisconnect: () => {
        this.connected = false;
        this.stompSubscriptions.clear();
        this.notifyConnectionListeners();
        this.scheduleReconnect();
      },
      onStompError: (frame) => {
        const msg = frame.headers['message'] || 'STOMP connection error';
        this.connectionError = msg;
        this.connected = false;
        this.notifyConnectionListeners();
        this.scheduleReconnect();
      },
      onWebSocketError: () => {
        this.connectionError = 'WebSocket connection error';
        this.connected = false;
        this.notifyConnectionListeners();
        this.scheduleReconnect();
      },
    });

    this.client.activate();
  }

  /**
   * Bounded exponential backoff reconnection scheduler:
   * 1s -> 2s -> 4s -> 8s -> 16s -> max 30s.
   */
  private scheduleReconnect(): void {
    if (this.isExplicitlyDisconnected || this.reconnectTimeoutId) {
      return;
    }

    // Only reconnect if there are active flight subscriptions or listeners
    if (this.flightCallbacks.size === 0 && this.connectionListeners.size === 0) {
      return;
    }

    const backoffMs = Math.min(1000 * Math.pow(2, this.reconnectAttempt), 30000);
    this.reconnectAttempt++;

    this.reconnectTimeoutId = setTimeout(() => {
      this.reconnectTimeoutId = null;
      if (!this.connected && !this.isExplicitlyDisconnected) {
        if (this.client) {
          this.client.activate();
        } else {
          this.connect();
        }
      }
    }, backoffMs);
  }

  /**
   * Subscribes a consumer callback to a specific flight's live status updates.
   * Returns an unsubscribe function for React cleanup.
   */
  public subscribe(flightId: string, callback: FlightStatusCallback): () => void {
    if (!flightId) return () => {};

    if (!this.flightCallbacks.has(flightId)) {
      this.flightCallbacks.set(flightId, new Set());
    }
    this.flightCallbacks.get(flightId)!.add(callback);

    // If already connected and not yet subscribed on STOMP broker, subscribe now
    if (this.connected && this.client && !this.stompSubscriptions.has(flightId)) {
      this.subscribeStompTopic(flightId);
    } else if (!this.connected) {
      this.connect();
    }

    return () => {
      this.unsubscribe(flightId, callback);
    };
  }

  /**
   * Unsubscribes a consumer callback.
   * If no consumers remain for this flight, cancels the STOMP broker subscription to prevent leaks.
   */
  public unsubscribe(flightId: string, callback: FlightStatusCallback): void {
    const callbacks = this.flightCallbacks.get(flightId);
    if (!callbacks) return;

    callbacks.delete(callback);

    if (callbacks.size === 0) {
      this.flightCallbacks.delete(flightId);

      const stompSub = this.stompSubscriptions.get(flightId);
      if (stompSub) {
        try {
          stompSub.unsubscribe();
        } catch (e) {
          // ignore
        }
        this.stompSubscriptions.delete(flightId);
      }
    }
  }

  /**
   * Registers a connection state listener (e.g. for connection indicator badges).
   */
  public addConnectionListener(listener: ConnectionStateListener): () => void {
    this.connectionListeners.add(listener);
    listener(this.connected, this.connectionError);
    return () => {
      this.connectionListeners.delete(listener);
    };
  }

  public isConnected(): boolean {
    return this.connected;
  }

  public getConnectionError(): string | null {
    return this.connectionError;
  }

  /**
   * Cleanly disconnects the shared STOMP connection.
   */
  public disconnect(): void {
    this.isExplicitlyDisconnected = true;
    if (this.reconnectTimeoutId) {
      clearTimeout(this.reconnectTimeoutId);
      this.reconnectTimeoutId = null;
    }

    this.stompSubscriptions.forEach((sub) => {
      try {
        sub.unsubscribe();
      } catch (e) {
        // ignore
      }
    });
    this.stompSubscriptions.clear();

    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.connected = false;
    this.notifyConnectionListeners();
  }

  private subscribeStompTopic(flightId: string): void {
    if (!this.client || !this.connected) return;

    const topic = `/topic/flight-status/${flightId}`;
    try {
      const stompSub = this.client.subscribe(topic, (message: IMessage) => {
        try {
          const event: FlightStatusEvent = JSON.parse(message.body);
          this.handleFlightStatusEvent(flightId, event);
        } catch (err) {
          console.error('Failed to parse flight status message', err);
        }
      });
      this.stompSubscriptions.set(flightId, stompSub);
    } catch (err) {
      console.warn(`Failed to subscribe to STOMP topic ${topic}:`, err);
    }
  }

  private restoreSubscriptions(): void {
    for (const flightId of this.flightCallbacks.keys()) {
      if (!this.stompSubscriptions.has(flightId)) {
        this.subscribeStompTopic(flightId);
      }
    }
  }

  private handleFlightStatusEvent(flightId: string, event: FlightStatusEvent): void {
    // Construct stable unique signature for deduplication
    const eventSig = event.eventId
      ? event.eventId
      : `${event.flightId || flightId}:${event.status}:${event.updatedAt || ''}:${event.delayMinutes || 0}`;

    if (this.processedEventSignatures.has(eventSig)) {
      // Suppress duplicate event
      return;
    }

    this.processedEventSignatures.add(eventSig);
    if (this.processedEventSignatures.size > 200) {
      // Keep cache bounded
      const it = this.processedEventSignatures.values();
      for (let i = 0; i < 50; i++) {
        const val = it.next().value;
        if (val) this.processedEventSignatures.delete(val);
      }
    }

    const callbacks = this.flightCallbacks.get(flightId);
    if (callbacks) {
      callbacks.forEach((cb) => {
        try {
          cb(event);
        } catch (err) {
          console.error('Error executing flight status callback', err);
        }
      });
    }
  }

  private notifyConnectionListeners(): void {
    this.connectionListeners.forEach((listener) => {
      try {
        listener(this.connected, this.connectionError);
      } catch (e) {
        // ignore
      }
    });
  }
}

// Export singleton instance
export const flightStatusWebSocketManager = new FlightStatusWebSocketManager();
