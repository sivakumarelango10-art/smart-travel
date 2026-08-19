import { apiClient } from './api';
import { ApiResponse, Flight, FlightSearchResponse } from '../types/api';
import {
  FlightCreateRequest,
  FlightUpdateRequest,
  FlightStatusUpdateRequest,
  FlightInventoryUpdateRequest,
  FlightScheduleChangeRequest,
  FlightCancelRequest,
  FlightGateChangeRequest,
  FlightTerminalChangeRequest,
  FlightAircraftChangeRequest,
  FlightOperationalStatusResponse,
  FlightDisruption,
  FlightImpactSummary,
  PageResponse,
} from '../types/admin';

/**
 * Admin Flight Service — integrates with /api/v1/admin/flights (ROLE_ADMIN required)
 * and the public /api/v1/flights for read operations.
 */
export const adminFlightService = {
  // ── Public read endpoints (used by admin for broader visibility) ─────────

  async searchFlights(params: Record<string, unknown>): Promise<ApiResponse<FlightSearchResponse>> {
    const res = await apiClient.get<ApiResponse<FlightSearchResponse>>('/v1/flights', { params });
    return res.data;
  },

  async getFlightById(flightId: string): Promise<ApiResponse<Flight>> {
    const res = await apiClient.get<ApiResponse<Flight>>(`/v1/flights/${flightId}`);
    return res.data;
  },

  // ── Admin CRUD ──────────────────────────────────────────────────────────

  async createFlight(request: FlightCreateRequest): Promise<ApiResponse<Flight>> {
    const res = await apiClient.post<ApiResponse<Flight>>('/v1/admin/flights', request);
    return res.data;
  },

  async updateFlight(id: string, request: FlightUpdateRequest): Promise<ApiResponse<Flight>> {
    const res = await apiClient.put<ApiResponse<Flight>>(`/v1/admin/flights/${id}`, request);
    return res.data;
  },

  async deleteFlight(id: string): Promise<ApiResponse<void>> {
    const res = await apiClient.delete<ApiResponse<void>>(`/v1/admin/flights/${id}`);
    return res.data;
  },

  async updateFlightStatus(id: string, request: FlightStatusUpdateRequest): Promise<ApiResponse<Flight>> {
    const res = await apiClient.patch<ApiResponse<Flight>>(`/v1/admin/flights/${id}/status`, request);
    return res.data;
  },

  async updateFlightInventory(id: string, request: FlightInventoryUpdateRequest): Promise<ApiResponse<Flight>> {
    const res = await apiClient.put<ApiResponse<Flight>>(`/v1/admin/flights/${id}/inventory`, request);
    return res.data;
  },

  // ── Disruption / Operational controls ──────────────────────────────────

  async rescheduleFlight(id: string, request: FlightScheduleChangeRequest): Promise<ApiResponse<FlightOperationalStatusResponse>> {
    const res = await apiClient.patch<ApiResponse<FlightOperationalStatusResponse>>(`/v1/admin/flights/${id}/schedule`, request);
    return res.data;
  },

  async cancelFlight(id: string, request: FlightCancelRequest): Promise<ApiResponse<FlightOperationalStatusResponse>> {
    const res = await apiClient.patch<ApiResponse<FlightOperationalStatusResponse>>(`/v1/admin/flights/${id}/cancel`, request);
    return res.data;
  },

  async updateGate(id: string, request: FlightGateChangeRequest): Promise<ApiResponse<FlightOperationalStatusResponse>> {
    const res = await apiClient.patch<ApiResponse<FlightOperationalStatusResponse>>(`/v1/admin/flights/${id}/gate`, request);
    return res.data;
  },

  async updateTerminal(id: string, request: FlightTerminalChangeRequest): Promise<ApiResponse<FlightOperationalStatusResponse>> {
    const res = await apiClient.patch<ApiResponse<FlightOperationalStatusResponse>>(`/v1/admin/flights/${id}/terminal`, request);
    return res.data;
  },

  async changeAircraft(id: string, request: FlightAircraftChangeRequest): Promise<ApiResponse<FlightOperationalStatusResponse>> {
    const res = await apiClient.patch<ApiResponse<FlightOperationalStatusResponse>>(`/v1/admin/flights/${id}/aircraft`, request);
    return res.data;
  },

  async getFlightDisruptions(id: string, page = 0, size = 20): Promise<ApiResponse<PageResponse<FlightDisruption>>> {
    const res = await apiClient.get<ApiResponse<PageResponse<FlightDisruption>>>(`/v1/admin/flights/${id}/disruptions`, {
      params: { page, size, sort: 'createdAt,desc' },
    });
    return res.data;
  },

  async getDisruptionImpact(id: string): Promise<ApiResponse<FlightImpactSummary>> {
    const res = await apiClient.get<ApiResponse<FlightImpactSummary>>(`/v1/admin/flights/${id}/impact`);
    return res.data;
  },

  async resolveDisruption(disruptionId: string): Promise<ApiResponse<FlightDisruption>> {
    const res = await apiClient.post<ApiResponse<FlightDisruption>>(`/v1/admin/flights/disruptions/${disruptionId}/resolve`);
    return res.data;
  },
};
