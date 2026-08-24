import React, { useState } from 'react';
import { Plane, Radio, AlertTriangle, CheckCircle2, ShieldAlert } from 'lucide-react';
import { FlightStatus, FlightStatusEvent } from '../types/api';
import { useFlightStatusWebSocket } from '../hooks/useFlightStatusWebSocket';

interface FlightLiveStatusTrackerProps {
  flightId: string;
  initialSnapshot?: any;
  flightNumber?: string;
  initialStatus?: FlightStatus;
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
  initialSnapshot,
  flightNumber = initialSnapshot?.flightNumber || 'FLIGHT',
  initialStatus = initialSnapshot?.status || 'SCHEDULED',
  initialDelayMinutes = initialSnapshot?.delayMinutes,
  initialDelayReason = initialSnapshot?.delayReason,
  scheduledDeparture = initialSnapshot?.scheduledDeparture,
  revisedDeparture = initialSnapshot?.revisedDeparture,
  scheduledArrival = initialSnapshot?.scheduledArrival,
  estimatedArrival = initialSnapshot?.estimatedArrival,
  departureAirportCode = initialSnapshot?.departureAirportCode || initialSnapshot?.departureAirport?.code,
  arrivalAirportCode = initialSnapshot?.arrivalAirportCode || initialSnapshot?.arrivalAirport?.code,
  dataSource = initialSnapshot?.source || 'SIMULATED',
  gate = initialSnapshot?.gate,
  terminal = initialSnapshot?.terminal,
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
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-bold rounded-md">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
            ON TIME
          </span>
        );
      case 'BOARDING':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-amber-50 border border-amber-200 text-amber-700 text-xs font-bold rounded-md">
            <span className="w-1.5 h-1.5 rounded-full bg-amber-500" />
            BOARDING
          </span>
        );
      case 'DEPARTED':
      case 'IN_AIR':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-secondary/10 border border-secondary/20 text-secondary text-xs font-bold rounded-md">
            <Plane className="w-3.5 h-3.5" />
            IN FLIGHT
          </span>
        );
      case 'ARRIVED':
      case 'LANDED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-slate-100 border border-slate-200 text-slate-700 text-xs font-bold rounded-md">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
            ARRIVED
          </span>
        );
      case 'DELAYED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-rose-50 border border-rose-200 text-rose-700 text-xs font-bold rounded-md">
            <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />
            DELAYED {delayMinutes ? `(+${delayMinutes}m)` : ''}
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-rose-50 border border-rose-200 text-rose-700 text-xs font-bold rounded-md">
            <ShieldAlert className="w-3.5 h-3.5 text-rose-600" />
            CANCELLED
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-slate-100 text-slate-700 text-xs font-bold rounded-md">
            {status}
          </span>
        );
    }
  };

  return (
    <div className="p-5 bg-white border border-slate-200 rounded-2xl shadow-sm space-y-4">
      <div className="flex items-center justify-between gap-2 pb-3 border-b border-slate-100">
        <div className="flex items-center gap-2.5">
          <div className="p-2 bg-secondary/10 text-secondary rounded-lg">
            <Radio className={`w-4 h-4 ${isConnected ? 'text-emerald-600' : 'text-slate-400'}`} />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-sm font-black text-primary">{flightNumber}</span>
              {departureAirportCode && arrivalAirportCode && (
                <span className="text-xs font-semibold text-slate-500">
                  ({departureAirportCode} → {arrivalAirportCode})
                </span>
              )}
            </div>
            <div className="flex items-center gap-2 mt-0.5">
              <span className="text-[11px] text-slate-500 flex items-center gap-1">
                {isConnected ? (
                  <span className="text-emerald-600 font-semibold">Live telemetry active</span>
                ) : (
                  <span>Reconnecting...</span>
                )}
              </span>

              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-secondary/10 text-secondary text-[10px] font-bold">
                <span className="w-1.5 h-1.5 rounded-full bg-secondary animate-pulse" />
                LIVE SIMULATION • SmartTravel Radar
              </span>
            </div>
          </div>
        </div>

        <div>{getStatusBadge(currentStatus)}</div>
      </div>

      {/* Delay Banner */}
      {currentStatus === 'DELAYED' && delayReason && (
        <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-700 flex items-start gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0 mt-0.5 text-rose-500" />
          <div>
            <div className="font-bold">Flight Delay Advisory</div>
            <p className="text-[11px] text-rose-600 mt-0.5">{delayReason}</p>
          </div>
        </div>
      )}

      {/* Timing & Gate Details */}
      <div className="grid grid-cols-2 gap-3 text-xs">
        <div className="p-3 bg-slate-50 rounded-xl border border-slate-200">
          <span className="text-[11px] text-slate-500 block mb-0.5">Scheduled Departure</span>
          <span className="font-bold text-primary text-sm">
            {scheduledDeparture
              ? new Date(scheduledDeparture).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              : '—'}
          </span>
          {revisedDep && currentStatus === 'DELAYED' && (
            <span className="block text-[11px] text-rose-600 font-bold mt-0.5">
              Revised: {new Date(revisedDep).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
          )}
          {currentTerminal && (
            <span className="block text-[10px] text-slate-500 mt-1 font-mono">
              Terminal: <strong>{currentTerminal}</strong>
            </span>
          )}
        </div>

        <div className="p-3 bg-slate-50 rounded-xl border border-slate-200">
          <span className="text-[11px] text-slate-500 block mb-0.5">Arrival (Estimated)</span>
          <span className="font-bold text-primary text-sm">
            {estArr
              ? new Date(estArr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              : scheduledArrival
              ? new Date(scheduledArrival).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              : '—'}
          </span>
          {estArr && currentStatus === 'DELAYED' && (
            <span className="block text-[11px] text-rose-600 font-bold mt-0.5">
              Est: {new Date(estArr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
          )}
          {currentGate && (
            <span className="block text-[10px] text-slate-500 mt-1 font-mono">
              Gate: <strong>{currentGate}</strong>
            </span>
          )}
        </div>
      </div>

      <div className="flex items-center justify-between text-[10px] text-slate-400 pt-1">
        <span>Source: {currentSource}</span>
        <span>Last sync: {lastUpdated.toLocaleTimeString()}</span>
      </div>
    </div>
  );
};
