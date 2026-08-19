import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import {
  Plane, ArrowLeft, Edit, AlertTriangle, Clock, RefreshCw,
  Zap, ChevronDown, ChevronUp, Activity, Package, MapPin, Wrench
} from 'lucide-react';
import { adminFlightService } from '../../services/adminFlightService';
import { StatusBadge } from '../../components/admin/StatusBadge';
import { useAdminToast } from '../../components/admin/AdminToast';
import { Flight } from '../../types/flight';
import { FlightDisruption, FlightImpactSummary } from '../../types/admin';

type ActionType = 'cancel' | 'reschedule' | 'gate' | 'terminal' | 'aircraft' | null;

export const AdminFlightDetailPage: React.FC = () => {
  const { flightId } = useParams<{ flightId: string }>();
  const navigate = useNavigate();
  const { showToast } = useAdminToast();

  const [flight, setFlight] = useState<Flight | null>(null);
  const [disruptions, setDisruptions] = useState<FlightDisruption[]>([]);
  const [impact, setImpact] = useState<FlightImpactSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [activeAction, setActiveAction] = useState<ActionType>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Form state for actions
  const [cancelForm, setCancelForm] = useState({ reason: '', description: '' });
  const [gateForm, setGateForm] = useState({ gate: '', reason: '' });
  const [terminalForm, setTerminalForm] = useState({ terminal: '', reason: '' });
  const [aircraftForm, setAircraftForm] = useState({ aircraftModel: '', reason: '' });
  const [scheduleForm, setScheduleForm] = useState({ newDepartureTime: '', newArrivalTime: '', reason: '', description: '' });

  const [showDisruptions, setShowDisruptions] = useState(false);
  const [resolvingId, setResolvingId] = useState<string | null>(null);

  const fetchData = async () => {
    if (!flightId) return;
    setLoading(true);
    setError(null);
    try {
      const [flightRes, disruptionRes] = await Promise.all([
        adminFlightService.getFlightById(flightId),
        adminFlightService.getFlightDisruptions(flightId, 0, 10),
      ]);
      setFlight(flightRes.data);
      setDisruptions(disruptionRes.data?.content ?? []);

      // Try impact (may fail if no bookings)
      try {
        const impactRes = await adminFlightService.getDisruptionImpact(flightId);
        setImpact(impactRes.data);
      } catch { /* no impact data available */ }
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Failed to load flight details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, [flightId]);

  const handleCancelFlight = async () => {
    if (!flightId) return;
    setActionLoading(true);
    try {
      await adminFlightService.cancelFlight(flightId, cancelForm);
      showToast('success', 'Flight cancelled', 'Flight has been cancelled and workflows triggered.');
      setActiveAction(null);
      fetchData();
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Cancel failed', err?.message ?? 'Could not cancel flight');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReschedule = async () => {
    if (!flightId) return;
    setActionLoading(true);
    try {
      await adminFlightService.rescheduleFlight(flightId, {
        newDepartureTime: new Date(scheduleForm.newDepartureTime).toISOString(),
        newArrivalTime: new Date(scheduleForm.newArrivalTime).toISOString(),
        reason: scheduleForm.reason,
        description: scheduleForm.description,
      });
      showToast('success', 'Flight rescheduled', 'Schedule updated successfully.');
      setActiveAction(null);
      fetchData();
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Reschedule failed', err?.message ?? 'Could not reschedule');
    } finally {
      setActionLoading(false);
    }
  };

  const handleGateChange = async () => {
    if (!flightId) return;
    setActionLoading(true);
    try {
      await adminFlightService.updateGate(flightId, gateForm);
      showToast('success', 'Gate updated', `Gate changed to ${gateForm.gate}`);
      setActiveAction(null);
      fetchData();
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Gate update failed', err?.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleTerminalChange = async () => {
    if (!flightId) return;
    setActionLoading(true);
    try {
      await adminFlightService.updateTerminal(flightId, terminalForm);
      showToast('success', 'Terminal updated', `Terminal changed to ${terminalForm.terminal}`);
      setActiveAction(null);
      fetchData();
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Terminal update failed', err?.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleAircraftChange = async () => {
    if (!flightId) return;
    setActionLoading(true);
    try {
      await adminFlightService.changeAircraft(flightId, aircraftForm);
      showToast('success', 'Aircraft updated', `Aircraft changed to ${aircraftForm.aircraftModel}`);
      setActiveAction(null);
      fetchData();
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Aircraft update failed', err?.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleResolveDisruption = async (disruptionId: string) => {
    setResolvingId(disruptionId);
    try {
      await adminFlightService.resolveDisruption(disruptionId);
      showToast('success', 'Disruption resolved', 'Disruption marked as resolved.');
      fetchData();
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Failed to resolve', err?.message);
    } finally {
      setResolvingId(null);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="h-8 w-64 bg-slate-800 rounded animate-pulse" />
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4 animate-pulse">
          {Array.from({ length: 6 }).map((_, i) => <div key={i} className="h-4 bg-slate-800 rounded w-full" />)}
        </div>
      </div>
    );
  }

  if (error || !flight) {
    return (
      <div className="p-8 text-center">
        <AlertTriangle className="w-12 h-12 text-rose-400 mx-auto mb-3" />
        <p className="text-white font-semibold">{error ?? 'Flight not found'}</p>
        <button onClick={() => navigate('/admin/flights')} className="mt-4 text-sky-400 hover:text-sky-300 text-sm">
          ← Back to flights
        </button>
      </div>
    );
  }

  const inlineFormInput = (label: string, value: string, onChange: (v: string) => void, type = 'text', placeholder = '') => (
    <div>
      <label className="block text-xs font-medium text-slate-400 mb-1">{label}</label>
      <input
        type={type}
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder={placeholder}
        className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30"
      />
    </div>
  );

  return (
    <div className="space-y-6 max-w-5xl">
      {/* Header */}
      <div className="flex items-center gap-3">
        <Link to="/admin/flights" className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div className="flex-1">
          <div className="flex items-center gap-3 flex-wrap">
            <h1 className="text-xl font-bold text-white font-mono">{flight.flightNumber}</h1>
            <StatusBadge status={flight.status} type="flight" />
            {!flight.active && (
              <span className="text-xs px-2 py-0.5 rounded-full bg-slate-700 text-slate-400 border border-slate-600">INACTIVE</span>
            )}
          </div>
          <p className="text-sm text-slate-400">{flight.airline} · {flight.aircraftModel}</p>
        </div>
        <Link
          to={`/admin/flights/${flight.id}/edit`}
          className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl transition"
        >
          <Edit className="w-4 h-4" /> Edit
        </Link>
        <button
          onClick={fetchData}
          className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition border border-slate-700"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Flight Info */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
          <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wide flex items-center gap-2">
            <Plane className="w-4 h-4 text-sky-400" /> Schedule
          </h2>
          <div className="flex items-center gap-4">
            <div className="text-center">
              <p className="text-xl font-bold text-white">{flight.departureAirport?.code}</p>
              <p className="text-xs text-slate-400">{flight.departureAirport?.city}</p>
              <p className="text-xs font-mono text-slate-300 mt-1">
                {new Date(flight.departureTime).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: false })}
              </p>
              <p className="text-[10px] text-slate-500">
                {new Date(flight.departureTime).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })}
              </p>
            </div>
            <div className="flex-1 flex flex-col items-center">
              <div className="w-full border-t border-dashed border-slate-700" />
              <p className="text-[11px] text-slate-500 mt-1">
                {flight.durationMinutes ? `${Math.floor(flight.durationMinutes/60)}h ${flight.durationMinutes%60}m` : '--'}
              </p>
            </div>
            <div className="text-center">
              <p className="text-xl font-bold text-white">{flight.arrivalAirport?.code}</p>
              <p className="text-xs text-slate-400">{flight.arrivalAirport?.city}</p>
              <p className="text-xs font-mono text-slate-300 mt-1">
                {new Date(flight.arrivalTime).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: false })}
              </p>
              <p className="text-[10px] text-slate-500">
                {new Date(flight.arrivalTime).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })}
              </p>
            </div>
          </div>
          {flight.delayMinutes && flight.delayMinutes > 0 && (
            <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-xs text-amber-400">
              <Clock className="w-3.5 h-3.5 inline mr-1" />
              Delayed by {flight.delayMinutes} minutes: {flight.delayReason}
            </div>
          )}
        </div>

        {/* Impact Summary */}
        {impact && (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
            <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wide flex items-center gap-2 mb-4">
              <Activity className="w-4 h-4 text-violet-400" /> Passenger Impact
            </h2>
            <div className="grid grid-cols-3 gap-4">
              <div>
                <p className="text-2xl font-bold text-white">{impact.affectedBookings}</p>
                <p className="text-xs text-slate-400">Bookings</p>
              </div>
              <div>
                <p className="text-2xl font-bold text-white">{impact.affectedPassengers}</p>
                <p className="text-xs text-slate-400">Passengers</p>
              </div>
              <div>
                <p className="text-2xl font-bold text-white">{impact.checkedInPassengers}</p>
                <p className="text-xs text-slate-400">Checked In</p>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Cabin Inventory */}
      {flight.cabinInventories && flight.cabinInventories.length > 0 && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-slate-800">
            <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wide flex items-center gap-2">
              <Package className="w-4 h-4 text-teal-400" /> Cabin Inventory
            </h2>
            <Link
              to={`/admin/flights/${flight.id}/seats`}
              className="text-xs text-sky-400 hover:text-sky-300 font-medium"
            >
              View Seat Map →
            </Link>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-slate-950/40">
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-400 uppercase">Cabin</th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-slate-400 uppercase">Total</th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-slate-400 uppercase">Available</th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-slate-400 uppercase">Utilization</th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-slate-400 uppercase">Base Price</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {flight.cabinInventories.map(cabin => {
                  const utilized = cabin.totalSeats > 0
                    ? Math.round(((cabin.totalSeats - cabin.availableSeats) / cabin.totalSeats) * 100)
                    : 0;
                  return (
                    <tr key={cabin.cabinClass} className="hover:bg-slate-800/20">
                      <td className="px-5 py-3">
                        <span className="text-sm font-semibold text-white">{cabin.cabinClass.replace(/_/g, ' ')}</span>
                      </td>
                      <td className="px-4 py-3 text-right text-slate-300">{cabin.totalSeats}</td>
                      <td className="px-4 py-3 text-right">
                        <span className={cabin.availableSeats < 10 ? 'text-rose-400 font-semibold' : 'text-emerald-400'}>
                          {cabin.availableSeats}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <div className="w-16 h-1.5 bg-slate-800 rounded-full overflow-hidden">
                            <div
                              className={`h-full rounded-full transition-all ${utilized >= 90 ? 'bg-rose-500' : utilized >= 70 ? 'bg-amber-500' : 'bg-emerald-500'}`}
                              style={{ width: `${utilized}%` }}
                            />
                          </div>
                          <span className="text-xs text-slate-400">{utilized}%</span>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-right text-slate-300">₹{Number(cabin.basePrice).toLocaleString('en-IN')}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Operational Actions */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
        <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wide flex items-center gap-2 mb-5">
          <Wrench className="w-4 h-4 text-orange-400" /> Operational Controls
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3">
          {[
            { label: 'Reschedule', action: 'reschedule' as ActionType, icon: <Clock className="w-4 h-4" />, color: 'hover:border-sky-500/40 hover:text-sky-400' },
            { label: 'Cancel Flight', action: 'cancel' as ActionType, icon: <AlertTriangle className="w-4 h-4" />, color: 'hover:border-rose-500/40 hover:text-rose-400' },
            { label: 'Gate Change', action: 'gate' as ActionType, icon: <MapPin className="w-4 h-4" />, color: 'hover:border-amber-500/40 hover:text-amber-400' },
            { label: 'Terminal', action: 'terminal' as ActionType, icon: <Zap className="w-4 h-4" />, color: 'hover:border-violet-500/40 hover:text-violet-400' },
            { label: 'Aircraft Swap', action: 'aircraft' as ActionType, icon: <Plane className="w-4 h-4" />, color: 'hover:border-teal-500/40 hover:text-teal-400' },
          ].map(btn => (
            <button
              key={btn.action}
              onClick={() => setActiveAction(activeAction === btn.action ? null : btn.action)}
              className={`flex flex-col items-center gap-2 p-4 bg-slate-800/60 border border-slate-700 rounded-xl text-sm text-slate-400 transition ${btn.color} ${activeAction === btn.action ? 'border-sky-500/50 text-sky-400 bg-sky-500/5' : ''}`}
            >
              {btn.icon}
              <span className="text-xs font-medium">{btn.label}</span>
            </button>
          ))}
        </div>

        {/* Inline action forms */}
        {activeAction === 'cancel' && (
          <div className="mt-4 p-4 bg-rose-500/5 border border-rose-500/20 rounded-xl space-y-3">
            <p className="text-sm font-semibold text-rose-400">Cancel Flight — This will trigger automated refunds and notifications</p>
            {inlineFormInput('Reason *', cancelForm.reason, v => setCancelForm(f => ({...f, reason: v})), 'text', 'e.g. Weather conditions')}
            {inlineFormInput('Description', cancelForm.description, v => setCancelForm(f => ({...f, description: v})), 'text', 'Additional details...')}
            <div className="flex gap-3 pt-1">
              <button onClick={() => setActiveAction(null)} className="px-4 py-2 text-sm text-slate-400 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-lg transition">Cancel</button>
              <button
                onClick={handleCancelFlight}
                disabled={!cancelForm.reason || actionLoading}
                className="px-4 py-2 text-sm text-white bg-rose-600 hover:bg-rose-500 rounded-lg transition disabled:opacity-50 flex items-center gap-2"
              >
                {actionLoading && <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
                Cancel Flight
              </button>
            </div>
          </div>
        )}

        {activeAction === 'reschedule' && (
          <div className="mt-4 p-4 bg-sky-500/5 border border-sky-500/20 rounded-xl space-y-3">
            <p className="text-sm font-semibold text-sky-400">Reschedule Flight</p>
            <div className="grid grid-cols-2 gap-3">
              {inlineFormInput('New Departure *', scheduleForm.newDepartureTime, v => setScheduleForm(f => ({...f, newDepartureTime: v})), 'datetime-local')}
              {inlineFormInput('New Arrival *', scheduleForm.newArrivalTime, v => setScheduleForm(f => ({...f, newArrivalTime: v})), 'datetime-local')}
            </div>
            {inlineFormInput('Reason *', scheduleForm.reason, v => setScheduleForm(f => ({...f, reason: v})), 'text', 'e.g. ATC delay')}
            <div className="flex gap-3 pt-1">
              <button onClick={() => setActiveAction(null)} className="px-4 py-2 text-sm text-slate-400 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-lg transition">Cancel</button>
              <button
                onClick={handleReschedule}
                disabled={!scheduleForm.newDepartureTime || !scheduleForm.newArrivalTime || !scheduleForm.reason || actionLoading}
                className="px-4 py-2 text-sm text-white bg-sky-600 hover:bg-sky-500 rounded-lg transition disabled:opacity-50 flex items-center gap-2"
              >
                {actionLoading && <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
                Reschedule
              </button>
            </div>
          </div>
        )}

        {activeAction === 'gate' && (
          <div className="mt-4 p-4 bg-amber-500/5 border border-amber-500/20 rounded-xl space-y-3">
            <p className="text-sm font-semibold text-amber-400">Update Gate</p>
            {inlineFormInput('New Gate *', gateForm.gate, v => setGateForm(f => ({...f, gate: v})), 'text', 'e.g. 14B')}
            {inlineFormInput('Reason', gateForm.reason, v => setGateForm(f => ({...f, reason: v})), 'text', 'Optional reason')}
            <div className="flex gap-3 pt-1">
              <button onClick={() => setActiveAction(null)} className="px-4 py-2 text-sm text-slate-400 hover:text-white bg-slate-800 rounded-lg transition">Cancel</button>
              <button onClick={handleGateChange} disabled={!gateForm.gate || actionLoading} className="px-4 py-2 text-sm text-white bg-amber-600 hover:bg-amber-500 rounded-lg transition disabled:opacity-50 flex items-center gap-2">
                {actionLoading && <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
                Update Gate
              </button>
            </div>
          </div>
        )}

        {activeAction === 'terminal' && (
          <div className="mt-4 p-4 bg-violet-500/5 border border-violet-500/20 rounded-xl space-y-3">
            <p className="text-sm font-semibold text-violet-400">Update Terminal</p>
            {inlineFormInput('New Terminal *', terminalForm.terminal, v => setTerminalForm(f => ({...f, terminal: v})), 'text', 'e.g. T2')}
            {inlineFormInput('Reason', terminalForm.reason, v => setTerminalForm(f => ({...f, reason: v})), 'text', 'Optional reason')}
            <div className="flex gap-3 pt-1">
              <button onClick={() => setActiveAction(null)} className="px-4 py-2 text-sm text-slate-400 hover:text-white bg-slate-800 rounded-lg transition">Cancel</button>
              <button onClick={handleTerminalChange} disabled={!terminalForm.terminal || actionLoading} className="px-4 py-2 text-sm text-white bg-violet-600 hover:bg-violet-500 rounded-lg transition disabled:opacity-50 flex items-center gap-2">
                {actionLoading && <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
                Update Terminal
              </button>
            </div>
          </div>
        )}

        {activeAction === 'aircraft' && (
          <div className="mt-4 p-4 bg-teal-500/5 border border-teal-500/20 rounded-xl space-y-3">
            <p className="text-sm font-semibold text-teal-400">Swap Aircraft</p>
            {inlineFormInput('New Aircraft Model *', aircraftForm.aircraftModel, v => setAircraftForm(f => ({...f, aircraftModel: v})), 'text', 'e.g. Airbus A321neo')}
            {inlineFormInput('Reason', aircraftForm.reason, v => setAircraftForm(f => ({...f, reason: v})), 'text', 'Optional reason')}
            <div className="flex gap-3 pt-1">
              <button onClick={() => setActiveAction(null)} className="px-4 py-2 text-sm text-slate-400 hover:text-white bg-slate-800 rounded-lg transition">Cancel</button>
              <button onClick={handleAircraftChange} disabled={!aircraftForm.aircraftModel || actionLoading} className="px-4 py-2 text-sm text-white bg-teal-600 hover:bg-teal-500 rounded-lg transition disabled:opacity-50 flex items-center gap-2">
                {actionLoading && <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
                Swap Aircraft
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Disruption History */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <button
          onClick={() => setShowDisruptions(!showDisruptions)}
          className="w-full flex items-center justify-between px-5 py-4 hover:bg-slate-800/40 transition"
        >
          <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wide flex items-center gap-2">
            <Zap className="w-4 h-4 text-amber-400" /> Disruption History ({disruptions.length})
          </h2>
          {showDisruptions ? <ChevronUp className="w-4 h-4 text-slate-400" /> : <ChevronDown className="w-4 h-4 text-slate-400" />}
        </button>
        {showDisruptions && (
          <div className="divide-y divide-slate-800/60">
            {disruptions.length === 0 ? (
              <p className="px-5 py-6 text-center text-slate-500 text-sm">No disruptions recorded</p>
            ) : (
              disruptions.map(d => (
                <div key={d.id} className="px-5 py-4 flex items-start justify-between gap-4">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-sm font-semibold text-white">{d.disruptionType.replace(/_/g, ' ')}</span>
                      <StatusBadge status={d.status} type="disruption" size="xs" />
                    </div>
                    <p className="text-xs text-slate-400">{d.reason}</p>
                    <p className="text-[11px] text-slate-500">by {d.createdBy} · {new Date(d.createdAt).toLocaleString()}</p>
                  </div>
                  {d.status === 'ACTIVE' && (
                    <button
                      onClick={() => handleResolveDisruption(d.id)}
                      disabled={resolvingId === d.id}
                      className="flex-shrink-0 px-3 py-1.5 text-xs text-emerald-400 border border-emerald-500/30 hover:bg-emerald-500/10 rounded-lg transition disabled:opacity-50"
                    >
                      {resolvingId === d.id ? 'Resolving...' : 'Resolve'}
                    </button>
                  )}
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
};
