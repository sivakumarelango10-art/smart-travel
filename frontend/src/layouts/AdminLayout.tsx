import React, { useState } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import {
  LayoutDashboard, Plane, BookmarkCheck, RotateCcw,
  Ticket, QrCode, Zap, Bell, Activity, Shield,
  LogOut, ChevronLeft, Menu, X, ChevronRight, Star
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { AdminToastProvider } from '../components/admin/AdminToast';
import { BrandLogo } from '../components/BrandLogo';
import { PageTransition } from '../components/PageTransition';

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
  badge?: string;
}

const navItems: NavItem[] = [
  { label: 'Dashboard',      path: '/admin',               icon: <LayoutDashboard className="w-4 h-4" /> },
  { label: 'Flights',        path: '/admin/flights',        icon: <Plane className="w-4 h-4" /> },
  { label: 'Bookings',       path: '/admin/bookings',       icon: <BookmarkCheck className="w-4 h-4" /> },
  { label: 'Refunds',        path: '/admin/refunds',        icon: <RotateCcw className="w-4 h-4" /> },
  { label: 'Reviews',        path: '/admin/reviews',        icon: <Star className="w-4 h-4" /> },
  { label: 'Tickets',        path: '/admin/tickets',        icon: <Ticket className="w-4 h-4" /> },
  { label: 'Check-In & QR',  path: '/admin/checkins',       icon: <QrCode className="w-4 h-4" /> },
  { label: 'Disruptions',    path: '/admin/disruptions',    icon: <Zap className="w-4 h-4" /> },
  { label: 'Notifications',  path: '/admin/notifications',  icon: <Bell className="w-4 h-4" /> },
  { label: 'System Health',  path: '/admin/system',         icon: <Activity className="w-4 h-4" /> },
];

const NavLink: React.FC<{ item: NavItem; collapsed: boolean; onClick?: () => void }> = ({ item, collapsed, onClick }) => {
  const location = useLocation();
  const isActive = item.path === '/admin'
    ? location.pathname === '/admin'
    : location.pathname.startsWith(item.path);

  return (
    <Link
      to={item.path}
      onClick={onClick}
      title={collapsed ? item.label : undefined}
      className={`flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all duration-150 group ${
        isActive
          ? 'bg-sky-500/15 text-sky-400 border border-sky-500/20'
          : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
      }`}
    >
      <span className={`flex-shrink-0 ${isActive ? 'text-sky-400' : 'text-slate-500 group-hover:text-slate-300'}`}>
        {item.icon}
      </span>
      {!collapsed && <span className="truncate">{item.label}</span>}
      {!collapsed && item.badge && (
        <span className="ml-auto px-1.5 py-0.5 text-[10px] font-bold bg-rose-500 text-white rounded-full">
          {item.badge}
        </span>
      )}
    </Link>
  );
};

export const AdminLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const sidebarContent = (collapsed: boolean, onNavClick?: () => void) => (
    <div className="flex flex-col h-full">
      {/* Brand */}
      <div className={`flex items-center gap-3 px-4 py-3.5 border-b border-slate-800 ${collapsed ? 'justify-center' : ''}`}>
        <BrandLogo size="xs" withLink={false} />
        {!collapsed && (
          <span className="text-[10px] uppercase font-black px-2 py-0.5 rounded-md bg-rose-500/15 text-rose-400 border border-rose-500/30 tracking-wider ml-auto">
            ADMIN
          </span>
        )}
      </div>

      {/* Nav Items */}
      <nav className="flex-1 overflow-y-auto p-3 space-y-1">
        {navItems.map(item => (
          <NavLink key={item.path} item={item} collapsed={collapsed} onClick={onNavClick} />
        ))}
      </nav>

      {/* User Info + Logout */}
      <div className="border-t border-slate-800 p-3 space-y-1">
        {!collapsed && (
          <div className="px-3 py-2 mb-1">
            <p className="text-xs font-semibold text-white truncate">{user?.fullName}</p>
            <p className="text-[10px] text-slate-400 truncate">{user?.email}</p>
            <span className="inline-flex mt-1 items-center px-1.5 py-0.5 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/20 text-[10px] font-bold">
              ADMIN
            </span>
          </div>
        )}
        <Link
          to="/"
          className="flex items-center gap-3 px-3 py-2 text-sm text-slate-400 hover:text-white hover:bg-slate-800/60 rounded-xl transition"
          title={collapsed ? 'Customer View' : undefined}
        >
          <Plane className="w-4 h-4 flex-shrink-0" />
          {!collapsed && 'Customer View'}
        </Link>
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-3 py-2 text-sm text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 rounded-xl transition"
          title={collapsed ? 'Sign Out' : undefined}
        >
          <LogOut className="w-4 h-4 flex-shrink-0" />
          {!collapsed && 'Sign Out'}
        </button>
      </div>
    </div>
  );

  return (
    <AdminToastProvider>
      <div className="min-h-screen bg-slate-950 text-slate-100 flex font-sans">
        {/* Desktop Sidebar */}
        <aside className={`hidden lg:flex flex-col flex-shrink-0 bg-slate-900 border-r border-slate-800 transition-all duration-300 ${sidebarCollapsed ? 'w-16' : 'w-60'}`}>
          {sidebarContent(sidebarCollapsed)}
          {/* Collapse toggle */}
          <button
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            className="absolute left-0 top-1/2 -translate-y-1/2 translate-x-full w-5 h-10 bg-slate-800 border border-slate-700 rounded-r-lg flex items-center justify-center text-slate-400 hover:text-white hover:bg-slate-700 transition z-10"
            style={{ left: sidebarCollapsed ? '4rem' : '15rem' }}
          >
            {sidebarCollapsed ? <ChevronRight className="w-3 h-3" /> : <ChevronLeft className="w-3 h-3" />}
          </button>
        </aside>

        {/* Mobile Sidebar Overlay */}
        {mobileOpen && (
          <div className="lg:hidden fixed inset-0 z-50 flex">
            <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setMobileOpen(false)} />
            <aside className="relative w-60 bg-slate-900 border-r border-slate-800 flex flex-col">
              <button
                onClick={() => setMobileOpen(false)}
                className="absolute top-3 right-3 p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition"
              >
                <X className="w-4 h-4" />
              </button>
              {sidebarContent(false, () => setMobileOpen(false))}
            </aside>
          </div>
        )}

        {/* Main Content */}
        <div className="flex-1 flex flex-col min-w-0">
          {/* Top bar (mobile) */}
          <header className="lg:hidden sticky top-0 z-40 bg-slate-900/90 backdrop-blur border-b border-slate-800 px-4 h-14 flex items-center gap-3">
            <button
              onClick={() => setMobileOpen(true)}
              className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition"
            >
              <Menu className="w-5 h-5" />
            </button>
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 rounded-lg bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center">
                <Shield className="w-3 h-3 text-white" />
              </div>
              <span className="text-sm font-bold text-white">Admin Panel</span>
            </div>
          </header>

          {/* Page Content */}
          <main className="flex-1 overflow-auto p-4 lg:p-6">
            <AnimatePresence mode="wait" initial={false}>
              <PageTransition key={location.pathname}>
                <Outlet />
              </PageTransition>
            </AnimatePresence>
          </main>
        </div>
      </div>
    </AdminToastProvider>
  );
};
