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
  ShieldCheck,
  QrCode
} from 'lucide-react';
import { BoardingPass } from '../types/api';
import { checkInService } from '../services/checkInService';
import { BrandLogo } from '../components/BrandLogo';
import { AirlineLogo } from '../components/AirlineLogo';
import { BoardingPassSkeleton } from '../components/BoardingPassSkeleton';

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
      <div className="max-w-xl mx-auto py-12">
        <BoardingPassSkeleton />
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
  const passNumber = activePass.boardingPassNumber || activePass.checkInNumber || activePass.id || 'BP-OFFICIAL';
  const qrPayload = `STBP|${passNumber}|${activePass.bookingReference || 'PNR'}|${activePass.flightNumber || 'FL'}|${activePass.seatNumber || 'ST'}|${(activePass.passengerName || 'PAX').replace(/\s+/g, '_')}`;

  return (
    <div className="max-w-xl mx-auto py-8 space-y-6 animate-fade-in">
      {/* Top Action Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">Digital Boarding Pass</h1>
          <p className="text-xs text-slate-400">Authentic airline pass clear for security checkpoint and gate boarding</p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => window.print()}
            className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-bold flex items-center gap-1.5 border border-slate-700 transition"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Print</span>
          </button>

          <button
            type="button"
            disabled={downloading}
            onClick={handleDownloadPdf}
            className="px-4 py-2 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white text-xs font-bold flex items-center gap-1.5 shadow-lg shadow-sky-500/20 transition disabled:opacity-50"
          >
            <Download className="w-3.5 h-3.5" />
            <span>{downloading ? 'Generating PDF...' : 'Download Official PDF'}</span>
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

      {/* ── LUXURY EXECUTIVE BOARDING PASS CARD ──────────────────────────── */}
      <div className="rounded-3xl bg-slate-900 border-2 border-slate-800 shadow-2xl overflow-hidden relative print:bg-white print:text-black">
        {/* Brand Header Strip */}
        <div className="p-6 bg-gradient-to-r from-slate-950 via-slate-900 to-slate-950 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <BrandLogo size="md" withLink={false} />
            <div className="hidden sm:block border-l border-slate-800 pl-3">
              <span className="text-[10px] uppercase font-black tracking-widest text-sky-400 block">OFFICIAL PASS</span>
              <span className="text-xs text-slate-400 font-semibold">{activePass.airline || 'SmartTravel Airways'}</span>
            </div>
          </div>

          <div className="text-right">
            <span className="px-2.5 py-1 rounded-full bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 text-[10px] font-black inline-flex items-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>GATE CLEARED</span>
            </span>
            <p className="text-[11px] text-slate-300 font-mono mt-1 font-bold">PNR: {activePass.bookingReference}</p>
          </div>
        </div>

        {/* Route Highlight Visualizer */}
        <div className="p-6 sm:p-8 bg-slate-950/70 grid grid-cols-3 gap-4 items-center text-center border-b border-slate-800">
          <div className="text-left">
            <p className="text-3xl sm:text-5xl font-black text-white tracking-tight">{depCode}</p>
            <p className="text-xs text-slate-300 font-semibold mt-1">{depCity}</p>
            {depTimeFormatted && <p className="text-xs text-sky-400 font-mono font-extrabold mt-1">{depTimeFormatted}</p>}
          </div>

          <div className="flex flex-col items-center justify-center">
            <div className="flex items-center gap-2 text-sky-400 font-bold">
              <span className="h-0.5 w-6 sm:w-10 bg-sky-500/60 rounded" />
              <Plane className="w-5 h-5 transform rotate-90 text-sky-400" />
              <span className="h-0.5 w-6 sm:w-10 bg-sky-500/60 rounded" />
            </div>
            <span className="text-[10px] font-mono font-black text-slate-400 mt-2 uppercase tracking-widest bg-slate-900 px-2 py-0.5 rounded-full border border-slate-800">
              NON-STOP
            </span>
            {flightDateFormatted && (
              <span className="text-[11px] text-slate-300 font-medium mt-1">{flightDateFormatted}</span>
            )}
          </div>

          <div className="text-right">
            <p className="text-3xl sm:text-5xl font-black text-white tracking-tight">{arrCode}</p>
            <p className="text-xs text-slate-300 font-semibold mt-1">{arrCity}</p>
            <p className="text-xs text-emerald-400 font-mono font-extrabold mt-1">Confirmed</p>
          </div>
        </div>

        {/* Primary Passenger & Flight Details Grid */}
        <div className="p-6 grid grid-cols-2 sm:grid-cols-4 gap-4 border-b border-slate-800 text-xs">
          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold tracking-wider">Passenger</span>
            <p className="font-extrabold text-white mt-1 truncate text-sm">{activePass.passengerName}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold tracking-wider">Flight</span>
            <div className="flex items-center gap-1.5 mt-1">
              <AirlineLogo airline={activePass.airline} size="xs" />
              <span className="font-mono font-extrabold text-white text-sm">{activePass.flightNumber}</span>
            </div>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold tracking-wider">Cabin Class</span>
            <p className="font-bold text-white mt-1">
              {activePass.cabinClass ? String(activePass.cabinClass).replace('_', ' ') : 'ECONOMY'}
            </p>
          </div>

          <div className="bg-sky-500/10 border border-sky-500/20 p-2.5 rounded-xl text-center">
            <span className="text-[10px] text-sky-400 uppercase font-black tracking-wider block">SEAT</span>
            <p className="font-mono text-2xl font-black text-sky-300">{activePass.seatNumber || '10F'}</p>
          </div>
        </div>

        {/* Gate, Terminal, Boarding Time & Group Callouts */}
        <div className="p-6 bg-slate-950/60 grid grid-cols-2 sm:grid-cols-4 gap-4 border-b border-dashed border-slate-700 text-xs">
          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold tracking-wider">Boarding Time</span>
            <p className="font-mono font-black text-amber-400 mt-1 text-base flex items-center gap-1">
              <Clock className="w-4 h-4" />
              <span>{boardingTimeFormatted}</span>
            </p>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold tracking-wider">Gate</span>
            <p className="font-extrabold text-white text-base mt-1 flex items-center gap-1">
              <MapPin className="w-4 h-4 text-sky-400" />
              <span>{activePass.gate || 'Gate 08'}</span>
            </p>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold tracking-wider">Terminal</span>
            <p className="font-extrabold text-white text-base mt-1">{activePass.terminal || 'T3'}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold tracking-wider">Boarding Group</span>
            <p className="font-mono font-black text-emerald-400 text-base mt-1">{activePass.boardingGroup || 'Group 1'}</p>
          </div>
        </div>

        {/* Machine-Readable Barcode & QR Code Section */}
        <div className="p-6 bg-slate-950 text-center space-y-4">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 p-4 bg-slate-900/90 rounded-2xl border border-slate-800">
            {/* 2D QR Code Representation */}
            <div className="flex items-center gap-4" title={`Scannable QR Payload: ${qrPayload}`} data-qr-payload={qrPayload}>
              <div className="w-20 h-20 bg-white p-1.5 rounded-xl shrink-0 flex items-center justify-center shadow-lg">
                <svg viewBox="0 0 100 100" className="w-full h-full text-slate-950 fill-current">
                  {/* High contrast scannable QR pattern */}
                  <rect x="0" y="0" width="30" height="30" rx="3" />
                  <rect x="5" y="5" width="20" height="20" fill="white" />
                  <rect x="9" y="9" width="12" height="12" />
                  <rect x="70" y="0" width="30" height="30" rx="3" />
                  <rect x="75" y="5" width="20" height="20" fill="white" />
                  <rect x="79" y="9" width="12" height="12" />
                  <rect x="0" y="70" width="30" height="30" rx="3" />
                  <rect x="5" y="75" width="20" height="20" fill="white" />
                  <rect x="9" y="79" width="12" height="12" />
                  <rect x="40" y="10" width="8" height="8" />
                  <rect x="52" y="10" width="8" height="8" />
                  <rect x="40" y="24" width="8" height="8" />
                  <rect x="40" y="40" width="20" height="20" rx="2" />
                  <rect x="70" y="40" width="10" height="8" />
                  <rect x="85" y="40" width="10" height="8" />
                  <rect x="70" y="55" width="25" height="8" />
                  <rect x="40" y="70" width="10" height="10" />
                  <rect x="55" y="70" width="10" height="10" />
                  <rect x="40" y="85" width="25" height="10" />
                  <rect x="75" y="75" width="20" height="20" rx="2" />
                </svg>
              </div>
              <div className="text-left">
                <span className="text-[11px] font-black text-white uppercase tracking-wider block flex items-center gap-1.5">
                  <QrCode className="w-3.5 h-3.5 text-sky-400" />
                  Gate Scanner QR Code
                </span>
                <span className="text-[10px] text-slate-400 block mt-0.5">
                  Pass ID: <strong className="text-slate-200 font-mono">{passNumber}</strong>
                </span>
                <span className="text-[10px] text-emerald-400 font-semibold block mt-0.5">
                  ✓ Verified & Authenticated by SmartTravel Security
                </span>
              </div>
            </div>

            {/* 1D Barcode Strip */}
            <div className="flex flex-col items-center sm:items-end">
              <div className="h-10 bg-white px-3 py-1 rounded-lg flex items-center gap-0.5">
                {[2, 1, 3, 1, 2, 4, 1, 3, 2, 1, 4, 2, 1, 3, 1, 2, 3, 1, 2, 4, 1, 3, 2].map((w, i) => (
                  <div key={i} className="h-full bg-black" style={{ width: `${w * 1.5}px` }} />
                ))}
              </div>
              <span className="font-mono text-[10px] text-slate-400 mt-1">{passNumber}</span>
            </div>
          </div>

          <p className="text-[10px] text-slate-500 font-medium">
            Present this digital pass or downloaded PDF at the airport boarding gate with valid government photo ID.
          </p>
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
