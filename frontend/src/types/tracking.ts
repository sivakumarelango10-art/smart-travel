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

export interface FlightStatusEvent {
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
