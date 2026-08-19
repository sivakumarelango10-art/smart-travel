import React from 'react';
import { Plane } from 'lucide-react';

interface PageLoaderProps {
  message?: string;
}

/**
 * Accessible, lightweight page loader with flight pulse animation
 * used as Suspense fallback for route transitions.
 */
export const PageLoader: React.FC<PageLoaderProps> = ({ message = 'Loading SmartTravel...' }) => {
  return (
    <div
      role="status"
      aria-live="polite"
      aria-busy="true"
      className="min-h-[50vh] flex flex-col items-center justify-center p-8 text-center"
    >
      <div className="relative flex items-center justify-center mb-4">
        <div className="w-12 h-12 rounded-full border-2 border-primary-200 dark:border-primary-900/50 border-t-primary-600 animate-spin" />
        <Plane className="w-5 h-5 text-primary-600 dark:text-primary-400 absolute" />
      </div>
      <p className="text-sm font-medium text-slate-600 dark:text-slate-300 animate-pulse">
        {message}
      </p>
      <span className="sr-only">Loading page content, please wait...</span>
    </div>
  );
};
