import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL, WS_BASE_URL } from '../config/constants';
import { FlightStatusEvent, DynamicPricingEvent, SeatMapUpdateEvent, RoomAvailabilityEvent } from '../types/api';

type FlightStatusCallback = (event: FlightStatusEvent) => void;
type DynamicPricingCallback = (event: DynamicPricingEvent) => void;
type SeatMapCallback = (event: SeatMapUpdateEvent) => void;
type RoomAvailabilityCallback = (event: RoomAvailabilityEvent) => void;
type ConnectionStateListener = (connected: boolean, error: string | null) => void;

/**
 * Singleton FlightStatusWebSocketManager
 * Manages ONE single shared STOMP/WebSocket connection for the entire application,
 * multiplexing flight status (/topic/flight-status/{flightId}) and dynamic pricing
 * (/topic/pricing/{flightId}) topic subscriptions without multiplying socket connections.
 */
class FlightStatusWebSocketManager {
  private client: Client | null = null;
  private connected: boolean = false;
  private connectionError: string | null = null;

  // Active status subscribers: Map<flightId, Set<callback>>
  private flightCallbacks: Map<string, Set<FlightStatusCallback>> = new Map();
  private stompSubscriptions: Map<string, StompSubscription> = new Map();

  // Active pricing subscribers: Map<flightId, Set<callback>>
  private pricingCallbacks: Map<string, Set<DynamicPricingCallback>> = new Map();
  private pricingStompSubscriptions: Map<string, StompSubscription> = new Map();

  // Active seat map subscribers: Map<flightId, Set<callback>>
  private seatMapCallbacks: Map<string, Set<SeatMapCallback>> = new Map();
  private seatMapStompSubscriptions: Map<string, StompSubscription> = new Map();

  // Active hotel room subscribers: Map<hotelId, Set<callback>>
  private hotelRoomCallbacks: Map<string, Set<RoomAvailabilityCallback>> = new Map();
  private hotelRoomStompSubscriptions: Map<string, StompSubscription> = new Map();

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
    if (this.flightCallbacks.size === 0 && this.pricingCallbacks.size === 0 && this.connectionListeners.size === 0) {
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
   * Subscribes a consumer callback to dynamic pricing updates (/topic/pricing/{flightId}).
   * Returns an unsubscribe function for React cleanup.
   */
  public subscribePricing(flightId: string, callback: DynamicPricingCallback): () => void {
    if (!flightId) return () => {};

    if (!this.pricingCallbacks.has(flightId)) {
      this.pricingCallbacks.set(flightId, new Set());
    }
    this.pricingCallbacks.get(flightId)!.add(callback);

    if (this.connected && this.client && !this.pricingStompSubscriptions.has(flightId)) {
      this.subscribePricingStompTopic(flightId);
    } else if (!this.connected) {
      this.connect();
    }

    return () => {
      this.unsubscribePricing(flightId, callback);
    };
  }

  /**
   * Subscribes a consumer callback to seat map updates (/topic/seat-map/{flightId}).
   */
  public subscribeSeatMap(flightId: string, callback: SeatMapCallback): () => void {
    if (!flightId) return () => {};

    if (!this.seatMapCallbacks.has(flightId)) {
      this.seatMapCallbacks.set(flightId, new Set());
    }
    this.seatMapCallbacks.get(flightId)!.add(callback);

    if (this.connected && this.client && !this.seatMapStompSubscriptions.has(flightId)) {
      this.subscribeSeatMapStompTopic(flightId);
    } else if (!this.connected) {
      this.connect();
    }

    return () => {
      this.unsubscribeSeatMap(flightId, callback);
    };
  }

  /**
   * Subscribes a consumer callback to hotel room updates (/topic/hotels/{hotelId}/rooms).
   */
  public subscribeHotelRooms(hotelId: string, callback: RoomAvailabilityCallback): () => void {
    if (!hotelId) return () => {};

    if (!this.hotelRoomCallbacks.has(hotelId)) {
      this.hotelRoomCallbacks.set(hotelId, new Set());
    }
    this.hotelRoomCallbacks.get(hotelId)!.add(callback);

    if (this.connected && this.client && !this.hotelRoomStompSubscriptions.has(hotelId)) {
      this.subscribeHotelRoomsStompTopic(hotelId);
    } else if (!this.connected) {
      this.connect();
    }

    return () => {
      this.unsubscribeHotelRooms(hotelId, callback);
    };
  }

  /**
   * Unsubscribes a consumer callback for flight status.
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
   * Unsubscribes a consumer callback for dynamic pricing.
   */
  public unsubscribePricing(flightId: string, callback: DynamicPricingCallback): void {
    const callbacks = this.pricingCallbacks.get(flightId);
    if (!callbacks) return;

    callbacks.delete(callback);

    if (callbacks.size === 0) {
      this.pricingCallbacks.delete(flightId);

      const stompSub = this.pricingStompSubscriptions.get(flightId);
      if (stompSub) {
        try {
          stompSub.unsubscribe();
        } catch (e) {
          // ignore
        }
        this.pricingStompSubscriptions.delete(flightId);
      }
    }
  }

  /**
   * Unsubscribes a consumer callback for seat map.
   */
  public unsubscribeSeatMap(flightId: string, callback: SeatMapCallback): void {
    const callbacks = this.seatMapCallbacks.get(flightId);
    if (!callbacks) return;

    callbacks.delete(callback);

    if (callbacks.size === 0) {
      this.seatMapCallbacks.delete(flightId);

      const stompSub = this.seatMapStompSubscriptions.get(flightId);
      if (stompSub) {
        try {
          stompSub.unsubscribe();
        } catch (e) {
          // ignore
        }
        this.seatMapStompSubscriptions.delete(flightId);
      }
    }
  }

  /**
   * Unsubscribes a consumer callback for hotel rooms.
   */
  public unsubscribeHotelRooms(hotelId: string, callback: RoomAvailabilityCallback): void {
    const callbacks = this.hotelRoomCallbacks.get(hotelId);
    if (!callbacks) return;

    callbacks.delete(callback);

    if (callbacks.size === 0) {
      this.hotelRoomCallbacks.delete(hotelId);

      const stompSub = this.hotelRoomStompSubscriptions.get(hotelId);
      if (stompSub) {
        try {
          stompSub.unsubscribe();
        } catch (e) {
          // ignore
        }
        this.hotelRoomStompSubscriptions.delete(hotelId);
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

    this.pricingStompSubscriptions.forEach((sub) => {
      try {
        sub.unsubscribe();
      } catch (e) {
        // ignore
      }
    });
    this.pricingStompSubscriptions.clear();

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

  private subscribePricingStompTopic(flightId: string): void {
    if (!this.client || !this.connected) return;

    const topic = `/topic/pricing/${flightId}`;
    try {
      const stompSub = this.client.subscribe(topic, (message: IMessage) => {
        try {
          const event: DynamicPricingEvent = JSON.parse(message.body);
          this.handleDynamicPricingEvent(flightId, event);
        } catch (err) {
          console.error('Failed to parse dynamic pricing message', err);
        }
      });
      this.pricingStompSubscriptions.set(flightId, stompSub);
    } catch (err) {
      console.warn(`Failed to subscribe to STOMP pricing topic ${topic}:`, err);
    }
  }

  private subscribeSeatMapStompTopic(flightId: string): void {
    if (!this.client || !this.connected) return;

    const topic = `/topic/seat-map/${flightId}`;
    try {
      const stompSub = this.client.subscribe(topic, (message: IMessage) => {
        try {
          const event: SeatMapUpdateEvent = JSON.parse(message.body);
          this.handleSeatMapEvent(flightId, event);
        } catch (err) {
          console.error('Failed to parse seat map message', err);
        }
      });
      this.seatMapStompSubscriptions.set(flightId, stompSub);
    } catch (err) {
      console.warn(`Failed to subscribe to STOMP seat map topic ${topic}:`, err);
    }
  }

  private subscribeHotelRoomsStompTopic(hotelId: string): void {
    if (!this.client || !this.connected) return;

    const topic = `/topic/hotels/${hotelId}/rooms`;
    try {
      const stompSub = this.client.subscribe(topic, (message: IMessage) => {
        try {
          const event: RoomAvailabilityEvent = JSON.parse(message.body);
          this.handleHotelRoomEvent(hotelId, event);
        } catch (err) {
          console.error('Failed to parse hotel room message', err);
        }
      });
      this.hotelRoomStompSubscriptions.set(hotelId, stompSub);
    } catch (err) {
      console.warn(`Failed to subscribe to STOMP hotel room topic ${topic}:`, err);
    }
  }

  private restoreSubscriptions(): void {
    for (const flightId of this.flightCallbacks.keys()) {
      if (!this.stompSubscriptions.has(flightId)) {
        this.subscribeStompTopic(flightId);
      }
    }
    for (const flightId of this.pricingCallbacks.keys()) {
      if (!this.pricingStompSubscriptions.has(flightId)) {
        this.subscribePricingStompTopic(flightId);
      }
    }
    for (const flightId of this.seatMapCallbacks.keys()) {
      if (!this.seatMapStompSubscriptions.has(flightId)) {
        this.subscribeSeatMapStompTopic(flightId);
      }
    }
    for (const hotelId of this.hotelRoomCallbacks.keys()) {
      if (!this.hotelRoomStompSubscriptions.has(hotelId)) {
        this.subscribeHotelRoomsStompTopic(hotelId);
      }
    }
  }

  private handleSeatMapEvent(flightId: string, event: SeatMapUpdateEvent): void {
    const callbacks = this.seatMapCallbacks.get(flightId);
    if (callbacks) {
      callbacks.forEach((cb) => {
        try {
          cb(event);
        } catch (err) {
          console.error('Error executing seat map callback', err);
        }
      });
    }
  }

  private handleHotelRoomEvent(hotelId: string, event: RoomAvailabilityEvent): void {
    const callbacks = this.hotelRoomCallbacks.get(hotelId);
    if (callbacks) {
      callbacks.forEach((cb) => {
        try {
          cb(event);
        } catch (err) {
          console.error('Error executing hotel room callback', err);
        }
      });
    }
  }

  private handleDynamicPricingEvent(flightId: string, event: DynamicPricingEvent): void {
    const callbacks = this.pricingCallbacks.get(flightId);
    if (callbacks) {
      callbacks.forEach((cb) => {
        try {
          cb(event);
        } catch (err) {
          console.error('Error executing dynamic pricing callback', err);
        }
      });
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
