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
  Palmtree,
  Train,
  Bus,
  Car,
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

  // Tomorrow as default date if not passed
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
        {/* Quick Travel Module Tabs (Only on full hero widget) */}
      {!compact && (
        <div className="flex items-center justify-center sm:justify-start gap-2 mb-3 overflow-x-auto pb-1 px-1">
          <div className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-blue-600 text-white font-medium text-xs shadow-sm shrink-0">
            <Plane className="w-4 h-4" />
            <span>Flights</span>
          </div>

          <div className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-slate-900 text-slate-400 text-xs font-medium border border-slate-800 cursor-not-allowed shrink-0">
            <Building2 className="w-4 h-4 text-slate-500" />
            <span>Hotels</span>
            <span className="text-[9px] uppercase px-1.5 py-0.5 rounded bg-slate-800 text-slate-500 font-bold">Soon</span>
          </div>

          <div className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-slate-900 text-slate-400 text-xs font-medium border border-slate-800 cursor-not-allowed shrink-0">
            <Palmtree className="w-4 h-4 text-slate-500" />
            <span>Holidays</span>
            <span className="text-[9px] uppercase px-1.5 py-0.5 rounded bg-slate-800 text-slate-500 font-bold">Soon</span>
          </div>

          <div className="hidden md:flex items-center gap-1.5 px-4 py-2 rounded-xl bg-slate-900 text-slate-400 text-xs font-medium border border-slate-800 cursor-not-allowed shrink-0">
            <Train className="w-4 h-4 text-slate-500" />
            <span>Trains</span>
            <span className="text-[9px] uppercase px-1.5 py-0.5 rounded bg-slate-800 text-slate-500 font-bold">Soon</span>
          </div>

          <div className="hidden md:flex items-center gap-1.5 px-4 py-2 rounded-xl bg-slate-900 text-slate-400 text-xs font-medium border border-slate-800 cursor-not-allowed shrink-0">
            <Bus className="w-4 h-4 text-slate-500" />
            <span>Buses</span>
            <span className="text-[9px] uppercase px-1.5 py-0.5 rounded bg-slate-800 text-slate-500 font-bold">Soon</span>
          </div>

          <div className="hidden lg:flex items-center gap-1.5 px-4 py-2 rounded-xl bg-slate-900 text-slate-400 text-xs font-medium border border-slate-800 cursor-not-allowed shrink-0">
            <Car className="w-4 h-4 text-slate-500" />
            <span>Cabs</span>
            <span className="text-[9px] uppercase px-1.5 py-0.5 rounded bg-slate-800 text-slate-500 font-bold">Soon</span>
          </div>
        </div>
      )}

      {/* Main Search Panel */}
      <form
        onSubmit={handleSubmit}
        className={`rounded-2xl ${
          compact
            ? 'bg-slate-900 border border-slate-800 p-4 sm:p-5 shadow-lg'
            : 'glass-hero-search p-5 sm:p-7'
        } space-y-5`}
      >
        {/* Top Options Bar (Trip Type, Cabin, Passengers) */}
        <div className="flex flex-wrap items-center justify-between gap-3 pb-3 border-b border-slate-800">
          {/* Trip Type Selector */}
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-2 cursor-pointer group">
              <input
                type="radio"
                name="tripType"
                value="ONE_WAY"
                checked={tripType === 'ONE_WAY'}
                onChange={() => setTripType('ONE_WAY')}
                className="w-4 h-4 text-blue-600 focus:ring-blue-500 focus:ring-offset-0 bg-slate-800 border-slate-700"
              />
              <span className={`text-xs font-medium transition ${tripType === 'ONE_WAY' ? 'text-white' : 'text-slate-400 group-hover:text-slate-300'}`}>
                One Way
              </span>
            </label>

            <label className="flex items-center gap-2 cursor-pointer group">
              <input
                type="radio"
                name="tripType"
                value="ROUND_TRIP"
                checked={tripType === 'ROUND_TRIP'}
                onChange={() => setTripType('ROUND_TRIP')}
                className="w-4 h-4 text-blue-600 focus:ring-blue-500 focus:ring-offset-0 bg-slate-800 border-slate-700"
              />
              <span className={`text-xs font-medium transition ${tripType === 'ROUND_TRIP' ? 'text-white' : 'text-slate-400 group-hover:text-slate-300'}`}>
                Round Trip
              </span>
            </label>
          </div>

          {/* Right Filters: Cabin & Passengers */}
          <div className="flex items-center gap-2.5">
            {/* Cabin Class Selection */}
            <div className="relative">
              <select
                value={cabinClass}
                onChange={(e) => setCabinClass(e.target.value as CabinClass)}
                className="bg-slate-900 border border-slate-700 text-slate-200 text-xs font-medium rounded-lg px-3 py-2 pr-7 focus:outline-none focus:border-blue-500 transition appearance-none cursor-pointer"
              >
                <option value="ECONOMY">Economy</option>
                <option value="PREMIUM_ECONOMY">Premium Economy</option>
                <option value="BUSINESS">Business Class</option>
                <option value="FIRST">First Class</option>
              </select>
              <ChevronDown className="w-3.5 h-3.5 text-slate-400 absolute right-2.5 top-3 pointer-events-none" />
            </div>

            {/* Passengers Count */}
            <div className="relative flex items-center bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-xs font-medium text-slate-200">
              <Users className="w-3.5 h-3.5 text-slate-400 mr-2" />
              <select
                value={passengers}
                onChange={(e) => setPassengers(parseInt(e.target.value, 10))}
                className="bg-transparent text-slate-200 focus:outline-none cursor-pointer pr-4 appearance-none font-medium"
              >
                {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
                  <option key={num} value={num} className="bg-slate-900">
                    {num} {num === 1 ? 'Adult' : 'Adults'}
                  </option>
                ))}
              </select>
              <ChevronDown className="w-3.5 h-3.5 text-slate-400 absolute right-2 top-3 pointer-events-none" />
            </div>
          </div>
        </div>

        {error && (
          <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-medium flex items-center gap-2 animate-fade-in">
            <span>{error}</span>
          </div>
        )}

        {/* Airport & Date Interactive Selector Grid */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-3 items-stretch">
          {/* FROM AIRPORT */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-3 relative' : 'md:col-span-4 relative'}>
            <div className="h-full p-3.5 rounded-xl bg-slate-950 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 focus-within:border-blue-500 focus-within:ring-1 focus-within:ring-blue-500/30 transition-all duration-150 group cursor-pointer relative flex flex-col justify-between">
              <div className="flex items-center justify-between text-[11px] font-medium text-slate-400 mb-1.5">
                <span className="flex items-center gap-1.5 uppercase tracking-wider font-semibold text-slate-400 group-hover:text-slate-300 transition-colors">
                  <PlaneTakeoff className="w-3.5 h-3.5 text-blue-400" />
                  From
                </span>
                <span className="font-mono text-xs px-2 py-0.5 rounded bg-slate-800 text-blue-400 font-bold border border-slate-700">
                  {selectedOriginAirport?.code}
                </span>
              </div>

              <div>
                <div className="text-xl sm:text-2xl font-bold text-white tracking-tight group-hover:text-slate-100 transition-colors truncate">
                  {selectedOriginAirport?.city}
                </div>
                <p className="text-[11px] text-slate-400 font-normal truncate mt-0.5">
                  {selectedOriginAirport?.name}
                </p>
              </div>

              {/* Seamless accessible click-anywhere select overlay */}
              <select
                value={origin}
                onChange={(e) => {
                  setOrigin(e.target.value);
                  setError(null);
                }}
                aria-label="Departure Airport"
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer text-base bg-slate-900 text-white"
              >
                {airports.map((a) => (
                  <option key={a.code} value={a.code} className="bg-slate-900 text-white py-2">
                    {a.city} ({a.code}) - {a.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* SWAP BUTTON */}
          <div className="md:col-span-1 flex items-center justify-center -my-1 md:my-0">
            <button
              type="button"
              onClick={handleSwap}
              title="Swap From and To"
              className="w-9 h-9 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 transition duration-150 flex items-center justify-center z-10"
            >
              <ArrowLeftRight className="w-4 h-4" />
            </button>
          </div>

          {/* TO AIRPORT */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-3 relative' : 'md:col-span-4 relative'}>
            <div className="h-full p-3.5 rounded-xl bg-slate-950 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 focus-within:border-blue-500 focus-within:ring-1 focus-within:ring-blue-500/30 transition-all duration-150 group cursor-pointer relative flex flex-col justify-between">
              <div className="flex items-center justify-between text-[11px] font-medium text-slate-400 mb-1.5">
                <span className="flex items-center gap-1.5 uppercase tracking-wider font-semibold text-slate-400 group-hover:text-slate-300 transition-colors">
                  <PlaneLanding className="w-3.5 h-3.5 text-blue-400" />
                  To
                </span>
                <span className="font-mono text-xs px-2 py-0.5 rounded bg-slate-800 text-blue-400 font-bold border border-slate-700">
                  {selectedDestAirport?.code}
                </span>
              </div>

              <div>
                <div className="text-xl sm:text-2xl font-bold text-white tracking-tight group-hover:text-slate-100 transition-colors truncate">
                  {selectedDestAirport?.city}
                </div>
                <p className="text-[11px] text-slate-400 font-normal truncate mt-0.5">
                  {selectedDestAirport?.name}
                </p>
              </div>

              {/* Seamless accessible click-anywhere select overlay */}
              <select
                value={destination}
                onChange={(e) => {
                  setDestination(e.target.value);
                  setError(null);
                }}
                aria-label="Destination Airport"
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer text-base bg-slate-900 text-white"
              >
                {airports.map((a) => (
                  <option key={a.code} value={a.code} className="bg-slate-900 text-white py-2">
                    {a.city} ({a.code}) - {a.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* DEPARTURE DATE */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-3' : 'md:col-span-3'}>
            <div className="h-full p-3.5 rounded-xl bg-slate-950 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 focus-within:border-blue-500 focus-within:ring-1 focus-within:ring-blue-500/30 transition-all duration-150 relative flex flex-col justify-between cursor-pointer group">
              <div className="flex items-center gap-1.5 text-[11px] font-medium text-slate-400 mb-1.5">
                <Calendar className="w-3.5 h-3.5 text-blue-400" />
                <span className="uppercase tracking-wider font-semibold group-hover:text-slate-300 transition-colors">Departure</span>
              </div>

              <div>
                <div className="text-base sm:text-lg font-bold text-white tracking-tight">
                  {new Date(departureDate + 'T00:00:00').toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' })}
                </div>
                <p className="text-[11px] text-slate-400 font-normal mt-0.5">
                  {new Date(departureDate + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'long' })}
                </p>
              </div>

              <input
                type="date"
                value={departureDate}
                min={new Date().toISOString().split('T')[0]}
                onChange={(e) => setDepartureDate(e.target.value)}
                aria-label="Departure Date"
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer [color-scheme:dark]"
              />
            </div>
          </div>

          {/* RETURN DATE (If Round Trip) */}
          {tripType === 'ROUND_TRIP' && (
            <div className="md:col-span-2">
              <div className="h-full p-3.5 rounded-xl bg-slate-950 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 focus-within:border-emerald-500 focus-within:ring-1 focus-within:ring-emerald-500/30 transition-all duration-150 relative flex flex-col justify-between cursor-pointer group">
                <div className="flex items-center gap-1.5 text-[11px] font-medium text-slate-400 mb-1.5">
                  <Calendar className="w-3.5 h-3.5 text-emerald-400" />
                  <span className="uppercase tracking-wider font-semibold group-hover:text-slate-300 transition-colors">Return</span>
                </div>

                <div>
                  <div className="text-base sm:text-lg font-bold text-white tracking-tight">
                    {new Date(returnDate + 'T00:00:00').toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' })}
                  </div>
                  <p className="text-[11px] text-slate-400 font-normal mt-0.5">
                    {new Date(returnDate + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'long' })}
                  </p>
                </div>

                <input
                  type="date"
                  value={returnDate}
                  min={departureDate}
                  onChange={(e) => setReturnDate(e.target.value)}
                  aria-label="Return Date"
                  className="absolute inset-0 w-full h-full opacity-0 cursor-pointer [color-scheme:dark]"
                />
              </div>
            </div>
          )}
        </div>

        {/* Bottom Search CTA Bar */}
        <div className="pt-2 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2 text-xs text-slate-400">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
            <span>Real-time flight inventory & seat selection</span>
          </div>

          <button
            type="submit"
            className="w-full sm:w-auto px-7 py-3 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm transition duration-150 flex items-center justify-center gap-2"
          >
            <Search className="w-4 h-4" />
            <span>Search Flights</span>
          </button>
        </div>
      </form>
    </div>
  );
};

