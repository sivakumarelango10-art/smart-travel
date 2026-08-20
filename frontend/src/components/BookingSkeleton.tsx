import React from 'react';
import { Skeleton } from './Skeleton';

export const BookingSkeleton: React.FC = () => {
  return (
    <div className="bg-slate-900/70 border border-slate-800/80 rounded-2xl p-6 space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <Skeleton className="w-24 h-5" />
            <Skeleton className="w-16 h-5 rounded-full" />
          </div>
          <Skeleton className="w-40 h-4" />
        </div>
        <Skeleton className="w-28 h-8 rounded-xl" />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t border-slate-800/60">
        <div className="space-y-1">
          <Skeleton className="w-20 h-3" />
          <Skeleton className="w-32 h-5" />
        </div>
        <div className="space-y-1">
          <Skeleton className="w-20 h-3" />
          <Skeleton className="w-32 h-5" />
        </div>
        <div className="space-y-1">
          <Skeleton className="w-20 h-3" />
          <Skeleton className="w-28 h-5" />
        </div>
      </div>
    </div>
  );
};
