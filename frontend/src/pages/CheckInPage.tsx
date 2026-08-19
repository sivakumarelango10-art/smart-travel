import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  CheckCircle2,
  Plane,
  ArrowRight,
  AlertCircle
} from 'lucide-react';
import { Booking, CheckInDetails } from '../types/api';
import { bookingService } from '../services/bookingService';
import { checkInService } from '../services/checkInService';

export const CheckInPage: React.FC = () => {
  const { bookingId } = useParams<{ bookingId: string }>();
  const navigate = useNavigate();

  const [booking, setBooking] = useState<Booking | null>(null);
  const [checkInDetails, setCheckInDetails] = useState<CheckInDetails | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [checkingIn, setCheckingIn] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const initData = async () => {
      if (!bookingId) return;
      try {
        setLoading(true);
        setError(null);
        const [bkgRes, chkRes] = await Promise.all([
          bookingService.getBookingById(bookingId),
          checkInService.getCheckInDetails(bookingId).catch(() => ({ success: false, data: null })),
        ]);

        if (bkgRes.success && bkgRes.data) {
          setBooking(bkgRes.data);
        }
        if (chkRes.success && chkRes.data) {
          setCheckInDetails(chkRes.data);
        }
      } catch (err: any) {
        setError(err?.message || 'Failed to load check-in details');
      } finally {
        setLoading(false);
      }
    };
    initData();
  }, [bookingId]);

  const handlePerformCheckIn = async () => {
    if (!bookingId) return;
    try {
      setCheckingIn(true);
      setError(null);
      const res = await checkInService.checkIn(bookingId, []);
      if (res.success && res.data) {
        setCheckInDetails(res.data);
        navigate(`/boarding-pass/${bookingId}`);
      }
    } catch (err: any) {
      setError(err?.message || 'Check-in failed. Please verify booking status.');
    } finally {
      setCheckingIn(false);
    }
  };

  if (loading) {
    return (
      <div className="py-20 flex flex-col items-center justify-center gap-3">
        <div className="w-10 h-10 border-4 border-sky-500/30 border-t-sky-500 rounded-full animate-spin"></div>
        <p className="text-sm text-slate-400 font-medium">Validating check-in eligibility...</p>
      </div>
    );
  }

  if (error && !booking) {
    return (
      <div className="max-w-md mx-auto py-16 text-center">
        <div className="rounded-2xl bg-slate-900 border border-slate-800 p-8 shadow-xl space-y-4">
          <h2 className="text-xl font-bold text-white">Check-In Unavailable</h2>
          <p className="text-sm text-slate-400">{error}</p>
          <Link
            to="/my-bookings"
            className="inline-block px-4 py-2 rounded-lg bg-slate-800 text-slate-200 text-xs font-semibold"
          >
            Back to My Bookings
          </Link>
        </div>
      </div>
    );
  }

  const isAlreadyCheckedIn = booking?.status === 'CHECKED_IN' || !!checkInDetails;

  return (
    <div className="max-w-3xl mx-auto py-8 space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3 pb-4 border-b border-slate-800">
        <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-sky-500/20 to-indigo-500/20 border border-sky-500/30 flex items-center justify-center text-sky-400">
          <Plane className="w-6 h-6" />
        </div>
        <div>
          <h1 className="text-2xl font-black text-white">Online Flight Check-In</h1>
          <p className="text-xs text-slate-400">
            PNR: <strong className="text-sky-400 font-mono">{booking?.bookingReference}</strong> • {booking?.airline} ({booking?.flightNumber})
          </p>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-medium flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Flight Information Card */}
      <div className="rounded-3xl bg-slate-900/90 border border-slate-800 p-6 shadow-2xl space-y-6">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 items-center">
          <div>
            <span className="text-[10px] text-slate-500 font-bold uppercase">Origin</span>
            <p className="text-xl font-extrabold text-white">{booking?.departureAirport.code}</p>
            <p className="text-xs text-slate-400">{booking?.departureAirport.city}</p>
            <p className="text-xs text-slate-500 mt-1">
              {booking?.departureTime && new Date(booking.departureTime).toLocaleString()}
            </p>
          </div>

          <div className="text-center">
            <span className="text-xs text-slate-400">{booking?.durationMinutes} mins</span>
            <div className="w-full flex items-center my-1.5">
              <div className="h-0.5 w-full bg-slate-700 relative">
                <div className="absolute -top-1 left-1/2 transform -translate-x-1/2 w-2 h-2 rounded-full bg-sky-400"></div>
              </div>
            </div>
            <span className="text-[10px] text-emerald-400 font-bold uppercase">Ready for Check-In</span>
          </div>

          <div className="text-left sm:text-right">
            <span className="text-[10px] text-slate-500 font-bold uppercase">Destination</span>
            <p className="text-xl font-extrabold text-white">{booking?.arrivalAirport.code}</p>
            <p className="text-xs text-slate-400">{booking?.arrivalAirport.city}</p>
            <p className="text-xs text-slate-500 mt-1">
              {booking?.arrivalTime && new Date(booking.arrivalTime).toLocaleString()}
            </p>
          </div>
        </div>

        {/* Passengers & Assigned Seats */}
        <div className="space-y-3 pt-4 border-t border-slate-800">
          <h3 className="font-bold text-white text-sm">Travelers for Check-In</h3>
          <div className="space-y-2">
            {booking?.passengers.map((pax, idx) => (
              <div
                key={idx}
                className="p-4 rounded-xl bg-slate-950/70 border border-slate-800 flex items-center justify-between text-xs"
              >
                <div>
                  <p className="font-bold text-white">
                    {pax.title} {pax.firstName} {pax.lastName}
                  </p>
                  <p className="text-[11px] text-slate-400">{booking.cabinClass.replace('_', ' ')}</p>
                </div>

                <div className="flex items-center gap-2">
                  <span className="font-mono font-bold text-sky-400 bg-sky-950/60 px-3 py-1 rounded-lg border border-sky-800/40">
                    Seat: {pax.seatNumber || 'Assigned'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Action Button */}
        <div className="pt-4 border-t border-slate-800">
          {isAlreadyCheckedIn ? (
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-2 text-emerald-400 text-xs font-semibold">
                <CheckCircle2 className="w-5 h-5" />
                <span>Check-In already completed for this reservation.</span>
              </div>
              <Link
                to={`/boarding-pass/${booking?.id}`}
                className="px-6 py-3 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white font-bold text-xs shadow-lg shadow-sky-500/20 flex items-center gap-2 transition"
              >
                <span>View Boarding Pass</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          ) : (
            <button
              type="button"
              disabled={checkingIn}
              onClick={handlePerformCheckIn}
              className="w-full py-3.5 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-bold text-sm shadow-xl shadow-emerald-500/25 transition flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {checkingIn ? (
                <span className="flex items-center gap-2">
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                  Issuing Boarding Passes...
                </span>
              ) : (
                <>
                  <CheckCircle2 className="w-5 h-5" />
                  <span>Confirm Check-In & Generate Boarding Pass</span>
                </>
              )}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
