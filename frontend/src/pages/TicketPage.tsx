import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Download,
  Printer,
  Users,
  BookmarkCheck
} from 'lucide-react';
import { Ticket } from '../types/api';
import { ticketService } from '../services/ticketService';
import { AirlineLogo } from '../components/AirlineLogo';
import { notify } from '../utils/toast';

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
        const res = await ticketService.getTicketByBookingId(bookingId);
        if (res.success && res.data) {
          setTicket(res.data);
        } else {
          setError('Ticket is currently being generated. Please check back shortly.');
        }
      } catch (err: any) {
        setError(err.message || 'Failed to load ticket information.');
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
      notify('Download Complete', `E-Ticket ${ticket.ticketNumber} downloaded successfully.`, 'SUCCESS');
    } catch (err: any) {
      notify('Download Error', err?.message || 'Please try again.', 'ERROR');
    } finally {
      setDownloading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center gap-4">
        <div className="w-12 h-12 border-4 border-amber-400/30 border-t-amber-400 rounded-full animate-spin"></div>
        <p className="text-sm text-slate-400 font-bold">Fetching official e-ticket document...</p>
      </div>
    );
  }

  if (error || !ticket) {
    return (
      <div className="max-w-md mx-auto py-20 text-center">
        <div className="rounded-3xl bg-[#14161F] border border-white/10 p-8 shadow-2xl space-y-4">
          <h2 className="text-xl font-extrabold text-white">E-Ticket Unavailable</h2>
          <p className="text-xs text-slate-400">{error || 'Ticket has not yet been issued for this reservation.'}</p>
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
            className="px-4 py-2.5 rounded-xl bg-[#14161F] hover:bg-[#181A22] text-slate-300 text-xs font-bold flex items-center gap-2 border border-white/10 transition shadow-sm"
          >
            <Printer className="w-4 h-4 text-amber-400" />
            <span>Print Ticket</span>
          </button>

          <button
            type="button"
            disabled={downloading}
            onClick={handleDownloadPdf}
            className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black text-xs font-black flex items-center gap-2 shadow-glow-gold transition disabled:opacity-50"
          >
            <Download className="w-4 h-4 text-black" />
            <span>{downloading ? 'Downloading...' : 'Download Official PDF'}</span>
          </button>
        </div>
      </div>

      {/* Official E-Ticket Layout */}
      <div className="rounded-3xl bg-[#14161F] border border-white/10 shadow-2xl overflow-hidden print:border-black print:bg-white print:text-black">
        {/* Ticket Header */}
        <div className="p-6 sm:p-7 bg-[#0B0C10] border-b border-white/10 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3.5">
            <AirlineLogo airline={ticket.airline} airlineCode={ticket.airlineCode} size="lg" />
            <div>
              <h2 className="font-extrabold text-white text-lg leading-tight">{ticket.airline}</h2>
              <p className="text-xs text-slate-400 font-medium">E-Ticket Receipt & Itinerary</p>
            </div>
          </div>

          <div className="text-right">
            <p className="text-[10px] text-slate-400 uppercase font-black tracking-wider">E-Ticket Number</p>
            <p className="font-mono text-base font-black text-amber-400">{ticket.ticketNumber}</p>
            <p className="text-[11px] text-slate-400 font-mono mt-0.5">PNR: {ticket.bookingReference}</p>
          </div>
        </div>

        {/* Flight Route & Timings */}
        <div className="p-6 sm:p-8 grid grid-cols-1 sm:grid-cols-3 gap-6 items-center border-b border-white/10">
          <div>
            <span className="text-[10px] uppercase font-black text-slate-500 tracking-wider">Departure</span>
            <p className="text-3xl font-black text-white mt-1">
              {new Date(ticket.departureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </p>
            <p className="text-base font-bold text-amber-400 mt-0.5">
              {ticket.departureAirport.city} ({ticket.departureAirport.code})
            </p>
            <p className="text-xs text-slate-400">{ticket.departureAirport.name}</p>
            <p className="text-[11px] text-slate-500 mt-1">{new Date(ticket.departureTime).toLocaleDateString()}</p>
          </div>

          <div className="text-center">
            <span className="text-xs font-mono font-bold text-amber-400 bg-amber-400/10 px-3 py-1 rounded-xl border border-amber-400/20">
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
            <p className="text-base font-bold text-amber-400 mt-0.5">
              {ticket.arrivalAirport.city} ({ticket.arrivalAirport.code})
            </p>
            <p className="text-xs text-slate-400">{ticket.arrivalAirport.name}</p>
            <p className="text-[11px] text-slate-500 mt-1">{new Date(ticket.arrivalTime).toLocaleDateString()}</p>
          </div>
        </div>

        {/* Passenger & Seat Assignment Table */}
        <div className="p-6 sm:p-7 space-y-4 border-b border-white/10">
          <h3 className="font-extrabold text-white text-base flex items-center gap-2">
            <Users className="w-4 h-4 text-amber-400" />
            <span>Traveler Details</span>
          </h3>
          <div className="divide-y divide-white/5">
            {ticket.passengers.map((pax, idx) => (
              <div key={idx} className="py-3.5 flex items-center justify-between text-xs">
                <div>
                  <p className="font-extrabold text-white text-sm">{pax.fullName}</p>
                  <p className="text-[11px] text-slate-400 mt-0.5">{ticket.cabinClass.replace('_', ' ')}</p>
                </div>
                <div className="flex items-center gap-3 font-mono">
                  <span className="font-black text-amber-400 bg-amber-400/10 px-3 py-1 rounded-xl border border-amber-400/20">
                    Seat {pax.seatNumber}
                  </span>
                  <span
                    className={`px-2.5 py-0.5 rounded-full text-[10px] font-black ${
                      pax.checkedIn ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30' : 'bg-[#181A22] text-slate-400'
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
        <div className="p-6 sm:p-7 bg-[#181A22]/50 flex flex-wrap items-center justify-between gap-4 text-xs">
          <div>
            <p className="text-slate-400">Issued On: {new Date(ticket.issuedAt).toLocaleString()}</p>
            <p className="text-slate-500 mt-0.5">Status: Electronic Ticket (ETKT) - Active</p>
          </div>

          <div className="text-right">
            <span className="text-slate-400 block text-[11px] font-semibold">Total Fare Paid</span>
            <span className="text-2xl font-black text-amber-400">
              ₹{ticket.totalAmount.toLocaleString('en-IN')}
            </span>
          </div>
        </div>
      </div>

      <div className="text-center pt-2">
        <Link
          to="/my-bookings"
          className="text-xs font-bold text-slate-400 hover:text-amber-400 inline-flex items-center gap-1.5 transition"
        >
          <BookmarkCheck className="w-4 h-4 text-amber-400" />
          <span>Back to My Bookings</span>
        </Link>
      </div>
    </div>
  );
};
