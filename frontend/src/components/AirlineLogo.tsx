import React from 'react';

interface AirlineLogoProps {
  airline?: string;
  airlineCode?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
  showIconOnly?: boolean;
}

interface AirlineBrandMeta {
  code: string;
  name: string;
  gradient: string;
  primaryColor: string;
  secondaryColor: string;
  accentColor: string;
  symbol: string;
  svgIcon: React.ReactNode;
}

export const AIRLINE_DATABASE: Record<string, AirlineBrandMeta> = {
  indigo: {
    code: '6E',
    name: 'IndiGo',
    gradient: 'from-[#00227B] via-[#004BBD] to-[#0A64FF]',
    primaryColor: '#00227B',
    secondaryColor: '#0A64FF',
    accentColor: '#60A5FA',
    symbol: '6E',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <path d="M4 18L14 15L24 7L28 8L20 17L28 20L26 23L16 20L8 23L4 18Z" opacity="0.9" />
        <circle cx="16" cy="16" r="1.5" fill="#93C5FD" />
      </svg>
    ),
  },
  'air india': {
    code: 'AI',
    name: 'Air India',
    gradient: 'from-[#8B0000] via-[#C8102E] to-[#E31B23]',
    primaryColor: '#8B0000',
    secondaryColor: '#E31B23',
    accentColor: '#F59E0B',
    symbol: 'AI',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <path d="M5 20C10 13 18 8 27 6C25 11 21 17 14 24L9 25L11 20L6 22L5 20Z" />
        <path d="M19 11C21 10 23 10 25 11C24 13 22 14 20 14L19 11Z" fill="#FDE68A" />
      </svg>
    ),
  },
  'air india express': {
    code: 'IX',
    name: 'Air India Express',
    gradient: 'from-[#D9381E] via-[#FF5722] to-[#FF7043]',
    primaryColor: '#D9381E',
    secondaryColor: '#FF5722',
    accentColor: '#FBBF24',
    symbol: 'IX',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <path d="M6 18L16 15L26 7L28 9L21 17L27 21L25 24L17 20L10 24L6 18Z" />
        <polygon points="12,14 18,10 19,13 13,16" fill="#FEF08A" />
      </svg>
    ),
  },
  'akasa air': {
    code: 'QP',
    name: 'Akasa Air',
    gradient: 'from-[#D84315] via-[#FF6D00] to-[#FFA000]',
    primaryColor: '#FF6D00',
    secondaryColor: '#FFA000',
    accentColor: '#FED7AA',
    symbol: 'QP',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <path d="M6 22C12 18 16 12 26 8L22 18C16 19 12 23 6 22Z" opacity="0.95" />
        <path d="M14 14L24 7L20 16L14 14Z" fill="#FFEDD5" />
      </svg>
    ),
  },
  spicejet: {
    code: 'SG',
    name: 'SpiceJet',
    gradient: 'from-[#C62828] via-[#E53935] to-[#FB8C00]',
    primaryColor: '#C62828',
    secondaryColor: '#E53935',
    accentColor: '#FDE047',
    symbol: 'SG',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <circle cx="8" cy="16" r="2.5" fill="#FEF08A" />
        <circle cx="14" cy="14" r="3" fill="#FDBA74" />
        <circle cx="21" cy="11" r="3.5" fill="#F87171" />
        <path d="M12 19L26 10L22 22L12 19Z" opacity="0.85" />
      </svg>
    ),
  },
  'alliance air': {
    code: '9I',
    name: 'Alliance Air',
    gradient: 'from-[#0D47A1] via-[#1976D2] to-[#42A5F5]',
    primaryColor: '#0D47A1',
    secondaryColor: '#1976D2',
    accentColor: '#90CAF9',
    symbol: '9I',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <path d="M6 16L14 13L24 8L26 10L19 17L25 21L23 23L15 19L9 22L6 16Z" />
      </svg>
    ),
  },
  vistara: {
    code: 'UK',
    name: 'Vistara',
    gradient: 'from-[#280524] via-[#4A154B] to-[#7B1FA2]',
    primaryColor: '#4A154B',
    secondaryColor: '#7B1FA2',
    accentColor: '#E9D5FF',
    symbol: 'UK',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <polygon points="16,4 20,12 28,16 20,20 16,28 12,20 4,16 12,12" opacity="0.95" />
        <circle cx="16" cy="16" r="2.5" fill="#F5D0FE" />
      </svg>
    ),
  },
  emirates: {
    code: 'EK',
    name: 'Emirates',
    gradient: 'from-[#8B0000] via-[#B71C1C] to-[#D32F2F]',
    primaryColor: '#B71C1C',
    secondaryColor: '#D32F2F',
    accentColor: '#FDE047',
    symbol: 'EK',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <path d="M5 21L15 17L25 9L27 10L20 18L26 22L24 24L16 21L9 24L5 21Z" />
        <rect x="14" y="14" width="4" height="4" fill="#FBBF24" opacity="0.75" />
      </svg>
    ),
  },
  'qatar airways': {
    code: 'QR',
    name: 'Qatar Airways',
    gradient: 'from-[#3B071E] via-[#5C0632] to-[#7B0B47]',
    primaryColor: '#5C0632',
    secondaryColor: '#7B0B47',
    accentColor: '#F472B6',
    symbol: 'QR',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <path d="M7 21C11 16 16 10 26 7C24 13 20 19 13 25L8 25L10 21L7 21Z" />
      </svg>
    ),
  },
  'singapore airlines': {
    code: 'SQ',
    name: 'Singapore Airlines',
    gradient: 'from-[#001E62] via-[#0A2F8A] to-[#1E40AF]',
    primaryColor: '#001E62',
    secondaryColor: '#0A2F8A',
    accentColor: '#F59E0B',
    symbol: 'SQ',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <path d="M4 22L16 17L28 7L27 10L18 18L26 23L23 25L15 20L7 24L4 22Z" />
        <path d="M16 11L24 7L20 13L16 11Z" fill="#FBBF24" />
      </svg>
    ),
  },
  lufthansa: {
    code: 'LH',
    name: 'Lufthansa',
    gradient: 'from-[#05164D] via-[#0A267A] to-[#143D99]',
    primaryColor: '#05164D',
    secondaryColor: '#FFB800',
    accentColor: '#FFB800',
    symbol: 'LH',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <circle cx="16" cy="16" r="12" fill="none" stroke="#FFB800" strokeWidth="2" />
        <path d="M9 19C13 14 17 11 23 9C21 14 17 17 12 21L9 19Z" fill="#FFB800" />
      </svg>
    ),
  },
  'british airways': {
    code: 'BA',
    name: 'British Airways',
    gradient: 'from-[#072146] via-[#0B3A7B] to-[#C8102E]',
    primaryColor: '#072146',
    secondaryColor: '#C8102E',
    accentColor: '#93C5FD',
    symbol: 'BA',
    svgIcon: (
      <svg viewBox="0 0 32 32" className="w-full h-full fill-current">
        <path d="M6 18C12 12 18 8 26 6C23 11 18 16 12 23L6 18Z" fill="#93C5FD" />
        <path d="M16 16C20 13 23 11 27 10C25 14 22 17 18 20L16 16Z" fill="#EF4444" />
      </svg>
    ),
  },
};

export const AirlineLogo: React.FC<AirlineLogoProps> = ({
  airline = 'SmartTravel Airways',
  airlineCode,
  size = 'md',
  className = '',
  showIconOnly = false,
}) => {
  const norm = airline ? airline.toLowerCase().trim() : '';
  const brand =
    AIRLINE_DATABASE[norm] ||
    Object.values(AIRLINE_DATABASE).find(
      (b) =>
        (airlineCode && b.code.toLowerCase() === airlineCode.toLowerCase()) ||
        (norm && norm.includes(b.name.toLowerCase()))
    );

  const sizeClasses = {
    xs: 'w-7 h-7 text-[10px]',
    sm: 'w-8 h-8 text-xs',
    md: 'w-11 h-11 text-sm',
    lg: 'w-12 h-12 text-base',
    xl: 'w-14 h-14 text-lg',
  }[size];

  const iconSizes = {
    xs: 'w-4 h-4',
    sm: 'w-4.5 h-4.5',
    md: 'w-6 h-6',
    lg: 'w-7 h-7',
    xl: 'w-8 h-8',
  }[size];

  const codeDisplay = airlineCode || (brand ? brand.code : (airline ? airline.slice(0, 2).toUpperCase() : 'ST'));
  const gradientClass = brand ? brand.gradient : 'from-slate-700 via-indigo-800 to-slate-900';

  return (
    <div
      className={`relative inline-flex items-center justify-center rounded-xl bg-gradient-to-br ${gradientClass} font-black tracking-wider text-white shadow-lg select-none shrink-0 border border-white/20 transition-all duration-300 group-hover:scale-105 overflow-hidden ${sizeClasses} ${className}`}
      title={`${airline} (${codeDisplay})`}
    >
      {/* Background airplane insignia watermark */}
      <div className="absolute -right-1 -bottom-1 opacity-25 w-3/4 h-3/4 text-white pointer-events-none">
        {brand?.svgIcon || (
          <svg viewBox="0 0 24 24" fill="currentColor" className="w-full h-full transform rotate-45">
            <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z" />
          </svg>
        )}
      </div>

      {/* Front visual content */}
      <div className="relative z-10 flex flex-col items-center justify-center">
        {showIconOnly && brand?.svgIcon ? (
          <div className={`${iconSizes} text-white drop-shadow-md flex items-center justify-center`}>
            {brand.svgIcon}
          </div>
        ) : (
          <div className="flex flex-col items-center">
            <span className="drop-shadow-md font-mono font-black text-white leading-none tracking-normal">
              {codeDisplay}
            </span>
          </div>
        )}
      </div>

      {/* Subtle glass reflection overlay */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/20 via-transparent to-white/15 pointer-events-none rounded-xl ring-1 ring-inset ring-white/10" />
    </div>
  );
};
