import { FlightStatus } from './flight';

export interface TrackedFlight {
  id: string;
  flightId: string;
  flightNumber: string;
  route: string;
  active: boolean;
  lastKnownStatus?: FlightStatus;
  lastKnownEta?: string;
  trackedAt: string;

  // Live operational details
  currentStatus?: FlightStatus;
  delayMinutes?: number;
  delayReason?: string;
  scheduledDeparture?: string;
  revisedDeparture?: string;
  scheduledArrival?: string;
  estimatedArrival?: string;
  departureAirportCode?: string;
  arrivalAirportCode?: string;
  departureAirportCity?: string;
  arrivalAirportCity?: string;
}

export interface FlightStatusSnapshot {
  flightNumber: string;
  status: FlightStatus;
  delayMinutes?: number;
  delayReason?: string;
  revisedDepartureTime?: string;
  revisedArrivalTime?: string;
  gate?: string;
  terminal?: string;
  updatedSource?: string;
  airline?: string;
  airlineCode?: string;
  originCode?: string;
  originCity?: string;
  originName?: string;
  destCode?: string;
  destCity?: string;
  destName?: string;
  scheduledDeparture?: string;
  scheduledArrival?: string;
  aircraftModel?: string;
  altitudeFeet?: number;
  groundSpeedKmph?: number;
  progressPercent?: number;
  baggageCarousel?: string;
  originLat?: number;
  originLng?: number;
  destLat?: number;
  destLng?: number;
  currentLat?: number;
  currentLng?: number;
  flightId?: string;
}

export interface FlightStatusEvent {
  eventId?: string;
  flightId: string;
  flightNumber: string;
  previousStatus?: FlightStatus;
  status: FlightStatus;
  delayMinutes?: number;
  delayReason?: string;
  scheduledDeparture?: string;
  revisedDeparture?: string;
  scheduledArrival?: string;
  estimatedArrival?: string;
  gate?: string;
  terminal?: string;
  updatedAt: string;
  source?: string;
}


