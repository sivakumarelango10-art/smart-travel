import React from 'react';
import { Skeleton } from './Skeleton';

export const FlightCardSkeleton: React.FC = () => {
  return (
    <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl p-6 shadow-xl space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Skeleton className="w-10 h-10" variant="circular" />
          <div className="space-y-1.5">
            <Skeleton className="w-28 h-4" />
            <Skeleton className="w-16 h-3" />
          </div>
        </div>
        <Skeleton className="w-24 h-6 rounded-full" />
      </div>

      <div className="grid grid-cols-3 items-center py-3 border-y border-slate-800/60">
        <div className="space-y-1.5">
          <Skeleton className="w-16 h-6" />
          <Skeleton className="w-24 h-3.5" />
          <Skeleton className="w-12 h-3" />
        </div>
        <div className="flex flex-col items-center space-y-2">
          <Skeleton className="w-16 h-3" />
          <Skeleton className="w-full h-1" />
          <Skeleton className="w-14 h-3" />
        </div>
        <div className="text-right space-y-1.5 flex flex-col items-end">
          <Skeleton className="w-16 h-6" />
          <Skeleton className="w-24 h-3.5" />
          <Skeleton className="w-12 h-3" />
        </div>
      </div>

      <div className="flex items-center justify-between pt-1">
        <div className="space-y-1">
          <Skeleton className="w-20 h-7" />
          <Skeleton className="w-28 h-3" />
        </div>
        <div className="flex gap-2.5">
          <Skeleton className="w-24 h-10 rounded-xl" />
          <Skeleton className="w-28 h-10 rounded-xl" />
        </div>
      </div>
    </div>
  );
};
