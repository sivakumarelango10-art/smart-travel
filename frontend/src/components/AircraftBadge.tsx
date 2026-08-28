import React from 'react';
import { Plane } from 'lucide-react';

interface AircraftBadgeProps {
  aircraftModel: string;
  className?: string;
  showManufacturer?: boolean;
}

export const AircraftBadge: React.FC<AircraftBadgeProps> = ({
  aircraftModel,
  className = '',
  showManufacturer = true,
}) => {
  const model = (aircraftModel || '').trim();
  const lower = model.toLowerCase();

  let manufacturer = 'Airbus';
  let badgeColor = 'bg-blue-500/15 text-blue-300 border-blue-500/30';
  let iconColor = 'text-blue-400';

  if (lower.includes('boeing') || lower.includes('737') || lower.includes('787') || lower.includes('777')) {
    manufacturer = 'Boeing';
    badgeColor = 'bg-cyan-500/15 text-cyan-300 border-cyan-500/30';
    iconColor = 'text-cyan-400';
  } else if (lower.includes('atr')) {
    manufacturer = 'ATR';
    badgeColor = 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30';
    iconColor = 'text-emerald-400';
  } else if (lower.includes('airbus') || lower.includes('a320') || lower.includes('a321') || lower.includes('a350') || lower.includes('a330') || lower.includes('a380')) {
    manufacturer = 'Airbus';
    badgeColor = 'bg-blue-500/15 text-blue-300 border-blue-500/30';
    iconColor = 'text-blue-400';
  }

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md border text-[11px] font-medium bg-[#141722] text-slate-300 border-white/10 ${className}`}
      title={`${manufacturer} • ${model}`}
    >
      <Plane className={`w-3 h-3 transform rotate-45 shrink-0 ${iconColor}`} />
      <span className="font-semibold text-slate-200 truncate">{model}</span>
      {showManufacturer && (
        <span className={`text-[9px] font-bold px-1 py-0.2 rounded border ${badgeColor} uppercase tracking-wider`}>
          {manufacturer}
        </span>
      )}
    </span>
  );
};
