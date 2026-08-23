import { AirportInfo, CabinClass, FareBreakdown } from './flight';

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED' | 'CHECKED_IN' | 'COMPLETED';

export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export interface Passenger {
  id?: string;
  title: 'Mr' | 'Ms' | 'Mrs' | 'Dr';
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  gender?: Gender;
  nationality?: string;
  passportNumber?: string;
  seatNumber?: string;
  checkedIn?: boolean;
}

export interface BookingCreateRequest {
  flightId: string;
  cabinClass: CabinClass;
  passengers: Passenger[];
  priceFreezeId?: string;
}

export interface Booking {
  id: string;
  bookingReference: string;
  userId: string;
  userEmail: string;
  flightId: string;
  flightNumber: string;
  airline: string;
  airlineCode: string;
  departureAirport: AirportInfo;
  arrivalAirport: AirportInfo;
  departureTime: string;
  arrivalTime: string;
  durationMinutes: number;
  cabinClass: CabinClass;
  passengerCount: number;
  passengers: Passenger[];
  fareBreakdown: FareBreakdown;
  totalAmount: number;
  currency: string;
  status: BookingStatus;
  ticketId?: string;
  ticketNumber?: string;
  expiresAt?: string;
  cancelledAt?: string;
  cancellationReason?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface BookingPageResponse {
  content: Booking[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
