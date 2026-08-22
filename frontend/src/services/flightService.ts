import { apiClient } from './api';
import { ApiResponse, Flight, FlightSearchParams, FlightSearchResponse, AirportInfo, FlightStatusSnapshot } from '../types/api';

export const POPULAR_AIRPORTS: AirportInfo[] = [
  { code: 'DEL', name: 'Indira Gandhi International Airport', city: 'New Delhi', country: 'India', terminal: 'T3' },
  { code: 'BOM', name: 'Chhatrapati Shivaji Maharaj International Airport', city: 'Mumbai', country: 'India', terminal: 'T2' },
  { code: 'BLR', name: 'Kempegowda International Airport', city: 'Bengaluru', country: 'India', terminal: 'T1' },
  { code: 'MAA', name: 'Chennai International Airport', city: 'Chennai', country: 'India', terminal: 'T4' },
  { code: 'HYD', name: 'Rajiv Gandhi International Airport', city: 'Hyderabad', country: 'India', terminal: 'T1' },
  { code: 'CCU', name: 'Netaji Subhash Chandra Bose International Airport', city: 'Kolkata', country: 'India', terminal: 'T2' },
  { code: 'DXB', name: 'Dubai International Airport', city: 'Dubai', country: 'UAE', terminal: 'T3' },
  { code: 'SIN', name: 'Singapore Changi Airport', city: 'Singapore', country: 'Singapore', terminal: 'T3' },
  { code: 'LHR', name: 'Heathrow Airport', city: 'London', country: 'United Kingdom', terminal: 'T5' },
  { code: 'JFK', name: 'John F. Kennedy International Airport', city: 'New York', country: 'United States', terminal: 'T4' },
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

