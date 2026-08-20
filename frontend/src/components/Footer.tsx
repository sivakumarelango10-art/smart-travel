import React from 'react';
import { Link } from 'react-router-dom';
import {
  ShieldCheck,
  Terminal,
  Lock,
  Headphones,
  Award,
  Zap
} from 'lucide-react';
import { APP_NAME, APP_VERSION } from '../config/constants';
import { BrandLogo } from './BrandLogo';

export const Footer: React.FC = () => {
  return (
    <footer className="border-t border-slate-800/80 bg-slate-950 text-slate-400 text-xs">
      {/* 1. TOP HIGHLIGHTS & TRUST BAR */}
      <div className="border-b border-slate-800/60 py-8 bg-slate-900/40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-2xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center font-black shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-white text-xs">100% Safe Payments</p>
              <p className="text-[11px] text-slate-400">PCI-DSS Level 1 & Razorpay Verified</p>
            </div>
          </div>

          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-2xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center font-black shrink-0">
              <Zap className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-white text-xs">Instant E-Tickets & Boarding</p>
              <p className="text-[11px] text-slate-400">Direct airline GDS and PNR issuance</p>
            </div>
          </div>

          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-2xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 flex items-center justify-center font-black shrink-0">
              <Headphones className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-white text-xs">24x7 Customer Support</p>
              <p className="text-[11px] text-slate-400">Dedicated desk for flight changes</p>
            </div>
          </div>

          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-2xl bg-amber-500/10 text-amber-400 border border-amber-500/20 flex items-center justify-center font-black shrink-0">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <p className="font-bold text-white text-xs">Best Fare Guarantee</p>
              <p className="text-[11px] text-slate-400">Zero hidden markups or seat fees</p>
            </div>
          </div>
        </div>
      </div>

      {/* 2. MAIN FOOTER SITEMAP */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8">
        {/* Col 1: Brand Info */}
        <div className="lg:col-span-2 space-y-4">
          <BrandLogo size="lg" withLink={true} />
          <p className="text-xs text-slate-400 leading-relaxed pr-4">
            India's most intuitive and elevated flight booking ecosystem. Experience lightning-fast flight searches, interactive seat selection, instant PNR generation, digital mobile boarding passes, and automated refunds.
          </p>
          <div className="flex flex-wrap items-center gap-2 pt-2">
            <span className="px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800 text-[10px] font-mono text-sky-300">
              Spring Boot 3.3.2
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800 text-[10px] font-mono text-indigo-300">
              React 18 + TypeScript
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800 text-[10px] font-mono text-emerald-300">
              MongoDB Atlas
            </span>
          </div>
        </div>

        {/* Col 2: Popular Domestic Routes */}
        <div className="space-y-3">
          <h4 className="text-xs font-black text-white uppercase tracking-wider">Domestic Flights</h4>
          <ul className="space-y-2 text-xs">
            <li>
              <Link to="/flights?origin=DEL&destination=BOM" className="hover:text-sky-400 transition">
                Delhi to Mumbai Flights
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=BLR&destination=GOI" className="hover:text-sky-400 transition">
                Bangalore to Goa Flights
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=BOM&destination=BLR" className="hover:text-sky-400 transition">
                Mumbai to Bangalore Flights
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=DEL&destination=MAA" className="hover:text-sky-400 transition">
                Delhi to Chennai Flights
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=HYD&destination=DEL" className="hover:text-sky-400 transition">
                Hyderabad to Delhi Flights
              </Link>
            </li>
          </ul>
        </div>

        {/* Col 3: Popular International Routes */}
        <div className="space-y-3">
          <h4 className="text-xs font-black text-white uppercase tracking-wider">International</h4>
          <ul className="space-y-2 text-xs">
            <li>
              <Link to="/flights?origin=BOM&destination=DXB" className="hover:text-sky-400 transition">
                Mumbai to Dubai Flights
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=DEL&destination=SIN" className="hover:text-sky-400 transition">
                Delhi to Singapore Flights
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=BLR&destination=BKK" className="hover:text-sky-400 transition">
                Bangalore to Bangkok Flights
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=MAA&destination=KUL" className="hover:text-sky-400 transition">
                Chennai to Kuala Lumpur
              </Link>
            </li>
            <li>
              <Link to="/flights?origin=DEL&destination=LHR" className="hover:text-sky-400 transition">
                Delhi to London Flights
              </Link>
            </li>
          </ul>
        </div>

        {/* Col 4: Quick Links & Support */}
        <div className="space-y-3">
          <h4 className="text-xs font-black text-white uppercase tracking-wider">Quick Access</h4>
          <ul className="space-y-2 text-xs">
            <li>
              <Link to="/my-bookings" className="hover:text-sky-400 transition">
                My Bookings & Tickets
              </Link>
            </li>
            <li>
              <Link to="/my-bookings" className="hover:text-sky-400 transition">
                Web Check-In Portal
              </Link>
            </li>
            <li>
              <Link to="/my-bookings" className="hover:text-sky-400 transition">
                Refunds & Cancellation
              </Link>
            </li>
            <li>
              <a
                href={import.meta.env.VITE_SWAGGER_URL || '/swagger-ui.html'}
                target="_blank"
                rel="noreferrer"
                className="hover:text-sky-400 transition flex items-center gap-1 font-mono text-sky-400"
              >
                <Terminal className="w-3.5 h-3.5" />
                Swagger API Docs
              </a>
            </li>
          </ul>
        </div>
      </div>

      {/* 3. BOTTOM COPYRIGHT & SECURITY ROW */}
      <div className="border-t border-slate-800/80 py-6 bg-slate-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex flex-wrap items-center gap-2 text-[11px] text-slate-500">
            <span>© {new Date().getFullYear()} {APP_NAME} Inc. All rights reserved.</span>
            <span>•</span>
            <span className="font-mono text-slate-400">v{APP_VERSION} Production</span>
          </div>

          <div className="flex items-center gap-4 text-[11px] text-slate-500">
            <span className="flex items-center gap-1">
              <Lock className="w-3.5 h-3.5 text-emerald-400" />
              256-Bit SSL Secured
            </span>
            <span>•</span>
            <span>Razorpay Payment Gateway</span>
          </div>
        </div>
      </div>
    </footer>
  );
};

