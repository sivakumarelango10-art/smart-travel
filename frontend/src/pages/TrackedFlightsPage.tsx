import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Plane, Radio, Trash2, ArrowRight, RefreshCw, AlertTriangle, Search, Bell, BellRing, CheckCircle2 } from 'lucide-react';
import { TrackedFlight } from '../types/api';
import { flightTrackingService } from '../services/flightTrackingService';
import { pushNotificationService } from '../services/pushNotificationService';
import { FlightLiveStatusTracker } from '../components/FlightLiveStatusTracker';

export const TrackedFlightsPage: React.FC = () => {
  const [trackedFlights, setTrackedFlights] = useState<TrackedFlight[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pushSubscribed, setPushSubscribed] = useState(false);
  const [pushLoading, setPushLoading] = useState(false);
  const [pushSuccessMsg, setPushSuccessMsg] = useState<string | null>(null);

  const fetchTracked = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await flightTrackingService.getTrackedFlights();
      setTrackedFlights(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load tracked flights');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTracked();
    checkPushStatus();
  }, []);

  const checkPushStatus = async () => {
    const subscribed = await pushNotificationService.isSubscribed();
    setPushSubscribed(subscribed);
  };

  const handleTogglePush = async () => {
    setPushLoading(true);
    setError(null);
    try {
      if (pushSubscribed) {
        await pushNotificationService.unsubscribe();
        setPushSubscribed(false);
        setPushSuccessMsg('Browser push notifications disabled.');
      } else {
        const success = await pushNotificationService.subscribe();
        if (success) {
          setPushSubscribed(true);
          setPushSuccessMsg('Browser push notifications enabled! You will receive background disruption alerts.');
        } else {
          setError('Notification permission was denied. Please allow notifications in your browser settings.');
        }
      }
    } catch (err: any) {
      setError(err.message || 'Failed to update push subscription');
    } finally {
      setPushLoading(false);
      setTimeout(() => setPushSuccessMsg(null), 5000);
    }
  };

  const handleTestPush = async () => {
    try {
      await pushNotificationService.sendTestPush();
      setPushSuccessMsg('Test notification dispatched to your browser!');
      setTimeout(() => setPushSuccessMsg(null), 5000);
    } catch (err: any) {
      setError(err.message || 'Failed to send test push notification');
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

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="flex flex-wrap items-center justify-between gap-4 pb-8 border-b border-slate-800">
          <div>
            <div className="flex items-center gap-2 text-cyan-400 text-xs font-semibold uppercase tracking-wider mb-1">
              <Radio className="w-4 h-4 animate-pulse" />
              <span>Real-Time Flight Tracker</span>
            </div>
            <h1 className="text-3xl font-extrabold text-white">Live Tracked Flights</h1>
            <p className="text-sm text-slate-400 mt-1">
              Receive instant WebSocket & Web Push updates on departures, arrivals, delays, and schedule changes.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={fetchTracked}
              disabled={loading}
              className="flex items-center gap-2 px-4 py-2 bg-slate-900 border border-slate-800 hover:bg-slate-800 text-sm font-medium text-slate-300 rounded-xl transition-colors disabled:opacity-50"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
            <Link
              to="/flights"
              className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white text-sm font-semibold rounded-xl transition-all shadow-lg shadow-cyan-500/20"
            >
              <Search className="w-4 h-4" />
              Find More Flights
            </Link>
          </div>
        </div>

        {/* Browser Web Push Notification Banner */}
        <div className="mt-6 p-4 bg-slate-900/80 border border-slate-800 rounded-2xl flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${pushSubscribed ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-slate-800 text-slate-400'}`}>
              {pushSubscribed ? <BellRing className="w-5 h-5" /> : <Bell className="w-5 h-5" />}
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">Browser Push Notifications</h4>
              <p className="text-xs text-slate-400">
                {pushSubscribed
                  ? 'Active · You will receive alerts even when the browser tab is closed'
                  : 'Enable Web Push notifications to receive live delay & boarding alerts on your device'}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {pushSubscribed && (
              <button
                onClick={handleTestPush}
                className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-300 rounded-lg transition-colors"
              >
                Send Test Alert
              </button>
            )}
            <button
              onClick={handleTogglePush}
              disabled={pushLoading}
              className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all shadow-md ${
                pushSubscribed
                  ? 'bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30'
                  : 'bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white'
              }`}
            >
              {pushLoading
                ? 'Processing...'
                : pushSubscribed
                ? 'Disable Push'
                : 'Enable Browser Push'}
            </button>
          </div>
        </div>

        {pushSuccessMsg && (
          <div className="mt-4 p-3.5 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-xl text-xs flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
            {pushSuccessMsg}
          </div>
        )}

        {error && (
          <div className="mt-4 p-4 bg-rose-500/10 border border-rose-500/30 text-rose-400 rounded-2xl text-sm flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 flex-shrink-0" />
            {error}
          </div>
        )}

        {/* Content */}
        <div className="mt-8 space-y-6">
          {loading ? (
            <div className="py-20 text-center text-slate-500">
              <div className="w-8 h-8 border-2 border-cyan-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
              Loading your live flight trackers...
            </div>
          ) : trackedFlights.length === 0 ? (
            <div className="py-16 text-center bg-slate-900/40 border border-slate-800/80 rounded-2xl p-8">
              <div className="w-12 h-12 bg-slate-800 rounded-2xl flex items-center justify-center mx-auto text-slate-500 mb-3">
                <Plane className="w-6 h-6" />
              </div>
              <h3 className="text-base font-bold text-white">No Flights Currently Tracked</h3>
              <p className="text-xs text-slate-400 max-w-md mx-auto mt-1 mb-6">
                You haven't tracked any flights yet. Browse available flights and click "Track Flight" to receive live real-time status alerts.
              </p>
              <Link
                to="/flights"
                className="inline-flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-cyan-500 to-blue-600 text-white text-sm font-semibold rounded-xl shadow-lg shadow-cyan-500/20"
              >
                Search Flights
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {trackedFlights.map((tf) => (
                <div
                  key={tf.id}
                  className="bg-slate-900/60 border border-slate-800 hover:border-slate-700 rounded-2xl p-5 backdrop-blur-md flex flex-col justify-between transition-all"
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
                      className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-rose-400 transition-colors p-1"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      Stop Tracking
                    </button>

                    <Link
                      to={`/book/${tf.flightId}`}
                      className="flex items-center gap-1 text-xs font-semibold text-cyan-400 hover:text-cyan-300 transition-colors"
                    >
                      Book This Flight
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
