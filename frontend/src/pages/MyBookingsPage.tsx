import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  BookmarkCheck,
  Plane,
  RotateCcw,
  ArrowRight,
  RefreshCw,
  XCircle,
  FileText,
  Clock,
  ShieldCheck,
  CheckCircle2,
  X,
  CreditCard
} from 'lucide-react';
import { Booking, RefundDetails } from '../types/api';
import { bookingService } from '../services/bookingService';
import { paymentService } from '../services/paymentService';
import { BookingSkeleton } from '../components/BookingSkeleton';
import { AirlineLogo } from '../components/AirlineLogo';

export const MyBookingsPage: React.FC = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [activeTab, setActiveTab] = useState<'ALL' | 'UPCOMING' | 'COMPLETED' | 'CANCELLED'>('ALL');
  const [error, setError] = useState<string | null>(null);

  // Cancellation & Refund Modal state
  const [cancellingBooking, setCancellingBooking] = useState<Booking | null>(null);
  const [cancelReason, setCancelReason] = useState<string>('Personal schedule change');
  const [cancelLoading, setCancelLoading] = useState<boolean>(false);
  const [refundInfo, setRefundInfo] = useState<RefundDetails | null>(null);
  const [viewingRefundBooking, setViewingRefundBooking] = useState<Booking | null>(null);

  const CANCELLATION_REASONS = [
    'Personal schedule change',
    'Medical emergency',
    'Business requirement changed',
    'Found a better fare',
    'Visa / travel document issue',
    'Other',
  ];

  const getRefundPolicyHint = (booking: Booking | null): { label: string; color: string } => {
    if (!booking) return { label: '', color: '' };
    const now = Date.now();
    const depMs = new Date(booking.departureTime).getTime();
    const hoursUntilDep = (depMs - now) / (1000 * 60 * 60);
    if (hoursUntilDep > 168) return { label: '100% refund (> 7 days before departure)', color: 'text-emerald-400' };
    if (hoursUntilDep >= 24) return { label: '50% refund (24h – 7 days before departure)', color: 'text-amber-400' };
    if (hoursUntilDep >= 0) return { label: 'No refund (< 24h before departure)', color: 'text-rose-400' };
    return { label: 'No refund (flight already departed)', color: 'text-rose-400' };
  };

  const fetchBookings = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await bookingService.getMyBookings(0, 50);
      if (res.success && res.data?.content) {
        setBookings(res.data.content);
      } else {
        setBookings([]);
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to load bookings');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const handleCancelBooking = async () => {
    if (!cancellingBooking) return;
    try {
      setCancelLoading(true);
      await bookingService.cancelBooking(cancellingBooking.id, cancelReason);
      setCancellingBooking(null);
      await fetchBookings();
    } catch (err: any) {
      alert('Cancellation failed: ' + (err?.message || 'Please try again.'));
    } finally {
      setCancelLoading(false);
    }
  };

  const handleViewRefund = async (booking: Booking) => {
    setViewingRefundBooking(booking);
    try {
      const res = await paymentService.getRefundByBooking(booking.id);
      if (res.success && res.data) {
        setRefundInfo(res.data);
      } else {
        setRefundInfo(null);
      }
    } catch {
      setRefundInfo(null);
    }
  };

  const filteredBookings = bookings.filter((b) => {
    const now = new Date().getTime();
    const depTime = new Date(b.departureTime).getTime();

    if (activeTab === 'UPCOMING') {
      return (b.status === 'CONFIRMED' || b.status === 'CHECKED_IN') && depTime >= now;
    }
    if (activeTab === 'COMPLETED') {
      return (b.status === 'CONFIRMED' || b.status === 'CHECKED_IN') && depTime < now;
    }
    if (activeTab === 'CANCELLED') {
      return b.status === 'CANCELLED';
    }
    return true;
  });

  return (
    <div className="space-y-6 pb-16 max-w-5xl mx-auto">
      {/* 1. HEADER */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 text-xs font-bold shadow-glow-gold mb-2">
            <BookmarkCheck className="w-3.5 h-3.5" />
            <span>Manage Your Travel Reservations</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
            My Trips & Reservations
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Access boarding passes, download tax invoices, check flight status, or manage cancellations.
          </p>
        </div>

        <button
          type="button"
          onClick={fetchBookings}
          className="p-2.5 rounded-xl bg-[#14161F] hover:bg-[#181A22] text-slate-300 hover:text-amber-400 border border-white/10 text-xs font-bold flex items-center gap-2 transition"
          title="Refresh Bookings"
        >
          <RefreshCw className="w-4 h-4 text-amber-400" />
          <span>Refresh</span>
        </button>
      </div>

      {/* 2. FILTER TABS */}
      <div className="flex items-center gap-2 border-b border-white/10 pb-3 overflow-x-auto scrollbar-none text-xs font-bold">
        {[
          { id: 'ALL', label: `All Trips (${bookings.length})` },
          {
            id: 'UPCOMING',
            label: `Upcoming (${
              bookings.filter(
                (b) => (b.status === 'CONFIRMED' || b.status === 'CHECKED_IN') && new Date(b.departureTime).getTime() >= Date.now()
              ).length
            })`,
          },
          {
            id: 'COMPLETED',
            label: `Completed (${
              bookings.filter(
                (b) => (b.status === 'CONFIRMED' || b.status === 'CHECKED_IN') && new Date(b.departureTime).getTime() < Date.now()
              ).length
            })`,
          },
          {
            id: 'CANCELLED',
            label: `Cancelled (${bookings.filter((b) => b.status === 'CANCELLED').length})`,
          },
        ].map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => setActiveTab(tab.id as any)}
            className={`px-4 py-2 rounded-xl transition ${
              activeTab === tab.id
                ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                : 'bg-[#14161F] text-slate-400 hover:text-white border border-white/10'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* 3. BOOKINGS LIST */}
      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <BookingSkeleton key={i} />
          ))}
        </div>
      ) : error ? (
        <div className="p-12 rounded-2xl bg-[#14161F] border border-white/10 text-center space-y-4 shadow-xl">
          <div className="w-12 h-12 rounded-2xl bg-rose-500/15 text-rose-400 border border-rose-500/30 flex items-center justify-center mx-auto">
            <XCircle className="w-6 h-6" />
          </div>
          <h3 className="font-bold text-white text-base">Unable to Load Bookings</h3>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">{error}</p>
          <button
            onClick={fetchBookings}
            className="px-4 py-2 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black text-xs font-extrabold transition shadow-glow-gold"
          >
            Try Again
          </button>
        </div>
      ) : filteredBookings.length === 0 ? (
        <div className="p-16 rounded-2xl bg-[#14161F] border border-white/10 text-center space-y-4 shadow-xl">
          <div className="w-14 h-14 rounded-2xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center mx-auto shadow-glow-gold">
            <Plane className="w-7 h-7 transform -rotate-45" />
          </div>
          <div className="space-y-1">
            <h3 className="font-black text-white text-lg">No Bookings in this Category</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              Ready to explore? Book cheap domestic and international flights today.
            </p>
          </div>
          <Link
            to="/flights"
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-glow-gold transition"
          >
            <span>Search Flights Now</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredBookings.map((b) => {
            const isConfirmed = b.status === 'CONFIRMED';
            const isCheckedIn = b.status === 'CHECKED_IN';
            const isCancelled = b.status === 'CANCELLED';

            return (
              <div
                key={b.id}
                className="p-6 rounded-2xl bg-[#14161F] border border-white/10 hover:border-amber-500/30 hover:shadow-card-hover space-y-5 transition-all duration-300"
              >
                {/* Header Row */}
                <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-white/10">
                  <div className="flex items-center gap-3">
                    <AirlineLogo airline={b.airline} size="md" />
                    <div>
                      <h3 className="font-bold text-white text-base leading-snug">{b.airline}</h3>
                      <div className="flex items-center gap-2 text-xs text-slate-400 mt-0.5">
                        <span className="font-mono text-amber-400 font-bold bg-[#181A22] px-1.5 py-0.2 rounded border border-white/10">
                          {b.flightNumber}
                        </span>
                        <span className="text-white/20">•</span>
                        <span>PNR: <strong className="font-mono text-amber-400 font-bold">{b.bookingReference}</strong></span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span
                      className={`text-xs font-bold px-3 py-1 rounded-full ${
                        isConfirmed
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : isCheckedIn
                          ? 'bg-amber-400/10 text-amber-400 border border-amber-400/20'
                          : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                      }`}
                    >
                      {b.status}
                    </span>
                  </div>
                </div>

                {/* Timings & Route */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 items-center text-center sm:text-left">
                  <div>
                    <p className="text-2xl font-black text-white whitespace-nowrap">
                      {new Date(b.departureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}
                    </p>
                    <p className="text-sm font-black text-amber-400 mt-1">{b.departureAirport.code}</p>
                    <p className="text-xs text-slate-400">{b.departureAirport.city}</p>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      {new Date(b.departureTime).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </p>
                  </div>

                  <div className="flex flex-col items-center px-2">
                    <span className="text-xs text-slate-400 font-semibold">
                      {Math.floor(b.durationMinutes / 60)}h {b.durationMinutes % 60 > 0 ? `${b.durationMinutes % 60}m` : '00m'}
                    </span>
                    <div className="w-full flex items-center my-2">
                      <div className="h-0.5 w-full bg-white/10 relative">
                        <div className="absolute -top-1.5 left-1/2 transform -translate-x-1/2 w-3 h-3 rounded-full bg-amber-400 flex items-center justify-center shadow-glow-gold">
                          <Plane className="w-2 h-2 text-black transform rotate-45" />
                        </div>
                      </div>
                    </div>
                    <span className="text-[10px] font-bold text-amber-400 uppercase tracking-wider px-2 py-0.5 rounded-full bg-amber-400/10 border border-amber-400/20">
                      {b.cabinClass.replace('_', ' ')}
                    </span>
                  </div>

                  <div className="text-left sm:text-right">
                    <p className="text-2xl font-black text-white whitespace-nowrap">
                      {new Date(b.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}
                    </p>
                    <p className="text-sm font-black text-amber-400 mt-1">{b.arrivalAirport.code}</p>
                    <p className="text-xs text-slate-400">{b.arrivalAirport.city}</p>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      {new Date(b.arrivalTime).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </p>
                  </div>
                </div>

                {/* Passengers List */}
                <div className="flex flex-wrap items-center gap-2 pt-1">
                  {b.passengers.map((p, idx) => (
                    <span
                      key={idx}
                      className="px-3 py-1.5 rounded-xl bg-[#181A22] border border-white/10 text-xs text-slate-300 font-medium flex items-center gap-1.5"
                    >
                      <span className="font-bold text-white">
                        {p.firstName} {p.lastName}
                      </span>
                      {p.seatNumber && (
                        <span className="text-amber-400 font-mono font-black bg-amber-400/10 px-1.5 py-0.2 rounded border border-amber-400/20">
                          Seat {p.seatNumber}
                        </span>
                      )}
                    </span>
                  ))}
                </div>

                {/* Action Footer */}
                <div className="pt-4 border-t border-white/10 flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <span className="text-[10px] text-slate-400 block font-medium">Total Fare Paid</span>
                    <span className="text-2xl font-black text-amber-400 tracking-tight">
                      ₹{b.totalAmount.toLocaleString('en-IN')}
                    </span>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    {/* Ticket Link / PDF */}
                    {b.ticketId && (
                      <Link
                        to={`/ticket/${b.id}`}
                        className="px-3.5 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-300 text-xs font-bold flex items-center gap-1.5 border border-white/10 transition"
                      >
                        <FileText className="w-3.5 h-3.5 text-amber-400" />
                        <span>View Ticket</span>
                      </Link>
                    )}

                    {/* Online Check-in / Boarding Pass */}
                    {isConfirmed && (
                      <Link
                        to={`/check-in/${b.id}`}
                        className="px-4 py-2 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black text-xs font-black flex items-center gap-1.5 transition shadow-glow-gold"
                      >
                        <span>Online Check-In</span>
                        <ArrowRight className="w-4 h-4 text-black" />
                      </Link>
                    )}

                    {isCheckedIn && (
                      <Link
                        to={`/boarding-pass/${b.id}`}
                        className="px-4 py-2 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black text-xs font-black flex items-center gap-1.5 transition shadow-glow-gold"
                      >
                        <span>Boarding Pass</span>
                        <ArrowRight className="w-4 h-4 text-black" />
                      </Link>
                    )}

                    {/* Cancel & Refund Action */}
                    {isConfirmed && (
                      <button
                        type="button"
                        onClick={() => setCancellingBooking(b)}
                        className="px-3.5 py-2 rounded-xl bg-rose-500/15 hover:bg-rose-500/25 text-rose-400 border border-rose-500/30 text-xs font-bold transition"
                      >
                        Cancel Flight
                      </button>
                    )}

                    {/* View Refund Details if Cancelled */}
                    {isCancelled && (
                      <button
                        type="button"
                        onClick={() => handleViewRefund(b)}
                        className="px-3.5 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-emerald-400 text-xs font-bold flex items-center gap-1.5 border border-white/10 transition"
                      >
                        <RotateCcw className="w-3.5 h-3.5 text-emerald-400" />
                        <span>Refund Status</span>
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Cancellation Confirmation Modal */}
      {cancellingBooking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="w-full max-w-md rounded-3xl bg-[#14161F] border border-white/10 p-6 shadow-2xl space-y-5">
            <div className="w-12 h-12 rounded-xl bg-rose-500/15 text-rose-400 border border-rose-500/30 flex items-center justify-center mx-auto">
              <XCircle className="w-7 h-7" />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-lg font-black text-white">Cancel Flight Reservation</h3>
              <p className="text-xs text-slate-400">
                Are you sure you want to cancel booking PNR{' '}
                <strong className="text-amber-400 font-mono font-bold">{cancellingBooking.bookingReference}</strong>?
              </p>
            </div>

            <div className="p-3.5 rounded-xl bg-[#181A22] border border-white/10 text-xs space-y-1.5 text-slate-300">
              <p className="font-bold text-white">Cancellation Policy:</p>
              <p>• Seats will be released back into public inventory immediately.</p>
              <p>• Refund will be automatically triggered via the payment gateway.</p>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-300">Reason for Cancellation</label>
              <select
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                className="w-full bg-[#181A22] border border-white/10 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-amber-400 transition cursor-pointer font-medium"
              >
                {CANCELLATION_REASONS.map((r) => (
                  <option key={r} value={r} className="bg-[#14161F]">{r}</option>
                ))}
              </select>
            </div>

            {/* Refund Policy Hint */}
            {cancellingBooking && (() => {
              const hint = getRefundPolicyHint(cancellingBooking);
              return hint.label ? (
                <div className="px-3.5 py-2 rounded-xl bg-[#181A22] border border-white/10 flex items-center gap-2">
                  <span className="text-xs font-semibold text-slate-400">Refund Estimate:</span>
                  <span className={`text-xs font-bold ${hint.color}`}>{hint.label}</span>
                </div>
              ) : null;
            })()}

            <div className="flex items-center gap-3 pt-2">
              <button
                type="button"
                disabled={cancelLoading}
                onClick={handleCancelBooking}
                className="flex-1 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs shadow-sm transition disabled:opacity-50"
              >
                {cancelLoading ? 'Cancelling...' : 'Confirm Cancellation'}
              </button>

              <button
                type="button"
                onClick={() => setCancellingBooking(null)}
                className="px-4 py-2.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-300 text-xs font-bold transition border border-white/10"
              >
                Keep Booking
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Refund Tracker Modal */}
      {viewingRefundBooking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="w-full max-w-md rounded-3xl bg-[#14161F] border border-white/10 p-6 shadow-2xl space-y-5">
            <div className="flex items-center justify-between border-b border-white/10 pb-3">
              <div className="flex items-center gap-2">
                <RotateCcw className="w-4 h-4 text-emerald-400" />
                <h3 className="text-base font-bold text-white">Refund Status & Timeline</h3>
              </div>
              <button
                type="button"
                onClick={() => setViewingRefundBooking(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3">
              <div className="p-3.5 rounded-xl bg-[#181A22] border border-white/10 space-y-1.5 text-xs">
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">Booking Reference:</span>
                  <span className="font-mono font-bold text-amber-400">{viewingRefundBooking.bookingReference}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">Total Booking Amount:</span>
                  <span className="font-bold text-white">₹{viewingRefundBooking.totalAmount.toLocaleString()}</span>
                </div>
                {refundInfo && (
                  <>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Refund Amount:</span>
                      <span className="font-black text-emerald-400">₹{refundInfo.amount.toLocaleString()}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Refund Status:</span>
                      <span className="font-bold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                        {refundInfo.status}
                      </span>
                    </div>
                  </>
                )}
              </div>

              {/* Status Timeline */}
              <div className="space-y-3 pt-2 text-xs">
                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center font-bold text-[10px]">
                    ✓
                  </div>
                  <div>
                    <strong className="text-white block">Booking Cancelled</strong>
                    <span className="text-slate-400 text-[11px]">Seat inventory released</span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center font-bold text-[10px]">
                    ✓
                  </div>
                  <div>
                    <strong className="text-white block">Refund Triggered</strong>
                    <span className="text-slate-400 text-[11px]">Processed via payment gateway</span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 rounded-full bg-amber-400/20 text-amber-400 border border-amber-400/30 flex items-center justify-center font-bold text-[10px]">
                    ●
                  </div>
                  <div>
                    <strong className="text-white block">Bank Settlement</strong>
                    <span className="text-slate-400 text-[11px]">Estimated in 1–3 business days</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="pt-2">
              <button
                type="button"
                onClick={() => setViewingRefundBooking(null)}
                className="w-full py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black text-xs font-black shadow-glow-gold"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
