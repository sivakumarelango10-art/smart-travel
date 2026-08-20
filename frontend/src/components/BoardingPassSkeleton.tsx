import React from 'react';

export const BoardingPassSkeleton: React.FC = () => {
  return (
    <div className="max-w-xl mx-auto rounded-3xl bg-slate-900 border border-slate-800 p-8 shadow-2xl animate-pulse space-y-6">
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div className="w-32 h-8 bg-slate-800 rounded-lg" />
        <div className="w-24 h-6 bg-slate-800 rounded" />
      </div>

      <div className="flex items-center justify-between py-6 border-b border-slate-800">
        <div className="space-y-2">
          <div className="w-20 h-10 bg-slate-800 rounded" />
          <div className="w-16 h-4 bg-slate-800/80 rounded" />
        </div>
        <div className="w-24 h-4 bg-slate-800 rounded" />
        <div className="space-y-2 text-right flex flex-col items-end">
          <div className="w-20 h-10 bg-slate-800 rounded" />
          <div className="w-16 h-4 bg-slate-800/80 rounded" />
        </div>
      </div>

      <div className="grid grid-cols-4 gap-4 py-4 border-b border-slate-800">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="space-y-1.5">
            <div className="w-12 h-3 bg-slate-800/60 rounded" />
            <div className="w-16 h-5 bg-slate-800 rounded" />
          </div>
        ))}
      </div>

      <div className="h-24 bg-slate-950 rounded-2xl flex items-center justify-center">
        <div className="w-48 h-12 bg-slate-800 rounded" />
      </div>
    </div>
  );
};
