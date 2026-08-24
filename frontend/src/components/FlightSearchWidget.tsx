import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  PlaneTakeoff,
  PlaneLanding,
  Calendar,
  Users,
  ArrowLeftRight,
  Search,
  Plane,
  Building2,
  ChevronDown
} from 'lucide-react';
import { CabinClass } from '../types/api';
import { flightService } from '../services/flightService';

interface FlightSearchWidgetProps {
  initialOrigin?: string;
  initialDestination?: string;
  initialDate?: string;
  initialCabin?: CabinClass;
  initialPassengers?: number;
  onSearch?: (params: any) => void;
  compact?: boolean;
}

export const FlightSearchWidget: React.FC<FlightSearchWidgetProps> = ({
  initialOrigin = 'DEL',
  initialDestination = 'BOM',
  initialDate,
  initialCabin = 'ECONOMY',
  initialPassengers = 1,
  onSearch,
  compact = false,
}) => {
  const navigate = useNavigate();
  const airports = flightService.getAirports();

  const defaultDate = initialDate || new Date(Date.now() + 86400000).toISOString().split('T')[0];

  const [tripType, setTripType] = useState<'ONE_WAY' | 'ROUND_TRIP'>('ONE_WAY');
  const [origin, setOrigin] = useState<string>(initialOrigin);
  const [destination, setDestination] = useState<string>(initialDestination);
  const [departureDate, setDepartureDate] = useState<string>(defaultDate);
  const [returnDate, setReturnDate] = useState<string>(
    new Date(Date.now() + 3 * 86400000).toISOString().split('T')[0]
  );
  const [cabinClass, setCabinClass] = useState<CabinClass>(initialCabin);
  const [passengers, setPassengers] = useState<number>(initialPassengers);
  const [error, setError] = useState<string | null>(null);

  const selectedOriginAirport = airports.find((a) => a.code === origin) || airports[0];
  const selectedDestAirport = airports.find((a) => a.code === destination) || airports[1];

  const handleSwap = () => {
    setOrigin(destination);
    setDestination(origin);
    setError(null);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (origin === destination) {
      setError('Origin and destination airports must be different.');
      return;
    }
    setError(null);

    const searchParams = {
      origin,
      destination,
      departureDate,
      cabinClass,
      passengers,
    };

    if (onSearch) {
      onSearch(searchParams);
    } else {
      const query = new URLSearchParams({
        origin,
        destination,
        departureDate,
        cabinClass,
        passengers: passengers.toString(),
      }).toString();
      navigate(`/flights?${query}`);
    }
  };

  return (
    <div className={`w-full ${compact ? '' : 'max-w-5xl mx-auto'}`}>
      {/* Search Header Tabs */}
      {!compact && (
        <div className="flex items-center gap-2 mb-3 px-1">
          <div className="flex items-center gap-2 px-4 py-2 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-glow-gold">
            <Plane className="w-4 h-4 text-black" />
            <span>Search Flights</span>
          </div>

          <button
            type="button"
            onClick={() => navigate('/hotels')}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#14161F]/80 hover:bg-[#1F222E] text-slate-300 text-xs font-semibold border border-white/10 shadow-sm transition hover:text-white"
          >
            <Building2 className="w-4 h-4 text-amber-400" />
            <span>Book Hotels</span>
          </button>
        </div>
      )}

      {/* Main Search Panel */}
      <form
        onSubmit={handleSubmit}
        className="rounded-2xl bg-[#12131A]/95 backdrop-blur-2xl border border-white/10 shadow-2xl p-5 sm:p-6 space-y-4"
      >
        {/* Top Controls: Trip Type, Cabin & Passengers */}
        <div className="flex flex-wrap items-center justify-between gap-3 pb-3 border-b border-white/10">
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="tripType"
                value="ONE_WAY"
                checked={tripType === 'ONE_WAY'}
                onChange={() => setTripType('ONE_WAY')}
                className="w-4 h-4 text-amber-400 focus:ring-amber-400 bg-[#1A1C24] border-white/20 accent-amber-400"
              />
              <span className={`text-xs font-bold transition ${tripType === 'ONE_WAY' ? 'text-amber-400' : 'text-slate-400 hover:text-slate-300'}`}>
                One Way
              </span>
            </label>

            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="tripType"
                value="ROUND_TRIP"
                checked={tripType === 'ROUND_TRIP'}
                onChange={() => setTripType('ROUND_TRIP')}
                className="w-4 h-4 text-amber-400 focus:ring-amber-400 bg-[#1A1C24] border-white/20 accent-amber-400"
              />
              <span className={`text-xs font-bold transition ${tripType === 'ROUND_TRIP' ? 'text-amber-400' : 'text-slate-400 hover:text-slate-300'}`}>
                Round Trip
              </span>
            </label>
          </div>

          <div className="flex items-center gap-2.5">
            {/* Cabin Class */}
            <div className="relative">
              <select
                value={cabinClass}
                onChange={(e) => setCabinClass(e.target.value as CabinClass)}
                className="bg-[#1A1C24] border border-white/10 text-slate-200 text-xs font-semibold rounded-xl px-3 py-2 pr-7 focus:outline-none focus:border-amber-400 transition appearance-none cursor-pointer"
              >
                <option value="ECONOMY">Economy Class</option>
                <option value="PREMIUM_ECONOMY">Premium Economy</option>
                <option value="BUSINESS">Business Class</option>
                <option value="FIRST">First Class</option>
              </select>
              <ChevronDown className="w-3.5 h-3.5 text-slate-400 absolute right-2.5 top-2.5 pointer-events-none" />
            </div>

            {/* Passengers */}
            <div className="relative flex items-center bg-[#1A1C24] border border-white/10 rounded-xl px-3 py-2 text-xs font-semibold text-slate-200">
              <Users className="w-3.5 h-3.5 text-amber-400 mr-2" />
              <select
                value={passengers}
                onChange={(e) => setPassengers(parseInt(e.target.value, 10))}
                className="bg-transparent text-slate-200 focus:outline-none cursor-pointer pr-4 appearance-none font-semibold"
              >
                {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
                  <option key={num} value={num} className="bg-[#1A1C24]">
                    {num} {num === 1 ? 'Traveler' : 'Travelers'}
                  </option>
                ))}
              </select>
              <ChevronDown className="w-3.5 h-3.5 text-slate-400 absolute right-2 top-2.5 pointer-events-none" />
            </div>
          </div>
        </div>

        {error && (
          <div className="p-3 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs font-semibold animate-fade-in">
            {error}
          </div>
        )}

        {/* Airport & Date Grid */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-3 items-stretch">
          {/* FROM AIRPORT */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-3 relative' : 'md:col-span-4 relative'}>
            <div className="h-full p-3.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 hover:border-amber-500/40 focus-within:border-amber-400 transition group cursor-pointer relative flex flex-col justify-between">
              <div className="flex items-center justify-between text-[11px] font-semibold text-slate-400 mb-1">
                <span className="flex items-center gap-1.5 uppercase tracking-wider text-slate-400 group-hover:text-amber-400 transition">
                  <PlaneTakeoff className="w-3.5 h-3.5 text-amber-400" /> From
                </span>
                <span className="font-mono text-xs px-2 py-0.5 rounded-md bg-[#12131A] text-amber-400 font-bold border border-white/10">
                  {selectedOriginAirport?.code}
                </span>
              </div>

              <div>
                <div className="text-xl sm:text-2xl font-bold text-white tracking-tight truncate">
                  {selectedOriginAirport?.city}
                </div>
                <p className="text-[11px] text-slate-400 font-normal truncate mt-0.5">
                  {selectedOriginAirport?.name}
                </p>
              </div>

              <select
                value={origin}
                onChange={(e) => {
                  setOrigin(e.target.value);
                  setError(null);
                }}
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                aria-label="Departure Airport"
              >
                {airports.map((airport) => (
                  <option key={airport.code} value={airport.code} className="bg-[#1A1C24] text-white">
                    {airport.city} ({airport.code}) - {airport.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Swap Button */}
            <button
              type="button"
              onClick={handleSwap}
              className="absolute -bottom-2 md:top-1/2 md:-bottom-auto -right-2 md:-right-3.5 z-20 w-8 h-8 rounded-full bg-[#1F222E] hover:bg-amber-400 hover:text-black border border-white/20 text-amber-400 flex items-center justify-center shadow-lg transition-all duration-200 hover:scale-110 active:rotate-180"
              title="Swap Departure and Arrival Airports"
            >
              <ArrowLeftRight className="w-3.5 h-3.5" />
            </button>
          </div>

          {/* TO AIRPORT */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-3 relative' : 'md:col-span-4 relative'}>
            <div className="h-full p-3.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 hover:border-amber-500/40 focus-within:border-amber-400 transition group cursor-pointer relative flex flex-col justify-between">
              <div className="flex items-center justify-between text-[11px] font-semibold text-slate-400 mb-1">
                <span className="flex items-center gap-1.5 uppercase tracking-wider text-slate-400 group-hover:text-amber-400 transition">
                  <PlaneLanding className="w-3.5 h-3.5 text-accent" /> To
                </span>
                <span className="font-mono text-xs px-2 py-0.5 rounded-md bg-[#12131A] text-accent font-bold border border-white/10">
                  {selectedDestAirport?.code}
                </span>
              </div>

              <div>
                <div className="text-xl sm:text-2xl font-bold text-white tracking-tight truncate">
                  {selectedDestAirport?.city}
                </div>
                <p className="text-[11px] text-slate-400 font-normal truncate mt-0.5">
                  {selectedDestAirport?.name}
                </p>
              </div>

              <select
                value={destination}
                onChange={(e) => {
                  setDestination(e.target.value);
                  setError(null);
                }}
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                aria-label="Arrival Airport"
              >
                {airports.map((airport) => (
                  <option key={airport.code} value={airport.code} className="bg-[#1A1C24] text-white">
                    {airport.city} ({airport.code}) - {airport.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* DEPARTURE DATE */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-2 relative' : 'md:col-span-2 relative'}>
            <div className="h-full p-3.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 hover:border-amber-500/40 focus-within:border-amber-400 transition group cursor-pointer relative flex flex-col justify-between">
              <div className="flex items-center justify-between text-[11px] font-semibold text-slate-400 mb-1">
                <span className="flex items-center gap-1.5 uppercase tracking-wider text-slate-400 group-hover:text-amber-400 transition">
                  <Calendar className="w-3.5 h-3.5 text-amber-400" /> Departure
                </span>
              </div>

              <div>
                <div className="text-base font-bold text-white tracking-tight">
                  {new Date(departureDate + 'T00:00:00').toLocaleDateString('en-US', {
                    day: 'numeric',
                    month: 'short',
                    weekday: 'short',
                  })}
                </div>
                <p className="text-[11px] text-slate-400 font-normal mt-0.5">
                  {new Date(departureDate + 'T00:00:00').getFullYear()}
                </p>
              </div>

              <input
                type="date"
                value={departureDate}
                min={new Date().toISOString().split('T')[0]}
                onChange={(e) => setDepartureDate(e.target.value)}
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                aria-label="Departure Date"
              />
            </div>
          </div>

          {/* RETURN DATE (If Round Trip) */}
          {tripType === 'ROUND_TRIP' && (
            <div className="md:col-span-2 relative">
              <div className="h-full p-3.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 hover:border-amber-500/40 focus-within:border-amber-400 transition group cursor-pointer relative flex flex-col justify-between">
                <div className="flex items-center justify-between text-[11px] font-semibold text-slate-400 mb-1">
                  <span className="flex items-center gap-1.5 uppercase tracking-wider text-slate-400 group-hover:text-accent transition">
                    <Calendar className="w-3.5 h-3.5 text-accent" /> Return
                  </span>
                </div>

                <div>
                  <div className="text-base font-bold text-white tracking-tight">
                    {new Date(returnDate + 'T00:00:00').toLocaleDateString('en-US', {
                      day: 'numeric',
                      month: 'short',
                      weekday: 'short',
                    })}
                  </div>
                  <p className="text-[11px] text-slate-400 font-normal mt-0.5">
                    {new Date(returnDate + 'T00:00:00').getFullYear()}
                  </p>
                </div>

                <input
                  type="date"
                  value={returnDate}
                  min={departureDate}
                  onChange={(e) => setReturnDate(e.target.value)}
                  className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                  aria-label="Return Date"
                />
              </div>
            </div>
          )}

          {/* SEARCH CTA BUTTON */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-2' : 'md:col-span-2'}>
            <button
              type="submit"
              className="w-full h-full min-h-[56px] rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-black text-sm shadow-glow-gold transition-all duration-200 flex items-center justify-center gap-2 group cursor-pointer"
            >
              <Search className="w-4 h-4 group-hover:scale-110 transition-transform text-black" />
              <span>Search</span>
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};
