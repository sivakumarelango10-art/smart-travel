import { apiClient } from './api';
import { ApiResponse, TrackedFlight } from '../types/api';

export const flightTrackingService = {
  /**
   * Track a flight to receive live status updates.
   */
  async trackFlight(flightId: string): Promise<TrackedFlight> {
    const response = await apiClient.post<ApiResponse<TrackedFlight>>(`/v1/flights/${flightId}/track`);
    return response.data.data;
  },

  /**
   * Stop tracking a flight.
   */
  async untrackFlight(flightId: string): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`/v1/flights/${flightId}/track`);
  },

  /**
   * Get all tracked flights for current user with live status.
   */
  async getTrackedFlights(): Promise<TrackedFlight[]> {
    const response = await apiClient.get<ApiResponse<TrackedFlight[]>>('/v1/flights/tracked');
    return response.data.data || [];
  },

  /**
   * Check if a flight is currently tracked by user.
   */
  async isTracking(flightId: string): Promise<boolean> {
    try {
      const response = await apiClient.get<ApiResponse<boolean>>(`/v1/flights/${flightId}/track/status`);
      return Boolean(response.data.data);
    } catch {
      return false;
    }
  },
};
