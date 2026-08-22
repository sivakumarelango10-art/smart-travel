import React, { useState } from 'react';
import { Plane, Radio, AlertTriangle, CheckCircle2, ShieldAlert } from 'lucide-react';
import { FlightStatus, FlightStatusEvent } from '../types/api';
import { useFlightStatusWebSocket } from '../hooks/useFlightStatusWebSocket';

interface FlightLiveStatusTrackerProps {
  flightId: string;
  flightNumber: string;
  initialStatus: FlightStatus;
  initialDelayMinutes?: number;
  initialDelayReason?: string;
  scheduledDeparture?: string;
  revisedDeparture?: string;
  scheduledArrival?: string;
  estimatedArrival?: string;
  departureAirportCode?: string;
  arrivalAirportCode?: string;
  dataSource?: 'LIVE' | 'CACHED' | 'SIMULATED' | string;
  gate?: string;
  terminal?: string;
}

export const FlightLiveStatusTracker: React.FC<FlightLiveStatusTrackerProps> = ({
  flightId,
  flightNumber,
  initialStatus,
  initialDelayMinutes,
  initialDelayReason,
  scheduledDeparture,
  revisedDeparture,
  scheduledArrival,
  estimatedArrival,
  departureAirportCode,
  arrivalAirportCode,
  dataSource = 'SIMULATED',
  gate,
  terminal,
}) => {
  const [currentStatus, setCurrentStatus] = useState<FlightStatus>(initialStatus);
  const [delayMinutes, setDelayMinutes] = useState<number | undefined>(initialDelayMinutes);
  const [delayReason, setDelayReason] = useState<string | undefined>(initialDelayReason);
  const [revisedDep, setRevisedDep] = useState<string | undefined>(revisedDeparture);
  const [estArr, setEstArr] = useState<string | undefined>(estimatedArrival);
  const [currentGate, setCurrentGate] = useState<string | undefined>(gate);
  const [currentTerminal, setCurrentTerminal] = useState<string | undefined>(terminal);
  const [currentSource, setCurrentSource] = useState<string>(dataSource);
  const [lastUpdated, setLastUpdated] = useState<Date>(new Date());

  const handleStatusUpdate = (event: FlightStatusEvent) => {
    setCurrentStatus(event.status);
    setDelayMinutes(event.delayMinutes);
    setDelayReason(event.delayReason);
    if (event.revisedDeparture) setRevisedDep(event.revisedDeparture);
    if (event.estimatedArrival) setEstArr(event.estimatedArrival);
    if (event.gate) setCurrentGate(event.gate);
    if (event.terminal) setCurrentTerminal(event.terminal);
    if (event.source) setCurrentSource(event.source);
    setLastUpdated(new Date(event.updatedAt || new Date()));
  };

  const { isConnected } = useFlightStatusWebSocket({
    flightId,
    onStatusUpdate: handleStatusUpdate,
    enabled: true,
  });

  const getStatusBadge = (status: FlightStatus) => {
    switch (status) {
      case 'ON_TIME':
      case 'SCHEDULED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-medium rounded-md">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
            ON TIME
          </span>
        );
      case 'BOARDING':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs font-medium rounded-md">
            <span className="w-1.5 h-1.5 rounded-full bg-amber-400" />
            BOARDING
          </span>
        );
      case 'DEPARTED':
      case 'IN_AIR':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-blue-500/10 border border-blue-500/20 text-blue-400 text-xs font-medium rounded-md">
            <Plane className="w-3.5 h-3.5 text-blue-400" />
            IN FLIGHT
          </span>
        );
      case 'ARRIVED':
      case 'LANDED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-slate-800 border border-slate-700 text-slate-300 text-xs font-medium rounded-md">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
            ARRIVED
          </span>
        );
      case 'DELAYED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-medium rounded-md">
            <AlertTriangle className="w-3.5 h-3.5" />
            DELAYED {delayMinutes ? `(+${delayMinutes}m)` : ''}
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs font-medium rounded-md">
            <ShieldAlert className="w-3.5 h-3.5" />
            CANCELLED
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-slate-800 text-slate-300 text-xs font-medium rounded-md">
            {status}
          </span>
        );
    }
  };

  return (
    <div className="p-4 bg-slate-900 border border-slate-800 rounded-xl">
      <div className="flex items-center justify-between gap-2 pb-3 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <div className="p-1.5 bg-slate-800 rounded-md text-blue-400">
            <Radio className={`w-4 h-4 ${isConnected ? 'text-emerald-400' : 'text-slate-500'}`} />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold text-white">{flightNumber}</span>
              {departureAirportCode && arrivalAirportCode && (
                <span className="text-[11px] text-slate-400">
                  ({departureAirportCode} → {arrivalAirportCode})
                </span>
              )}
            </div>
            <div className="flex items-center gap-2 mt-0.5">
              <span className="text-[10px] text-slate-500 flex items-center gap-1">
                {isConnected ? (
                  <span className="text-emerald-400 font-medium">Live sync active</span>
                ) : (
                  <span>Reconnecting...</span>
                )}
              </span>

              {/* Data Provenance Badge */}
              <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20 text-[9px] font-medium">
                <span className="w-1.5 h-1.5 rounded-full bg-blue-400 animate-pulse" />
                LIVE SIMULATION • SmartTravel Engine
              </span>
            </div>
          </div>
        </div>

        <div>{getStatusBadge(currentStatus)}</div>
      </div>

      {/* Delay Banner if delayed */}
      {currentStatus === 'DELAYED' && delayReason && (
        <div className="mt-3 p-2.5 bg-rose-500/10 border border-rose-500/20 rounded-xl text-xs text-rose-300 flex items-start gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0 mt-0.5 text-rose-400" />
          <div>
            <div className="font-semibold">Flight Delay Advisory</div>
            <p className="text-[11px] text-rose-400/90 mt-0.5">{delayReason}</p>
          </div>
        </div>
      )}

      {/* Flight Timing & Gate Details */}
      <div className="mt-3 grid grid-cols-2 gap-3 text-xs">
        <div className="p-2.5 bg-slate-800/40 rounded-xl border border-slate-800">
          <span className="text-[11px] text-slate-400 block mb-0.5">Departure</span>
          <span className="font-semibold text-white">
            {scheduledDeparture
              ? new Date(scheduledDeparture).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              : '—'}
          </span>
          {revisedDep && currentStatus === 'DELAYED' && (
            <span className="block text-[11px] text-rose-400 font-medium mt-0.5">
              Revised: {new Date(revisedDep).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
          )}
          {currentTerminal && (
            <span className="block text-[10px] text-slate-400 mt-1 font-mono">
              Terminal: {currentTerminal}
            </span>
          )}
        </div>

        <div className="p-2.5 bg-slate-800/40 rounded-xl border border-slate-800">
          <span className="text-[11px] text-slate-400 block mb-0.5">Arrival (Est.)</span>
          <span className="font-semibold text-white">
            {estArr
              ? new Date(estArr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              : scheduledArrival
              ? new Date(scheduledArrival).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              : '—'}
          </span>
          {estArr && currentStatus === 'DELAYED' && (
            <span className="block text-[11px] text-rose-400 font-medium mt-0.5">
              Est: {new Date(estArr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
          )}
          {currentGate && (
            <span className="block text-[10px] text-slate-400 mt-1 font-mono">
              Gate: {currentGate}
            </span>
          )}
        </div>
      </div>

      <div className="mt-2.5 flex items-center justify-between text-[10px] text-slate-500">
        <span>Source: {currentSource}</span>
        <span>Last sync: {lastUpdated.toLocaleTimeString()}</span>
      </div>
    </div>
  );
};
