export type CabinClass = 'ECONOMY' | 'PREMIUM_ECONOMY' | 'BUSINESS' | 'FIRST';

export type FlightStatus = 'SCHEDULED' | 'BOARDING' | 'DEPARTED' | 'IN_AIR' | 'LANDED' | 'ARRIVED' | 'DELAYED' | 'CANCELLED' | 'DIVERTED';

export interface AirportInfo {
  code: string;
  name: string;
  city: string;
  country: string;
  terminal?: string;
  gate?: string;
}

export interface CabinInventory {
  cabinClass: CabinClass;
  totalSeats: number;
  availableSeats: number;
  basePrice: number;
  taxAmount: number;
  feeAmount: number;
  totalPrice: number;
}

export interface FareBreakdown {
  baseFare: number;
  taxAmount: number;
  airportFee: number;
  serviceFee: number;
  discountAmount?: number;
  totalFare: number;
  currency: string;
}

export interface Flight {
  id: string;
  flightNumber: string;
  airline: string;
  airlineCode: string;
  departureAirport: AirportInfo;
  arrivalAirport: AirportInfo;
  departureTime: string;
  arrivalTime: string;
  aircraftModel: string;
  durationMinutes: number;
  stops: number;
  basePrice: number;
  totalSeats: number;
  availableSeats: number;
  cabinClasses: CabinClass[];
  cabinInventories: CabinInventory[];
  status: FlightStatus;
  isBookable?: boolean;
  active?: boolean;
  delayMinutes?: number;
  delayReason?: string;
  revisedDepartureTime?: string;
  revisedArrivalTime?: string;
  cancellationReason?: string;
  createdAt?: string;
}

export interface FlightSearchParams {
  origin?: string;
  destination?: string;
  departureDate?: string;
  cabinClass?: CabinClass;
  passengers?: number;
  minPrice?: number;
  maxPrice?: number;
  airlines?: string[];
  timeWindow?: 'MORNING' | 'AFTERNOON' | 'EVENING' | 'NIGHT';
  nonStopOnly?: boolean;
  sortBy?: 'PRICE' | 'DURATION' | 'DEPARTURE_TIME' | 'ARRIVAL_TIME';
  sortDirection?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
}

export interface FlightSearchResponse {
  content: Flight[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
