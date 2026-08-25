import { apiClient } from './api';
import { ApiResponse, Flight, FlightSearchParams, FlightSearchResponse, AirportInfo, FlightStatusSnapshot } from '../types/api';

export const POPULAR_AIRPORTS: AirportInfo[] = [
  // Major Indian Metros & Hubs
  { code: 'DEL', name: 'Indira Gandhi International Airport', city: 'New Delhi', country: 'India', terminal: 'T3' },
  { code: 'BOM', name: 'Chhatrapati Shivaji Maharaj International Airport', city: 'Mumbai', country: 'India', terminal: 'T2' },
  { code: 'BLR', name: 'Kempegowda International Airport', city: 'Bengaluru', country: 'India', terminal: 'T2' },
  { code: 'MAA', name: 'Chennai International Airport', city: 'Chennai', country: 'India', terminal: 'T1' },
  { code: 'HYD', name: 'Rajiv Gandhi International Airport', city: 'Hyderabad', country: 'India', terminal: 'T1' },
  { code: 'CCU', name: 'Netaji Subhash Chandra Bose International Airport', city: 'Kolkata', country: 'India', terminal: 'T2' },

  // Indian Leisure & Hotspot Destinations
  { code: 'GOI', name: 'Dabolim Airport', city: 'Goa', country: 'India', terminal: 'T1' },
  { code: 'GOX', name: 'Manohar International Airport (Mopa)', city: 'Goa', country: 'India', terminal: 'T1' },
  { code: 'SXR', name: 'Sheikh ul-Alam International Airport', city: 'Srinagar (Kashmir)', country: 'India', terminal: 'T1' },
  { code: 'COK', name: 'Cochin International Airport', city: 'Kochi (Kerala)', country: 'India', terminal: 'T3' },
  { code: 'JAI', name: 'Jaipur International Airport', city: 'Jaipur', country: 'India', terminal: 'T2' },
  { code: 'UDR', name: 'Maharana Pratap Airport', city: 'Udaipur', country: 'India', terminal: 'T1' },
  { code: 'VNS', name: 'Lal Bahadur Shastri International Airport', city: 'Varanasi', country: 'India', terminal: 'T1' },
  { code: 'IXZ', name: 'Veer Savarkar International Airport', city: 'Port Blair (Andaman)', country: 'India', terminal: 'T2' },
  { code: 'AMD', name: 'Sardar Vallabhbhai Patel International Airport', city: 'Ahmedabad', country: 'India', terminal: 'T2' },
  { code: 'PNQ', name: 'Pune International Airport', city: 'Pune', country: 'India', terminal: 'T1' },
  { code: 'ATQ', name: 'Sri Guru Ram Dass Jee International Airport', city: 'Amritsar', country: 'India', terminal: 'T1' },
  { code: 'GAU', name: 'Lokpriya Gopinath Bordoloi International Airport', city: 'Guwahati', country: 'India', terminal: 'T1' },
  { code: 'IXC', name: 'Shaheed Bhagat Singh International Airport', city: 'Chandigarh', country: 'India', terminal: 'T1' },
  { code: 'TRV', name: 'Thiruvananthapuram International Airport', city: 'Thiruvananthapuram', country: 'India', terminal: 'T2' },

  // Global Tropical & Vacation Escapes
  { code: 'DPS', name: 'Ngurah Rai International Airport', city: 'Bali', country: 'Indonesia', terminal: 'International' },
  { code: 'MLE', name: 'Velana International Airport', city: 'Malé', country: 'Maldives', terminal: 'T1' },
  { code: 'BKK', name: 'Suvarnabhumi Airport', city: 'Bangkok', country: 'Thailand', terminal: 'Main' },
  { code: 'HKT', name: 'Phuket International Airport', city: 'Phuket', country: 'Thailand', terminal: 'T2' },

  // Middle East Hubs
  { code: 'DXB', name: 'Dubai International Airport', city: 'Dubai', country: 'United Arab Emirates', terminal: 'T3' },
  { code: 'AUH', name: 'Zayed International Airport', city: 'Abu Dhabi', country: 'United Arab Emirates', terminal: 'T1' },
  { code: 'DOH', name: 'Hamad International Airport', city: 'Doha', country: 'Qatar', terminal: 'T1' },

  // Southeast Asia & Far East
  { code: 'SIN', name: 'Singapore Changi Airport', city: 'Singapore', country: 'Singapore', terminal: 'T3' },
  { code: 'KUL', name: 'Kuala Lumpur International Airport', city: 'Kuala Lumpur', country: 'Malaysia', terminal: 'KLIA1' },
  { code: 'HND', name: 'Tokyo Haneda Airport', city: 'Tokyo', country: 'Japan', terminal: 'T3' },
  { code: 'NRT', name: 'Narita International Airport', city: 'Tokyo', country: 'Japan', terminal: 'T1' },
  { code: 'ICN', name: 'Incheon International Airport', city: 'Seoul', country: 'South Korea', terminal: 'T2' },

  // Europe & UK
  { code: 'LHR', name: 'Heathrow Airport', city: 'London', country: 'United Kingdom', terminal: 'T2' },
  { code: 'CDG', name: 'Paris Charles de Gaulle Airport', city: 'Paris', country: 'France', terminal: 'T2E' },
  { code: 'FRA', name: 'Frankfurt Airport', city: 'Frankfurt', country: 'Germany', terminal: 'T1' },
  { code: 'AMS', name: 'Amsterdam Airport Schiphol', city: 'Amsterdam', country: 'Netherlands', terminal: 'Lounge 3' },
  { code: 'ZRH', name: 'Zurich Airport', city: 'Zurich', country: 'Switzerland', terminal: 'Airside Center' },

  // North America & Australia
  { code: 'JFK', name: 'John F. Kennedy International Airport', city: 'New York', country: 'United States', terminal: 'T4' },
  { code: 'SFO', name: 'San Francisco International Airport', city: 'San Francisco', country: 'United States', terminal: 'International' },
  { code: 'YYZ', name: 'Toronto Pearson International Airport', city: 'Toronto', country: 'Canada', terminal: 'T1' },
  { code: 'SYD', name: 'Sydney Kingsford Smith Airport', city: 'Sydney', country: 'Australia', terminal: 'T1' },
];

export interface CachedSearchResult {
  timestamp: number;
  data: ApiResponse<FlightSearchResponse>;
}

const searchCache = new Map<string, CachedSearchResult>();
const CACHE_TTL_MS = 3 * 60 * 1000; // 3 minutes

export const flightService = {
  getCachedSearch(params: FlightSearchParams): CachedSearchResult | null {
    const cacheKey = JSON.stringify(params);
    const cached = searchCache.get(cacheKey);
    if (cached && Date.now() - cached.timestamp < CACHE_TTL_MS) {
      return cached;
    }
    return null;
  },

  async searchFlights(
    params: FlightSearchParams,
    options?: { forceRefresh?: boolean; signal?: AbortSignal }
  ): Promise<ApiResponse<FlightSearchResponse>> {
    const cacheKey = JSON.stringify(params);
    const cached = searchCache.get(cacheKey);

    if (!options?.forceRefresh && cached && Date.now() - cached.timestamp < CACHE_TTL_MS) {
      return cached.data;
    }

    const res = await apiClient.get<ApiResponse<FlightSearchResponse>>('/v1/flights/search', {
      params,
      signal: options?.signal,
    });
    if (res.data && res.data.success) {
      searchCache.set(cacheKey, { timestamp: Date.now(), data: res.data });
    }
    return res.data;
  },

  async getAllFlights(page = 0, size = 10): Promise<ApiResponse<FlightSearchResponse>> {
    const res = await apiClient.get<ApiResponse<FlightSearchResponse>>('/v1/flights', {
      params: { page, size },
    });
    return res.data;
  },

  async getFlightById(flightId: string): Promise<ApiResponse<Flight>> {
    const res = await apiClient.get<ApiResponse<Flight>>(`/v1/flights/${flightId}`);
    return res.data;
  },

  async getLiveFlightStatus(flightNumber: string): Promise<ApiResponse<FlightStatusSnapshot>> {
    const res = await apiClient.get<ApiResponse<FlightStatusSnapshot>>(`/v1/flights/live/${flightNumber}`);
    return res.data;
  },

  async getPopularLiveFlights(): Promise<ApiResponse<FlightStatusSnapshot[]>> {
    const res = await apiClient.get<ApiResponse<FlightStatusSnapshot[]>>('/v1/flights/live/popular');
    return res.data;
  },

  getAirports(): AirportInfo[] {
    return POPULAR_AIRPORTS;
  },
};

