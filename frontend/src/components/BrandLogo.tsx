import React from 'react';
import { Link } from 'react-router-dom';

interface BrandLogoProps {
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  withLink?: boolean;
  className?: string;
  showTagline?: boolean;
  theme?: 'dark' | 'light' | 'auto';
}

export const BrandLogo: React.FC<BrandLogoProps> = ({
  size = 'md',
  withLink = true,
  className = '',
  showTagline = true,
}) => {
  const sizeMap = {
    xs: { logoH: 'h-6', text: 'text-base', tagline: 'text-[8px]' },
    sm: { logoH: 'h-8', text: 'text-lg', tagline: 'text-[9px]' },
    md: { logoH: 'h-9', text: 'text-xl', tagline: 'text-[10px]' },
    lg: { logoH: 'h-11', text: 'text-2xl', tagline: 'text-[11px]' },
    xl: { logoH: 'h-14', text: 'text-3xl', tagline: 'text-xs' },
  }[size];

  const content = (
    <div className={`inline-flex items-center gap-2.5 group select-none ${className}`}>
      {/* Brand Icon Badge */}
      <div className="relative flex items-center justify-center shrink-0">
        <img
          src="/logo.png"
          alt="SmartTravel Logo"
          className={`${sizeMap.logoH} w-auto object-contain transition-transform duration-200 group-hover:scale-105`}
          onError={(e) => {
            // Graceful fallback SVG icon if PNG fails
            const target = e.currentTarget;
            target.style.display = 'none';
            if (target.nextElementSibling) {
              (target.nextElementSibling as HTMLElement).style.display = 'flex';
            }
          }}
        />
        {/* Fallback luxury gold geometric brand emblem */}
        <div
          style={{ display: 'none' }}
          className={`${sizeMap.logoH} aspect-square rounded-xl bg-gradient-to-br from-amber-400 via-amber-500 to-amber-600 flex items-center justify-center text-black font-black shadow-glow-gold`}
        >
          <svg className="w-3/5 h-3/5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path strokeLinecap="round" strokeLinejoin="round" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
      </div>

      {/* Brand Wordmark & Tagline */}
      <div className="flex flex-col leading-none">
        <div className="flex items-center tracking-tight">
          <span className={`font-black ${sizeMap.text} text-white`}>
            Smart
          </span>
          <span className={`font-black ${sizeMap.text} text-secondary ml-0.5`}>
            Travel
          </span>
          <span className="w-1.5 h-1.5 rounded-full bg-accent ml-1 animate-pulse"></span>
        </div>
        {showTagline && (
          <span className={`font-bold tracking-widest uppercase text-slate-400 ${sizeMap.tagline} mt-0.5`}>
            EXPLORE • BOOK • JOURNEY
          </span>
        )}
      </div>
    </div>
  );

  if (withLink) {
    return (
      <Link
        to="/"
        className="inline-flex items-center focus:outline-none focus-visible:ring-2 focus-visible:ring-secondary rounded-lg"
        aria-label="SmartTravel Homepage"
      >
        {content}
      </Link>
    );
  }

  return content;
};
