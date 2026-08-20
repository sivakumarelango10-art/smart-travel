import { apiClient } from './api';
import { ApiResponse, Hotel, HotelSearchParams, RoomType } from '../types/api';

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
    const response = await apiClient.get<
      ApiResponse<{
        content: Hotel[];
        totalElements: number;
        totalPages: number;
        number: number;
      }>
    >('/v1/hotels', { params });
    const data = response.data.data;
    return {
      content: data?.content || [],
      totalElements: data?.totalElements || 0,
      totalPages: data?.totalPages || 0,
      page: data?.number || 0,
    };
  },

  /**
   * Get single hotel details.
   */
  async getHotel(hotelId: string): Promise<Hotel> {
    const response = await apiClient.get<ApiResponse<Hotel>>(`/v1/hotels/${hotelId}`);
    return response.data.data;
  },

  /**
   * Get available room types for a hotel.
   */
  async getRoomTypes(hotelId: string): Promise<RoomType[]> {
    const response = await apiClient.get<ApiResponse<RoomType[]>>(`/v1/hotels/${hotelId}/rooms`);
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
};
