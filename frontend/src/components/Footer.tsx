import React from 'react';
import { Link } from 'react-router-dom';
import {
  ShieldCheck,
  Lock,
  Headphones,
  Award,
  Zap,
  Terminal,
  Plane,
  Building2,
  Compass
} from 'lucide-react';
import { APP_NAME, APP_VERSION } from '../config/constants';
import { BrandLogo } from './BrandLogo';

export const Footer: React.FC = () => {
  return (
    <footer className="border-t border-white/10 bg-[#090A0F] text-slate-400 text-xs">
      {/* 1. TRUST BAR */}
      <div className="border-b border-white/5 py-8 bg-[#0B0C10]/90">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center shrink-0 shadow-glow-gold">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-white text-xs">100% Secure Payments</p>
              <p className="text-[11px] text-slate-400">Encrypted PCI-DSS & Instant Confirmation</p>
            </div>
          </div>

          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center shrink-0 shadow-glow-emerald">
              <Zap className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-white text-xs">Instant E-Ticket Issuance</p>
              <p className="text-[11px] text-slate-400">Direct airline PNR & Digital QR Boarding Pass</p>
            </div>
          </div>

          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-xl bg-accent/10 text-accent border border-accent/20 flex items-center justify-center shrink-0 shadow-glow-coral">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-white text-xs">Best Fare Guarantee</p>
              <p className="text-[11px] text-slate-400">Transparent pricing with Fare Lock feature</p>
            </div>
          </div>

          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center shrink-0 shadow-glow-gold">
              <Headphones className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-white text-xs">24/7 Dedicated Support</p>
              <p className="text-[11px] text-slate-400">Automated cancellation & express refunds</p>
            </div>
          </div>
        </div>
      </div>

      {/* 2. SITEMAP & BRANDING */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8">
        {/* Col 1: Brand & Description */}
        <div className="lg:col-span-2 space-y-4">
          <BrandLogo size="lg" withLink={true} />
          <p className="text-xs text-slate-400 leading-relaxed pr-6">
            SmartTravel is a next-generation travel platform offering lightning-fast flight searches, interactive seat selection, luxury hotel bookings with live availability, real-time live flight radar, and transparent auto-refunds.
          </p>
          <div className="flex flex-wrap items-center gap-2 pt-1">
            <span className="px-2.5 py-1 rounded-lg bg-[#14161F] border border-white/10 text-[10px] font-mono text-amber-400 font-bold">
              Spring Boot 3.3.x
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-[#14161F] border border-white/10 text-[10px] font-mono text-white">
              React 18 + TS
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-[#14161F] border border-white/10 text-[10px] font-mono text-emerald-400">
              MongoDB Atlas
            </span>
          </div>
        </div>

        {/* Col 2: Flights & Routes */}
        <div className="space-y-3">
          <h4 className="text-xs font-bold text-white uppercase tracking-wider flex items-center gap-1.5">
            <Plane className="w-3.5 h-3.5 text-amber-400" /> Popular Flights
          </h4>
          <ul className="space-y-2 text-xs">
            <li>
              <Link to="/flights?origin=DEL&destination=BOM" className="hover:text-amber-400 transition">
                Delhi (DEL) → Mumbai (BOM)
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=BLR&destination=DEL" className="hover:text-amber-400 transition">
                Bangalore (BLR) → Delhi (DEL)
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=BOM&destination=GOI" className="hover:text-amber-400 transition">
                Mumbai (BOM) → Goa (GOI)
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=BOM&destination=DXB" className="hover:text-amber-400 transition">
                Mumbai (BOM) → Dubai (DXB)
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=DEL&destination=LHR" className="hover:text-amber-400 transition">
                Delhi (DEL) → London (LHR)
              </Link>
            </li>
          </ul>
        </div>

        {/* Col 3: Stays & Features */}
        <div className="space-y-3">
          <h4 className="text-xs font-bold text-white uppercase tracking-wider flex items-center gap-1.5">
            <Building2 className="w-3.5 h-3.5 text-amber-400" /> Hotels & Features
          </h4>
          <ul className="space-y-2 text-xs">
            <li>
              <Link to="/hotels" className="hover:text-amber-400 transition">
                Luxury Stays & Resorts
              </Link>
            </li>
            <li>
              <Link to="/live-tracker" className="hover:text-amber-400 transition">
                Live Flight Radar
              </Link>
            </li>
            <li>
              <Link to="/offers" className="hover:text-amber-400 transition">
                Price Freeze & Deals
              </Link>
            </li>
            <li>
              <Link to="/my-bookings" className="hover:text-amber-400 transition">
                Web Check-In & Boarding Pass
              </Link>
            </li>
            <li>
              <Link to="/my-bookings" className="hover:text-amber-400 transition">
                Refunds & Cancellation
              </Link>
            </li>
          </ul>
        </div>

        {/* Col 4: Quick Links & Documentation */}
        <div className="space-y-3">
          <h4 className="text-xs font-bold text-white uppercase tracking-wider flex items-center gap-1.5">
            <Compass className="w-3.5 h-3.5 text-amber-400" /> Developer & Info
          </h4>
          <ul className="space-y-2 text-xs">
            <li>
              <Link to="/account" className="hover:text-amber-400 transition">
                User Preferences & Profile
              </Link>
            </li>
            <li>
              <a
                href={import.meta.env.VITE_SWAGGER_URL || '/swagger-ui.html'}
                target="_blank"
                rel="noreferrer"
                className="hover:text-amber-400 transition flex items-center gap-1 font-mono text-amber-400"
              >
                <Terminal className="w-3.5 h-3.5" />
                Swagger REST APIs
              </a>
            </li>
          </ul>
        </div>
      </div>

      {/* 3. COPYRIGHT & SECURITY */}
      <div className="border-t border-white/5 py-6 bg-[#060709]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2 text-[11px] text-slate-500">
            <span>© {new Date().getFullYear()} {APP_NAME}. All rights reserved.</span>
            <span>•</span>
            <span className="font-mono text-slate-400">v{APP_VERSION}</span>
          </div>

          <div className="flex items-center gap-4 text-[11px] text-slate-500">
            <span className="flex items-center gap-1 text-emerald-400">
              <Lock className="w-3.5 h-3.5" />
              256-Bit SSL Encrypted
            </span>
            <span>•</span>
            <span>PCI-DSS Verified</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
