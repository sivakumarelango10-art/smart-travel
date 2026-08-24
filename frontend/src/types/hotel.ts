export type RoomCategory =
  | 'STANDARD'
  | 'DELUXE'
  | 'PREMIUM'
  | 'SUITE'
  | 'JUNIOR_SUITE'
  | 'EXECUTIVE_SUITE'
  | 'PRESIDENTIAL_SUITE'
  | 'VILLA';

export interface RoomType {
  id: string;
  name: string;
  category: RoomCategory;
  description: string;
  totalRooms: number;
  availableRooms: number;
  maxOccupancy: number;
  bedType: string;
  sizeInSqFt: number;
  nightlyRate: number;
  taxAmount: number;
  totalNightlyRate: number;
  currency: string;
  amenities: string[];
  imageUrls: string[];
  breakfastIncluded: boolean;
  refundable: boolean;
}

export interface HotelAddress {
  line1: string;
  line2?: string;
  city: string;
  state: string;
  postalCode?: string;
  country: string;
}

export interface HotelContactInfo {
  phone?: string;
  email?: string;
  website?: string;
}

export interface Hotel {
  id: string;
  name: string;
  address: HotelAddress;
  nearestAirportCode?: string;
  starRating: number;
  description: string;
  baseNightlyRate: number;
  currency: string;
  amenities: string[];
  imageUrls: string[];
  contactInfo?: HotelContactInfo;
  averageRating: number;
  totalReviews: number;
  active: boolean;
  roomTypes: RoomType[];
  createdAt?: string;
  updatedAt?: string;
}

export interface HotelSearchParams {
  city?: string;
  airportCode?: string;
  minStars?: number;
  maxPrice?: number;
  page?: number;
  size?: number;
}

export interface RoomAvailabilityEvent {
  hotelId: string;
  roomTypeId: string;
  roomTypeName?: string;
  category?: RoomCategory;
  availableRooms: number;
  totalRooms: number;
  nightlyRate?: number;
  action: string;
  timestamp?: string;
}
