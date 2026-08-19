import { AirportInfo, CabinClass } from './flight';

export type TicketStatus = 'ISSUED' | 'CONFIRMED' | 'USED' | 'CANCELLED' | 'REFUNDED';

export interface PassengerTicketInfo {
  passengerId: string;
  fullName: string;
  seatNumber: string;
  cabinClass: CabinClass;
  checkedIn: boolean;
}

export interface Ticket {
  id: string;
  ticketNumber: string;
  bookingId: string;
  bookingReference: string;
  userId: string;
  flightId: string;
  flightNumber: string;
  airline: string;
  airlineCode: string;
  departureAirport: AirportInfo;
  arrivalAirport: AirportInfo;
  departureTime: string;
  arrivalTime: string;
  aircraftModel: string;
  cabinClass: CabinClass;
  passengers: PassengerTicketInfo[];
  fareAmount: number;
  taxAmount: number;
  totalAmount: number;
  currency: string;
  status: TicketStatus;
  issuedAt: string;
}

export interface BoardingPass {
  id: string;
  checkInNumber: string;
  bookingId: string;
  bookingReference: string;
  ticketNumber: string;
  passengerName: string;
  flightNumber: string;
  airline: string;
  departureAirport: AirportInfo;
  arrivalAirport: AirportInfo;
  departureTime: string;
  boardingTime: string;
  gate?: string;
  terminal?: string;
  seatNumber: string;
  cabinClass: CabinClass;
  sequenceNumber?: number;
  qrCode?: string;
  barcode?: string;
  createdAt: string;
}

export interface CheckInDetails {
  id: string;
  checkInNumber: string;
  bookingId: string;
  status: 'COMPLETED';
  checkedInPassengers: PassengerTicketInfo[];
  boardingPasses: BoardingPass[];
  createdAt: string;
}
