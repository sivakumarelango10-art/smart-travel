import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Plane, BookmarkCheck, RotateCcw, Ticket,
  Zap, AlertTriangle, TrendingUp, CheckCircle, Clock, XCircle,
  ChevronRight, RefreshCw
} from 'lucide-react';
import { adminFlightService } from '../../services/adminFlightService';
import { adminBookingService } from '../../services/adminBookingService';
import { adminRefundService } from '../../services/adminRefundService';
import { adminTicketService } from '../../services/adminTicketService';
import { healthService } from '../../services/healthService';
import { StatusBadge } from '../../components/admin/StatusBadge';
import { Booking } from '../../types/booking';
import { Flight } from '../../types/flight';
import { AdminRefund } from '../../types/admin';

interface DashboardMetrics {
  totalFlights: number;
  activeFlights: number;
  cancelledFlights: number;
  delayedFlights: number;
  totalBookings: number;
  confirmedBookings: number;
  pendingBookings: number;
  cancelledBookings: number;
  totalRefunds: number;
  pendingRefunds: number;
  completedRefunds: number;
  totalTickets: number;
  healthStatus: string;
  dbStatus: string;
}

interface RecentData {
  recentBookings: Booking[];
  recentRefunds: AdminRefund[];
  recentFlights: Flight[];
}

const MetricCard: React.FC<{
  label: string;
  value: number | string;
  icon: React.ReactNode;
  colorClass: string;
  link?: string;
  sublabel?: string;
}> = ({ label, value, icon, colorClass, link, sublabel }) => {
  const content = (
    <div className={`bg-slate-900 border border-slate-800 rounded-2xl p-5 hover:border-slate-700 transition-all duration-200 group ${link ? 'cursor-pointer' : ''}`}>
      <div className="flex items-start justify-between mb-3">
        <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${colorClass}`}>
          {icon}
        </div>
        {link && <ChevronRight className="w-4 h-4 text-slate-600 group-hover:text-slate-400 transition" />}
      </div>
      <p className="text-2xl font-bold text-white tabular-nums">{value}</p>
      <p className="text-xs text-slate-400 mt-1 font-medium">{label}</p>
      {sublabel && <p className="text-[10px] text-slate-500 mt-0.5">{sublabel}</p>}
    </div>
  );
  return link ? <Link to={link}>{content}</Link> : content;
};

const SkeletonCard = () => (
  <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 animate-pulse">
    <div className="w-10 h-10 bg-slate-800 rounded-xl mb-3" />
    <div className="h-7 w-16 bg-slate-800 rounded mb-2" />
    <div className="h-3 w-24 bg-slate-800 rounded" />
  </div>
);

export const AdminDashboardPage: React.FC = () => {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [recent, setRecent] = useState<RecentData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastRefresh, setLastRefresh] = useState(new Date());

  const fetchDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      // Batch 1: Parallel counts
      const [
        allFlights, delayedFlights, cancelledFlights,
        allBookings, confirmedBookings, pendingBookings, cancelledBookings,
        allRefunds, pendingRefunds, completedRefunds,
        allTickets, health
      ] = await Promise.all([
        adminFlightService.searchFlights({ page: 0, size: 1 }),
        adminFlightService.searchFlights({ page: 0, size: 1, status: 'DELAYED' }),
        adminFlightService.searchFlights({ page: 0, size: 1, status: 'CANCELLED' }),
        adminBookingService.getAllBookings(0, 1),
        adminBookingService.getAllBookings(0, 1, 'CONFIRMED'),
        adminBookingService.getAllBookings(0, 1, 'PENDING'),
        adminBookingService.getAllBookings(0, 1, 'CANCELLED'),
        adminRefundService.getAllRefunds(0, 1),
        adminRefundService.getAllRefunds(0, 1, 'PENDING'),
        adminRefundService.getAllRefunds(0, 1, 'COMPLETED'),
        adminTicketService.getAllTickets(0, 1),
        healthService.getHealth(),
      ]);

      const totalFlights = allFlights.data?.totalElements ?? 0;
      const active = totalFlights - (cancelledFlights.data?.totalElements ?? 0) - (delayedFlights.data?.totalElements ?? 0);

      setMetrics({
        totalFlights,
        activeFlights: Math.max(0, active),
        cancelledFlights: cancelledFlights.data?.totalElements ?? 0,
        delayedFlights: delayedFlights.data?.totalElements ?? 0,
        totalBookings: allBookings.data?.totalElements ?? 0,
        confirmedBookings: confirmedBookings.data?.totalElements ?? 0,
        pendingBookings: pendingBookings.data?.totalElements ?? 0,
        cancelledBookings: cancelledBookings.data?.totalElements ?? 0,
        totalRefunds: allRefunds.data?.totalElements ?? 0,
        pendingRefunds: pendingRefunds.data?.totalElements ?? 0,
        completedRefunds: completedRefunds.data?.totalElements ?? 0,
        totalTickets: allTickets.data?.totalElements ?? 0,
        healthStatus: health.data?.status ?? 'UNKNOWN',
        dbStatus: health.data?.database ?? 'UNKNOWN',
      });

      // Batch 2: Recent items
      const [recentBookingsRes, recentRefundsRes, recentFlightsRes] = await Promise.all([
        adminBookingService.getAllBookings(0, 5, undefined, 'createdAt,desc'),
        adminRefundService.getAllRefunds(0, 5),
        adminFlightService.searchFlights({ page: 0, size: 5 }),
      ]);

      setRecent({
        recentBookings: recentBookingsRes.data?.content ?? [],
        recentRefunds: recentRefundsRes.data?.content ?? [],
        recentFlights: recentFlightsRes.data?.content ?? [],
      });

      setLastRefresh(new Date());
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const healthColor = metrics?.healthStatus === 'UP' ? 'text-emerald-400' : 'text-rose-400';
  const dbColor = metrics?.dbStatus === 'CONNECTED' ? 'text-emerald-400' : 'text-rose-400';

  return (
    <div className="space-y-8 max-w-[1400px]">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Operations Dashboard</h1>
          <p className="text-sm text-slate-400 mt-0.5">
            Last updated: {lastRefresh.toLocaleTimeString()}
          </p>
        </div>
        <button
          onClick={fetchDashboard}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl transition disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          {error}
        </div>
      )}

      {/* System Health Banner */}
      {metrics && (
        <div className="flex items-center gap-4 p-4 rounded-2xl bg-slate-900 border border-slate-800">
          <div className={`w-3 h-3 rounded-full flex-shrink-0 ${metrics.healthStatus === 'UP' ? 'bg-emerald-400 shadow-lg shadow-emerald-400/40 animate-pulse' : 'bg-rose-400'}`} />
          <div className="flex items-center gap-6 flex-1 flex-wrap">
            <div>
              <p className="text-[11px] text-slate-500 font-medium uppercase tracking-wide">API Status</p>
              <p className={`text-sm font-bold ${healthColor}`}>{metrics.healthStatus}</p>
            </div>
            <div>
              <p className="text-[11px] text-slate-500 font-medium uppercase tracking-wide">MongoDB</p>
              <p className={`text-sm font-bold ${dbColor}`}>{metrics.dbStatus}</p>
            </div>
          </div>
          <Link to="/admin/system" className="text-xs text-sky-400 hover:text-sky-300 flex items-center gap-1 font-medium">
            Full Status <ChevronRight className="w-3 h-3" />
          </Link>
        </div>
      )}

      {/* Flight Metrics */}
      <section>
        <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-4 flex items-center gap-2">
          <Plane className="w-4 h-4" /> Flights
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {loading ? (
            Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)
          ) : (
            <>
              <MetricCard label="Total Flights" value={metrics?.totalFlights ?? 0} icon={<Plane className="w-5 h-5 text-sky-400" />} colorClass="bg-sky-500/10 border border-sky-500/20" link="/admin/flights" />
              <MetricCard label="Active" value={metrics?.activeFlights ?? 0} icon={<CheckCircle className="w-5 h-5 text-emerald-400" />} colorClass="bg-emerald-500/10 border border-emerald-500/20" link="/admin/flights" />
              <MetricCard label="Delayed" value={metrics?.delayedFlights ?? 0} icon={<Clock className="w-5 h-5 text-amber-400" />} colorClass="bg-amber-500/10 border border-amber-500/20" link="/admin/flights" />
              <MetricCard label="Cancelled" value={metrics?.cancelledFlights ?? 0} icon={<XCircle className="w-5 h-5 text-rose-400" />} colorClass="bg-rose-500/10 border border-rose-500/20" link="/admin/flights" />
            </>
          )}
        </div>
      </section>

      {/* Booking Metrics */}
      <section>
        <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-4 flex items-center gap-2">
          <BookmarkCheck className="w-4 h-4" /> Bookings
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {loading ? (
            Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)
          ) : (
            <>
              <MetricCard label="Total Bookings" value={metrics?.totalBookings ?? 0} icon={<BookmarkCheck className="w-5 h-5 text-indigo-400" />} colorClass="bg-indigo-500/10 border border-indigo-500/20" link="/admin/bookings" />
              <MetricCard label="Confirmed" value={metrics?.confirmedBookings ?? 0} icon={<CheckCircle className="w-5 h-5 text-emerald-400" />} colorClass="bg-emerald-500/10 border border-emerald-500/20" link="/admin/bookings" />
              <MetricCard label="Pending" value={metrics?.pendingBookings ?? 0} icon={<Clock className="w-5 h-5 text-amber-400" />} colorClass="bg-amber-500/10 border border-amber-500/20" link="/admin/bookings" />
              <MetricCard label="Cancelled" value={metrics?.cancelledBookings ?? 0} icon={<XCircle className="w-5 h-5 text-rose-400" />} colorClass="bg-rose-500/10 border border-rose-500/20" link="/admin/bookings" />
            </>
          )}
        </div>
      </section>

      {/* Refunds + Tickets */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Refunds */}
        <section>
          <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-4 flex items-center gap-2">
            <RotateCcw className="w-4 h-4" /> Refunds
          </h2>
          <div className="grid grid-cols-3 gap-3">
            {loading ? (
              Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)
            ) : (
              <>
                <MetricCard label="Total" value={metrics?.totalRefunds ?? 0} icon={<RotateCcw className="w-4 h-4 text-violet-400" />} colorClass="bg-violet-500/10 border border-violet-500/20" link="/admin/refunds" />
                <MetricCard label="Pending" value={metrics?.pendingRefunds ?? 0} icon={<Clock className="w-4 h-4 text-amber-400" />} colorClass="bg-amber-500/10 border border-amber-500/20" link="/admin/refunds" />
                <MetricCard label="Completed" value={metrics?.completedRefunds ?? 0} icon={<TrendingUp className="w-4 h-4 text-emerald-400" />} colorClass="bg-emerald-500/10 border border-emerald-500/20" link="/admin/refunds" />
              </>
            )}
          </div>
        </section>

        {/* Tickets */}
        <section>
          <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-4 flex items-center gap-2">
            <Ticket className="w-4 h-4" /> Tickets
          </h2>
          <div className="grid grid-cols-1 gap-3">
            {loading ? (
              <SkeletonCard />
            ) : (
              <MetricCard label="Total Tickets Issued" value={metrics?.totalTickets ?? 0} icon={<Ticket className="w-5 h-5 text-teal-400" />} colorClass="bg-teal-500/10 border border-teal-500/20" link="/admin/tickets" sublabel="All platform tickets" />
            )}
          </div>
        </section>
      </div>

      {/* Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Bookings */}
        <section className="bg-slate-900 border border-slate-800 rounded-2xl">
          <div className="flex items-center justify-between px-5 py-4 border-b border-slate-800">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <BookmarkCheck className="w-4 h-4 text-indigo-400" />
              Recent Bookings
            </h3>
            <Link to="/admin/bookings" className="text-xs text-sky-400 hover:text-sky-300 font-medium">View all</Link>
          </div>
          <div className="divide-y divide-slate-800/60">
            {loading ? (
              Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="px-5 py-3 flex items-center gap-3 animate-pulse">
                  <div className="w-10 h-3 bg-slate-800 rounded" />
                  <div className="flex-1 h-3 bg-slate-800 rounded" />
                  <div className="w-16 h-5 bg-slate-800 rounded-full" />
                </div>
              ))
            ) : recent?.recentBookings.length === 0 ? (
              <p className="px-5 py-8 text-center text-slate-500 text-sm">No bookings yet</p>
            ) : (
              recent?.recentBookings.map(b => (
                <Link key={b.id} to={`/admin/bookings/${b.id}`} className="flex items-center gap-3 px-5 py-3 hover:bg-slate-800/40 transition">
                  <div>
                    <p className="text-xs font-mono font-bold text-white">{b.bookingReference}</p>
                    <p className="text-[11px] text-slate-400">{b.userEmail}</p>
                  </div>
                  <div className="flex-1 text-right">
                    <p className="text-xs text-slate-300">{b.flightNumber}</p>
                    <p className="text-[11px] text-slate-500">{new Date(b.createdAt).toLocaleDateString()}</p>
                  </div>
                  <StatusBadge status={b.status} type="booking" size="xs" />
                </Link>
              ))
            )}
          </div>
        </section>

        {/* Recent Refunds */}
        <section className="bg-slate-900 border border-slate-800 rounded-2xl">
          <div className="flex items-center justify-between px-5 py-4 border-b border-slate-800">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <RotateCcw className="w-4 h-4 text-violet-400" />
              Recent Refunds
            </h3>
            <Link to="/admin/refunds" className="text-xs text-sky-400 hover:text-sky-300 font-medium">View all</Link>
          </div>
          <div className="divide-y divide-slate-800/60">
            {loading ? (
              Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="px-5 py-3 flex items-center gap-3 animate-pulse">
                  <div className="w-24 h-3 bg-slate-800 rounded" />
                  <div className="flex-1 h-3 bg-slate-800 rounded" />
                  <div className="w-16 h-5 bg-slate-800 rounded-full" />
                </div>
              ))
            ) : recent?.recentRefunds.length === 0 ? (
              <p className="px-5 py-8 text-center text-slate-500 text-sm">No refunds yet</p>
            ) : (
              recent?.recentRefunds.map(r => (
                <div key={r.id} className="flex items-center gap-3 px-5 py-3">
                  <div>
                    <p className="text-xs font-mono font-bold text-white">{r.refundNumber}</p>
                    <p className="text-[11px] text-slate-400">{r.bookingReference}</p>
                  </div>
                  <div className="flex-1 text-right">
                    <p className="text-xs font-semibold text-white">₹{Number(r.amount).toLocaleString('en-IN')}</p>
                    <p className="text-[11px] text-slate-500">{new Date(r.requestedAt).toLocaleDateString()}</p>
                  </div>
                  <StatusBadge status={r.status} type="refund" size="xs" />
                </div>
              ))
            )}
          </div>
        </section>
      </div>

      {/* Active Disruptions notice */}
      <section>
        <Link to="/admin/disruptions" className="flex items-center gap-3 p-4 rounded-2xl bg-slate-900 border border-amber-500/20 hover:border-amber-500/40 transition group">
          <div className="w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center">
            <Zap className="w-5 h-5 text-amber-400" />
          </div>
          <div className="flex-1">
            <p className="text-sm font-semibold text-white">Disruption Management</p>
            <p className="text-xs text-slate-400">View and manage active flight disruptions, delays, and cancellations</p>
          </div>
          <ChevronRight className="w-5 h-5 text-slate-600 group-hover:text-amber-400 transition" />
        </Link>
      </section>
    </div>
  );
};
