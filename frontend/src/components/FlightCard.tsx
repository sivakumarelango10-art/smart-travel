import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plane,
  Clock,
  Luggage,
  ShieldCheck,
  ChevronRight,
  AlertTriangle
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

  return (
    <div
      className={`rounded-2xl border transition duration-200 overflow-hidden ${
        isDisrupted
          ? 'bg-slate-900/90 border-amber-500/30 shadow-lg shadow-amber-500/5'
          : 'bg-slate-900/90 hover:bg-slate-850 border-slate-800 hover:border-slate-700 shadow-xl'
      }`}
    >
      {/* Disruption Alert Banner if applicable */}
      {isDisrupted && (
        <div
          className={`px-4 py-2 text-xs font-semibold flex items-center gap-2 ${
            flight.status === 'CANCELLED'
              ? 'bg-rose-500/10 text-rose-400 border-b border-rose-500/20'
              : 'bg-amber-500/10 text-amber-400 border-b border-amber-500/20'
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

      <div className="p-5 sm:p-6 grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
        {/* Airline & Aircraft Info */}
        <div className="lg:col-span-3 flex items-center gap-3.5">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-tr from-sky-500/20 to-indigo-500/20 border border-sky-500/30 flex items-center justify-center shrink-0">
            <Plane className="w-6 h-6 text-sky-400" />
          </div>
          <div>
            <h3 className="font-bold text-white text-base leading-snug">{flight.airline}</h3>
            <div className="flex items-center gap-2 text-xs text-slate-400 mt-0.5">
              <span className="font-mono text-slate-300 font-semibold">{flight.flightNumber}</span>
              <span>•</span>
              <span className="text-slate-500">{flight.aircraftModel}</span>
            </div>
          </div>
        </div>

        {/* Departure -> Duration -> Arrival Timings */}
        <div className="lg:col-span-5 flex items-center justify-between gap-4">
          {/* Departure */}
          <div className="text-left">
            <p className="text-xl font-extrabold text-white">{formatTime(depDate)}</p>
            <p className="text-xs font-bold text-sky-400 mt-0.5">{flight.departureAirport.code}</p>
            <p className="text-[11px] text-slate-400">{flight.departureAirport.city}</p>
            {flight.departureAirport.terminal && (
              <span className="inline-block mt-1 text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                {flight.departureAirport.terminal}
              </span>
            )}
          </div>

          {/* Duration Graphic */}
          <div className="flex-1 flex flex-col items-center px-2">
            <span className="text-[11px] text-slate-400 font-medium flex items-center gap-1">
              <Clock className="w-3 h-3 text-slate-500" />
              {formatDuration(flight.durationMinutes)}
            </span>
            <div className="w-full flex items-center my-1.5">
              <div className="h-0.5 w-full bg-slate-700 relative">
                <div className="absolute -top-1 left-1/2 transform -translate-x-1/2 w-2 h-2 rounded-full bg-sky-400"></div>
              </div>
            </div>
            <span className="text-[10px] font-semibold text-emerald-400 uppercase tracking-wider">
              {flight.stops === 0 ? 'Non-Stop' : `${flight.stops} Stop(s)`}
            </span>
          </div>

          {/* Arrival */}
          <div className="text-right">
            <p className="text-xl font-extrabold text-white">{formatTime(arrDate)}</p>
            <p className="text-xs font-bold text-sky-400 mt-0.5">{flight.arrivalAirport.code}</p>
            <p className="text-[11px] text-slate-400">{flight.arrivalAirport.city}</p>
            {flight.arrivalAirport.terminal && (
              <span className="inline-block mt-1 text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                {flight.arrivalAirport.terminal}
              </span>
            )}
          </div>
        </div>

        {/* Pricing & Booking CTA */}
        <div className="lg:col-span-4 flex flex-row lg:flex-col items-center lg:items-end justify-between gap-3 pt-3 lg:pt-0 border-t lg:border-t-0 border-slate-800">
          <div className="text-left lg:text-right">
            <div className="flex items-baseline gap-1.5 lg:justify-end">
              <span className="text-2xl font-black text-white">
                ₹{totalPrice.toLocaleString('en-IN')}
              </span>
              {passengerCount > 1 && (
                <span className="text-xs text-slate-400">
                  (₹{pricePerPax.toLocaleString('en-IN')}/pax)
                </span>
              )}
            </div>
            <div className="flex items-center gap-2 mt-0.5 text-[11px] text-slate-400">
              <span className="text-slate-500">Taxes & fees included</span>
              <span>•</span>
              <span
                className={`font-semibold ${
                  availableSeats < 10 ? 'text-amber-400' : 'text-slate-400'
                }`}
              >
                {availableSeats} seats left
              </span>
            </div>
          </div>

          <button
            type="button"
            onClick={handleSelectFlight}
            disabled={!isBookable}
            className={`w-full sm:w-auto px-5 py-2.5 rounded-xl font-semibold text-sm transition duration-150 flex items-center justify-center gap-2 shadow-md ${
              isBookable
                ? 'bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white shadow-sky-500/20'
                : 'bg-slate-800 text-slate-500 cursor-not-allowed border border-slate-700'
            }`}
          >
            <span>{isBookable ? 'Select Flight' : 'Unavailable'}</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Flight Perks Footer */}
      <div className="px-5 py-2.5 bg-slate-950/60 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-3 text-[11px] text-slate-400">
        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1">
            <Luggage className="w-3.5 h-3.5 text-slate-500" />
            Check-in 15kg + Cabin 7kg
          </span>
          <span className="flex items-center gap-1 text-emerald-400">
            <ShieldCheck className="w-3.5 h-3.5" />
            Instant Confirmation
          </span>
        </div>

        <div className="flex items-center gap-2 text-slate-500">
          <span>Cabin:</span>
          <span className="font-semibold text-slate-300">{selectedCabin.replace('_', ' ')}</span>
        </div>
      </div>
    </div>
  );
};
