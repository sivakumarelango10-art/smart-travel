import React from 'react';
import { Compass, ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';

export const NotFoundPage: React.FC = () => {
  return (
    <div className="text-center py-20 space-y-4">
      <div className="w-16 h-16 rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/20 flex items-center justify-center mx-auto">
        <Compass className="w-8 h-8" />
      </div>
      <h1 className="text-4xl font-extrabold text-white">404</h1>
      <p className="text-slate-400 text-sm">Destination Not Found. The requested route does not exist.</p>
      <Link
        to="/"
        className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-sky-600 hover:bg-sky-500 text-white text-xs font-medium transition shadow-lg shadow-sky-500/20"
      >
        <ArrowLeft className="w-4 h-4" />
        Return to Overview
      </Link>
    </div>
  );
};
