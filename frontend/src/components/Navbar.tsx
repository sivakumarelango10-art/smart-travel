import React, { useState, useRef, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  Plane,
  Building2,
  Tag,
  BookmarkCheck,
  Bell,
  LogOut,
  CheckCheck,
  Clock,
  ChevronDown,
  Shield,
  Menu,
  X,
  User,
  Radio
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useNotifications } from '../context/NotificationContext';
import { BrandLogo } from './BrandLogo';
import { PushNotificationModal } from './PushNotificationModal';

export const Navbar: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotifications();

  const [showNotifications, setShowNotifications] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const [showPushModal, setShowPushModal] = useState(false);

  const notifRef = useRef<HTMLDivElement>(null);
  const userMenuRef = useRef<HTMLDivElement>(null);

  const isActive = (path: string) => {
    if (path === '/flights' && (location.pathname === '/' || location.pathname === '/flights')) return true;
    return location.pathname === path;
  };

  // Scroll listener for subtle header elevation
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 15);
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
      className={`sticky top-0 z-50 transition-all duration-200 ${
        isScrolled
          ? 'bg-primary/95 backdrop-blur-md border-b border-slate-800 shadow-md py-2.5'
          : 'bg-primary border-b border-slate-800/80 py-3'
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between">
          {/* 1. BRAND LOGO */}
          <div className="flex items-center gap-8">
            <BrandLogo size="md" showTagline={true} withLink={true} />

            {/* 2. DESKTOP NAVIGATION */}
            <nav className="hidden lg:flex items-center gap-1 bg-slate-950/60 p-1 rounded-xl border border-slate-800">
              <Link
                to="/flights"
                className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all duration-150 flex items-center gap-2 ${
                  isActive('/flights')
                    ? 'text-white bg-slate-800 border border-slate-700 shadow-sm'
                    : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
                }`}
              >
                <Plane className={`w-3.5 h-3.5 ${isActive('/flights') ? 'text-secondary' : 'text-slate-400'}`} />
                <span>Flights</span>
              </Link>

              <Link
                to="/hotels"
                className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all duration-150 flex items-center gap-2 ${
                  isActive('/hotels')
                    ? 'text-white bg-slate-800 border border-slate-700 shadow-sm'
                    : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
                }`}
              >
                <Building2 className={`w-3.5 h-3.5 ${isActive('/hotels') ? 'text-secondary' : 'text-slate-400'}`} />
                <span>Hotels & Stays</span>
              </Link>

              <Link
                to="/live-tracker"
                className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all duration-150 flex items-center gap-2 ${
                  isActive('/live-tracker')
                    ? 'text-white bg-slate-800 border border-slate-700 shadow-sm'
                    : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
                }`}
              >
                <Radio className="w-3.5 h-3.5 text-secondary animate-pulse" />
                <span>Live Radar</span>
              </Link>

              <Link
                to="/offers"
                className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all duration-150 flex items-center gap-2 ${
                  isActive('/offers')
                    ? 'text-white bg-slate-800 border border-slate-700 shadow-sm'
                    : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
                }`}
              >
                <Tag className="w-3.5 h-3.5 text-accent" />
                <span>Deals & Offers</span>
              </Link>
            </nav>
          </div>

          {/* 3. RIGHT CONTROLS: NOTIFICATIONS & AUTH */}
          <div className="flex items-center gap-2.5 sm:gap-3">
            {/* Live Notifications Popover */}
            <div className="relative" ref={notifRef}>
              <button
                type="button"
                onClick={() => setShowNotifications(!showNotifications)}
                className="relative p-2 rounded-xl bg-slate-800/80 hover:bg-slate-800 text-slate-300 hover:text-white border border-slate-700 transition"
                aria-label="View notifications"
              >
                <Bell className="w-4 h-4" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 w-4 h-4 bg-accent text-white text-[10px] font-black rounded-full flex items-center justify-center shadow-sm animate-pulse">
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </button>

              {/* Notification Popover Dropdown */}
              {showNotifications && (
                <div className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl bg-primary border border-slate-800 shadow-2xl z-50 p-4 space-y-3 dropdown-enter">
                  <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-white">Travel Alerts & Updates</span>
                      {unreadCount > 0 && (
                        <span className="px-2 py-0.5 rounded-full bg-accent/20 text-accent font-bold text-[10px]">
                          {unreadCount} new
                        </span>
                      )}
                    </div>
                    {unreadCount > 0 && (
                      <button
                        type="button"
                        onClick={markAllAsRead}
                        className="text-[11px] font-semibold text-secondary hover:underline flex items-center gap-1"
                      >
                        <CheckCheck className="w-3 h-3" /> Mark all read
                      </button>
                    )}
                  </div>

                  <div className="max-h-72 overflow-y-auto space-y-2 pr-1">
                    {notifications.length === 0 ? (
                      <div className="py-8 text-center text-xs text-slate-400">
                        <Clock className="w-8 h-8 text-slate-600 mx-auto mb-2" />
                        <p className="font-semibold text-slate-300">No new travel notifications</p>
                        <p className="text-[11px] text-slate-500 mt-0.5">Flight status and gate updates will appear here.</p>
                      </div>
                    ) : (
                      notifications.slice(0, 5).map((notif) => (
                        <div
                          key={notif.id}
                          onClick={() => markAsRead(notif.id)}
                          className={`p-2.5 rounded-xl border text-xs cursor-pointer transition ${
                            notif.isRead
                              ? 'bg-slate-900/50 border-slate-800 text-slate-400'
                              : 'bg-slate-900 border-slate-700 text-slate-200'
                          }`}
                        >
                          <div className="flex items-start justify-between gap-2">
                            <span className="font-bold text-white text-xs">{notif.title}</span>
                            <span className="text-[10px] text-slate-500 shrink-0">
                              {new Date(notif.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          </div>
                          <p className="text-[11px] text-slate-300 mt-1 leading-snug">{notif.message}</p>
                        </div>
                      ))
                    )}
                  </div>

                  <div className="pt-2 border-t border-slate-800 flex items-center justify-between text-xs">
                    <button
                      type="button"
                      onClick={() => {
                        setShowNotifications(false);
                        setShowPushModal(true);
                      }}
                      className="text-[11px] text-secondary hover:underline font-semibold"
                    >
                      Push Alert Settings
                    </button>
                    <Link
                      to="/account"
                      onClick={() => setShowNotifications(false)}
                      className="text-[11px] text-slate-400 hover:text-white font-medium"
                    >
                      View All Alerts →
                    </Link>
                  </div>
                </div>
              )}
            </div>

            {/* Authenticated User Menu or Sign In */}
            {isAuthenticated ? (
              <div className="relative" ref={userMenuRef}>
                <button
                  type="button"
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center gap-2 p-1.5 sm:px-3 sm:py-1.5 rounded-xl bg-slate-800/90 hover:bg-slate-800 text-white border border-slate-700 text-xs font-semibold transition"
                >
                  <div className="w-6 h-6 rounded-lg bg-secondary/20 border border-secondary/40 text-secondary font-bold flex items-center justify-center text-xs">
                    {user?.fullName?.charAt(0) || user?.email?.charAt(0) || 'U'}
                  </div>
                  <span className="hidden sm:inline font-medium max-w-[120px] truncate">
                    {user?.fullName || user?.email?.split('@')[0]}
                  </span>
                  <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
                </button>

                {/* User Dropdown */}
                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-56 rounded-2xl bg-primary border border-slate-800 shadow-2xl z-50 p-2 space-y-1 dropdown-enter">
                    <div className="px-3 py-2 border-b border-slate-800">
                      <div className="text-xs font-bold text-white truncate">{user?.fullName || 'Traveler'}</div>
                      <div className="text-[11px] text-slate-400 truncate">{user?.email}</div>
                    </div>

                    <Link
                      to="/my-bookings"
                      className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-medium text-slate-300 hover:text-white hover:bg-slate-800 transition"
                    >
                      <BookmarkCheck className="w-4 h-4 text-secondary" />
                      <span>My Bookings & Trips</span>
                    </Link>

                    <Link
                      to="/account"
                      className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-medium text-slate-300 hover:text-white hover:bg-slate-800 transition"
                    >
                      <User className="w-4 h-4 text-secondary" />
                      <span>Account & Preferences</span>
                    </Link>

                    {isAdmin && (
                      <Link
                        to="/admin"
                        className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-medium text-accent hover:bg-slate-800 transition"
                      >
                        <Shield className="w-4 h-4" />
                        <span>Admin Dashboard</span>
                      </Link>
                    )}

                    <div className="pt-1 border-t border-slate-800">
                      <button
                        type="button"
                        onClick={handleLogout}
                        className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-medium text-rose-400 hover:bg-rose-500/10 transition text-left"
                      >
                        <LogOut className="w-4 h-4" />
                        <span>Sign Out</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="px-3.5 py-1.5 rounded-xl bg-slate-800/90 hover:bg-slate-800 text-slate-200 hover:text-white border border-slate-700 text-xs font-semibold transition"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="hidden sm:inline-flex px-3.5 py-1.5 rounded-xl bg-secondary hover:bg-secondary-hover text-white text-xs font-bold shadow-sm shadow-secondary/30 transition"
                >
                  Get Started
                </Link>
              </div>
            )}

            {/* Mobile Hamburger Toggle */}
            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="lg:hidden p-2 rounded-xl bg-slate-800/80 text-slate-300 hover:text-white border border-slate-700"
              aria-label="Toggle mobile menu"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>
      </div>

      {/* 4. MOBILE NAVIGATION DRAWER */}
      {mobileMenuOpen && (
        <div className="lg:hidden border-t border-slate-800 bg-primary/98 backdrop-blur-xl px-4 py-4 space-y-3 animate-fade-in">
          <nav className="space-y-1">
            <Link
              to="/flights"
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold ${
                isActive('/flights') ? 'bg-slate-800 text-white' : 'text-slate-300 hover:bg-slate-850'
              }`}
            >
              <Plane className="w-4 h-4 text-secondary" />
              <span>Flights</span>
            </Link>

            <Link
              to="/hotels"
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold ${
                isActive('/hotels') ? 'bg-slate-800 text-white' : 'text-slate-300 hover:bg-slate-850'
              }`}
            >
              <Building2 className="w-4 h-4 text-secondary" />
              <span>Hotels & Stays</span>
            </Link>

            <Link
              to="/live-tracker"
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold ${
                isActive('/live-tracker') ? 'bg-slate-800 text-white' : 'text-slate-300 hover:bg-slate-850'
              }`}
            >
              <Radio className="w-4 h-4 text-secondary animate-pulse" />
              <span>Live Flight Radar</span>
            </Link>

            <Link
              to="/offers"
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold ${
                isActive('/offers') ? 'bg-slate-800 text-white' : 'text-slate-300 hover:bg-slate-850'
              }`}
            >
              <Tag className="w-4 h-4 text-accent" />
              <span>Deals & Offers</span>
            </Link>

            {isAuthenticated && (
              <Link
                to="/my-bookings"
                className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold ${
                  isActive('/my-bookings') ? 'bg-slate-800 text-white' : 'text-slate-300 hover:bg-slate-850'
                }`}
              >
                <BookmarkCheck className="w-4 h-4 text-secondary" />
                <span>My Bookings</span>
              </Link>
            )}
          </nav>
        </div>
      )}

      {/* Push Notification Setup Modal */}
      {showPushModal && (
        <PushNotificationModal isOpen={showPushModal} onClose={() => setShowPushModal(false)} />
      )}
    </header>
  );
};
