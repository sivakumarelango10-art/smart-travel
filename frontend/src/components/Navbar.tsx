import React, { useState, useRef, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  Plane,
  Search,
  BookmarkCheck,
  Bell,
  LogIn,
  UserPlus,
  LogOut,
  CheckCheck,
  AlertCircle,
  Clock,
  Sparkles,
  ChevronDown
} from 'lucide-react';
import { APP_NAME } from '../config/constants';
import { useAuth } from '../context/AuthContext';
import { useNotifications } from '../context/NotificationContext';

export const Navbar: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotifications();

  const [showNotifications, setShowNotifications] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  const notifRef = useRef<HTMLDivElement>(null);
  const userMenuRef = useRef<HTMLDivElement>(null);

  const isActive = (path: string) => location.pathname === path;

  // Close dropdowns on outside click
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (notifRef.current && !notifRef.current.contains(event.target as Node)) {
        setShowNotifications(false);
      }
      if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
        setShowUserMenu(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    setShowUserMenu(false);
    navigate('/');
  };

  return (
    <header className="sticky top-0 z-50 backdrop-blur-md bg-slate-900/90 border-b border-slate-800/80 shadow-md shadow-black/20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand Logo */}
        <Link to="/" className="flex items-center gap-2.5 group">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-sky-500/20 group-hover:scale-105 transition-transform duration-200">
            <Plane className="w-5 h-5 text-white transform -rotate-45 group-hover:rotate-0 transition-transform duration-300" />
          </div>
          <div>
            <span className="font-bold text-lg text-white tracking-tight flex items-center gap-1.5">
              {APP_NAME}
              <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                PROD
              </span>
            </span>
            <p className="text-[11px] text-slate-400 -mt-0.5">Enterprise Flight Booking Platform</p>
          </div>
        </Link>

        {/* Center Nav Links */}
        <nav className="hidden md:flex items-center gap-1">
          <Link
            to="/"
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition duration-150 flex items-center gap-1.5 ${
              isActive('/')
                ? 'text-white bg-slate-800 border border-slate-700'
                : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
            }`}
          >
            <Search className="w-4 h-4 text-sky-400" />
            Search Flights
          </Link>

          {isAuthenticated && (
            <Link
              to="/my-bookings"
              className={`px-3 py-1.5 rounded-lg text-sm font-medium transition duration-150 flex items-center gap-1.5 ${
                isActive('/my-bookings')
                  ? 'text-white bg-slate-800 border border-slate-700'
                  : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <BookmarkCheck className="w-4 h-4 text-indigo-400" />
              My Bookings
            </Link>
          )}
        </nav>

        {/* Right Section: Notifications & User Auth */}
        <div className="flex items-center gap-2 sm:gap-3">
          {isAuthenticated ? (
            <>
              {/* Notification Popover */}
              <div className="relative" ref={notifRef}>
                <button
                  type="button"
                  onClick={() => setShowNotifications(!showNotifications)}
                  className="relative p-2 rounded-xl text-slate-300 hover:text-white hover:bg-slate-800 transition duration-150"
                  aria-label="Notifications"
                >
                  <Bell className="w-5 h-5" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1.5 right-1.5 w-4 h-4 rounded-full bg-rose-500 text-white text-[10px] font-bold flex items-center justify-center animate-pulse">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </button>

                {showNotifications && (
                  <div className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl overflow-hidden z-50">
                    <div className="p-3.5 border-b border-slate-800 flex items-center justify-between bg-slate-950/60">
                      <div className="flex items-center gap-2">
                        <Bell className="w-4 h-4 text-sky-400" />
                        <span className="font-semibold text-sm text-white">Notifications</span>
                        {unreadCount > 0 && (
                          <span className="px-1.5 py-0.5 rounded-full bg-sky-500/10 text-sky-400 text-[10px] font-bold border border-sky-500/20">
                            {unreadCount} new
                          </span>
                        )}
                      </div>
                      {unreadCount > 0 && (
                        <button
                          onClick={markAllAsRead}
                          className="text-xs text-sky-400 hover:text-sky-300 flex items-center gap-1 font-medium"
                        >
                          <CheckCheck className="w-3.5 h-3.5" />
                          Mark all read
                        </button>
                      )}
                    </div>

                    <div className="max-h-80 overflow-y-auto divide-y divide-slate-800/60">
                      {notifications.length === 0 ? (
                        <div className="py-8 text-center text-slate-500 text-xs">
                          No notifications right now.
                        </div>
                      ) : (
                        notifications.map((n) => (
                          <div
                            key={n.id}
                            onClick={() => !n.isRead && markAsRead(n.id)}
                            className={`p-3.5 transition duration-150 cursor-pointer ${
                              n.isRead ? 'bg-slate-900/40 hover:bg-slate-800/40' : 'bg-sky-950/20 hover:bg-sky-950/40 border-l-2 border-sky-500'
                            }`}
                          >
                            <div className="flex items-start gap-2.5">
                              <div className="mt-0.5">
                                {n.type.includes('CANCEL') || n.priority === 'URGENT' ? (
                                  <AlertCircle className="w-4 h-4 text-rose-400" />
                                ) : n.type.includes('DELAY') ? (
                                  <Clock className="w-4 h-4 text-amber-400" />
                                ) : (
                                  <Sparkles className="w-4 h-4 text-sky-400" />
                                )}
                              </div>
                              <div className="flex-1 min-w-0">
                                <p className={`text-xs font-semibold ${n.isRead ? 'text-slate-300' : 'text-white'}`}>
                                  {n.title}
                                </p>
                                <p className="text-[11px] text-slate-400 mt-0.5 line-clamp-2">
                                  {n.message}
                                </p>
                                <p className="text-[10px] text-slate-500 mt-1">
                                  {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </p>
                              </div>
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                )}
              </div>

              {/* User Avatar Menu */}
              <div className="relative" ref={userMenuRef}>
                <button
                  type="button"
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center gap-2 px-2.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-750 border border-slate-700 text-slate-200 transition"
                >
                  <div className="w-7 h-7 rounded-lg bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center text-white font-bold text-xs">
                    {user?.fullName?.charAt(0) || user?.email?.charAt(0) || 'U'}
                  </div>
                  <span className="hidden sm:inline text-xs font-medium text-slate-200 max-w-[120px] truncate">
                    {user?.fullName || user?.email}
                  </span>
                  <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
                </button>

                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-56 rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl py-1.5 overflow-hidden z-50">
                    <div className="px-3.5 py-2.5 border-b border-slate-800 bg-slate-950/40">
                      <p className="text-xs font-semibold text-white truncate">{user?.fullName}</p>
                      <p className="text-[11px] text-slate-400 truncate">{user?.email}</p>
                    </div>

                    <Link
                      to="/my-bookings"
                      onClick={() => setShowUserMenu(false)}
                      className="flex items-center gap-2 px-3.5 py-2 text-xs text-slate-300 hover:text-white hover:bg-slate-800 transition"
                    >
                      <BookmarkCheck className="w-4 h-4 text-indigo-400" />
                      My Bookings
                    </Link>

                    <button
                      onClick={handleLogout}
                      className="w-full flex items-center gap-2 px-3.5 py-2 text-xs text-rose-400 hover:bg-rose-500/10 transition text-left"
                    >
                      <LogOut className="w-4 h-4" />
                      Sign Out
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="px-3 py-1.5 rounded-lg text-xs sm:text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800 transition flex items-center gap-1.5"
              >
                <LogIn className="w-4 h-4" />
                Login
              </Link>
              <Link
                to="/register"
                className="px-3.5 py-1.5 rounded-lg text-xs sm:text-sm font-medium bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white shadow-md shadow-sky-500/20 transition flex items-center gap-1.5"
              >
                <UserPlus className="w-4 h-4" />
                Register
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
