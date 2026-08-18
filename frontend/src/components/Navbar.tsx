import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Plane, LayoutDashboard, LogIn, UserPlus } from 'lucide-react';
import { APP_NAME } from '../config/constants';

export const Navbar: React.FC = () => {
  const location = useLocation();

  const isActive = (path: string) => location.pathname === path;

  return (
    <header className="sticky top-0 z-50 backdrop-blur-md bg-slate-900/80 border-b border-slate-800/80">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand Logo */}
        <Link to="/" className="flex items-center gap-2.5 group">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-sky-500/20 group-hover:scale-105 transition-transform duration-200">
            <Plane className="w-5 h-5 text-white" />
          </div>
          <div>
            <span className="font-bold text-lg text-white tracking-tight flex items-center gap-1.5">
              {APP_NAME}
              <span className="text-[10px] uppercase font-semibold px-2 py-0.5 rounded-full bg-sky-500/10 text-sky-400 border border-sky-500/20">
                Phase 1
              </span>
            </span>
            <p className="text-[11px] text-slate-400 -mt-0.5">Enterprise Travel Ecosystem</p>
          </div>
        </Link>

        {/* Navigation Links */}
        <nav className="flex items-center gap-1 sm:gap-2">
          <Link
            to="/"
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition duration-150 ${
              isActive('/')
                ? 'text-white bg-slate-800 border border-slate-700'
                : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
            }`}
          >
            Overview
          </Link>

          <Link
            to="/dashboard"
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition duration-150 flex items-center gap-1.5 ${
              isActive('/dashboard')
                ? 'text-white bg-slate-800 border border-slate-700'
                : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
            }`}
          >
            <LayoutDashboard className="w-4 h-4 text-sky-400" />
            Dashboard
          </Link>

          <div className="h-4 w-px bg-slate-800 mx-1 hidden sm:block"></div>

          <Link
            to="/login"
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition duration-150 flex items-center gap-1.5 ${
              isActive('/login')
                ? 'text-white bg-slate-800 border border-slate-700'
                : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
            }`}
          >
            <LogIn className="w-4 h-4" />
            Login
          </Link>

          <Link
            to="/register"
            className="px-3.5 py-1.5 rounded-lg text-sm font-medium bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white shadow-md shadow-sky-500/20 transition duration-150 flex items-center gap-1.5"
          >
            <UserPlus className="w-4 h-4" />
            Register
          </Link>
        </nav>
      </div>
    </header>
  );
};
