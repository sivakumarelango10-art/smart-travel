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
      className={`sticky top-0 z-50 transition-all duration-300 ${
        isScrolled
          ? 'bg-[#0B0C10]/95 backdrop-blur-xl border-b border-white/10 shadow-2xl py-2.5'
          : 'bg-[#0B0C10]/80 backdrop-blur-lg border-b border-white/5 py-3.5'
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between">
          {/* 1. BRAND LOGO */}
          <div className="flex items-center gap-8">
            <BrandLogo size="md" showTagline={true} withLink={true} />

            {/* 2. DESKTOP NAVIGATION */}
            <nav className="hidden lg:flex items-center gap-1.5 bg-[#14161F]/80 p-1.5 rounded-xl border border-white/10 shadow-inner">
              <Link
                to="/flights"
                className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all duration-150 flex items-center gap-2 ${
                  isActive('/flights')
                    ? 'text-black bg-gradient-to-r from-amber-400 to-amber-500 font-bold shadow-glow-gold'
                    : 'text-slate-300 hover:text-white hover:bg-white/5'
                }`}
              >
                <Plane className={`w-3.5 h-3.5 ${isActive('/flights') ? 'text-black' : 'text-amber-400'}`} />
                <span>Flights</span>
              </Link>

              <Link
                to="/hotels"
                className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all duration-150 flex items-center gap-2 ${
                  isActive('/hotels')
                    ? 'text-black bg-gradient-to-r from-amber-400 to-amber-500 font-bold shadow-glow-gold'
                    : 'text-slate-300 hover:text-white hover:bg-white/5'
                }`}
              >
                <Building2 className={`w-3.5 h-3.5 ${isActive('/hotels') ? 'text-black' : 'text-amber-400'}`} />
                <span>Hotels & Stays</span>
              </Link>

              <Link
                to="/live-tracker"
                className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all duration-150 flex items-center gap-2 ${
                  isActive('/live-tracker')
                    ? 'text-black bg-gradient-to-r from-amber-400 to-amber-500 font-bold shadow-glow-gold'
                    : 'text-slate-300 hover:text-white hover:bg-white/5'
                }`}
              >
                <Radio className={`w-3.5 h-3.5 ${isActive('/live-tracker') ? 'text-black' : 'text-amber-400 animate-pulse'}`} />
                <span>Live Radar</span>
              </Link>

              <Link
                to="/offers"
                className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all duration-150 flex items-center gap-2 ${
                  isActive('/offers')
                    ? 'text-black bg-gradient-to-r from-amber-400 to-amber-500 font-bold shadow-glow-gold'
                    : 'text-slate-300 hover:text-white hover:bg-white/5'
                }`}
              >
                <Tag className={`w-3.5 h-3.5 ${isActive('/offers') ? 'text-black' : 'text-accent'}`} />
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
                className="relative p-2.5 rounded-xl bg-[#14161F] hover:bg-[#1F222E] text-slate-300 hover:text-white border border-white/10 transition shadow-sm"
                aria-label="View notifications"
              >
                <Bell className="w-4 h-4 text-amber-400" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 w-4 h-4 bg-accent text-white text-[10px] font-black rounded-full flex items-center justify-center shadow-md animate-pulse">
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </button>

              {/* Notification Popover Dropdown */}
              {showNotifications && (
                <div className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl bg-[#12131A] border border-white/10 shadow-2xl z-50 p-4 space-y-3 dropdown-enter">
                  <div className="flex items-center justify-between border-b border-white/10 pb-3">
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
                              ? 'bg-[#181A22]/50 border-white/5 text-slate-400'
                              : 'bg-[#1A1C26] border-amber-500/30 text-slate-200 shadow-sm'
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

                  <div className="pt-2 border-t border-white/10 flex items-center justify-between text-xs">
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
                  className="flex items-center gap-2 p-1.5 sm:px-3 sm:py-1.5 rounded-xl bg-[#14161F] hover:bg-[#1F222E] text-white border border-white/10 text-xs font-semibold transition"
                >
                  <div className="w-6 h-6 rounded-lg bg-amber-400/20 border border-amber-400/40 text-amber-400 font-black flex items-center justify-center text-xs">
                    {user?.fullName?.charAt(0) || user?.email?.charAt(0) || 'U'}
                  </div>
                  <span className="hidden sm:inline font-medium max-w-[120px] truncate">
                    {user?.fullName || user?.email?.split('@')[0]}
                  </span>
                  <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
                </button>

                {/* User Dropdown */}
                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-56 rounded-2xl bg-[#12131A] border border-white/10 shadow-2xl z-50 p-2 space-y-1 dropdown-enter">
                    <div className="px-3 py-2 border-b border-white/10">
                      <div className="text-xs font-bold text-white truncate">{user?.fullName || 'Traveler'}</div>
                      <div className="text-[11px] text-slate-400 truncate">{user?.email}</div>
                    </div>

                    <Link
                      to="/my-bookings"
                      className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-medium text-slate-300 hover:text-white hover:bg-white/5 transition"
                    >
                      <BookmarkCheck className="w-4 h-4 text-amber-400" />
                      <span>My Bookings & Trips</span>
                    </Link>

                    <Link
                      to="/account"
                      className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-medium text-slate-300 hover:text-white hover:bg-white/5 transition"
                    >
                      <User className="w-4 h-4 text-amber-400" />
                      <span>Account & Preferences</span>
                    </Link>

                    {isAdmin && (
                      <Link
                        to="/admin"
                        className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-medium text-accent hover:bg-white/5 transition"
                      >
                        <Shield className="w-4 h-4" />
                        <span>Admin Dashboard</span>
                      </Link>
                    )}

                    <div className="pt-1 border-t border-white/10">
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
                  className="px-3.5 py-1.5 rounded-xl bg-[#14161F] hover:bg-[#1F222E] text-slate-200 hover:text-white border border-white/10 text-xs font-semibold transition"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="hidden sm:inline-flex px-4 py-1.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black text-xs font-bold shadow-glow-gold transition"
                >
                  Get Started
                </Link>
              </div>
            )}

            {/* Mobile Hamburger Toggle */}
            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="lg:hidden p-2.5 rounded-xl bg-[#14161F] text-slate-300 hover:text-white border border-white/10"
              aria-label="Toggle mobile menu"
            >
              {mobileMenuOpen ? <X className="w-5 h-5 text-amber-400" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>
      </div>

      {/* 4. MOBILE NAVIGATION DRAWER */}
      {mobileMenuOpen && (
        <div className="lg:hidden border-t border-white/10 bg-[#0B0C10]/98 backdrop-blur-2xl px-4 py-4 space-y-3 animate-fade-in shadow-2xl">
          <nav className="space-y-1">
            <Link
              to="/flights"
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold ${
                isActive('/flights') ? 'bg-amber-400 text-black font-bold' : 'text-slate-300 hover:bg-white/5'
              }`}
            >
              <Plane className={`w-4 h-4 ${isActive('/flights') ? 'text-black' : 'text-amber-400'}`} />
              <span>Flights</span>
            </Link>

            <Link
              to="/hotels"
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold ${
                isActive('/hotels') ? 'bg-amber-400 text-black font-bold' : 'text-slate-300 hover:bg-white/5'
              }`}
            >
              <Building2 className={`w-4 h-4 ${isActive('/hotels') ? 'text-black' : 'text-amber-400'}`} />
              <span>Hotels & Stays</span>
            </Link>

            <Link
              to="/live-tracker"
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold ${
                isActive('/live-tracker') ? 'bg-amber-400 text-black font-bold' : 'text-slate-300 hover:bg-white/5'
              }`}
            >
              <Radio className={`w-4 h-4 ${isActive('/live-tracker') ? 'text-black' : 'text-amber-400 animate-pulse'}`} />
              <span>Live Flight Radar</span>
            </Link>

            <Link
              to="/offers"
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold ${
                isActive('/offers') ? 'bg-amber-400 text-black font-bold' : 'text-slate-300 hover:bg-white/5'
              }`}
            >
              <Tag className={`w-4 h-4 ${isActive('/offers') ? 'text-black' : 'text-accent'}`} />
              <span>Deals & Offers</span>
            </Link>

            {isAuthenticated && (
              <Link
                to="/my-bookings"
                className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold ${
                  isActive('/my-bookings') ? 'bg-amber-400 text-black font-bold' : 'text-slate-300 hover:bg-white/5'
                }`}
              >
                <BookmarkCheck className={`w-4 h-4 ${isActive('/my-bookings') ? 'text-black' : 'text-amber-400'}`} />
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
