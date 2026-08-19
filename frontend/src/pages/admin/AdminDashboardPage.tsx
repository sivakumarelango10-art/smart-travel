import React, { useEffect, useState, useCallback } from 'react';
import {
  Plane,
  Users,
  Layers,
  ArrowUpRight,
  Compass,
  DollarSign,
  BarChart3,
  AlertCircle,
} from 'lucide-react';
import { analyticsService } from '../../services/analyticsService';
import {
  OverviewAnalytics,
  RevenueAnalytics,
  BookingAnalytics,
  FlightAnalytics,
  SeatAnalytics,
  PaymentAnalytics,
  CustomerAnalytics,
  AnalyticsPeriod,
  FlightPerformance,
} from '../../types/analytics';
import { KpiCard } from '../../components/admin/KpiCard';
import { MiniBarChart } from '../../components/admin/MiniBarChart';
import { DateRangeSelector } from '../../components/admin/DateRangeSelector';
import { AlertsPanel } from '../../components/admin/AlertsPanel';

export const AdminDashboardPage: React.FC = () => {
  const [period, setPeriod] = useState<AnalyticsPeriod>('last30days');
  const [customFrom, setCustomFrom] = useState<string | undefined>();
  const [customTo, setCustomTo] = useState<string | undefined>();

  const [overview, setOverview] = useState<OverviewAnalytics | null>(null);
  const [revenue, setRevenue] = useState<RevenueAnalytics | null>(null);
  const [bookings, setBookings] = useState<BookingAnalytics | null>(null);
  const [flights, setFlights] = useState<FlightAnalytics | null>(null);
  const [seats, setSeats] = useState<SeatAnalytics | null>(null);
  const [payments, setPayments] = useState<PaymentAnalytics | null>(null);
  const [customers, setCustomers] = useState<CustomerAnalytics | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const [flightRankMode, setFlightRankMode] = useState<'revenue' | 'bookings' | 'occupancy'>('revenue');

  const fetchDashboardData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [
        overviewData,
        revenueData,
        bookingsData,
        flightsData,
        seatsData,
        paymentsData,
        customersData,
      ] = await Promise.all([
        analyticsService.getOverview(),
        analyticsService.getRevenue(period, customFrom, customTo),
        analyticsService.getBookings(period, customFrom, customTo),
        analyticsService.getFlights(period, customFrom, customTo),
        analyticsService.getSeats(),
        analyticsService.getPayments(period, customFrom, customTo),
        analyticsService.getCustomers(period, customFrom, customTo),
      ]);

      setOverview(overviewData);
      setRevenue(revenueData);
      setBookings(bookingsData);
      setFlights(flightsData);
      setSeats(seatsData);
      setPayments(paymentsData);
      setCustomers(customersData);
      setLastUpdated(new Date());
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to load analytics dashboard data';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [period, customFrom, customTo]);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  const handlePeriodChange = (newPeriod: AnalyticsPeriod, from?: string, to?: string) => {
    setPeriod(newPeriod);
    setCustomFrom(from);
    setCustomTo(to);
  };

  const formatCurrency = (val?: number) => {
    if (val === undefined || val === null) return '₹0';
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(val);
  };

  const getRankedFlights = (): FlightPerformance[] => {
    if (!flights) return [];
    switch (flightRankMode) {
      case 'revenue':
        return flights.topByRevenue || [];
      case 'bookings':
        return flights.topByBookings || [];
      case 'occupancy':
        return flights.topByOccupancy || [];
      default:
        return [];
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2.5">
            <BarChart3 className="w-7 h-7 text-primary-600" />
            Executive Operations Dashboard
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Real-time platform revenue, booking lifecycle, cabin inventory, and operational metrics.
          </p>
        </div>

        <DateRangeSelector
          period={period}
          onPeriodChange={handlePeriodChange}
          onRefresh={fetchDashboardData}
          loading={loading}
          lastUpdated={lastUpdated}
        />
      </div>

      {/* Error state */}
      {error && (
        <div className="p-4 bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/40 rounded-xl text-rose-800 dark:text-rose-300 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <AlertCircle className="w-5 h-5 text-rose-500 shrink-0" />
            <div>
              <p className="text-sm font-semibold">Failed to fetch analytics</p>
              <p className="text-xs text-rose-700 dark:text-rose-400">{error}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={fetchDashboardData}
            className="px-3 py-1.5 bg-rose-600 text-white text-xs font-semibold rounded-lg hover:bg-rose-700 transition-colors"
          >
            Retry
          </button>
        </div>
      )}

      {/* Operational Alerts Banner */}
      <AlertsPanel overview={overview} flights={flights} loading={loading} />

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <KpiCard
          title="Gross Revenue"
          value={formatCurrency(revenue?.grossRevenue ?? overview?.totalGrossRevenue)}
          subtitle={period === 'last30days' ? 'Last 30 days total' : `Selected ${period}`}
          icon={DollarSign}
          iconColor="text-emerald-600"
          iconBg="bg-emerald-50 dark:bg-emerald-950/40"
          trend={{
            value: formatCurrency(revenue?.netRevenue),
            isPositive: true,
            label: 'Net revenue',
          }}
          loading={loading}
        />

        <KpiCard
          title="Total Bookings"
          value={(bookings?.totalBookings ?? overview?.totalBookings ?? 0).toLocaleString()}
          subtitle={`${bookings?.confirmedBookings ?? overview?.confirmedBookings ?? 0} confirmed`}
          icon={Compass}
          iconColor="text-primary-600"
          iconBg="bg-primary-50 dark:bg-primary-950/40"
          trend={{
            value: `${bookings?.confirmationRate ?? 0}%`,
            isPositive: (bookings?.confirmationRate ?? 0) >= 50,
            label: 'Confirmation rate',
          }}
          loading={loading}
        />

        <KpiCard
          title="Active Flights"
          value={(overview?.activeFlights ?? flights?.activeFlights ?? 0).toLocaleString()}
          subtitle={`${flights?.flightsDepartingToday ?? 0} departing today`}
          icon={Plane}
          iconColor="text-sky-600"
          iconBg="bg-sky-50 dark:bg-sky-950/40"
          trend={{
            value: `${flights?.averageOccupancyPercentage ?? 0}%`,
            isPositive: (flights?.averageOccupancyPercentage ?? 0) >= 60,
            label: 'Avg occupancy',
          }}
          loading={loading}
        />

        <KpiCard
          title="Total Customers"
          value={(customers?.totalCustomers ?? overview?.totalCustomers ?? 0).toLocaleString()}
          subtitle={`${customers?.activeCustomers ?? overview?.activeCustomers ?? 0} active accounts`}
          icon={Users}
          iconColor="text-indigo-600"
          iconBg="bg-indigo-50 dark:bg-indigo-950/40"
          trend={{
            value: `${customers?.newCustomersInPeriod ?? 0} new`,
            isPositive: true,
            label: 'In selected period',
          }}
          loading={loading}
        />
      </div>

      {/* Secondary KPI Row */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-3.5 shadow-sm">
          <p className="text-[11px] font-semibold uppercase text-slate-400">Total Seats</p>
          <p className="text-lg font-bold text-slate-900 dark:text-white mt-0.5">
            {seats?.totalSeats.toLocaleString() ?? '0'}
          </p>
          <p className="text-[11px] text-slate-500 mt-0.5">{seats?.availableSeats.toLocaleString()} available</p>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-3.5 shadow-sm">
          <p className="text-[11px] font-semibold uppercase text-slate-400">Seat Occupancy</p>
          <p className="text-lg font-bold text-emerald-600 dark:text-emerald-400 mt-0.5">
            {seats?.overallOccupancyPercentage ?? 0}%
          </p>
          <p className="text-[11px] text-slate-500 mt-0.5">{seats?.bookedSeats.toLocaleString()} booked</p>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-3.5 shadow-sm">
          <p className="text-[11px] font-semibold uppercase text-slate-400">Tickets Issued</p>
          <p className="text-lg font-bold text-slate-900 dark:text-white mt-0.5">
            {overview?.ticketsIssued.toLocaleString() ?? '0'}
          </p>
          <p className="text-[11px] text-slate-500 mt-0.5">Active e-tickets</p>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-3.5 shadow-sm">
          <p className="text-[11px] font-semibold uppercase text-slate-400">Check-Ins Done</p>
          <p className="text-lg font-bold text-slate-900 dark:text-white mt-0.5">
            {overview?.checkInsCompleted.toLocaleString() ?? '0'}
          </p>
          <p className="text-[11px] text-slate-500 mt-0.5">Boarding passes</p>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-3.5 shadow-sm">
          <p className="text-[11px] font-semibold uppercase text-slate-400">Payment Success</p>
          <p className="text-lg font-bold text-sky-600 dark:text-sky-400 mt-0.5">
            {payments?.paymentSuccessRate ?? overview?.paymentSuccessRate ?? 0}%
          </p>
          <p className="text-[11px] text-slate-500 mt-0.5">{payments?.successfulPayments ?? 0} verified</p>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-3.5 shadow-sm">
          <p className="text-[11px] font-semibold uppercase text-slate-400">Refunds Processed</p>
          <p className="text-lg font-bold text-rose-600 dark:text-rose-400 mt-0.5">
            {formatCurrency(revenue?.refundedAmount ?? overview?.totalRefundedAmount)}
          </p>
          <p className="text-[11px] text-slate-500 mt-0.5">Disruption refunds</p>
        </div>
      </div>

      {/* Main Charts & Analytics Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Revenue Trend Chart */}
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
                <DollarSign className="w-4 h-4 text-emerald-500" />
                Gross Revenue Trend
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Daily verified payment volume for {period}
              </p>
            </div>
            <span className="text-sm font-bold text-emerald-600 dark:text-emerald-400">
              {formatCurrency(revenue?.grossRevenue)}
            </span>
          </div>

          <MiniBarChart
            data={revenue?.trend || []}
            valueKey="grossRevenue"
            label="Gross Revenue"
            color="#10b981"
            formatValue={(v) => formatCurrency(v)}
            height={180}
          />

          {/* Revenue Period Breakdown comparison */}
          <div className="grid grid-cols-3 gap-2 mt-4 pt-4 border-t border-slate-100 dark:border-slate-700/60 text-center">
            <div className="p-2 rounded bg-slate-50 dark:bg-slate-900/40">
              <p className="text-[10px] text-slate-400 uppercase font-semibold">Today</p>
              <p className="text-xs font-bold text-slate-800 dark:text-slate-200 mt-0.5">
                {formatCurrency(revenue?.revenueToday)}
              </p>
            </div>
            <div className="p-2 rounded bg-slate-50 dark:bg-slate-900/40">
              <p className="text-[10px] text-slate-400 uppercase font-semibold">This Month</p>
              <p className="text-xs font-bold text-slate-800 dark:text-slate-200 mt-0.5">
                {formatCurrency(revenue?.revenueThisMonth)}
              </p>
            </div>
            <div className="p-2 rounded bg-slate-50 dark:bg-slate-900/40">
              <p className="text-[10px] text-slate-400 uppercase font-semibold">Prev Month</p>
              <p className="text-xs font-bold text-slate-800 dark:text-slate-200 mt-0.5">
                {formatCurrency(revenue?.revenuePreviousMonth)}
              </p>
            </div>
          </div>
        </div>

        {/* Booking Volume Trend Chart */}
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
                <Compass className="w-4 h-4 text-primary-500" />
                Booking Volume Trend
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Daily created bookings for {period}
              </p>
            </div>
            <span className="text-sm font-bold text-primary-600 dark:text-primary-400">
              {bookings?.totalBookings ?? 0} Bookings
            </span>
          </div>

          <MiniBarChart
            data={bookings?.trend || []}
            valueKey="bookings"
            label="Total Bookings"
            color="#2563eb"
            formatValue={(v) => `${v} bookings`}
            height={180}
          />

          {/* Booking Rate Metrics */}
          <div className="grid grid-cols-3 gap-2 mt-4 pt-4 border-t border-slate-100 dark:border-slate-700/60 text-center">
            <div className="p-2 rounded bg-slate-50 dark:bg-slate-900/40">
              <p className="text-[10px] text-slate-400 uppercase font-semibold">Confirmed</p>
              <p className="text-xs font-bold text-emerald-600 dark:text-emerald-400 mt-0.5">
                {bookings?.confirmationRate ?? 0}%
              </p>
            </div>
            <div className="p-2 rounded bg-slate-50 dark:bg-slate-900/40">
              <p className="text-[10px] text-slate-400 uppercase font-semibold">Cancelled</p>
              <p className="text-xs font-bold text-rose-600 dark:text-rose-400 mt-0.5">
                {bookings?.cancellationRate ?? 0}%
              </p>
            </div>
            <div className="p-2 rounded bg-slate-50 dark:bg-slate-900/40">
              <p className="text-[10px] text-slate-400 uppercase font-semibold">Avg Value</p>
              <p className="text-xs font-bold text-slate-800 dark:text-slate-200 mt-0.5">
                {formatCurrency(bookings?.averageBookingValue)}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Cabin Utilization & Flight Status Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Cabin Utilization */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
                <Layers className="w-4 h-4 text-indigo-500" />
                Cabin-Wise Seat Utilization
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Seat inventory isolation & occupancy across cabin classes
              </p>
            </div>
            <span className="text-xs font-semibold px-2.5 py-1 rounded bg-indigo-50 dark:bg-indigo-950/40 text-indigo-700 dark:text-indigo-300">
              Overall: {seats?.overallOccupancyPercentage ?? 0}%
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {(seats?.cabinUtilization || []).map((cabin) => (
              <div
                key={cabin.cabinClass}
                className="p-4 rounded-xl border border-slate-100 dark:border-slate-700/80 bg-slate-50/50 dark:bg-slate-900/30"
              >
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider">
                    {cabin.cabinClass.replace('_', ' ')}
                  </span>
                  <span className="text-xs font-bold text-primary-600 dark:text-primary-400">
                    {cabin.occupancyPercentage}% Occupancy
                  </span>
                </div>

                {/* Progress bar */}
                <div className="w-full bg-slate-200 dark:bg-slate-700 rounded-full h-2 mb-3 overflow-hidden">
                  <div
                    className="bg-primary-600 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${Math.min(cabin.occupancyPercentage, 100)}%` }}
                  />
                </div>

                <div className="grid grid-cols-3 text-center text-xs">
                  <div>
                    <span className="text-slate-400 text-[10px] block">Total</span>
                    <span className="font-semibold text-slate-700 dark:text-slate-300">
                      {cabin.totalSeats}
                    </span>
                  </div>
                  <div>
                    <span className="text-emerald-500 text-[10px] block">Booked</span>
                    <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                      {cabin.bookedSeats}
                    </span>
                  </div>
                  <div>
                    <span className="text-slate-400 text-[10px] block">Available</span>
                    <span className="font-semibold text-slate-700 dark:text-slate-300">
                      {cabin.availableSeats}
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Flight Status Breakdown */}
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
              <Plane className="w-4 h-4 text-sky-500" />
              Flight Operational Status
            </h2>
          </div>

          <div className="space-y-3">
            {[
              { label: 'Scheduled / On Time', count: flights?.scheduledFlights ?? 0, color: 'bg-emerald-500' },
              { label: 'Boarding', count: flights?.boardingFlights ?? 0, color: 'bg-sky-500' },
              { label: 'Delayed', count: flights?.delayedFlights ?? 0, color: 'bg-amber-500' },
              { label: 'Departed / Arrived', count: (flights?.departedFlights ?? 0) + (flights?.arrivedFlights ?? 0), color: 'bg-indigo-500' },
              { label: 'Cancelled', count: flights?.cancelledFlights ?? 0, color: 'bg-rose-500' },
            ].map((st) => (
              <div key={st.label} className="flex items-center justify-between text-xs py-1">
                <div className="flex items-center gap-2">
                  <div className={`w-2.5 h-2.5 rounded-full ${st.color}`} />
                  <span className="text-slate-600 dark:text-slate-300 font-medium">{st.label}</span>
                </div>
                <span className="font-bold text-slate-900 dark:text-white">{st.count}</span>
              </div>
            ))}
          </div>

          <div className="mt-5 pt-4 border-t border-slate-100 dark:border-slate-700/60 space-y-2">
            <div className="flex items-center justify-between text-xs">
              <span className="text-slate-500">Departing Today:</span>
              <span className="font-bold text-slate-900 dark:text-white">
                {flights?.flightsDepartingToday ?? 0}
              </span>
            </div>
            <div className="flex items-center justify-between text-xs">
              <span className="text-slate-500">Low Inventory (&lt;10% seats):</span>
              <span className="font-bold text-amber-600 dark:text-amber-400">
                {flights?.flightsWithLowInventory ?? 0}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Top Flights Table */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
          <div>
            <h2 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
              <ArrowUpRight className="w-4 h-4 text-primary-500" />
              Flight Performance Leaderboard
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Ranked flight catalog metrics (no passenger PII exposed)
            </p>
          </div>

          <div className="flex items-center gap-1 bg-slate-100 dark:bg-slate-700/60 p-1 rounded-lg self-start sm:self-auto">
            <button
              type="button"
              onClick={() => setFlightRankMode('revenue')}
              className={`px-3 py-1 text-xs font-semibold rounded-md transition-colors ${
                flightRankMode === 'revenue'
                  ? 'bg-white dark:bg-slate-800 text-primary-600 dark:text-primary-400 shadow-sm'
                  : 'text-slate-600 dark:text-slate-300 hover:text-slate-900'
              }`}
            >
              By Revenue
            </button>
            <button
              type="button"
              onClick={() => setFlightRankMode('bookings')}
              className={`px-3 py-1 text-xs font-semibold rounded-md transition-colors ${
                flightRankMode === 'bookings'
                  ? 'bg-white dark:bg-slate-800 text-primary-600 dark:text-primary-400 shadow-sm'
                  : 'text-slate-600 dark:text-slate-300 hover:text-slate-900'
              }`}
            >
              By Bookings
            </button>
            <button
              type="button"
              onClick={() => setFlightRankMode('occupancy')}
              className={`px-3 py-1 text-xs font-semibold rounded-md transition-colors ${
                flightRankMode === 'occupancy'
                  ? 'bg-white dark:bg-slate-800 text-primary-600 dark:text-primary-400 shadow-sm'
                  : 'text-slate-600 dark:text-slate-300 hover:text-slate-900'
              }`}
            >
              By Occupancy
            </button>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-slate-200 dark:border-slate-700 text-slate-400 font-semibold uppercase">
                <th className="pb-3 pr-4">Flight</th>
                <th className="pb-3 pr-4">Airline</th>
                <th className="pb-3 pr-4">Route</th>
                <th className="pb-3 pr-4 text-right">Bookings</th>
                <th className="pb-3 pr-4 text-right">Revenue</th>
                <th className="pb-3 text-right">Occupancy</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
              {getRankedFlights().length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-6 text-center text-slate-400">
                    No flight performance records found for this ranking
                  </td>
                </tr>
              ) : (
                getRankedFlights().map((f, idx) => (
                  <tr key={f.flightId || idx} className="hover:bg-slate-50 dark:hover:bg-slate-700/30">
                    <td className="py-3 pr-4 font-bold text-slate-900 dark:text-white">
                      {f.flightNumber}
                    </td>
                    <td className="py-3 pr-4 text-slate-600 dark:text-slate-300">{f.airline}</td>
                    <td className="py-3 pr-4 font-medium text-slate-700 dark:text-slate-300">
                      {f.origin && f.destination ? `${f.origin} → ${f.destination}` : '—'}
                    </td>
                    <td className="py-3 pr-4 text-right font-semibold text-slate-900 dark:text-white">
                      {f.bookingCount ?? 0}
                    </td>
                    <td className="py-3 pr-4 text-right font-bold text-emerald-600 dark:text-emerald-400">
                      {formatCurrency(f.revenue)}
                    </td>
                    <td className="py-3 text-right font-semibold text-slate-900 dark:text-white">
                      {f.occupancyPercentage ? `${f.occupancyPercentage}%` : '—'}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Customer & Audience Metrics */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2">
              <Users className="w-4 h-4 text-indigo-500" />
              Customer Retention & Engagement
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Aggregated platform traveler counts (zero PII exposure)
            </p>
          </div>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-center">
          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900/40 border border-slate-100 dark:border-slate-700/60">
            <p className="text-xs text-slate-400 uppercase font-semibold">Total Customers</p>
            <p className="text-2xl font-bold text-slate-900 dark:text-white mt-1">
              {customers?.totalCustomers.toLocaleString() ?? '0'}
            </p>
            <p className="text-xs text-slate-500 mt-1">{customers?.activeCustomers.toLocaleString()} active</p>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900/40 border border-slate-100 dark:border-slate-700/60">
            <p className="text-xs text-slate-400 uppercase font-semibold">Booked Travelers</p>
            <p className="text-2xl font-bold text-primary-600 dark:text-primary-400 mt-1">
              {customers?.customersWithBookings.toLocaleString() ?? '0'}
            </p>
            <p className="text-xs text-slate-500 mt-1">With &ge; 1 confirmed booking</p>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900/40 border border-slate-100 dark:border-slate-700/60">
            <p className="text-xs text-slate-400 uppercase font-semibold">Repeat Customers</p>
            <p className="text-2xl font-bold text-emerald-600 dark:text-emerald-400 mt-1">
              {customers?.repeatCustomers.toLocaleString() ?? '0'}
            </p>
            <p className="text-xs text-slate-500 mt-1">Multiple bookings</p>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900/40 border border-slate-100 dark:border-slate-700/60">
            <p className="text-xs text-slate-400 uppercase font-semibold">Avg Bookings/User</p>
            <p className="text-2xl font-bold text-indigo-600 dark:text-indigo-400 mt-1">
              {customers?.averageBookingsPerCustomer ?? 0}
            </p>
            <p className="text-xs text-slate-500 mt-1">Per active booker</p>
          </div>
        </div>
      </div>
    </div>
  );
};
