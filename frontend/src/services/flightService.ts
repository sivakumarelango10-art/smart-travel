import { apiClient } from './api';
import {
  ApiResponse,
  Flight,
  FlightSearchParams,
  FlightSearchResponse,
  FlightStatusSnapshot,
  AirportInfo,
  CabinInventory,
  CabinClass,
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
const CACHE_TTL_MS = 15 * 60 * 1000; // 15 minutes TTL
const STORAGE_PREFIX = 'smarttravel_flight_cache_';

export function normalizeSearchKey(params: FlightSearchParams): string {
  const o = (params.origin || 'DEL').toUpperCase().trim();
  const d = (params.destination || 'BOM').toUpperCase().trim();
  const date = (params.departureDate || new Date().toISOString().split('T')[0]).trim();
  const cabin = (params.cabinClass || 'ECONOMY').toUpperCase().trim();
  const pax = params.passengers ? Number(params.passengers) : 1;
  return `flight_${o}_${d}_${date}_${cabin}_${pax}`;
}

const INTL_AIRPORTS = new Set([
  'DPS', 'MLE', 'BKK', 'HKT', 'SIN', 'KUL', 'HND', 'NRT', 'ICN',
  'DXB', 'AUH', 'DOH', 'LHR', 'CDG', 'FRA', 'AMS', 'ZRH', 'JFK', 'SFO', 'YYZ', 'SYD'
]);

function getAirportByCode(code: string): AirportInfo {
  const clean = (code || 'DEL').toUpperCase().trim();
  const found = POPULAR_AIRPORTS.find((a) => a.code.toUpperCase() === clean);
  if (found) return found;
  return {
    code: clean,
    name: `${clean} International Airport`,
    city: clean,
    country: 'India',
    terminal: 'T1',
  };
}

function isIntlRoute(orig: string, dest: string): boolean {
  return INTL_AIRPORTS.has(orig.toUpperCase().trim()) || INTL_AIRPORTS.has(dest.toUpperCase().trim());
}

function calculateRouteDuration(orig: string, dest: string): number {
  const o = (orig || 'DEL').toUpperCase().trim();
  const d = (dest || 'BOM').toUpperCase().trim();
  if (!isIntlRoute(o, d)) {
    if ((o === 'DEL' && d === 'BOM') || (o === 'BOM' && d === 'DEL')) return 130;
    if ((o === 'DEL' && d === 'BLR') || (o === 'BLR' && d === 'DEL')) return 165;
    if ((o === 'BOM' && d === 'BLR') || (o === 'BLR' && d === 'BOM')) return 95;
    if ((o === 'BOM' && d === 'GOI') || (o === 'GOI' && d === 'BOM')) return 70;
    if ((o === 'DEL' && d === 'GOI') || (o === 'GOI' && d === 'DEL')) return 150;
    if ((o === 'DEL' && d === 'CCU') || (o === 'CCU' && d === 'DEL')) return 135;
    if ((o === 'DEL' && d === 'MAA') || (o === 'MAA' && d === 'DEL')) return 170;
    return 130;
  }
  if (o === 'DXB' || d === 'DXB' || o === 'AUH' || d === 'AUH' || o === 'DOH' || d === 'DOH') return 215;
  if (o === 'SIN' || d === 'SIN' || o === 'KUL' || d === 'KUL' || o === 'BKK' || d === 'BKK') return 310;
  if (o === 'MLE' || d === 'MLE') return 195;
  if (o === 'DPS' || d === 'DPS') return 520;
  if (o === 'LHR' || d === 'LHR' || o === 'FRA' || d === 'FRA' || o === 'CDG' || d === 'CDG') return 560;
  if (o === 'JFK' || d === 'JFK' || o === 'SFO' || d === 'SFO') return 940;
  return 240;
}

function calculateBaseFare(orig: string, dest: string): number {
  const o = (orig || 'DEL').toUpperCase().trim();
  const d = (dest || 'BOM').toUpperCase().trim();
  if (!isIntlRoute(o, d)) {
    if ((o === 'DEL' && d === 'BOM') || (o === 'BOM' && d === 'DEL')) return 3950;
    if ((o === 'DEL' && d === 'BLR') || (o === 'BLR' && d === 'DEL')) return 4650;
    if ((o === 'BOM' && d === 'GOI') || (o === 'GOI' && d === 'BOM')) return 2950;
    return 4200;
  }
  if (o === 'DXB' || d === 'DXB' || o === 'AUH' || d === 'AUH') return 18500;
  if (o === 'BKK' || d === 'BKK' || o === 'SIN' || d === 'SIN' || o === 'KUL' || d === 'KUL') return 14800;
  if (o === 'MLE' || d === 'MLE') return 12500;
  if (o === 'DPS' || d === 'DPS') return 17500;
  if (o === 'LHR' || d === 'LHR' || o === 'CDG' || d === 'CDG') return 48000;
  return 22000;
}

export function generateInstantRouteFlights(params: FlightSearchParams): Flight[] {
  const origCode = (params.origin || 'DEL').toUpperCase().trim();
  const destCode = (params.destination || 'BOM').toUpperCase().trim();
  const dateStr = (params.departureDate || new Date().toISOString().split('T')[0]).trim();
  const depAirport = getAirportByCode(origCode);
  const arrAirport = getAirportByCode(destCode);
  const isIntl = isIntlRoute(origCode, destCode);
  const baseDuration = calculateRouteDuration(origCode, destCode);
  const baseFare = calculateBaseFare(origCode, destCode);
  const dateClean = dateStr.replace(/-/g, '');

  const scheduleTemplates = isIntl
    ? [
        { code: 'EK', airline: 'Emirates', num: '500', time: '04:15', model: 'Boeing 777-300ER', mult: 1.05 },
        { code: 'AI', airline: 'Air India', num: '101', time: '07:30', model: 'Boeing 787-9 Dreamliner', mult: 0.95 },
        { code: 'SQ', airline: 'Singapore Airlines', num: '402', time: '11:45', model: 'Airbus A350-900', mult: 1.10 },
        { code: 'BA', airline: 'British Airways', num: '112', time: '15:20', model: 'Boeing 787-9 Dreamliner', mult: 1.15 },
        { code: 'QR', airline: 'Qatar Airways', num: '570', time: '19:10', model: 'Airbus A350-1000', mult: 1.08 },
        { code: 'LH', airline: 'Lufthansa', num: '760', time: '23:30', model: 'Airbus A350-900', mult: 1.12 },
      ]
    : [
        { code: 'AI', airline: 'Air India', num: '101', time: '06:00', model: 'Airbus A320neo', mult: 0.96 },
        { code: '6E', airline: 'IndiGo', num: '202', time: '08:30', model: 'Airbus A321neo', mult: 0.92 },
        { code: 'UK', airline: 'Vistara', num: '955', time: '10:15', model: 'Boeing 787-9 Dreamliner', mult: 1.08 },
        { code: 'SG', airline: 'SpiceJet', num: '8169', time: '12:00', model: 'Boeing 737-800', mult: 0.88 },
        { code: '6E', airline: 'IndiGo', num: '5314', time: '14:15', model: 'Airbus A320neo', mult: 0.95 },
        { code: 'QP', airline: 'Akasa Air', num: '1301', time: '17:30', model: 'Boeing 737 MAX 8', mult: 0.90 },
        { code: 'IX', airline: 'Air India Express', num: '801', time: '19:15', model: 'Boeing 737 MAX 8', mult: 0.89 },
        { code: '6E', airline: 'IndiGo', num: '605', time: '21:00', model: 'Airbus A321neo', mult: 0.94 },
        { code: 'AI', airline: 'Air India', num: '103', time: '22:45', model: 'Airbus A320ceo', mult: 0.98 },
      ];

  return scheduleTemplates.map((item, idx) => {
    const flightNum = `${item.code}-${item.num}-${dateClean}`;
    const id = `inst_${origCode}_${destCode}_${item.code}${item.num}_${dateClean}`;
    const depDateTime = `${dateStr}T${item.time}:00Z`;
    const depTimestamp = new Date(depDateTime).getTime();
    const arrTimestamp = depTimestamp + baseDuration * 60 * 1000;
    const arrDateTime = new Date(arrTimestamp).toISOString();
    const price = Math.round(baseFare * item.mult);

    const cabinInventories: CabinInventory[] = [
      {
        cabinClass: 'ECONOMY',
        totalSeats: 140,
        availableSeats: 115 - ((idx * 7) % 40),
        basePrice: price,
        taxAmount: Math.round(price * 0.05),
        feeAmount: Math.round(price * 0.03),
        totalPrice: Math.round(price * 1.08),
      },
      {
        cabinClass: 'PREMIUM_ECONOMY',
        totalSeats: 24,
        availableSeats: 18 - ((idx * 2) % 10),
        basePrice: Math.round(price * 1.5),
        taxAmount: Math.round(price * 1.5 * 0.05),
        feeAmount: Math.round(price * 1.5 * 0.03),
        totalPrice: Math.round(price * 1.5 * 1.08),
      },
      {
        cabinClass: 'BUSINESS',
        totalSeats: 16,
        availableSeats: 10 - (idx % 6),
        basePrice: Math.round(price * 2.8),
        taxAmount: Math.round(price * 2.8 * 0.05),
        feeAmount: Math.round(price * 2.8 * 0.03),
        totalPrice: Math.round(price * 2.8 * 1.08),
      },
    ];

    const cabinClasses: CabinClass[] = ['ECONOMY', 'PREMIUM_ECONOMY', 'BUSINESS'];

    return {
      id,
      flightNumber: flightNum,
      airline: item.airline,
      airlineCode: item.code,
      departureAirport: {
        ...depAirport,
        terminal: depAirport.terminal || 'T3',
        gate: `Gate ${((idx * 3 + 4) % 24) + 1}`,
      },
      arrivalAirport: {
        ...arrAirport,
        terminal: arrAirport.terminal || 'T2',
        gate: `Gate ${((idx * 2 + 7) % 18) + 1}`,
      },
      departureTime: depDateTime,
      arrivalTime: arrDateTime,
      durationMinutes: baseDuration,
      aircraftModel: item.model,
      stops: 0,
      basePrice: price,
      totalSeats: 180,
      availableSeats: 143,
      cabinClasses,
      cabinInventories,
      status: 'SCHEDULED',
      isBookable: true,
      active: true,
      createdAt: new Date().toISOString(),
    };
  });
}

function getStorageCache<T>(key: string): CacheEntry<T> | null {
  try {
    const raw = localStorage.getItem(STORAGE_PREFIX + key) || sessionStorage.getItem(STORAGE_PREFIX + key);
    if (!raw) return null;
    const parsed: CacheEntry<T> = JSON.parse(raw);
    if (parsed && Date.now() - parsed.timestamp < CACHE_TTL_MS) {
      return parsed;
    }
  } catch {
    // Storage quota or parse failed
  }
  return null;
}

function setStorageCache<T>(key: string, data: CacheEntry<T>): void {
  try {
    localStorage.setItem(STORAGE_PREFIX + key, JSON.stringify(data));
  } catch {
    try {
      sessionStorage.setItem(STORAGE_PREFIX + key, JSON.stringify(data));
    } catch {
      // Storage unavailable
    }
  }
}

export const flightService = {
  getCachedSearch(params: FlightSearchParams): CachedSearchResult | null {
    const cacheKey = normalizeSearchKey(params);
    const memoryCached = MEMORY_SEARCH_CACHE.get(cacheKey);
    if (memoryCached && Date.now() - memoryCached.timestamp < CACHE_TTL_MS) {
      return memoryCached;
    }
    const storageCached = getStorageCache<ApiResponse<FlightSearchResponse>>(cacheKey);
    if (storageCached) {
      MEMORY_SEARCH_CACHE.set(cacheKey, storageCached);
      return storageCached;
    }
    return null;
  },

  /**
   * Fast optimistic flight retrieval from in-memory cache, persistent storage,
   * or instant route catalog for sub-5ms zero-latency rendering.
   */
  getInstantSearch(params: FlightSearchParams): Flight[] {
    // 1. Check exact memory / storage cache
    const cached = this.getCachedSearch(params);
    if (cached?.data?.data) {
      const list = Array.isArray(cached.data.data)
        ? cached.data.data
        : (cached.data.data as any)?.content;
      if (Array.isArray(list) && list.length > 0) {
        return list;
      }
    }

    // 2. Check route-level cache (ignoring pax / cabin variation)
    const o = (params.origin || 'DEL').toUpperCase().trim();
    const d = (params.destination || 'BOM').toUpperCase().trim();
    const date = (params.departureDate || new Date().toISOString().split('T')[0]).trim();
    const routeKeyPrefix = `flight_${o}_${d}_${date}`;
    for (const [key, entry] of MEMORY_SEARCH_CACHE.entries()) {
      if (key.startsWith(routeKeyPrefix) && Date.now() - entry.timestamp < CACHE_TTL_MS) {
        const list = Array.isArray(entry.data.data)
          ? entry.data.data
          : (entry.data.data as any)?.content;
        if (Array.isArray(list) && list.length > 0) {
          return list;
        }
      }
    }

    // 3. Instant Route Catalog (deterministic, zero waiting time)
    return generateInstantRouteFlights(params);
  },

  async searchFlights(
    params: FlightSearchParams,
    options?: { forceRefresh?: boolean; signal?: AbortSignal }
  ): Promise<ApiResponse<FlightSearchResponse>> {
    const cacheKey = normalizeSearchKey(params);
    const cached = this.getCachedSearch(params);

    if (!options?.forceRefresh && cached) {
      // Return cached authentic data instantly; revalidate in background if older than 45 seconds
      if (Date.now() - cached.timestamp > 45 * 1000) {
        this.revalidateSearchInBackground(params, cacheKey);
      }
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
      if (cached) {
        return cached.data;
      }
      throw err;
    }
  },

  async revalidateSearchInBackground(params: FlightSearchParams, cacheKey: string) {
    try {
      const res = await apiClient.get<ApiResponse<FlightSearchResponse>>('/v1/flights/search', { params });
      if (res.data && res.data.success) {
        const cacheEntry: CachedSearchResult = { timestamp: Date.now(), data: res.data };
        MEMORY_SEARCH_CACHE.set(cacheKey, cacheEntry);
        setStorageCache(cacheKey, cacheEntry);

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
    } catch {
      // Background revalidation silently ignored
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
          message: 'Flight retrieved from cache',
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

