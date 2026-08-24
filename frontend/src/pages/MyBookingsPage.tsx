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
    if (hoursUntilDep > 168) return { label: '100% refund (> 7 days before departure)', color: 'text-emerald-600' };
    if (hoursUntilDep >= 24) return { label: '50% refund (24h – 7 days before departure)', color: 'text-amber-600' };
    if (hoursUntilDep >= 0) return { label: 'No refund (< 24h before departure)', color: 'text-rose-600' };
    return { label: 'No refund (flight already departed)', color: 'text-rose-600' };
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
    const isPast = new Date(b.arrivalTime) < new Date();
    if (activeTab === 'UPCOMING') return !isPast && b.status !== 'CANCELLED';
    if (activeTab === 'COMPLETED') return isPast && b.status !== 'CANCELLED';
    if (activeTab === 'CANCELLED') return b.status === 'CANCELLED';
    return true;
  });

  return (
    <div className="space-y-6 pb-16 max-w-6xl mx-auto">
      {/* 1. TOP HEADER & FILTER TABS */}
      <section className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 sm:p-6 rounded-2xl bg-white border border-slate-200 shadow-sm">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-black text-primary tracking-tight">
              My Bookings & Reservations
            </h1>
            <span className="text-xs font-bold text-secondary bg-secondary/10 px-2.5 py-0.5 rounded-full border border-secondary/20">
              {bookings.length} {bookings.length === 1 ? 'Trip' : 'Trips'}
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Access your active itineraries, digital boarding passes, and refund history
          </p>
        </div>

        {/* Tab Pills */}
        <div className="flex items-center gap-1.5 p-1 bg-slate-100 rounded-xl">
          {(['ALL', 'UPCOMING', 'COMPLETED', 'CANCELLED'] as const).map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                activeTab === tab
                  ? 'bg-white text-primary shadow-sm'
                  : 'text-slate-600 hover:text-primary'
              }`}
            >
              {tab.charAt(0) + tab.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </section>

      {/* 2. BOOKINGS LIST */}
      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <BookingSkeleton key={i} />
          ))}
        </div>
      ) : error ? (
        <div className="p-10 rounded-2xl bg-white border border-slate-200 text-center space-y-4 shadow-sm">
          <p className="text-sm text-slate-600">{error}</p>
          <button
            onClick={fetchBookings}
            className="px-4 py-2 rounded-xl bg-primary text-white text-xs font-bold inline-flex items-center gap-2"
          >
            <RefreshCw className="w-4 h-4" /> Retry Loading
          </button>
        </div>
      ) : filteredBookings.length === 0 ? (
        <div className="p-12 rounded-2xl bg-white border border-slate-200 text-center space-y-4 shadow-sm">
          <div className="w-14 h-14 rounded-2xl bg-secondary/10 text-secondary border border-secondary/20 flex items-center justify-center mx-auto">
            <BookmarkCheck className="w-7 h-7" />
          </div>
          <h3 className="text-lg font-black text-primary">No Bookings Found</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            You don't have any bookings matching this category. Ready for your next journey?
          </p>
          <Link
            to="/flights"
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary text-white text-xs font-bold shadow-sm"
          >
            <span>Search Flights</span>
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
                className="rounded-2xl bg-white border border-slate-200 hover:border-slate-300 hover:shadow-card p-5 sm:p-6 space-y-5 transition-all duration-200"
              >
                {/* Header: Airline & PNR */}
                <div className="flex flex-wrap items-center justify-between gap-3 pb-4 border-b border-slate-100">
                  <div className="flex items-center gap-3">
                    <AirlineLogo airline={b.airline} airlineCode={b.airlineCode} size="md" />
                    <div>
                      <h3 className="font-bold text-primary text-base leading-snug">{b.airline}</h3>
                      <div className="flex items-center gap-2 text-xs text-slate-500 mt-0.5">
                        <span className="font-mono text-slate-700 font-bold bg-slate-100 px-1.5 py-0.2 rounded border border-slate-200">
                          {b.flightNumber}
                        </span>
                        <span>•</span>
                        <span>PNR: <strong className="font-mono text-primary font-bold">{b.bookingReference}</strong></span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span
                      className={`text-xs font-bold px-3 py-1 rounded-full ${
                        isConfirmed
                          ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                          : isCheckedIn
                          ? 'bg-secondary/10 text-secondary border border-secondary/20'
                          : 'bg-rose-50 text-rose-700 border border-rose-200'
                      }`}
                    >
                      {b.status}
                    </span>
                  </div>
                </div>

                {/* Timings & Route */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 items-center text-center sm:text-left">
                  <div>
                    <p className="text-2xl font-black text-primary whitespace-nowrap">
                      {new Date(b.departureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}
                    </p>
                    <p className="text-sm font-black text-secondary mt-1">{b.departureAirport.code}</p>
                    <p className="text-xs text-slate-600">{b.departureAirport.city}</p>
                    <p className="text-[11px] text-slate-400 mt-0.5">
                      {new Date(b.departureTime).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </p>
                  </div>

                  <div className="flex flex-col items-center px-2">
                    <span className="text-xs text-slate-500 font-semibold">
                      {Math.floor(b.durationMinutes / 60)}h {b.durationMinutes % 60 > 0 ? `${b.durationMinutes % 60}m` : '00m'}
                    </span>
                    <div className="w-full flex items-center my-2">
                      <div className="h-0.5 w-full bg-slate-200 relative">
                        <div className="absolute -top-1.5 left-1/2 transform -translate-x-1/2 w-3 h-3 rounded-full bg-secondary flex items-center justify-center shadow-sm">
                          <Plane className="w-2 h-2 text-white transform rotate-45" />
                        </div>
                      </div>
                    </div>
                    <span className="text-[10px] font-bold text-slate-600 uppercase tracking-wider px-2 py-0.5 rounded-full bg-slate-100 border border-slate-200">
                      {b.cabinClass.replace('_', ' ')}
                    </span>
                  </div>

                  <div className="text-left sm:text-right">
                    <p className="text-2xl font-black text-primary whitespace-nowrap">
                      {new Date(b.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}
                    </p>
                    <p className="text-sm font-black text-secondary mt-1">{b.arrivalAirport.code}</p>
                    <p className="text-xs text-slate-600">{b.arrivalAirport.city}</p>
                    <p className="text-[11px] text-slate-400 mt-0.5">
                      {new Date(b.arrivalTime).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </p>
                  </div>
                </div>

                {/* Passengers List */}
                <div className="flex flex-wrap items-center gap-2 pt-1">
                  {b.passengers.map((p, idx) => (
                    <span
                      key={idx}
                      className="px-3 py-1.5 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-700 font-medium flex items-center gap-1.5"
                    >
                      <span className="font-bold text-primary">
                        {p.firstName} {p.lastName}
                      </span>
                      {p.seatNumber && (
                        <span className="text-secondary font-mono font-black bg-secondary/10 px-1.5 py-0.2 rounded border border-secondary/20">
                          Seat {p.seatNumber}
                        </span>
                      )}
                    </span>
                  ))}
                </div>

                {/* Action Footer */}
                <div className="pt-4 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <span className="text-[10px] text-slate-400 block font-medium">Total Fare Paid</span>
                    <span className="text-2xl font-black text-primary tracking-tight">
                      ₹{b.totalAmount.toLocaleString('en-IN')}
                    </span>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    {/* Ticket Link / PDF */}
                    {b.ticketId && (
                      <Link
                        to={`/ticket/${b.id}`}
                        className="px-3.5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold flex items-center gap-1.5 border border-slate-200 transition"
                      >
                        <FileText className="w-3.5 h-3.5 text-secondary" />
                        <span>View Ticket</span>
                      </Link>
                    )}

                    {/* Online Check-in / Boarding Pass */}
                    {isConfirmed && (
                      <Link
                        to={`/check-in/${b.id}`}
                        className="px-4 py-2 rounded-xl bg-secondary hover:bg-secondary-hover text-white text-xs font-bold flex items-center gap-1.5 transition shadow-sm"
                      >
                        <span>Online Check-In</span>
                        <ArrowRight className="w-4 h-4" />
                      </Link>
                    )}

                    {isCheckedIn && (
                      <Link
                        to={`/boarding-pass/${b.id}`}
                        className="px-4 py-2 rounded-xl bg-secondary hover:bg-secondary-hover text-white text-xs font-bold flex items-center gap-1.5 transition shadow-sm"
                      >
                        <span>Boarding Pass</span>
                        <ArrowRight className="w-4 h-4" />
                      </Link>
                    )}

                    {/* Cancel & Refund Action */}
                    {isConfirmed && (
                      <button
                        type="button"
                        onClick={() => setCancellingBooking(b)}
                        className="px-3.5 py-2 rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 text-xs font-bold transition"
                      >
                        Cancel Flight
                      </button>
                    )}

                    {/* View Refund Details if Cancelled */}
                    {isCancelled && (
                      <button
                        type="button"
                        onClick={() => handleViewRefund(b)}
                        className="px-3.5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold flex items-center gap-1.5 border border-slate-200 transition"
                      >
                        <RotateCcw className="w-3.5 h-3.5 text-emerald-600" />
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
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-md rounded-2xl bg-white border border-slate-200 p-6 shadow-2xl space-y-5">
            <div className="w-12 h-12 rounded-xl bg-rose-50 text-rose-500 border border-rose-200 flex items-center justify-center mx-auto">
              <XCircle className="w-7 h-7" />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-lg font-black text-primary">Cancel Flight Reservation</h3>
              <p className="text-xs text-slate-500">
                Are you sure you want to cancel booking PNR{' '}
                <strong className="text-primary font-mono font-bold">{cancellingBooking.bookingReference}</strong>?
              </p>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200 text-xs space-y-1.5 text-slate-600">
              <p className="font-bold text-primary">Cancellation Policy:</p>
              <p>• Seats will be released back into public inventory immediately.</p>
              <p>• Refund will be automatically triggered via the payment gateway.</p>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-700">Reason for Cancellation</label>
              <select
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                className="w-full bg-white border border-slate-300 rounded-xl px-3 py-2 text-xs text-primary focus:outline-none focus:border-secondary transition cursor-pointer font-medium"
              >
                {CANCELLATION_REASONS.map((r) => (
                  <option key={r} value={r}>{r}</option>
                ))}
              </select>
            </div>

            {/* Refund Policy Hint */}
            {cancellingBooking && (() => {
              const hint = getRefundPolicyHint(cancellingBooking);
              return hint.label ? (
                <div className="px-3.5 py-2 rounded-xl bg-slate-50 border border-slate-200 flex items-center gap-2">
                  <span className="text-xs font-semibold text-slate-500">Refund Estimate:</span>
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
                className="px-4 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold transition border border-slate-200"
              >
                Keep Booking
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Refund Tracker Modal */}
      {viewingRefundBooking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-md rounded-2xl bg-white border border-slate-200 p-6 shadow-2xl space-y-5">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <RotateCcw className="w-4 h-4 text-emerald-600" />
                <h3 className="text-base font-bold text-primary">Refund Status & Timeline</h3>
              </div>
              <button
                type="button"
                onClick={() => setViewingRefundBooking(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3">
              <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200 space-y-1.5 text-xs">
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Booking Reference:</span>
                  <span className="font-mono font-bold text-primary">{viewingRefundBooking.bookingReference}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Total Booking Amount:</span>
                  <span className="font-bold text-primary">₹{viewingRefundBooking.totalAmount.toLocaleString()}</span>
                </div>
                {refundInfo && (
                  <>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-500">Refund Amount:</span>
                      <span className="font-black text-emerald-600">₹{refundInfo.amount.toLocaleString()}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-500">Refund Status:</span>
                      <span className="font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                        {refundInfo.status}
                      </span>
                    </div>
                  </>
                )}
              </div>

              {/* Status Timeline */}
              <div className="space-y-3 pt-2 text-xs">
                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center font-bold text-[10px]">
                    ✓
                  </div>
                  <div>
                    <strong className="text-primary block">Booking Cancelled</strong>
                    <span className="text-slate-400 text-[11px]">Seat inventory released</span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center font-bold text-[10px]">
                    ✓
                  </div>
                  <div>
                    <strong className="text-primary block">Refund Triggered</strong>
                    <span className="text-slate-400 text-[11px]">Processed via payment gateway</span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 rounded-full bg-secondary/20 text-secondary flex items-center justify-center font-bold text-[10px]">
                    ●
                  </div>
                  <div>
                    <strong className="text-primary block">Bank Settlement</strong>
                    <span className="text-slate-400 text-[11px]">Estimated in 1–3 business days</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="pt-2">
              <button
                type="button"
                onClick={() => setViewingRefundBooking(null)}
                className="w-full py-2 rounded-xl bg-primary text-white text-xs font-bold"
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
