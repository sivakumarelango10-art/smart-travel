import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Plane,
  Download,
  Printer,
  BookmarkCheck
} from 'lucide-react';
import { BoardingPass } from '../types/api';
import { checkInService } from '../services/checkInService';

export const BoardingPassPage: React.FC = () => {
  const { bookingId } = useParams<{ bookingId: string }>();

  const [boardingPass, setBoardingPass] = useState<BoardingPass | null>(null);
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
          setBoardingPass(bpRes.data);
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
      a.download = `BoardingPass_${boardingPass?.bookingReference || 'SmartTravel'}.pdf`;
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

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center gap-4">
        <div className="w-12 h-12 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin"></div>
        <p className="text-sm text-slate-400 font-bold">Retrieving digital mobile boarding pass...</p>
      </div>
    );
  }

  if (error || !boardingPass) {
    return (
      <div className="max-w-md mx-auto py-20 text-center">
        <div className="rounded-3xl bg-slate-900 border border-slate-800 p-8 shadow-2xl space-y-4">
          <h2 className="text-xl font-extrabold text-white">Boarding Pass Not Ready</h2>
          <p className="text-xs text-slate-400">{error || 'Please complete online check-in first.'}</p>
          <Link
            to={`/check-in/${bookingId}`}
            className="inline-block px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 text-white font-bold text-xs shadow-lg"
          >
            Go to Check-In
          </Link>
        </div>
      </div>
    );
  }

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

      {/* Boarding Pass Card */}
      <div className="rounded-3xl bg-slate-900 border-2 border-slate-800 shadow-2xl overflow-hidden relative print:bg-white print:text-black">
        {/* Top Header */}
        <div className="p-6 bg-gradient-to-r from-indigo-950/80 via-slate-900 to-sky-950/80 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-indigo-500/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center font-black">
              <Plane className="w-6 h-6" />
            </div>
            <div>
              <h2 className="font-extrabold text-white text-base leading-tight">{boardingPass.airline}</h2>
              <p className="text-xs text-slate-400 font-mono">Flight {boardingPass.flightNumber}</p>
            </div>
          </div>

          <div className="text-right">
            <span className="px-3 py-1 rounded-full bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 text-[10px] font-black">
              BOARDING PASS
            </span>
            <p className="text-[11px] text-slate-400 font-mono mt-1 font-bold">PNR: {boardingPass.bookingReference}</p>
          </div>
        </div>

        {/* Route Info */}
        <div className="p-6 sm:p-8 grid grid-cols-3 gap-4 items-center text-center border-b border-slate-800">
          <div className="text-left">
            <p className="text-3xl sm:text-4xl font-black text-white">{boardingPass.departureAirport.code}</p>
            <p className="text-xs text-slate-400 mt-0.5">{boardingPass.departureAirport.city}</p>
          </div>

          <div className="flex flex-col items-center">
            <Plane className="w-6 h-6 text-indigo-400 transform rotate-90" />
            <span className="text-[10px] font-mono font-black text-slate-500 mt-1 uppercase tracking-widest">NON-STOP</span>
          </div>

          <div className="text-right">
            <p className="text-3xl sm:text-4xl font-black text-white">{boardingPass.arrivalAirport.code}</p>
            <p className="text-xs text-slate-400 mt-0.5">{boardingPass.arrivalAirport.city}</p>
          </div>
        </div>

        {/* Passenger & Flight Details Grid */}
        <div className="p-6 grid grid-cols-2 sm:grid-cols-4 gap-4 border-b border-slate-800 text-xs">
          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold">Passenger</span>
            <p className="font-extrabold text-white mt-1 truncate text-sm">{boardingPass.passengerName}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold">Seat</span>
            <p className="font-mono text-xl font-black text-sky-400 mt-0.5">{boardingPass.seatNumber}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold">Cabin</span>
            <p className="font-bold text-white mt-1">{boardingPass.cabinClass.replace('_', ' ')}</p>
          </div>

          <div>
            <span className="text-[10px] text-slate-500 uppercase font-bold">Boarding Time</span>
            <p className="font-mono font-black text-amber-400 mt-0.5 text-sm">{boardingPass.boardingTime}</p>
          </div>
        </div>

        {/* Gate & Terminal Row */}
        <div className="p-6 bg-slate-950/60 flex items-center justify-between border-b border-dashed border-slate-700 text-xs">
          <div className="flex items-center gap-6 sm:gap-8">
            <div>
              <span className="text-[10px] text-slate-500 uppercase font-bold">Terminal</span>
              <p className="font-extrabold text-white text-base">{boardingPass.terminal || 'T3'}</p>
            </div>
            <div>
              <span className="text-[10px] text-slate-500 uppercase font-bold">Gate</span>
              <p className="font-extrabold text-white text-base">{boardingPass.gate || 'Gate 14'}</p>
            </div>
          </div>

          <div className="text-right">
            <span className="text-[10px] text-slate-500 uppercase font-bold">Sequence</span>
            <p className="font-mono font-black text-slate-300 text-base">{boardingPass.sequenceNumber || '042'}</p>
          </div>
        </div>

        {/* Barcode & Security Stamp */}
        <div className="p-6 bg-slate-950 text-center space-y-3">
          <div className="font-mono text-[10px] text-slate-500 tracking-widest uppercase">
            *M1{boardingPass.bookingReference}/{boardingPass.passengerName.replace(' ', '/')} {boardingPass.flightNumber}*
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

      <div className="text-center pt-2">
        <Link
          to="/my-bookings"
          className="text-xs font-bold text-slate-400 hover:text-sky-400 inline-flex items-center gap-1.5 transition"
        >
          <BookmarkCheck className="w-4 h-4 text-indigo-400" />
          <span>Return to My Bookings</span>
        </Link>
      </div>
    </div>
  );
};

