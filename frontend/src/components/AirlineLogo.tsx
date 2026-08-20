import React from 'react';

interface AirlineLogoProps {
  airline?: string;
  airlineCode?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
}

interface AirlineBrandMeta {
  code: string;
  name: string;
  gradient: string;
  textColor: string;
  badgeBg: string;
  symbol: string;
}

const AIRLINE_DATABASE: Record<string, AirlineBrandMeta> = {
  indigo: {
    code: '6E',
    name: 'IndiGo',
    gradient: 'from-blue-600 to-indigo-800',
    textColor: 'text-blue-200',
    badgeBg: 'bg-blue-600',
    symbol: '6E',
  },
  'air india': {
    code: 'AI',
    name: 'Air India',
    gradient: 'from-red-600 via-rose-700 to-amber-700',
    textColor: 'text-amber-200',
    badgeBg: 'bg-red-600',
    symbol: 'AI',
  },
  'air india express': {
    code: 'IX',
    name: 'Air India Express',
    gradient: 'from-orange-600 to-red-600',
    textColor: 'text-orange-200',
    badgeBg: 'bg-orange-600',
    symbol: 'IX',
  },
  'akasa air': {
    code: 'QP',
    name: 'Akasa Air',
    gradient: 'from-orange-500 to-amber-600',
    textColor: 'text-white',
    badgeBg: 'bg-orange-500',
    symbol: 'QP',
  },
  spicejet: {
    code: 'SG',
    name: 'SpiceJet',
    gradient: 'from-red-600 to-amber-500',
    textColor: 'text-amber-100',
    badgeBg: 'bg-red-600',
    symbol: 'SG',
  },
  vistara: {
    code: 'UK',
    name: 'Vistara',
    gradient: 'from-purple-900 via-indigo-900 to-purple-800',
    textColor: 'text-purple-200',
    badgeBg: 'bg-purple-900',
    symbol: 'UK',
  },
  emirates: {
    code: 'EK',
    name: 'Emirates',
    gradient: 'from-red-700 via-red-800 to-amber-600',
    textColor: 'text-amber-200',
    badgeBg: 'bg-red-700',
    symbol: 'EK',
  },
  'qatar airways': {
    code: 'QR',
    name: 'Qatar Airways',
    gradient: 'from-purple-950 via-rose-950 to-burgundy-900',
    textColor: 'text-rose-200',
    badgeBg: 'bg-purple-950',
    symbol: 'QR',
  },
  'singapore airlines': {
    code: 'SQ',
    name: 'Singapore Airlines',
    gradient: 'from-blue-900 via-amber-600 to-blue-950',
    textColor: 'text-amber-200',
    badgeBg: 'bg-blue-900',
    symbol: 'SQ',
  },
  lufthansa: {
    code: 'LH',
    name: 'Lufthansa',
    gradient: 'from-yellow-500 via-amber-500 to-blue-900',
    textColor: 'text-blue-950',
    badgeBg: 'bg-yellow-500',
    symbol: 'LH',
  },
  'british airways': {
    code: 'BA',
    name: 'British Airways',
    gradient: 'from-blue-700 via-red-600 to-blue-900',
    textColor: 'text-white',
    badgeBg: 'bg-blue-700',
    symbol: 'BA',
  },
};

export const AirlineLogo: React.FC<AirlineLogoProps> = ({
  airline = 'SmartTravel Airways',
  airlineCode,
  size = 'md',
  className = '',
}) => {
  const norm = airline.toLowerCase().trim();
  const brand =
    AIRLINE_DATABASE[norm] ||
    Object.values(AIRLINE_DATABASE).find(
      (b) =>
        (airlineCode && b.code.toLowerCase() === airlineCode.toLowerCase()) ||
        norm.includes(b.name.toLowerCase())
    );

  const sizeClasses = {
    xs: 'w-6 h-6 text-[10px]',
    sm: 'w-8 h-8 text-xs',
    md: 'w-10 h-10 text-sm',
    lg: 'w-12 h-12 text-base',
    xl: 'w-14 h-14 text-lg',
  }[size];

  const codeDisplay = airlineCode || (brand ? brand.code : airline.slice(0, 2).toUpperCase());
  const gradientClass = brand ? brand.gradient : 'from-sky-600 via-indigo-700 to-blue-800';

  return (
    <div
      className={`relative inline-flex items-center justify-center rounded-xl bg-gradient-to-br ${gradientClass} font-black tracking-wider text-white shadow-md select-none shrink-0 border border-white/15 transition-transform duration-200 group-hover:scale-105 ${sizeClasses} ${className}`}
      title={airline}
    >
      <span className="drop-shadow-sm font-mono">{codeDisplay}</span>
      <div className="absolute inset-0 rounded-xl ring-1 ring-inset ring-white/10 pointer-events-none" />
    </div>
  );
};
