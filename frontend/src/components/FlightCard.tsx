import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
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
  Compass,
  ChevronDown,
  ChevronUp
} from 'lucide-react';
import { Flight, CabinClass } from '../types/api';
import { PriceHistoryModal } from './PriceHistoryModal';
import { PriceFreezeModal } from './PriceFreezeModal';
import { PriceBreakdownCard } from './PriceBreakdownCard';
import { AirlineLogo } from './AirlineLogo';
import { AircraftBadge } from './AircraftBadge';
import { AnimatedPrice } from './AnimatedPrice';
import { flightTrackingService } from '../services/flightTrackingService';
import { useAuth } from '../context/AuthContext';
import { useFlightPricingWebSocket } from '../hooks/useFlightPricingWebSocket';
import { cardEntranceVariants } from '../lib/motion';

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

  // Real-time WebSocket dynamic pricing subscription
  const { latestEvent, updatedPrice } = useFlightPricingWebSocket(flight.id, selectedCabin);

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

  const initialPricePerPax = cabinInv ? cabinInv.totalPrice : flight.basePrice;
  const pricePerPax = updatedPrice != null ? updatedPrice : initialPricePerPax;
  const totalPrice = pricePerPax * passengerCount;
  const availableSeats = latestEvent?.availableSeats != null
    ? latestEvent.availableSeats
    : (cabinInv ? cabinInv.availableSeats : flight.availableSeats);

  const isDisrupted = flight.status === 'DELAYED' || flight.status === 'CANCELLED';
  const isBookable =
    (flight.isBookable === undefined || flight.isBookable === true) &&
    availableSeats >= passengerCount &&
    flight.status !== 'CANCELLED' &&
    flight.status !== 'ARRIVED';

  const handleSelectFlight = () => {
    const targetUrl = `/book/${flight.id}?cabinClass=${selectedCabin}&passengers=${passengerCount}`;
    if (!isAuthenticated) {
      navigate(`/login?redirect=${encodeURIComponent(targetUrl)}`);
      return;
    }
    navigate(targetUrl, { state: { flight } });
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
      <motion.div
        variants={cardEntranceVariants}
        whileHover={{ y: -4, transition: { duration: 0.2, ease: [0.22, 1, 0.36, 1] } }}
        className={`rounded-2xl border transition-colors duration-300 overflow-hidden bg-[#14161F] shadow-lg ${
          isDisrupted
            ? 'border-amber-400/80 shadow-amber-500/10'
            : 'border-white/10 hover:border-amber-500/40 hover:shadow-card-hover'
        }`}
      >
        {/* Disruption Alert Banner */}
        {isDisrupted && (
          <div
            className={`px-5 py-2 text-xs font-semibold flex items-center gap-2 ${
              flight.status === 'CANCELLED'
                ? 'bg-rose-500/15 text-rose-300 border-b border-rose-500/30'
                : 'bg-amber-500/15 text-amber-300 border-b border-amber-500/30'
            }`}
          >
            <AlertTriangle className="w-3.5 h-3.5 shrink-0" />
            <span>
              {flight.status === 'CANCELLED'
                ? `Flight Cancelled: ${flight.cancellationReason || 'Operational constraint'}`
                : `Flight Delayed by ${flight.delayMinutes || 0} mins (Revised Departure: ${
                    flight.revisedDepartureTime
                      ? `${formatTimeParts(new Date(flight.revisedDepartureTime)).time} ${formatTimeParts(new Date(flight.revisedDepartureTime)).period}`
                      : 'TBD'
                  })`}
            </span>
          </div>
        )}

        {/* Card Top Header Strip */}
        <div className="px-5 sm:px-6 py-2.5 bg-[#181A22] border-b border-white/5 flex flex-wrap items-center justify-between gap-2 text-xs text-slate-400">
          <div className="flex items-center gap-3">
            <span className="flex items-center gap-1.5 font-medium text-slate-300">
              <Luggage className="w-3.5 h-3.5 text-amber-400" />
              Check-in 15kg • Cabin 7kg
            </span>
            <span className="text-white/20">•</span>
            <span className="flex items-center gap-1 text-emerald-400 font-medium">
              <ShieldCheck className="w-3.5 h-3.5" />
              Instant Boarding Pass
            </span>
          </div>

          <div className="flex items-center gap-2">
            {flight.dataSource === 'LIVE' && (
              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px] font-bold">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                Live Radar Feed
              </span>
            )}
            <span className="font-semibold text-slate-400">
              Class: <strong className="text-amber-400 font-bold uppercase">{selectedCabin}</strong>
            </span>
          </div>
        </div>

        {/* Main Card Body */}
        <div className="p-5 sm:p-6 flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-5 lg:gap-6">
          {/* 1. Airline Info */}
          <div className="flex items-center gap-3.5 lg:w-60 shrink-0">
            <AirlineLogo airline={flight.airline} airlineCode={flight.airlineCode} size="lg" />
            <div className="min-w-0 flex-1">
              <h3 className="font-bold text-white text-[15px] leading-snug truncate" title={flight.airline}>
                {flight.airline}
              </h3>
              <div className="flex items-center gap-1.5 text-xs text-slate-400 mt-1 flex-wrap">
                <span className="font-mono text-amber-400 font-bold bg-[#12131A] px-1.5 py-0.5 rounded border border-white/10 text-[11px]">
                  {flight.flightNumber}
                </span>
                <AircraftBadge aircraftModel={flight.aircraftModel} />
              </div>
            </div>
          </div>

          {/* 2. Route & Times */}
          <div className="flex-1 flex items-center justify-between gap-3 sm:gap-6 px-1 sm:px-4 py-3 lg:py-0 border-y lg:border-y-0 border-white/5">
            {/* Departure */}
            <div className="text-left shrink-0">
              <div className="flex items-baseline gap-1">
                <span className="text-2xl font-black text-white tracking-tight leading-none">{depTime.time}</span>
                <span className="text-xs font-bold text-slate-400 uppercase">{depTime.period}</span>
              </div>
              <p className="text-xs font-black text-amber-400 uppercase tracking-wider mt-1">{flight.departureAirport.code}</p>
              <p className="text-xs text-slate-300 font-medium truncate max-w-[110px]">{flight.departureAirport.city}</p>
              {flight.departureAirport.terminal && (
                <span className="inline-block mt-1 text-[10px] font-semibold px-2 py-0.5 rounded-md bg-[#181A22] text-slate-300 border border-white/10">
                  {flight.departureAirport.terminal}
                </span>
              )}
            </div>

            {/* Duration Visualizer */}
            <div className="flex-1 flex flex-col items-center px-2 max-w-[150px]">
              <span className="text-xs text-slate-400 font-medium flex items-center gap-1 whitespace-nowrap">
                <Clock className="w-3.5 h-3.5 text-amber-400" />
                {formatDuration(flight.durationMinutes)}
              </span>
              <div className="w-full flex items-center my-2">
                <div className="h-0.5 w-full bg-white/10 relative">
                  <div className="absolute -top-1.5 left-1/2 transform -translate-x-1/2 w-3 h-3 rounded-full bg-amber-400 flex items-center justify-center shadow-glow-gold">
                    <Plane className="w-2 h-2 text-black transform rotate-45" />
                  </div>
                </div>
              </div>
              <span className="text-[10px] font-bold text-slate-300 uppercase tracking-wide px-2 py-0.5 rounded-full bg-[#181A22] border border-white/10 whitespace-nowrap">
                {!flight.stops || flight.stops === 0 ? 'Non-Stop' : flight.stops === 1 ? '1 Stop' : `${flight.stops} Stops`}
              </span>
            </div>

            {/* Arrival */}
            <div className="text-right shrink-0">
              <div className="flex items-baseline justify-end gap-1">
                <span className="text-2xl font-black text-white tracking-tight leading-none">{arrTime.time}</span>
                <span className="text-xs font-bold text-slate-400 uppercase">{arrTime.period}</span>
              </div>
              <p className="text-xs font-black text-amber-400 uppercase tracking-wider mt-1">{flight.arrivalAirport.code}</p>
              <p className="text-xs text-slate-300 font-medium truncate max-w-[110px]">{flight.arrivalAirport.city}</p>
              {flight.arrivalAirport.terminal && (
                <span className="inline-block mt-1 text-[10px] font-semibold px-2 py-0.5 rounded-md bg-[#181A22] text-slate-300 border border-white/10">
                  {flight.arrivalAirport.terminal}
                </span>
              )}
            </div>
          </div>

          {/* 3. Pricing & Select CTA */}
          <div className="flex flex-row lg:flex-col items-center lg:items-end justify-between lg:justify-center gap-3 lg:min-w-[190px] shrink-0 lg:pl-6 lg:border-l lg:border-white/10">
            <div className="text-left lg:text-right">
              <div className="flex items-baseline gap-1.5 lg:justify-end">
                <AnimatedPrice
                  value={totalPrice}
                  className="text-2xl font-black text-amber-400 tracking-tight"
                />
                {passengerCount > 1 && (
                  <span className="text-xs text-slate-400 font-medium">
                    (₹{pricePerPax.toLocaleString('en-IN')}/pax)
                  </span>
                )}
              </div>

              {latestEvent && (
                <motion.div
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  className="flex items-center gap-1 text-[10px] font-bold text-emerald-400 lg:justify-end mt-0.5"
                >
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                  <span>Live Fare Active</span>
                </motion.div>
              )}

              <p className="text-[11px] text-slate-400 mt-0.5 flex items-center gap-1.5 lg:justify-end">
                <span>Taxes incl.</span>
                <span className="text-white/20">•</span>
                <span
                  className={`font-semibold ${
                    availableSeats < 10 ? 'text-accent' : 'text-slate-400'
                  }`}
                >
                  {availableSeats < 10 && <Flame className="w-3 h-3 text-accent inline mr-0.5" />}
                  {availableSeats} seats left
                </span>
              </p>
            </div>

            <motion.button
              whileHover={{ scale: isBookable ? 1.02 : 1 }}
              whileTap={{ scale: isBookable ? 0.97 : 1 }}
              type="button"
              onClick={handleSelectFlight}
              disabled={!isBookable}
              className={`w-full sm:w-auto lg:w-36 px-4 py-2.5 rounded-xl font-black text-xs sm:text-sm transition duration-200 flex items-center justify-center gap-1.5 cursor-pointer whitespace-nowrap ${
                isBookable
                  ? 'bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black shadow-glow-gold'
                  : 'bg-slate-800 text-slate-500 cursor-not-allowed border border-white/5'
              }`}
            >
              <span>{isBookable ? 'Select Flight' : 'Sold Out'}</span>
              {isBookable && <ChevronRight className="w-4 h-4 text-black" />}
            </motion.button>
          </div>
        </div>

        {/* Feature Action Bar (Price History, Freeze Price, Live Radar, Fare Breakdown) */}
        <div className="px-5 sm:px-6 py-2.5 bg-[#181A22] border-t border-white/5 flex flex-wrap items-center justify-between gap-2 text-xs">
          <div className="flex items-center gap-2 flex-wrap">
            <motion.button
              whileTap={{ scale: 0.95 }}
              type="button"
              onClick={() => setShowPriceHistory(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#14161F] hover:bg-[#1F222E] text-slate-300 hover:text-white border border-white/10 text-xs font-semibold transition"
            >
              <TrendingUp className="w-3.5 h-3.5 text-amber-400" />
              <span>Price Trend</span>
            </motion.button>

            <motion.button
              whileTap={{ scale: 0.95 }}
              type="button"
              onClick={() => setShowPriceFreeze(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#14161F] hover:bg-[#1F222E] text-slate-300 hover:text-white border border-white/10 text-xs font-semibold transition"
            >
              <Lock className="w-3.5 h-3.5 text-accent" />
              <span>Freeze Fare</span>
            </motion.button>

            <motion.button
              whileTap={{ scale: 0.95 }}
              type="button"
              onClick={handleTrackFlight}
              disabled={trackingLoading}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-xs font-semibold transition ${
                isTracked
                  ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                  : 'bg-[#14161F] hover:bg-[#1F222E] text-slate-300 border-white/10'
              }`}
            >
              <Radio className={`w-3.5 h-3.5 ${isTracked ? 'text-emerald-400' : 'text-slate-400'}`} />
              <span>{isTracked ? 'Tracking Active' : 'Track Flight'}</span>
            </motion.button>

            <motion.button
              whileTap={{ scale: 0.95 }}
              type="button"
              onClick={() => navigate(`/tracked-flights?flight=${flight.flightNumber}`)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#14161F] hover:bg-[#1F222E] text-amber-400 border border-white/10 text-xs font-semibold transition"
            >
              <Compass className="w-3.5 h-3.5 text-amber-400" />
              <span>Live Radar</span>
            </motion.button>

            {trackMessage && (
              <span className="text-[11px] text-amber-400 font-semibold animate-fade-in">{trackMessage}</span>
            )}
          </div>

          <button
            type="button"
            onClick={() => setShowBreakdown(!showBreakdown)}
            className="text-slate-400 hover:text-white font-semibold flex items-center gap-1 transition"
          >
            <span>{showBreakdown ? 'Hide Fare Details' : 'View Fare Breakdown'}</span>
            {showBreakdown ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
          </button>
        </div>

        {/* Collapsible Fare Breakdown with AnimatePresence */}
        <AnimatePresence>
          {showBreakdown && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              transition={{ duration: 0.25, ease: [0.22, 1, 0.36, 1] }}
              className="overflow-hidden border-t border-white/10 bg-[#12131A]"
            >
              <div className="p-4 sm:p-5">
                <PriceBreakdownCard
                  flightId={flight.id}
                  cabinClass={selectedCabin}
                  passengerCount={passengerCount}
                />
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>

      {/* Modals with AnimatePresence */}
      <AnimatePresence>
        {showPriceHistory && (
          <PriceHistoryModal
            flightId={flight.id}
            flightNumber={flight.flightNumber}
            onClose={() => setShowPriceHistory(false)}
          />
        )}
      </AnimatePresence>

      <AnimatePresence>
        {showPriceFreeze && (
          <PriceFreezeModal
            flightId={flight.id}
            flightNumber={flight.flightNumber}
            cabinClass={selectedCabin}
            passengerCount={passengerCount}
            currentPrice={pricePerPax}
            onClose={() => setShowPriceFreeze(false)}
            onFreezeCreated={() => {
              setShowPriceFreeze(false);
              alert(`Fare of ₹${totalPrice.toLocaleString()} locked for 48 hours!`);
            }}
          />
        )}
      </AnimatePresence>
    </>
  );
};
