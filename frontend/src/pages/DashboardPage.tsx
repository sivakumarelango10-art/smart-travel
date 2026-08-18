import React from 'react';
import { LayoutDashboard, Plane } from 'lucide-react';
import { Link } from 'react-router-dom';

export const DashboardPage: React.FC = () => {
  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <LayoutDashboard className="w-6 h-6 text-sky-400" />
            Traveler Dashboard
          </h1>
          <p className="text-xs text-slate-400">Phase 1 Foundation Dashboard Placeholder</p>
        </div>
      </div>

      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-8 text-center space-y-4">
        <div className="w-16 h-16 rounded-2xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center mx-auto">
          <Plane className="w-8 h-8" />
        </div>
        <h2 className="text-lg font-bold text-white">Live Dashboard Ready for Feature Modules</h2>
        <p className="text-xs text-slate-400 max-w-lg mx-auto leading-relaxed">
          The dashboard will aggregate your live flight tracking radar, booking itineraries, cancellation refund requests, frozen prices, and personalized destination feeds.
        </p>

        <div className="pt-2">
          <Link
            to="/"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium border border-slate-700 transition"
          >
            ← Back to System Overview
          </Link>
        </div>
      </div>
    </div>
  );
};
