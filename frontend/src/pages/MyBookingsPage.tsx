import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  BookmarkCheck,
  Plane,
  RotateCcw,
  ArrowRight,
  RefreshCw,
  XCircle,
  FileText
} from 'lucide-react';
import { Booking, RefundDetails } from '../types/api';
import { bookingService } from '../services/bookingService';
import { paymentService } from '../services/paymentService';

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
    if (activeTab === 'UPCOMING') {
      return b.status === 'CONFIRMED' || b.status === 'CHECKED_IN';
    }
    if (activeTab === 'COMPLETED') {
      return b.status === 'COMPLETED';
    }
    if (activeTab === 'CANCELLED') {
      return b.status === 'CANCELLED' || b.status === 'EXPIRED';
    }
    return true;
  });

  return (
    <div className="space-y-8 py-4 max-w-5xl mx-auto animate-fade-in">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div className="flex items-center gap-3.5">
          <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 flex items-center justify-center font-black">
            <BookmarkCheck className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">My Flight Bookings</h1>
            <p className="text-xs text-slate-400 mt-0.5">Manage upcoming itineraries, boarding passes, and refund claims</p>
          </div>
        </div>

        <button
          onClick={fetchBookings}
          disabled={loading}
          className="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-200 text-xs font-bold flex items-center gap-2 border border-slate-700 transition shadow-sm"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-sky-400' : ''}`} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-800/80 pb-3 overflow-x-auto">
        {[
          { id: 'ALL', label: 'All Trips', count: bookings.length },
          {
            id: 'UPCOMING',
            label: 'Upcoming',
            count: bookings.filter((b) => ['CONFIRMED', 'CHECKED_IN'].includes(b.status)).length,
          },
          {
            id: 'COMPLETED',
            label: 'Completed',
            count: bookings.filter((b) => b.status === 'COMPLETED').length,
          },
          {
            id: 'CANCELLED',
            label: 'Cancelled / Expired',
            count: bookings.filter((b) => ['CANCELLED', 'EXPIRED'].includes(b.status)).length,
          },
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as any)}
            className={`px-4 py-2 rounded-2xl text-xs font-bold transition flex items-center gap-2 shrink-0 ${
              activeTab === tab.id
                ? 'bg-sky-500 text-white shadow-lg shadow-sky-500/25'
                : 'bg-slate-900 text-slate-400 hover:text-white hover:bg-slate-800 border border-slate-800'
            }`}
          >
            <span>{tab.label}</span>
            <span
              className={`px-2 py-0.5 rounded-full text-[10px] font-black ${
                activeTab === tab.id ? 'bg-white/20 text-white' : 'bg-slate-800 text-slate-400'
              }`}
            >
              {tab.count}
            </span>
          </button>
        ))}
      </div>

      {/* Bookings List */}
      {loading ? (
        <div className="py-24 text-center space-y-4">
          <div className="w-12 h-12 border-4 border-sky-500/30 border-t-sky-500 rounded-full animate-spin mx-auto"></div>
          <p className="text-xs text-slate-400 font-bold">Synchronizing your booking records...</p>
        </div>
      ) : error ? (
        <div className="p-8 rounded-3xl bg-slate-900 border border-slate-800 text-center space-y-3 shadow-xl">
          <p className="text-xs text-rose-400 font-semibold">{error}</p>
          <button
            onClick={fetchBookings}
            className="px-5 py-2.5 rounded-xl bg-slate-800 text-slate-200 text-xs font-bold transition"
          >
            Retry
          </button>
        </div>
      ) : filteredBookings.length === 0 ? (
        <div className="p-12 sm:p-16 rounded-3xl bg-slate-900/60 border border-slate-800 text-center space-y-4 shadow-xl">
          <div className="w-16 h-16 rounded-3xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center mx-auto shadow-lg">
            <Plane className="w-8 h-8 transform -rotate-45" />
          </div>
          <div className="space-y-1">
            <h3 className="font-extrabold text-white text-lg">No Bookings Found</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              You don't have any reservations under "{activeTab.toLowerCase()}". Ready to plan your next journey?
            </p>
          </div>
          <div className="pt-2">
            <Link
              to="/flights"
              className="inline-block px-6 py-3 rounded-2xl bg-gradient-to-r from-sky-500 via-indigo-500 to-blue-600 hover:from-sky-400 hover:via-indigo-400 hover:to-blue-500 text-white font-black text-xs shadow-xl shadow-sky-500/25 transition-all"
            >
              Book a Flight
            </Link>
          </div>
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
                className="p-6 sm:p-7 rounded-3xl bg-slate-900/90 border border-slate-800 hover:border-slate-700 shadow-2xl space-y-5 transition-all duration-200 backdrop-blur-xl"
              >
                {/* Header */}
                <div className="flex flex-wrap items-center justify-between gap-3 pb-4 border-b border-slate-800">
                  <div className="flex items-center gap-3.5">
                    <div className="w-11 h-11 rounded-2xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center font-black">
                      <Plane className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="font-extrabold text-white text-base leading-tight">{b.airline}</h3>
                      <p className="text-xs text-slate-400 font-mono mt-0.5">
                        Flight {b.flightNumber} • PNR:{' '}
                        <strong className="text-sky-400 font-black">{b.bookingReference}</strong>
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-black border ${
                        isCheckedIn
                          ? 'bg-indigo-500/15 text-indigo-400 border-indigo-500/30'
                          : isConfirmed
                          ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30'
                          : isCancelled
                          ? 'bg-rose-500/15 text-rose-400 border-rose-500/30'
                          : 'bg-slate-800 text-slate-400 border-slate-700'
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
                    <p className="text-sm font-bold text-sky-400 mt-1">{b.departureAirport.code}</p>
                    <p className="text-xs text-slate-300">{b.departureAirport.city}</p>
                    <p className="text-[11px] text-slate-500 mt-0.5">{new Date(b.departureTime).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</p>
                  </div>

                  <div className="flex flex-col items-center px-2">
                    <span className="text-xs text-slate-400 font-bold">
                      {Math.floor(b.durationMinutes / 60)}h {b.durationMinutes % 60 > 0 ? `${b.durationMinutes % 60}m` : '00m'}
                    </span>
                    <div className="w-full flex items-center my-2">
                      <div className="h-0.5 w-full bg-slate-700/80 relative">
                        <div className="absolute -top-1.5 left-1/2 transform -translate-x-1/2 w-3 h-3 rounded-full bg-sky-400 flex items-center justify-center">
                          <Plane className="w-2 h-2 text-slate-950 transform rotate-45" />
                        </div>
                      </div>
                    </div>
                    <span className="text-[10px] font-semibold text-emerald-400 uppercase tracking-wider px-2 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/20">
                      {b.cabinClass.replace('_', ' ')}
                    </span>
                  </div>

                  <div className="text-left sm:text-right">
                    <p className="text-2xl font-black text-white whitespace-nowrap">
                      {new Date(b.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}
                    </p>
                    <p className="text-sm font-bold text-sky-400 mt-1">{b.arrivalAirport.code}</p>
                    <p className="text-xs text-slate-300">{b.arrivalAirport.city}</p>
                    <p className="text-[11px] text-slate-500 mt-0.5">{new Date(b.arrivalTime).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</p>
                  </div>
                </div>

                {/* Passengers List */}
                <div className="flex flex-wrap items-center gap-2 pt-2">
                  {b.passengers.map((p, idx) => (
                    <span
                      key={idx}
                      className="px-3 py-1.5 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-300 font-medium flex items-center gap-1.5"
                    >
                      <span className="font-bold text-white">
                        {p.firstName} {p.lastName}
                      </span>
                      {p.seatNumber && (
                        <span className="text-sky-400 font-mono font-black bg-sky-950/60 px-1.5 py-0.2 rounded border border-sky-800/40">
                          {p.seatNumber}
                        </span>
                      )}
                    </span>
                  ))}
                </div>

                {/* Action Footer */}
                <div className="pt-4 border-t border-slate-800 flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <span className="text-[11px] text-slate-400 block font-semibold">Total Fare</span>
                    <span className="text-2xl font-black text-white tracking-tight">
                      ₹{b.totalAmount.toLocaleString('en-IN')}
                    </span>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    {/* Ticket Link / PDF */}
                    {b.ticketId && (
                      <Link
                        to={`/ticket/${b.id}`}
                        className="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-200 text-xs font-bold flex items-center gap-1.5 border border-slate-700 transition shadow-sm"
                      >
                        <FileText className="w-3.5 h-3.5 text-sky-400" />
                        <span>View Ticket</span>
                      </Link>
                    )}

                    {/* Online Check-in / Boarding Pass */}
                    {isConfirmed && (
                      <Link
                        to={`/check-in/${b.id}`}
                        className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white text-xs font-black flex items-center gap-1.5 shadow-lg shadow-sky-500/20 transition"
                      >
                        <span>Online Check-In</span>
                        <ArrowRight className="w-4 h-4" />
                      </Link>
                    )}

                    {isCheckedIn && (
                      <Link
                        to={`/boarding-pass/${b.id}`}
                        className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white text-xs font-black flex items-center gap-1.5 shadow-lg shadow-indigo-500/20 transition"
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
                        className="px-4 py-2.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/20 text-xs font-bold transition"
                      >
                        Cancel Flight
                      </button>
                    )}

                    {/* View Refund Details if Cancelled */}
                    {isCancelled && (
                      <button
                        type="button"
                        onClick={() => handleViewRefund(b)}
                        className="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-300 hover:text-white text-xs font-bold flex items-center gap-1.5 border border-slate-700 transition"
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
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fade-in">
          <div className="w-full max-w-md rounded-3xl bg-slate-900 border border-slate-800 p-6 sm:p-8 shadow-2xl space-y-5">
            <div className="w-14 h-14 rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/20 flex items-center justify-center mx-auto">
              <XCircle className="w-8 h-8" />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-xl font-black text-white">Cancel Flight Reservation</h3>
              <p className="text-xs text-slate-400">
                Are you sure you want to cancel booking PNR{' '}
                <strong className="text-white font-mono">{cancellingBooking.bookingReference}</strong>?
              </p>
            </div>

            <div className="p-4 rounded-2xl bg-slate-950 border border-slate-800 text-xs space-y-2 text-slate-300">
              <p className="font-bold text-white">Cancellation Policy & Terms:</p>
              <p>• Seats will be released back into public airline inventory immediately.</p>
              <p>• Eligible refund will be automatically triggered via Razorpay gateway.</p>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-300">Reason for Cancellation</label>
              <input
                type="text"
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-sky-500 transition font-medium"
              />
            </div>

            <div className="flex items-center gap-3 pt-2">
              <button
                type="button"
                disabled={cancelLoading}
                onClick={handleCancelBooking}
                className="flex-1 py-3 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-black text-xs shadow-lg shadow-rose-600/25 transition disabled:opacity-50"
              >
                {cancelLoading ? 'Cancelling...' : 'Confirm Cancellation'}
              </button>

              <button
                type="button"
                onClick={() => setCancellingBooking(null)}
                className="px-5 py-3 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-300 text-xs font-bold transition border border-slate-700"
              >
                Keep Booking
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Refund Information Modal */}
      {viewingRefundBooking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fade-in">
          <div className="w-full max-w-md rounded-3xl bg-slate-900 border border-slate-800 p-6 sm:p-8 shadow-2xl space-y-5">
            <div className="w-14 h-14 rounded-2xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center mx-auto">
              <RotateCcw className="w-7 h-7" />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-xl font-black text-white">Refund Statement</h3>
              <p className="text-xs text-slate-400 font-mono">PNR: {viewingRefundBooking.bookingReference}</p>
            </div>

            {refundInfo ? (
              <div className="p-4 rounded-2xl bg-slate-950 border border-slate-800 text-xs space-y-2.5 text-slate-300">
                <div className="flex justify-between">
                  <span className="text-slate-400">Refund Reference:</span>
                  <span className="font-mono font-bold text-sky-400">{refundInfo.refundNumber}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Status:</span>
                  <span className="font-black text-emerald-400">{refundInfo.status}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Refund Amount:</span>
                  <span className="font-black text-white text-sm">₹{refundInfo.amount.toLocaleString('en-IN')}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Gateway Ref:</span>
                  <span className="font-mono text-slate-300">{refundInfo.gatewayRefundId || 'Direct Bank Credit'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Processed On:</span>
                  <span>{new Date(refundInfo.createdAt).toLocaleString()}</span>
                </div>
              </div>
            ) : (
              <div className="p-4 rounded-2xl bg-slate-950 border border-slate-800 text-xs text-center text-slate-400">
                Refund record is being processed by the automated gateway queue. Please allow 1-2 business days.
              </div>
            )}

            <button
              type="button"
              onClick={() => setViewingRefundBooking(null)}
              className="w-full py-3 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-200 text-xs font-bold transition border border-slate-700"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

