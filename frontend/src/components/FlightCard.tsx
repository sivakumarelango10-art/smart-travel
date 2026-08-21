import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plane,
  Clock,
  Luggage,
  ShieldCheck,
  ChevronRight,
  AlertTriangle,
  Flame,
  TrendingUp,
  Lock,
  Radio,
  ChevronDown,
  ChevronUp
} from 'lucide-react';
import { Flight, CabinClass } from '../types/api';
import { PriceHistoryModal } from './PriceHistoryModal';
import { PriceFreezeModal } from './PriceFreezeModal';
import { PriceBreakdownCard } from './PriceBreakdownCard';
import { AirlineLogo } from './AirlineLogo';
import { flightTrackingService } from '../services/flightTrackingService';
import { useAuth } from '../context/AuthContext';

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
  const { isAuthenticated } = useAuth();

  const [showPriceHistory, setShowPriceHistory] = useState(false);
  const [showPriceFreeze, setShowPriceFreeze] = useState(false);
  const [showBreakdown, setShowBreakdown] = useState(false);
  const [isTracked, setIsTracked] = useState(false);
  const [trackingLoading, setTrackingLoading] = useState(false);
  const [trackMessage, setTrackMessage] = useState<string | null>(null);

  const depDate = new Date(flight.departureTime);
  const arrDate = new Date(flight.arrivalTime);

  const formatTimeParts = (date: Date) => {
    const hours = date.getHours();
    const mins = date.getMinutes().toString().padStart(2, '0');
    const period = hours >= 12 ? 'PM' : 'AM';
    const h12 = hours % 12 || 12;
    return {
      time: `${h12.toString().padStart(2, '0')}:${mins}`,
      period,
    };
  };

  const depTime = formatTimeParts(depDate);
  const arrTime = formatTimeParts(arrDate);

  const formatDuration = (mins: number) => {
    const hours = Math.floor(mins / 60);
    const m = mins % 60;
    return `${hours}h ${m > 0 ? `${m}m` : '00m'}`;
  };

  // Find matching cabin inventory
  const cabinInv =
    flight.cabinInventories?.find((c) => c.cabinClass === selectedCabin) ||
    flight.cabinInventories?.[0];

  const pricePerPax = cabinInv ? cabinInv.totalPrice : flight.basePrice;
  const totalPrice = pricePerPax * passengerCount;
  const availableSeats = cabinInv ? cabinInv.availableSeats : flight.availableSeats;

  const isDisrupted = flight.status === 'DELAYED' || flight.status === 'CANCELLED';
  const isBookable =
    (flight.isBookable === undefined || flight.isBookable === true) &&
    availableSeats >= passengerCount &&
    flight.status !== 'CANCELLED' &&
    flight.status !== 'ARRIVED';

  const handleSelectFlight = () => {
    navigate(`/book/${flight.id}?cabinClass=${selectedCabin}&passengers=${passengerCount}`);
  };

  const handleTrackFlight = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!isAuthenticated) {
      alert('Please sign in to track this flight in real time.');
      return;
    }

    setTrackingLoading(true);
    try {
      if (isTracked) {
        await flightTrackingService.untrackFlight(flight.id);
        setIsTracked(false);
        setTrackMessage('Unsubscribed from live updates');
      } else {
        await flightTrackingService.trackFlight(flight.id);
        setIsTracked(true);
        setTrackMessage('Subscribed to live status alerts!');
      }
      setTimeout(() => setTrackMessage(null), 3000);
    } catch (err: any) {
      alert(err.message || 'Failed to update tracking');
    } finally {
      setTrackingLoading(false);
    }
  };

  return (
    <>
      <div
        className={`rounded-2xl border transition-all duration-200 overflow-hidden ${
          isDisrupted
            ? 'bg-slate-900/95 border-amber-500/30 shadow-lg shadow-amber-500/5'
            : 'bg-slate-900/90 hover:bg-slate-900 border-slate-800 hover:border-slate-700/80 shadow-lg hover:shadow-xl'
        }`}
      >
        {/* Disruption Alert Banner */}
        {isDisrupted && (
          <div
            className={`px-5 py-2 text-xs font-semibold flex items-center gap-2 ${
              flight.status === 'CANCELLED'
                ? 'bg-rose-500/15 text-rose-400 border-b border-rose-500/20'
                : 'bg-amber-500/15 text-amber-400 border-b border-amber-500/20'
            }`}
          >
            <AlertTriangle className="w-3.5 h-3.5 shrink-0" />
            <span>
              {flight.status === 'CANCELLED'
                ? `Flight Cancelled: ${flight.cancellationReason || 'Operational constraint'}`
                : `Flight Delayed by ${flight.delayMinutes || 0} minutes (Revised Departure: ${
                    flight.revisedDepartureTime
                      ? `${formatTimeParts(new Date(flight.revisedDepartureTime)).time} ${formatTimeParts(new Date(flight.revisedDepartureTime)).period}`
                      : 'TBD'
                  })`}
            </span>
          </div>
        )}

        {/* Card Header Strip */}
        <div className="px-5 sm:px-6 py-2 bg-slate-950/40 border-b border-slate-800/60 flex flex-wrap items-center justify-between gap-3 text-[11px] text-slate-400">
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1.5 font-medium">
              <Luggage className="w-3.5 h-3.5 text-slate-500" />
              Check-in 15kg • Cabin 7kg included
            </span>
            <span className="hidden sm:inline text-slate-700">•</span>
            <span className="hidden sm:flex items-center gap-1 text-emerald-400/90 font-medium">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
              Instant E-Ticket & Pass
            </span>
          </div>
          <div className="flex items-center gap-2 ml-auto">
            <span className="font-semibold text-slate-400">
              Cabin: <strong className="text-sky-400 font-bold uppercase">{selectedCabin}</strong>
            </span>
          </div>
        </div>

        {/* Main Card Body */}
        <div className="p-5 sm:p-6 flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-5 lg:gap-6">
          {/* 1. Airline & Aircraft Info */}
          <div className="flex items-center gap-3.5 lg:w-56 shrink-0">
            <AirlineLogo airline={flight.airline} airlineCode={flight.airlineCode} size="lg" />
            <div className="min-w-0 flex-1">
              <h3 className="font-bold text-white text-[15px] leading-snug truncate" title={flight.airline}>
                {flight.airline}
              </h3>
              <div className="flex items-center gap-1.5 text-xs text-slate-400 mt-0.5 flex-wrap">
                <span className="font-mono text-slate-300 font-semibold bg-slate-950 px-1.5 py-0.5 rounded border border-slate-800 text-[11px] whitespace-nowrap">
                  {flight.flightNumber}
                </span>
                <span className="text-slate-400 text-[11px] truncate">{flight.aircraftModel}</span>
              </div>
            </div>
          </div>

          {/* 2. Route & Timings Visualizer */}
          <div className="flex-1 flex items-center justify-between gap-3 sm:gap-6 px-1 sm:px-4 py-3 lg:py-0 border-y lg:border-y-0 border-slate-800/70">
            {/* Departure */}
            <div className="text-left shrink-0">
              <div className="flex items-baseline gap-1">
                <span className="text-2xl font-black text-white tracking-tight leading-none">{depTime.time}</span>
                <span className="text-xs font-bold text-slate-400 uppercase">{depTime.period}</span>
              </div>
              <p className="text-xs font-extrabold text-sky-400 uppercase tracking-wide mt-1.5">{flight.departureAirport.code}</p>
              <p className="text-xs text-slate-300 font-medium truncate max-w-[110px]">{flight.departureAirport.city}</p>
              {flight.departureAirport.terminal && (
                <span className="inline-block mt-1 text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-800/80 text-slate-400 border border-slate-700">
                  {flight.departureAirport.terminal}
                </span>
              )}
            </div>

            {/* Duration Graphic */}
            <div className="flex-1 flex flex-col items-center px-2 max-w-[150px]">
              <span className="text-xs text-slate-400 font-medium flex items-center gap-1 whitespace-nowrap">
                <Clock className="w-3.5 h-3.5 text-slate-500" />
                {formatDuration(flight.durationMinutes)}
              </span>
              <div className="w-full flex items-center my-2">
                <div className="h-0.5 w-full bg-slate-700/80 relative">
                  <div className="absolute -top-1.5 left-1/2 transform -translate-x-1/2 w-3 h-3 rounded-full bg-blue-500 flex items-center justify-center">
                    <Plane className="w-2 h-2 text-white transform rotate-45" />
                  </div>
                </div>
              </div>
              <span className="text-[10px] font-semibold text-slate-300 uppercase tracking-wide px-2 py-0.5 rounded-md bg-slate-800 border border-slate-700 whitespace-nowrap">
                {!flight.stops || flight.stops === 0 ? 'Non-Stop' : flight.stops === 1 ? '1 Stop' : `${flight.stops} Stops`}
              </span>
            </div>

            {/* Arrival */}
            <div className="text-right shrink-0">
              <div className="flex items-baseline justify-end gap-1">
                <span className="text-2xl font-black text-white tracking-tight leading-none">{arrTime.time}</span>
                <span className="text-xs font-bold text-slate-400 uppercase">{arrTime.period}</span>
              </div>
              <p className="text-xs font-bold text-slate-200 uppercase tracking-wide mt-1.5">{flight.arrivalAirport.code}</p>
              <p className="text-xs text-slate-400 font-normal truncate max-w-[110px]">{flight.arrivalAirport.city}</p>
              {flight.arrivalAirport.terminal && (
                <span className="inline-block mt-1 text-[10px] font-medium px-2 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                  {flight.arrivalAirport.terminal}
                </span>
              )}
            </div>
          </div>

          {/* 3. Fare & Booking Button */}
          <div className="flex flex-row lg:flex-col items-center lg:items-end justify-between lg:justify-center gap-3 lg:min-w-[210px] shrink-0 lg:pl-6 lg:border-l lg:border-slate-800">
            <div className="text-left lg:text-right">
              <div className="flex items-baseline gap-1.5 lg:justify-end">
                <span className="text-2xl font-bold text-white tracking-tight">
                  ₹{totalPrice.toLocaleString('en-IN')}
                </span>
                {passengerCount > 1 && (
                  <span className="text-xs text-slate-400 font-normal">
                    (₹{pricePerPax.toLocaleString('en-IN')}/pax)
                  </span>
                )}
              </div>
              <p className="text-[11px] text-slate-400 whitespace-nowrap mt-0.5 flex items-center gap-1.5 lg:justify-end">
                <span>Taxes incl.</span>
                <span className="text-slate-600">•</span>
                <span
                  className={`font-medium flex items-center gap-1 ${
                    availableSeats < 10 ? 'text-amber-400' : 'text-slate-400'
                  }`}
                >
                  {availableSeats < 10 && <Flame className="w-3 h-3 text-amber-400" />}
                  {availableSeats} seats left
                </span>
              </p>
            </div>

            <button
              type="button"
              onClick={handleSelectFlight}
              disabled={!isBookable}
              className={`w-full sm:w-auto lg:w-36 px-4 py-2.5 rounded-lg font-semibold text-xs sm:text-sm transition duration-150 flex items-center justify-center gap-1.5 cursor-pointer whitespace-nowrap ${
                isBookable
                  ? 'bg-blue-600 hover:bg-blue-700 text-white'
                  : 'bg-slate-800 text-slate-500 cursor-not-allowed border border-slate-700'
              }`}
            >
              <span>{isBookable ? 'Select Flight' : 'Sold Out'}</span>
              {isBookable && <ChevronRight className="w-4 h-4 shrink-0" />}
            </button>
          </div>
        </div>

        {/* Feature Action Bar (Price History, Freeze Price, Live Tracking, Fare Breakdown) */}
        <div className="px-5 sm:px-6 py-2 bg-slate-950/60 border-t border-slate-800 flex flex-wrap items-center justify-between gap-2 text-xs">
          <div className="flex items-center gap-2 flex-wrap">
            <button
              type="button"
              onClick={() => setShowPriceHistory(true)}
              className="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 transition duration-150"
            >
              <TrendingUp className="w-3.5 h-3.5 text-blue-400" />
              <span>Price Trend</span>
            </button>

            <button
              type="button"
              onClick={() => setShowPriceFreeze(true)}
              className="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 transition duration-150"
            >
              <Lock className="w-3.5 h-3.5 text-blue-400" />
              <span>Freeze Fare</span>
            </button>

            <button
              type="button"
              onClick={handleTrackFlight}
              disabled={trackingLoading}
              className={`flex items-center gap-1.5 px-2.5 py-1 rounded-md border transition duration-150 ${
                isTracked
                  ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400 font-medium'
                  : 'bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border-slate-700'
              }`}
            >
              <Radio className={`w-3.5 h-3.5 ${isTracked ? 'text-emerald-400' : 'text-slate-400'}`} />
              <span>{isTracked ? 'Tracking Live' : 'Track Status'}</span>
            </button>

            {trackMessage && (
              <span className="text-[11px] text-blue-400 animate-fade-in">{trackMessage}</span>
            )}
          </div>

          <button
            type="button"
            onClick={() => setShowBreakdown(!showBreakdown)}
            className="flex items-center gap-1 text-slate-400 hover:text-white transition-colors text-[11px] font-medium ml-auto"
          >
            <span>{showBreakdown ? 'Hide Fare Breakdown' : 'Fare Breakdown'}</span>
            {showBreakdown ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
          </button>
        </div>

        {/* Collapsible Dynamic Price Breakdown */}
        {showBreakdown && (
          <div className="p-4 bg-slate-950/90 border-t border-slate-800/80">
            <PriceBreakdownCard
              flightId={flight.id}
              cabinClass={selectedCabin}
              passengerCount={passengerCount}
              onFreezeClick={() => setShowPriceFreeze(true)}
            />
          </div>
        )}
      </div>

      {/* Modals */}
      {showPriceHistory && (
        <PriceHistoryModal
          flightId={flight.id}
          flightNumber={flight.flightNumber}
          cabinClass={selectedCabin}
          onClose={() => setShowPriceHistory(false)}
        />
      )}

      {showPriceFreeze && (
        <PriceFreezeModal
          flightId={flight.id}
          flightNumber={flight.flightNumber}
          cabinClass={selectedCabin}
          passengerCount={passengerCount}
          currentPrice={totalPrice}
          onClose={() => setShowPriceFreeze(false)}
          onFreezeCreated={() => {
            setShowPriceFreeze(false);
            alert(`Price locked for flight ${flight.flightNumber}! You can use this rate during booking.`);
          }}
        />
      )}
    </>
  );
};
