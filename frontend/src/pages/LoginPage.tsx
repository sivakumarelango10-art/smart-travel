import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { LogIn, Lock, Mail, AlertCircle, Plane, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const from = (location.state as any)?.from?.pathname || '/';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please fill in both email and password.');
      return;
    }
    try {
      setLoading(true);
      setError(null);
      await login({ email, password });
      navigate(from, { replace: true });
    } catch (err: any) {
      setError(err?.message || 'Invalid email or password credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto py-12 sm:py-16 px-4 animate-fade-in">
      <div className="rounded-3xl bg-slate-900/90 border border-slate-800 p-8 sm:p-9 shadow-2xl space-y-6 relative overflow-hidden backdrop-blur-xl">
        <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-sky-500 via-indigo-500 to-emerald-500"></div>

        <div className="text-center space-y-2.5">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-sky-500/20 to-indigo-500/20 border border-sky-500/30 flex items-center justify-center mx-auto shadow-xl shadow-sky-500/15">
            <Plane className="w-7 h-7 text-sky-400 transform -rotate-45" />
          </div>
          <div className="space-y-1">
            <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">Welcome to SmartTravel</h1>
            <p className="text-xs text-slate-400">Sign in to access your flight bookings & tickets</p>
          </div>
        </div>

        {error && (
          <div className="p-4 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-semibold flex items-center gap-2.5">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-300">Email Address</label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-500 absolute left-4 top-3.5" />
              <input
                type="email"
                placeholder="sarah@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition font-medium"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-300">Password</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-500 absolute left-4 top-3.5" />
              <input
                type="password"
                placeholder="••••••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition font-medium"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-sky-500 via-indigo-500 to-blue-600 hover:from-sky-400 hover:via-indigo-400 hover:to-blue-500 text-white font-black text-sm shadow-xl shadow-sky-500/25 hover:shadow-sky-500/40 hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {loading ? (
              <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
            ) : (
              <>
                <LogIn className="w-4 h-4" />
                <span>Sign In to Account</span>
              </>
            )}
          </button>
        </form>

        <div className="pt-4 border-t border-slate-800 text-center space-y-3">
          <p className="text-xs text-slate-400">
            Don't have an account?{' '}
            <Link to="/register" className="text-sky-400 hover:text-sky-300 font-bold ml-1 transition">
              Create an account
            </Link>
          </p>

          <div className="flex items-center justify-center gap-2 text-[11px] text-slate-500">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
            <span>256-Bit Encrypted & Tokenized Session</span>
          </div>
        </div>
      </div>
    </div>
  );
};

