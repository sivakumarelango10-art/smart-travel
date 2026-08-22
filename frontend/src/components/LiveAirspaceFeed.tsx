import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plane,
  Radio,
  Compass,
  Clock,
  AlertTriangle,
  Search,
  Activity,
  Layers,
  ChevronRight
} from 'lucide-react';
import { FlightStatus } from '../types/flight';

export interface AirspaceFlightFeedItem {
  id: string;
  flightNumber: string;
  airline: string;
  airlineCode: string;
  originCode: string;
  originCity: string;
  destCode: string;
  destCity: string;
  status: FlightStatus;
  altitudeFeet: number;
  groundSpeedKmph: number;
  progressPercent: number;
  aircraftModel: string;
  terminal?: string;
  gate?: string;
  delayMinutes?: number;
  delayReason?: string;
  eta: string;
  isInternational?: boolean;
}

interface LiveAirspaceFeedProps {
  onSelectFlight?: (flightNumber: string) => void;
  selectedFlightNumber?: string;
  compact?: boolean;
  limit?: number;
}

const INITIAL_AIRSPACE_FEEDS: AirspaceFlightFeedItem[] = [
  {
    id: 'fl-feed-1',
    flightNumber: 'AI-101',
    airline: 'Air India',
    airlineCode: 'AI',
    originCode: 'DEL',
    originCity: 'New Delhi',
    destCode: 'BOM',
    destCity: 'Mumbai',
    status: 'DEPARTED',
    altitudeFeet: 36000,
    groundSpeedKmph: 840,
    progressPercent: 62,
    aircraftModel: 'Boeing 787-8 Dreamliner',
    terminal: 'T3',
    gate: 'Gate 14',
    eta: '42m remaining',
  },
  {
    id: 'fl-feed-2',
    flightNumber: '6E-204',
    airline: 'IndiGo',
    airlineCode: '6E',
    originCode: 'BLR',
    originCity: 'Bengaluru',
    destCode: 'DEL',
    destCity: 'New Delhi',
    status: 'DEPARTED',
    altitudeFeet: 34000,
    groundSpeedKmph: 790,
    progressPercent: 48,
    aircraftModel: 'Airbus A321neo',
    terminal: 'T1',
    gate: 'Gate 8',
    eta: '1h 10m remaining',
  },
  {
    id: 'fl-feed-3',
    flightNumber: 'UK-955',
    airline: 'Vistara',
    airlineCode: 'UK',
    originCode: 'BOM',
    originCity: 'Mumbai',
    destCode: 'GOI',
    destCity: 'Goa',
    status: 'BOARDING',
    altitudeFeet: 0,
    groundSpeedKmph: 0,
    progressPercent: 0,
    aircraftModel: 'Airbus A320neo',
    terminal: 'T2',
    gate: 'Gate 22B',
    eta: 'Departure in 15m',
  },
  {
    id: 'fl-feed-4',
    flightNumber: 'EK-500',
    airline: 'Emirates',
    airlineCode: 'EK',
    originCode: 'DXB',
    originCity: 'Dubai',
    destCode: 'BOM',
    destCity: 'Mumbai',
    status: 'DEPARTED',
    altitudeFeet: 38000,
    groundSpeedKmph: 890,
    progressPercent: 78,
    aircraftModel: 'Boeing 777-300ER',
    terminal: 'T2',
    gate: 'Gate A4',
    eta: '35m remaining',
    isInternational: true,
  },
  {
    id: 'fl-feed-5',
    flightNumber: 'BA-112',
    airline: 'British Airways',
    airlineCode: 'BA',
    originCode: 'LHR',
    originCity: 'London',
    destCode: 'DEL',
    destCity: 'New Delhi',
    status: 'DEPARTED',
    altitudeFeet: 39000,
    groundSpeedKmph: 910,
    progressPercent: 86,
    aircraftModel: 'Boeing 787-9',
    terminal: 'T3',
    gate: 'Gate 18',
    eta: '1h 05m remaining',
    isInternational: true,
  },
  {
    id: 'fl-feed-6',
    flightNumber: 'SQ-402',
    airline: 'Singapore Airlines',
    airlineCode: 'SQ',
    originCode: 'SIN',
    originCity: 'Singapore',
    destCode: 'BOM',
    destCity: 'Mumbai',
    status: 'ON_TIME',
    altitudeFeet: 37000,
    groundSpeedKmph: 860,
    progressPercent: 32,
    aircraftModel: 'Airbus A350-900',
    terminal: 'T2',
    gate: 'Gate B12',
    eta: '3h 15m remaining',
    isInternational: true,
  },
  {
    id: 'fl-feed-7',
    flightNumber: 'SG-303',
    airline: 'SpiceJet',
    airlineCode: 'SG',
    originCode: 'DEL',
    originCity: 'New Delhi',
    destCode: 'CCU',
    destCity: 'Kolkata',
    status: 'DELAYED',
    altitudeFeet: 0,
    groundSpeedKmph: 0,
    progressPercent: 0,
    aircraftModel: 'Boeing 737-800',
    terminal: 'T1D',
    gate: 'Gate 4',
    delayMinutes: 35,
    delayReason: 'Incoming aircraft delayed by ATC congestion',
    eta: 'Est. Dep 16:45',
  },
  {
    id: 'fl-feed-8',
    flightNumber: 'QP-1102',
    airline: 'Akasa Air',
    airlineCode: 'QP',
    originCode: 'BOM',
    originCity: 'Mumbai',
    destCode: 'BLR',
    destCity: 'Bengaluru',
    status: 'DEPARTED',
    altitudeFeet: 33000,
    groundSpeedKmph: 780,
    progressPercent: 54,
    aircraftModel: 'Boeing 737 MAX 8',
    terminal: 'T1',
    gate: 'Gate 9',
    eta: '40m remaining',
  },
  {
    id: 'fl-feed-9',
    flightNumber: 'QR-570',
    airline: 'Qatar Airways',
    airlineCode: 'QR',
    originCode: 'DOH',
    originCity: 'Doha',
    destCode: 'DEL',
    destCity: 'New Delhi',
    status: 'DEPARTED',
    altitudeFeet: 38000,
    groundSpeedKmph: 885,
    progressPercent: 70,
    aircraftModel: 'Boeing 777-300ER',
    terminal: 'T3',
    gate: 'Gate 11',
    eta: '55m remaining',
    isInternational: true,
  },
];

const LIVE_EVENT_LOGS = [
  'AI-101 (DEL ➔ BOM) cruising at FL360 • Ground Speed 840 km/h',
  'UK-955 (BOM ➔ GOI) passenger boarding initiated at Gate 22B',
  'BA-112 (LHR ➔ DEL) entered Indian Airspace • Descending to FL240',
  '6E-204 (BLR ➔ DEL) updated arrival ETA: On Schedule',
  'EK-500 (DXB ➔ BOM) approach clearance granted for Runway 27',
  'SG-303 (DEL ➔ CCU) revised departure time updated to 16:45',
  'QP-1102 (BOM ➔ BLR) passing over Pune waypoint at 33,000 ft',
  'SQ-402 (SIN ➔ BOM) cruising smoothly over Bay of Bengal',
];

export const LiveAirspaceFeed: React.FC<LiveAirspaceFeedProps> = ({
  onSelectFlight,
  selectedFlightNumber,
  compact = false,
  limit,
}) => {
  const navigate = useNavigate();
  const [feedItems, setFeedItems] = useState<AirspaceFlightFeedItem[]>(INITIAL_AIRSPACE_FEEDS);
  const [filter, setFilter] = useState<'ALL' | 'IN_AIR' | 'BOARDING' | 'DELAYED' | 'INTL'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [activeEventIndex, setActiveEventIndex] = useState(0);
  const [lastTick, setLastTick] = useState<Date>(new Date());

  // Real-time subtle telemetry interpolation ticks (simulating live airspace movements)
  useEffect(() => {
    const interval = setInterval(() => {
      setFeedItems((prev) =>
        prev.map((flight) => {
          if (flight.status === 'DEPARTED') {
            const nextProgress = flight.progressPercent >= 98 ? 10 : flight.progressPercent + 0.5;
            const speedJitter = Math.floor(Math.random() * 9) - 4;
            return {
              ...flight,
              progressPercent: Math.min(100, Math.round(nextProgress * 10) / 10),
              groundSpeedKmph: Math.max(700, Math.min(950, flight.groundSpeedKmph + speedJitter)),
            };
          }
          return flight;
        })
      );
      setLastTick(new Date());
    }, 3500);

    return () => clearInterval(interval);
  }, []);

  // Event log ticker rotation
  useEffect(() => {
    const tickerInterval = setInterval(() => {
      setActiveEventIndex((prev) => (prev + 1) % LIVE_EVENT_LOGS.length);
    }, 4500);
    return () => clearInterval(tickerInterval);
  }, []);

  const handleFlightClick = (flightNumber: string) => {
    if (onSelectFlight) {
      onSelectFlight(flightNumber);
    } else {
      navigate(`/tracked-flights?flight=${flightNumber}`);
    }
  };

  const filteredItems = feedItems
    .filter((f) => {
      if (filter === 'IN_AIR') return f.status === 'DEPARTED';
      if (filter === 'BOARDING') return f.status === 'BOARDING';
      if (filter === 'DELAYED') return f.status === 'DELAYED';
      if (filter === 'INTL') return f.isInternational;
      return true;
    })
    .filter((f) => {
      if (!searchQuery.trim()) return true;
      const q = searchQuery.toLowerCase();
      return (
        f.flightNumber.toLowerCase().includes(q) ||
        f.airline.toLowerCase().includes(q) ||
        f.originCity.toLowerCase().includes(q) ||
        f.destCity.toLowerCase().includes(q) ||
        f.originCode.toLowerCase().includes(q) ||
        f.destCode.toLowerCase().includes(q)
      );
    })
    .slice(0, limit || feedItems.length);

  return (
    <div className="space-y-4">
      {/* 1. REAL-TIME EVENT STREAM TICKER */}
      <div className="flex items-center justify-between gap-3 px-4 py-2.5 bg-slate-900/95 border border-slate-800 rounded-2xl shadow-lg backdrop-blur-xl overflow-hidden text-xs">
        <div className="flex items-center gap-2 flex-shrink-0 text-emerald-400 font-mono font-bold tracking-wider uppercase text-[11px]">
          <span className="relative flex h-2.5 w-2.5">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500" />
          </span>
          <span className="flex items-center gap-1">
            <Radio className="w-3.5 h-3.5" />
            LIVE AIRSPACE FEED
          </span>
        </div>

        <div className="flex-1 overflow-hidden">
          <div className="transition-all duration-500 ease-in-out text-slate-300 font-mono truncate text-[11px] flex items-center gap-2">
            <span className="text-slate-500">[{lastTick.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}]</span>
            <span className="text-sky-300 font-semibold">{LIVE_EVENT_LOGS[activeEventIndex]}</span>
          </div>
        </div>

        <div className="hidden sm:flex items-center gap-1.5 text-[10px] text-slate-500 font-mono flex-shrink-0">
          <Activity className="w-3 h-3 text-sky-400" />
          <span>Active Radar Stream</span>
        </div>
      </div>

      {/* 2. FILTER & SEARCH TOOLBAR (shown in full view) */}
      {!compact && (
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
          <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0 text-xs scrollbar-none">
            <button
              type="button"
              onClick={() => setFilter('ALL')}
              className={`px-3 py-1.5 rounded-xl font-bold transition whitespace-nowrap flex items-center gap-1.5 ${
                filter === 'ALL'
                  ? 'bg-sky-500 text-white shadow-md shadow-sky-500/20'
                  : 'bg-slate-900 text-slate-400 hover:text-slate-200 border border-slate-800'
              }`}
            >
              <Layers className="w-3.5 h-3.5" />
              <span>All Airspace ({feedItems.length})</span>
            </button>
            <button
              type="button"
              onClick={() => setFilter('IN_AIR')}
              className={`px-3 py-1.5 rounded-xl font-bold transition whitespace-nowrap flex items-center gap-1.5 ${
                filter === 'IN_AIR'
                  ? 'bg-sky-500 text-white shadow-md shadow-sky-500/20'
                  : 'bg-slate-900 text-slate-400 hover:text-slate-200 border border-slate-800'
              }`}
            >
              <Plane className="w-3.5 h-3.5 text-sky-400" />
              <span>In Flight ({feedItems.filter((f) => f.status === 'DEPARTED').length})</span>
            </button>
            <button
              type="button"
              onClick={() => setFilter('BOARDING')}
              className={`px-3 py-1.5 rounded-xl font-bold transition whitespace-nowrap flex items-center gap-1.5 ${
                filter === 'BOARDING'
                  ? 'bg-amber-500 text-slate-950 shadow-md shadow-amber-500/20'
                  : 'bg-slate-900 text-slate-400 hover:text-slate-200 border border-slate-800'
              }`}
            >
              <Clock className="w-3.5 h-3.5 text-amber-400" />
              <span>Boarding ({feedItems.filter((f) => f.status === 'BOARDING').length})</span>
            </button>
            <button
              type="button"
              onClick={() => setFilter('DELAYED')}
              className={`px-3 py-1.5 rounded-xl font-bold transition whitespace-nowrap flex items-center gap-1.5 ${
                filter === 'DELAYED'
                  ? 'bg-rose-500 text-white shadow-md shadow-rose-500/20'
                  : 'bg-slate-900 text-slate-400 hover:text-slate-200 border border-slate-800'
              }`}
            >
              <AlertTriangle className="w-3.5 h-3.5 text-rose-400" />
              <span>Delayed ({feedItems.filter((f) => f.status === 'DELAYED').length})</span>
            </button>
            <button
              type="button"
              onClick={() => setFilter('INTL')}
              className={`px-3 py-1.5 rounded-xl font-bold transition whitespace-nowrap flex items-center gap-1.5 ${
                filter === 'INTL'
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/20'
                  : 'bg-slate-900 text-slate-400 hover:text-slate-200 border border-slate-800'
              }`}
            >
              <Compass className="w-3.5 h-3.5 text-indigo-400" />
              <span>International</span>
            </button>
          </div>

          <div className="relative min-w-[200px]">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-500" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Filter live radar..."
              className="w-full pl-9 pr-3 py-1.5 bg-slate-900 border border-slate-800 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-sky-500"
            />
          </div>
        </div>
      )}

      {/* 3. FLIGHT TELEMETRY CARDS FEED GRID */}
      <div className={`grid gap-3.5 ${compact ? 'grid-cols-1' : 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3'}`}>
        {filteredItems.map((flight) => {
          const isSelected = selectedFlightNumber?.toUpperCase() === flight.flightNumber.toUpperCase();

          return (
            <div
              key={flight.id}
              onClick={() => handleFlightClick(flight.flightNumber)}
              className={`group relative p-4 rounded-2xl cursor-pointer transition-all duration-200 border text-left overflow-hidden ${
                isSelected
                  ? 'bg-gradient-to-br from-sky-950/80 via-slate-900 to-slate-900 border-sky-500 shadow-xl shadow-sky-500/10 ring-1 ring-sky-500'
                  : 'bg-slate-900/90 hover:bg-slate-850 border-slate-800/90 hover:border-slate-700 shadow-lg hover:shadow-xl'
              }`}
            >
              {/* Airline & Status Bar */}
              <div className="flex items-center justify-between gap-2 mb-3">
                <div className="flex items-center gap-2">
                  <div className="w-7 h-7 rounded-lg bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-[11px] text-sky-400 font-mono">
                    {flight.airlineCode}
                  </div>
                  <div>
                    <div className="flex items-center gap-1.5">
                      <span className="font-mono font-black text-sm text-white tracking-wide">
                        {flight.flightNumber}
                      </span>
                      {flight.isInternational && (
                        <span className="px-1.5 py-0.2 bg-indigo-500/20 border border-indigo-500/30 text-indigo-300 text-[9px] font-bold rounded">
                          INTL
                        </span>
                      )}
                    </div>
                    <span className="text-[10px] text-slate-400 block leading-tight">
                      {flight.airline}
                    </span>
                  </div>
                </div>

                <span
                  className={`px-2.5 py-1 rounded-lg text-[10px] font-bold flex items-center gap-1.5 border ${
                    flight.status === 'DELAYED'
                      ? 'bg-rose-500/15 text-rose-400 border-rose-500/30'
                      : flight.status === 'BOARDING'
                      ? 'bg-amber-500/15 text-amber-400 border-amber-500/30'
                      : flight.status === 'DEPARTED'
                      ? 'bg-sky-500/15 text-sky-400 border-sky-500/30'
                      : 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30'
                  }`}
                >
                  <span className="w-1.5 h-1.5 rounded-full bg-current animate-pulse" />
                  {flight.status === 'DEPARTED' ? 'IN FLIGHT' : flight.status.replace('_', ' ')}
                </span>
              </div>

              {/* Route Display */}
              <div className="flex items-center justify-between gap-2 my-2.5 py-2 px-3 rounded-xl bg-slate-950/70 border border-slate-800/80 font-mono">
                <div>
                  <span className="text-base font-black text-white">{flight.originCode}</span>
                  <span className="text-[10px] text-slate-400 block truncate max-w-[80px]">
                    {flight.originCity}
                  </span>
                </div>

                <div className="flex-1 flex flex-col items-center px-2">
                  <div className="flex items-center gap-1 w-full justify-center text-sky-400">
                    <div className="h-[1px] flex-1 bg-gradient-to-r from-transparent via-sky-500 to-transparent" />
                    <Plane className="w-3.5 h-3.5 transform -rotate-45" />
                    <div className="h-[1px] flex-1 bg-gradient-to-r from-transparent via-sky-500 to-transparent" />
                  </div>
                  <span className="text-[9px] text-slate-500 mt-0.5">{flight.eta}</span>
                </div>

                <div className="text-right">
                  <span className="text-base font-black text-white">{flight.destCode}</span>
                  <span className="text-[10px] text-slate-400 block truncate max-w-[80px]">
                    {flight.destCity}
                  </span>
                </div>
              </div>

              {/* Progress Arc Bar */}
              {flight.status === 'DEPARTED' && (
                <div className="space-y-1 mb-2.5">
                  <div className="flex justify-between text-[10px] font-mono text-slate-400">
                    <span>Flight Progress</span>
                    <span className="text-sky-400 font-bold">{flight.progressPercent}%</span>
                  </div>
                  <div className="w-full bg-slate-950 rounded-full h-1.5 overflow-hidden border border-slate-800">
                    <div
                      className="bg-gradient-to-r from-sky-500 via-blue-500 to-indigo-500 h-1.5 rounded-full transition-all duration-700"
                      style={{ width: `${flight.progressPercent}%` }}
                    />
                  </div>
                </div>
              )}

              {/* Live Telemetry Bar */}
              <div className="flex items-center justify-between text-[10px] font-mono pt-2 border-t border-slate-800/60 text-slate-400">
                <div className="flex items-center gap-3">
                  <span>ALT: <strong className="text-slate-200">{flight.altitudeFeet > 0 ? `${flight.altitudeFeet.toLocaleString()} FT` : 'GND'}</strong></span>
                  <span>SPD: <strong className="text-slate-200">{flight.groundSpeedKmph > 0 ? `${flight.groundSpeedKmph} KM/H` : '0'}</strong></span>
                </div>

                <div className="flex items-center gap-1 text-sky-400 font-semibold group-hover:translate-x-0.5 transition-transform">
                  <span>Radar View</span>
                  <ChevronRight className="w-3.5 h-3.5" />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {filteredItems.length === 0 && (
        <div className="p-8 text-center bg-slate-900 border border-slate-800 rounded-2xl text-slate-400 text-xs">
          No live flights match your active filter or search query.
        </div>
      )}
    </div>
  );
};
