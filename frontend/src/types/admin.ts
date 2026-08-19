/**
 * Admin-specific TypeScript types for Phase 10 Admin Frontend.
 * These types mirror the backend DTOs exactly as verified from the Spring Boot source.
 */

import { Flight, FlightStatus, CabinClass } from './flight';
import { Booking, BookingStatus } from './booking';

// ─── Page Response (generic) ─────────────────────────────────────────────────

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// ─── Admin Flight ─────────────────────────────────────────────────────────────

export type { Flight as AdminFlight };

export interface FlightCreateRequest {
  flightNumber: string;
  airline: string;
  airlineCode: string;
  departureAirport: AirportDto;
  arrivalAirport: AirportDto;
  departureTime: string; // ISO-8601 UTC
  arrivalTime: string;   // ISO-8601 UTC
  aircraftModel: string;
  basePrice: number;
  totalSeats: number;
  availableSeats?: number;
  cabinClasses?: CabinClass[];
  cabinInventories?: CabinInventoryDto[];
  status?: FlightStatus;
}

export interface FlightUpdateRequest {
  airline?: string;
  airlineCode?: string;
  departureAirport?: AirportDto;
  arrivalAirport?: AirportDto;
  departureTime?: string;
  arrivalTime?: string;
  aircraftModel?: string;
  basePrice?: number;
  totalSeats?: number;
  availableSeats?: number;
  cabinClasses?: CabinClass[];
  cabinInventories?: CabinInventoryDto[];
  active?: boolean;
}

export interface FlightStatusUpdateRequest {
  status: FlightStatus;
  reason?: string;
}

export interface FlightInventoryUpdateRequest {
  cabinInventories: CabinInventoryDto[];
}

export interface CabinInventoryDto {
  cabinClass: CabinClass;
  totalSeats: number;
  availableSeats: number;
  basePrice: number;
  taxAmount?: number;
  feeAmount?: number;
}

export interface AirportDto {
  code: string;
  name: string;
  city: string;
  country: string;
  terminal?: string;
  gate?: string;
}

// ─── Disruption ───────────────────────────────────────────────────────────────

export type DisruptionType = 'DELAY' | 'CANCELLATION' | 'GATE_CHANGE' | 'TERMINAL_CHANGE' | 'AIRCRAFT_CHANGE' | 'DIVERSION';
export type DisruptionStatus = 'ACTIVE' | 'RESOLVED';

export interface FlightDisruption {
  id: string;
  flightId: string;
  flightNumber: string;
  disruptionType: DisruptionType;
  reason: string;
  description?: string;
  previousDepartureTime?: string;
  newDepartureTime?: string;
  previousArrivalTime?: string;
  newArrivalTime?: string;
  previousGate?: string;
  newGate?: string;
  previousTerminal?: string;
  newTerminal?: string;
  previousAircraftModel?: string;
  newAircraftModel?: string;
  status: DisruptionStatus;
  createdBy: string;
  createdAt: string;
  resolvedAt?: string;
}

export interface FlightOperationalStatusResponse {
  flightId: string;
  flightNumber: string;
  currentStatus: FlightStatus;
  disruption?: FlightDisruption;
}

export interface FlightImpactSummary {
  flightId: string;
  flightNumber: string;
  affectedBookings: number;
  affectedPassengers: number;
  checkedInPassengers: number;
}

export interface FlightScheduleChangeRequest {
  newDepartureTime: string;
  newArrivalTime: string;
  reason: string;
  description?: string;
}

export interface FlightCancelRequest {
  reason: string;
  description?: string;
}

export interface FlightGateChangeRequest {
  gate: string;
  reason?: string;
}

export interface FlightTerminalChangeRequest {
  terminal: string;
  reason?: string;
}

export interface FlightAircraftChangeRequest {
  aircraftModel: string;
  reason?: string;
  force?: boolean;
}

// ─── Admin Booking ────────────────────────────────────────────────────────────

export type { Booking as AdminBooking, BookingStatus };

export interface BookingCancelRequest {
  cancellationReason?: string;
}

// ─── Admin Ticket ─────────────────────────────────────────────────────────────

export type TicketStatus = 'ISSUED' | 'VOID' | 'USED' | 'CANCELLED';

export interface AdminTicket {
  id: string;
  ticketNumber: string;
  bookingId: string;
  bookingReference: string;
  userId: string;
  userEmail?: string;
  flightId: string;
  flightNumber: string;
  airline?: string;
  passengerName?: string;
  cabinClass?: CabinClass;
  seatNumber?: string;
  status: TicketStatus;
  issuedAt: string;
  expiresAt?: string;
  checkedIn?: boolean;
}

// ─── Admin Refund ─────────────────────────────────────────────────────────────

export type RefundStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
export type RefundReason = 'FLIGHT_CANCELLED' | 'CUSTOMER_REQUEST' | 'OVERBOOKING' | 'SCHEDULE_CHANGE' | 'OTHER';

export interface AdminRefund {
  id: string;
  refundNumber: string;
  paymentId: string;
  razorpayPaymentId?: string;
  bookingId: string;
  bookingReference: string;
  userId: string;
  amount: number;
  amountPaise: number;
  currency: string;
  reason: RefundReason;
  description?: string;
  status: RefundStatus;
  gatewayRefundId?: string;
  failureReason?: string;
  requestedAt: string;
  completedAt?: string;
}

export interface RefundProcessRequest {
  reason?: RefundReason;
  description?: string;
}

export interface RefundEligibilityResponse {
  eligible: boolean;
  paymentId: string;
  reason: RefundReason;
  refundableAmount?: number;
  message?: string;
}

// ─── Health ───────────────────────────────────────────────────────────────────

export interface HealthResponse {
  status: 'UP' | 'DOWN' | 'DEGRADED';
  service: string;
  environment: string;
  database: 'CONNECTED' | 'DISCONNECTED' | 'UNKNOWN';
  timestamp: string;
}
