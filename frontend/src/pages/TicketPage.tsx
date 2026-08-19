import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Download,
  Printer,
  Plane,
  Users,
  BookmarkCheck
} from 'lucide-react';
import { Ticket } from '../types/api';
import { ticketService } from '../services/ticketService';

export const TicketPage: React.FC = () => {
  const { bookingId } = useParams<{ bookingId: string }>();

  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [downloading, setDownloading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTicketData = async () => {
      if (!bookingId) return;
      try {
        setLoading(true);
        setError(null);
        const tktRes = await ticketService.getTicketByBookingId(bookingId);

        if (tktRes.success && tktRes.data) {
          setTicket(tktRes.data);
        }
      } catch (err: any) {
        setError(err?.message || 'Failed to load e-ticket');
      } finally {
        setLoading(false);
      }
    };
    fetchTicketData();
  }, [bookingId]);

  const handleDownloadPdf = async () => {
    if (!ticket?.id) return;
    try {
      setDownloading(true);
      const blob = await ticketService.downloadTicketPdf(ticket.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `ETicket_${ticket.ticketNumber}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err: any) {
      alert('Download error: ' + (err?.message || 'Please try again.'));
    } finally {
      setDownloading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center gap-4">
        <div className="w-12 h-12 border-4 border-sky-500/30 border-t-sky-500 rounded-full animate-spin"></div>
        <p className="text-sm text-slate-400 font-bold">Fetching official e-ticket document...</p>
      </div>
    );
  }

  if (error || !ticket) {
    return (
      <div className="max-w-md mx-auto py-20 text-center">
        <div className="rounded-3xl bg-slate-900 border border-slate-800 p-8 shadow-2xl space-y-4">
          <h2 className="text-xl font-extrabold text-white">E-Ticket Unavailable</h2>
          <p className="text-xs text-slate-400">{error || 'Ticket has not yet been issued for this reservation.'}</p>
          <Link
            to="/my-bookings"
            className="inline-block px-5 py-2.5 rounded-xl bg-slate-800 text-slate-200 hover:bg-slate-750 text-xs font-bold transition"
          >
            Back to My Bookings
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto py-8 space-y-6 animate-fade-in">
      {/* Top Action Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">Electronic Flight Ticket</h1>
          <p className="text-xs text-slate-400">Official passenger receipt and authoritative travel itinerary</p>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => window.print()}
            className="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-200 text-xs font-bold flex items-center gap-2 border border-slate-700 transition shadow-sm"
          >
            <Printer className="w-4 h-4" />
            <span>Print Ticket</span>
          </button>

          <button
            type="button"
            disabled={downloading}
            onClick={handleDownloadPdf}
            className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white text-xs font-black flex items-center gap-2 shadow-xl shadow-sky-500/25 transition disabled:opacity-50"
          >
            <Download className="w-4 h-4" />
            <span>{downloading ? 'Downloading...' : 'Download Official PDF'}</span>
          </button>
        </div>
      </div>

      {/* Official E-Ticket Layout */}
      <div className="rounded-3xl bg-slate-900 border border-slate-800 shadow-2xl overflow-hidden print:border-black print:bg-white print:text-black">
        {/* Ticket Header */}
        <div className="p-6 sm:p-7 bg-gradient-to-r from-sky-950/60 to-indigo-950/60 border-b border-slate-800 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3.5">
            <div className="w-12 h-12 rounded-2xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center font-black">
              <Plane className="w-6 h-6" />
            </div>
            <div>
              <h2 className="font-extrabold text-white text-lg leading-tight">{ticket.airline}</h2>
              <p className="text-xs text-slate-400 font-medium">E-Ticket Receipt & Itinerary</p>
            </div>
          </div>

          <div className="text-right">
            <p className="text-[10px] text-slate-400 uppercase font-black tracking-wider">E-Ticket Number</p>
            <p className="font-mono text-base font-black text-emerald-400">{ticket.ticketNumber}</p>
            <p className="text-[11px] text-slate-400 font-mono mt-0.5">PNR: {ticket.bookingReference}</p>
          </div>
        </div>

        {/* Flight Route & Timings */}
        <div className="p-6 sm:p-8 grid grid-cols-1 sm:grid-cols-3 gap-6 items-center border-b border-slate-800">
          <div>
            <span className="text-[10px] uppercase font-black text-slate-500 tracking-wider">Departure</span>
            <p className="text-3xl font-black text-white mt-1">
              {new Date(ticket.departureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </p>
            <p className="text-base font-bold text-sky-400 mt-0.5">
              {ticket.departureAirport.city} ({ticket.departureAirport.code})
            </p>
            <p className="text-xs text-slate-400">{ticket.departureAirport.name}</p>
            <p className="text-[11px] text-slate-500 mt-1">{new Date(ticket.departureTime).toLocaleDateString()}</p>
          </div>

          <div className="text-center">
            <span className="text-xs font-mono font-bold text-sky-300 bg-sky-950/60 px-3 py-1 rounded-xl border border-sky-800/40">
              Flight {ticket.flightNumber}
            </span>
            <p className="text-xs text-slate-400 mt-2">{ticket.aircraftModel}</p>
            <p className="text-[10px] text-emerald-400 font-black uppercase tracking-wider mt-1">Confirmed Non-Stop</p>
          </div>

          <div className="text-left sm:text-right">
            <span className="text-[10px] uppercase font-black text-slate-500 tracking-wider">Arrival</span>
            <p className="text-3xl font-black text-white mt-1">
              {new Date(ticket.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </p>
            <p className="text-base font-bold text-sky-400 mt-0.5">
              {ticket.arrivalAirport.city} ({ticket.arrivalAirport.code})
            </p>
            <p className="text-xs text-slate-400">{ticket.arrivalAirport.name}</p>
            <p className="text-[11px] text-slate-500 mt-1">{new Date(ticket.arrivalTime).toLocaleDateString()}</p>
          </div>
        </div>

        {/* Passenger & Seat Assignment Table */}
        <div className="p-6 sm:p-7 space-y-4 border-b border-slate-800">
          <h3 className="font-extrabold text-white text-base flex items-center gap-2">
            <Users className="w-4 h-4 text-sky-400" />
            <span>Traveler Details</span>
          </h3>
          <div className="divide-y divide-slate-800/80">
            {ticket.passengers.map((pax, idx) => (
              <div key={idx} className="py-3.5 flex items-center justify-between text-xs">
                <div>
                  <p className="font-extrabold text-white text-sm">{pax.fullName}</p>
                  <p className="text-[11px] text-slate-400 mt-0.5">{ticket.cabinClass.replace('_', ' ')}</p>
                </div>
                <div className="flex items-center gap-3 font-mono">
                  <span className="font-black text-sky-400 bg-sky-950/60 px-3 py-1 rounded-xl border border-sky-800/40">
                    Seat {pax.seatNumber}
                  </span>
                  <span
                    className={`px-2.5 py-0.5 rounded-full text-[10px] font-black ${
                      pax.checkedIn ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30' : 'bg-slate-800 text-slate-400'
                    }`}
                  >
                    {pax.checkedIn ? 'Checked In' : 'Confirmed'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Payment & Fare Receipt */}
        <div className="p-6 sm:p-7 bg-slate-950/60 flex flex-wrap items-center justify-between gap-4 text-xs">
          <div>
            <p className="text-slate-400">Issued On: {new Date(ticket.issuedAt).toLocaleString()}</p>
            <p className="text-slate-500 mt-0.5">Status: Electronic Ticket (ETKT) - Active</p>
          </div>

          <div className="text-right">
            <span className="text-slate-400 block text-[11px] font-semibold">Total Fare Paid</span>
            <span className="text-2xl font-black text-emerald-400">
              ₹{ticket.totalAmount.toLocaleString('en-IN')}
            </span>
          </div>
        </div>
      </div>

      <div className="text-center pt-2">
        <Link
          to="/my-bookings"
          className="text-xs font-bold text-slate-400 hover:text-sky-400 inline-flex items-center gap-1.5 transition"
        >
          <BookmarkCheck className="w-4 h-4 text-indigo-400" />
          <span>Back to My Bookings</span>
        </Link>
      </div>
    </div>
  );
};

