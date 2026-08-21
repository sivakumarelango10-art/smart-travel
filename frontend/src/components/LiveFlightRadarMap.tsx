import React from 'react';
import { Navigation, Radio } from 'lucide-react';
import { FlightStatusSnapshot } from '../types/tracking';

interface LiveFlightRadarMapProps {
  flight: FlightStatusSnapshot;
}

export const LiveFlightRadarMap: React.FC<LiveFlightRadarMapProps> = ({ flight }) => {
  const originCode = flight.originCode || 'DEL';
  const destCode = flight.destCode || 'BOM';
  const originCity = flight.originCity || 'New Delhi';
  const destCity = flight.destCity || 'Mumbai';
  const progress = Math.min(100, Math.max(0, flight.progressPercent ?? 55));
  const altitude = flight.altitudeFeet ?? (flight.status === 'DEPARTED' ? 36000 : 0);
  const speed = flight.groundSpeedKmph ?? (flight.status === 'DEPARTED' ? 840 : 0);
  const aircraft = flight.aircraftModel || 'Airbus A321neo';

  // SVG Radar Coordinates mapping
  const startX = 70;
  const startY = 190;
  const endX = 530;
  const endY = 100;
  const controlX = 300;
  const controlY = 30; // Curved arc peak

  // Calculate quadratic Bezier point at t = progress / 100
  const t = progress / 100;
  const planeX = (1 - t) * (1 - t) * startX + 2 * (1 - t) * t * controlX + t * t * endX;
  const planeY = (1 - t) * (1 - t) * startY + 2 * (1 - t) * t * controlY + t * t * endY;

  // Tangent angle for plane orientation
  const dx = 2 * (1 - t) * (controlX - startX) + 2 * t * (endX - controlX);
  const dy = 2 * (1 - t) * (controlY - startY) + 2 * t * (endY - controlY);
  const angleRad = Math.atan2(dy, dx);
  const angleDeg = (angleRad * 180) / Math.PI;

  return (
    <div className="relative overflow-hidden rounded-2xl bg-slate-950 border border-slate-800/80 shadow-2xl p-4 sm:p-6">
      {/* Radar Background Visualizer & Grid Lines */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-blue-950/20 via-slate-950/80 to-slate-950 pointer-events-none" />
      
      {/* Radar Header Bar */}
      <div className="relative z-10 flex flex-wrap items-center justify-between gap-3 pb-4 border-b border-slate-800/80 text-xs">
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-ping" />
          <span className="font-mono font-bold text-emerald-400 uppercase tracking-widest text-[11px] flex items-center gap-1.5">
            <Radio className="w-3.5 h-3.5 text-emerald-400" />
            LIVE AIRSPACE RADAR • {flight.flightNumber}
          </span>
        </div>

        <div className="flex items-center gap-3 text-slate-400 font-mono text-[11px]">
          <span className="flex items-center gap-1 text-slate-300">
            <Navigation className="w-3.5 h-3.5 text-blue-400" />
            HDG: {Math.round(angleDeg + 90)}°
          </span>
          <span className="text-slate-600">|</span>
          <span className="text-slate-300">ALT: {altitude.toLocaleString()} FT</span>
          <span className="text-slate-600">|</span>
          <span className="text-slate-300">SPD: {speed} KM/H</span>
        </div>
      </div>

      {/* Interactive Radar Flight Canvas (SVG) */}
      <div className="relative z-10 my-4 h-48 sm:h-56 w-full flex items-center justify-center">
        <svg
          viewBox="0 0 600 240"
          className="w-full h-full select-none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            {/* Grid Pattern */}
            <pattern id="radar-grid" width="40" height="40" patternUnits="userSpaceOnUse">
              <path d="M 40 0 L 0 0 0 40" fill="none" stroke="rgba(51, 65, 85, 0.25)" strokeWidth="0.75" />
            </pattern>
            {/* Route Gradient */}
            <linearGradient id="routeGradient" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#38bdf8" stopOpacity="0.8" />
              <stop offset="50%" stopColor="#60a5fa" stopOpacity="1" />
              <stop offset="100%" stopColor="#a855f7" stopOpacity="0.8" />
            </linearGradient>
            {/* Glow Filter */}
            <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
              <feGaussianBlur stdDeviation="3" result="glow" />
              <feComposite in="SourceGraphic" in2="glow" operator="over" />
            </filter>
          </defs>

          {/* Grid Background */}
          <rect width="600" height="240" fill="url(#radar-grid)" />

          {/* Concentric Radar Rings */}
          <circle cx="300" cy="120" r="100" fill="none" stroke="rgba(59, 130, 246, 0.08)" strokeWidth="1" strokeDasharray="3,3" />
          <circle cx="300" cy="120" r="180" fill="none" stroke="rgba(59, 130, 246, 0.05)" strokeWidth="1" />

          {/* Geodesic Flight Route Arc */}
          <path
            d={`M ${startX} ${startY} Q ${controlX} ${controlY} ${endX} ${endY}`}
            fill="none"
            stroke="rgba(51, 65, 85, 0.7)"
            strokeWidth="3"
            strokeDasharray="6,4"
          />

          {/* Active Flown Portion of Path */}
          <path
            d={`M ${startX} ${startY} Q ${controlX} ${controlY} ${endX} ${endY}`}
            fill="none"
            stroke="url(#routeGradient)"
            strokeWidth="3.5"
            strokeDasharray="600"
            strokeDashoffset={600 * (1 - t)}
            filter="url(#glow)"
          />

          {/* Origin Airport Node */}
          <g transform={`translate(${startX}, ${startY})`}>
            <circle r="7" fill="#0284c7" />
            <circle r="12" fill="none" stroke="#38bdf8" strokeWidth="1.5" opacity="0.6" />
            <text x="0" y="24" fill="#38bdf8" fontSize="11" fontWeight="bold" textAnchor="middle" fontFamily="monospace">
              {originCode}
            </text>
          </g>

          {/* Destination Airport Node */}
          <g transform={`translate(${endX}, ${endY})`}>
            <circle r="7" fill="#9333ea" />
            <circle r="12" fill="none" stroke="#c084fc" strokeWidth="1.5" opacity="0.6" />
            <text x="0" y="24" fill="#c084fc" fontSize="11" fontWeight="bold" textAnchor="middle" fontFamily="monospace">
              {destCode}
            </text>
          </g>

          {/* Moving Aircraft Marker */}
          <g transform={`translate(${planeX}, ${planeY}) rotate(${angleDeg})`}>
            <circle r="16" fill="rgba(56, 189, 248, 0.15)" className="animate-ping" />
            <circle r="10" fill="#0284c7" stroke="#38bdf8" strokeWidth="2" />
            <path
              d="M0 -7 L4 2 L1 2 L1 6 L-1 6 L-1 2 L-4 2 Z"
              fill="#ffffff"
              transform="rotate(90)"
            />
          </g>
        </svg>
      </div>

      {/* Route & Aircraft Details Bar */}
      <div className="relative z-10 grid grid-cols-1 sm:grid-cols-3 gap-3 pt-3 border-t border-slate-800/80 text-xs">
        {/* Origin */}
        <div className="p-2.5 rounded-xl bg-slate-900/80 border border-slate-800">
          <div className="flex items-center justify-between text-[11px] text-slate-400">
            <span>DEPARTURE</span>
            <span className="font-mono text-sky-400 font-bold">{originCode}</span>
          </div>
          <p className="font-bold text-white text-sm mt-0.5">{originCity}</p>
          <div className="flex items-center justify-between text-[11px] text-slate-400 mt-1">
            <span>Gate: {flight.gate || 'TBD'}</span>
            <span>Terminal: {flight.terminal || 'T3'}</span>
          </div>
        </div>

        {/* Aircraft & Telemetry Center */}
        <div className="p-2.5 rounded-xl bg-slate-900/80 border border-slate-800 text-center flex flex-col justify-between">
          <div className="text-[11px] text-slate-400 font-mono">
            {flight.airline || 'SmartTravel Airlines'} • {flight.flightNumber}
          </div>
          <div className="font-bold text-white text-xs my-0.5">{aircraft}</div>
          <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden mt-1">
            <div
              className="bg-gradient-to-r from-sky-400 to-indigo-500 h-full rounded-full transition-all duration-500"
              style={{ width: `${progress}%` }}
            />
          </div>
          <div className="text-[10px] text-slate-400 mt-1 font-mono">{progress.toFixed(0)}% COMPLETED</div>
        </div>

        {/* Destination */}
        <div className="p-2.5 rounded-xl bg-slate-900/80 border border-slate-800 text-right">
          <div className="flex items-center justify-between text-[11px] text-slate-400">
            <span className="font-mono text-indigo-400 font-bold">{destCode}</span>
            <span>ARRIVAL</span>
          </div>
          <p className="font-bold text-white text-sm mt-0.5">{destCity}</p>
          <div className="flex items-center justify-between text-[11px] text-slate-400 mt-1">
            <span>Baggage: {flight.baggageCarousel || 'Belt 4'}</span>
            <span>{flight.status === 'ARRIVED' ? 'Arrived' : 'On Schedule'}</span>
          </div>
        </div>
      </div>
    </div>
  );
};
