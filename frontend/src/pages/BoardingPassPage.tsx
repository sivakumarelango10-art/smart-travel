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
  QrCode,
  ExternalLink
} from 'lucide-react';
import { BoardingPass } from '../types/api';
import { checkInService } from '../services/checkInService';
import { BrandLogo } from '../components/BrandLogo';
import { AirlineLogo } from '../components/AirlineLogo';
import { BoardingPassSkeleton } from '../components/BoardingPassSkeleton';
import { RealQRCode } from '../components/RealQRCode';

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
        <div className="rounded-2xl bg-white border border-slate-200 p-8 shadow-sm space-y-4">
          <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 border border-amber-200 flex items-center justify-center mx-auto">
            <AlertCircle className="w-6 h-6" />
          </div>
          <h2 className="text-lg font-black text-primary">Boarding Pass Not Ready</h2>
          <p className="text-xs text-slate-500">{error || 'Please complete online check-in to generate your mobile boarding pass.'}</p>
          <div className="pt-2">
            <Link
              to={`/check-in/${bookingId}`}
              className="inline-block px-5 py-2.5 rounded-xl bg-primary hover:bg-primary-hover text-white font-bold text-xs shadow-sm transition"
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
  const origin = typeof window !== 'undefined' && window.location.origin ? window.location.origin : 'https://smart-travel.sage.vercel.app';
  const qrScannerUrl = `${origin}/verify-pass/${encodeURIComponent(passNumber)}?pnr=${encodeURIComponent(activePass.bookingReference || '')}&flight=${encodeURIComponent(activePass.flightNumber || '')}&pax=${encodeURIComponent(activePass.passengerName || '')}&seat=${encodeURIComponent(activePass.seatNumber || '')}`;

  return (
    <div className="max-w-xl mx-auto py-8 space-y-6">
      {/* Top Action Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-primary tracking-tight">Digital Boarding Pass</h1>
          <p className="text-xs text-slate-500">Authorized e-pass valid for airport security and boarding gate</p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => window.print()}
            className="px-3 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold flex items-center gap-1.5 border border-slate-200 transition"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Print</span>
          </button>

          <button
            type="button"
            disabled={downloading}
            onClick={handleDownloadPdf}
            className="px-4 py-2 rounded-xl bg-primary hover:bg-primary-hover text-white text-xs font-bold flex items-center gap-1.5 shadow-sm transition disabled:opacity-50"
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
              className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition flex items-center gap-1.5 shrink-0 ${
                selectedPassIndex === idx
                  ? 'bg-primary text-white shadow-sm'
                  : 'bg-white text-slate-600 hover:text-primary border border-slate-200'
              }`}
            >
              <User className="w-3.5 h-3.5" />
              <span>{p.passengerName}</span>
              <span className="font-mono text-[11px] opacity-80">({p.seatNumber})</span>
            </button>
          ))}
        </div>
      )}

      {/* ── BOARDING PASS CARD ──────────────────────────── */}
      <div className="rounded-3xl bg-white border border-slate-200 shadow-card overflow-hidden relative">
        {/* Brand Header Strip */}
        <div className="p-5 bg-primary text-white flex items-center justify-between">
          <div className="flex items-center gap-3">
            <BrandLogo size="md" withLink={false} />
            <div className="hidden sm:block border-l border-slate-700 pl-3">
              <span className="text-[10px] uppercase font-bold tracking-widest text-secondary block">OFFICIAL PASS</span>
              <span className="text-xs text-slate-300 font-semibold">{activePass.airline || 'SmartTravel Airways'}</span>
            </div>
          </div>

          <div className="text-right">
            <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-[10px] font-black inline-flex items-center gap-1">
              <ShieldCheck className="w-3 h-3" />
              <span>GATE CLEARED</span>
            </span>
            <p className="text-[11px] text-slate-300 font-mono mt-0.5 font-bold">PNR: {activePass.bookingReference}</p>
          </div>
        </div>

        {/* Route Highlight Visualizer */}
        <div className="p-6 sm:p-7 bg-slate-50 grid grid-cols-3 gap-4 items-center text-center border-b border-slate-200">
          <div className="text-left">
            <p className="text-3xl sm:text-4xl font-black text-primary tracking-tight">{depCode}</p>
            <p className="text-xs text-slate-600 font-semibold mt-0.5">{depCity}</p>
            {depTimeFormatted && <p className="text-xs text-secondary font-mono font-bold mt-0.5">{depTimeFormatted}</p>}
          </div>

          <div className="flex flex-col items-center justify-center">
            <div className="flex items-center gap-2 text-secondary font-bold">
              <span className="h-0.5 w-6 sm:w-8 bg-secondary/40 rounded" />
              <Plane className="w-5 h-5 transform rotate-90 text-secondary" />
              <span className="h-0.5 w-6 sm:w-8 bg-secondary/40 rounded" />
            </div>
            <span className="text-[10px] font-mono font-bold text-slate-500 mt-1 uppercase tracking-wider bg-white px-2 py-0.5 rounded-full border border-slate-200">
              NON-STOP
            </span>
            {flightDateFormatted && (
              <span className="text-[11px] text-slate-500 font-medium mt-1">{flightDateFormatted}</span>
            )}
          </div>

          <div className="text-right">
            <p className="text-3xl sm:text-4xl font-black text-primary tracking-tight">{arrCode}</p>
            <p className="text-xs text-slate-600 font-semibold mt-0.5">{arrCity}</p>
            <p className="text-xs text-emerald-600 font-mono font-bold mt-0.5">Confirmed</p>
          </div>
        </div>

        {/* Primary Passenger & Flight Details Grid */}
        <div className="p-5 grid grid-cols-2 sm:grid-cols-4 gap-4 border-b border-slate-100 text-xs">
          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Passenger</span>
            <p className="font-bold text-primary mt-0.5 truncate text-sm">{activePass.passengerName}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Flight</span>
            <div className="flex items-center gap-1.5 mt-0.5">
              <AirlineLogo airline={activePass.airline} size="xs" />
              <span className="font-mono font-bold text-primary text-sm">{activePass.flightNumber}</span>
            </div>
          </div>

          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Cabin Class</span>
            <p className="font-bold text-primary mt-0.5">
              {activePass.cabinClass ? String(activePass.cabinClass).replace('_', ' ') : 'ECONOMY'}
            </p>
          </div>

          <div className="bg-secondary/10 border border-secondary/20 p-2 rounded-xl text-center">
            <span className="text-[10px] text-secondary uppercase font-bold tracking-wider block">SEAT</span>
            <p className="font-mono text-xl font-black text-primary">{activePass.seatNumber || '10F'}</p>
          </div>
        </div>

        {/* Gate, Terminal, Boarding Time */}
        <div className="p-5 bg-slate-50 grid grid-cols-2 sm:grid-cols-4 gap-4 border-b border-dashed border-slate-200 text-xs">
          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Boarding Time</span>
            <p className="font-mono font-bold text-accent mt-0.5 text-sm flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" />
              <span>{boardingTimeFormatted}</span>
            </p>
          </div>

          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Gate</span>
            <p className="font-bold text-primary text-sm mt-0.5 flex items-center gap-1">
              <MapPin className="w-3.5 h-3.5 text-secondary" />
              <span>{activePass.gate || 'Gate 08'}</span>
            </p>
          </div>

          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Terminal</span>
            <p className="font-bold text-primary text-sm mt-0.5">{activePass.terminal || 'T3'}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Boarding Group</span>
            <p className="font-mono font-bold text-emerald-600 text-sm mt-0.5">{activePass.boardingGroup || 'Group 1'}</p>
          </div>
        </div>

        {/* QR Code Section */}
        <div className="p-5 text-center space-y-3">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 p-4 bg-slate-50 rounded-2xl border border-slate-200">
            <div className="flex items-center gap-3 text-left">
              <div className="p-1.5 bg-white rounded-xl shrink-0 shadow-sm border border-slate-200">
                <RealQRCode value={qrScannerUrl} size={76} includeMargin={false} />
              </div>
              <div className="space-y-0.5">
                <span className="text-xs font-bold text-primary flex items-center gap-1">
                  <QrCode className="w-3.5 h-3.5 text-secondary" />
                  Scannable Gate QR Code
                </span>
                <span className="text-[11px] text-slate-500 block">
                  Scan with any phone camera to verify manifest
                </span>
                <Link
                  to={`/verify-pass/${encodeURIComponent(passNumber)}?pnr=${encodeURIComponent(activePass.bookingReference || '')}&flight=${encodeURIComponent(activePass.flightNumber || '')}&pax=${encodeURIComponent(activePass.passengerName || '')}&seat=${encodeURIComponent(activePass.seatNumber || '')}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 text-[11px] font-bold text-secondary hover:underline pt-0.5"
                >
                  <span>Open Verification Manifest</span>
                  <ExternalLink className="w-3 h-3" />
                </Link>
              </div>
            </div>

            {/* 1D Barcode */}
            <div className="flex flex-col items-center sm:items-end">
              <div className="h-8 bg-white px-2 py-1 rounded-md flex items-center gap-0.5 border border-slate-200">
                {[2, 1, 3, 1, 2, 4, 1, 3, 2, 1, 4, 2, 1, 3, 1, 2, 3, 1, 2, 4, 1, 3, 2].map((w, i) => (
                  <div key={i} className="h-full bg-slate-800" style={{ width: `${w * 1.5}px` }} />
                ))}
              </div>
              <span className="font-mono text-[10px] text-slate-400 mt-1">{passNumber}</span>
            </div>
          </div>

          <p className="text-[10px] text-slate-400">
            Present this digital pass or downloaded PDF at airport security and gate with government photo ID.
          </p>
        </div>
      </div>

      <div className="text-center pt-2 flex items-center justify-center gap-6 text-xs">
        <Link
          to={`/ticket/${bookingId}`}
          className="font-bold text-slate-600 hover:text-primary inline-flex items-center gap-1.5 transition"
        >
          <BookmarkCheck className="w-4 h-4 text-secondary" />
          <span>View E-Ticket Receipt</span>
        </Link>

        <span className="text-slate-300">•</span>

        <Link
          to="/my-bookings"
          className="font-bold text-slate-600 hover:text-primary inline-flex items-center gap-1.5 transition"
        >
          <span>Return to My Bookings</span>
        </Link>
      </div>
    </div>
  );
};
