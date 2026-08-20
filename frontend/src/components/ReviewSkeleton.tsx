import React from 'react';
import { Skeleton } from './Skeleton';

export const ReviewSkeleton: React.FC = () => {
  return (
    <div className="bg-slate-900/60 border border-slate-800/80 rounded-2xl p-6 space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Skeleton className="w-10 h-10" variant="circular" />
          <div className="space-y-1.5">
            <Skeleton className="w-32 h-4" />
            <Skeleton className="w-20 h-3" />
          </div>
        </div>
        <div className="flex gap-1">
          {[...Array(5)].map((_, i) => (
            <Skeleton key={i} className="w-4 h-4 rounded-sm" />
          ))}
        </div>
      </div>
      <Skeleton className="w-3/4 h-5" />
      <div className="space-y-2">
        <Skeleton className="w-full h-3.5" />
        <Skeleton className="w-5/6 h-3.5" />
      </div>
      <div className="flex gap-2 pt-2">
        <Skeleton className="w-16 h-16 rounded-lg" />
        <Skeleton className="w-16 h-16 rounded-lg" />
      </div>
    </div>
  );
};
