import { apiClient } from './api';
import {
  ApiResponse,
  Flight,
  FlightSearchParams,
  FlightSearchResponse,
  FlightStatusSnapshot,
  AirportInfo,
} from '../types/api';

export const POPULAR_AIRPORTS: AirportInfo[] = [
  // Domestic Major Metros
  { code: 'DEL', name: 'Indira Gandhi International Airport', city: 'New Delhi', country: 'India', terminal: 'T3' },
  { code: 'BOM', name: 'Chhatrapati Shivaji Maharaj International Airport', city: 'Mumbai', country: 'India', terminal: 'T2' },
  { code: 'BLR', name: 'Kempegowda International Airport', city: 'Bengaluru', country: 'India', terminal: 'T2' },
  { code: 'MAA', name: 'Chennai International Airport', city: 'Chennai', country: 'India', terminal: 'T1' },
  { code: 'CCU', name: 'Netaji Subhash Chandra Bose International Airport', city: 'Kolkata', country: 'India', terminal: 'T2' },
  { code: 'HYD', name: 'Rajiv Gandhi International Airport', city: 'Hyderabad', country: 'India', terminal: 'T1' },

  // Domestic Leisure & Regional Hotspots
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

  // Tropical Escapes & Southeast Asia
  { code: 'DPS', name: 'Ngurah Rai International Airport', city: 'Bali', country: 'Indonesia', terminal: 'International' },
  { code: 'MLE', name: 'Velana International Airport', city: 'Malé', country: 'Maldives', terminal: 'T1' },
  { code: 'BKK', name: 'Suvarnabhumi Airport', city: 'Bangkok', country: 'Thailand', terminal: 'Main' },
  { code: 'HKT', name: 'Phuket International Airport', city: 'Phuket', country: 'Thailand', terminal: 'T2' },
  { code: 'SIN', name: 'Singapore Changi Airport', city: 'Singapore', country: 'Singapore', terminal: 'T3' },
  { code: 'KUL', name: 'Kuala Lumpur International Airport', city: 'Kuala Lumpur', country: 'Malaysia', terminal: 'KLIA1' },
  { code: 'HND', name: 'Tokyo Haneda Airport', city: 'Tokyo', country: 'Japan', terminal: 'T3' },
  { code: 'NRT', name: 'Narita International Airport', city: 'Tokyo', country: 'Japan', terminal: 'T1' },
  { code: 'ICN', name: 'Incheon International Airport', city: 'Seoul', country: 'South Korea', terminal: 'T2' },

  // Middle East
  { code: 'DXB', name: 'Dubai International Airport', city: 'Dubai', country: 'United Arab Emirates', terminal: 'T3' },
  { code: 'AUH', name: 'Zayed International Airport', city: 'Abu Dhabi', country: 'United Arab Emirates', terminal: 'T1' },
  { code: 'DOH', name: 'Hamad International Airport', city: 'Doha', country: 'Qatar', terminal: 'T1' },

  // Europe & UK
  { code: 'LHR', name: 'London Heathrow Airport', city: 'London', country: 'United Kingdom', terminal: 'T2' },
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

export interface CacheEntry<T> {
  timestamp: number;
  data: T;
}

export type CachedSearchResult = CacheEntry<ApiResponse<FlightSearchResponse>>;

const MEMORY_SEARCH_CACHE = new Map<string, CachedSearchResult>();
const MEMORY_FLIGHT_CACHE = new Map<string, CacheEntry<Flight>>();
const CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
const STORAGE_PREFIX = 'smarttravel_flight_cache_';

function getStorageCache<T>(key: string): CacheEntry<T> | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_PREFIX + key);
    if (!raw) return null;
    const parsed: CacheEntry<T> = JSON.parse(raw);
    if (parsed && Date.now() - parsed.timestamp < CACHE_TTL_MS) {
      return parsed;
    }
  } catch {
    // sessionStorage unavailable or parse failed
  }
  return null;
}

function setStorageCache<T>(key: string, data: CacheEntry<T>): void {
  try {
    sessionStorage.setItem(STORAGE_PREFIX + key, JSON.stringify(data));
  } catch {
    // Storage quota or unavailable
  }
}

export const flightService = {
  getCachedSearch(params: FlightSearchParams): CachedSearchResult | null {
    const cacheKey = JSON.stringify(params);
    const memoryCached = MEMORY_SEARCH_CACHE.get(cacheKey);
    if (memoryCached && Date.now() - memoryCached.timestamp < CACHE_TTL_MS) {
      return memoryCached;
    }
    const sessionCached = getStorageCache<ApiResponse<FlightSearchResponse>>(cacheKey);
    if (sessionCached) {
      MEMORY_SEARCH_CACHE.set(cacheKey, sessionCached);
      return sessionCached;
    }
    return null;
  },

  async searchFlights(
    params: FlightSearchParams,
    options?: { forceRefresh?: boolean; signal?: AbortSignal }
  ): Promise<ApiResponse<FlightSearchResponse>> {
    const cacheKey = JSON.stringify(params);
    const cached = this.getCachedSearch(params);

    if (!options?.forceRefresh && cached) {
      return cached.data;
    }

    try {
      const res = await apiClient.get<ApiResponse<FlightSearchResponse>>('/v1/flights/search', {
        params,
        signal: options?.signal,
      });

      if (res.data && res.data.success) {
        const cacheEntry: CachedSearchResult = { timestamp: Date.now(), data: res.data };
        MEMORY_SEARCH_CACHE.set(cacheKey, cacheEntry);
        setStorageCache(cacheKey, cacheEntry);

        // Pre-populate individual flight caches for 0ms booking page loads
        const flightList = Array.isArray(res.data.data)
          ? res.data.data
          : (res.data.data as any)?.content;
        if (Array.isArray(flightList)) {
          flightList.forEach((f: Flight) => {
            if (f && f.id) {
              const fKey = `flight_${f.id}`;
              const fEntry: CacheEntry<Flight> = { timestamp: Date.now(), data: f };
              MEMORY_FLIGHT_CACHE.set(fKey, fEntry);
              setStorageCache(fKey, fEntry);
            }
          });
        }
      }
      return res.data;
    } catch (err: any) {
      // If network failed or aborted, and we have a stale cache, return it rather than failing
      if (cached) {
        return cached.data;
      }
      throw err;
    }
  },

  async getAllFlights(page = 0, size = 10): Promise<ApiResponse<FlightSearchResponse>> {
    const res = await apiClient.get<ApiResponse<FlightSearchResponse>>('/v1/flights', {
      params: { page, size },
    });
    return res.data;
  },

  getCachedFlightById(flightId: string): Flight | null {
    if (!flightId) return null;
    const key = `flight_${flightId}`;
    const mem = MEMORY_FLIGHT_CACHE.get(key);
    if (mem && Date.now() - mem.timestamp < CACHE_TTL_MS) {
      return mem.data;
    }
    const session = getStorageCache<Flight>(key);
    if (session && session.data) {
      MEMORY_FLIGHT_CACHE.set(key, session);
      return session.data;
    }
    return null;
  },

  async getFlightById(flightId: string, options?: { forceRefresh?: boolean }): Promise<ApiResponse<Flight>> {
    const key = `flight_${flightId}`;
    const cached = this.getCachedFlightById(flightId);

    if (!options?.forceRefresh && cached) {
      return {
        success: true,
        message: 'Flight retrieved from cache',
        data: cached,
        timestamp: new Date().toISOString(),
      };
    }

    try {
      const res = await apiClient.get<ApiResponse<Flight>>(`/v1/flights/${flightId}`);
      if (res.data && res.data.success && res.data.data) {
        const entry: CacheEntry<Flight> = { timestamp: Date.now(), data: res.data.data };
        MEMORY_FLIGHT_CACHE.set(key, entry);
        setStorageCache(key, entry);
      }
      return res.data;
    } catch (err) {
      if (cached) {
        return {
          success: true,
          message: 'Flight retrieved from cache (offline fallback)',
          data: cached,
          timestamp: new Date().toISOString(),
        };
      }
      throw err;
    }
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
