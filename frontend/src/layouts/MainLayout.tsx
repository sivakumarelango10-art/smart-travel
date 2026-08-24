import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { Footer } from '../components/Footer';
import { NetworkStatusBanner } from '../components/NetworkStatusBanner';

export const MainLayout: React.FC = () => {
  const location = useLocation();
  const isHomePage = location.pathname === '/';

  return (
    <div className="min-h-screen bg-[#0B0C10] text-slate-100 flex flex-col selection:bg-amber-400 selection:text-black font-sans">
      <Navbar />
      <main className={`flex-1 w-full page-transition ${isHomePage ? '' : 'max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8'}`}>
        <Outlet />
      </main>
      <Footer />
      <NetworkStatusBanner />
    </div>
  );
};
