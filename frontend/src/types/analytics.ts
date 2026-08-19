/**
 * TypeScript definitions for Phase 11 Admin Analytics.
 */

export type AnalyticsPeriod = 'today' | 'yesterday' | 'last7days' | 'last30days' | 'thisMonth' | 'lastMonth' | 'custom';

export interface TrendDataPoint {
  date: string;
  bookings?: number;
  confirmed?: number;
  cancelled?: number;
  expired?: number;
  pending?: number;
  grossRevenue?: number;
  refunds?: number;
  netRevenue?: number;
  newCustomers?: number;
  successfulPayments?: number;
  failedPayments?: number;
}

export interface OverviewAnalytics {
  totalBookings: number;
  confirmedBookings: number;
  pendingBookings: number;
  cancelledBookings: number;
  expiredBookings: number;
  totalGrossRevenue: number;
  totalRefundedAmount: number;
  totalNetRevenue: number;
  totalFlights: number;
  activeFlights: number;
  scheduledFlights: number;
  delayedFlights: number;
  cancelledFlights: number;
  departedFlights: number;
  totalSeats: number;
  availableSeats: number;
  bookedSeats: number;
  heldSeats: number;
  ticketsIssued: number;
  checkInsCompleted: number;
  totalCustomers: number;
  activeCustomers: number;
  successfulPayments: number;
  failedPayments: number;
  paymentSuccessRate: number;
  generatedAt: string;
}

export interface RevenueAnalytics {
  grossRevenue: number;
  refundedAmount: number;
  netRevenue: number;
  successfulPaymentCount: number;
  averageOrderValue: number;
  revenueToday: number;
  revenueLast7Days: number;
  revenueLast30Days: number;
  revenueThisMonth: number;
  revenuePreviousMonth: number;
  trend: TrendDataPoint[];
  period: string;
  from?: string;
  to?: string;
  generatedAt: string;
}

export interface BookingAnalytics {
  totalBookings: number;
  confirmedBookings: number;
  pendingBookings: number;
  cancelledBookings: number;
  expiredBookings: number;
  confirmationRate: number;
  cancellationRate: number;
  expirationRate: number;
  averageBookingValue: number;
  trend: TrendDataPoint[];
  period: string;
  from?: string;
  to?: string;
  generatedAt: string;
}

export interface FlightPerformance {
  flightId: string;
  flightNumber: string;
  airline: string;
  origin?: string;
  destination?: string;
  departureTime?: string;
  bookingCount: number;
  revenue: number;
  totalSeats: number;
  bookedSeats: number;
  occupancyPercentage?: number;
}

export interface FlightAnalytics {
  totalFlights: number;
  activeFlights: number;
  scheduledFlights: number;
  boardingFlights: number;
  delayedFlights: number;
  cancelledFlights: number;
  departedFlights: number;
  arrivedFlights: number;
  divertedFlights: number;
  flightsDepartingToday: number;
  flightsWithLowInventory: number;
  averageOccupancyPercentage: number;
  statusDistribution: Record<string, number>;
  topByRevenue: FlightPerformance[];
  topByBookings: FlightPerformance[];
  topByOccupancy: FlightPerformance[];
  leastUtilized: FlightPerformance[];
  period: string;
  from?: string;
  to?: string;
  generatedAt: string;
}

export interface CabinUtilization {
  cabinClass: string;
  totalSeats: number;
  availableSeats: number;
  bookedSeats: number;
  heldSeats: number;
  occupancyPercentage: number;
}

export interface SeatAnalytics {
  totalSeats: number;
  availableSeats: number;
  bookedSeats: number;
  heldSeats: number;
  overallOccupancyPercentage: number;
  cabinUtilization: CabinUtilization[];
  generatedAt: string;
}

export interface PaymentAnalytics {
  totalPayments: number;
  successfulPayments: number;
  failedPayments: number;
  pendingPayments: number;
  cancelledPayments: number;
  expiredPayments: number;
  totalSuccessfulAmount: number;
  totalRefundedAmount: number;
  paymentSuccessRate: number;
  trend: TrendDataPoint[];
  period: string;
  from?: string;
  to?: string;
  generatedAt: string;
}

export interface CustomerAnalytics {
  totalCustomers: number;
  activeCustomers: number;
  customersWithBookings: number;
  repeatCustomers: number;
  averageBookingsPerCustomer: number;
  newCustomersInPeriod: number;
  trend: TrendDataPoint[];
  period: string;
  from?: string;
  to?: string;
  generatedAt: string;
}
