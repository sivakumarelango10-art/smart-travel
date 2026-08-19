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
    <div className="space-y-8 py-4 max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 flex items-center justify-center">
            <BookmarkCheck className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-2xl font-black text-white">My Flight Bookings</h1>
            <p className="text-xs text-slate-400">View upcoming itineraries, boarding passes, and refund statuses</p>
          </div>
        </div>

        <button
          onClick={fetchBookings}
          disabled={loading}
          className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-200 text-xs font-semibold flex items-center gap-1.5 border border-slate-700 transition"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-sky-400' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-800/80 pb-2 overflow-x-auto">
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
            className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 shrink-0 ${
              activeTab === tab.id
                ? 'bg-sky-500 text-white shadow-lg shadow-sky-500/20'
                : 'bg-slate-900 text-slate-400 hover:text-white hover:bg-slate-800'
            }`}
          >
            <span>{tab.label}</span>
            <span
              className={`px-1.5 py-0.2 rounded-full text-[10px] ${
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
        <div className="py-16 text-center space-y-3">
          <div className="w-10 h-10 border-4 border-sky-500/30 border-t-sky-500 rounded-full animate-spin mx-auto"></div>
          <p className="text-xs text-slate-400 font-medium">Fetching your bookings from database...</p>
        </div>
      ) : error ? (
        <div className="p-8 rounded-2xl bg-slate-900 border border-slate-800 text-center space-y-3">
          <p className="text-xs text-rose-400 font-medium">{error}</p>
          <button
            onClick={fetchBookings}
            className="px-4 py-2 rounded-xl bg-slate-800 text-slate-200 text-xs font-semibold"
          >
            Retry
          </button>
        </div>
      ) : filteredBookings.length === 0 ? (
        <div className="p-12 rounded-3xl bg-slate-900/60 border border-slate-800 text-center space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center mx-auto">
            <Plane className="w-8 h-8 transform -rotate-45" />
          </div>
          <h3 className="font-bold text-white text-lg">No Bookings in this Category</h3>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">
            You don't have any reservations under "{activeTab.toLowerCase()}". Ready to plan your next journey?
          </p>
          <Link
            to="/flights"
            className="inline-block px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white font-bold text-xs shadow-lg shadow-sky-500/20"
          >
            Book a Flight
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
                className="p-6 rounded-3xl bg-slate-900/90 border border-slate-800 hover:border-slate-700 shadow-xl space-y-5 transition duration-150"
              >
                {/* Header */}
                <div className="flex flex-wrap items-center justify-between gap-3 pb-4 border-b border-slate-800">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-500/20 to-indigo-500/20 border border-sky-500/30 flex items-center justify-center">
                      <Plane className="w-5 h-5 text-sky-400" />
                    </div>
                    <div>
                      <h3 className="font-bold text-white text-base">{b.airline}</h3>
                      <p className="text-xs text-slate-400 font-mono">
                        Flight {b.flightNumber} • PNR:{' '}
                        <strong className="text-sky-400">{b.bookingReference}</strong>
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-bold border ${
                        isCheckedIn
                          ? 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20'
                          : isConfirmed
                          ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                          : isCancelled
                          ? 'bg-rose-500/10 text-rose-400 border-rose-500/20'
                          : 'bg-slate-800 text-slate-400 border-slate-700'
                      }`}
                    >
                      {b.status}
                    </span>
                  </div>
                </div>

                {/* Timings & Route */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 items-center">
                  <div>
                    <p className="text-xl font-extrabold text-white">
                      {new Date(b.departureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </p>
                    <p className="text-xs font-bold text-sky-400">{b.departureAirport.code}</p>
                    <p className="text-[11px] text-slate-400">{b.departureAirport.city}</p>
                    <p className="text-[10px] text-slate-500">{new Date(b.departureTime).toLocaleDateString()}</p>
                  </div>

                  <div className="text-center">
                    <span className="text-[11px] text-slate-400">{b.durationMinutes} mins</span>
                    <div className="w-full flex items-center my-1.5">
                      <div className="h-0.5 w-full bg-slate-700 relative">
                        <div className="absolute -top-1 left-1/2 transform -translate-x-1/2 w-2 h-2 rounded-full bg-sky-400"></div>
                      </div>
                    </div>
                    <span className="text-[10px] text-slate-500">{b.cabinClass.replace('_', ' ')}</span>
                  </div>

                  <div className="text-left sm:text-right">
                    <p className="text-xl font-extrabold text-white">
                      {new Date(b.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </p>
                    <p className="text-xs font-bold text-sky-400">{b.arrivalAirport.code}</p>
                    <p className="text-[11px] text-slate-400">{b.arrivalAirport.city}</p>
                    <p className="text-[10px] text-slate-500">{new Date(b.arrivalTime).toLocaleDateString()}</p>
                  </div>
                </div>

                {/* Passengers List */}
                <div className="flex flex-wrap items-center gap-2 pt-2">
                  {b.passengers.map((p, idx) => (
                    <span
                      key={idx}
                      className="px-2.5 py-1 rounded-lg bg-slate-950 border border-slate-800 text-[11px] text-slate-300 font-medium flex items-center gap-1.5"
                    >
                      <span>
                        {p.firstName} {p.lastName}
                      </span>
                      {p.seatNumber && <span className="text-sky-400 font-mono font-bold">({p.seatNumber})</span>}
                    </span>
                  ))}
                </div>

                {/* Action Footer */}
                <div className="pt-4 border-t border-slate-800 flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <span className="text-[11px] text-slate-400 block">Total Fare</span>
                    <span className="text-lg font-black text-white">
                      ₹{b.totalAmount.toLocaleString('en-IN')}
                    </span>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    {/* Ticket Link / PDF */}
                    {b.ticketId && (
                      <Link
                        to={`/ticket/${b.id}`}
                        className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-200 text-xs font-semibold flex items-center gap-1.5 border border-slate-700 transition"
                      >
                        <FileText className="w-3.5 h-3.5 text-sky-400" />
                        <span>View Ticket</span>
                      </Link>
                    )}

                    {/* Online Check-in / Boarding Pass */}
                    {isConfirmed && (
                      <Link
                        to={`/check-in/${b.id}`}
                        className="px-4 py-2 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white text-xs font-bold flex items-center gap-1.5 shadow-md shadow-sky-500/20 transition"
                      >
                        <span>Online Check-In</span>
                        <ArrowRight className="w-4 h-4" />
                      </Link>
                    )}

                    {isCheckedIn && (
                      <Link
                        to={`/boarding-pass/${b.id}`}
                        className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold flex items-center gap-1.5 shadow-md shadow-indigo-500/20 transition"
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
                        className="px-3.5 py-2 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/20 text-xs font-semibold transition"
                      >
                        Cancel Flight
                      </button>
                    )}

                    {/* View Refund Details if Cancelled */}
                    {isCancelled && (
                      <button
                        type="button"
                        onClick={() => handleViewRefund(b)}
                        className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold flex items-center gap-1.5 border border-slate-700 transition"
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
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md">
          <div className="w-full max-w-md rounded-3xl bg-slate-900 border border-slate-800 p-6 sm:p-8 shadow-2xl space-y-5">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/20 flex items-center justify-center mx-auto">
              <XCircle className="w-6 h-6" />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-xl font-bold text-white">Cancel Flight Reservation</h3>
              <p className="text-xs text-slate-400">
                Are you sure you want to cancel booking PNR{' '}
                <strong className="text-white">{cancellingBooking.bookingReference}</strong>?
              </p>
            </div>

            <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 text-xs space-y-2 text-slate-300">
              <p className="font-semibold text-white">Cancellation Terms:</p>
              <p>• Seats will be released back into public airline inventory immediately.</p>
              <p>• Eligible refund will be automatically processed via Razorpay gateway.</p>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-medium text-slate-300">Reason for Cancellation</label>
              <input
                type="text"
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-sky-500 transition"
              />
            </div>

            <div className="flex items-center gap-3 pt-2">
              <button
                type="button"
                disabled={cancelLoading}
                onClick={handleCancelBooking}
                className="flex-1 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs shadow-lg shadow-rose-600/20 transition disabled:opacity-50"
              >
                {cancelLoading ? 'Cancelling...' : 'Confirm Cancellation'}
              </button>

              <button
                type="button"
                onClick={() => setCancellingBooking(null)}
                className="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold transition"
              >
                Keep Booking
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Refund Information Modal */}
      {viewingRefundBooking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md">
          <div className="w-full max-w-md rounded-3xl bg-slate-900 border border-slate-800 p-6 sm:p-8 shadow-2xl space-y-5">
            <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center mx-auto">
              <RotateCcw className="w-6 h-6" />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-xl font-bold text-white">Refund Statement</h3>
              <p className="text-xs text-slate-400">PNR: {viewingRefundBooking.bookingReference}</p>
            </div>

            {refundInfo ? (
              <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 text-xs space-y-2.5 text-slate-300">
                <div className="flex justify-between">
                  <span>Refund Reference:</span>
                  <span className="font-mono font-bold text-sky-400">{refundInfo.refundNumber}</span>
                </div>
                <div className="flex justify-between">
                  <span>Status:</span>
                  <span className="font-bold text-emerald-400">{refundInfo.status}</span>
                </div>
                <div className="flex justify-between">
                  <span>Refund Amount:</span>
                  <span className="font-bold text-white">₹{refundInfo.amount.toLocaleString('en-IN')}</span>
                </div>
                <div className="flex justify-between">
                  <span>Gateway Ref:</span>
                  <span className="font-mono text-slate-400">{refundInfo.gatewayRefundId || 'Direct Bank Credit'}</span>
                </div>
                <div className="flex justify-between">
                  <span>Processed On:</span>
                  <span>{new Date(refundInfo.createdAt).toLocaleString()}</span>
                </div>
              </div>
            ) : (
              <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 text-xs text-center text-slate-400">
                Refund record is being processed by the finance queue. Please allow 1-2 business days.
              </div>
            )}

            <button
              type="button"
              onClick={() => setViewingRefundBooking(null)}
              className="w-full py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold transition"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
