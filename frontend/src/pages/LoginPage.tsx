import React from 'react';
import { LogIn, Lock, Mail } from 'lucide-react';
import { Link } from 'react-router-dom';

export const LoginPage: React.FC = () => {
  return (
    <div className="max-w-md mx-auto py-12">
      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-8 shadow-xl space-y-6">
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center mx-auto">
            <LogIn className="w-6 h-6" />
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Welcome Back</h1>
          <p className="text-xs text-slate-400">Authentication Foundation Ready (Phase 1 UI Placeholder)</p>
        </div>

        <form onSubmit={(e) => e.preventDefault()} className="space-y-4">
          <div className="space-y-1">
            <label className="text-xs font-medium text-slate-300">Email Address</label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
              <input
                type="email"
                placeholder="traveler@smarttravel.com"
                disabled
                className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-4 py-2.5 text-sm text-slate-300 focus:outline-none cursor-not-allowed opacity-75"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-medium text-slate-300">Password</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
              <input
                type="password"
                placeholder="••••••••••••"
                disabled
                className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-4 py-2.5 text-sm text-slate-300 focus:outline-none cursor-not-allowed opacity-75"
              />
            </div>
          </div>

          <div className="p-3 rounded-lg bg-sky-500/10 border border-sky-500/20 text-sky-400 text-xs">
            <span className="font-semibold">Phase 1 Foundation:</span> Full JWT Authentication Flow will be activated in the dedicated Authentication phase.
          </div>

          <button
            type="button"
            disabled
            className="w-full py-2.5 rounded-lg bg-slate-800 text-slate-400 text-sm font-medium cursor-not-allowed"
          >
            Login (Ready in Auth Phase)
          </button>
        </form>

        <div className="text-center text-xs text-slate-400">
          Don't have an account?{' '}
          <Link to="/register" className="text-sky-400 hover:text-sky-300 font-medium">
            Register here
          </Link>
        </div>
      </div>
    </div>
  );
};
