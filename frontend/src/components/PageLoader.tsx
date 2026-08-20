import React from 'react';
import { BrandLogo } from './BrandLogo';

interface PageLoaderProps {
  message?: string;
}

/**
 * Accessible, lightweight page loader with branded logo and pulse animation
 * used as Suspense fallback for route transitions.
 */
export const PageLoader: React.FC<PageLoaderProps> = ({ message = 'Loading SmartTravel...' }) => {
  return (
    <div
      role="status"
      aria-live="polite"
      aria-busy="true"
      className="min-h-[50vh] flex flex-col items-center justify-center p-8 text-center animate-fade-in"
    >
      <div className="relative flex items-center justify-center mb-5">
        <div className="absolute inset-0 rounded-full bg-sky-500/10 blur-xl animate-pulse" />
        <BrandLogo size="lg" withLink={false} className="animate-pulse" />
      </div>
      <div className="flex items-center gap-2">
        <div className="w-2 h-2 rounded-full bg-sky-400 animate-bounce [animation-delay:-0.3s]" />
        <div className="w-2 h-2 rounded-full bg-indigo-400 animate-bounce [animation-delay:-0.15s]" />
        <div className="w-2 h-2 rounded-full bg-blue-500 animate-bounce" />
      </div>
      <p className="text-xs font-semibold text-slate-400 mt-3 tracking-wide">
        {message}
      </p>
      <span className="sr-only">Loading page content, please wait...</span>
    </div>
  );
};
