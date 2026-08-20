import React from 'react';

export const FlightCardSkeleton: React.FC = () => {
  return (
    <div className="rounded-2xl bg-slate-900/60 border border-slate-800 p-6 animate-pulse space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 bg-slate-800 rounded-xl" />
          <div className="space-y-1.5">
            <div className="w-24 h-4 bg-slate-800 rounded" />
            <div className="w-16 h-3 bg-slate-800/80 rounded" />
          </div>
        </div>
        <div className="w-28 h-6 bg-slate-800 rounded-full" />
      </div>

      <div className="grid grid-cols-3 gap-4 py-3 border-y border-slate-800/60">
        <div className="space-y-1">
          <div className="w-16 h-6 bg-slate-800 rounded" />
          <div className="w-12 h-3 bg-slate-800/60 rounded" />
        </div>
        <div className="flex flex-col items-center justify-center space-y-1">
          <div className="w-14 h-3 bg-slate-800/80 rounded" />
          <div className="w-20 h-1 bg-slate-800 rounded" />
        </div>
        <div className="space-y-1 text-right flex flex-col items-end">
          <div className="w-16 h-6 bg-slate-800 rounded" />
          <div className="w-12 h-3 bg-slate-800/60 rounded" />
        </div>
      </div>

      <div className="flex items-center justify-between pt-1">
        <div className="w-20 h-4 bg-slate-800/60 rounded" />
        <div className="flex items-center gap-3">
          <div className="w-20 h-6 bg-slate-800 rounded" />
          <div className="w-24 h-9 bg-slate-800 rounded-xl" />
        </div>
      </div>
    </div>
  );
};
