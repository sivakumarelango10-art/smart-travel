import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  CheckCircle2,
  Plane,
  ArrowRight,
  AlertCircle,
  Users
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
      <div className="py-24 flex flex-col items-center justify-center gap-4">
        <div className="w-12 h-12 border-4 border-amber-400/30 border-t-amber-400 rounded-full animate-spin"></div>
        <p className="text-sm text-slate-400 font-bold">Validating online check-in eligibility & flight status...</p>
      </div>
    );
  }

  if (error && !booking) {
    return (
      <div className="max-w-md mx-auto py-20 text-center">
        <div className="rounded-3xl bg-[#14161F] border border-white/10 p-8 shadow-2xl space-y-4">
          <h2 className="text-xl font-extrabold text-white">Check-In Unavailable</h2>
          <p className="text-xs text-slate-400">{error}</p>
          <Link
            to="/my-bookings"
            className="inline-block px-5 py-2.5 rounded-xl bg-[#181A22] text-slate-200 hover:bg-[#1F222E] text-xs font-bold transition border border-white/10"
          >
            Back to My Bookings
          </Link>
        </div>
      </div>
    );
  }

  const isAlreadyCheckedIn = booking?.status === 'CHECKED_IN' || Boolean(booking?.checkedIn) || Boolean(booking?.checkInNumber) || !!checkInDetails;

  return (
    <div className="max-w-3xl mx-auto py-8 space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center gap-4 pb-4 border-b border-white/10">
        <div className="w-12 h-12 rounded-2xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center font-black shadow-glow-gold">
          <Plane className="w-6 h-6 transform -rotate-45" />
        </div>
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">Online Flight Check-In</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            PNR: <strong className="text-amber-400 font-mono">{booking?.bookingReference}</strong> • {booking?.airline} ({booking?.flightNumber})
          </p>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-semibold flex items-center gap-2.5">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Flight Information Card */}
      <div className="rounded-3xl bg-[#14161F] border border-white/10 p-6 sm:p-8 shadow-2xl space-y-6 backdrop-blur-xl">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 items-center text-center sm:text-left">
          <div>
            <span className="text-[10px] text-slate-400 font-black uppercase tracking-wider">Origin</span>
            <p className="text-2xl sm:text-3xl font-black text-white mt-1">{booking?.departureAirport.code}</p>
            <p className="text-xs text-slate-300">{booking?.departureAirport.city}</p>
            <p className="text-[11px] text-slate-400 mt-1">
              {booking?.departureTime && new Date(booking.departureTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
            </p>
          </div>

          <div className="text-center flex flex-col items-center">
            <span className="text-xs text-slate-400 font-bold">{booking?.durationMinutes} mins</span>
            <div className="w-full flex items-center my-2">
              <div className="h-0.5 w-full bg-white/10 relative">
                <div className="absolute -top-1.5 left-1/2 transform -translate-x-1/2 w-3 h-3 rounded-full bg-amber-400 flex items-center justify-center shadow-glow-gold">
                  <Plane className="w-2 h-2 text-black transform rotate-45" />
                </div>
              </div>
            </div>
            <span className="text-[10px] text-amber-400 font-black uppercase tracking-wider px-2.5 py-0.5 rounded-full bg-amber-400/10 border border-amber-400/20">
              Check-In Open
            </span>
          </div>

          <div className="text-center sm:text-right">
            <span className="text-[10px] text-slate-400 font-black uppercase tracking-wider">Destination</span>
            <p className="text-2xl sm:text-3xl font-black text-white mt-1">{booking?.arrivalAirport.code}</p>
            <p className="text-xs text-slate-300">{booking?.arrivalAirport.city}</p>
            <p className="text-[11px] text-slate-400 mt-1">
              {booking?.arrivalTime && new Date(booking.arrivalTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
            </p>
          </div>
        </div>

        {/* Passengers & Assigned Seats */}
        <div className="space-y-4 pt-6 border-t border-white/10">
          <div className="flex items-center justify-between">
            <h3 className="font-extrabold text-white text-base flex items-center gap-2">
              <Users className="w-4 h-4 text-amber-400" />
              <span>Travelers & Seat Verification</span>
            </h3>
            <span className="text-[11px] text-slate-400">
              Please review traveler details before final confirmation
            </span>
          </div>

          <div className="space-y-2.5">
            {booking?.passengers.map((pax, idx) => (
              <div
                key={idx}
                className="p-4 rounded-2xl bg-[#181A22] border border-white/10 flex items-center justify-between text-xs"
              >
                <div>
                  <p className="font-extrabold text-white text-sm">
                    {pax.title ? `${pax.title} ` : ''}{pax.firstName} {pax.lastName}
                  </p>
                  <p className="text-[11px] text-slate-400 mt-0.5 uppercase tracking-wider">
                    {booking.cabinClass.replace('_', ' ')}
                  </p>
                </div>

                <div className="flex items-center gap-2">
                  <span className="font-mono font-black text-amber-400 bg-amber-400/10 px-3 py-1.5 rounded-xl border border-amber-400/30">
                    Seat: {pax.seatNumber || 'Auto-Assigned on Confirm'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Action Button */}
        <div className="pt-6 border-t border-white/10">
          {isAlreadyCheckedIn ? (
            <div className="flex flex-wrap items-center justify-between gap-4 p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/20">
              <div className="flex items-center gap-2 text-emerald-400 text-xs font-bold">
                <CheckCircle2 className="w-5 h-5" />
                <span>Check-In already completed for this reservation.</span>
              </div>
              <Link
                to={`/boarding-pass/${booking?.id}`}
                className="px-6 py-3 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-white font-black text-xs shadow-lg shadow-emerald-500/20 flex items-center gap-2 transition"
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
              className="w-full py-4 rounded-2xl bg-gradient-to-r from-amber-400 via-amber-500 to-amber-600 hover:from-amber-300 hover:via-amber-400 hover:to-amber-500 text-black font-black text-sm shadow-glow-gold hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
            >
              {checkingIn ? (
                <span className="flex items-center gap-2">
                  <span className="w-4 h-4 border-2 border-black/30 border-t-black rounded-full animate-spin"></span>
                  Confirming Check-In & Issuing Digital Passes...
                </span>
              ) : (
                <>
                  <CheckCircle2 className="w-5 h-5 text-black" />
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

