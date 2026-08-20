import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { UserPlus, User, Lock, Mail, Phone, AlertCircle, ShieldCheck, Check, X, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Real-time password rules
  const hasMinLength = password.length >= 8;
  const hasUpperCase = /[A-Z]/.test(password);
  const hasLowerCase = /[a-z]/.test(password);
  const hasNumber = /[0-9]/.test(password);
  const hasSpecial = /[@#$%^&+=!._-]/.test(password);
  const isPasswordStrong = hasMinLength && hasUpperCase && hasLowerCase && hasNumber && hasSpecial;
  const passwordsMatch = password.length > 0 && confirmPassword.length > 0 && password === confirmPassword;
  const passwordMismatch = confirmPassword.length > 0 && password !== confirmPassword;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (loading) return;

    if (!fullName.trim() || !email.trim() || !password || !confirmPassword) {
      setError('Please fill in all required fields.');
      return;
    }

    if (!isPasswordStrong) {
      setError('Password must meet all security requirements below.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await register({
        fullName: fullName.trim(),
        email: email.trim(),
        password,
        confirmPassword,
        phoneNumber: phoneNumber.trim() || undefined,
      });
      navigate('/');
    } catch (err: any) {
      setError(err?.message || 'Registration failed. Please check your details.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto py-12 sm:py-16 px-4 animate-fade-in">
      <div className="rounded-3xl bg-slate-900/90 border border-slate-800 p-8 sm:p-9 shadow-2xl space-y-6 relative overflow-hidden backdrop-blur-xl">
        <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-sky-500 via-indigo-500 to-emerald-500"></div>

        <div className="text-center space-y-2.5">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-indigo-500/20 to-sky-500/20 border border-indigo-500/30 flex items-center justify-center mx-auto shadow-xl shadow-indigo-500/15">
            <UserPlus className="w-7 h-7 text-indigo-400" />
          </div>
          <div className="space-y-1">
            <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">Create Your Account</h1>
            <p className="text-xs text-slate-400">Join SmartTravel for seamless flight bookings & e-tickets</p>
          </div>
        </div>

        {error && (
          <div className="p-4 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-semibold flex items-center gap-2.5 animate-slide-up">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-300">Full Name *</label>
            <div className="relative">
              <User className="w-4 h-4 text-slate-500 absolute left-4 top-3.5" />
              <input
                type="text"
                placeholder="Sarah Connor"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition font-medium"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-300">Email Address *</label>
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
            <label className="text-xs font-bold text-slate-300">Password *</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-500 absolute left-4 top-3.5" />
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder="••••••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-11 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition font-medium"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3.5 top-3.5 text-slate-500 hover:text-slate-300 transition"
                aria-label="Toggle password visibility"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* Password Security Helper Checklist */}
          {password.length > 0 && (
            <div className="p-3 rounded-2xl bg-slate-950/70 border border-slate-800 text-[11px] space-y-1.5 animate-fade-in">
              <p className="font-bold text-slate-400">Password requirements:</p>
              <div className="grid grid-cols-2 gap-1 text-[10px]">
                <div className={`flex items-center gap-1.5 ${hasMinLength ? 'text-emerald-400 font-semibold' : 'text-slate-500'}`}>
                  {hasMinLength ? <Check className="w-3 h-3 text-emerald-400" /> : <X className="w-3 h-3" />}
                  <span>8+ characters</span>
                </div>
                <div className={`flex items-center gap-1.5 ${hasUpperCase ? 'text-emerald-400 font-semibold' : 'text-slate-500'}`}>
                  {hasUpperCase ? <Check className="w-3 h-3 text-emerald-400" /> : <X className="w-3 h-3" />}
                  <span>Uppercase letter</span>
                </div>
                <div className={`flex items-center gap-1.5 ${hasLowerCase ? 'text-emerald-400 font-semibold' : 'text-slate-500'}`}>
                  {hasLowerCase ? <Check className="w-3 h-3 text-emerald-400" /> : <X className="w-3 h-3" />}
                  <span>Lowercase letter</span>
                </div>
                <div className={`flex items-center gap-1.5 ${hasNumber && hasSpecial ? 'text-emerald-400 font-semibold' : 'text-slate-500'}`}>
                  {hasNumber && hasSpecial ? <Check className="w-3 h-3 text-emerald-400" /> : <X className="w-3 h-3" />}
                  <span>Number & symbol</span>
                </div>
              </div>
            </div>
          )}

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-300">Confirm Password *</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-500 absolute left-4 top-3.5" />
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                placeholder="••••••••••••"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                className={`w-full bg-slate-950 border rounded-xl pl-11 pr-11 py-3 text-sm text-white placeholder-slate-600 focus:outline-none transition font-medium ${
                  passwordMismatch
                    ? 'border-rose-500 focus:border-rose-500'
                    : passwordsMatch
                    ? 'border-emerald-500/70 focus:border-emerald-500'
                    : 'border-slate-800 focus:border-sky-500'
                }`}
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-3.5 top-3.5 text-slate-500 hover:text-slate-300 transition"
                aria-label="Toggle confirm password visibility"
              >
                {showConfirmPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {passwordMismatch && (
              <p className="text-[11px] text-rose-400 font-bold flex items-center gap-1 mt-1 animate-fade-in">
                <AlertCircle className="w-3 h-3 shrink-0" />
                <span>Passwords do not match.</span>
              </p>
            )}
            {passwordsMatch && (
              <p className="text-[11px] text-emerald-400 font-semibold flex items-center gap-1 mt-1 animate-fade-in">
                <Check className="w-3 h-3 shrink-0" />
                <span>Passwords match</span>
              </p>
            )}
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-300">Phone Number (Optional)</label>
            <div className="relative">
              <Phone className="w-4 h-4 text-slate-500 absolute left-4 top-3.5" />
              <input
                type="tel"
                placeholder="+91 98765 43210"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition font-medium"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading || (confirmPassword.length > 0 && !passwordsMatch)}
            className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-sky-500 via-indigo-500 to-blue-600 hover:from-sky-400 hover:via-indigo-400 hover:to-blue-500 text-white font-black text-sm shadow-xl shadow-sky-500/25 hover:shadow-sky-500/40 hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
          >
            {loading ? (
              <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
            ) : (
              <>
                <UserPlus className="w-4 h-4" />
                <span>Create Free Account</span>
              </>
            )}
          </button>
        </form>

        <div className="pt-4 border-t border-slate-800 text-center space-y-3">
          <p className="text-xs text-slate-400">
            Already have an account?{' '}
            <Link to="/login" className="text-sky-400 hover:text-sky-300 font-bold ml-1 transition">
              Sign in
            </Link>
          </p>

          <div className="flex items-center justify-center gap-2 text-[11px] text-slate-500">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
            <span>Instant Access • No Hidden Booking Fees</span>
          </div>
        </div>
      </div>
    </div>
  );
};


