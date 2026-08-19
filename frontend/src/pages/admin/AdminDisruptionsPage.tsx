import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Zap, Search, AlertTriangle, CheckCircle, ChevronLeft, ChevronRight } from 'lucide-react';
import { adminFlightService } from '../../services/adminFlightService';
import { adminBookingService } from '../../services/adminBookingService';
import { StatusBadge } from '../../components/admin/StatusBadge';
import { useAdminToast } from '../../components/admin/AdminToast';
import { Booking } from '../../types/booking';
import { FlightDisruption } from '../../types/admin';

export const AdminDisruptionsPage: React.FC = () => {
  const { showToast } = useAdminToast();
  const [recentBookings, setRecentBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [flightIdInput, setFlightIdInput] = useState('');
  const [disruptions, setDisruptions] = useState<FlightDisruption[]>([]);
  const [disruptionLoading, setDisruptionLoading] = useState(false);
  const [disruptionPage, setDisruptionPage] = useState(0);
  const [disruptionTotalPages, setDisruptionTotalPages] = useState(0);
  const [resolvingId, setResolvingId] = useState<string | null>(null);

  // Load recent cancelled/delayed bookings as a proxy for "disrupted flights"
  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const [cancelled, delayed] = await Promise.all([
          adminBookingService.getAllBookings(0, 5, 'CANCELLED'),
          adminBookingService.getAllBookings(0, 5, 'PENDING'),
        ]);
        const allBookings = [...(cancelled.data?.content ?? []), ...(delayed.data?.content ?? [])];
        setRecentBookings(allBookings.slice(0, 8));
      } catch (e: unknown) {
        const err = e as { message?: string };
        setError(err?.message ?? 'Could not load disruption overview');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const fetchDisruptions = useCallback(async (p = 0) => {
    if (!flightIdInput.trim()) return;
    setDisruptionLoading(true);
    try {
      const res = await adminFlightService.getFlightDisruptions(flightIdInput.trim(), p, 10);
      setDisruptions(res.data?.content ?? []);
      setDisruptionTotalPages(res.data?.totalPages ?? 0);
      setDisruptionPage(p);
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Failed to load disruptions', err?.message);
    } finally {
      setDisruptionLoading(false);
    }
  }, [flightIdInput]);

  const handleResolve = async (disruptionId: string) => {
    setResolvingId(disruptionId);
    try {
      await adminFlightService.resolveDisruption(disruptionId);
      showToast('success', 'Disruption resolved');
      fetchDisruptions(disruptionPage);
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Resolve failed', err?.message);
    } finally {
      setResolvingId(null);
    }
  };

  return (
    <div className="space-y-6 max-w-[1200px]">
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
          <Zap className="w-6 h-6 text-amber-400" /> Disruption Management
        </h1>
        <p className="text-sm text-slate-400 mt-0.5">Search flight disruption history and resolve active disruptions</p>
      </div>

      {/* Flight ID search */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wide mb-4">Disruption History by Flight</h2>
        <div className="flex gap-3">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
            <input
              type="text"
              value={flightIdInput}
              onChange={e => setFlightIdInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && fetchDisruptions(0)}
              placeholder="Enter Flight ID..."
              className="w-full pl-9 pr-4 py-2.5 bg-slate-800 border border-slate-700 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30 transition"
            />
          </div>
          <button
            onClick={() => fetchDisruptions(0)}
            disabled={!flightIdInput.trim() || disruptionLoading}
            className="px-5 py-2.5 text-sm font-medium text-white bg-amber-600 hover:bg-amber-500 rounded-xl transition disabled:opacity-50 flex items-center gap-2"
          >
            {disruptionLoading && <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
            Load Disruptions
          </button>
        </div>

        {disruptions.length > 0 && (
          <div className="mt-4 divide-y divide-slate-800/60">
            {disruptions.map(d => (
              <div key={d.id} className="py-4 flex items-start justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2 flex-wrap mb-1">
                    <span className="text-sm font-semibold text-white">{d.disruptionType.replace(/_/g,' ')}</span>
                    <StatusBadge status={d.status} type="disruption" size="xs" />
                  </div>
                  <p className="text-xs text-slate-400">{d.reason}</p>
                  {d.description && <p className="text-[11px] text-slate-500 mt-0.5">{d.description}</p>}
                  <p className="text-[11px] text-slate-500 mt-1">by {d.createdBy} · {new Date(d.createdAt).toLocaleString('en-IN')}</p>
                </div>
                <div className="flex-shrink-0 flex items-center gap-2">
                  <Link to={`/admin/flights/${d.flightId}`} className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                    View Flight
                  </Link>
                  {d.status === 'ACTIVE' && (
                    <button
                      onClick={() => handleResolve(d.id)}
                      disabled={resolvingId === d.id}
                      className="flex items-center gap-1 px-3 py-1.5 text-xs text-emerald-400 border border-emerald-500/30 hover:bg-emerald-500/10 rounded-lg transition disabled:opacity-50"
                    >
                      {resolvingId === d.id ? <span className="w-3 h-3 border-2 border-emerald-400/30 border-t-emerald-400 rounded-full animate-spin" /> : <CheckCircle className="w-3 h-3" />}
                      Resolve
                    </button>
                  )}
                </div>
              </div>
            ))}
            {disruptionTotalPages > 1 && (
              <div className="flex items-center justify-end gap-2 pt-3">
                <button onClick={() => fetchDisruptions(disruptionPage - 1)} disabled={disruptionPage === 0} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg disabled:opacity-40"><ChevronLeft className="w-4 h-4" /></button>
                <span className="text-xs text-slate-500">Page {disruptionPage + 1} of {disruptionTotalPages}</span>
                <button onClick={() => fetchDisruptions(disruptionPage + 1)} disabled={disruptionPage >= disruptionTotalPages - 1} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg disabled:opacity-40"><ChevronRight className="w-4 h-4" /></button>
              </div>
            )}
          </div>
        )}
        {flightIdInput && !disruptionLoading && disruptions.length === 0 && (
          <p className="mt-4 text-sm text-slate-500 text-center py-4">No disruptions found for this flight ID.</p>
        )}
      </div>

      {/* Recent disrupted bookings */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-800">
          <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wide flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-amber-400" /> Recent Cancelled/Pending Bookings
          </h2>
          <Link to="/admin/bookings?status=CANCELLED" className="text-xs text-sky-400 hover:text-sky-300">View all →</Link>
        </div>
        <div className="divide-y divide-slate-800/60">
          {loading ? (
            Array.from({ length: 5 }).map((_, i) => <div key={i} className="px-5 py-4 animate-pulse"><div className="h-4 bg-slate-800 rounded" /></div>)
          ) : error ? (
            <p className="px-5 py-6 text-rose-400 text-sm">{error}</p>
          ) : recentBookings.length === 0 ? (
            <p className="px-5 py-8 text-center text-slate-500 text-sm">No disrupted bookings</p>
          ) : (
            recentBookings.map(b => (
              <Link key={b.id} to={`/admin/bookings/${b.id}`} className="flex items-center gap-4 px-5 py-3.5 hover:bg-slate-800/40 transition">
                <div className="flex-1">
                  <p className="text-sm font-mono font-bold text-white">{b.bookingReference}</p>
                  <p className="text-[11px] text-slate-400">{b.userEmail} · {b.flightNumber}</p>
                </div>
                <StatusBadge status={b.status} type="booking" size="xs" />
              </Link>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
