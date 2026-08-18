import React from 'react';
import { UserPlus } from 'lucide-react';
import { Link } from 'react-router-dom';

export const RegisterPage: React.FC = () => {
  return (
    <div className="max-w-md mx-auto py-12">
      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-8 shadow-xl space-y-6">
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center mx-auto">
            <UserPlus className="w-6 h-6" />
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Create Account</h1>
          <p className="text-xs text-slate-400">Join SmartTravel Platform (Phase 1 UI Placeholder)</p>
        </div>

        <form onSubmit={(e) => e.preventDefault()} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <label className="text-xs font-medium text-slate-300">First Name</label>
              <input
                type="text"
                placeholder="John"
                disabled
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-300 cursor-not-allowed opacity-75"
              />
            </div>
            <div className="space-y-1">
              <label className="text-xs font-medium text-slate-300">Last Name</label>
              <input
                type="text"
                placeholder="Doe"
                disabled
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-300 cursor-not-allowed opacity-75"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-medium text-slate-300">Email</label>
            <input
              type="email"
              placeholder="john.doe@smarttravel.com"
              disabled
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-300 cursor-not-allowed opacity-75"
            />
          </div>

          <div className="p-3 rounded-lg bg-sky-500/10 border border-sky-500/20 text-sky-400 text-xs">
            <span className="font-semibold">Phase 1 Foundation:</span> Full registration flow with Bean Validation will be enabled in the dedicated Authentication phase.
          </div>

          <button
            type="button"
            disabled
            className="w-full py-2.5 rounded-lg bg-slate-800 text-slate-400 text-sm font-medium cursor-not-allowed"
          >
            Register (Ready in Auth Phase)
          </button>
        </form>

        <div className="text-center text-xs text-slate-400">
          Already have an account?{' '}
          <Link to="/login" className="text-sky-400 hover:text-sky-300 font-medium">
            Log in
          </Link>
        </div>
      </div>
    </div>
  );
};
