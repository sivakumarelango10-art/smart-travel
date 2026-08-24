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
      const cleanNumber = flightNumber.trim().toUpperCase();
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
      <section className="p-6 sm:p-8 rounded-2xl bg-white border border-slate-200 shadow-sm flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-secondary text-xs font-bold uppercase tracking-wider mb-1">
            <Radio className="w-4 h-4 text-emerald-500 animate-pulse" />
            <span>Global Airspace Telemetry</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-primary tracking-tight">
            Live Flight Radar & Radar Telemetry
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1 max-w-2xl">
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
            className="flex items-center gap-1.5 px-3.5 py-2 bg-slate-100 border border-slate-200 hover:bg-slate-200 text-xs font-bold text-slate-700 rounded-xl transition disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${searchLoading || loadingTracked ? 'animate-spin text-secondary' : ''}`} />
            <span>Live Refresh</span>
          </button>
          <Link
            to="/flights"
            className="flex items-center gap-1.5 px-4 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-bold rounded-xl transition shadow-sm"
          >
            <Search className="w-3.5 h-3.5" />
            <span>Search Flights</span>
          </Link>
        </div>
      </section>

      {/* 2. SEARCH BAR & POPULAR SUGGESTIONS */}
      <section className="p-5 sm:p-6 bg-white border border-slate-200 rounded-2xl shadow-sm space-y-4">
        <form onSubmit={handleSearchSubmit} className="flex flex-col sm:flex-row items-center gap-3">
          <div className="relative flex-1 w-full">
            <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value.toUpperCase())}
              placeholder="Enter Flight Number (e.g. AI-101, 6E-204, UK-955, EK-500)..."
              className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-primary font-mono font-bold placeholder-slate-400 focus:outline-none focus:border-secondary text-xs uppercase tracking-wider transition"
            />
          </div>
          <button
            type="submit"
            disabled={searchLoading || !searchQuery.trim()}
            className="w-full sm:w-auto px-6 py-3 bg-primary hover:bg-primary-hover text-white font-bold text-xs rounded-xl transition shadow-sm flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
          >
            <Radio className={`w-4 h-4 text-secondary ${searchLoading ? 'animate-spin' : ''}`} />
            <span>{searchLoading ? 'Scanning Airspace...' : 'Track Live Flight'}</span>
          </button>
        </form>

        {/* Quick Select Pills */}
        <div className="flex items-center gap-2 flex-wrap text-xs pt-1">
          <span className="text-slate-500 font-semibold flex items-center gap-1">
            <Compass className="w-3.5 h-3.5 text-secondary" />
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
                  ? 'bg-secondary text-white border-secondary shadow-sm'
                  : 'bg-slate-50 hover:bg-slate-100 border-slate-200 text-slate-700'
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
        <div className="p-4 bg-rose-50 border border-rose-200 text-rose-700 rounded-2xl text-xs flex items-center gap-3">
          <AlertTriangle className="w-4 h-4 flex-shrink-0 text-rose-500" />
          <span>{searchError}</span>
        </div>
      )}

      {trackActionMsg && (
        <div className="p-3.5 bg-emerald-50 border border-emerald-200 text-emerald-700 rounded-xl text-xs font-semibold flex items-center gap-2 animate-fade-in">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          <span>{trackActionMsg}</span>
        </div>
      )}

      {activeSnapshot && (
        <section className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-black text-primary">Live Aircraft Telemetry: {activeSnapshot.flightNumber}</h2>
            {isAuthenticated && (
              <button
                type="button"
                onClick={handleTrackCurrentFlight}
                disabled={isCurrentFlightTracked}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold flex items-center gap-1.5 transition ${
                  isCurrentFlightTracked
                    ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                    : 'bg-primary hover:bg-primary-hover text-white shadow-sm'
                }`}
              >
                {isCurrentFlightTracked ? (
                  <>
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                    <span>Tracked in Dashboard</span>
                  </>
                ) : (
                  <>
                    <BookmarkPlus className="w-3.5 h-3.5" />
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
          <h2 className="text-lg font-black text-primary">Live Commercial Air Traffic</h2>
          <p className="text-xs text-slate-500">Real-time ADS-B transponder telemetry across active flight paths</p>
        </div>

        <LiveAirspaceFeed
          compact={false}
          limit={12}
          onSelectFlight={handleSelectAirspaceFlight}
        />
      </section>

      {/* 5. USER'S SAVED TRACKED FLIGHTS */}
      {isAuthenticated && (
        <section className="space-y-4 pt-4 border-t border-slate-200">
          <div>
            <h2 className="text-lg font-black text-primary">My Saved Tracked Flights</h2>
            <p className="text-xs text-slate-500">Flights you are actively monitoring</p>
          </div>

          {loadingTracked ? (
            <div className="p-6 text-center text-xs text-slate-500 font-semibold">Loading saved flights...</div>
          ) : trackedFlights.length === 0 ? (
            <div className="p-8 rounded-2xl bg-white border border-slate-200 text-center space-y-2">
              <Plane className="w-8 h-8 text-slate-300 mx-auto" />
              <p className="text-xs text-slate-500">You are not actively tracking any flights.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {trackedFlights.map((tf) => (
                <div
                  key={tf.flightId}
                  className="p-4 rounded-xl bg-white border border-slate-200 hover:border-slate-300 hover:shadow-sm flex items-center justify-between gap-3 transition"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono font-bold text-primary text-sm">{tf.flightNumber}</span>
                      <span className="text-xs text-slate-500">{tf.departureAirportCode ?? tf.route?.split('→')[0]?.trim()} ➔ {tf.arrivalAirportCode ?? tf.route?.split('→')[1]?.trim()}</span>
                    </div>
                    <p className="text-[11px] text-slate-400 mt-0.5">{tf.route}</p>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => handleSelectAirspaceFlight(tf.flightNumber)}
                      className="px-3 py-1.5 rounded-lg bg-primary hover:bg-primary-hover text-white text-xs font-bold transition shadow-sm"
                    >
                      View Radar
                    </button>
                    <button
                      type="button"
                      onClick={() => handleUntrack(tf.flightId)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition"
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
