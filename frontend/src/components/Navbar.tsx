import React, { useState, useRef, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence, useScroll, useMotionValueEvent } from 'framer-motion';
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
import { dropdownVariants, mobileDrawerVariants, activePillTransition } from '../lib/motion';

export const Navbar: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotifications();

  const [showNotifications, setShowNotifications] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const [hidden, setHidden] = useState(false);
  const [showPushModal, setShowPushModal] = useState(false);

  const notifRef = useRef<HTMLDivElement>(null);
  const userMenuRef = useRef<HTMLDivElement>(null);
  const { scrollY } = useScroll();

  const isActive = (path: string) => {
    if (path === '/flights' && (location.pathname === '/' || location.pathname === '/flights')) return true;
    return location.pathname === path;
  };

  // Direction-aware scroll listener with Framer Motion scroll hooks
  useMotionValueEvent(scrollY, "change", (latest) => {
    const previous = scrollY.getPrevious() ?? 0;
    const diff = latest - previous;

    setIsScrolled(latest > 20);

    // Hide when scrolling down past 90px, reveal when scrolling up
    if (latest > 90 && diff > 8 && !mobileMenuOpen && !showNotifications && !showUserMenu) {
      setHidden(true);
    } else if (diff < -6 || latest <= 50) {
      setHidden(false);
    }
  });

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

  // Close mobile menu & dropdowns on route change
  useEffect(() => {
    setMobileMenuOpen(false);
    setShowNotifications(false);
    setShowUserMenu(false);
    setHidden(false);
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    setShowUserMenu(false);
    navigate('/');
  };

  const navLinks = [
    { path: '/flights', label: 'Flights', icon: Plane, pulse: false },
    { path: '/hotels', label: 'Hotels & Stays', icon: Building2, pulse: false },
    { path: '/live-tracker', label: 'Live Radar', icon: Radio, pulse: true },
    { path: '/offers', label: 'Deals & Offers', icon: Tag, pulse: false },
  ];

  return (
    <motion.header
      variants={{
        visible: { y: 0, opacity: 1 },
        hidden: { y: '-100%', opacity: 0.9 },
      }}
      animate={hidden ? 'hidden' : 'visible'}
      transition={{ duration: 0.3, ease: [0.22, 1, 0.36, 1] }}
      className={`sticky top-0 z-50 transition-colors duration-300 ${
        isScrolled
          ? 'bg-[#0B0C10]/95 backdrop-blur-xl border-b border-white/10 shadow-2xl py-2.5'
          : 'bg-[#0B0C10]/85 backdrop-blur-lg border-b border-white/5 py-3.5'
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between">
          {/* 1. BRAND LOGO */}
          <div className="flex items-center gap-8">
            <BrandLogo size="md" showTagline={true} withLink={true} />

            {/* 2. DESKTOP NAVIGATION WITH SHARED LAYOUT ID PILL */}
            <nav className="hidden lg:flex items-center gap-1.5 bg-[#14161F]/80 p-1.5 rounded-xl border border-white/10 shadow-inner">
              {navLinks.map((link) => {
                const IconComponent = link.icon;
                const active = isActive(link.path);

                return (
                  <Link
                    key={link.path}
                    to={link.path}
                    className={`relative px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-colors duration-200 flex items-center gap-2 ${
                      active ? 'text-black font-bold' : 'text-slate-300 hover:text-white hover:bg-white/5'
                    }`}
                  >
                    {active && (
                      <motion.div
                        layoutId="activeNavTabIndicator"
                        className="absolute inset-0 bg-gradient-to-r from-amber-400 to-amber-500 rounded-lg shadow-glow-gold -z-10"
                        transition={activePillTransition}
                      />
                    )}
                    <IconComponent
                      className={`w-3.5 h-3.5 relative z-10 transition-colors ${
                        active
                          ? 'text-black'
                          : link.path === '/offers'
                          ? 'text-accent'
                          : link.pulse
                          ? 'text-amber-400 animate-pulse'
                          : 'text-amber-400'
                      }`}
                    />
                    <span className="relative z-10">{link.label}</span>
                  </Link>
                );
              })}
            </nav>
          </div>

          {/* 3. RIGHT CONTROLS: NOTIFICATIONS & AUTH */}
          <div className="flex items-center gap-2.5 sm:gap-3">
            {/* Live Notifications Popover */}
            <div className="relative" ref={notifRef}>
              <motion.button
                whileTap={{ scale: 0.94 }}
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
              </motion.button>

              {/* Notification Popover Dropdown */}
              <AnimatePresence>
                {showNotifications && (
                  <motion.div
                    variants={dropdownVariants}
                    initial="hidden"
                    animate="visible"
                    exit="exit"
                    className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl bg-[#12131A] border border-white/10 shadow-2xl z-50 p-4 space-y-3 origin-top-right"
                  >
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
                          className="text-[11px] font-semibold text-secondary hover:underline flex items-center gap-1 transition"
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
                                ? 'bg-[#181A22]/50 border-white/5 text-slate-400 hover:bg-[#181A22]'
                                : 'bg-[#1A1C26] border-amber-500/30 text-slate-200 shadow-sm hover:border-amber-500/50'
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
                        className="text-[11px] text-slate-400 hover:text-white font-medium transition"
                      >
                        View All Alerts →
                      </Link>
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            {/* Authenticated User Menu or Sign In */}
            {isAuthenticated ? (
              <div className="relative" ref={userMenuRef}>
                <motion.button
                  whileTap={{ scale: 0.96 }}
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
                  <ChevronDown className={`w-3.5 h-3.5 text-slate-400 transition-transform duration-200 ${showUserMenu ? 'rotate-180' : ''}`} />
                </motion.button>

                {/* User Dropdown */}
                <AnimatePresence>
                  {showUserMenu && (
                    <motion.div
                      variants={dropdownVariants}
                      initial="hidden"
                      animate="visible"
                      exit="exit"
                      className="absolute right-0 mt-2 w-56 rounded-2xl bg-[#12131A] border border-white/10 shadow-2xl z-50 p-2 space-y-1 origin-top-right"
                    >
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
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="px-3.5 py-1.5 rounded-xl bg-[#14161F] hover:bg-[#1F222E] active:scale-95 text-slate-200 hover:text-white border border-white/10 text-xs font-semibold transition"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="hidden sm:inline-flex px-4 py-1.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 active:scale-95 text-black text-xs font-bold shadow-glow-gold transition"
                >
                  Get Started
                </Link>
              </div>
            )}

            {/* Mobile Hamburger Toggle */}
            <motion.button
              whileTap={{ scale: 0.92 }}
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="lg:hidden p-2.5 rounded-xl bg-[#14161F] text-slate-300 hover:text-white border border-white/10"
              aria-label="Toggle mobile menu"
            >
              {mobileMenuOpen ? <X className="w-5 h-5 text-amber-400" /> : <Menu className="w-5 h-5" />}
            </motion.button>
          </div>
        </div>
      </div>

      {/* 4. MOBILE NAVIGATION DRAWER WITH HEIGHT/OPACITY SPRING */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <motion.div
            variants={mobileDrawerVariants}
            initial="closed"
            animate="open"
            exit="closed"
            className="lg:hidden overflow-hidden border-t border-white/10 bg-[#0B0C10]/98 backdrop-blur-2xl px-4 py-4 space-y-3 shadow-2xl"
          >
            <nav className="space-y-1">
              {navLinks.map((link) => {
                const IconComponent = link.icon;
                const active = isActive(link.path);

                return (
                  <Link
                    key={link.path}
                    to={link.path}
                    className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                      active ? 'bg-amber-400 text-black font-bold shadow-glow-gold' : 'text-slate-300 hover:bg-white/5'
                    }`}
                  >
                    <IconComponent
                      className={`w-4 h-4 ${
                        active
                          ? 'text-black'
                          : link.path === '/offers'
                          ? 'text-accent'
                          : 'text-amber-400'
                      }`}
                    />
                    <span>{link.label}</span>
                  </Link>
                );
              })}

              {isAuthenticated && (
                <Link
                  to="/my-bookings"
                  className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                    isActive('/my-bookings') ? 'bg-amber-400 text-black font-bold shadow-glow-gold' : 'text-slate-300 hover:bg-white/5'
                  }`}
                >
                  <BookmarkCheck className={`w-4 h-4 ${isActive('/my-bookings') ? 'text-black' : 'text-amber-400'}`} />
                  <span>My Bookings</span>
                </Link>
              )}
            </nav>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Push Notification Setup Modal */}
      {showPushModal && (
        <PushNotificationModal isOpen={showPushModal} onClose={() => setShowPushModal(false)} />
      )}
    </motion.header>
  );
};
