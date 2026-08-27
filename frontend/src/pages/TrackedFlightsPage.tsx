import React, { useState, useEffect, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  Plane,
  Radio,
  Trash2,
  RefreshCw,
  AlertTriangle,
  Search,
  CheckCircle2,
  Compass,
  BookmarkPlus
} from 'lucide-react';
import { TrackedFlight, FlightStatusSnapshot } from '../types/api';
import { flightTrackingService } from '../services/flightTrackingService';
import { flightService } from '../services/flightService';
import { pushNotificationService } from '../services/pushNotificationService';
import { FlightLiveStatusTracker } from '../components/FlightLiveStatusTracker';
import { LiveAirspaceFeed } from '../components/LiveAirspaceFeed';
import { PushNotificationModal } from '../components/PushNotificationModal';
import { useAuth } from '../context/AuthContext';

const POPULAR_FLIGHT_SUGGESTIONS = [
  { code: 'AI-101', route: 'DEL → BOM' },
  { code: '6E-204', route: 'BLR → DEL' },
  { code: 'UK-955', route: 'BOM → GOI' },
  { code: 'EK-500', route: 'DXB → BOM' },
  { code: 'BA-112', route: 'LHR → DEL' },
  { code: 'SQ-402', route: 'SIN → BOM' },
];

export const TrackedFlightsPage: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();

  // Search & Radar States
  const initialFlight = searchParams.get('flight') || 'AI-101';
  const [searchQuery, setSearchQuery] = useState(initialFlight);
  const [activeSnapshot, setActiveSnapshot] = useState<FlightStatusSnapshot | null>(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  // User Tracked Flights States
  const [trackedFlights, setTrackedFlights] = useState<TrackedFlight[]>([]);
  const [loadingTracked, setLoadingTracked] = useState(false);
  const [trackedError, setTrackedError] = useState<string | null>(null);

  // Push Notifications States
  const [showPushModal, setShowPushModal] = useState(false);

  // Quick Action feedback
  const [trackActionMsg, setTrackActionMsg] = useState<string | null>(null);

  // Fetch Live Status for Selected Flight
  const fetchLiveStatus = useCallback(async (flightNumber: string) => {
    if (!flightNumber.trim()) return;
    setSearchLoading(true);
    setSearchError(null);
    try {
      const cleanNumber = flightNumber.trim().toUpperCase().replace(/\s+/g, '-').replace(/_+/g, '-');
      const res = await flightService.getLiveFlightStatus(cleanNumber);
      if (res.success && res.data) {
        setActiveSnapshot(res.data);
      } else {
        setSearchError('Unable to locate live telemetry for flight ' + cleanNumber);
      }
    } catch (err: any) {
      setSearchError(err?.message || 'Error querying live flight radar');
    } finally {
      setSearchLoading(false);
    }
  }, []);

  // Fetch User Tracked Flights
  const fetchTracked = useCallback(async () => {
    if (!isAuthenticated) return;
    setLoadingTracked(true);
    setTrackedError(null);
    try {
      const data = await flightTrackingService.getTrackedFlights();
      setTrackedFlights(data);
    } catch (err: any) {
      setTrackedError(err.message || 'Failed to load tracked flights');
    } finally {
      setLoadingTracked(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    const flightParam = searchParams.get('flight');
    if (flightParam && flightParam.toUpperCase() !== searchQuery) {
      setSearchQuery(flightParam.toUpperCase());
      fetchLiveStatus(flightParam.toUpperCase());
    } else {
      fetchLiveStatus(searchQuery);
    }
    if (isAuthenticated) {
      fetchTracked();
      checkPushStatus();
    }
  }, [isAuthenticated, fetchTracked, fetchLiveStatus, searchParams]);

  const checkPushStatus = async () => {
    await pushNotificationService.isSubscribed();
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchLiveStatus(searchQuery);
    setSearchParams({ flight: searchQuery });
  };

  const handleSelectAirspaceFlight = (code: string) => {
    setSearchQuery(code);
    fetchLiveStatus(code);
    setSearchParams({ flight: code });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleTrackCurrentFlight = async () => {
    if (!activeSnapshot) return;
    if (!isAuthenticated) {
      alert('Please sign in to add this flight to your tracking board.');
      return;
    }

    const flightIdToTrack = (activeSnapshot.flightId && !activeSnapshot.flightId.startsWith('radar_') && !activeSnapshot.flightId.startsWith('sim_'))
      ? activeSnapshot.flightId
      : activeSnapshot.flightNumber;
    try {
      await flightTrackingService.trackFlight(flightIdToTrack);
      setTrackActionMsg(`Flight ${activeSnapshot.flightNumber} added to your tracking board!`);
      fetchTracked();
    } catch (err: any) {
      alert(err.message || 'Failed to track flight');
    } finally {
      setTimeout(() => setTrackActionMsg(null), 4000);
    }
  };

  const handleUntrack = async (flightId: string) => {
    if (!confirm('Stop tracking this flight?')) return;
    try {
      await flightTrackingService.untrackFlight(flightId);
      setTrackedFlights((prev) => prev.filter((f) => f.flightId !== flightId));
    } catch (err: any) {
      alert(err.message || 'Failed to untrack flight');
    }
  };

  const isCurrentFlightTracked = Boolean(
    activeSnapshot &&
      trackedFlights.some(
        (tf) =>
          tf.flightNumber.toUpperCase() === activeSnapshot.flightNumber.toUpperCase() ||
          tf.flightId === activeSnapshot.flightId
      )
  );

  return (
    <div className="space-y-8 pb-16 max-w-6xl mx-auto">
      {/* 1. HERO HEADER */}
      <section className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-2xl flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-amber-400 text-xs font-bold uppercase tracking-wider mb-1">
            <Radio className="w-4 h-4 text-amber-400 animate-pulse" />
            <span>Global Airspace Telemetry</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
            Live Flight Radar & Telemetry
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1 max-w-2xl">
            Track commercial flights in real time with live airspace radar telemetry, altitude profiles, speed gauges, and gate status alerts.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            onClick={() => {
              if (activeSnapshot) fetchLiveStatus(activeSnapshot.flightNumber);
              if (isAuthenticated) fetchTracked();
            }}
            disabled={searchLoading || loadingTracked}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-[#181A22] border border-white/10 hover:bg-[#1F222E] text-xs font-bold text-slate-300 rounded-xl transition disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${searchLoading || loadingTracked ? 'animate-spin text-amber-400' : 'text-amber-400'}`} />
            <span>Live Refresh</span>
          </button>
          <Link
            to="/flights"
            className="flex items-center gap-1.5 px-4 py-2 bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs rounded-xl shadow-glow-gold transition"
          >
            <Search className="w-3.5 h-3.5 text-black" />
            <span>Search Flights</span>
          </Link>
        </div>
      </section>

      {/* 2. SEARCH BAR & POPULAR SUGGESTIONS */}
      <section className="p-5 sm:p-6 bg-[#14161F] border border-white/10 rounded-2xl shadow-xl space-y-4">
        <form onSubmit={handleSearchSubmit} className="flex flex-col sm:flex-row items-center gap-3">
          <div className="relative flex-1 w-full">
            <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value.toUpperCase())}
              placeholder="Enter Flight Number (e.g. AI-101, 6E-204, UK-955, EK-500)..."
              className="w-full pl-11 pr-4 py-3 bg-[#181A22] border border-white/10 rounded-xl text-white font-mono font-bold placeholder-slate-500 focus:outline-none focus:border-amber-400 text-xs uppercase tracking-wider transition"
            />
          </div>
          <button
            type="submit"
            disabled={searchLoading || !searchQuery.trim()}
            className="w-full sm:w-auto px-6 py-3 bg-gradient-to-r from-amber-400 to-amber-500 text-black font-black text-xs rounded-xl shadow-glow-gold transition flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
          >
            <Radio className={`w-4 h-4 text-black ${searchLoading ? 'animate-spin' : ''}`} />
            <span>{searchLoading ? 'Scanning Airspace...' : 'Track Live Flight'}</span>
          </button>
        </form>

        {/* Quick Select Pills */}
        <div className="flex items-center gap-2 flex-wrap text-xs pt-1">
          <span className="text-slate-400 font-semibold flex items-center gap-1">
            <Compass className="w-3.5 h-3.5 text-amber-400" />
            Active Airspace:
          </span>
          {POPULAR_FLIGHT_SUGGESTIONS.map((item) => (
            <button
              key={item.code}
              type="button"
              onClick={() => {
                setSearchQuery(item.code);
                fetchLiveStatus(item.code);
              }}
              className={`px-2.5 py-1 rounded-lg text-xs font-mono font-bold transition flex items-center gap-1.5 border ${
                activeSnapshot?.flightNumber === item.code
                  ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black border-transparent shadow-glow-gold'
                  : 'bg-[#181A22] hover:bg-[#1F222E] border-white/10 text-slate-300'
              }`}
            >
              <span>{item.code}</span>
              <span className="text-[10px] opacity-75">({item.route})</span>
            </button>
          ))}
        </div>
      </section>

      {/* 3. ACTIVE LIVE FLIGHT RADAR & TELEMETRY */}
      {searchError && (
        <div className="p-4 bg-rose-500/15 border border-rose-500/30 text-rose-400 rounded-2xl text-xs flex items-center gap-3">
          <AlertTriangle className="w-4 h-4 flex-shrink-0 text-rose-400" />
          <span>{searchError}</span>
        </div>
      )}

      {trackActionMsg && (
        <div className="p-3.5 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-xl text-xs font-semibold flex items-center gap-2 animate-fade-in shadow-glow-emerald">
          <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          <span>{trackActionMsg}</span>
        </div>
      )}

      {activeSnapshot && (
        <section className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-black text-white">Live Aircraft Telemetry: {activeSnapshot.flightNumber}</h2>
            {isAuthenticated && (
              <button
                type="button"
                onClick={handleTrackCurrentFlight}
                disabled={isCurrentFlightTracked}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold flex items-center gap-1.5 transition ${
                  isCurrentFlightTracked
                    ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                    : 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                }`}
              >
                {isCurrentFlightTracked ? (
                  <>
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Tracked in Dashboard</span>
                  </>
                ) : (
                  <>
                    <BookmarkPlus className="w-3.5 h-3.5 text-black" />
                    <span>Save to Tracked Flights</span>
                  </>
                )}
              </button>
            )}
          </div>

          <FlightLiveStatusTracker flightId={activeSnapshot.flightId || activeSnapshot.flightNumber} initialSnapshot={activeSnapshot} />
        </section>
      )}

      {/* 4. LIVE AIRSPACE TRAFFIC FEED */}
      <section className="space-y-4">
        <div>
          <h2 className="text-lg font-black text-white">Live Commercial Air Traffic</h2>
          <p className="text-xs text-slate-400">Real-time ADS-B transponder telemetry across active flight paths</p>
        </div>

        <LiveAirspaceFeed
          compact={false}
          limit={12}
          onSelectFlight={handleSelectAirspaceFlight}
        />
      </section>

      {/* 5. USER'S SAVED TRACKED FLIGHTS */}
      {isAuthenticated && (
        <section className="space-y-4 pt-4 border-t border-white/10">
          <div>
            <h2 className="text-lg font-black text-white">My Saved Tracked Flights</h2>
            <p className="text-xs text-slate-400">Flights you are actively monitoring</p>
          </div>

          {loadingTracked ? (
            <div className="p-6 text-center text-xs text-slate-400 font-semibold">Loading saved flights...</div>
          ) : trackedFlights.length === 0 ? (
            <div className="p-8 rounded-2xl bg-[#14161F] border border-white/10 text-center space-y-2">
              <Plane className="w-8 h-8 text-slate-600 mx-auto" />
              <p className="text-xs text-slate-400">You are not actively tracking any flights.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {trackedFlights.map((tf) => (
                <div
                  key={tf.flightId}
                  className="p-4 rounded-xl bg-[#14161F] border border-white/10 hover:border-amber-500/30 hover:shadow-card-hover flex items-center justify-between gap-3 transition"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono font-bold text-amber-400 text-sm">{tf.flightNumber}</span>
                      <span className="text-xs text-slate-300">{tf.departureAirportCode ?? tf.route?.split('→')[0]?.trim()} ➔ {tf.arrivalAirportCode ?? tf.route?.split('→')[1]?.trim()}</span>
                    </div>
                    <p className="text-[11px] text-slate-500 mt-0.5">{tf.route}</p>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => handleSelectAirspaceFlight(tf.flightNumber)}
                      className="px-3 py-1.5 rounded-lg bg-gradient-to-r from-amber-400 to-amber-500 text-black text-xs font-black transition shadow-glow-gold"
                    >
                      View Radar
                    </button>
                    <button
                      type="button"
                      onClick={() => handleUntrack(tf.flightId)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition"
                      title="Remove from tracking"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      )}

      {showPushModal && <PushNotificationModal isOpen={showPushModal} onClose={() => setShowPushModal(false)} />}
    </div>
  );
};
