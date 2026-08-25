import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import { Navbar } from '../components/Navbar';
import { Footer } from '../components/Footer';
import { NetworkStatusBanner } from '../components/NetworkStatusBanner';
import { PageTransition } from '../components/PageTransition';

export const MainLayout: React.FC = () => {
  const location = useLocation();
  const isHomePage = location.pathname === '/';

  return (
    <div className="min-h-screen bg-[#0B0C10] text-slate-100 flex flex-col selection:bg-amber-400 selection:text-black font-sans">
      <Navbar />
      <main className={`flex-1 w-full ${isHomePage ? '' : 'max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8'}`}>
        <AnimatePresence mode="wait" initial={false}>
          <PageTransition key={location.pathname}>
            <Outlet />
          </PageTransition>
        </AnimatePresence>
      </main>
      <Footer />
      <NetworkStatusBanner />
    </div>
  );
};

