import { CabinClass } from './flight';

export type SeatStatus = 'AVAILABLE' | 'HELD' | 'BOOKED' | 'BLOCKED';

export interface Seat {
  id?: string;
  seatNumber: string;
  flightId: string;
  cabinClass: CabinClass;
  row: number;
  column: string;
  isWindow: boolean;
  isAisle: boolean;
  isMiddle: boolean;
  isEmergencyExit: boolean;
  extraLegroom: boolean;
  price: number;
  status: SeatStatus;
  heldByUserId?: string;
  holdExpiresAt?: string;
}

export interface SeatHoldRequest {
  seatNumber: string;
  cabinClass: CabinClass;
}

export interface SeatHoldResponse {
  success: boolean;
  seatNumber: string;
  status: SeatStatus;
  holdExpiresAt: string;
  message?: string;
}

export interface SeatMapResponse {
  flightId: string;
  flightNumber: string;
  aircraftModel: string;
  totalSeats: number;
  availableSeatsCount: number;
  seats: Seat[];
  cabinSeats?: Record<CabinClass, Seat[]>;
}

