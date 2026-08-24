import React, { useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Cookie,
  Shield,
  HardDrive,
  Lock,
  FileText,
  Printer,
  CheckCircle2,
  XCircle,
  Bell
} from 'lucide-react';

export const CookiePolicyPage: React.FC = () => {
  useEffect(() => {
    document.title = 'Cookie & Storage Policy | SmartTravel';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  return (
    <div className="max-w-4xl mx-auto py-8 sm:py-12 px-4 sm:px-6 lg:px-8 space-y-8 animate-fade-in text-slate-300">
      {/* Header Banner */}
      <div className="p-6 sm:p-10 rounded-3xl bg-gradient-to-b from-[#14161F] to-[#0E1017] border border-white/10 shadow-2xl relative overflow-hidden space-y-4">
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 text-xs font-bold shadow-glow-gold">
          <Cookie className="w-3.5 h-3.5" />
          <span>Transparent Storage & Token Policy</span>
        </div>

        <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight">
          Cookie & Client Storage Policy
        </h1>

        <p className="text-sm text-slate-400 leading-relaxed max-w-2xl">
          This document provides a transparent explanation of how SmartTravel uses client-side web storage mechanisms (such as <code>localStorage</code> and <code>sessionStorage</code>) and why our platform does not deploy third-party advertising tracking cookies.
        </p>

        <div className="flex flex-wrap items-center gap-4 text-xs font-mono text-slate-400 pt-2 border-t border-white/5">
          <span>Effective Date: <strong>[EFFECTIVE DATE - e.g. August 24, 2026]</strong></span>
          <span>•</span>
          <span className="text-amber-400 font-bold">Version: 1.0</span>
        </div>

        <div className="flex flex-wrap items-center gap-3 pt-2">
          <button
            type="button"
            onClick={() => window.print()}
            className="px-4 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-200 text-xs font-bold flex items-center gap-2 border border-white/10 transition"
          >
            <Printer className="w-4 h-4 text-amber-400" />
            <span>Print Policy</span>
          </button>
          <Link
            to="/privacy-policy"
            className="px-4 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-200 text-xs font-bold flex items-center gap-2 border border-white/10 transition"
          >
            <Shield className="w-4 h-4 text-amber-400" />
            <span>Privacy Policy</span>
          </Link>
        </div>
      </div>

      {/* Main Content Sections */}
      <div className="space-y-6 text-xs sm:text-sm leading-relaxed">
        {/* Card 1: Executive Summary */}
        <div className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
          <h2 className="text-lg font-black text-white flex items-center gap-2.5">
            <Shield className="w-5 h-5 text-amber-400" />
            1. Zero Tracking Cookies Architecture
          </h2>
          <p>
            Unlike many travel websites that place tracking cookies or third-party behavioral advertising pixels across your devices, <strong>SmartTravel does NOT utilize third-party advertising cookies, ad trackers, or third-party profiling cookies</strong>.
          </p>
          <p>
            Our web application uses modern, secure Web APIs—primarily <strong>HTML5 Web Storage (<code>localStorage</code> and <code>sessionStorage</code>)</strong>—solely for essential functions like keeping you logged in and providing fast search interactions.
          </p>
        </div>

        {/* Card 2: Itemized Storage Keys Table */}
        <div className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
          <h2 className="text-lg font-black text-white flex items-center gap-2.5">
            <HardDrive className="w-5 h-5 text-amber-400" />
            2. Detailed Inventory of Client Storage Keys
          </h2>
          <p className="text-slate-400">
            Below is the complete, comprehensive list of client-side storage keys stored in your browser by the SmartTravel frontend:
          </p>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border border-white/10 rounded-2xl overflow-hidden">
              <thead className="bg-[#181A22] text-white uppercase font-bold text-[10px]">
                <tr>
                  <th className="p-3 border-b border-white/10">Storage Key</th>
                  <th className="p-3 border-b border-white/10">Mechanism</th>
                  <th className="p-3 border-b border-white/10">Purpose & Data Stored</th>
                  <th className="p-3 border-b border-white/10">Duration</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5 bg-[#14161F]">
                <tr>
                  <td className="p-3 font-mono font-bold text-amber-400">smarttravel_access_token</td>
                  <td className="p-3 text-slate-300">localStorage / sessionStorage</td>
                  <td className="p-3 text-slate-400">
                    Stores the cryptographic JSON Web Token (JWT) Bearer token used to authenticate REST API requests for your account.
                  </td>
                  <td className="p-3 text-white font-mono">24 Hours / Session</td>
                </tr>
                <tr>
                  <td className="p-3 font-mono font-bold text-amber-400">smarttravel_refresh_token</td>
                  <td className="p-3 text-slate-300">localStorage / sessionStorage</td>
                  <td className="p-3 text-slate-400">
                    Stores the token refresh credential if you check &quot;Remember Me&quot; during login, allowing automatic session renewal.
                  </td>
                  <td className="p-3 text-white font-mono">7 Days</td>
                </tr>
                <tr>
                  <td className="p-3 font-mono font-bold text-amber-400">smarttravel_user</td>
                  <td className="p-3 text-slate-300">localStorage / sessionStorage</td>
                  <td className="p-3 text-slate-400">
                    Caches non-sensitive profile summary (User ID, Email, Full Name, Roles) to instantly render the navbar without layout shifts.
                  </td>
                  <td className="p-3 text-white font-mono">Session / Cleared on Logout</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        {/* Card 3: Categorization */}
        <div className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
          <h2 className="text-lg font-black text-white flex items-center gap-2.5">
            <Lock className="w-5 h-5 text-amber-400" />
            3. Storage Categories Explained
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="p-4 rounded-2xl bg-[#181A22] border border-white/10 space-y-2">
              <div className="flex items-center gap-2 text-emerald-400 font-bold">
                <CheckCircle2 className="w-4 h-4" />
                <span>Strictly Necessary Storage (Active)</span>
              </div>
              <p className="text-slate-400">
                Authentication tokens and user session caches are strictly necessary for the operation of your account, flight reservations, and secure checkouts. Without these keys, you cannot sign in or manage bookings.
              </p>
            </div>

            <div className="p-4 rounded-2xl bg-[#181A22] border border-white/10 space-y-2">
              <div className="flex items-center gap-2 text-rose-400 font-bold">
                <XCircle className="w-4 h-4" />
                <span>Marketing & Advertising Trackers (None)</span>
              </div>
              <p className="text-slate-400">
                We do NOT place advertising pixels, cross-site trackers, or commercial cookies. We do NOT track your browsing habits across third-party websites.
              </p>
            </div>
          </div>
        </div>

        {/* Card 4: Web Push API */}
        <div className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
          <h2 className="text-lg font-black text-white flex items-center gap-2.5">
            <Bell className="w-5 h-5 text-amber-400" />
            4. Web Push Notification Subscriptions
          </h2>
          <p>
            If you opt-in to browser push notifications, your browser generates a push subscription token via the <strong>W3C Push API</strong>. This subscription is stored in our database (<code>push_subscriptions</code>) to send you real-time flight gate alerts and check-in reminders.
          </p>
          <p>
            You can revoke push notification permission at any time directly through your web browser&apos;s site permissions settings.
          </p>
        </div>

        {/* Card 5: How to Clear Storage */}
        <div className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
          <h2 className="text-lg font-black text-white flex items-center gap-2.5">
            <FileText className="w-5 h-5 text-amber-400" />
            5. Managing & Clearing Your Browser Storage
          </h2>
          <p>
            You can clear all stored SmartTravel tokens at any time simply by clicking <strong>&quot;Sign Out&quot;</strong> in the top navigation bar, or by clearing your browser&apos;s site data via developer tools or browser privacy settings:
          </p>
          <ul className="list-disc pl-5 space-y-1 text-slate-400 text-xs">
            <li><strong>Google Chrome / Edge:</strong> Settings &rarr; Privacy and security &rarr; Clear browsing data &rarr; Cookies and other site data.</li>
            <li><strong>Mozilla Firefox:</strong> Settings &rarr; Privacy &amp; Security &rarr; Cookies and Site Data &rarr; Clear Data.</li>
            <li><strong>Apple Safari:</strong> Preferences &rarr; Privacy &rarr; Manage Website Data &rarr; Remove All.</li>
          </ul>
        </div>
      </div>
    </div>
  );
};
