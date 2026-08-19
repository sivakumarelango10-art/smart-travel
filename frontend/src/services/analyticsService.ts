import { apiClient } from './api';
import {
  AnalyticsPeriod,
  OverviewAnalytics,
  RevenueAnalytics,
  BookingAnalytics,
  FlightAnalytics,
  SeatAnalytics,
  PaymentAnalytics,
  CustomerAnalytics,
} from '../types/analytics';
import { ApiResponse } from '../types/api';

/**
 * Admin Analytics API service client.
 * Calls /api/v1/admin/analytics endpoints protected by ROLE_ADMIN.
 */
export const analyticsService = {
  /**
   * Get platform overview KPIs
   */
  async getOverview(): Promise<OverviewAnalytics> {
    const res = await apiClient.get<ApiResponse<OverviewAnalytics>>('/admin/analytics/overview');
    return res.data.data;
  },

  /**
   * Get revenue analytics and daily trend
   */
  async getRevenue(period: AnalyticsPeriod = 'last30days', from?: string, to?: string): Promise<RevenueAnalytics> {
    const params = new URLSearchParams({ period });
    if (period === 'custom' && from && to) {
      params.append('from', from);
      params.append('to', to);
    }
    const res = await apiClient.get<ApiResponse<RevenueAnalytics>>(`/admin/analytics/revenue?${params.toString()}`);
    return res.data.data;
  },

  /**
   * Get booking analytics, confirmation/cancellation rates and trend
   */
  async getBookings(period: AnalyticsPeriod = 'last30days', from?: string, to?: string): Promise<BookingAnalytics> {
    const params = new URLSearchParams({ period });
    if (period === 'custom' && from && to) {
      params.append('from', from);
      params.append('to', to);
    }
    const res = await apiClient.get<ApiResponse<BookingAnalytics>>(`/admin/analytics/bookings?${params.toString()}`);
    return res.data.data;
  },

  /**
   * Get flight status distribution, top performers, and occupancy
   */
  async getFlights(period: AnalyticsPeriod = 'last30days', from?: string, to?: string): Promise<FlightAnalytics> {
    const params = new URLSearchParams({ period });
    if (period === 'custom' && from && to) {
      params.append('from', from);
      params.append('to', to);
    }
    const res = await apiClient.get<ApiResponse<FlightAnalytics>>(`/admin/analytics/flights?${params.toString()}`);
    return res.data.data;
  },

  /**
   * Get seat utilization by cabin class
   */
  async getSeats(): Promise<SeatAnalytics> {
    const res = await apiClient.get<ApiResponse<SeatAnalytics>>('/admin/analytics/seats');
    return res.data.data;
  },

  /**
   * Get payment metrics and success/failure trend
   */
  async getPayments(period: AnalyticsPeriod = 'last30days', from?: string, to?: string): Promise<PaymentAnalytics> {
    const params = new URLSearchParams({ period });
    if (period === 'custom' && from && to) {
      params.append('from', from);
      params.append('to', to);
    }
    const res = await apiClient.get<ApiResponse<PaymentAnalytics>>(`/admin/analytics/payments?${params.toString()}`);
    return res.data.data;
  },

  /**
   * Get customer counts and activity metrics (no PII)
   */
  async getCustomers(period: AnalyticsPeriod = 'last30days', from?: string, to?: string): Promise<CustomerAnalytics> {
    const params = new URLSearchParams({ period });
    if (period === 'custom' && from && to) {
      params.append('from', from);
      params.append('to', to);
    }
    const res = await apiClient.get<ApiResponse<CustomerAnalytics>>(`/admin/analytics/customers?${params.toString()}`);
    return res.data.data;
  },
};
