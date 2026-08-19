import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plane,
  Clock,
  Luggage,
  ShieldCheck,
  ChevronRight,
  AlertTriangle,
  Flame
} from 'lucide-react';
import { Flight, CabinClass } from '../types/api';

interface FlightCardProps {
  flight: Flight;
  selectedCabin?: CabinClass;
  passengerCount?: number;
}

export const FlightCard: React.FC<FlightCardProps> = ({
  flight,
  selectedCabin = 'ECONOMY',
  passengerCount = 1,
}) => {
  const navigate = useNavigate();

  const depDate = new Date(flight.departureTime);
  const arrDate = new Date(flight.arrivalTime);

  const formatTime = (date: Date) =>
    date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true });

  const formatDuration = (mins: number) => {
    const hours = Math.floor(mins / 60);
    const m = mins % 60;
    return `${hours}h ${m}m`;
  };

  // Find matching cabin inventory
  const cabinInv =
    flight.cabinInventories?.find((c) => c.cabinClass === selectedCabin) ||
    flight.cabinInventories?.[0];

  const pricePerPax = cabinInv ? cabinInv.totalPrice : flight.basePrice;
  const totalPrice = pricePerPax * passengerCount;
  const availableSeats = cabinInv ? cabinInv.availableSeats : flight.availableSeats;

  const isDisrupted = flight.status === 'DELAYED' || flight.status === 'CANCELLED';
  const isBookable = flight.isBookable && availableSeats >= passengerCount && flight.status !== 'CANCELLED';

  const handleSelectFlight = () => {
    navigate(`/book/${flight.id}?cabinClass=${selectedCabin}&passengers=${passengerCount}`);
  };

  // Airline color coding helper
  const getAirlineColor = (name: string) => {
    if (name.toLowerCase().includes('indigo')) return 'from-indigo-600 to-blue-700 text-white';
    if (name.toLowerCase().includes('air india')) return 'from-rose-600 to-red-700 text-white';
    if (name.toLowerCase().includes('vistara')) return 'from-purple-700 to-indigo-900 text-white';
    if (name.toLowerCase().includes('spicejet')) return 'from-orange-500 to-red-600 text-white';
    if (name.toLowerCase().includes('emirates')) return 'from-red-600 to-slate-900 text-white';
    return 'from-sky-500 to-indigo-600 text-white';
  };

  return (
    <div
      className={`rounded-3xl border transition-all duration-200 overflow-hidden ${
        isDisrupted
          ? 'bg-slate-900/95 border-amber-500/30 shadow-xl shadow-amber-500/5'
          : 'bg-slate-900/90 hover:bg-slate-900 border-slate-800 hover:border-slate-700 shadow-xl hover:shadow-2xl'
      }`}
    >
      {/* Disruption Alert Banner */}
      {isDisrupted && (
        <div
          className={`px-5 py-2.5 text-xs font-bold flex items-center gap-2 ${
            flight.status === 'CANCELLED'
              ? 'bg-rose-500/15 text-rose-400 border-b border-rose-500/20'
              : 'bg-amber-500/15 text-amber-400 border-b border-amber-500/20'
          }`}
        >
          <AlertTriangle className="w-4 h-4 shrink-0" />
          <span>
            {flight.status === 'CANCELLED'
              ? `Flight Cancelled: ${flight.cancellationReason || 'Operational constraint'}`
              : `Flight Delayed by ${flight.delayMinutes || 0} minutes (Revised Departure: ${
                  flight.revisedDepartureTime
                    ? formatTime(new Date(flight.revisedDepartureTime))
                    : 'TBD'
                })`}
          </span>
        </div>
      )}

      {/* Main Card Body */}
      <div className="p-5 sm:p-6 grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
        {/* 1. Airline & Aircraft Info */}
        <div className="lg:col-span-3 flex items-center gap-3.5">
          <div
            className={`w-12 h-12 rounded-2xl bg-gradient-to-tr ${getAirlineColor(
              flight.airline
            )} flex items-center justify-center shadow-lg font-black text-sm shrink-0 border border-white/10`}
          >
            {flight.airline.slice(0, 2).toUpperCase()}
          </div>
          <div>
            <h3 className="font-extrabold text-white text-base leading-snug">{flight.airline}</h3>
            <div className="flex items-center gap-2 text-xs text-slate-400 mt-0.5">
              <span className="font-mono text-slate-300 font-bold bg-slate-950 px-2 py-0.5 rounded border border-slate-800">
                {flight.flightNumber}
              </span>
              <span>•</span>
              <span className="text-slate-400">{flight.aircraftModel}</span>
            </div>
          </div>
        </div>

        {/* 2. Route & Timings Visualizer */}
        <div className="lg:col-span-5 flex items-center justify-between gap-4">
          {/* Departure */}
          <div className="text-left min-w-[70px]">
            <p className="text-2xl font-black text-white">{formatTime(depDate)}</p>
            <p className="text-sm font-bold text-sky-400 mt-0.5">{flight.departureAirport.code}</p>
            <p className="text-[11px] text-slate-400 truncate max-w-[100px]">{flight.departureAirport.city}</p>
            {flight.departureAirport.terminal && (
              <span className="inline-block mt-1 text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                {flight.departureAirport.terminal}
              </span>
            )}
          </div>

          {/* Duration Graphic */}
          <div className="flex-1 flex flex-col items-center px-2">
            <span className="text-[11px] text-slate-400 font-bold flex items-center gap-1">
              <Clock className="w-3.5 h-3.5 text-slate-500" />
              {formatDuration(flight.durationMinutes)}
            </span>
            <div className="w-full flex items-center my-2">
              <div className="h-0.5 w-full bg-slate-700/80 relative">
                <div className="absolute -top-1.5 left-1/2 transform -translate-x-1/2 w-3 h-3 rounded-full bg-sky-400 flex items-center justify-center shadow-md shadow-sky-400/50">
                  <Plane className="w-2 h-2 text-slate-950 transform rotate-45" />
                </div>
              </div>
            </div>
            <span className="text-[10px] font-extrabold text-emerald-400 uppercase tracking-wider px-2 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/20">
              {flight.stops === 0 ? 'Non-Stop' : `${flight.stops} Stop(s)`}
            </span>
          </div>

          {/* Arrival */}
          <div className="text-right min-w-[70px]">
            <p className="text-2xl font-black text-white">{formatTime(arrDate)}</p>
            <p className="text-sm font-bold text-sky-400 mt-0.5">{flight.arrivalAirport.code}</p>
            <p className="text-[11px] text-slate-400 truncate max-w-[100px]">{flight.arrivalAirport.city}</p>
            {flight.arrivalAirport.terminal && (
              <span className="inline-block mt-1 text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                {flight.arrivalAirport.terminal}
              </span>
            )}
          </div>
        </div>

        {/* 3. Fare & Booking Button */}
        <div className="lg:col-span-4 flex flex-row lg:flex-col items-center lg:items-end justify-between gap-3 pt-3 lg:pt-0 border-t lg:border-t-0 border-slate-800">
          <div className="text-left lg:text-right">
            <div className="flex items-baseline gap-1.5 lg:justify-end">
              <span className="text-2xl sm:text-3xl font-black text-white tracking-tight">
                ₹{totalPrice.toLocaleString('en-IN')}
              </span>
              {passengerCount > 1 && (
                <span className="text-xs text-slate-400 font-medium">
                  (₹{pricePerPax.toLocaleString('en-IN')}/pax)
                </span>
              )}
            </div>
            <div className="flex items-center gap-2 mt-0.5 text-[11px]">
              <span className="text-slate-400">Taxes & fees included</span>
              <span>•</span>
              <span
                className={`font-bold flex items-center gap-1 ${
                  availableSeats < 10 ? 'text-amber-400' : 'text-slate-400'
                }`}
              >
                {availableSeats < 10 && <Flame className="w-3 h-3 text-amber-400" />}
                {availableSeats} seats left
              </span>
            </div>
          </div>

          <button
            type="button"
            onClick={handleSelectFlight}
            disabled={!isBookable}
            className={`w-full sm:w-auto px-6 py-3 rounded-2xl font-black text-xs sm:text-sm transition-all duration-200 flex items-center justify-center gap-2 shadow-lg ${
              isBookable
                ? 'bg-gradient-to-r from-sky-500 via-indigo-500 to-blue-600 hover:from-sky-400 hover:via-indigo-400 hover:to-blue-500 text-white shadow-sky-500/25 hover:shadow-sky-500/40 hover:scale-[1.02] active:scale-[0.98]'
                : 'bg-slate-800 text-slate-500 cursor-not-allowed border border-slate-700'
            }`}
          >
            <span>{isBookable ? 'Select Flight' : 'Unavailable'}</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Flight Perks Footer Bar */}
      <div className="px-6 py-3 bg-slate-950/70 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-3 text-[11px] text-slate-400">
        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1.5">
            <Luggage className="w-3.5 h-3.5 text-slate-500" />
            <span>Check-in 15kg + Cabin 7kg included</span>
          </span>
          <span className="flex items-center gap-1.5 text-emerald-400 font-semibold">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Instant E-Ticket & Pass</span>
          </span>
        </div>

        <div className="flex items-center gap-2 text-slate-400">
          <span>Cabin:</span>
          <span className="font-bold text-slate-200">{selectedCabin.replace('_', ' ')}</span>
        </div>
      </div>
    </div>
  );
};

