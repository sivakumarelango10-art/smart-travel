import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  CheckCircle2,
  Download,
  Plane,
  Armchair,
  Users,
  ArrowRight,
  BookmarkCheck
} from 'lucide-react';
import { Booking, Ticket } from '../types/api';
import { bookingService } from '../services/bookingService';
import { ticketService } from '../services/ticketService';

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
      <div className="py-20 flex flex-col items-center justify-center gap-3">
        <div className="w-10 h-10 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
        <p className="text-sm text-slate-400 font-medium">Finalizing and issuing ticket confirmation...</p>
      </div>
    );
  }

  if (error || !booking) {
    return (
      <div className="max-w-md mx-auto py-16 text-center">
        <div className="rounded-2xl bg-slate-900 border border-slate-800 p-8 shadow-xl space-y-4">
          <h2 className="text-xl font-bold text-white">Booking Not Found</h2>
          <p className="text-sm text-slate-400">{error || 'Unable to retrieve reservation details.'}</p>
          <Link
            to="/my-bookings"
            className="inline-block px-4 py-2 rounded-lg bg-slate-800 text-slate-200 hover:bg-slate-700 text-xs font-semibold"
          >
            Go to My Bookings
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto py-8 space-y-8">
      {/* Confirmation Success Header */}
      <div className="text-center space-y-3">
        <div className="w-16 h-16 rounded-3xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center mx-auto shadow-xl shadow-emerald-500/10">
          <CheckCircle2 className="w-10 h-10" />
        </div>
        <h1 className="text-3xl font-black text-white tracking-tight">Booking Confirmed!</h1>
        <p className="text-sm text-slate-400">
          Your reservation is confirmed. A receipt and e-ticket have been generated.
        </p>

        {/* PNR Banner */}
        <div className="inline-flex items-center gap-3 px-6 py-2 rounded-2xl bg-slate-900 border border-slate-800 shadow-lg">
          <span className="text-xs text-slate-400 uppercase font-semibold">Booking PNR:</span>
          <span className="font-mono text-xl font-black text-sky-400 tracking-wider">
            {booking.bookingReference}
          </span>
          {ticket?.ticketNumber && (
            <>
              <span className="text-slate-600">•</span>
              <span className="text-xs text-slate-400 uppercase font-semibold">Ticket:</span>
              <span className="font-mono text-sm font-bold text-emerald-400">{ticket.ticketNumber}</span>
            </>
          )}
        </div>
      </div>

      {/* Main Ticket Summary Card */}
      <div className="rounded-3xl bg-slate-900/90 border border-slate-800 shadow-2xl overflow-hidden backdrop-blur-xl">
        {/* Card Header */}
        <div className="p-6 bg-gradient-to-r from-sky-950/40 via-indigo-950/40 to-slate-900 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-sky-500/20 text-sky-400 border border-sky-500/30 flex items-center justify-center">
              <Plane className="w-5 h-5" />
            </div>
            <div>
              <h2 className="font-bold text-white text-base">{booking.airline}</h2>
              <p className="text-xs text-slate-400 font-mono">Flight {booking.flightNumber}</p>
            </div>
          </div>

          <span className="px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 text-xs font-bold border border-emerald-500/20">
            {booking.status}
          </span>
        </div>

        {/* Flight Route Timings */}
        <div className="p-6 grid grid-cols-1 sm:grid-cols-3 gap-6 items-center text-center sm:text-left border-b border-slate-800">
          <div>
            <p className="text-2xl font-black text-white">
              {new Date(booking.departureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </p>
            <p className="text-sm font-bold text-sky-400">{booking.departureAirport.code}</p>
            <p className="text-xs text-slate-400">{booking.departureAirport.city}</p>
            <p className="text-[11px] text-slate-500">{new Date(booking.departureTime).toLocaleDateString()}</p>
          </div>

          <div className="flex flex-col items-center">
            <span className="text-xs text-slate-400 font-medium">{booking.durationMinutes} mins</span>
            <div className="w-full flex items-center my-2">
              <div className="h-0.5 w-full bg-slate-700 relative">
                <div className="absolute -top-1 left-1/2 transform -translate-x-1/2 w-2 h-2 rounded-full bg-sky-400"></div>
              </div>
            </div>
            <span className="text-[10px] text-emerald-400 font-bold uppercase">Confirmed Non-Stop</span>
          </div>

          <div className="text-center sm:text-right">
            <p className="text-2xl font-black text-white">
              {new Date(booking.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </p>
            <p className="text-sm font-bold text-sky-400">{booking.arrivalAirport.code}</p>
            <p className="text-xs text-slate-400">{booking.arrivalAirport.city}</p>
            <p className="text-[11px] text-slate-500">{new Date(booking.arrivalTime).toLocaleDateString()}</p>
          </div>
        </div>

        {/* Passengers & Assigned Seats */}
        <div className="p-6 space-y-4 border-b border-slate-800">
          <h3 className="font-bold text-white text-sm flex items-center gap-2">
            <Users className="w-4 h-4 text-sky-400" />
            <span>Passenger & Seat Details</span>
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {booking.passengers.map((pax, idx) => (
              <div
                key={idx}
                className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 flex justify-between items-center text-xs"
              >
                <div>
                  <p className="font-bold text-white">
                    {pax.title} {pax.firstName} {pax.lastName}
                  </p>
                  <p className="text-[10px] text-slate-400 uppercase">{booking.cabinClass.replace('_', ' ')}</p>
                </div>
                <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-sky-500/10 text-sky-400 border border-sky-500/20 font-mono font-bold text-xs">
                  <Armchair className="w-3.5 h-3.5" />
                  <span>Seat {pax.seatNumber || 'Assigned'}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Total Paid & Payment Ref */}
        <div className="p-6 bg-slate-950/40 flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-xs text-slate-400">Total Amount Paid</p>
            <p className="text-2xl font-black text-emerald-400">
              ₹{booking.totalAmount.toLocaleString('en-IN')}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            {ticket?.id && (
              <button
                type="button"
                disabled={downloadingPdf}
                onClick={handleDownloadPdf}
                className="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold flex items-center gap-2 border border-slate-700 transition"
              >
                <Download className="w-4 h-4 text-sky-400" />
                <span>{downloadingPdf ? 'Downloading...' : 'Download E-Ticket PDF'}</span>
              </button>
            )}

            <Link
              to={`/check-in/${booking.id}`}
              className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white text-xs font-bold flex items-center gap-2 shadow-lg shadow-sky-500/20 transition"
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
          className="text-xs font-semibold text-slate-400 hover:text-sky-400 inline-flex items-center gap-1.5 transition"
        >
          <BookmarkCheck className="w-4 h-4 text-indigo-400" />
          <span>View all upcoming trips in My Bookings</span>
        </Link>
      </div>
    </div>
  );
};
