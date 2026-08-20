import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Plane,
  Download,
  Printer,
  BookmarkCheck,
  User,
  AlertCircle,
  Clock,
  MapPin,
  CheckCircle2
} from 'lucide-react';
import { BoardingPass } from '../types/api';
import { checkInService } from '../services/checkInService';

export const BoardingPassPage: React.FC = () => {
  const { bookingId } = useParams<{ bookingId: string }>();

  const [boardingPasses, setBoardingPasses] = useState<BoardingPass[]>([]);
  const [selectedPassIndex, setSelectedPassIndex] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [downloading, setDownloading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      if (!bookingId) return;
      try {
        setLoading(true);
        setError(null);
        const bpRes = await checkInService.getBoardingPass(bookingId);

        if (bpRes.success && bpRes.data) {
          const rawData = bpRes.data;
          const passes: BoardingPass[] = Array.isArray(rawData)
            ? rawData
            : rawData
            ? [rawData]
            : [];
          setBoardingPasses(passes);
          setSelectedPassIndex(0);
        } else {
          setBoardingPasses([]);
        }
      } catch (err: any) {
        setError(err?.message || 'Failed to fetch boarding pass');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [bookingId]);

  const handleDownloadPdf = async () => {
    if (!bookingId) return;
    try {
      setDownloading(true);
      const blob = await checkInService.downloadBoardingPassPdf(bookingId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      const ref = activePass?.bookingReference || 'SmartTravel';
      a.download = `BoardingPass_${ref}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err: any) {
      alert('Failed to download boarding pass PDF: ' + (err?.message || 'Please try again.'));
    } finally {
      setDownloading(false);
    }
  };

  const getAirportCode = (airport: any, fallback = 'DEL'): string => {
    if (!airport) return fallback;
    if (typeof airport === 'string') return airport;
    return airport.code || airport.iata || fallback;
  };

  const getAirportCity = (airport: any, fallback = ''): string => {
    if (!airport) return fallback;
    if (typeof airport === 'string') return airport;
    return airport.city || airport.name || fallback;
  };

  const formatTime = (timeStr?: string): string => {
    if (!timeStr) return '--:--';
    try {
      const d = new Date(timeStr);
      if (isNaN(d.getTime())) return timeStr;
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return timeStr;
    }
  };

  const formatDate = (timeStr?: string): string => {
    if (!timeStr) return '';
    try {
      const d = new Date(timeStr);
      if (isNaN(d.getTime())) return timeStr;
      return d.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });
    } catch {
      return timeStr;
    }
  };

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center gap-4">
        <div className="w-12 h-12 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin"></div>
        <p className="text-sm text-slate-400 font-bold">Retrieving digital mobile boarding pass...</p>
      </div>
    );
  }

  if (error || boardingPasses.length === 0) {
    return (
      <div className="max-w-md mx-auto py-20 text-center">
        <div className="rounded-3xl bg-slate-900 border border-slate-800 p-8 shadow-2xl space-y-4">
          <div className="w-12 h-12 rounded-2xl bg-amber-500/10 text-amber-400 border border-amber-500/20 flex items-center justify-center mx-auto">
            <AlertCircle className="w-6 h-6" />
          </div>
          <h2 className="text-xl font-extrabold text-white">Boarding Pass Not Ready</h2>
          <p className="text-xs text-slate-400">{error || 'Please complete online check-in to generate your mobile boarding pass.'}</p>
          <div className="pt-2">
            <Link
              to={`/check-in/${bookingId}`}
              className="inline-block px-6 py-3 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white font-bold text-xs shadow-lg shadow-sky-500/20 transition"
            >
              Go to Online Check-In
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const activePass = boardingPasses[selectedPassIndex] || boardingPasses[0];

  const depCode = getAirportCode(activePass.departureAirport, 'DEL');
  const depCity = getAirportCity(activePass.departureAirport, 'Delhi');
  const arrCode = getAirportCode(activePass.arrivalAirport, 'BOM');
  const arrCity = getAirportCity(activePass.arrivalAirport, 'Mumbai');

  const boardingTimeFormatted = formatTime(activePass.boardingTime || activePass.departureTime);
  const depTimeFormatted = formatTime(activePass.departureTime);
  const flightDateFormatted = formatDate(activePass.departureTime);

  return (
    <div className="max-w-xl mx-auto py-8 space-y-6 animate-fade-in">
      {/* Top Action Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">Digital Boarding Pass</h1>
          <p className="text-xs text-slate-400">Ready for airport security and boarding gate verification</p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            type="button"
            onClick={() => window.print()}
            className="p-2.5 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-300 hover:text-white border border-slate-700 transition shadow-sm"
            title="Print"
          >
            <Printer className="w-4 h-4" />
          </button>

          <button
            type="button"
            disabled={downloading}
            onClick={handleDownloadPdf}
            className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white text-xs font-black flex items-center gap-2 shadow-xl shadow-sky-500/20 transition disabled:opacity-50"
          >
            <Download className="w-4 h-4" />
            <span>{downloading ? 'Downloading...' : 'Download PDF Pass'}</span>
          </button>
        </div>
      </div>

      {/* Multiple Travelers Selector Tabs */}
      {boardingPasses.length > 1 && (
        <div className="flex items-center gap-2 overflow-x-auto pb-1">
          {boardingPasses.map((p, idx) => (
            <button
              key={p.id || idx}
              type="button"
              onClick={() => setSelectedPassIndex(idx)}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 shrink-0 ${
                selectedPassIndex === idx
                  ? 'bg-sky-500 text-white shadow-lg shadow-sky-500/25'
                  : 'bg-slate-900 text-slate-400 hover:text-slate-200 border border-slate-800'
              }`}
            >
              <User className="w-3.5 h-3.5" />
              <span>{p.passengerName}</span>
              <span className="font-mono text-[11px] opacity-80">({p.seatNumber})</span>
            </button>
          ))}
        </div>
      )}

      {/* Boarding Pass Card */}
      <div className="rounded-3xl bg-slate-900 border-2 border-slate-800 shadow-2xl overflow-hidden relative print:bg-white print:text-black">
        {/* Top Header */}
        <div className="p-6 bg-gradient-to-r from-indigo-950/80 via-slate-900 to-sky-950/80 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-indigo-500/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center font-black">
              <Plane className="w-6 h-6" />
            </div>
            <div>
              <h2 className="font-extrabold text-white text-base leading-tight">{activePass.airline || 'SmartAir'}</h2>
              <p className="text-xs text-slate-400 font-mono">Flight {activePass.flightNumber}</p>
            </div>
          </div>

          <div className="text-right">
            <span className="px-3 py-1 rounded-full bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 text-[10px] font-black inline-flex items-center gap-1">
              <CheckCircle2 className="w-3 h-3" />
              <span>BOARDING PASS</span>
            </span>
            <p className="text-[11px] text-slate-400 font-mono mt-1 font-bold">PNR: {activePass.bookingReference}</p>
          </div>
        </div>

        {/* Route Info */}
        <div className="p-6 sm:p-8 grid grid-cols-3 gap-4 items-center text-center border-b border-slate-800">
          <div className="text-left">
            <p className="text-3xl sm:text-4xl font-black text-white">{depCode}</p>
            <p className="text-xs text-slate-400 mt-0.5">{depCity}</p>
            {depTimeFormatted && <p className="text-[11px] text-sky-400 font-mono font-bold mt-1">{depTimeFormatted}</p>}
          </div>

          <div className="flex flex-col items-center">
            <Plane className="w-6 h-6 text-indigo-400 transform rotate-90" />
            <span className="text-[10px] font-mono font-black text-slate-500 mt-1 uppercase tracking-widest">NON-STOP</span>
            {flightDateFormatted && (
              <span className="text-[10px] text-slate-400 font-medium mt-0.5">{flightDateFormatted}</span>
            )}
          </div>

          <div className="text-right">
            <p className="text-3xl sm:text-4xl font-black text-white">{arrCode}</p>
            <p className="text-xs text-slate-400 mt-0.5">{arrCity}</p>
          </div>
        </div>

        {/* Passenger & Flight Details Grid */}
        <div className="p-6 grid grid-cols-2 sm:grid-cols-4 gap-4 border-b border-slate-800 text-xs">
          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold">Passenger</span>
            <p className="font-extrabold text-white mt-1 truncate text-sm">{activePass.passengerName}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold">Seat</span>
            <p className="font-mono text-xl font-black text-sky-400 mt-0.5">{activePass.seatNumber || '5F'}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold">Cabin</span>
            <p className="font-bold text-white mt-1">
              {activePass.cabinClass ? String(activePass.cabinClass).replace('_', ' ') : 'ECONOMY'}
            </p>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold">Boarding Time</span>
            <p className="font-mono font-black text-amber-400 mt-0.5 text-sm flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" />
              <span>{boardingTimeFormatted}</span>
            </p>
          </div>
        </div>

        {/* Gate & Terminal Row */}
        <div className="p-6 bg-slate-950/60 flex items-center justify-between border-b border-dashed border-slate-700 text-xs">
          <div className="flex items-center gap-6 sm:gap-8">
            <div>
              <span className="text-[10px] text-slate-500 uppercase font-bold">Terminal</span>
              <p className="font-extrabold text-white text-base">{activePass.terminal || 'T3'}</p>
            </div>
            <div>
              <span className="text-[10px] text-slate-500 uppercase font-bold">Gate</span>
              <p className="font-extrabold text-white text-base flex items-center gap-1">
                <MapPin className="w-3.5 h-3.5 text-sky-400" />
                <span>{activePass.gate || 'Gate 14'}</span>
              </p>
            </div>
          </div>

          <div className="text-right">
            <span className="text-[10px] text-slate-500 uppercase font-bold">Boarding Group</span>
            <p className="font-mono font-black text-emerald-400 text-base">{activePass.boardingGroup || 'Group 2'}</p>
          </div>
        </div>

        {/* Barcode & Security Stamp */}
        <div className="p-6 bg-slate-950 text-center space-y-3">
          <div className="font-mono text-[10px] text-slate-500 tracking-widest uppercase">
            *M1{activePass.bookingReference || 'PNR'}/{(activePass.passengerName || 'PAX').replace(/\s+/g, '/')} {activePass.flightNumber || 'FLIGHT'}*
          </div>
          {/* Simulated 2D Barcode Stripes */}
          <div className="h-12 bg-slate-900 rounded-xl flex items-center justify-around px-4 opacity-80 border border-slate-800">
            {Array.from({ length: 48 }).map((_, i) => (
              <div
                key={i}
                className="h-8 bg-white"
                style={{ width: `${(i % 3) + 1}px` }}
              ></div>
            ))}
          </div>
          <p className="text-[10px] text-slate-500 font-medium">Scan at airport security checkpoint and boarding gate</p>
        </div>
      </div>

      <div className="text-center pt-2 flex items-center justify-center gap-6 text-xs">
        <Link
          to={`/ticket/${bookingId}`}
          className="font-bold text-slate-400 hover:text-sky-400 inline-flex items-center gap-1.5 transition"
        >
          <BookmarkCheck className="w-4 h-4 text-sky-400" />
          <span>View E-Ticket Receipt</span>
        </Link>

        <span className="text-slate-700">•</span>

        <Link
          to="/my-bookings"
          className="font-bold text-slate-400 hover:text-indigo-400 inline-flex items-center gap-1.5 transition"
        >
          <span>Return to My Bookings</span>
        </Link>
      </div>
    </div>
  );
};
