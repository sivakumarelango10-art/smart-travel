import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import {
  ArrowLeft, BookmarkCheck, User, Plane, CreditCard, Ticket,
  UserCheck, RefreshCw, AlertTriangle, XCircle
} from 'lucide-react';
import { adminBookingService } from '../../services/adminBookingService';
import { adminTicketService } from '../../services/adminTicketService';
import { StatusBadge } from '../../components/admin/StatusBadge';
import { useAdminToast } from '../../components/admin/AdminToast';
import { Booking } from '../../types/booking';
import { AdminTicket } from '../../types/admin';

export const AdminBookingDetailPage: React.FC = () => {
  const { bookingId } = useParams<{ bookingId: string }>();
  const navigate = useNavigate();
  const { showToast } = useAdminToast();

  const [booking, setBooking] = useState<Booking | null>(null);
  const [ticket, setTicket] = useState<AdminTicket | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [cancelReason, setCancelReason] = useState('');
  const [cancelling, setCancelling] = useState(false);
  const [showCancelForm, setShowCancelForm] = useState(false);

  const [retryingTicket, setRetryingTicket] = useState(false);

  const fetchData = async () => {
    if (!bookingId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await adminBookingService.getBookingById(bookingId);
      setBooking(res.data);

      // Attempt ticket fetch
      try {
        const tRes = await adminTicketService.getTicketByBookingId(bookingId);
        setTicket(tRes.data);
      } catch { /* ticket may not exist yet */ }
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Booking not found');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, [bookingId]);

  const handleCancel = async () => {
    if (!bookingId || !booking) return;
    setCancelling(true);
    try {
      await adminBookingService.cancelBooking(bookingId, { cancellationReason: cancelReason || 'Admin action' });
      showToast('success', 'Booking cancelled', `Booking ${booking.bookingReference} has been cancelled.`);
      setShowCancelForm(false);
      fetchData();
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Cancel failed', err?.message);
    } finally {
      setCancelling(false);
    }
  };

  const handleRetryTicket = async () => {
    if (!bookingId) return;
    setRetryingTicket(true);
    try {
      const res = await adminTicketService.retryIssueTicket(bookingId);
      setTicket(res.data);
      showToast('success', 'Ticket issued', `Ticket ${res.data?.ticketNumber} issued successfully.`);
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Ticket issuance failed', err?.message);
    } finally {
      setRetryingTicket(false);
    }
  };

  if (loading) return (
    <div className="space-y-6">
      <div className="h-8 w-48 bg-slate-800 rounded animate-pulse" />
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4 animate-pulse">
        {Array.from({ length: 8 }).map((_, i) => <div key={i} className="h-4 bg-slate-800 rounded" />)}
      </div>
    </div>
  );

  if (error || !booking) return (
    <div className="p-8 text-center">
      <AlertTriangle className="w-12 h-12 text-rose-400 mx-auto mb-3" />
      <p className="text-white font-semibold">{error ?? 'Booking not found'}</p>
      <button onClick={() => navigate('/admin/bookings')} className="mt-4 text-sky-400 hover:text-sky-300 text-sm">← Back to bookings</button>
    </div>
  );

  const row = (label: string, value: React.ReactNode) => (
    <div className="flex items-start justify-between py-2.5 border-b border-slate-800/60 last:border-0">
      <span className="text-xs text-slate-500 font-medium w-36 flex-shrink-0">{label}</span>
      <span className="text-sm text-slate-200 text-right flex-1">{value}</span>
    </div>
  );

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center gap-3">
        <Link to="/admin/bookings" className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div className="flex-1">
          <div className="flex items-center gap-3 flex-wrap">
            <h1 className="text-xl font-bold text-white font-mono">{booking.bookingReference}</h1>
            <StatusBadge status={booking.status} type="booking" />
          </div>
          <p className="text-sm text-slate-400">Booking ID: <span className="font-mono">{booking.id}</span></p>
        </div>
        <button onClick={fetchData} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition border border-slate-700">
          <RefreshCw className="w-4 h-4" />
        </button>
        {(booking.status === 'CONFIRMED' || booking.status === 'PENDING') && (
          <button
            onClick={() => setShowCancelForm(!showCancelForm)}
            className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-rose-400 bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/20 rounded-xl transition"
          >
            <XCircle className="w-4 h-4" /> Cancel Booking
          </button>
        )}
      </div>

      {showCancelForm && (
        <div className="p-4 bg-rose-500/5 border border-rose-500/20 rounded-2xl space-y-3">
          <p className="text-sm font-semibold text-rose-400">Force Cancel Booking</p>
          <input
            type="text"
            value={cancelReason}
            onChange={e => setCancelReason(e.target.value)}
            placeholder="Reason for cancellation..."
            className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:border-rose-500"
          />
          <div className="flex gap-3">
            <button onClick={() => setShowCancelForm(false)} className="px-4 py-2 text-sm text-slate-400 hover:text-white bg-slate-800 rounded-lg transition">Dismiss</button>
            <button onClick={handleCancel} disabled={cancelling} className="flex items-center gap-2 px-4 py-2 text-sm text-white bg-rose-600 hover:bg-rose-500 rounded-lg transition disabled:opacity-50">
              {cancelling && <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
              Confirm Cancel
            </button>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Booking Info */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide flex items-center gap-2 mb-4">
            <BookmarkCheck className="w-4 h-4 text-indigo-400" /> Booking Details
          </h2>
          {row('PNR', <span className="font-mono font-bold">{booking.bookingReference}</span>)}
          {row('Status', <StatusBadge status={booking.status} type="booking" />)}
          {row('Cabin Class', booking.cabinClass)}
          {row('Passengers', String(booking.passengerCount))}
          {row('Created', new Date(booking.createdAt).toLocaleString('en-IN'))}
          {booking.expiresAt && row('Expires At', new Date(booking.expiresAt).toLocaleString('en-IN'))}
          {booking.cancelledAt && row('Cancelled At', new Date(booking.cancelledAt).toLocaleString('en-IN'))}
          {booking.cancellationReason && row('Cancel Reason', booking.cancellationReason)}
        </div>

        {/* Customer Info */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide flex items-center gap-2 mb-4">
            <User className="w-4 h-4 text-sky-400" /> Customer
          </h2>
          {row('Email', booking.userEmail)}
          {row('User ID', <span className="font-mono text-xs">{booking.userId}</span>)}
        </div>

        {/* Flight Info */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide flex items-center gap-2 mb-4">
            <Plane className="w-4 h-4 text-sky-400" /> Flight
          </h2>
          {row('Flight', <Link to={`/admin/flights/${booking.flightId}`} className="font-mono text-sky-400 hover:text-sky-300">{booking.flightNumber}</Link>)}
          {row('Airline', booking.airline)}
          {row('Route', `${booking.departureAirport?.code} → ${booking.arrivalAirport?.code}`)}
          {row('Departure', new Date(booking.departureTime).toLocaleString('en-IN'))}
          {row('Arrival', new Date(booking.arrivalTime).toLocaleString('en-IN'))}
          {row('Duration', `${Math.floor(booking.durationMinutes / 60)}h ${booking.durationMinutes % 60}m`)}
        </div>

        {/* Fare */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide flex items-center gap-2 mb-4">
            <CreditCard className="w-4 h-4 text-emerald-400" /> Fare
          </h2>
          {booking.fareBreakdown && (
            <>
              {row('Base Fare', `₹${Number(booking.fareBreakdown.baseFare).toLocaleString('en-IN')}`)}
              {row('Tax', `₹${Number(booking.fareBreakdown.taxAmount).toLocaleString('en-IN')}`)}
              {row('Airport Fee', `₹${Number(booking.fareBreakdown.airportFee).toLocaleString('en-IN')}`)}
              {row('Service Fee', `₹${Number(booking.fareBreakdown.serviceFee).toLocaleString('en-IN')}`)}
              {booking.fareBreakdown.discountAmount && row('Discount', `-₹${Number(booking.fareBreakdown.discountAmount).toLocaleString('en-IN')}`)}
            </>
          )}
          {row('Total', <span className="text-lg font-bold text-white">₹{Number(booking.totalAmount).toLocaleString('en-IN')}</span>)}
          {row('Currency', booking.currency)}
        </div>
      </div>

      {/* Passengers */}
      {booking.passengers && booking.passengers.length > 0 && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
          <div className="px-5 py-4 border-b border-slate-800">
            <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide flex items-center gap-2">
              <User className="w-4 h-4 text-violet-400" /> Passengers ({booking.passengers.length})
            </h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead><tr className="bg-slate-950/40">
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-400 uppercase">#</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-slate-400 uppercase">Name</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-slate-400 uppercase">Seat</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-slate-400 uppercase">Check-in</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-slate-400 uppercase">DOB</th>
              </tr></thead>
              <tbody className="divide-y divide-slate-800/60">
                {booking.passengers.map((p, i) => (
                  <tr key={i} className="hover:bg-slate-800/20">
                    <td className="px-5 py-3 text-slate-400">{i + 1}</td>
                    <td className="px-4 py-3 font-medium text-white">{p.title} {p.firstName} {p.lastName}</td>
                    <td className="px-4 py-3 font-mono text-slate-300">{p.seatNumber ?? '—'}</td>
                    <td className="px-4 py-3">
                      {p.checkedIn
                        ? <span className="flex items-center gap-1 text-emerald-400 text-xs"><UserCheck className="w-3.5 h-3.5" /> Checked In</span>
                        : <span className="text-slate-500 text-xs">Not checked in</span>
                      }
                    </td>
                    <td className="px-4 py-3 text-slate-400">{p.dateOfBirth ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Ticket Info */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide flex items-center gap-2">
            <Ticket className="w-4 h-4 text-teal-400" /> E-Ticket
          </h2>
          {booking.status === 'CONFIRMED' && !ticket && (
            <button
              onClick={handleRetryTicket}
              disabled={retryingTicket}
              className="flex items-center gap-2 px-3 py-1.5 text-xs text-teal-400 border border-teal-500/30 hover:bg-teal-500/10 rounded-lg transition disabled:opacity-50"
            >
              {retryingTicket && <span className="w-3 h-3 border-2 border-teal-400/30 border-t-teal-400 rounded-full animate-spin" />}
              Retry Issuance
            </button>
          )}
        </div>
        {ticket ? (
          <div className="space-y-2">
            {row('Ticket Number', <span className="font-mono font-bold text-teal-400">{ticket.ticketNumber}</span>)}
            {row('Status', <StatusBadge status={ticket.status} type="ticket" />)}
            {row('Issued At', new Date(ticket.issuedAt).toLocaleString('en-IN'))}
            {booking.ticketId && (
              <div className="mt-2">
                <button
                  onClick={async () => {
                    try {
                      const blob = await adminTicketService.downloadTicketPdf(booking.ticketId!);
                      const url = URL.createObjectURL(blob);
                      const a = document.createElement('a');
                      a.href = url;
                      a.download = `ticket-${ticket.ticketNumber}.pdf`;
                      a.click();
                      URL.revokeObjectURL(url);
                    } catch { showToast('error', 'PDF download failed'); }
                  }}
                  className="px-3 py-1.5 text-xs text-sky-400 border border-sky-500/30 hover:bg-sky-500/10 rounded-lg transition"
                >
                  Download PDF
                </button>
              </div>
            )}
          </div>
        ) : (
          <p className="text-sm text-slate-500">No ticket issued yet.</p>
        )}
      </div>
    </div>
  );
};
