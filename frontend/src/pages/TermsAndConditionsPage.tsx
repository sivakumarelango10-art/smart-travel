import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  FileText,
  Shield,
  Plane,
  Building2,
  CreditCard,
  RefreshCw,
  AlertTriangle,
  Compass,
  Lock,
  Layers,
  ChevronRight,
  Printer,
  Scale,
  Sparkles
} from 'lucide-react';

interface Section {
  id: string;
  title: string;
  number: string;
}

const SECTIONS: Section[] = [
  { id: 'acceptance', title: 'Acceptance of Terms & Platform Role', number: '1' },
  { id: 'eligibility-accounts', title: 'Eligibility, Registration & Account Security', number: '2' },
  { id: 'flight-services', title: 'Flight Search, Reservations & E-Tickets', number: '3' },
  { id: 'hotel-services', title: 'Hotel Stays, Amenities & Room Holds', number: '4' },
  { id: 'passenger-accuracy', title: 'Passenger Information & Traveler Accuracy', number: '5' },
  { id: 'pricing-taxes', title: 'Pricing Structure, GST & Convenience Fees', number: '6' },
  { id: 'dynamic-pricing', title: 'Dynamic Pricing Engine & Real-Time Adjustments', number: '7' },
  { id: 'price-freeze', title: 'Price Freeze Program & 30-Minute Fare Lock', number: '8' },
  { id: 'seat-room-selection', title: 'Interactive Seat Maps & Room Availability', number: '9' },
  { id: 'booking-confirmation', title: 'Booking Confirmation & Seat Hold Expiry', number: '10' },
  { id: 'payment-terms', title: 'Payment Processing & Razorpay Gateway', number: '11' },
  { id: 'cancellation-policy', title: 'Cancellation Policy & Time-Based Tiers', number: '12' },
  { id: 'refund-terms', title: 'Refund Calculations, Automated Processing & Timelines', number: '13' },
  { id: 'flight-changes-disruptions', title: 'Schedule Changes, Delays & Airline Disruptions', number: '14' },
  { id: 'airline-hotel-policies', title: 'Third-Party Airline & Hotel Operator Rules', number: '15' },
  { id: 'live-tracking-disclaimer', title: 'Live Flight Radar & Simulated Telemetry Disclaimer', number: '16' },
  { id: 'user-conduct', title: 'User Conduct & Prohibited Activities', number: '17' },
  { id: 'intellectual-property', title: 'Intellectual Property Rights', number: '18' },
  { id: 'availability-disclaimers', title: 'Service Availability & Warranty Disclaimers', number: '19' },
  { id: 'limitation-liability', title: 'Limitation of Liability & Force Majeure', number: '20' },
  { id: 'indemnification', title: 'Indemnification Obligations', number: '21' },
  { id: 'governing-law', title: 'Dispute Resolution, Arbitration & Governing Law', number: '22' },
  { id: 'changes-to-terms', title: 'Modifications to Terms & Conditions', number: '23' },
  { id: 'contact-support', title: 'Customer Support & Legal Notices', number: '24' },
];

export const TermsAndConditionsPage: React.FC = () => {
  const [activeSection, setActiveSection] = useState<string>('acceptance');
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);

  useEffect(() => {
    document.title = 'Terms & Conditions | SmartTravel';
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
            <Scale className="w-3.5 h-3.5" />
            <span>Authoritative Travel Facilitation Terms</span>
          </div>

          <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight">
            SmartTravel Terms & Conditions
          </h1>

          <p className="text-sm text-slate-400 leading-relaxed">
            Please read these Terms and Conditions carefully. They constitute a legally binding agreement between you and SmartTravel governing your access to our flight and hotel booking platform, dynamic pricing engine, cancellation and refund workflows, and flight tracking systems.
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
            <span>Print Terms</span>
          </button>
          <Link
            to="/privacy-policy"
            className="px-4 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-200 text-xs font-bold flex items-center gap-2 border border-white/10 transition"
          >
            <Shield className="w-4 h-4 text-amber-400" />
            <span>View Privacy Policy</span>
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

        {/* Terms Content Body */}
        <main className="lg:col-span-8 space-y-10 text-xs sm:text-sm leading-relaxed">
          {/* Section 1 */}
          <section id="acceptance" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                1
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Acceptance of Terms & Platform Role</h2>
            </div>

            <p>
              By accessing, browsing, registering on, or using <strong>SmartTravel</strong> (the &quot;Platform&quot;), operated by <strong>[SMARTTRAVEL LEGAL ENTITY NAME]</strong> (&quot;SmartTravel&quot;, &quot;we&quot;, &quot;us&quot;, or &quot;our&quot;), you acknowledge that you have read, understood, and agree to be bound by these Terms and Conditions (&quot;Terms&quot;) and our <Link to="/privacy-policy" className="text-amber-400 font-bold hover:underline">Privacy Policy</Link>.
            </p>
            <div className="p-4 rounded-2xl bg-[#181A22] border border-white/10 space-y-2 text-xs">
              <strong className="text-amber-400 block font-bold">Important Distinction Regarding SmartTravel&apos;s Role:</strong>
              <p>
                SmartTravel acts as an online travel technology and reservation facilitation platform. We facilitate searches, reservations, fare locking, payment collection, and digital ticket issuance between travelers and third-party operating carriers (e.g., airlines) and accommodation providers (e.g., hotels).
              </p>
              <p>
                Unless explicitly stated otherwise, SmartTravel does not own or operate aircraft or hotel properties. The ultimate provision of flight transport and lodging is governed by the respective operating airline or hotel provider&apos;s contract of carriage and property policies.
              </p>
            </div>
          </section>

          {/* Section 2 */}
          <section id="eligibility-accounts" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                2
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Eligibility, Registration & Account Security</h2>
            </div>

            <p>
              To create an account or book travel through SmartTravel, you must be at least <strong>18 years of age</strong> and legally capable of entering into binding contracts under applicable Indian law (including the Indian Contract Act, 1872).
            </p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li><strong>Account Credentials:</strong> You are responsible for maintaining the confidentiality of your login email and password. All actions conducted through your account are deemed authorized by you.</li>
              <li><strong>Password Strength:</strong> New accounts require passwords with a minimum of 8 characters, containing uppercase, lowercase, numeric, and special characters. Passwords are saved using salted <code>BCrypt</code> one-way hashing.</li>
              <li><strong>Account Deletion:</strong> You may permanently terminate your account at any time via the <Link to="/account" className="text-amber-400 font-bold hover:underline">My Account</Link> dashboard.</li>
            </ul>
          </section>

          {/* Section 3 */}
          <section id="flight-services" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                3
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Flight Search, Reservations & E-Tickets</h2>
            </div>

            <p>
              SmartTravel provides real-time flight schedules, cabin inventory availability, and authoritative electronic ticket generation:
            </p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li><strong>PNR Generation:</strong> Successful flight bookings generate a unique 6-character Passenger Name Record (PNR) stored in the database.</li>
              <li><strong>Authoritative E-Ticket:</strong> Upon payment verification, an official Electronic Ticket (ETKT) is issued with route timings, baggage allowances, passenger details, and printable PDF downloads.</li>
              <li><strong>Digital QR Boarding Pass:</strong> Passengers who complete Web Check-In receive a digital boarding pass with a dynamically verified QR token scannable at airport turnstiles and security gates.</li>
            </ul>
          </section>

          {/* Section 4 */}
          <section id="hotel-services" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                4
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Hotel Stays, Amenities & Room Holds</h2>
            </div>

            <p>
              When booking accommodations on SmartTravel:
            </p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li><strong>15-Minute Inventory Hold:</strong> Initiating room reservation checkout places a temporary 15-minute hold on the selected room category to prevent double booking. If checkout is not completed within 15 minutes, the room inventory is automatically released.</li>
              <li><strong>Check-in Times & House Rules:</strong> Hotel check-in/out times, security deposits, and age requirements are set by individual properties. Standard government photo ID (Aadhaar, Passport, Driving License) is mandatory at hotel reception.</li>
            </ul>
          </section>

          {/* Section 5 */}
          <section id="passenger-accuracy" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                5
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Passenger Information & Traveler Accuracy</h2>
            </div>

            <p>
              It is the user&apos;s sole responsibility to ensure that all passenger names, birth dates, titles, and passport numbers entered during booking match the government-issued photo ID used for travel exactly. Airlines enforce strict identity verification at airport check-in and will deny boarding if ticket names do not match official travel documents.
            </p>
          </section>

          {/* Section 6 */}
          <section id="pricing-taxes" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                6
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Pricing Structure, GST & Convenience Fees</h2>
            </div>

            <p>
              All prices displayed on SmartTravel are quoted in <strong>Indian Rupees (INR)</strong>.
            </p>
            <div className="p-4 rounded-2xl bg-[#181A22] border border-white/10 space-y-1.5 text-xs">
              <strong className="text-white block font-bold">Itemized Fare Breakdown Elements:</strong>
              <p>• <strong>Base Fare:</strong> Airline transportation charge based on cabin class and distance.</p>
              <p>• <strong>Taxes & Airport Surcharges:</strong> Statutory Goods & Services Tax (GST), Passenger Service Fees, and airport development levies.</p>
              <p>• <strong>Platform Convenience Fees:</strong> Nominal technology facilitation charge (e.g., ₹150 per passenger) itemized before payment.</p>
              <p>• <strong>Final Total:</strong> The final total amount displayed in the review screen represents the all-inclusive sum charged to your payment card/account.</p>
            </div>
          </section>

          {/* Section 7 */}
          <section id="dynamic-pricing" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                7
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Dynamic Pricing Engine & Real-Time Adjustments</h2>
            </div>

            <p>
              SmartTravel utilizes an automated <strong>Dynamic Pricing Engine</strong> that recalibrates flight fares in real-time. Prices may fluctuate before booking confirmation based on:
            </p>
            <ul className="list-disc pl-5 space-y-1 text-slate-400">
              <li><strong>Cabin Occupancy Demand:</strong> 0–40% seats booked (+0%), 40–60% (+5%), 60–80% (+10%), 80–90% (+20%), and 90–100% (+30%).</li>
              <li><strong>Seasonal Adjustments:</strong> Peak travel season demand curves.</li>
              <li><strong>Holiday Surges:</strong> Public holiday and festival flight route adjustments.</li>
            </ul>
            <p>
              We never guarantee that unreserved fares will remain static. Prices displayed in search results may update until you initiate a reservation hold or apply a valid Price Freeze.
            </p>
          </section>

          {/* Section 8 */}
          <section id="price-freeze" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                8
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Price Freeze Program & 30-Minute Fare Lock</h2>
            </div>

            <p>
              SmartTravel offers a <strong>Price Freeze</strong> feature allowing authenticated users to lock in a flight fare for up to <strong>30 minutes</strong>:
            </p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li><strong>Protection Against Surges:</strong> An active Price Freeze protects the fare against subsequent dynamic price surges during the 30-minute window.</li>
              <li><strong>Single Active Freeze:</strong> Users may hold one active price freeze per flight cabin. If expired or unused, the freeze transitions to <code>EXPIRED</code>.</li>
              <li><strong>Application:</strong> To utilize the locked price, the user must apply the freeze during checkout before the 30-minute countdown elapses.</li>
            </ul>
          </section>

          {/* Section 9 */}
          <section id="seat-room-selection" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                9
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Interactive Seat Maps & Room Availability</h2>
            </div>

            <p>
              Our interactive aircraft seat map allows passengers to select specific physical seats (Standard, Window, Aisle, or Extra Legroom). Seat selections and room holds are held atomically during checkout. If another user completes payment for a seat first, the unreserved seat will be released and alternative selection will be required.
            </p>
          </section>

          {/* Section 10 */}
          <section id="booking-confirmation" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                10
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Booking Confirmation & Seat Hold Expiry</h2>
            </div>

            <p>
              When you submit passenger details and proceed to checkout, a temporary <strong>15-minute payment window</strong> is initiated. If payment is not verified within 15 minutes, the booking transitions to <code>EXPIRED</code> and reserved seat inventory is returned to the public pool.
            </p>
          </section>

          {/* Section 11 */}
          <section id="payment-terms" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                11
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Payment Processing & Razorpay Gateway</h2>
            </div>

            <p>
              All online payments are securely processed through our authorized payment partner, <strong>Razorpay</strong>. Payment confirmation requires cryptographic HMAC signature verification. In sandbox/demonstration environments, 1-Click test simulation is provided for verification.
            </p>
          </section>

          {/* Section 12 */}
          <section id="cancellation-policy" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                12
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Cancellation Policy & Time-Based Tiers</h2>
            </div>

            <p>
              Flight booking cancellations on SmartTravel are governed by our authoritative, time-based cancellation policy engine:
            </p>

            <div className="overflow-x-auto pt-2">
              <table className="w-full text-left text-xs border border-white/10 rounded-2xl overflow-hidden">
                <thead className="bg-[#181A22] text-white uppercase font-bold text-[10px]">
                  <tr>
                    <th className="p-3 border-b border-white/10">Cancellation Timeframe</th>
                    <th className="p-3 border-b border-white/10">Refund Percentage</th>
                    <th className="p-3 border-b border-white/10">Policy Details</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/5 bg-[#14161F]">
                  <tr>
                    <td className="p-3 font-bold text-white">&gt; 168 Hours (&gt; 7 Days) before departure</td>
                    <td className="p-3 font-bold text-emerald-400">100% Refund</td>
                    <td className="p-3 text-slate-400">Full refund of gross booking amount paid.</td>
                  </tr>
                  <tr>
                    <td className="p-3 font-bold text-white">24 to 168 Hours (1–7 Days) before departure</td>
                    <td className="p-3 font-bold text-amber-400">50% Partial Refund</td>
                    <td className="p-3 text-slate-400">50% partial refund calculated via exact paise arithmetic.</td>
                  </tr>
                  <tr>
                    <td className="p-3 font-bold text-white">&lt; 24 Hours before departure</td>
                    <td className="p-3 font-bold text-rose-400">0% (No Refund)</td>
                    <td className="p-3 text-slate-400">Late cancellation window; fare is strictly non-refundable.</td>
                  </tr>
                  <tr>
                    <td className="p-3 font-bold text-white">After Scheduled Departure</td>
                    <td className="p-3 font-bold text-rose-400">0% (No Show / No Refund)</td>
                    <td className="p-3 text-slate-400">Flight has departed; ticket is marked as completed or expired.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          {/* Section 13 */}
          <section id="refund-terms" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                13
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Refund Calculations, Automated Processing & Timelines</h2>
            </div>

            <p>
              When a booking cancellation is confirmed, our backend executes an automated refund workflow (MongoDB <code>refunds</code>):
            </p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li><strong>Automated Execution:</strong> Refunds are initiated automatically upon user or administrator cancellation.</li>
              <li><strong>Payment Gateway Integration:</strong> Refund commands are transmitted to Razorpay referencing the original transaction ID.</li>
              <li><strong>Disbursement Timeline:</strong> While SmartTravel initiates refunds instantly, banking settlement cycles typically take <strong>5 to 7 business days</strong> to reflect in your bank account or card statement.</li>
              <li><strong>Tracking:</strong> You can monitor live refund status (<code>REQUESTED</code>, <code>PROCESSING</code>, <code>COMPLETED</code>) anytime in <Link to="/my-bookings" className="text-amber-400 font-bold hover:underline">My Bookings</Link>.</li>
            </ul>
          </section>

          {/* Section 14 */}
          <section id="flight-changes-disruptions" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                14
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Schedule Changes, Delays & Airline Disruptions</h2>
            </div>

            <p>
              Airlines reserve the right to alter flight departure schedules, aircraft models, or gate allocations due to weather, air traffic control directives, or operational contingencies. When an airline cancels a flight, SmartTravel automatically triggers <strong>100% full refund eligibility</strong> regardless of the booking cancellation window.
            </p>
          </section>

          {/* Section 15 */}
          <section id="airline-hotel-policies" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                15
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Third-Party Airline & Hotel Operator Rules</h2>
            </div>

            <p>
              Travelers agree to abide by the specific terms of carriage and property rules established by operating carriers and hotels:
            </p>
            <ul className="list-disc pl-5 space-y-1 text-slate-400">
              <li><strong>Baggage Allowances:</strong> Standard cabin (7 kg) and check-in baggage allowances are determined by the airline and cabin class. Excess baggage fees are payable directly to the carrier.</li>
              <li><strong>Airport Check-in Deadlines:</strong> Airport check-in counters typically close 60 minutes prior to domestic departure and 120 minutes prior to international departure.</li>
            </ul>
          </section>

          {/* Section 16 */}
          <section id="live-tracking-disclaimer" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                16
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Live Flight Radar & Simulated Telemetry Disclaimer</h2>
            </div>

            <div className="p-4 rounded-2xl bg-amber-400/10 border border-amber-400/20 text-xs text-slate-300 space-y-2">
              <strong className="text-amber-400 font-bold flex items-center gap-1.5">
                <Compass className="w-4 h-4" /> Live Airspace Radar Simulation Notice
              </strong>
              <p>
                The Live Flight Radar and transponder telemetry presented on SmartTravel (including coordinates, altitude, airspeed, and simulated delays) may be generated by our flight simulation engine for demonstrative, scheduling, and system testing purposes.
              </p>
              <p>
                Live radar telemetry must NOT be used for real-world aircraft navigation, emergency operations, or air traffic safety. Travelers should always consult official airport terminal displays for gate announcements.
              </p>
            </div>
          </section>

          {/* Section 17 */}
          <section id="user-conduct" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                17
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">User Conduct & Prohibited Activities</h2>
            </div>

            <p>You agree not to:</p>
            <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
              <li>Make speculative, false, or fraudulent reservations;</li>
              <li>Use automated scrapers, bots, or scripts to harvest flight fares or seat availability without authorization;</li>
              <li>Attempt to circumvent security filters, payment verification checks, or API rate limits;</li>
              <li>Impersonate any person or provide fabricated passenger identification credentials.</li>
            </ul>
          </section>

          {/* Section 18 */}
          <section id="intellectual-property" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                18
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Intellectual Property Rights</h2>
            </div>

            <p>
              All software code, user interface designs, logos, graphics, dynamic pricing algorithms, and database schemas associated with SmartTravel are the proprietary property of <strong>[SMARTTRAVEL LEGAL ENTITY NAME]</strong> and protected by Indian and international copyright and intellectual property laws.
            </p>
          </section>

          {/* Section 19 */}
          <section id="availability-disclaimers" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                19
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Service Availability & Warranty Disclaimers</h2>
            </div>

            <p>
              SmartTravel is provided on an &quot;AS IS&quot; and &quot;AS AVAILABLE&quot; basis. While we maintain high availability through clustered cloud architecture and automated health probes, we do not warrant that platform operations will be entirely error-free or uninterrupted at all times.
            </p>
          </section>

          {/* Section 20 */}
          <section id="limitation-liability" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                20
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Limitation of Liability & Force Majeure</h2>
            </div>

            <p>
              To the maximum extent permitted by applicable Indian law, SmartTravel shall not be liable for any indirect, incidental, or consequential damages resulting from flight cancellations, denied boarding, hotel property conditions, or events of Force Majeure (including extreme weather, acts of God, war, strikes, or governmental travel restrictions).
            </p>
            <p>
              SmartTravel&apos;s total aggregate liability arising out of or related to any booking shall not exceed the total transaction amount paid by the customer for that specific booking.
            </p>
          </section>

          {/* Section 21 */}
          <section id="indemnification" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                21
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Indemnification Obligations</h2>
            </div>

            <p>
              You agree to indemnify, defend, and hold harmless SmartTravel, its directors, employees, and technology affiliates from any claims, liabilities, damages, or expenses arising from your violation of these Terms or infringement of any third-party rights.
            </p>
          </section>

          {/* Section 22 */}
          <section id="governing-law" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                22
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Dispute Resolution, Arbitration & Governing Law</h2>
            </div>

            <p>
              These Terms shall be governed by and construed in accordance with the <strong>laws of India</strong>. Any dispute, controversy, or claim arising out of or relating to these Terms shall be subject to the exclusive jurisdiction of the competent courts in <strong>[JURISDICTION CITY, e.g. New Delhi, India]</strong>.
            </p>
          </section>

          {/* Section 23 */}
          <section id="changes-to-terms" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                23
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Modifications to Terms & Conditions</h2>
            </div>

            <p>
              We reserve the right to revise these Terms at any time. The updated version will be posted on this page with an updated &quot;Last Updated&quot; date. Continued use of SmartTravel following revisions indicates acceptance of the amended Terms.
            </p>
          </section>

          {/* Section 24 */}
          <section id="contact-support" className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
            <div className="flex items-center gap-3 pb-3 border-b border-white/10">
              <div className="w-8 h-8 rounded-xl bg-amber-400/10 text-amber-400 flex items-center justify-center font-black font-mono">
                24
              </div>
              <h2 className="text-lg sm:text-xl font-black text-white">Customer Support & Legal Notices</h2>
            </div>

            <p>For support inquiries, booking assistance, or formal legal notices, please reach out to:</p>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
              <div className="p-4 rounded-2xl bg-[#181A22] border border-white/10 space-y-1.5 text-xs">
                <strong className="text-amber-400 font-bold block text-sm">Customer Support Helpdesk</strong>
                <p><strong>Support Email:</strong> <a href="mailto:support@smarttravel.com" className="text-amber-400 hover:underline">[SUPPORT EMAIL - support@smarttravel.com]</a></p>
                <p><strong>Support Desk:</strong> Available 24/7 for booking modifications and refund assistance</p>
              </div>

              <div className="p-4 rounded-2xl bg-[#181A22] border border-white/10 space-y-1.5 text-xs">
                <strong className="text-emerald-400 font-bold block text-sm">Corporate & Legal Inquiries</strong>
                <p><strong>Company:</strong> [SMARTTRAVEL LEGAL ENTITY NAME]</p>
                <p><strong>Registered Office:</strong> [REGISTERED OFFICE ADDRESS]</p>
                <p><strong>Legal Email:</strong> <a href="mailto:legal@smarttravel.com" className="text-amber-400 hover:underline">[LEGAL EMAIL - legal@smarttravel.com]</a></p>
              </div>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
};
