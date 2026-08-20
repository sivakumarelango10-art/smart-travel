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
  AdminDashboardData,
} from '../types/analytics';
import { ApiResponse } from '../types/api';

// Fast client-side cache (15-second TTL)
const cache = new Map<string, { timestamp: number; data: any }>();
const CACHE_TTL_MS = 15000;

function getCached<T>(key: string): T | null {
  const entry = cache.get(key);
  if (entry && Date.now() - entry.timestamp < CACHE_TTL_MS) {
    return entry.data as T;
  }
  return null;
}

function setCache(key: string, data: any) {
  cache.set(key, { timestamp: Date.now(), data });
}

/**
 * Admin Analytics API service client.
 * Calls /api/v1/admin/analytics endpoints protected by ROLE_ADMIN with sub-100ms caching.
 */
export const analyticsService = {
  /**
   * Unified single-request Dashboard (overview, revenue, bookings, flights, seats, payments, customers)
   */
  async getDashboard(period: AnalyticsPeriod = 'last30days', from?: string, to?: string, forceFresh = false): Promise<AdminDashboardData> {
    const cacheKey = `dashboard_${period}_${from || ''}_${to || ''}`;
    if (!forceFresh) {
      const cached = getCached<AdminDashboardData>(cacheKey);
      if (cached) return cached;
    }

    const params = new URLSearchParams({ period });
    if (period === 'custom' && from && to) {
      params.append('from', from);
      params.append('to', to);
    }

    try {
      const res = await apiClient.get<ApiResponse<AdminDashboardData>>(`/admin/analytics/dashboard?${params.toString()}`);
      if (res.data && res.data.data) {
        setCache(cacheKey, res.data.data);
        return res.data.data;
      }
    } catch {
      // Graceful fallback to legacy multi-endpoint if single endpoint is deploying
    }

    // Parallel fallback
    const [overview, revenue, bookings, flights, seats, payments, customers] = await Promise.all([
      analyticsService.getOverview(),
      analyticsService.getRevenue(period, from, to),
      analyticsService.getBookings(period, from, to),
      analyticsService.getFlights(period, from, to),
      analyticsService.getSeats(),
      analyticsService.getPayments(period, from, to),
      analyticsService.getCustomers(period, from, to),
    ]);

    const result: AdminDashboardData = {
      overview,
      revenue,
      bookings,
      flights,
      seats,
      payments,
      customers,
    };
    setCache(cacheKey, result);
    return result;
  },

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
