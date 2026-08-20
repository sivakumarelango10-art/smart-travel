import React from 'react';
import { BrandLogo } from './BrandLogo';

interface PageLoaderProps {
  message?: string;
}

/**
 * Accessible, perfectly centered page loader with prominent brand logo and elegant glow animation
 * used as Suspense fallback for route transitions.
 */
export const PageLoader: React.FC<PageLoaderProps> = ({ message = 'Loading SmartTravel...' }) => {
  return (
    <div
      role="status"
      aria-live="polite"
      aria-busy="true"
      className="min-h-[calc(100vh-220px)] w-full flex flex-col items-center justify-center p-8 text-center animate-fade-in my-auto"
    >
      <div className="relative flex items-center justify-center mb-6">
        <div className="absolute inset-0 -m-6 rounded-full bg-gradient-to-tr from-sky-500/20 via-indigo-500/15 to-emerald-500/15 blur-2xl animate-pulse" />
        <BrandLogo size="xl" withLink={false} className="relative z-10 animate-pulse drop-shadow-xl" />
      </div>

      <div className="flex items-center gap-2.5 my-2">
        <div className="w-2.5 h-2.5 rounded-full bg-sky-400 animate-bounce [animation-delay:-0.3s] shadow-lg shadow-sky-400/50" />
        <div className="w-2.5 h-2.5 rounded-full bg-indigo-400 animate-bounce [animation-delay:-0.15s] shadow-lg shadow-indigo-400/50" />
        <div className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-bounce shadow-lg shadow-emerald-400/50" />
      </div>

      <p className="text-sm font-semibold text-slate-300 mt-2 tracking-wide">
        {message}
      </p>
      <span className="sr-only">Loading page content, please wait...</span>
    </div>
  );
};
