import { apiClient } from './api';
import { ApiResponse, Hotel, HotelSearchParams, RoomType } from '../types/api';

const hotelSearchCache = new Map<string, { timestamp: number; data: any }>();
const hotelDetailCache = new Map<string, { timestamp: number; data: Hotel }>();
const CACHE_TTL_MS = 15 * 60 * 1000; // 15 minutes
const STORAGE_PREFIX = 'smarttravel_hotel_cache_';

function getStorage<T>(key: string): { timestamp: number; data: T } | null {
  try {
    const raw = localStorage.getItem(STORAGE_PREFIX + key) || sessionStorage.getItem(STORAGE_PREFIX + key);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (parsed && Date.now() - parsed.timestamp < CACHE_TTL_MS) {
      return parsed;
    }
  } catch {}
  return null;
}

function setStorage<T>(key: string, data: { timestamp: number; data: T }): void {
  try {
    localStorage.setItem(STORAGE_PREFIX + key, JSON.stringify(data));
  } catch {
    try {
      sessionStorage.setItem(STORAGE_PREFIX + key, JSON.stringify(data));
    } catch {}
  }
}

export const hotelService = {
  /**
   * Fast retrieval from in-memory or storage cache for sub-5ms instant rendering.
   */
  getCachedSearch(params?: HotelSearchParams): {
    content: Hotel[];
    totalElements: number;
    totalPages: number;
    page: number;
  } | null {
    const cacheKey = JSON.stringify(params || {});
    const mem = hotelSearchCache.get(cacheKey);
    if (mem && Date.now() - mem.timestamp < CACHE_TTL_MS) return mem.data;

    const stored = getStorage<{ content: Hotel[]; totalElements: number; totalPages: number; page: number }>(cacheKey);
    if (stored) {
      hotelSearchCache.set(cacheKey, stored);
      return stored.data;
    }

    return null;
  },

  getInstantSearch(params?: HotelSearchParams): {
    content: Hotel[];
    totalElements: number;
    totalPages: number;
    page: number;
  } {
    return this.getCachedSearch(params) || {
      content: [],
      totalElements: 0,
      totalPages: 0,
      page: 0,
    };
  },

  /**
   * Fast single hotel lookup from cache.
   */
  getCachedHotel(hotelId: string): Hotel | null {
    if (!hotelId) return null;
    const cleanId = hotelId.trim().toLowerCase();
    const mem = hotelDetailCache.get(cleanId);
    if (mem && Date.now() - mem.timestamp < CACHE_TTL_MS) return mem.data;

    const stored = getStorage<Hotel>(`detail_${cleanId}`);
    if (stored) {
      hotelDetailCache.set(cleanId, stored);
      return stored.data;
    }

    return null;
  },

  getInstantHotel(hotelId: string): Hotel | null {
    return this.getCachedHotel(hotelId);
  },

  /**
   * Search hotels from live MongoDB Atlas database with Stale-While-Revalidate (SWR).
   */
  async searchHotels(params?: HotelSearchParams): Promise<{
    content: Hotel[];
    totalElements: number;
    totalPages: number;
    page: number;
  }> {
    const cacheKey = JSON.stringify(params || {});
    const cached = this.getCachedSearch(params);

    const networkPromise = apiClient
      .get<
        ApiResponse<{
          content: Hotel[];
          totalElements: number;
          totalPages: number;
          number: number;
        }>
      >('/v1/hotels', { params })
      .then((response) => {
        const data = response.data?.data;
        if (data && Array.isArray(data.content)) {
          const result = {
            content: data.content,
            totalElements: data.totalElements,
            totalPages: data.totalPages,
            page: data.number,
          };
          const entry = { timestamp: Date.now(), data: result };
          hotelSearchCache.set(cacheKey, entry);
          setStorage(cacheKey, entry);

          // Populate individual hotels into detail cache for 0ms navigation
          data.content.forEach((h: Hotel) => {
            if (h && h.id) {
              const hEntry = { timestamp: Date.now(), data: h };
              hotelDetailCache.set(h.id.toLowerCase(), hEntry);
              setStorage(`detail_${h.id.toLowerCase()}`, hEntry);
            }
          });

          return result;
        }
        return cached || { content: [], totalElements: 0, totalPages: 0, page: 0 };
      });

    if (cached) {
      // Revalidate in background while returning cached data immediately
      networkPromise.catch(() => {});
      return cached;
    }

    return networkPromise;
  },

  /**
   * Get single hotel details from MongoDB Atlas with SWR caching.
   */
  async getHotel(hotelId: string): Promise<Hotel> {
    const cleanId = hotelId ? hotelId.trim().replace(/\s+/g, '-').replace(/_+/g, '-') : '';
    const cached = this.getCachedHotel(cleanId);

    const networkPromise = apiClient
      .get<ApiResponse<Hotel>>(`/v1/hotels/${encodeURIComponent(cleanId)}`)
      .then((response) => {
        const serverHotel = response.data?.data;
        if (serverHotel) {
          const entry = { timestamp: Date.now(), data: serverHotel };
          hotelDetailCache.set(cleanId.toLowerCase(), entry);
          setStorage(`detail_${cleanId.toLowerCase()}`, entry);
          return serverHotel;
        }
        if (cached) return cached;
        throw new Error('Hotel not found');
      });

    if (cached) {
      networkPromise.catch(() => {});
      return cached;
    }

    return networkPromise;
  },


  /**
   * Get available room types for a hotel.
   */
  async getRoomTypes(hotelId: string): Promise<RoomType[]> {
    const cleanId = hotelId ? hotelId.trim().replace(/\s+/g, '-').replace(/_+/g, '-') : '';
    const instant = this.getInstantHotel(cleanId);
    const instantRooms = instant?.roomTypes || [];

    try {
      const response = await apiClient.get<ApiResponse<RoomType[]>>(`/v1/hotels/${encodeURIComponent(cleanId)}/rooms`, {
        timeout: 4000,
      });
      return response.data?.data || instantRooms;
    } catch {
      return instantRooms;
    }
  },

  /**
   * Temporarily hold a room during checkout.
   */
  async holdRoom(hotelId: string, roomTypeId: string, roomCount: number = 1): Promise<RoomType> {
    const response = await apiClient.post<ApiResponse<RoomType>>(
      `/v1/hotels/${hotelId}/rooms/${roomTypeId}/hold`,
      null,
      { params: { roomCount } }
    );
    return response.data.data;
  },

  /**
   * Release a held room if checkout is cancelled.
   */
  async releaseRoom(hotelId: string, roomTypeId: string, roomCount: number = 1): Promise<void> {
    await apiClient.post<ApiResponse<void>>(
      `/v1/hotels/${hotelId}/rooms/${roomTypeId}/release`,
      null,
      { params: { roomCount } }
    );
  },

  /**
   * Calculate authoritative stay price and tax breakdown.
   */
  async calculatePrice(request: import('../types/hotel').HotelPriceCalculateRequest): Promise<import('../types/hotel').HotelPriceCalculateResponse> {
    const response = await apiClient.post<ApiResponse<import('../types/hotel').HotelPriceCalculateResponse>>(
      '/v1/hotels/pricing/calculate',
      request
    );
    return response.data.data;
  },

  /**
   * Create a confirmed hotel reservation.
   */
  async createBooking(request: import('../types/hotel').CreateHotelBookingRequest): Promise<import('../types/hotel').HotelBooking> {
    const response = await apiClient.post<ApiResponse<import('../types/hotel').HotelBooking>>(
      '/v1/hotels/bookings',
      request
    );
    return response.data.data;
  },

  /**
   * Get user's hotel reservations.
   */
  async getMyBookings(page: number = 0, size: number = 20): Promise<{
    content: import('../types/hotel').HotelBooking[];
    totalElements: number;
    totalPages: number;
  }> {
    let serverBookings: import('../types/hotel').HotelBooking[] = [];
    try {
      const response = await apiClient.get<ApiResponse<{
        content: import('../types/hotel').HotelBooking[];
        totalElements: number;
        totalPages: number;
      }>>('/v1/hotels/bookings/my', { params: { page, size } });
      serverBookings = response.data?.data?.content || [];
    } catch {
      // Backend may be waking up; proceed to load local saved bookings
    }

    // Merge with any local bookings
    let localBookings: import('../types/hotel').HotelBooking[] = [];
    try {
      localBookings = JSON.parse(localStorage.getItem('smarttravel_local_hotel_bookings') || '[]');
    } catch {}

    const combinedMap = new Map<string, import('../types/hotel').HotelBooking>();
    localBookings.forEach((b) => {
      if (b && (b.bookingReference || b.id)) {
        combinedMap.set(b.bookingReference || b.id, b);
      }
    });
    serverBookings.forEach((b) => {
      if (b && (b.bookingReference || b.id)) {
        combinedMap.set(b.bookingReference || b.id, b);
      }
    });

    const allBookings = Array.from(combinedMap.values()).sort(
      (a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()
    );

    return {
      content: allBookings.slice(page * size, (page + 1) * size),
      totalElements: allBookings.length,
      totalPages: Math.ceil(allBookings.length / size) || 1,
    };
  },

  /**
   * Get single hotel booking details by ID.
   */
  async getBooking(id: string): Promise<import('../types/hotel').HotelBooking> {
    const response = await apiClient.get<ApiResponse<import('../types/hotel').HotelBooking>>(`/v1/hotels/bookings/${id}`);
    return response.data.data;
  },

  /**
   * Cancel hotel booking with automated refund calculation.
   */
  async cancelBooking(id: string, cancellationReason?: string): Promise<import('../types/hotel').HotelBooking> {
    const response = await apiClient.post<ApiResponse<import('../types/hotel').HotelBooking>>(
      `/v1/hotels/bookings/${id}/cancel`,
      { cancellationReason }
    );
    return response.data.data;
  },

  /**
   * Preview refund for a hotel booking before confirming cancellation.
   */
  async getRefundPreview(id: string): Promise<import('../types/hotel').HotelRefundCalculation> {
    const response = await apiClient.get<ApiResponse<import('../types/hotel').HotelRefundCalculation>>(
      `/v1/hotels/bookings/${id}/refund-preview`
    );
    return response.data.data;
  },
};
