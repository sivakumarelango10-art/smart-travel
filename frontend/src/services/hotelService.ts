import { apiClient } from './api';
import { ApiResponse, Hotel, HotelSearchParams, RoomType } from '../types/api';

const hotelSearchCache = new Map<string, { timestamp: number; data: any }>();
const CACHE_TTL_MS = 3 * 60 * 1000;

export const hotelService = {
  /**
   * Search hotels with pagination and filters.
   */
  async searchHotels(params?: HotelSearchParams): Promise<{
    content: Hotel[];
    totalElements: number;
    totalPages: number;
    page: number;
  }> {
    const cacheKey = JSON.stringify(params || {});
    const cached = hotelSearchCache.get(cacheKey);

    if (cached && Date.now() - cached.timestamp < CACHE_TTL_MS) {
      return cached.data;
    }

    const response = await apiClient.get<
      ApiResponse<{
        content: Hotel[];
        totalElements: number;
        totalPages: number;
        number: number;
      }>
    >('/v1/hotels', { params });
    const data = response.data.data;
    const result = {
      content: data?.content || [],
      totalElements: data?.totalElements || 0,
      totalPages: data?.totalPages || 0,
      page: data?.number || 0,
    };
    hotelSearchCache.set(cacheKey, { timestamp: Date.now(), data: result });
    return result;
  },

  /**
   * Get single hotel details.
   */
  async getHotel(hotelId: string): Promise<Hotel> {
    const cleanId = hotelId ? hotelId.trim().replace(/\s+/g, '-').replace(/_+/g, '-') : '';
    const response = await apiClient.get<ApiResponse<Hotel>>(`/v1/hotels/${encodeURIComponent(cleanId)}`);
    return response.data.data;
  },

  /**
   * Get available room types for a hotel.
   */
  async getRoomTypes(hotelId: string): Promise<RoomType[]> {
    const cleanId = hotelId ? hotelId.trim().replace(/\s+/g, '-').replace(/_+/g, '-') : '';
    const response = await apiClient.get<ApiResponse<RoomType[]>>(`/v1/hotels/${encodeURIComponent(cleanId)}/rooms`);
    return response.data.data || [];
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
    const response = await apiClient.get<ApiResponse<{
      content: import('../types/hotel').HotelBooking[];
      totalElements: number;
      totalPages: number;
    }>>('/v1/hotels/bookings/my', { params: { page, size } });
    return response.data.data || { content: [], totalElements: 0, totalPages: 0 };
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
