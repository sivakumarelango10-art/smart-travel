import React, { useState, useRef } from 'react';
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
  ChevronDown,
  X,
  MapPin,
  Sparkles
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

const POPULAR_CITIES = [
  { code: 'DEL', city: 'Delhi', name: 'Indira Gandhi International Airport' },
  { code: 'BOM', city: 'Mumbai', name: 'Chhatrapati Shivaji Maharaj Airport' },
  { code: 'BLR', city: 'Bengaluru', name: 'Kempegowda International Airport' },
  { code: 'MAA', city: 'Chennai', name: 'Chennai International Airport' },
  { code: 'HYD', city: 'Hyderabad', name: 'Rajiv Gandhi International Airport' },
  { code: 'CCU', city: 'Kolkata', name: 'Netaji Subhash Chandra Bose Airport' },
  { code: 'GOI', city: 'Goa', name: 'Dabolim Airport' },
  { code: 'DXB', city: 'Dubai', name: 'Dubai International Airport' },
  { code: 'SIN', city: 'Singapore', name: 'Singapore Changi Airport' },
  { code: 'LHR', city: 'London', name: 'London Heathrow Airport' },
  { code: 'BKK', city: 'Bangkok', name: 'Suvarnabhumi Airport' },
];

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

  // Modal / Dropdown active states for mobile & touch browsers
  const [showOriginModal, setShowOriginModal] = useState<boolean>(false);
  const [showDestModal, setShowDestModal] = useState<boolean>(false);
  const [showDateModal, setShowDateModal] = useState<boolean>(false);
  const [airportSearchQuery, setAirportSearchQuery] = useState<string>('');

  const dateInputRef = useRef<HTMLInputElement | null>(null);

  const selectedOriginAirport = airports.find((a) => a.code === origin) || airports[0];
  const selectedDestAirport = airports.find((a) => a.code === destination) || airports[1];

  const handleSwap = (e: React.MouseEvent) => {
    e.stopPropagation();
    setOrigin(destination);
    setDestination(origin);
    setError(null);
  };

  const handleSelectOrigin = (code: string) => {
    if (code === destination) {
      setDestination(origin);
    }
    setOrigin(code);
    setShowOriginModal(false);
    setError(null);
  };

  const handleSelectDest = (code: string) => {
    if (code === origin) {
      setOrigin(destination);
    }
    setDestination(code);
    setShowDestModal(false);
    setError(null);
  };

  const handleOpenDatePicker = () => {
    if (dateInputRef.current) {
      try {
        if (typeof (dateInputRef.current as any).showPicker === 'function') {
          (dateInputRef.current as any).showPicker();
        } else {
          dateInputRef.current.focus();
          dateInputRef.current.click();
        }
      } catch {
        setShowDateModal(true);
      }
    } else {
      setShowDateModal(true);
    }
  };

  const handleSetQuickDate = (daysFromToday: number) => {
    const d = new Date(Date.now() + daysFromToday * 86400000);
    setDepartureDate(d.toISOString().split('T')[0]);
    setShowDateModal(false);
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

  const filteredAirports = airports.filter((a) => {
    const q = airportSearchQuery.toLowerCase().trim();
    if (!q) return true;
    return (
      a.city.toLowerCase().includes(q) ||
      a.code.toLowerCase().includes(q) ||
      a.name.toLowerCase().includes(q) ||
      a.country.toLowerCase().includes(q)
    );
  });

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
                className="w-4 h-4 text-amber-400 focus:ring-amber-400 bg-[#1A1C24] border-white/20 accent-amber-400 cursor-pointer"
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
                className="w-4 h-4 text-amber-400 focus:ring-amber-400 bg-[#1A1C24] border-white/20 accent-amber-400 cursor-pointer"
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
        <div className="grid grid-cols-1 md:grid-cols-12 gap-3 items-stretch relative">
          {/* FROM AIRPORT (DEPARTURE) */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-3 relative' : 'md:col-span-4 relative'}>
            <div
              onClick={() => {
                setAirportSearchQuery('');
                setShowOriginModal(true);
              }}
              className="h-full p-3.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 hover:border-amber-500/40 focus-within:border-amber-400 transition group cursor-pointer relative flex flex-col justify-between select-none"
            >
              <div className="flex items-center justify-between text-[11px] font-semibold text-slate-400 mb-1">
                <span className="flex items-center gap-1.5 uppercase tracking-wider text-slate-400 group-hover:text-amber-400 transition">
                  <PlaneTakeoff className="w-3.5 h-3.5 text-amber-400" /> From (Departure)
                </span>
                <span className="font-mono text-xs px-2 py-0.5 rounded-md bg-[#12131A] text-amber-400 font-bold border border-white/10 shadow-glow-gold">
                  {selectedOriginAirport?.code}
                </span>
              </div>

              <div>
                <div className="text-xl sm:text-2xl font-bold text-white tracking-tight truncate flex items-center justify-between">
                  <span>{selectedOriginAirport?.city}</span>
                  <ChevronDown className="w-4 h-4 text-slate-500 group-hover:text-amber-400 transition" />
                </div>
                <p className="text-[11px] text-slate-400 font-normal truncate mt-0.5">
                  {selectedOriginAirport?.name}
                </p>
              </div>
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

          {/* TO AIRPORT (DESTINATION) */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-3 relative' : 'md:col-span-4 relative'}>
            <div
              onClick={() => {
                setAirportSearchQuery('');
                setShowDestModal(true);
              }}
              className="h-full p-3.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 hover:border-amber-500/40 focus-within:border-amber-400 transition group cursor-pointer relative flex flex-col justify-between select-none"
            >
              <div className="flex items-center justify-between text-[11px] font-semibold text-slate-400 mb-1">
                <span className="flex items-center gap-1.5 uppercase tracking-wider text-slate-400 group-hover:text-amber-400 transition">
                  <PlaneLanding className="w-3.5 h-3.5 text-amber-400" /> To (Arrival)
                </span>
                <span className="font-mono text-xs px-2 py-0.5 rounded-md bg-[#12131A] text-amber-400 font-bold border border-white/10 shadow-glow-gold">
                  {selectedDestAirport?.code}
                </span>
              </div>

              <div>
                <div className="text-xl sm:text-2xl font-bold text-white tracking-tight truncate flex items-center justify-between">
                  <span>{selectedDestAirport?.city}</span>
                  <ChevronDown className="w-4 h-4 text-slate-500 group-hover:text-amber-400 transition" />
                </div>
                <p className="text-[11px] text-slate-400 font-normal truncate mt-0.5">
                  {selectedDestAirport?.name}
                </p>
              </div>
            </div>
          </div>

          {/* DEPARTURE DATE */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-2 relative' : 'md:col-span-2 relative'}>
            <div
              onClick={handleOpenDatePicker}
              className="h-full p-3.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 hover:border-amber-500/40 focus-within:border-amber-400 transition group cursor-pointer relative flex flex-col justify-between select-none"
            >
              <div className="flex items-center justify-between text-[11px] font-semibold text-slate-400 mb-1">
                <span className="flex items-center gap-1.5 uppercase tracking-wider text-slate-400 group-hover:text-amber-400 transition">
                  <Calendar className="w-3.5 h-3.5 text-amber-400" /> Departure Date
                </span>
                <ChevronDown className="w-3.5 h-3.5 text-slate-500 group-hover:text-amber-400 transition" />
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

              {/* Native Date Input with Cross-Browser showPicker Support */}
              <input
                ref={dateInputRef}
                type="date"
                value={departureDate}
                min={new Date().toISOString().split('T')[0]}
                onChange={(e) => setDepartureDate(e.target.value)}
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer -webkit-appearance-none"
                aria-label="Departure Date"
              />
            </div>
          </div>

          {/* RETURN DATE (If Round Trip) */}
          {tripType === 'ROUND_TRIP' && (
            <div className="md:col-span-2 relative">
              <div className="h-full p-3.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 hover:border-amber-500/40 focus-within:border-amber-400 transition group cursor-pointer relative flex flex-col justify-between select-none">
                <div className="flex items-center justify-between text-[11px] font-semibold text-slate-400 mb-1">
                  <span className="flex items-center gap-1.5 uppercase tracking-wider text-slate-400 group-hover:text-amber-400 transition">
                    <Calendar className="w-3.5 h-3.5 text-amber-400" /> Return Date
                  </span>
                  <ChevronDown className="w-3.5 h-3.5 text-slate-500 group-hover:text-amber-400 transition" />
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
                  className="absolute inset-0 w-full h-full opacity-0 cursor-pointer -webkit-appearance-none"
                  aria-label="Return Date"
                />
              </div>
            </div>
          )}

          {/* SEARCH SUBMIT BUTTON */}
          <div className={tripType === 'ROUND_TRIP' ? 'md:col-span-2 flex items-stretch' : 'md:col-span-2 flex items-stretch'}>
            <button
              type="submit"
              className="w-full min-h-[56px] rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-extrabold text-sm shadow-glow-gold transition flex items-center justify-center gap-2 cursor-pointer active:scale-95"
            >
              <Search className="w-4 h-4 text-black" />
              <span>Search Flights</span>
            </button>
          </div>
        </div>
      </form>

      {/* MODAL 1: DEPARTURE AIRPORT SELECTOR (IOS, ANDROID & ALL BROWSERS) */}
      {showOriginModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="bg-[#14161F] border border-white/10 rounded-3xl max-w-lg w-full max-h-[85vh] flex flex-col shadow-2xl overflow-hidden">
            {/* Modal Header */}
            <div className="p-4 sm:p-5 border-b border-white/10 flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center shadow-glow-gold">
                  <PlaneTakeoff className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="font-extrabold text-white text-sm">Select Departure City / Airport</h3>
                  <p className="text-[11px] text-slate-400">Where are you flying from?</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowOriginModal(false)}
                className="w-8 h-8 rounded-full bg-[#181A22] text-slate-400 hover:text-white flex items-center justify-center transition border border-white/10"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Search Input */}
            <div className="p-4 border-b border-white/10 bg-[#181A22]">
              <div className="relative">
                <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                <input
                  type="text"
                  placeholder="Search city, code (e.g. DEL, BOM) or country..."
                  value={airportSearchQuery}
                  onChange={(e) => setAirportSearchQuery(e.target.value)}
                  autoFocus
                  className="w-full bg-[#14161F] border border-white/10 rounded-xl pl-10 pr-4 py-2.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-amber-400 transition"
                />
              </div>

              {/* Quick Popular Pills */}
              <div className="flex items-center gap-1.5 overflow-x-auto pt-3 pb-1 no-scrollbar">
                <span className="text-[10px] uppercase font-bold text-slate-400 shrink-0">Popular:</span>
                {POPULAR_CITIES.slice(0, 6).map((c) => (
                  <button
                    key={c.code}
                    type="button"
                    onClick={() => handleSelectOrigin(c.code)}
                    className={`px-2.5 py-1 rounded-lg text-xs font-bold shrink-0 transition ${
                      origin === c.code
                        ? 'bg-amber-400 text-black shadow-glow-gold'
                        : 'bg-[#14161F] text-slate-300 hover:text-white border border-white/10'
                    }`}
                  >
                    {c.city} ({c.code})
                  </button>
                ))}
              </div>
            </div>

            {/* Airport List */}
            <div className="flex-1 overflow-y-auto p-2 divide-y divide-white/5">
              {filteredAirports.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-400">
                  No airports found matching &quot;{airportSearchQuery}&quot;
                </div>
              ) : (
                filteredAirports.map((airport) => {
                  const isSelected = origin === airport.code;
                  return (
                    <button
                      key={airport.code}
                      type="button"
                      onClick={() => handleSelectOrigin(airport.code)}
                      className={`w-full p-3 text-left rounded-xl transition flex items-center justify-between ${
                        isSelected
                          ? 'bg-amber-400/10 border border-amber-400/30'
                          : 'hover:bg-[#181A22]'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-lg bg-[#181A22] border border-white/10 flex items-center justify-center font-mono font-bold text-xs text-amber-400">
                          {airport.code}
                        </div>
                        <div>
                          <p className="text-xs font-bold text-white">
                            {airport.city}, {airport.country}
                          </p>
                          <p className="text-[10px] text-slate-400 truncate max-w-xs">{airport.name}</p>
                        </div>
                      </div>
                      {isSelected && (
                        <span className="text-[10px] font-black px-2 py-0.5 rounded-full bg-amber-400 text-black">
                          SELECTED
                        </span>
                      )}
                    </button>
                  );
                })
              )}
            </div>
          </div>
        </div>
      )}

      {/* MODAL 2: DESTINATION AIRPORT SELECTOR */}
      {showDestModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="bg-[#14161F] border border-white/10 rounded-3xl max-w-lg w-full max-h-[85vh] flex flex-col shadow-2xl overflow-hidden">
            {/* Modal Header */}
            <div className="p-4 sm:p-5 border-b border-white/10 flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center shadow-glow-gold">
                  <PlaneLanding className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="font-extrabold text-white text-sm">Select Destination City / Airport</h3>
                  <p className="text-[11px] text-slate-400">Where are you flying to?</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowDestModal(false)}
                className="w-8 h-8 rounded-full bg-[#181A22] text-slate-400 hover:text-white flex items-center justify-center transition border border-white/10"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Search Input */}
            <div className="p-4 border-b border-white/10 bg-[#181A22]">
              <div className="relative">
                <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                <input
                  type="text"
                  placeholder="Search city, code (e.g. DEL, BOM) or country..."
                  value={airportSearchQuery}
                  onChange={(e) => setAirportSearchQuery(e.target.value)}
                  autoFocus
                  className="w-full bg-[#14161F] border border-white/10 rounded-xl pl-10 pr-4 py-2.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-amber-400 transition"
                />
              </div>

              {/* Quick Popular Pills */}
              <div className="flex items-center gap-1.5 overflow-x-auto pt-3 pb-1 no-scrollbar">
                <span className="text-[10px] uppercase font-bold text-slate-400 shrink-0">Popular:</span>
                {POPULAR_CITIES.slice(0, 6).map((c) => (
                  <button
                    key={c.code}
                    type="button"
                    onClick={() => handleSelectDest(c.code)}
                    className={`px-2.5 py-1 rounded-lg text-xs font-bold shrink-0 transition ${
                      destination === c.code
                        ? 'bg-amber-400 text-black shadow-glow-gold'
                        : 'bg-[#14161F] text-slate-300 hover:text-white border border-white/10'
                    }`}
                  >
                    {c.city} ({c.code})
                  </button>
                ))}
              </div>
            </div>

            {/* Airport List */}
            <div className="flex-1 overflow-y-auto p-2 divide-y divide-white/5">
              {filteredAirports.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-400">
                  No airports found matching &quot;{airportSearchQuery}&quot;
                </div>
              ) : (
                filteredAirports.map((airport) => {
                  const isSelected = destination === airport.code;
                  return (
                    <button
                      key={airport.code}
                      type="button"
                      onClick={() => handleSelectDest(airport.code)}
                      className={`w-full p-3 text-left rounded-xl transition flex items-center justify-between ${
                        isSelected
                          ? 'bg-amber-400/10 border border-amber-400/30'
                          : 'hover:bg-[#181A22]'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-lg bg-[#181A22] border border-white/10 flex items-center justify-center font-mono font-bold text-xs text-amber-400">
                          {airport.code}
                        </div>
                        <div>
                          <p className="text-xs font-bold text-white">
                            {airport.city}, {airport.country}
                          </p>
                          <p className="text-[10px] text-slate-400 truncate max-w-xs">{airport.name}</p>
                        </div>
                      </div>
                      {isSelected && (
                        <span className="text-[10px] font-black px-2 py-0.5 rounded-full bg-amber-400 text-black">
                          SELECTED
                        </span>
                      )}
                    </button>
                  );
                })
              )}
            </div>
          </div>
        </div>
      )}

      {/* MODAL 3: CROSS-DEVICE DEPARTURE DATE SELECTOR */}
      {showDateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="bg-[#14161F] border border-white/10 rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-white/10">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center shadow-glow-gold">
                  <Calendar className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="font-extrabold text-white text-sm">Select Departure Date</h3>
                  <p className="text-[11px] text-slate-400">Choose when you wish to travel</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowDateModal(false)}
                className="w-8 h-8 rounded-full bg-[#181A22] text-slate-400 hover:text-white flex items-center justify-center transition border border-white/10"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Quick Date Chips */}
            <div className="space-y-2">
              <label className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">
                Quick Selection:
              </label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => handleSetQuickDate(0)}
                  className="p-2.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-white text-xs font-bold border border-white/10 transition text-left"
                >
                  🚀 Today
                </button>
                <button
                  type="button"
                  onClick={() => handleSetQuickDate(1)}
                  className="p-2.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-amber-400 text-xs font-bold border border-white/10 transition text-left"
                >
                  ⚡ Tomorrow
                </button>
                <button
                  type="button"
                  onClick={() => handleSetQuickDate(3)}
                  className="p-2.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-white text-xs font-bold border border-white/10 transition text-left"
                >
                  📅 In 3 Days
                </button>
                <button
                  type="button"
                  onClick={() => handleSetQuickDate(7)}
                  className="p-2.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-white text-xs font-bold border border-white/10 transition text-left"
                >
                  🏖️ Next Week
                </button>
              </div>
            </div>

            {/* Direct Date Input */}
            <div className="space-y-2 pt-2 border-t border-white/10">
              <label className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">
                Or Pick Custom Calendar Date:
              </label>
              <input
                type="date"
                value={departureDate}
                min={new Date().toISOString().split('T')[0]}
                onChange={(e) => {
                  setDepartureDate(e.target.value);
                  setShowDateModal(false);
                }}
                className="w-full bg-[#181A22] border border-white/10 rounded-xl px-4 py-3 text-sm text-white font-bold focus:outline-none focus:border-amber-400 transition"
              />
            </div>

            <button
              type="button"
              onClick={() => setShowDateModal(false)}
              className="w-full py-3 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-glow-gold transition"
            >
              Confirm Date
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
