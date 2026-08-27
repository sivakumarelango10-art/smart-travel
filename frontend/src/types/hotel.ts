export type RoomCategory =
  | 'STANDARD'
  | 'DELUXE'
  | 'PREMIUM'
  | 'SUITE'
  | 'JUNIOR_SUITE'
  | 'EXECUTIVE_SUITE'
  | 'PRESIDENTIAL_SUITE'
  | 'VILLA';

export interface VirtualTour {
  enabled: boolean;
  panoramaUrl: string;
  thumbnailUrl?: string;
  title?: string;
  description?: string;
  roomCategory?: string;
}

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
  virtualTour?: VirtualTour;
}

export interface HotelAddress {
  line1: string;
  line2?: string;
  city: string;
  state: string;
  postalCode?: string;
  country: string;
  latitude?: number;
  longitude?: number;
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
  virtualTour?: VirtualTour;
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

export interface HotelPriceCalculateRequest {
  hotelId: string;
  roomTypeId: string;
  checkInDate: string;
  checkOutDate: string;
  guestCount: number;
  roomCount: number;
  couponCode?: string;
}

export interface HotelPriceCalculateResponse {
  hotelId: string;
  hotelName: string;
  roomTypeId: string;
  roomTypeName: string;
  roomCategory: RoomCategory;
  checkInDate: string;
  checkOutDate: string;
  nights: number;
  guestCount: number;
  roomCount: number;
  nightlyRate: number;
  baseAmount: number;
  taxAmount: number;
  taxRatePercentage: number;
  discountAmount: number;
  totalAmount: number;
  currency: string;
  cancellationPolicy: string;
  isAvailable: boolean;
  availableRooms: number;
}

export interface CreateHotelBookingRequest {
  hotelId: string;
  roomTypeId: string;
  checkInDate: string;
  checkOutDate: string;
  guestCount: number;
  roomCount: number;
  primaryGuestName: string;
  primaryGuestEmail: string;
  primaryGuestPhone?: string;
  specialRequests?: string;
  couponCode?: string;
  paymentMethod?: string;
}

export interface HotelBooking {
  id: string;
  bookingReference: string;
  userId: string;
  userEmail: string;
  hotelId: string;
  hotelName: string;
  hotelCity: string;
  hotelAddress: string;
  hotelImageUrl: string;
  roomTypeId: string;
  roomTypeName: string;
  roomCategory: RoomCategory;
  checkInDate: string;
  checkOutDate: string;
  nights: number;
  guestCount: number;
  roomCount: number;
  primaryGuestName: string;
  primaryGuestEmail: string;
  primaryGuestPhone?: string;
  specialRequests?: string;
  nightlyRate: number;
  baseAmount: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  currency: string;
  status: 'CONFIRMED' | 'CANCELLED' | 'REFUNDED';
  paymentId?: string;
  paymentStatus?: string;
  cancellationPolicy?: string;
  cancelledAt?: string;
  cancellationReason?: string;
  refundAmount?: number;
  createdAt: string;
  updatedAt?: string;
}

export interface HotelRefundCalculation {
  bookingReference: string;
  originalAmount: number;
  refundAmount: number;
  refundPercentage: number;
  policyApplied: string;
  timelineDescription: string;
}
