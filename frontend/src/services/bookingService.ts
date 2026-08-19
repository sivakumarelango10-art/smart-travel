import { apiClient } from './api';
import { ApiResponse, Booking, BookingCreateRequest, BookingPageResponse } from '../types/api';

export const bookingService = {
  async createBooking(request: BookingCreateRequest): Promise<ApiResponse<Booking>> {
    const res = await apiClient.post<ApiResponse<Booking>>('/v1/bookings', request);
    return res.data;
  },

  async getMyBookings(page = 0, size = 10, sort = 'createdAt,desc', status?: string): Promise<ApiResponse<BookingPageResponse>> {
    const params: Record<string, any> = { page, size, sort };
    if (status) params.status = status;
    const res = await apiClient.get<ApiResponse<BookingPageResponse>>('/v1/bookings', { params });
    return res.data;
  },

  async getBookingById(bookingId: string): Promise<ApiResponse<Booking>> {
    const res = await apiClient.get<ApiResponse<Booking>>(`/v1/bookings/${bookingId}`);
    return res.data;
  },

  async getBookingByReference(reference: string): Promise<ApiResponse<Booking>> {
    const res = await apiClient.get<ApiResponse<Booking>>(`/v1/bookings/reference/${reference}`);
    return res.data;
  },

  async cancelBooking(bookingId: string, reason: string): Promise<ApiResponse<Booking>> {
    const res = await apiClient.patch<ApiResponse<Booking>>(`/v1/bookings/${bookingId}/cancel`, {
      cancellationReason: reason,
    });
    return res.data;
  },

  async getDisruptions(bookingId: string): Promise<ApiResponse<any>> {
    const res = await apiClient.get<ApiResponse<any>>(`/v1/bookings/${bookingId}/disruptions`);
    return res.data;
  },
};
