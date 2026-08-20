import React from 'react';

export const BookingSkeleton: React.FC = () => {
  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-pulse">
      <div className="h-40 bg-slate-900 border border-slate-800 rounded-3xl p-6" />
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2 h-96 bg-slate-900 border border-slate-800 rounded-3xl p-6" />
        <div className="h-80 bg-slate-900 border border-slate-800 rounded-3xl p-6" />
      </div>
    </div>
  );
};
