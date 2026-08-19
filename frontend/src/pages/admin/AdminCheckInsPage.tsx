import React, { useState } from 'react';
import { UserCheck, Search, AlertTriangle } from 'lucide-react';
import { Link } from 'react-router-dom';
import { adminBookingService } from '../../services/adminBookingService';
import { StatusBadge } from '../../components/admin/StatusBadge';
import { Booking } from '../../types/booking';

export const AdminCheckInsPage: React.FC = () => {
  const [query, setQuery] = useState('');
  const [booking, setBooking] = useState<Booking | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async () => {
    if (!query.trim()) return;
    setLoading(true); setError(null); setBooking(null);
    try {
      const res = await adminBookingService.getBookingByReference(query.trim().toUpperCase());
      setBooking(res.data);
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Booking not found');
    } finally {
      setLoading(false);
    }
  };

  const checkedInCount = booking?.passengers?.filter(p => p.checkedIn).length ?? 0;
  const totalPax = booking?.passengers?.length ?? 0;

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
          <UserCheck className="w-6 h-6 text-emerald-400" /> Check-In Viewer
        </h1>
        <p className="text-sm text-slate-400 mt-0.5">Look up check-in status by Booking PNR</p>
      </div>

      <div className="flex gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="text"
            value={query}
            onChange={e => setQuery(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
            placeholder="Enter PNR (e.g. ST8K4P2Q)"
            className="w-full pl-9 pr-4 py-2.5 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30 transition"
          />
        </div>
        <button
          onClick={handleSearch}
          disabled={!query.trim() || loading}
          className="px-5 py-2.5 text-sm font-medium text-white bg-sky-600 hover:bg-sky-500 rounded-xl transition disabled:opacity-50 flex items-center gap-2"
        >
          {loading && <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
          Lookup
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />{error}
        </div>
      )}

      {booking && (
        <div className="space-y-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
            <div className="flex items-start justify-between flex-wrap gap-3 mb-4">
              <div>
                <div className="flex items-center gap-3">
                  <h2 className="font-mono font-bold text-xl text-white">{booking.bookingReference}</h2>
                  <StatusBadge status={booking.status} type="booking" />
                </div>
                <p className="text-sm text-slate-400 mt-0.5">{booking.userEmail} · {booking.flightNumber} · {booking.departureAirport?.code}→{booking.arrivalAirport?.code}</p>
              </div>
              <div className="text-right">
                <p className="text-2xl font-bold text-white">{checkedInCount}/{totalPax}</p>
                <p className="text-xs text-slate-400">Checked In</p>
              </div>
            </div>
            {/* Progress bar */}
            <div className="h-2 bg-slate-800 rounded-full overflow-hidden mb-4">
              <div
                className="h-full bg-gradient-to-r from-emerald-500 to-teal-400 rounded-full transition-all"
                style={{ width: totalPax > 0 ? `${(checkedInCount/totalPax)*100}%` : '0%' }}
              />
            </div>

            <table className="w-full text-sm">
              <thead><tr className="border-b border-slate-800">
                <th className="text-left py-2 text-xs font-semibold text-slate-400 uppercase">Passenger</th>
                <th className="text-left py-2 text-xs font-semibold text-slate-400 uppercase">Seat</th>
                <th className="text-left py-2 text-xs font-semibold text-slate-400 uppercase">Check-In Status</th>
              </tr></thead>
              <tbody className="divide-y divide-slate-800/60">
                {booking.passengers?.map((p, i) => (
                  <tr key={i}>
                    <td className="py-3 font-medium text-white">{p.title} {p.firstName} {p.lastName}</td>
                    <td className="py-3 font-mono text-slate-300">{p.seatNumber ?? '—'}</td>
                    <td className="py-3">
                      {p.checkedIn
                        ? <span className="flex items-center gap-1.5 text-emerald-400 text-xs font-semibold"><UserCheck className="w-3.5 h-3.5" /> Checked In</span>
                        : <span className="text-slate-500 text-xs">Not Checked In</span>
                      }
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="mt-4 pt-4 border-t border-slate-800">
              <Link to={`/admin/bookings/${booking.id}`} className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                View Full Booking Details →
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
