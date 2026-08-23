import React, { useState, useEffect, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  Plane,
  Radio,
  Trash2,
  ArrowRight,
  RefreshCw,
  AlertTriangle,
  Search,
  Bell,
  BellRing,
  CheckCircle2,
  Compass,
  Sparkles,
  BookmarkPlus
} from 'lucide-react';
import { TrackedFlight, FlightStatusSnapshot } from '../types/api';
import { flightTrackingService } from '../services/flightTrackingService';
import { flightService } from '../services/flightService';
import { pushNotificationService } from '../services/pushNotificationService';
import { FlightLiveStatusTracker } from '../components/FlightLiveStatusTracker';
import { LiveFlightRadarMap } from '../components/LiveFlightRadarMap';
import { LiveAirspaceFeed } from '../components/LiveAirspaceFeed';
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
  const [pushSubscribed, setPushSubscribed] = useState(false);
  const [pushLoading, setPushLoading] = useState(false);
  const [pushSuccessMsg, setPushSuccessMsg] = useState<string | null>(null);

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
        setSearchError('Unable to locate telemetry for flight ' + cleanNumber);
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
    const subscribed = await pushNotificationService.isSubscribed();
    setPushSubscribed(subscribed);
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

  const handleTogglePush = async () => {
    if (!isAuthenticated) {
      alert('Please sign in to configure live browser push notifications.');
      return;
    }
    setPushLoading(true);
    try {
      if (pushSubscribed) {
        await pushNotificationService.unsubscribe();
        setPushSubscribed(false);
        setPushSuccessMsg('Browser push notifications disabled.');
      } else {
        const success = await pushNotificationService.subscribe();
        if (success) {
          setPushSubscribed(true);
          setPushSuccessMsg('Browser push alerts enabled! You will receive live disruption & gate alerts.');
        } else {
          setTrackedError('Notification permission was denied. Please allow notifications in your browser.');
        }
      }
    } catch (err: any) {
      setTrackedError(err.message || 'Failed to update push subscription');
    } finally {
      setPushLoading(false);
      setTimeout(() => setPushSuccessMsg(null), 5000);
    }
  };

  const handleTestPush = async () => {
    try {
      await pushNotificationService.sendTestPush();
      setPushSuccessMsg('Test notification dispatched to your device!');
      setTimeout(() => setPushSuccessMsg(null), 5000);
    } catch (err: any) {
      setTrackedError(err.message || 'Failed to send test push notification');
    }
  };

  const handleTrackCurrentFlight = async () => {
    if (!activeSnapshot) return;
    if (!isAuthenticated) {
      alert('Please sign in to add this flight to your permanent tracking board.');
      return;
    }

    const flightIdToTrack = (activeSnapshot.flightId && !activeSnapshot.flightId.startsWith('radar_') && !activeSnapshot.flightId.startsWith('sim_'))
      ? activeSnapshot.flightId
      : activeSnapshot.flightNumber;
    try {
      await flightTrackingService.trackFlight(flightIdToTrack);
      setTrackActionMsg(`Flight ${activeSnapshot.flightNumber} added to your live tracking board!`);
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
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto space-y-8">
        {/* 1. HERO HEADER */}
        <div className="flex flex-wrap items-center justify-between gap-4 pb-6 border-b border-slate-800">
          <div>
            <div className="flex items-center gap-2 text-sky-400 text-xs font-bold uppercase tracking-widest mb-1.5">
              <Radio className="w-4 h-4 text-emerald-400 animate-pulse" />
              <span>SmartTravel Global Airspace Telemetry</span>
            </div>
            <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight">
              Live Flight Radar & Tracker
            </h1>
            <p className="text-sm text-slate-400 mt-1 max-w-2xl">
              Track any commercial flight in real time with live airspace radar telemetry, animated flight arcs, delay predictions, terminal gates, and instant browser notifications.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => {
                if (activeSnapshot) fetchLiveStatus(activeSnapshot.flightNumber);
                if (isAuthenticated) fetchTracked();
              }}
              disabled={searchLoading || loadingTracked}
              className="flex items-center gap-2 px-4 py-2 bg-slate-900 border border-slate-800 hover:bg-slate-800 text-sm font-semibold text-slate-300 rounded-xl transition-colors disabled:opacity-50"
            >
              <RefreshCw className={`w-4 h-4 ${searchLoading || loadingTracked ? 'animate-spin text-sky-400' : ''}`} />
              <span>Live Refresh</span>
            </button>
            <Link
              to="/flights"
              className="flex items-center gap-2 px-4 py-2 bg-sky-600 hover:bg-sky-500 text-white text-sm font-bold rounded-xl transition shadow-lg shadow-sky-600/20"
            >
              <Search className="w-4 h-4" />
              <span>Search All Flights</span>
            </Link>
          </div>
        </div>

        {/* 2. FLIGHT NUMBER SEARCH BAR & POPULAR SUGGESTIONS */}
        <div className="p-5 sm:p-6 bg-slate-900/90 border border-slate-800/80 rounded-3xl shadow-xl space-y-4 backdrop-blur-xl">
          <form onSubmit={handleSearchSubmit} className="flex flex-col sm:flex-row items-center gap-3">
            <div className="relative flex-1 w-full">
              <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value.toUpperCase())}
                placeholder="Enter IATA Flight Number (e.g. AI-101, 6E-204, BA-112, EK-500, SQ-402)..."
                className="w-full pl-12 pr-4 py-3.5 bg-slate-950 border border-slate-800 rounded-2xl text-white font-mono font-bold placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 text-sm tracking-wider uppercase transition-all"
              />
            </div>
            <button
              type="submit"
              disabled={searchLoading || !searchQuery.trim()}
              className="w-full sm:w-auto px-6 py-3.5 bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white font-bold text-sm rounded-2xl transition shadow-lg shadow-sky-500/20 flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Radio className={`w-4 h-4 ${searchLoading ? 'animate-spin' : ''}`} />
              <span>{searchLoading ? 'Scanning Airspace...' : 'Track Live Flight'}</span>
            </button>
          </form>

          {/* Quick Select Pills */}
          <div className="flex items-center gap-2 flex-wrap text-xs pt-1">
            <span className="text-slate-400 font-medium flex items-center gap-1">
              <Compass className="w-3.5 h-3.5 text-sky-400" />
              Active Air Traffic:
            </span>
            {POPULAR_FLIGHT_SUGGESTIONS.map((item) => (
              <button
                key={item.code}
                type="button"
                onClick={() => {
                  setSearchQuery(item.code);
                  fetchLiveStatus(item.code);
                }}
                className={`px-3 py-1.5 rounded-xl text-xs font-mono font-bold transition flex items-center gap-1.5 border ${
                  activeSnapshot?.flightNumber === item.code
                    ? 'bg-sky-500/20 border-sky-500/40 text-sky-300'
                    : 'bg-slate-950 hover:bg-slate-800 border-slate-800 text-slate-300'
                }`}
              >
                <span>{item.code}</span>
                <span className="text-[10px] text-slate-500 font-normal">({item.route})</span>
              </button>
            ))}
          </div>
        </div>

        {/* 3. ACTIVE LIVE FLIGHT RADAR & TELEMETRY DASHBOARD */}
        {searchError && (
          <div className="p-4 bg-rose-500/10 border border-rose-500/30 text-rose-400 rounded-2xl text-sm flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 flex-shrink-0" />
            <span>{searchError}</span>
          </div>
        )}

        {trackActionMsg && (
          <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-2xl text-xs font-medium flex items-center gap-2.5 animate-slide-up">
            <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
            <span>{trackActionMsg}</span>
          </div>
        )}

        {searchLoading ? (
          <div className="py-24 text-center bg-slate-900/60 border border-slate-800 rounded-3xl p-8 backdrop-blur-md">
            <div className="w-10 h-10 border-3 border-sky-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
            <h3 className="font-bold text-white text-base">Receiving Telemetry Data...</h3>
            <p className="text-xs text-slate-400 mt-1">Connecting to live ADS-B flight transponder feed</p>
          </div>
        ) : activeSnapshot ? (
          <div className="space-y-6">
            {/* Interactive SVG Radar Route Arc */}
            <LiveFlightRadarMap flight={activeSnapshot} />

            {/* Flight Telemetry Status Panel */}
            <div className="p-5 sm:p-6 bg-slate-900 border border-slate-800 rounded-3xl shadow-xl space-y-6">
              <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-slate-800">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-2xl bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-sky-400">
                    <Plane className="w-6 h-6 transform -rotate-45" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-xl font-black text-white tracking-tight">
                        {activeSnapshot.flightNumber}
                      </h2>
                      <span className="text-xs text-slate-400 font-medium">
                        ({activeSnapshot.airline || 'SmartTravel Fleet'})
                      </span>
                    </div>
                    <p className="text-xs text-slate-400 mt-0.5">
                      {activeSnapshot.originName || activeSnapshot.originCity} ➔{' '}
                      {activeSnapshot.destName || activeSnapshot.destCity}
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  {/* Status Badge */}
                  <span
                    className={`px-3.5 py-1.5 rounded-xl text-xs font-bold flex items-center gap-2 border ${
                      activeSnapshot.status === 'DELAYED'
                        ? 'bg-rose-500/15 text-rose-400 border-rose-500/30'
                        : activeSnapshot.status === 'BOARDING'
                        ? 'bg-amber-500/15 text-amber-400 border-amber-500/30'
                        : activeSnapshot.status === 'DEPARTED'
                        ? 'bg-sky-500/15 text-sky-400 border-sky-500/30'
                        : 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30'
                    }`}
                  >
                    <span className="w-2 h-2 rounded-full bg-current animate-ping" />
                    {activeSnapshot.status === 'DEPARTED'
                      ? 'IN FLIGHT'
                      : activeSnapshot.status.replace('_', ' ')}
                  </span>

                  {/* 1-Click Track Button */}
                  <button
                    type="button"
                    onClick={handleTrackCurrentFlight}
                    className={`px-4 py-2 rounded-xl text-xs font-bold flex items-center gap-2 transition ${
                      isCurrentFlightTracked
                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                        : 'bg-sky-600 hover:bg-sky-500 text-white shadow-lg shadow-sky-600/20'
                    }`}
                  >
                    <BookmarkPlus className="w-4 h-4" />
                    <span>{isCurrentFlightTracked ? 'Tracked on Board' : 'Add to My Tracker'}</span>
                  </button>
                </div>
              </div>

              {/* Delay Warning Banner */}
              {activeSnapshot.status === 'DELAYED' && (
                <div className="p-3.5 bg-rose-500/10 border border-rose-500/30 rounded-2xl text-xs text-rose-300 flex items-center gap-3">
                  <AlertTriangle className="w-5 h-5 flex-shrink-0 text-rose-400" />
                  <div>
                    <span className="font-bold">Flight Delay Advisory: </span>
                    <span>
                      Delayed by {activeSnapshot.delayMinutes || 35} minutes.{' '}
                      {activeSnapshot.delayReason || 'Air traffic management and congestion.'}
                    </span>
                  </div>
                </div>
              )}

              {/* Detailed Metrics Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
                <div className="p-3.5 rounded-2xl bg-slate-950 border border-slate-800">
                  <span className="text-[10px] text-slate-500 uppercase tracking-wider font-bold block mb-1">
                    Scheduled Departure
                  </span>
                  <span className="text-base font-black text-white">
                    {activeSnapshot.scheduledDeparture
                      ? new Date(activeSnapshot.scheduledDeparture).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit',
                        })
                      : '—'}
                  </span>
                  {activeSnapshot.revisedDepartureTime && (
                    <span className="block text-[11px] text-rose-400 font-semibold mt-0.5">
                      Revised: {new Date(activeSnapshot.revisedDepartureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  )}
                </div>

                <div className="p-3.5 rounded-2xl bg-slate-950 border border-slate-800">
                  <span className="text-[10px] text-slate-500 uppercase tracking-wider font-bold block mb-1">
                    Estimated Arrival
                  </span>
                  <span className="text-base font-black text-white">
                    {activeSnapshot.revisedArrivalTime
                      ? new Date(activeSnapshot.revisedArrivalTime).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit',
                        })
                      : activeSnapshot.scheduledArrival
                      ? new Date(activeSnapshot.scheduledArrival).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit',
                        })
                      : '—'}
                  </span>
                  <span className="block text-[11px] text-emerald-400 font-medium mt-0.5">
                    Terminal {activeSnapshot.terminal || 'T3'} • Gate {activeSnapshot.gate || 'TBD'}
                  </span>
                </div>

                <div className="p-3.5 rounded-2xl bg-slate-950 border border-slate-800">
                  <span className="text-[10px] text-slate-500 uppercase tracking-wider font-bold block mb-1">
                    Baggage Reclaim
                  </span>
                  <span className="text-base font-black text-white">
                    {activeSnapshot.baggageCarousel || 'Belt 4'}
                  </span>
                  <span className="block text-[11px] text-slate-400 mt-0.5">Arrival Hall</span>
                </div>

                <div className="p-3.5 rounded-2xl bg-slate-950 border border-slate-800">
                  <span className="text-[10px] text-slate-500 uppercase tracking-wider font-bold block mb-1">
                    Data Feed Provenance
                  </span>
                  <span className="text-xs font-mono font-bold text-blue-400 flex items-center gap-1.5 mt-0.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-blue-400 animate-pulse" />
                    LIVE SIMULATION • SmartTravel Engine
                  </span>
                  <span className="block text-[10px] text-slate-500 mt-0.5">Airspace Radar & WebSocket Sync</span>
                </div>
              </div>
            </div>
          </div>
        ) : null}

        {/* 4. LIVE AIRSPACE TELEMETRY & FLIGHT RADAR FEED */}
        <div className="space-y-3 pt-2">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
              <h3 className="text-lg font-bold text-white tracking-tight">
                Live Airspace Fleet & Radar Feed
              </h3>
            </div>
            <span className="text-xs text-slate-400 font-mono">Click any flight to lock radar track</span>
          </div>

          <LiveAirspaceFeed
            onSelectFlight={handleSelectAirspaceFlight}
            selectedFlightNumber={activeSnapshot?.flightNumber || searchQuery}
          />
        </div>

        {/* 5. BROWSER PUSH NOTIFICATION CONTROLLER */}
        <div className="p-5 bg-slate-900 border border-slate-800 rounded-3xl flex flex-wrap items-center justify-between gap-4 shadow-xl">
          <div className="flex items-center gap-3.5">
            <div
              className={`w-12 h-12 rounded-2xl flex items-center justify-center ${
                pushSubscribed
                  ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  : 'bg-slate-800 text-slate-400'
              }`}
            >
              {pushSubscribed ? <BellRing className="w-6 h-6" /> : <Bell className="w-6 h-6" />}
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">Live Disruption & Gate Change Web Push</h4>
              <p className="text-xs text-slate-400 mt-0.5">
                {pushSubscribed
                  ? 'Active · Device receives instantaneous alerts for delays, boarding calls, and gate adjustments'
                  : 'Enable browser push notifications to receive updates even when this tab is closed'}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {pushSubscribed && (
              <button
                type="button"
                onClick={handleTestPush}
                className="px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300 rounded-xl transition-colors"
              >
                Send Test Alert
              </button>
            )}
            <button
              type="button"
              onClick={handleTogglePush}
              disabled={pushLoading}
              className={`px-5 py-2.5 rounded-xl text-xs font-bold transition shadow-md ${
                pushSubscribed
                  ? 'bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30'
                  : 'bg-sky-600 hover:bg-sky-500 text-white shadow-sky-600/20'
              }`}
            >
              {pushLoading
                ? 'Connecting...'
                : pushSubscribed
                ? 'Disable Push'
                : 'Enable Browser Alerts'}
            </button>
          </div>
        </div>

        {pushSuccessMsg && (
          <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-2xl text-xs font-medium flex items-center gap-2.5 animate-slide-up">
            <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
            <span>{pushSuccessMsg}</span>
          </div>
        )}

        {trackedError && (
          <div className="p-4 bg-rose-500/10 border border-rose-500/30 text-rose-400 rounded-2xl text-xs font-medium flex items-center gap-2.5">
            <AlertTriangle className="w-4 h-4 flex-shrink-0" />
            <span>{trackedError}</span>
          </div>
        )}

        {/* 5. MY SUBSCRIBED FLIGHTS (WEBSOCKET MULTI-FEED BOARD) */}
        <div className="space-y-4 pt-4 border-t border-slate-800">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-bold text-white">My Subscribed Live Flights</h2>
              <span className="text-xs font-mono font-bold text-sky-400 bg-sky-950/60 px-2.5 py-0.5 rounded-full border border-sky-800/40">
                {trackedFlights.length} Active
              </span>
            </div>
            {isAuthenticated && (
              <button
                onClick={fetchTracked}
                className="text-xs text-sky-400 hover:text-sky-300 flex items-center gap-1 font-semibold"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${loadingTracked ? 'animate-spin' : ''}`} />
                Sync Trackers
              </button>
            )}
          </div>

          {!isAuthenticated ? (
            <div className="p-8 text-center bg-slate-900/60 border border-slate-800 rounded-3xl space-y-3">
              <Sparkles className="w-8 h-8 text-sky-400 mx-auto" />
              <h3 className="font-bold text-white text-base">Sign In to Save Multiple Tracked Flights</h3>
              <p className="text-xs text-slate-400 max-w-md mx-auto">
                Create a free traveler account to maintain persistent flight trackers, configure automated SMS/email alerts, and receive live background Web Push notifications.
              </p>
              <div className="pt-2">
                <Link
                  to="/login"
                  className="px-6 py-2.5 bg-sky-600 hover:bg-sky-500 text-white text-xs font-bold rounded-xl inline-flex items-center gap-2 shadow-lg shadow-sky-600/20 transition"
                >
                  Sign In / Register
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          ) : loadingTracked ? (
            <div className="py-12 text-center text-slate-500">
              <div className="w-8 h-8 border-2 border-sky-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
              Updating your subscribed flight feeds...
            </div>
          ) : trackedFlights.length === 0 ? (
            <div className="py-12 text-center bg-slate-900 border border-slate-800 rounded-3xl p-6">
              <Plane className="w-8 h-8 text-slate-600 mx-auto mb-2" />
              <h3 className="text-sm font-bold text-white">No Flights Subscribed Yet</h3>
              <p className="text-xs text-slate-400 max-w-md mx-auto mt-1 mb-4">
                Use the search bar above or browse flight catalogs to subscribe to live status broadcasts.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {trackedFlights.map((tf) => (
                <div
                  key={tf.id}
                  className="bg-slate-900/80 border border-slate-800 hover:border-slate-750 rounded-3xl p-5 backdrop-blur-md flex flex-col justify-between transition-all shadow-xl"
                >
                  <FlightLiveStatusTracker
                    flightId={tf.flightId}
                    flightNumber={tf.flightNumber}
                    initialStatus={tf.currentStatus || tf.lastKnownStatus || 'SCHEDULED'}
                    initialDelayMinutes={tf.delayMinutes}
                    initialDelayReason={tf.delayReason}
                    scheduledDeparture={tf.scheduledDeparture}
                    revisedDeparture={tf.revisedDeparture}
                    scheduledArrival={tf.scheduledArrival}
                    estimatedArrival={tf.estimatedArrival || tf.lastKnownEta}
                    departureAirportCode={tf.departureAirportCode}
                    arrivalAirportCode={tf.arrivalAirportCode}
                  />

                  {/* Actions Footer */}
                  <div className="mt-4 pt-3 border-t border-slate-800 flex items-center justify-between">
                    <button
                      onClick={() => handleUntrack(tf.flightId)}
                      className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-rose-400 transition-colors p-1 font-medium"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      Remove from Tracker
                    </button>

                    <Link
                      to={`/book/${tf.flightId}`}
                      className="flex items-center gap-1 text-xs font-bold text-sky-400 hover:text-sky-300 transition-colors"
                    >
                      <span>Book Flight</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
