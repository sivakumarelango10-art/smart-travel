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

    // Parallel fallback with Promise.allSettled
    try {
      const [overviewRes, revenueRes, bookingsRes, flightsRes, seatsRes, paymentsRes, customersRes] = await Promise.allSettled([
        analyticsService.getOverview(),
        analyticsService.getRevenue(period, from, to),
        analyticsService.getBookings(period, from, to),
        analyticsService.getFlights(period, from, to),
        analyticsService.getSeats(),
        analyticsService.getPayments(period, from, to),
        analyticsService.getCustomers(period, from, to),
      ]);

      const overview = overviewRes.status === 'fulfilled' ? overviewRes.value : ({} as any);
      const revenue = revenueRes.status === 'fulfilled' ? revenueRes.value : ({} as any);
      const bookings = bookingsRes.status === 'fulfilled' ? bookingsRes.value : ({} as any);
      const flights = flightsRes.status === 'fulfilled' ? flightsRes.value : ({} as any);
      const seats = seatsRes.status === 'fulfilled' ? seatsRes.value : ({} as any);
      const payments = paymentsRes.status === 'fulfilled' ? paymentsRes.value : ({} as any);
      const customers = customersRes.status === 'fulfilled' ? customersRes.value : ({} as any);

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
    } catch {
      return {
        overview: {
          totalBookings: 0,
          confirmedBookings: 0,
          pendingBookings: 0,
          cancelledBookings: 0,
          expiredBookings: 0,
          totalGrossRevenue: 0,
          totalRefundedAmount: 0,
          totalNetRevenue: 0,
          totalFlights: 0,
          activeFlights: 0,
          scheduledFlights: 0,
          delayedFlights: 0,
          cancelledFlights: 0,
          departedFlights: 0,
          totalSeats: 0,
          availableSeats: 0,
          bookedSeats: 0,
          heldSeats: 0,
          ticketsIssued: 0,
          checkInsCompleted: 0,
          totalCustomers: 0,
          activeCustomers: 0,
          successfulPayments: 0,
          failedPayments: 0,
          paymentSuccessRate: 100,
          generatedAt: new Date().toISOString(),
        },
        revenue: {
          grossRevenue: 0,
          refundedAmount: 0,
          netRevenue: 0,
          successfulPaymentCount: 0,
          averageOrderValue: 0,
          revenueToday: 0,
          revenueLast7Days: 0,
          revenueLast30Days: 0,
          revenueThisMonth: 0,
          revenuePreviousMonth: 0,
          trend: [],
          period,
          generatedAt: new Date().toISOString(),
        },
        bookings: {
          totalBookings: 0,
          confirmedBookings: 0,
          pendingBookings: 0,
          cancelledBookings: 0,
          expiredBookings: 0,
          confirmationRate: 0,
          cancellationRate: 0,
          expirationRate: 0,
          averageBookingValue: 0,
          trend: [],
          period,
          generatedAt: new Date().toISOString(),
        },
        flights: {
          totalFlights: 0,
          activeFlights: 0,
          scheduledFlights: 0,
          boardingFlights: 0,
          delayedFlights: 0,
          cancelledFlights: 0,
          departedFlights: 0,
          arrivedFlights: 0,
          divertedFlights: 0,
          flightsDepartingToday: 0,
          flightsWithLowInventory: 0,
          averageOccupancyPercentage: 0,
          statusDistribution: {},
          topByRevenue: [],
          topByBookings: [],
          topByOccupancy: [],
          leastUtilized: [],
          period,
          generatedAt: new Date().toISOString(),
        },
        seats: {
          totalSeats: 0,
          availableSeats: 0,
          bookedSeats: 0,
          heldSeats: 0,
          overallOccupancyPercentage: 0,
          cabinUtilization: [],
          generatedAt: new Date().toISOString(),
        },
        payments: {
          totalPayments: 0,
          successfulPayments: 0,
          failedPayments: 0,
          pendingPayments: 0,
          cancelledPayments: 0,
          expiredPayments: 0,
          totalSuccessfulAmount: 0,
          totalRefundedAmount: 0,
          paymentSuccessRate: 100,
          trend: [],
          period,
          generatedAt: new Date().toISOString(),
        },
        customers: {
          totalCustomers: 0,
          activeCustomers: 0,
          customersWithBookings: 0,
          repeatCustomers: 0,
          averageBookingsPerCustomer: 0,
          newCustomersInPeriod: 0,
          trend: [],
          period,
          generatedAt: new Date().toISOString(),
        },
      };
    }
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
