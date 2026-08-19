import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ShieldCheck,
  Zap,
  RotateCcw,
  Sparkles,
  ArrowRight,
  TrendingUp,
  RefreshCw
} from 'lucide-react';
import { FlightSearchWidget } from '../components/FlightSearchWidget';
import { healthService } from '../services/healthService';
import { HealthData } from '../types/api';

const POPULAR_ROUTES = [
  { from: 'DEL', to: 'BOM', fromCity: 'New Delhi', toCity: 'Mumbai', price: '4,500', time: '2h 10m', image: '🌆' },
  { from: 'BLR', to: 'DEL', fromCity: 'Bengaluru', toCity: 'New Delhi', price: '5,200', time: '2h 45m', image: '🏛️' },
  { from: 'BOM', to: 'BLR', fromCity: 'Mumbai', toCity: 'Bengaluru', price: '3,800', time: '1h 50m', image: '🌳' },
  { from: 'DEL', to: 'DXB', fromCity: 'New Delhi', toCity: 'Dubai', price: '12,900', time: '3h 50m', image: '🏖️' },
];

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [health, setHealth] = useState<HealthData | null>(null);
  const [healthLoading, setHealthLoading] = useState<boolean>(true);

  const fetchHealth = async () => {
    setHealthLoading(true);
    try {
      const res = await healthService.getHealth();
      setHealth(res.data);
    } catch {
      setHealth(null);
    } finally {
      setHealthLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  const handleQuickRoute = (from: string, to: string) => {
    const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
    navigate(`/flights?origin=${from}&destination=${to}&departureDate=${tomorrow}&cabinClass=ECONOMY&passengers=1`);
  };

  return (
    <div className="space-y-16 py-4">
      {/* Hero Section */}
      <section className="text-center space-y-6 max-w-4xl mx-auto pt-4 sm:pt-8">
        <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-gradient-to-r from-sky-500/10 via-indigo-500/10 to-emerald-500/10 border border-sky-500/20 text-sky-400 text-xs font-bold tracking-wide shadow-sm">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Real-Time Atomic Flight Engine • Instant Seat Map Lock</span>
        </div>

        <h1 className="text-4xl sm:text-6xl font-black tracking-tight bg-gradient-to-r from-white via-slate-100 to-sky-400 bg-clip-text text-transparent leading-tight">
          Fly Smarter with High-Precision Booking
        </h1>

        <p className="text-slate-400 text-sm sm:text-base max-w-2xl mx-auto leading-relaxed">
          Search thousands of routes, pick your exact aircraft seat in real-time, and experience automated disruption refunds backed by enterprise reliability.
        </p>

        {/* Embedded Flight Search Widget */}
        <div className="pt-4 text-left">
          <FlightSearchWidget />
        </div>
      </section>

      {/* Platform Guarantees Banner */}
      <section className="grid grid-cols-1 sm:grid-cols-3 gap-6 max-w-6xl mx-auto">
        <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 shadow-xl flex items-start gap-4">
          <div className="w-12 h-12 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center shrink-0">
            <Zap className="w-6 h-6" />
          </div>
          <div>
            <h3 className="font-bold text-white text-base">Atomic Seat Holds</h3>
            <p className="text-xs text-slate-400 mt-1 leading-relaxed">
              Real-time cabin seat maps with instant 15-minute reservation locking. No double-bookings.
            </p>
          </div>
        </div>

        <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 shadow-xl flex items-start gap-4">
          <div className="w-12 h-12 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 flex items-center justify-center shrink-0">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <h3 className="font-bold text-white text-base">Instant E-Ticket & Pass</h3>
            <p className="text-xs text-slate-400 mt-1 leading-relaxed">
              Authoritative PDF ticket issuance, online check-in, and mobile boarding passes.
            </p>
          </div>
        </div>

        <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 shadow-xl flex items-start gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center shrink-0">
            <RotateCcw className="w-6 h-6" />
          </div>
          <div>
            <h3 className="font-bold text-white text-base">Auto-Refund Disruption</h3>
            <p className="text-xs text-slate-400 mt-1 leading-relaxed">
              Real-time flight status tracking with automatic gateway refund triggers on cancellation.
            </p>
          </div>
        </div>
      </section>

      {/* Popular Trending Routes */}
      <section className="max-w-6xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-sky-400" />
            <h2 className="text-xl font-bold text-white">Popular Domestic & International Routes</h2>
          </div>
          <span className="text-xs text-slate-400 hidden sm:inline">Best fares for tomorrow</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {POPULAR_ROUTES.map((route) => (
            <div
              key={`${route.from}-${route.to}`}
              onClick={() => handleQuickRoute(route.from, route.to)}
              className="p-5 rounded-2xl bg-slate-900/80 hover:bg-slate-850 border border-slate-800 hover:border-slate-700 shadow-xl cursor-pointer transition duration-150 group flex flex-col justify-between space-y-4"
            >
              <div className="flex items-center justify-between">
                <span className="text-2xl">{route.image}</span>
                <span className="text-xs font-mono font-bold px-2 py-0.5 rounded bg-sky-500/10 text-sky-400 border border-sky-500/20">
                  {route.time}
                </span>
              </div>

              <div>
                <div className="flex items-center gap-2 font-bold text-white text-base group-hover:text-sky-400 transition">
                  <span>{route.fromCity}</span>
                  <ArrowRight className="w-4 h-4 text-slate-500 group-hover:translate-x-1 transition-transform" />
                  <span>{route.toCity}</span>
                </div>
                <p className="text-xs text-slate-500 font-mono mt-0.5">
                  {route.from} ➔ {route.to}
                </p>
              </div>

              <div className="pt-3 border-t border-slate-800 flex items-baseline justify-between">
                <span className="text-xs text-slate-400">From</span>
                <span className="text-lg font-black text-emerald-400">₹{route.price}</span>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Backend Health Status Badge */}
      <section className="max-w-md mx-auto pt-4">
        <div className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800 shadow-lg flex items-center justify-between text-xs">
          <div className="flex items-center gap-2.5">
            <div
              className={`w-2.5 h-2.5 rounded-full ${
                health?.status === 'UP' && health?.database === 'CONNECTED'
                  ? 'bg-emerald-400 animate-pulse'
                  : 'bg-amber-400'
              }`}
            ></div>
            <span className="text-slate-300 font-medium">
              API Status: <strong className="text-white">{health?.status || 'ONLINE'}</strong> • DB:{' '}
              <strong className="text-sky-400">{health?.database || 'CONNECTED'}</strong>
            </span>
          </div>

          <button
            onClick={fetchHealth}
            disabled={healthLoading}
            className="text-slate-400 hover:text-white transition"
            title="Re-verify health"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${healthLoading ? 'animate-spin text-sky-400' : ''}`} />
          </button>
        </div>
      </section>
    </div>
  );
};
