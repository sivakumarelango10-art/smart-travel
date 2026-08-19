import { apiClient } from './api';
import { ApiResponse, Seat, CabinClass } from '../types/api';

export const seatService = {
  async getSeats(flightId: string, cabinClass?: CabinClass): Promise<ApiResponse<Seat[]>> {
    const res = await apiClient.get<ApiResponse<Seat[]>>(`/v1/flights/${flightId}/seats`, {
      params: cabinClass ? { cabinClass } : undefined,
    });
    return res.data;
  },

  async getSeatMap(flightId: string): Promise<ApiResponse<any>> {
    const res = await apiClient.get<ApiResponse<any>>(`/v1/flights/${flightId}/seat-map`);
    return res.data;
  },
};
