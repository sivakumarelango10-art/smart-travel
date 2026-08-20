import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  CheckCircle2,
  Download,
  Plane,
  Armchair,
  Users,
  ArrowRight,
  BookmarkCheck,
  ShieldCheck,
  Clock
} from 'lucide-react';
import { Booking, Ticket } from '../types/api';
import { bookingService } from '../services/bookingService';
import { ticketService } from '../services/ticketService';
import { AirlineLogo } from '../components/AirlineLogo';

export const BookingConfirmationPage: React.FC = () => {
  const { bookingId } = useParams<{ bookingId: string }>();

  const [booking, setBooking] = useState<Booking | null>(null);
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [downloadingPdf, setDownloadingPdf] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchBookingAndTicket = async () => {
      if (!bookingId) return;
      try {
        setLoading(true);
        setError(null);
        const [bkgRes, tktRes] = await Promise.all([
          bookingService.getBookingById(bookingId),
          ticketService.getTicketByBookingId(bookingId).catch(() => ({ success: false, data: null })),
        ]);

        if (bkgRes.success && bkgRes.data) {
          setBooking(bkgRes.data);
        }
        if (tktRes.success && tktRes.data) {
          setTicket(tktRes.data);
        }
      } catch (err: any) {
        setError(err?.message || 'Failed to load booking confirmation details');
      } finally {
        setLoading(false);
      }
    };
    fetchBookingAndTicket();
  }, [bookingId]);

  const handleDownloadPdf = async () => {
    if (!ticket?.id) return;
    try {
      setDownloadingPdf(true);
      const blob = await ticketService.downloadTicketPdf(ticket.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Ticket_${booking?.bookingReference || 'SmartTravel'}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err: any) {
      alert('Failed to download PDF ticket: ' + (err?.message || 'Please try again.'));
    } finally {
      setDownloadingPdf(false);
    }
  };

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center gap-4">
        <div className="w-12 h-12 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
        <p className="text-sm text-slate-400 font-bold">Finalizing transaction & issuing official e-ticket...</p>
      </div>
    );
  }

  if (error || !booking) {
    return (
      <div className="max-w-md mx-auto py-20 text-center">
        <div className="rounded-3xl bg-slate-900 border border-slate-800 p-8 shadow-2xl space-y-4">
          <h2 className="text-xl font-extrabold text-white">Booking Not Found</h2>
          <p className="text-xs text-slate-400">{error || 'Unable to retrieve reservation details.'}</p>
          <Link
            to="/my-bookings"
            className="inline-block px-5 py-2.5 rounded-xl bg-slate-800 text-slate-200 hover:bg-slate-750 text-xs font-bold transition"
          >
            Go to My Bookings
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto py-8 space-y-8 animate-fade-in">
      {/* 1. SUCCESS HERO BANNER */}
      <div className="text-center space-y-4">
        <div className="w-20 h-20 rounded-3xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center mx-auto shadow-2xl shadow-emerald-500/20">
          <CheckCircle2 className="w-12 h-12" />
        </div>
        <div>
          <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight">Booking Confirmed!</h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1 max-w-md mx-auto">
            Your flight reservation is locked. An official e-ticket receipt has been issued.
          </p>
        </div>

        {/* PNR Banner */}
        <div className="inline-flex flex-wrap items-center justify-center gap-3 px-6 py-3 rounded-2xl bg-slate-900/90 border border-slate-800 shadow-xl backdrop-blur-xl">
          <span className="text-xs text-slate-400 uppercase font-black tracking-wider">Booking PNR:</span>
          <span className="font-mono text-xl sm:text-2xl font-black text-sky-400 tracking-wider">
            {booking.bookingReference}
          </span>
          {ticket?.ticketNumber && (
            <>
              <span className="text-slate-600 hidden sm:inline">•</span>
              <span className="text-xs text-slate-400 uppercase font-bold">Ticket:</span>
              <span className="font-mono text-sm font-black text-emerald-400">{ticket.ticketNumber}</span>
            </>
          )}
        </div>
      </div>

      {/* 2. MAIN TICKET SUMMARY CARD */}
      <div className="rounded-3xl bg-slate-900/90 border border-slate-800 shadow-2xl overflow-hidden backdrop-blur-xl">
        {/* Card Header */}
        <div className="p-6 sm:p-7 bg-gradient-to-r from-sky-950/60 via-slate-900 to-indigo-950/60 border-b border-slate-800 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3.5">
            <AirlineLogo airline={booking.airline} airlineCode={booking.airlineCode} size="lg" />
            <div>
              <h2 className="font-extrabold text-white text-lg leading-tight">{booking.airline}</h2>
              <div className="flex items-center gap-2 text-xs text-slate-400 mt-0.5">
                <span className="font-mono text-slate-300 font-bold bg-slate-950 px-2 py-0.5 rounded border border-slate-800">
                  {booking.flightNumber}
                </span>
                <span>•</span>
                <span>{booking.cabinClass.replace('_', ' ')}</span>
              </div>
            </div>
          </div>

          <span className="px-3.5 py-1.5 rounded-full bg-emerald-500/15 text-emerald-400 text-xs font-black border border-emerald-500/30 flex items-center gap-1.5">
            <ShieldCheck className="w-4 h-4" />
            <span>CONFIRMED & ISSUED</span>
          </span>
        </div>

        {/* Flight Route Timings */}
        <div className="p-6 sm:p-8 grid grid-cols-1 sm:grid-cols-3 gap-6 items-center text-center sm:text-left border-b border-slate-800">
          <div>
            <span className="text-[10px] uppercase font-black text-slate-500 tracking-wider">Departure</span>
            <p className="text-3xl font-black text-white mt-1">
              {new Date(booking.departureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </p>
            <p className="text-base font-bold text-sky-400 mt-0.5">{booking.departureAirport.code}</p>
            <p className="text-xs text-slate-300">{booking.departureAirport.city}</p>
            <p className="text-[11px] text-slate-500 mt-1">
              {new Date(booking.departureTime).toLocaleDateString('en-US', {
                weekday: 'short',
                month: 'short',
                day: 'numeric',
              })}
            </p>
          </div>

          <div className="flex flex-col items-center">
            <span className="text-xs text-slate-400 font-bold flex items-center gap-1">
              <Clock className="w-3.5 h-3.5 text-slate-500" />
              {booking.durationMinutes} mins
            </span>
            <div className="w-full flex items-center my-2">
              <div className="h-0.5 w-full bg-slate-700/80 relative">
                <div className="absolute -top-1.5 left-1/2 transform -translate-x-1/2 w-3 h-3 rounded-full bg-sky-400 flex items-center justify-center">
                  <Plane className="w-2 h-2 text-slate-950 transform rotate-45" />
                </div>
              </div>
            </div>
            <span className="text-[10px] text-emerald-400 font-black uppercase tracking-wider px-2.5 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/20">
              Confirmed Non-Stop
            </span>
          </div>

          <div className="text-center sm:text-right">
            <span className="text-[10px] uppercase font-black text-slate-500 tracking-wider">Arrival</span>
            <p className="text-3xl font-black text-white mt-1">
              {new Date(booking.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </p>
            <p className="text-base font-bold text-sky-400 mt-0.5">{booking.arrivalAirport.code}</p>
            <p className="text-xs text-slate-300">{booking.arrivalAirport.city}</p>
            <p className="text-[11px] text-slate-500 mt-1">
              {new Date(booking.arrivalTime).toLocaleDateString('en-US', {
                weekday: 'short',
                month: 'short',
                day: 'numeric',
              })}
            </p>
          </div>
        </div>

        {/* Passengers & Assigned Seats */}
        <div className="p-6 sm:p-7 space-y-4 border-b border-slate-800">
          <h3 className="font-extrabold text-white text-base flex items-center gap-2">
            <Users className="w-4 h-4 text-sky-400" />
            <span>Passenger & Seat Allocations</span>
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            {booking.passengers.map((pax, idx) => (
              <div
                key={idx}
                className="p-4 rounded-2xl bg-slate-950/80 border border-slate-800 flex justify-between items-center text-xs"
              >
                <div>
                  <p className="font-extrabold text-white text-sm">
                    {pax.title} {pax.firstName} {pax.lastName}
                  </p>
                  <p className="text-[11px] text-slate-400 mt-0.5">{pax.gender} • {pax.nationality || 'Indian'}</p>
                </div>
                <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-sky-950/60 text-sky-300 border border-sky-800/40 font-mono font-bold text-xs">
                  <Armchair className="w-3.5 h-3.5" />
                  <span>Seat {pax.seatNumber || 'Assigned'}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Total Paid & Next Action Buttons */}
        <div className="p-6 sm:p-7 bg-slate-950/60 flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-xs text-slate-400 font-semibold">Total Amount Paid</p>
            <p className="text-3xl font-black text-emerald-400 tracking-tight">
              ₹{booking.totalAmount.toLocaleString('en-IN')}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            {ticket?.id && (
              <button
                type="button"
                disabled={downloadingPdf}
                onClick={handleDownloadPdf}
                className="px-5 py-3 rounded-2xl bg-slate-800 hover:bg-slate-750 text-slate-200 text-xs font-bold flex items-center gap-2 border border-slate-700 transition shadow-md"
              >
                <Download className="w-4 h-4 text-sky-400" />
                <span>{downloadingPdf ? 'Downloading...' : 'Download PDF Ticket'}</span>
              </button>
            )}

            <Link
              to={`/check-in/${booking.id}`}
              className="px-6 py-3 rounded-2xl bg-gradient-to-r from-sky-500 via-indigo-500 to-blue-600 hover:from-sky-400 hover:via-indigo-400 hover:to-blue-500 text-white text-xs font-black flex items-center gap-2 shadow-xl shadow-sky-500/25 hover:scale-[1.02] active:scale-[0.98] transition-all"
            >
              <span>Online Check-In</span>
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </div>

      {/* Footer Navigation Link */}
      <div className="text-center">
        <Link
          to="/my-bookings"
          className="text-xs font-bold text-slate-400 hover:text-sky-400 inline-flex items-center gap-1.5 transition"
        >
          <BookmarkCheck className="w-4 h-4 text-indigo-400" />
          <span>View all upcoming trips in My Bookings</span>
        </Link>
      </div>
    </div>
  );
};

