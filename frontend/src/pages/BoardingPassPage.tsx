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
import { notify } from '../utils/toast';

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
      const passRef = (boardingPasses[selectedPassIndex] || boardingPasses[0])?.bookingReference || 'SmartTravel';
      a.download = `BoardingPass_${passRef}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      notify('Pass Downloaded', `Boarding pass for ${passRef} downloaded successfully.`, 'SUCCESS');
    } catch (err: any) {
      notify('Download Error', 'Failed to download boarding pass PDF: ' + (err?.message || 'Please try again.'), 'ERROR');
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
      return d.toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return timeStr;
    }
  };

  if (loading) {
    return (
      <div className="max-w-2xl mx-auto py-8">
        <BoardingPassSkeleton />
      </div>
    );
  }

  if (error || boardingPasses.length === 0) {
    return (
      <div className="max-w-md mx-auto py-16 text-center space-y-4 px-4">
        <div className="w-14 h-14 rounded-2xl bg-rose-500/15 text-rose-400 border border-rose-500/30 flex items-center justify-center mx-auto">
          <AlertCircle className="w-7 h-7" />
        </div>
        <h2 className="text-xl font-bold text-white">Boarding Pass Not Available</h2>
        <p className="text-xs text-slate-400">
          {error || 'Online check-in must be completed before a digital boarding pass can be generated.'}
        </p>
        <div className="flex justify-center gap-3 pt-2">
          <Link
            to={bookingId ? `/check-in/${bookingId}` : '/my-bookings'}
            className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-glow-gold transition"
          >
            Complete Check-In Now
          </Link>
          <Link
            to="/my-bookings"
            className="px-4 py-2.5 rounded-xl bg-[#14161F] text-slate-300 font-bold text-xs border border-white/10 transition"
          >
            Back to Bookings
          </Link>
        </div>
      </div>
    );
  }

  const activePass = boardingPasses[selectedPassIndex] || boardingPasses[0];

  // Resolve airport codes & cities safely
  const depCode = getAirportCode(activePass.departureAirport, 'DEL');
  const arrCode = getAirportCode(activePass.arrivalAirport, 'BOM');
  const depCity = getAirportCity(activePass.departureAirport, depCode);
  const arrCity = getAirportCity(activePass.arrivalAirport, arrCode);

  const depTimeFormatted = formatTime(activePass.departureTime);
  const flightDateFormatted = formatDate(activePass.departureTime);
  const boardingTimeFormatted = activePass.boardingTime
    ? formatTime(activePass.boardingTime)
    : activePass.departureTime
    ? formatTime(new Date(new Date(activePass.departureTime).getTime() - 45 * 60000).toISOString())
    : '45m before departure';

  const passNumber =
    activePass.boardingPassNumber ||
    activePass.eTicketNumber ||
    activePass.ticketNumber ||
    `BP-${activePass.bookingReference || 'ST'}-${activePass.seatNumber || '10F'}`;

  const qrScannerUrl = `${window.location.origin}/verify-pass/${encodeURIComponent(passNumber)}?pnr=${encodeURIComponent(activePass.bookingReference || '')}&flight=${encodeURIComponent(activePass.flightNumber || '')}&pax=${encodeURIComponent(activePass.passengerName || '')}&seat=${encodeURIComponent(activePass.seatNumber || '')}`;

  return (
    <div className="max-w-2xl mx-auto py-8 space-y-6 animate-fade-in px-4">
      {/* ── TOP CONTROLS & DOWNLOAD BAR ──────────────────── */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-black text-white tracking-tight">Official Boarding Pass</h1>
          <p className="text-xs text-slate-400">Present this gate pass and photo ID at security checkpoints</p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => window.print()}
            className="p-2.5 sm:px-3.5 sm:py-2 bg-[#14161F] hover:bg-[#181A22] text-slate-300 text-xs font-bold rounded-xl border border-white/10 flex items-center gap-1.5 transition shadow-sm"
            title="Print Boarding Pass"
          >
            <Printer className="w-4 h-4 text-amber-400" />
            <span className="hidden sm:inline">Print Pass</span>
          </button>

          <button
            type="button"
            disabled={downloading}
            onClick={handleDownloadPdf}
            className="px-4 py-2 bg-gradient-to-r from-amber-400 to-amber-500 text-black text-xs font-extrabold rounded-xl flex items-center gap-1.5 shadow-glow-gold transition disabled:opacity-50 cursor-pointer"
          >
            <Download className="w-4 h-4 text-black" />
            <span>{downloading ? 'Downloading...' : 'Download PDF'}</span>
          </button>
        </div>
      </div>

      {/* ── PASSENGER SELECTOR TABS (If Multiple) ─────────── */}
      {boardingPasses.length > 1 && (
        <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none text-xs">
          <span className="text-slate-400 font-semibold shrink-0">Pass for:</span>
          {boardingPasses.map((p, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => setSelectedPassIndex(idx)}
              className={`px-3 py-1.5 rounded-xl font-bold transition flex items-center gap-1.5 shrink-0 ${
                selectedPassIndex === idx
                  ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                  : 'bg-[#14161F] text-slate-300 hover:text-white border border-white/10'
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
      <div className="rounded-3xl bg-[#14161F] border border-white/10 shadow-2xl overflow-hidden relative">
        {/* Brand Header Strip */}
        <div className="p-5 bg-[#0B0C10] text-white flex items-center justify-between border-b border-white/10">
          <div className="flex items-center gap-3">
            <BrandLogo size="md" withLink={false} />
            <div className="hidden sm:block border-l border-white/10 pl-3">
              <span className="text-[10px] uppercase font-bold tracking-widest text-amber-400 block">OFFICIAL PASS</span>
              <span className="text-xs text-slate-300 font-semibold">{activePass.airline || 'SmartTravel Airways'}</span>
            </div>
          </div>

          <div className="text-right">
            <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 text-[10px] font-black inline-flex items-center gap-1 shadow-glow-emerald">
              <ShieldCheck className="w-3 h-3" />
              <span>GATE CLEARED</span>
            </span>
            <p className="text-[11px] text-amber-400 font-mono mt-0.5 font-bold">PNR: {activePass.bookingReference}</p>
          </div>
        </div>

        {/* Route Highlight Visualizer */}
        <div className="p-6 sm:p-7 bg-[#181A22] grid grid-cols-3 gap-4 items-center text-center border-b border-white/10">
          <div className="text-left">
            <p className="text-3xl sm:text-4xl font-black text-white tracking-tight">{depCode}</p>
            <p className="text-xs text-slate-400 font-semibold mt-0.5">{depCity}</p>
            {depTimeFormatted && <p className="text-xs text-amber-400 font-mono font-bold mt-0.5">{depTimeFormatted}</p>}
          </div>

          <div className="flex flex-col items-center justify-center">
            <div className="flex items-center gap-2 text-amber-400 font-bold">
              <span className="h-0.5 w-6 sm:w-8 bg-amber-400/40 rounded" />
              <Plane className="w-5 h-5 transform rotate-90 text-amber-400" />
              <span className="h-0.5 w-6 sm:w-8 bg-amber-400/40 rounded" />
            </div>
            <span className="text-[10px] font-mono font-bold text-amber-400 mt-1 uppercase tracking-wider bg-[#14161F] px-2 py-0.5 rounded-full border border-amber-400/20">
              NON-STOP
            </span>
            {flightDateFormatted && (
              <span className="text-[11px] text-slate-400 font-medium mt-1">{flightDateFormatted}</span>
            )}
          </div>

          <div className="text-right">
            <p className="text-3xl sm:text-4xl font-black text-white tracking-tight">{arrCode}</p>
            <p className="text-xs text-slate-400 font-semibold mt-0.5">{arrCity}</p>
            <p className="text-xs text-emerald-400 font-mono font-bold mt-0.5">Confirmed</p>
          </div>
        </div>

        {/* Primary Passenger & Flight Details Grid */}
        <div className="p-5 grid grid-cols-2 sm:grid-cols-4 gap-4 border-b border-white/10 text-xs">
          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Passenger</span>
            <p className="font-bold text-white mt-0.5 truncate text-sm">{activePass.passengerName}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Flight</span>
            <div className="flex items-center gap-1.5 mt-0.5">
              <AirlineLogo airline={activePass.airline} size="xs" />
              <span className="font-mono font-bold text-amber-400 text-sm">{activePass.flightNumber}</span>
            </div>
          </div>

          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Cabin Class</span>
            <p className="font-bold text-white mt-0.5">
              {activePass.cabinClass ? String(activePass.cabinClass).replace('_', ' ') : 'ECONOMY'}
            </p>
          </div>

          <div className="bg-amber-400/10 border border-amber-400/20 p-2 rounded-xl text-center shadow-glow-gold">
            <span className="text-[10px] text-amber-400 uppercase font-bold tracking-wider block">SEAT</span>
            <p className="font-mono text-xl font-black text-amber-400">{activePass.seatNumber || '10F'}</p>
          </div>
        </div>

        {/* Gate, Terminal, Boarding Time */}
        <div className="p-5 bg-[#181A22] grid grid-cols-2 sm:grid-cols-4 gap-4 border-b border-dashed border-white/10 text-xs">
          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Boarding Time</span>
            <p className="font-mono font-bold text-amber-400 mt-0.5 text-sm flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" />
              <span>{boardingTimeFormatted}</span>
            </p>
          </div>

          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Gate</span>
            <p className="font-bold text-white text-sm mt-0.5 flex items-center gap-1">
              <MapPin className="w-3.5 h-3.5 text-amber-400" />
              <span>{activePass.gate || 'Gate 08'}</span>
            </p>
          </div>

          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Terminal</span>
            <p className="font-bold text-white text-sm mt-0.5">{activePass.terminal || 'T3'}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Boarding Group</span>
            <p className="font-mono font-bold text-emerald-400 text-sm mt-0.5">{activePass.boardingGroup || 'Group 1'}</p>
          </div>
        </div>

        {/* QR Code Section */}
        <div className="p-5 text-center space-y-3">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 p-4 bg-[#181A22] rounded-2xl border border-white/10">
            <div className="flex items-center gap-3 text-left">
              <div className="p-1.5 bg-white rounded-xl shrink-0 shadow-sm border border-slate-200">
                <RealQRCode value={qrScannerUrl} size={76} includeMargin={false} />
              </div>
              <div className="space-y-0.5">
                <span className="text-xs font-bold text-white flex items-center gap-1">
                  <QrCode className="w-3.5 h-3.5 text-amber-400" />
                  Scannable Gate QR Code
                </span>
                <span className="text-[11px] text-slate-400 block">
                  Scan with any phone camera to verify manifest
                </span>
                <Link
                  to={`/verify-pass/${encodeURIComponent(passNumber)}?pnr=${encodeURIComponent(activePass.bookingReference || '')}&flight=${encodeURIComponent(activePass.flightNumber || '')}&pax=${encodeURIComponent(activePass.passengerName || '')}&seat=${encodeURIComponent(activePass.seatNumber || '')}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 text-[11px] font-bold text-amber-400 hover:underline pt-0.5"
                >
                  <span>Open Verification Manifest</span>
                  <ExternalLink className="w-3 h-3 text-amber-400" />
                </Link>
              </div>
            </div>

            {/* 1D Barcode */}
            <div className="flex flex-col items-center sm:items-end">
              <div className="h-8 bg-[#14161F] px-2 py-1 rounded-md flex items-center gap-0.5 border border-white/10">
                {[2, 1, 3, 1, 2, 4, 1, 3, 2, 1, 4, 2, 1, 3, 1, 2, 3, 1, 2, 4, 1, 3, 2].map((w, i) => (
                  <div key={i} className="h-full bg-amber-400" style={{ width: `${w * 1.5}px` }} />
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
          className="font-bold text-slate-400 hover:text-amber-400 inline-flex items-center gap-1.5 transition"
        >
          <BookmarkCheck className="w-4 h-4 text-amber-400" />
          <span>View E-Ticket Receipt</span>
        </Link>

        <span className="text-white/20">•</span>

        <Link
          to="/my-bookings"
          className="font-bold text-slate-400 hover:text-amber-400 inline-flex items-center gap-1.5 transition"
        >
          <span>Return to My Bookings</span>
        </Link>
      </div>
    </div>
  );
};
