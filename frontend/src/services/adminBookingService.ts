import { apiClient } from './api';
import { ApiResponse } from '../types/api';
import { AdminBooking, BookingStatus, BookingCancelRequest, PageResponse } from '../types/admin';

/**
 * Admin Booking Service — integrates with /api/v1/admin/bookings (ROLE_ADMIN required)
 */
export const adminBookingService = {
  async getAllBookings(
    page = 0,
    size = 20,
    status?: BookingStatus,
    sort = 'createdAt,desc',
  ): Promise<ApiResponse<PageResponse<AdminBooking>>> {
    const params: Record<string, unknown> = { page, size, sort };
    if (status) params.status = status;
    const res = await apiClient.get<ApiResponse<PageResponse<AdminBooking>>>('/v1/admin/bookings', { params });
    return res.data;
  },

  async getBookingById(id: string): Promise<ApiResponse<AdminBooking>> {
    const res = await apiClient.get<ApiResponse<AdminBooking>>(`/v1/admin/bookings/${id}`);
    return res.data;
  },

  async getBookingByReference(reference: string): Promise<ApiResponse<AdminBooking>> {
    const res = await apiClient.get<ApiResponse<AdminBooking>>(`/v1/admin/bookings/reference/${reference}`);
    return res.data;
  },

  async cancelBooking(id: string, request?: BookingCancelRequest): Promise<ApiResponse<AdminBooking>> {
    const res = await apiClient.patch<ApiResponse<AdminBooking>>(`/v1/admin/bookings/${id}/cancel`, request ?? {});
    return res.data;
  },
};
