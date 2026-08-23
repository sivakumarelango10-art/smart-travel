import { useEffect, useState } from 'react';
import { flightStatusWebSocketManager } from '../services/flightStatusWebSocketManager';
import { CabinClass, DynamicPricingEvent } from '../types/api';

/**
 * Hook to subscribe to real-time dynamic pricing updates for a flight.
 * Multiplexes over the shared STOMP/WebSocket connection.
 */
export function useFlightPricingWebSocket(
  flightId: string | undefined,
  cabinClass?: CabinClass,
  onPriceChange?: (event: DynamicPricingEvent) => void
) {
  const [latestEvent, setLatestEvent] = useState<DynamicPricingEvent | null>(null);
  const [updatedPrice, setUpdatedPrice] = useState<number | null>(null);

  useEffect(() => {
    if (!flightId) return;

    const unsubscribe = flightStatusWebSocketManager.subscribePricing(
      flightId,
      (event: DynamicPricingEvent) => {
        if (!cabinClass || event.cabinClass === cabinClass) {
          setLatestEvent(event);
          setUpdatedPrice(event.newPrice);
          onPriceChange?.(event);
        }
      }
    );

    return () => {
      unsubscribe();
    };
  }, [flightId, cabinClass, onPriceChange]);

  return {
    latestEvent,
    updatedPrice,
  };
}
