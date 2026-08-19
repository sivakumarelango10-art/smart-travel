import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import {
  BookmarkCheck, Search, Filter, RefreshCw, Eye,
  ChevronLeft, ChevronRight, AlertTriangle, XCircle
} from 'lucide-react';
import { adminBookingService } from '../../services/adminBookingService';
import { StatusBadge } from '../../components/admin/StatusBadge';
import { useAdminToast } from '../../components/admin/AdminToast';
import { Booking, BookingStatus } from '../../types/booking';

const BOOKING_STATUSES: BookingStatus[] = ['PENDING','CONFIRMED','CANCELLED','EXPIRED','CHECKED_IN','COMPLETED'];

export const AdminBookingsPage: React.FC = () => {
  const { showToast } = useAdminToast();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [pnrSearch, setPnrSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<BookingStatus | ''>('');
  const searchTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);

  const [cancelTarget, setCancelTarget] = useState<Booking | null>(null);
  const [cancelReason, setCancelReason] = useState('');
  const [cancelling, setCancelling] = useState(false);

  const fetchBookings = useCallback(async (p = 0) => {
    setLoading(true);
    setError(null);
    try {
      if (pnrSearch.trim().length >= 3) {
        // Exact PNR lookup
        try {
          const res = await adminBookingService.getBookingByReference(pnrSearch.trim());
          if (res.data) {
            setBookings([res.data]);
            setTotalPages(1);
            setTotalElements(1);
          }
        } catch {
          setBookings([]);
          setTotalPages(0);
          setTotalElements(0);
        }
      } else {
        const res = await adminBookingService.getAllBookings(p, 20, statusFilter || undefined);
        setBookings(res.data?.content ?? []);
        setTotalPages(res.data?.totalPages ?? 0);
        setTotalElements(res.data?.totalElements ?? 0);
      }
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Failed to load bookings');
    } finally {
      setLoading(false);
    }
  }, [statusFilter, pnrSearch]);

  useEffect(() => {
    setPage(0);
    if (searchTimeout.current) clearTimeout(searchTimeout.current);
    searchTimeout.current = setTimeout(() => fetchBookings(0), 350);
    return () => { if (searchTimeout.current) clearTimeout(searchTimeout.current); };
  }, [fetchBookings]);

  const handleCancel = async () => {
    if (!cancelTarget) return;
    setCancelling(true);
    try {
      await adminBookingService.cancelBooking(cancelTarget.id, { cancellationReason: cancelReason || 'Admin action' });
      showToast('success', 'Booking cancelled', `Booking ${cancelTarget.bookingReference} has been cancelled.`);
      setCancelTarget(null);
      setCancelReason('');
      fetchBookings(page);
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Cancel failed', err?.message ?? 'Could not cancel booking');
    } finally {
      setCancelling(false);
    }
  };

  return (
    <div className="space-y-6 max-w-[1400px]">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <BookmarkCheck className="w-6 h-6 text-indigo-400" /> Booking Management
          </h1>
          <p className="text-sm text-slate-400 mt-0.5">{totalElements.toLocaleString()} total bookings</p>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-[220px] max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search by PNR (e.g. ST8K4P2Q)..."
            value={pnrSearch}
            onChange={e => setPnrSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30 transition"
          />
        </div>
        <div className="relative">
          <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <select
            value={statusFilter}
            onChange={e => { setStatusFilter(e.target.value as BookingStatus | ''); setPnrSearch(''); }}
            className="pl-9 pr-8 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:border-sky-500 appearance-none cursor-pointer"
          >
            <option value="">All Statuses</option>
            {BOOKING_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <button onClick={() => fetchBookings(page)} className="p-2 bg-slate-800 border border-slate-700 rounded-xl text-slate-400 hover:text-white hover:bg-slate-700 transition">
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />{error}
        </div>
      )}

      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-950/60 border-b border-slate-800">
                <th className="text-left px-5 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">PNR / Reference</th>
                <th className="text-left px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Customer</th>
                <th className="text-left px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Flight</th>
                <th className="text-left px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Status</th>
                <th className="text-right px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Amount</th>
                <th className="text-left px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Created</th>
                <th className="text-center px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                Array.from({ length: 8 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    {Array.from({ length: 7 }).map((_, j) => (
                      <td key={j} className="px-5 py-4"><div className="h-4 bg-slate-800 rounded w-full" /></td>
                    ))}
                  </tr>
                ))
              ) : bookings.length === 0 ? (
                <tr><td colSpan={7} className="px-5 py-12 text-center">
                  <BookmarkCheck className="w-10 h-10 text-slate-700 mx-auto mb-3" />
                  <p className="text-slate-500 text-sm">No bookings found</p>
                </td></tr>
              ) : (
                bookings.map(b => (
                  <tr key={b.id} className="hover:bg-slate-800/30 transition">
                    <td className="px-5 py-4">
                      <p className="font-mono font-bold text-white text-sm">{b.bookingReference}</p>
                      <p className="text-[11px] text-slate-500 font-mono">{b.id?.slice(-8)}</p>
                    </td>
                    <td className="px-4 py-4">
                      <p className="text-sm text-white truncate max-w-[160px]">{b.userEmail}</p>
                      <p className="text-[11px] text-slate-500">{b.passengerCount} pax</p>
                    </td>
                    <td className="px-4 py-4">
                      <p className="text-sm font-mono text-white">{b.flightNumber}</p>
                      <p className="text-[11px] text-slate-400">{b.departureAirport?.code} → {b.arrivalAirport?.code}</p>
                    </td>
                    <td className="px-4 py-4"><StatusBadge status={b.status} type="booking" /></td>
                    <td className="px-4 py-4 text-right">
                      <p className="text-sm font-semibold text-white">₹{Number(b.totalAmount).toLocaleString('en-IN')}</p>
                      <p className="text-[11px] text-slate-500">{b.cabinClass}</p>
                    </td>
                    <td className="px-4 py-4">
                      <p className="text-xs text-slate-300">{new Date(b.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}</p>
                    </td>
                    <td className="px-4 py-4">
                      <div className="flex items-center justify-center gap-2">
                        <Link to={`/admin/bookings/${b.id}`} className="p-1.5 text-slate-400 hover:text-sky-400 hover:bg-sky-500/10 rounded-lg transition" title="View details">
                          <Eye className="w-4 h-4" />
                        </Link>
                        {(b.status === 'CONFIRMED' || b.status === 'PENDING') && (
                          <button
                            onClick={() => { setCancelTarget(b); setCancelReason(''); }}
                            className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition"
                            title="Cancel booking"
                          >
                            <XCircle className="w-4 h-4" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && !pnrSearch && (
          <div className="flex items-center justify-between px-5 py-4 border-t border-slate-800">
            <p className="text-xs text-slate-500">Page {page + 1} of {totalPages} ({totalElements.toLocaleString()} total)</p>
            <div className="flex items-center gap-2">
              <button onClick={() => { setPage(p => p - 1); fetchBookings(page - 1); }} disabled={page === 0 || loading} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition disabled:opacity-40">
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button onClick={() => { setPage(p => p + 1); fetchBookings(page + 1); }} disabled={page >= totalPages - 1 || loading} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition disabled:opacity-40">
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Cancel Confirm with reason input */}
      {cancelTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setCancelTarget(null)} />
          <div className="relative w-full max-w-md bg-slate-900 border border-slate-700 rounded-2xl shadow-2xl p-6 space-y-4 animate-fade-in">
            <h3 className="text-base font-semibold text-white">Cancel Booking {cancelTarget.bookingReference}</h3>
            <p className="text-sm text-slate-400">This will cancel the booking and trigger automatic refund processing.</p>
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1">Cancellation Reason</label>
              <input
                type="text"
                value={cancelReason}
                onChange={e => setCancelReason(e.target.value)}
                placeholder="Admin action reason..."
                className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:border-rose-500"
              />
            </div>
            <div className="flex justify-end gap-3">
              <button onClick={() => setCancelTarget(null)} className="px-4 py-2 text-sm text-slate-400 hover:text-white bg-slate-800 rounded-lg transition">Cancel</button>
              <button
                onClick={handleCancel}
                disabled={cancelling}
                className="flex items-center gap-2 px-4 py-2 text-sm text-white bg-rose-600 hover:bg-rose-500 rounded-lg transition disabled:opacity-50"
              >
                {cancelling && <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
                Confirm Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
