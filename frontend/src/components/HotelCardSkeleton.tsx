import React from 'react';
import { Skeleton } from './Skeleton';

export const HotelCardSkeleton: React.FC = () => {
  return (
    <div className="bg-slate-900/80 border border-slate-800/80 rounded-2xl overflow-hidden shadow-xl flex flex-col md:flex-row">
      <div className="w-full md:w-72 h-52 md:h-auto shrink-0">
        <Skeleton className="w-full h-full" variant="rectangular" />
      </div>

      <div className="p-6 flex-1 flex flex-col justify-between space-y-4">
        <div>
          <div className="flex items-start justify-between gap-4">
            <div className="space-y-2">
              <Skeleton className="w-48 h-6" />
              <Skeleton className="w-32 h-4" />
            </div>
            <Skeleton className="w-14 h-7 rounded-lg" />
          </div>

          <div className="flex gap-2 mt-4">
            <Skeleton className="w-16 h-6 rounded-full" />
            <Skeleton className="w-20 h-6 rounded-full" />
            <Skeleton className="w-24 h-6 rounded-full" />
          </div>
        </div>

        <div className="flex items-center justify-between pt-4 border-t border-slate-800/60">
          <div className="space-y-1">
            <Skeleton className="w-28 h-6" />
            <Skeleton className="w-20 h-3" />
          </div>
          <Skeleton className="w-32 h-10 rounded-xl" />
        </div>
      </div>
    </div>
  );
};
