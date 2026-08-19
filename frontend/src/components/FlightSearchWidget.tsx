import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  PlaneTakeoff,
  PlaneLanding,
  Calendar,
  Users,
  ArrowLeftRight,
  Search,
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

  const [origin, setOrigin] = useState<string>(initialOrigin);
  const [destination, setDestination] = useState<string>(initialDestination);
  const [departureDate, setDepartureDate] = useState<string>(defaultDate);
  const [cabinClass, setCabinClass] = useState<CabinClass>(initialCabin);
  const [passengers, setPassengers] = useState<number>(initialPassengers);
  const [error, setError] = useState<string | null>(null);

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
    <form
      onSubmit={handleSubmit}
      className={`rounded-2xl bg-slate-900/90 border border-slate-800 p-4 sm:p-6 shadow-2xl backdrop-blur-xl space-y-4 ${
        compact ? 'shadow-lg' : ''
      }`}
    >
      {/* Top Filter Pills */}
      <div className="flex flex-wrap items-center justify-between gap-3 pb-3 border-b border-slate-800/80">
        <div className="flex items-center gap-2">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-sky-500/10 text-sky-400 border border-sky-500/20 text-xs font-semibold">
            <Sparkles className="w-3.5 h-3.5" />
            One-Way Flight Search
          </div>
        </div>

        <div className="flex items-center gap-3">
          {/* Cabin Class Selection */}
          <select
            value={cabinClass}
            onChange={(e) => setCabinClass(e.target.value as CabinClass)}
            className="bg-slate-800 border border-slate-700 text-slate-200 text-xs rounded-lg px-2.5 py-1.5 focus:outline-none focus:border-sky-500 transition"
          >
            <option value="ECONOMY">Economy</option>
            <option value="PREMIUM_ECONOMY">Premium Economy</option>
            <option value="BUSINESS">Business Class</option>
            <option value="FIRST">First Class</option>
          </select>

          {/* Passengers Count */}
          <div className="flex items-center gap-1.5 bg-slate-800 border border-slate-700 rounded-lg px-2.5 py-1 text-xs text-slate-200">
            <Users className="w-3.5 h-3.5 text-sky-400" />
            <select
              value={passengers}
              onChange={(e) => setPassengers(parseInt(e.target.value, 10))}
              className="bg-transparent text-slate-200 focus:outline-none cursor-pointer"
            >
              {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
                <option key={num} value={num} className="bg-slate-900">
                  {num} {num === 1 ? 'Adult' : 'Adults'}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {error && (
        <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-medium flex items-center gap-2">
          <span>⚠️ {error}</span>
        </div>
      )}

      {/* Inputs Grid */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-3 items-center">
        {/* Origin */}
        <div className="md:col-span-3.5 relative group">
          <label className="block text-[11px] font-medium text-slate-400 mb-1">From Airport</label>
          <div className="relative">
            <PlaneTakeoff className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            <select
              value={origin}
              onChange={(e) => {
                setOrigin(e.target.value);
                setError(null);
              }}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-3 py-2.5 text-sm font-semibold text-white focus:outline-none focus:border-sky-500 transition appearance-none cursor-pointer"
            >
              {airports.map((a) => (
                <option key={a.code} value={a.code} className="bg-slate-900 text-white">
                  {a.city} ({a.code}) - {a.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Swap Button */}
        <div className="md:col-span-1 flex items-center justify-center pt-4">
          <button
            type="button"
            onClick={handleSwap}
            title="Swap Origin and Destination"
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-300 hover:text-white transition duration-150 transform hover:rotate-180"
          >
            <ArrowLeftRight className="w-4 h-4 text-sky-400" />
          </button>
        </div>

        {/* Destination */}
        <div className="md:col-span-3.5 relative group">
          <label className="block text-[11px] font-medium text-slate-400 mb-1">To Airport</label>
          <div className="relative">
            <PlaneLanding className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            <select
              value={destination}
              onChange={(e) => {
                setDestination(e.target.value);
                setError(null);
              }}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-3 py-2.5 text-sm font-semibold text-white focus:outline-none focus:border-sky-500 transition appearance-none cursor-pointer"
            >
              {airports.map((a) => (
                <option key={a.code} value={a.code} className="bg-slate-900 text-white">
                  {a.city} ({a.code}) - {a.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Departure Date */}
        <div className="md:col-span-2 relative group">
          <label className="block text-[11px] font-medium text-slate-400 mb-1">Departure Date</label>
          <div className="relative">
            <Calendar className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
            <input
              type="date"
              value={departureDate}
              min={new Date().toISOString().split('T')[0]}
              onChange={(e) => setDepartureDate(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-3 py-2.5 text-sm font-semibold text-white focus:outline-none focus:border-sky-500 transition cursor-pointer [color-scheme:dark]"
            />
          </div>
        </div>

        {/* Submit Button */}
        <div className="md:col-span-2 pt-4 md:pt-5">
          <button
            type="submit"
            className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white font-semibold text-sm shadow-lg shadow-sky-500/25 transition duration-150 flex items-center justify-center gap-2 group"
          >
            <Search className="w-4 h-4 group-hover:scale-110 transition-transform" />
            Search
          </button>
        </div>
      </div>
    </form>
  );
};
