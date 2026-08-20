import React, { useState, useRef, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  Plane,
  Building2,
  Compass,
  Tag,
  BookmarkCheck,
  Bell,
  LogIn,
  UserPlus,
  LogOut,
  CheckCheck,
  AlertCircle,
  Clock,
  Sparkles,
  ChevronDown,
  Shield,
  Menu,
  X,
  User
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useNotifications } from '../context/NotificationContext';
import { BrandLogo } from './BrandLogo';

export const Navbar: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotifications();

  const [showNotifications, setShowNotifications] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);

  const notifRef = useRef<HTMLDivElement>(null);
  const userMenuRef = useRef<HTMLDivElement>(null);

  const isActive = (path: string) => location.pathname === path;

  // Scroll listener for sticky navbar effect
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

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

  // Close mobile menu on route change
  useEffect(() => {
    setMobileMenuOpen(false);
    setShowNotifications(false);
    setShowUserMenu(false);
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    setShowUserMenu(false);
    navigate('/');
  };

  return (
    <header
      className={`sticky top-0 z-50 transition-all duration-300 ${
        isScrolled
          ? 'bg-slate-950/95 backdrop-blur-xl border-b border-slate-800/80 shadow-2xl shadow-black/40 py-2.5'
          : 'bg-gradient-to-b from-slate-950/90 to-slate-950/70 backdrop-blur-lg border-b border-white/5 py-3.5'
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between">
          {/* LEFT: Brand Logo */}
          <BrandLogo size="md" showBadge={true} withLink={true} />

          {/* CENTER: Navigation Links (Desktop) */}
          <nav className="hidden lg:flex items-center gap-1 bg-slate-900/60 p-1.5 rounded-2xl border border-white/10 backdrop-blur-md">
            <Link
              to="/flights"
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all duration-200 flex items-center gap-1.5 ${
                isActive('/flights') || isActive('/')
                  ? 'text-white bg-gradient-to-r from-sky-500/20 to-indigo-500/20 border border-sky-500/30 shadow-sm text-sky-300'
                  : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Plane className="w-3.5 h-3.5 text-sky-400" />
              <span>Flights</span>
            </Link>

            <Link
              to="/hotels"
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all duration-200 flex items-center gap-1.5 ${
                isActive('/hotels')
                  ? 'text-white bg-gradient-to-r from-sky-500/20 to-indigo-500/20 border border-sky-500/30 shadow-sm text-sky-300'
                  : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Building2 className="w-3.5 h-3.5 text-indigo-400" />
              <span>Stays & Hotels</span>
            </Link>

            <Link
              to="/tracked-flights"
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all duration-200 flex items-center gap-1.5 ${
                isActive('/tracked-flights')
                  ? 'text-white bg-gradient-to-r from-cyan-500/20 to-emerald-500/20 border border-cyan-500/30 shadow-sm text-cyan-300'
                  : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <span>Live Tracker</span>
            </Link>

            <a
              href="#destinations"
              onClick={(e) => {
                if (location.pathname === '/') {
                  e.preventDefault();
                  document.getElementById('destinations')?.scrollIntoView({ behavior: 'smooth' });
                }
              }}
              className="px-3.5 py-1.5 rounded-xl text-xs font-medium text-slate-300 hover:text-white hover:bg-slate-800/60 transition flex items-center gap-1.5"
            >
              <Compass className="w-3.5 h-3.5 text-indigo-400" />
              <span>Destinations</span>
            </a>

            <a
              href="#offers"
              onClick={(e) => {
                if (location.pathname === '/') {
                  e.preventDefault();
                  document.getElementById('offers')?.scrollIntoView({ behavior: 'smooth' });
                }
              }}
              className="px-3.5 py-1.5 rounded-xl text-xs font-medium text-slate-300 hover:text-white hover:bg-slate-800/60 transition flex items-center gap-1.5"
            >
              <Tag className="w-3.5 h-3.5 text-amber-400" />
              <span>Offers</span>
            </a>
          </nav>

          {/* RIGHT: Notifications & User Auth */}
          <div className="flex items-center gap-2 sm:gap-3">
            {isAuthenticated ? (
              <>
                {/* My Bookings Button */}
                <Link
                  to="/my-bookings"
                  className={`hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold transition border ${
                    isActive('/my-bookings')
                      ? 'bg-indigo-500/20 text-indigo-300 border-indigo-500/30'
                      : 'bg-slate-900/80 text-slate-300 hover:text-white hover:bg-slate-800 border-slate-800'
                  }`}
                >
                  <BookmarkCheck className="w-4 h-4 text-indigo-400" />
                  <span>My Bookings</span>
                </Link>

                {/* Notifications Popover */}
                <div className="relative" ref={notifRef}>
                  <button
                    type="button"
                    onClick={() => setShowNotifications(!showNotifications)}
                    className="relative p-2.5 rounded-xl bg-slate-900/80 hover:bg-slate-800 text-slate-300 hover:text-white border border-slate-800 transition duration-150"
                    aria-label="Notifications"
                  >
                    <Bell className="w-4 h-4" />
                    {unreadCount > 0 && (
                      <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-rose-500 text-white text-[9px] font-black flex items-center justify-center animate-pulse shadow-md shadow-rose-500/50">
                        {unreadCount > 9 ? '9+' : unreadCount}
                      </span>
                    )}
                  </button>

                  {showNotifications && (
                    <div className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl bg-slate-900/95 border border-slate-800 shadow-2xl overflow-hidden z-50 backdrop-blur-2xl animate-fade-in">
                      <div className="p-3.5 border-b border-slate-800 flex items-center justify-between bg-slate-950/80">
                        <div className="flex items-center gap-2">
                          <Bell className="w-4 h-4 text-sky-400" />
                          <span className="font-bold text-sm text-white">Notifications</span>
                          {unreadCount > 0 && (
                            <span className="px-2 py-0.5 rounded-full bg-sky-500/15 text-sky-400 text-[10px] font-bold border border-sky-500/30">
                              {unreadCount} unread
                            </span>
                          )}
                        </div>
                        {unreadCount > 0 && (
                          <button
                            onClick={markAllAsRead}
                            className="text-xs text-sky-400 hover:text-sky-300 flex items-center gap-1 font-semibold"
                          >
                            <CheckCheck className="w-3.5 h-3.5" />
                            Mark all read
                          </button>
                        )}
                      </div>

                      <div className="max-h-80 overflow-y-auto divide-y divide-slate-800/60">
                        {notifications.length === 0 ? (
                          <div className="py-10 text-center text-slate-500 text-xs flex flex-col items-center gap-2">
                            <Sparkles className="w-6 h-6 text-slate-600" />
                            <span>No notifications right now</span>
                          </div>
                        ) : (
                          notifications.map((n) => (
                            <div
                              key={n.id}
                              onClick={() => !n.isRead && markAsRead(n.id)}
                              className={`p-3.5 transition duration-150 cursor-pointer ${
                                n.isRead
                                  ? 'bg-slate-900/40 hover:bg-slate-800/40'
                                  : 'bg-sky-950/30 hover:bg-sky-950/50 border-l-2 border-sky-500'
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

                {/* User Profile Dropdown */}
                <div className="relative" ref={userMenuRef}>
                  <button
                    type="button"
                    onClick={() => setShowUserMenu(!showUserMenu)}
                    className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl bg-slate-900/90 hover:bg-slate-850 border border-slate-800 text-slate-200 transition shadow-sm"
                  >
                    <div className="w-7 h-7 rounded-lg bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center text-white font-black text-xs shadow-md">
                      {user?.fullName?.charAt(0).toUpperCase() || user?.email?.charAt(0).toUpperCase() || 'U'}
                    </div>
                    <span className="hidden sm:inline text-xs font-semibold text-slate-200 max-w-[120px] truncate">
                      {user?.fullName || user?.email}
                    </span>
                    <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
                  </button>

                  {showUserMenu && (
                    <div className="absolute right-0 mt-2 w-60 rounded-2xl bg-slate-900/95 border border-slate-800 shadow-2xl py-1.5 overflow-hidden z-50 backdrop-blur-xl animate-fade-in">
                      <div className="px-4 py-3 border-b border-slate-800 bg-slate-950/60">
                        <p className="text-xs font-bold text-white truncate">{user?.fullName || 'Traveler'}</p>
                        <p className="text-[11px] text-slate-400 truncate">{user?.email}</p>
                        {isAdmin && (
                          <span className="inline-flex mt-1.5 px-2 py-0.5 rounded-full bg-rose-500/15 text-rose-400 border border-rose-500/30 text-[9px] font-black">
                            ADMINISTRATOR
                          </span>
                        )}
                      </div>

                      <Link
                        to="/account"
                        onClick={() => setShowUserMenu(false)}
                        className="flex items-center gap-2.5 px-4 py-2.5 text-xs text-slate-300 hover:text-white hover:bg-slate-800/80 transition"
                      >
                        <User className="w-4 h-4 text-sky-400" />
                        <span>My Account</span>
                      </Link>

                      <Link
                        to="/my-bookings"
                        onClick={() => setShowUserMenu(false)}
                        className="flex items-center gap-2.5 px-4 py-2.5 text-xs text-slate-300 hover:text-white hover:bg-slate-800/80 transition"
                      >
                        <BookmarkCheck className="w-4 h-4 text-indigo-400" />
                        <span>My Bookings</span>
                      </Link>

                      {isAdmin && (
                        <Link
                          to="/admin"
                          onClick={() => setShowUserMenu(false)}
                          className="flex items-center gap-2.5 px-4 py-2.5 text-xs text-sky-400 hover:text-sky-300 hover:bg-slate-800/80 transition font-semibold"
                        >
                          <Shield className="w-4 h-4 text-sky-400" />
                          <span>Admin Control Center</span>
                        </Link>
                      )}

                      <div className="border-t border-slate-800/80 my-1"></div>

                      <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-2.5 px-4 py-2.5 text-xs text-rose-400 hover:bg-rose-500/10 transition text-left font-medium"
                      >
                        <LogOut className="w-4 h-4" />
                        <span>Sign Out</span>
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="px-3.5 py-2 rounded-xl text-xs sm:text-sm font-semibold text-slate-200 hover:text-white hover:bg-slate-800/80 border border-transparent hover:border-slate-700 transition flex items-center gap-1.5"
                >
                  <LogIn className="w-4 h-4 text-sky-400" />
                  <span>Sign In</span>
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 rounded-xl text-xs sm:text-sm font-bold bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white shadow-lg shadow-sky-500/25 hover:shadow-sky-500/40 transition flex items-center gap-1.5"
                >
                  <UserPlus className="w-4 h-4" />
                  <span>Create Account</span>
                </Link>
              </div>
            )}

            {/* Mobile Hamburger Menu Button */}
            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="lg:hidden p-2 rounded-xl bg-slate-900/80 border border-slate-800 text-slate-300 hover:text-white transition ml-1"
              aria-label="Toggle Menu"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Mobile Navigation Drawer */}
        {mobileMenuOpen && (
          <div className="lg:hidden mt-3 pt-3 border-t border-slate-800/80 space-y-2 pb-2 animate-fade-in">
            <Link
              to="/flights"
              onClick={() => setMobileMenuOpen(false)}
              className="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold bg-slate-900/80 text-white border border-slate-800"
            >
              <Plane className="w-4 h-4 text-sky-400" />
              <span>Search Flights</span>
            </Link>

            <Link
              to="/hotels"
              onClick={() => setMobileMenuOpen(false)}
              className="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold bg-slate-900/80 text-white border border-slate-800"
            >
              <Building2 className="w-4 h-4 text-indigo-400" />
              <span>Stays & Hotels</span>
            </Link>

            <Link
              to="/tracked-flights"
              onClick={() => setMobileMenuOpen(false)}
              className="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold bg-slate-900/80 text-white border border-slate-800"
            >
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <span>Live Flight Tracker</span>
            </Link>

            {isAuthenticated && (
              <>
                <Link
                  to="/account"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold bg-slate-900/80 text-white border border-slate-800"
                >
                  <User className="w-4 h-4 text-sky-400" />
                  <span>My Account</span>
                </Link>

                <Link
                  to="/my-bookings"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold bg-slate-900/80 text-white border border-slate-800"
                >
                  <BookmarkCheck className="w-4 h-4 text-indigo-400" />
                  <span>My Bookings</span>
                </Link>
              </>
            )}

            {isAuthenticated && isAdmin && (
              <Link
                to="/admin"
                className="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold bg-sky-950/40 text-sky-300 border border-sky-500/30"
              >
                <Shield className="w-4 h-4 text-sky-400" />
                <span>Admin Portal</span>
              </Link>
            )}

            <a
              href="#destinations"
              onClick={() => setMobileMenuOpen(false)}
              className="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold text-slate-300 hover:bg-slate-900"
            >
              <Compass className="w-4 h-4 text-indigo-400" />
              <span>Popular Destinations</span>
            </a>

            <a
              href="#offers"
              onClick={() => setMobileMenuOpen(false)}
              className="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold text-slate-300 hover:bg-slate-900"
            >
              <Tag className="w-4 h-4 text-amber-400" />
              <span>Exclusive Offers</span>
            </a>
          </div>
        )}
      </div>
    </header>
  );
};

