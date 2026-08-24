import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Shield,
  Lock,
  Eye,
  FileText,
  UserCheck,
  Server,
  Bell,
  Trash2,
  RefreshCw,
  HelpCircle,
  ChevronRight,
  ExternalLink,
  Printer,
  Compass,
  CreditCard,
  Building2,
  Layers,
  Sparkles
} from 'lucide-react';

interface Section {
  id: string;
  title: string;
  number: string;
}

const SECTIONS: Section[] = [
  { id: 'introduction', title: 'Introduction & About SmartTravel', number: '1' },
  { id: 'scope', title: 'Scope of this Privacy Policy', number: '2' },
  { id: 'dpdp-compliance', title: 'Indian DPDP Act, 2023 Compliance Notice', number: '3' },
  { id: 'data-collected', title: 'Personal Data We Collect', number: '4' },
  { id: 'account-passenger-data', title: 'Account, Traveler & Passenger Data', number: '5' },
  { id: 'booking-inventory-data', title: 'Booking, Flight & Hotel Reservation Data', number: '6' },
  { id: 'payment-refund-data', title: 'Payment & Refund Transaction Records', number: '7' },
  { id: 'flight-tracking-data', title: 'Flight Tracking & Radar Telemetry', number: '8' },
  { id: 'notifications-data', title: 'Notifications & Web Push Subscriptions', number: '9' },
  { id: 'device-storage-data', title: 'Device Data, Storage & Token Usage', number: '10' },
  { id: 'purposes-processing', title: 'Purposes & Legal Grounds for Processing', number: '11' },
  { id: 'dynamic-pricing-transparency', title: 'Dynamic Pricing & Pricing History', number: '12' },
  { id: 'data-sharing', title: 'Data Sharing & Third-Party Service Providers', number: '13' },
  { id: 'data-security', title: 'Data Security & Storage Safeguards', number: '14' },
  { id: 'retention-deletion', title: 'Data Retention & Account Deletion', number: '15' },
  { id: 'user-rights', title: 'Your Rights as a Data Principal', number: '16' },
  { id: 'children-privacy', title: "Children's & Minor Passenger Privacy", number: '17' },
  { id: 'international-transfers', title: 'International Data Transfers', number: '18' },
  { id: 'cookies-tracking', title: 'Cookies & Client-Side Storage Policy', number: '19' },
  { id: 'policy-changes', title: 'Modifications to this Privacy Policy', number: '20' },
  { id: 'contact-grievance', title: 'Contact Information & Grievance Officer', number: '21' },
];

export const PrivacyPolicyPage: React.FC = () => {
  const [activeSection, setActiveSection] = useState<string>('introduction');
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);

  useEffect(() => {
    document.title = 'Privacy Policy | SmartTravel';
    const handleScroll = () => {
      const scrollPos = window.scrollY + 180;
      for (let i = SECTIONS.length - 1; i >= 0; i--) {
        const el = document.getElementById(SECTIONS[i].id);
        if (el && el.offsetTop <= scrollPos) {
          setActiveSection(SECTIONS[i].id);
          break;
        }
      }
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const scrollTo = (id: string) => {
    const el = document.getElementById(id);
    if (el) {
      const offset = 100;
      const bodyRect = document.body.getBoundingClientRect().top;
      const elementRect = el.getBoundingClientRect().top;
      const elementPosition = elementRect - bodyRect;
      const offsetPosition = elementPosition - offset;

      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth',
      });
      setActiveSection(id);
      setMobileMenuOpen(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto py-8 sm:py-12 px-4 sm:px-6 lg:px-8 space-y-8 animate-fade-in text-slate-300">
      {/* Header Banner */}
      <div className="p-6 sm:p-10 rounded-3xl bg-gradient-to-b from-[#14161F] to-[#0E1017] border border-white/10 shadow-2xl relative overflow-hidden">
        <div className="relative z-10 space-y-4 max-w-3xl">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 text-xs font-bold shadow-glow-gold">
            <Shield className="w-3.5 h-3.5" />
            <span>Digital Personal Data Protection Act, 2023 Compliant</span>
          </div>

          <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight">
            SmartTravel Privacy Policy
          </h1>

          <p className="text-sm text-slate-400 leading-relaxed">
            This Privacy Policy explains how SmartTravel collects, uses, processes, stores, and protects your personal data when you access our travel platform, book flights and hotel stays, track flight status, manage your reservations, or communicate with us.
          </p>

          <div className="flex flex-wrap items-center gap-4 text-xs font-mono text-slate-400 pt-2 border-t border-white/5">
            <span>Effective Date: <strong>[EFFECTIVE DATE - e.g. August 24, 2026]</strong></span>
            <span>•</span>
            <span>Last Updated: <strong>[LAST UPDATED DATE - e.g. August 24, 2026]</strong></span>
            <span>•</span>
            <span className="text-amber-400 font-bold">Version: 1.0</span>
          </div>
        </div>

        <div className="mt-6 flex flex-wrap items-center gap-3">
          <button
            type="button"
            onClick={() => window.print()}
            className="px-4 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-200 text-xs font-bold flex items-center gap-2 border border-white/10 transition"
          >
            <Printer className="w-4 h-4 text-amber-400" />
            <span>Print Document</span>
          </button>
          <Link
            to="/terms-and-conditions"
            className="px-4 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-200 text-xs font-bold flex items-center gap-2 border border-white/10 transition"
          >
            <FileText className="w-4 h-4 text-amber-400" />
            <span>View Terms & Conditions</span>
          </Link>
        </div>
      </div>

      {/* Main Grid: Sticky Sidebar + Content Body */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Desktop Sticky Table of Contents */}
        <aside className="hidden lg:block lg:col-span-4 sticky top-24 bg-[#14161F] border border-white/10 rounded-3xl p-5 shadow-2xl space-y-3">
          <div className="flex items-center justify-between pb-3 border-b border-white/10">
            <span className="text-xs font-bold uppercase tracking-wider text-white flex items-center gap-2">
              <Layers className="w-4 h-4 text-amber-400" /> Table of Contents
            </span>
            <span className="text-[11px] font-mono text-slate-500">{SECTIONS.length} Sections</span>
          </div>

          <nav className="max-h-[calc(100vh-220px)] overflow-y-auto space-y-1 pr-1 custom-scrollbar">
            {SECTIONS.map((sec) => (
              <button
                key={sec.id}
                type="button"
                onClick={() => scrollTo(sec.id)}
                className={`w-full text-left px-3 py-2 rounded-xl text-xs flex items-center justify-between transition-all ${
                  activeSection === sec.id
                    ? 'bg-gradient-to-r from-amber-400/20 to-amber-500/10 text-amber-300 font-bold border border-amber-400/30'
                    : 'text-slate-400 hover:text-white hover:bg-[#181A22]'
                }`}
              >
                <span className="truncate pr-2">
                  <strong className="text-amber-400 font-mono mr-1.5">{sec.number}.</strong>
                  {sec.title}
                </span>
                <ChevronRight className={`w-3 h-3 shrink-0 ${activeSection === sec.id ? 'text-amber-400' : 'text-slate-600'}`} />
              </button>
            ))}
          </nav>
        </aside>

        {/* Mobile Collapsible TOC Drawer */}
        <div className="lg:hidden col-span-1 bg-[#14161F] border border-white/10 rounded-2xl p-4 space-y-2">
          <button
            type="button"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="w-full flex items-center justify-between text-xs font-bold text-white"
          >
            <span className="flex items-center gap-2">
              <Layers className="w-4 h-4 text-amber-400" />
              Quick Navigation Index ({SECTIONS.length} Sections)
            </span>
            <span className="text-amber-400">{mobileMenuOpen ? 'Hide' : 'Show'}</span>
          </button>

          {mobileMenuOpen && (
            <div className="pt-3 border-t border-white/10 space-y-1 max-h-72 overflow-y-auto">
              {SECTIONS.map((sec) => (
                <button
                  key={sec.id}
                  type="button"
                  onClick={() => scrollTo(sec.id)}
                  className="w-full text-left px-3 py-2 rounded-lg text-xs text-slate-300 hover:bg-[#181A22] flex items-center gap-2"
                >
                  <span className="text-amber-400 font-mono">{sec.number}.</span>
                  <span className="truncate">{sec.title}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Policy Content Body */}
        <main className="lg:col-span-8 space-y-10 text-xs sm:text-sm leading-relaxed">
          {/* Section 1 */}
          <section id="introduction" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                1
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Introduction & About SmartTravel</h2>
            </div>

            <p>
              Welcome to <strong>SmartTravel</strong> (&quot;SmartTravel&quot;, &quot;Platform&quot;, &quot;we&quot;, &quot;our&quot;, or &quot;us&quot;), operated by <strong>[SMARTTRAVEL LEGAL ENTITY NAME]</strong>, having its registered office at <strong>[REGISTERED OFFICE ADDRESS]</strong>.
            </p>
            <p>
              SmartTravel is a modern online travel facilitation and management platform providing comprehensive travel services including scheduled flight search, interactive cabin seat selection, price freeze reservations, real-time dynamic flight pricing, automated cancellations with policy-based refunds, digital e-ticket issuance, simulated live flight radar tracking, and luxury hotel accommodation booking.
            </p>
            <p>
              We value your trust and are dedicated to processing your personal data transparently, securely, and in full accordance with applicable data protection laws.
            </p>
          </section>

          {/* Section 2 */}
          <section id="scope" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                2
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Scope of this Privacy Policy</h2>
            </div>

            <p>
              This Privacy Policy applies to all digital personal data collected or processed through:
            </p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li>Our website and web application accessible via desktop, tablet, and mobile browsers;</li>
              <li>Customer user account registration, authentication, and profile preference management;</li>
              <li>Flight search, itinerary generation, seat map selection, price lock creation, and reservation booking;</li>
              <li>Payment order initialization, gateway checkout, transaction verification, and refund disbursements;</li>
              <li>Hotel discovery, room type reservation holds, and stay booking confirmations;</li>
              <li>Live airspace telemetry tracking subscriptions and in-app/push notification dispatches;</li>
              <li>Any customer service inquiries, verification workflows, or platform communications.</li>
            </ul>
          </section>

          {/* Section 3 */}
          <section id="dpdp-compliance" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                3
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Indian DPDP Act, 2023 Compliance Notice</h2>
            </div>

            <p>
              For users in the territory of India and travelers booking Indian domestic or international travel itineraries, personal data processing is governed by the <strong>Digital Personal Data Protection Act, 2023 (DPDP Act, 2023)</strong> and the notified <strong>Digital Personal Data Protection Rules, 2025</strong>.
            </p>
            <div className="p-4 rounded-2xl bg-[#181A22] border border-white/10 space-y-2 text-xs">
              <strong className="text-amber-400 block font-bold">Key DPDP Principles Implemented in SmartTravel:</strong>
              <p>• <strong>Notice & Purpose Limitation:</strong> We inform you of the specific personal data collected and the distinct purposes for which it is processed prior to collection.</p>
              <p>• <strong>Consent & Withdrawal:</strong> Clear, non-deceptive, affirmative consent is collected where required. You may withdraw optional consents at any time.</p>
              <p>• <strong>Data Principal Rights:</strong> You have the right to access a summary of your personal data, seek correction of inaccurate information, and request erasure of your data when processing purposes are fulfilled.</p>
              <p>• <strong>Grievance Redressal:</strong> An accessible mechanism is provided to resolve privacy concerns through our designated Grievance Officer.</p>
            </div>
          </section>

          {/* Section 4 */}
          <section id="data-collected" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                4
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Personal Data We Collect</h2>
            </div>

            <p>
              We collect only the personal data that is strictly necessary to provide travel facilitation services, fulfill bookings, comply with aviation and tax regulations, and secure our systems against fraud.
            </p>
            <p>
              Depending on your interactions with the Platform, we collect data directly from you when you register, book, or configure preferences, as well as operational data generated during platform utilization.
            </p>
          </section>

          {/* Section 5 */}
          <section id="account-passenger-data" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                5
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Account, Traveler & Passenger Data</h2>
            </div>

            <p>When you create an account, manage your profile, or enter traveler information for a booking:</p>
            <div className="space-y-3">
              <div className="p-3.5 rounded-xl bg-[#181A22] border border-white/5">
                <strong className="text-white block font-bold">1. User Account Profile (MongoDB <code>users</code> collection):</strong>
                <p className="text-slate-400 mt-1 text-xs">
                  Full name, email address (and normalized lookup index), securely hashed password (stored exclusively via <code>BCrypt</code> cryptographic one-way hashing; plain passwords are never stored), phone number, account roles, and verification flags.
                </p>
              </div>

              <div className="p-3.5 rounded-xl bg-[#181A22] border border-white/5">
                <strong className="text-white block font-bold">2. Travel Preferences & Saved Profile Details (<code>UserPreferences</code>):</strong>
                <p className="text-slate-400 mt-1 text-xs">
                  Preferred cabin class (Economy, Business, First), preferred seat type (Window, Aisle, Extra Legroom), preferred room category, home departure airport, dietary meal preference (Vegetarian, Non-Veg, Vegan), favorite destinations, saved billing/postal address (Street, City, State, Postal Code, Country), optional nationality, and travel document number (Passport Number).
                </p>
              </div>

              <div className="p-3.5 rounded-xl bg-[#181A22] border border-white/5">
                <strong className="text-white block font-bold">3. Passenger & Traveler Details (<code>Passenger</code> embedded in <code>bookings</code>):</strong>
                <p className="text-slate-400 mt-1 text-xs">
                  Title (Mr, Ms, Mrs, Dr), First Name, Last Name, Date of Birth, Gender (Male, Female, Other), Nationality, Passport Number (for international itineraries), allocated Seat Number, and digital Web Check-In confirmation status.
                </p>
              </div>
            </div>
          </section>

          {/* Section 6 */}
          <section id="booking-inventory-data" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                6
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Booking, Flight & Hotel Reservation Data</h2>
            </div>

            <p>During the flight reservation and hotel booking process, the following transactional data is captured and recorded:</p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li><strong>PNR & Booking Reference:</strong> Authoritative 6-character alphanumeric passenger name record (e.g., <code>AI98K2</code>).</li>
              <li><strong>Flight Itinerary:</strong> Flight Number, Operating Airline, Origin & Destination Airports, Departure and Arrival timestamps, flight duration, and Cabin Class.</li>
              <li><strong>Itemized Fare Breakdown:</strong> Itemized base fare, government GST/taxes, convenience fees, and total gross charge in Indian Rupees (INR).</li>
              <li><strong>E-Tickets & Boarding Passes:</strong> Issued ticket number, QR code verification token, gate number, terminal, and boarding group.</li>
              <li><strong>Hotel Bookings:</strong> Selected property ID, room type ID, room count, check-in/out dates, and 15-minute temporary inventory hold references.</li>
            </ul>
          </section>

          {/* Section 7 */}
          <section id="payment-refund-data" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                7
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Payment & Refund Transaction Records</h2>
            </div>

            <p>
              SmartTravel prioritizes financial security and complies with strict payment data minimization rules:
            </p>
            <div className="p-4 rounded-2xl bg-amber-400/10 border border-amber-400/20 text-xs text-slate-300 space-y-2">
              <strong className="text-amber-400 font-bold flex items-center gap-1.5">
                <CreditCard className="w-4 h-4" /> Payment Card Information Is NOT Stored on SmartTravel Servers
              </strong>
              <p>
                We do NOT collect, receive, or store your debit/credit card numbers, CVV codes, UPI PINs, or net banking passwords. All payment transactions are processed securely through our authorized payment gateway partner, <strong>Razorpay</strong>, using 256-bit encrypted PCI-DSS Level 1 certified infrastructure.
              </p>
            </div>

            <p className="pt-2"><strong>What we store in payment and refund records (MongoDB <code>payments</code> and <code>refunds</code>):</strong></p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li>Internal Payment Transaction ID and Booking Reference;</li>
              <li>Razorpay Gateway Order ID (<code>order_...</code>), Payment ID (<code>pay_...</code>), and cryptographic signature hash;</li>
              <li>Transaction Amount (in paise and INR), Currency (<code>INR</code>), Payment Method (<code>RAZORPAY</code>), and Status (<code>CREATED</code>, <code>VERIFIED</code>, <code>FAILED</code>);</li>
              <li>Refund Number, Refund Reason (<code>CUSTOMER_CANCELLATION</code>, <code>FLIGHT_CANCELLED</code>, <code>ADMIN_OVERRIDE</code>), calculated refund amount (paise/INR), Razorpay gateway refund identifier (<code>rfnd_...</code>), and completion timestamps.</li>
            </ul>
          </section>

          {/* Section 8 */}
          <section id="flight-tracking-data" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                8
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Flight Tracking & Radar Telemetry</h2>
            </div>

            <p>
              When you use our Live Flight Radar or add a flight to your tracked list (MongoDB <code>tracked_flights</code>):
            </p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li>We record your user identifier, flight number, tracking activation status, and notification preferences.</li>
              <li><strong>Simulation Transparency Notice:</strong> Flight telemetry (live coordinates, altitude, airspeed, heading, and delay probability events) presented on the Live Flight Radar may be generated by our internal simulation engine for demonstration, flight schedule monitoring, and testing purposes.</li>
            </ul>
          </section>

          {/* Section 9 */}
          <section id="notifications-data" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                9
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Notifications & Web Push Subscriptions</h2>
            </div>

            <p>
              We maintain in-app notification records (MongoDB <code>notifications</code>) and browser push notification endpoint subscriptions (MongoDB <code>push_subscriptions</code>) to deliver critical operational travel updates.
            </p>
            <p>
              Push subscriptions store your browser endpoint URL, public encryption key (<code>p256dh</code>), and authentication secret (<code>auth</code>) as specified in the W3C Web Push Protocol. You can revoke push notification permissions at any time through your web browser settings.
            </p>
          </section>

          {/* Section 10 */}
          <section id="device-storage-data" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                10
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Device Data, Storage & Token Usage</h2>
            </div>

            <p>
              To maintain authenticated session state and provide secure API communications, the Platform utilizes your browser&apos;s client-side storage (<code>localStorage</code> and <code>sessionStorage</code>):
            </p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li><code>smarttravel_access_token</code>: JSON Web Token (JWT) Bearer token used to authenticate REST API requests (expires in 24 hours).</li>
              <li><code>smarttravel_refresh_token</code>: Cryptographic refresh token saved if you select &quot;Remember Me&quot; (expires in 7 days).</li>
              <li><code>smarttravel_user</code>: Cached non-sensitive profile summary (User ID, Email, Full Name, Roles) for fast UI rendering.</li>
              <li><strong>HTTP Headers & Correlation:</strong> We log standard technical request metadata including IP address, user agent, HTTP method, and correlation tracking IDs (<code>X-Request-ID</code>) for security diagnostics and server performance.</li>
            </ul>
          </section>

          {/* Section 11 */}
          <section id="purposes-processing" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                11
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Purposes & Legal Grounds for Processing</h2>
            </div>

            <p>We process personal data only for lawful, specific, and transparent purposes:</p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
              <div className="p-3.5 rounded-xl bg-[#181A22] border border-white/5 space-y-1">
                <strong className="text-amber-400 font-bold block">1. Service Fulfillment</strong>
                <p className="text-slate-400">Processing reservations, generating PNRs, issuing PDF e-tickets and digital QR boarding passes, and holding hotel rooms.</p>
              </div>
              <div className="p-3.5 rounded-xl bg-[#181A22] border border-white/5 space-y-1">
                <strong className="text-amber-400 font-bold block">2. Payment & Refunds</strong>
                <p className="text-slate-400">Verifying payments with Razorpay, calculating automated cancellation refunds, and crediting bank accounts.</p>
              </div>
              <div className="p-3.5 rounded-xl bg-[#181A22] border border-white/5 space-y-1">
                <strong className="text-amber-400 font-bold block">3. Operational Updates</strong>
                <p className="text-slate-400">Delivering flight delay alerts, gate change advisories, check-in reminders, and payment receipts via in-app and push channels.</p>
              </div>
              <div className="p-3.5 rounded-xl bg-[#181A22] border border-white/5 space-y-1">
                <strong className="text-amber-400 font-bold block">4. Security & Compliance</strong>
                <p className="text-slate-400">Preventing fraudulent transactions, enforcing rate limits, securing accounts, and meeting statutory tax requirements.</p>
              </div>
            </div>
          </section>

          {/* Section 12 */}
          <section id="dynamic-pricing-transparency" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                12
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Dynamic Pricing & Pricing History</h2>
            </div>

            <p>
              SmartTravel operates an algorithmic <strong>Dynamic Pricing Engine</strong> that calculates real-time flight fares based on cabin seat inventory occupancy (0–40% occupancy: +0%; 40–60%: +5%; 60–80%: +10%; 80–90%: +20%; 90–100%: +30%), calendar seasonality, and holiday surges.
            </p>
            <p>
              To ensure consumer transparency, we provide itemized fare breakdowns before checkout and record price history snapshots. When you activate a <strong>Price Freeze</strong>, we lock the fare for 30 minutes, protecting your booking from real-time dynamic surges until expiration.
            </p>
          </section>

          {/* Section 13 */}
          <section id="data-sharing" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                13
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Data Sharing & Third-Party Service Providers</h2>
            </div>

            <p>
              We do NOT sell, rent, or trade your personal data. We share personal data only with trusted third parties strictly necessary to deliver travel services:
            </p>
            <ul className="list-disc pl-5 space-y-2 text-slate-400">
              <li><strong>Airlines & Travel Operators:</strong> Passenger names, birth dates, passport details, and seat selections are shared with operating carriers (e.g., Air India, IndiGo, SmartTravel Airways) to generate tickets and airport manifests.</li>
              <li><strong>Payment Gateway (Razorpay):</strong> Order totals, PNRs, and traveler contact information are transmitted to Razorpay Software Private Limited for payment processing and automated refund disbursement.</li>
              <li><strong>Infrastructure & Hosting:</strong> MongoDB Atlas for encrypted database persistence and cloud server infrastructure.</li>
              <li><strong>OpenStreetMap / Leaflet:</strong> Map tile servers for displaying flight radar visualization (no passenger PII is sent to map tile providers).</li>
              <li><strong>Legal Authorities:</strong> We may disclose data if required by applicable Indian or international law, court order, or airport security regulation.</li>
            </ul>
          </section>

          {/* Section 14 */}
          <section id="data-security" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                14
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Data Security & Storage Safeguards</h2>
            </div>

            <p>SmartTravel employs industry-standard administrative, technical, and physical safeguards:</p>
            <div className="space-y-2 text-xs text-slate-400">
              <p>• <strong>Transport Security:</strong> All data transmitted between your browser and our servers is encrypted using Transport Layer Security (TLS 1.2 / TLS 1.3).</p>
              <p>• <strong>Password Hashing:</strong> Passwords are protected using salted <code>BCrypt</code> cryptographic one-way hashing algorithms.</p>
              <p>• <strong>JWT Token Signatures:</strong> Authentication tokens are signed using 512-bit cryptographic keys (<code>HMAC-SHA512</code>) with strict expiration limits.</p>
              <p>• <strong>Role-Based Access Control:</strong> Strict authorization filters ensure users can only access their own reservations and preferences; administrative endpoints require verified <code>ROLE_ADMIN</code> privileges.</p>
            </div>
          </section>

          {/* Section 15 */}
          <section id="retention-deletion" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                15
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Data Retention & Account Deletion</h2>
            </div>

            <p>
              We retain personal data for as long as your account remains active or as needed to provide booking fulfillment, tax reporting, and fraud prevention.
            </p>
            <div className="p-4 rounded-2xl bg-[#181A22] border border-white/10 space-y-2 text-xs">
              <strong className="text-white block font-bold">Self-Service Account Deletion:</strong>
              <p>
                You can delete your account at any time through the <Link to="/account" className="text-amber-400 font-bold hover:underline">My Account</Link> settings panel. When you submit an account deletion request via <code>DELETE /v1/auth/me</code>:
              </p>
              <ul className="list-disc pl-5 space-y-1 text-slate-400">
                <li>Your user account record, saved travel preferences, and push notification tokens are permanently removed from the database;</li>
                <li>Your browser authentication tokens are invalidated immediately;</li>
                <li>Completed financial transaction and tax invoice records may be archived as required by Indian financial and tax laws.</li>
              </ul>
            </div>
          </section>

          {/* Section 16 */}
          <section id="user-rights" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                16
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Your Rights as a Data Principal</h2>
            </div>

            <p>Under the DPDP Act, 2023, you have the following enforceable rights regarding your personal data:</p>
            <ul className="list-disc pl-5 space-y-2 text-slate-400">
              <li><strong>Right to Access:</strong> Review a summary of your personal data processed by SmartTravel and the identities of third parties with whom it has been shared.</li>
              <li><strong>Right to Correction & Updating:</strong> Update inaccurate, incomplete, or outdated personal data directly via your <Link to="/account" className="text-amber-400 font-bold hover:underline">Profile Settings</Link>.</li>
              <li><strong>Right to Erasure:</strong> Request the deletion of your personal data once the travel booking purpose is completed.</li>
              <li><strong>Right to Grievance Redressal:</strong> Submit grievances to our designated Grievance Officer regarding any data processing concern.</li>
              <li><strong>Right to Nominate:</strong> Nominate another individual to exercise your data rights in the event of death or incapacity.</li>
            </ul>
          </section>

          {/* Section 17 */}
          <section id="children-privacy" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                17
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Children&apos;s & Minor Passenger Privacy</h2>
            </div>

            <p>
              SmartTravel accounts may only be created by individuals aged 18 years or older. Bookings for child or infant passengers (under age 18) must be made by a parent or lawful legal guardian who provides lawful consent for processing the child&apos;s name and travel document information for flight ticketing.
            </p>
          </section>

          {/* Section 18 */}
          <section id="international-transfers" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                18
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">International Data Transfers</h2>
            </div>

            <p>
              When you book international flight itineraries (e.g., flights departing India for international destinations), passenger manifest details are transmitted to destination immigration authorities and foreign operating carriers in compliance with international aviation treaties.
            </p>
          </section>

          {/* Section 19 */}
          <section id="cookies-tracking" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                19
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Cookies & Client-Side Storage Policy</h2>
            </div>

            <p>
              SmartTravel does NOT use tracking cookies or third-party advertising cookies. We use exclusively local browser storage (<code>localStorage</code> / <code>sessionStorage</code>) for security tokens and cached session state. For full details, please review our dedicated <Link to="/cookie-policy" className="text-amber-400 font-bold hover:underline">Cookie & Storage Policy</Link>.
            </p>
          </section>

          {/* Section 20 */}
          <section id="policy-changes" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                20
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Modifications to this Privacy Policy</h2>
            </div>

            <p>
              We may update this Privacy Policy periodically to reflect changes in our travel services, security architecture, or statutory regulations. Any updates will be published on this page with a revised &quot;Last Updated&quot; date. Material changes will be communicated via in-app notifications.
            </p>
          </section>

          {/* Section 21 */}
          <section id="contact-grievance" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                21
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Contact Information & Grievance Officer</h2>
            </div>

            <p>
              If you have any questions, wish to exercise your data principal rights, or have a privacy grievance, please contact us or our designated Grievance Officer:
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
              <div className="p-4 rounded-2xl bg-[#181A22] border border-white/10 space-y-1.5 text-xs">
                <strong className="text-amber-400 font-bold block text-sm">Privacy & Support Team</strong>
                <p><strong>Entity:</strong> [SMARTTRAVEL LEGAL ENTITY NAME]</p>
                <p><strong>Address:</strong> [REGISTERED OFFICE ADDRESS]</p>
                <p><strong>Email:</strong> <a href="mailto:privacy@smarttravel.com" className="text-amber-400 hover:underline">[PRIVACY CONTACT EMAIL - privacy@smarttravel.com]</a></p>
                <p><strong>Customer Support:</strong> <a href="mailto:support@smarttravel.com" className="text-amber-400 hover:underline">[SUPPORT EMAIL - support@smarttravel.com]</a></p>
              </div>

              <div className="p-4 rounded-2xl bg-[#181A22] border border-white/10 space-y-1.5 text-xs">
                <strong className="text-emerald-400 font-bold block text-sm">Data Protection & Grievance Officer</strong>
                <p><strong>Name / Title:</strong> [GRIEVANCE OFFICER NAME / DESIGNATION]</p>
                <p><strong>Grievance Email:</strong> <a href="mailto:grievance@smarttravel.com" className="text-amber-400 hover:underline">[GRIEVANCE EMAIL - grievance@smarttravel.com]</a></p>
                <p><strong>Response Time:</strong> Within 30 days as mandated by DPDP Rules</p>
              </div>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
};
