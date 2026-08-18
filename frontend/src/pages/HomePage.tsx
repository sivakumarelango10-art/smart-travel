import React, { useState, useEffect } from 'react';
import { 
  Activity, 
  Database, 
  Server, 
  Sparkles, 
  RefreshCw, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  Layers, 
  Plane,
  Coins,
  RotateCcw,
  Armchair,
  Star,
  Compass
} from 'lucide-react';
import { healthService } from '../services/healthService';
import { HealthData } from '../types/api';

export const HomePage: React.FC = () => {
  const [health, setHealth] = useState<HealthData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [latencyMs, setLatencyMs] = useState<number | null>(null);
  const [lastChecked, setLastChecked] = useState<string | null>(null);

  const fetchHealth = async () => {
    setLoading(true);
    setError(null);
    const start = performance.now();
    try {
      const res = await healthService.getHealth();
      const end = performance.now();
      setLatencyMs(Math.round(end - start));
      setHealth(res.data);
      setLastChecked(new Date().toLocaleTimeString());
    } catch (err: any) {
      setError(err?.message || 'Failed to connect to SmartTravel backend API');
      setHealth(null);
      setLatencyMs(null);
      setLastChecked(new Date().toLocaleTimeString());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  return (
    <div className="space-y-12">
      {/* Hero Banner */}
      <section className="text-center space-y-4 max-w-3xl mx-auto pt-4">
        <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-sky-500/10 border border-sky-500/20 text-sky-400 text-xs font-semibold tracking-wide">
          <Sparkles className="w-3.5 h-3.5" />
          Phase 1 Foundation Operational
        </div>

        <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight bg-gradient-to-r from-white via-slate-200 to-sky-400 bg-clip-text text-transparent">
          SmartTravel Platform
        </h1>

        <p className="text-slate-400 text-sm sm:text-base leading-relaxed">
          Clean architectural foundation connecting a high-throughput Java 21 & Spring Boot 3.3.x backend with MongoDB and a modern React 18 + TypeScript frontend.
        </p>
      </section>

      {/* Live Frontend ↔ Backend Integration Card */}
      <section aria-label="System Connectivity Health" className="max-w-2xl mx-auto">
        <div className="rounded-2xl bg-slate-900/90 border border-slate-800 p-6 shadow-2xl backdrop-blur-xl relative overflow-hidden">
          {/* Top Gradient Accent */}
          <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-sky-500 via-indigo-500 to-emerald-500"></div>

          <div className="flex items-center justify-between pb-4 border-b border-slate-800">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20">
                <Activity className="w-5 h-5" />
              </div>
              <div>
                <h2 className="font-semibold text-white text-base">Backend & Database Connectivity</h2>
                <p className="text-xs text-slate-400">Real-time health telemetry from <code className="text-sky-400 bg-sky-950/60 px-1 py-0.5 rounded">GET /api/health</code></p>
              </div>
            </div>

            <button
              onClick={fetchHealth}
              disabled={loading}
              className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 hover:text-white border border-slate-700 text-xs font-medium flex items-center gap-1.5 transition disabled:opacity-50"
              title="Re-ping backend health endpoint"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-sky-400' : ''}`} />
              Ping Health
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-4">
            {/* Backend Service Status */}
            <div className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800/80 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span className="flex items-center gap-1">
                  <Server className="w-3.5 h-3.5 text-sky-400" />
                  Backend Service
                </span>
                {health?.status === 'UP' ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                ) : (
                  <XCircle className="w-4 h-4 text-rose-400" />
                )}
              </div>
              <div className="font-semibold text-sm text-white flex items-center gap-2">
                {loading ? (
                  <span className="text-slate-500 animate-pulse">Checking...</span>
                ) : health?.status === 'UP' ? (
                  <span className="text-emerald-400">Backend Status: Connected</span>
                ) : (
                  <span className="text-rose-400">Disconnected</span>
                )}
              </div>
              <p className="text-[11px] text-slate-500">{health?.service || 'SmartTravel Backend'}</p>
            </div>

            {/* Database Status */}
            <div className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800/80 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span className="flex items-center gap-1">
                  <Database className="w-3.5 h-3.5 text-indigo-400" />
                  MongoDB
                </span>
                {health?.database === 'CONNECTED' ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                ) : (
                  <Clock className="w-4 h-4 text-amber-400" />
                )}
              </div>
              <div className="font-semibold text-sm text-white">
                {loading ? (
                  <span className="text-slate-500 animate-pulse">Checking...</span>
                ) : health?.database === 'CONNECTED' ? (
                  <span className="text-emerald-400">MongoDB: Connected</span>
                ) : health?.database === 'DISCONNECTED' ? (
                  <span className="text-amber-400">MongoDB: Standby/Test</span>
                ) : (
                  <span className="text-slate-400">Ready</span>
                )}
              </div>
              <p className="text-[11px] text-slate-500">Profile: {health?.environment || 'dev'}</p>
            </div>

            {/* Network Latency */}
            <div className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800/80 space-y-1">
              <div className="flex items-center justify-between text-xs text-slate-400">
                <span className="flex items-center gap-1">
                  <Clock className="w-3.5 h-3.5 text-amber-400" />
                  API Latency
                </span>
              </div>
              <div className="font-semibold text-sm text-white font-mono">
                {loading ? (
                  <span className="text-slate-500 animate-pulse">Measuring...</span>
                ) : latencyMs !== null ? (
                  <span className="text-sky-300">{latencyMs} ms</span>
                ) : (
                  <span className="text-slate-500">—</span>
                )}
              </div>
              <p className="text-[11px] text-slate-500">Last checked: {lastChecked || 'Initial'}</p>
            </div>
          </div>

          {error && (
            <div className="mt-4 p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs flex items-center gap-2">
              <XCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}
        </div>
      </section>

      {/* Feature Execution Roadmap Overview */}
      <section aria-label="Roadmap Modules" className="space-y-6 max-w-5xl mx-auto">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-white tracking-tight flex items-center gap-2">
              <Layers className="w-5 h-5 text-sky-400" />
              Planned Business Modules (Staged Implementation)
            </h2>
            <p className="text-xs text-slate-400">Architected and ready for sequential implementation in subsequent phases.</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800/80 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold px-2 py-0.5 rounded bg-sky-500/10 text-sky-400 border border-sky-500/20">
                Phase 2
              </span>
              <Plane className="w-4 h-4 text-sky-400" />
            </div>
            <h3 className="font-semibold text-white text-sm">Live Flight Radar & Telemetry</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Real-time flight simulation, dynamic ETA recalibration, STOMP WebSocket broadcasts, and multi-flight dashboards.
            </p>
          </div>

          <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800/80 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                Phase 3
              </span>
              <Coins className="w-4 h-4 text-emerald-400" />
            </div>
            <h3 className="font-semibold text-white text-sm">Dynamic Pricing & Price Freeze</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Multi-factor algorithmic surge pricing, transparent price breakdowns, price history charts, and 30-minute freeze guarantees.
            </p>
          </div>

          <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800/80 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold px-2 py-0.5 rounded bg-rose-500/10 text-rose-400 border border-rose-500/20">
                Phase 4
              </span>
              <RotateCcw className="w-4 h-4 text-rose-400" />
            </div>
            <h3 className="font-semibold text-white text-sm">Cancellation & Refund Engine</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Configurable tier-based policy engine, automated refund state machine, immutable audit timeline, and mock payment gateway.
            </p>
          </div>

          <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800/80 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20">
                Phase 5
              </span>
              <Armchair className="w-4 h-4 text-amber-400" />
            </div>
            <h3 className="font-semibold text-white text-sm">Dynamic Seat & Room Selection</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Aircraft cabin seat maps with atomic concurrency locks, hotel room categories with image previews, and persistent user preferences.
            </p>
          </div>

          <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800/80 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold px-2 py-0.5 rounded bg-purple-500/10 text-purple-400 border border-purple-500/20">
                Phase 6
              </span>
              <Star className="w-4 h-4 text-purple-400" />
            </div>
            <h3 className="font-semibold text-white text-sm">Verified Reviews & Moderation</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Booking-verified rating gatekeeper, community helpful votes, report flagging, and staff moderation review queues.
            </p>
          </div>

          <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800/80 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
                Phase 6
              </span>
              <Compass className="w-4 h-4 text-cyan-400" />
            </div>
            <h3 className="font-semibold text-white text-sm">Hybrid Recommendations</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Content-based and collaborative filtering with transparent "Why this recommendation?" justifications and user feedback loops.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
};
