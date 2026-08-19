import { apiClient } from './api';
import { ApiResponse, BoardingPass, CheckInDetails } from '../types/api';

export const checkInService = {
  async checkIn(bookingId: string, seatSelections: any[] = []): Promise<ApiResponse<CheckInDetails>> {
    const res = await apiClient.post<ApiResponse<CheckInDetails>>(`/v1/bookings/${bookingId}/check-in`, {
      seatSelections,
    });
    return res.data;
  },

  async getCheckInDetails(bookingId: string): Promise<ApiResponse<CheckInDetails>> {
    const res = await apiClient.get<ApiResponse<CheckInDetails>>(`/v1/bookings/${bookingId}/check-in`);
    return res.data;
  },

  async getBoardingPass(bookingId: string): Promise<ApiResponse<BoardingPass>> {
    const res = await apiClient.get<ApiResponse<BoardingPass>>(`/v1/bookings/${bookingId}/boarding-pass`);
    return res.data;
  },

  async downloadBoardingPassPdf(bookingId: string): Promise<Blob> {
    const res = await apiClient.get(`/v1/bookings/${bookingId}/boarding-pass/pdf`, {
      responseType: 'blob',
    });
    return res.data;
  },
};
