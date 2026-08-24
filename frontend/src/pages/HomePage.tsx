import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ShieldCheck,
  Zap,
  RotateCcw,
  Sparkles,
  ArrowRight,
  RefreshCw,
  Compass,
  ChevronLeft,
  ChevronRight,
  Tag,
  MapPin,
  Award,
  CreditCard,
  Headphones,
  Radio,
  Plane,
  Building2,
  Lock
} from 'lucide-react';
import { FlightSearchWidget } from '../components/FlightSearchWidget';
import { RecommendationsSection } from '../components/RecommendationsSection';
import { OptimizedImage } from '../components/OptimizedImage';
import { LiveAirspaceFeed } from '../components/LiveAirspaceFeed';
import { healthService } from '../services/healthService';
import { HealthData } from '../types/api';

const HERO_DESTINATIONS = [
  {
    name: 'GOA',
    country: 'India',
    tagline: 'Dive into golden beaches, sunsets, and unforgettable coastal escapes.',
    from: 'DEL',
    to: 'GOI',
    image: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=1920&q=80',
    price: '3,999',
  },
  {
    name: 'DUBAI',
    country: 'United Arab Emirates',
    tagline: 'Experience futuristic skylines, luxury desert safaris, and world-class shopping.',
    from: 'BOM',
    to: 'DXB',
    image: 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1920&q=80',
    price: '12,499',
  },
  {
    name: 'BALI',
    country: 'Indonesia',
    tagline: 'Discover tropical paradises, sacred temples, and lush emerald terraces.',
    from: 'DEL',
    to: 'DPS',
    image: 'https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1920&q=80',
    price: '16,999',
  },
  {
    name: 'SINGAPORE',
    country: 'Singapore',
    tagline: 'Explore a vibrant global crossroads of lush green gardens and modern innovation.',
    from: 'BLR',
    to: 'SIN',
    image: 'https://images.unsplash.com/photo-1525625293386-3f8f99389edd?auto=format&fit=crop&w=1920&q=80',
    price: '14,299',
  },
];

const POPULAR_DESTINATIONS = [
  {
    city: 'Goa',
    country: 'India',
    from: 'DEL',
    to: 'GOI',
    price: '3,999',
    tag: 'Beach & Coastal',
    image: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=800&q=80',
  },
  {
    city: 'Dubai',
    country: 'UAE',
    from: 'BOM',
    to: 'DXB',
    price: '12,499',
    tag: 'Luxury Skyline',
    image: 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=800&q=80',
  },
  {
    city: 'Singapore',
    country: 'Singapore',
    from: 'BLR',
    to: 'SIN',
    price: '14,299',
    tag: 'Modern Gardens',
    image: 'https://images.unsplash.com/photo-1525625293386-3f8f99389edd?auto=format&fit=crop&w=800&q=80',
  },
  {
    city: 'Bali',
    country: 'Indonesia',
    from: 'DEL',
    to: 'DPS',
    price: '16,999',
    tag: 'Tropical Island',
    image: 'https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80',
  },
];

const OFFERS = [
  {
    badge: 'DOMESTIC FLIGHTS',
    title: 'Flat 15% OFF on Commercial Flights',
    desc: 'Book your flight across 20+ major airports with instant fare discount.',
    code: 'SMARTFLY',
  },
  {
    badge: 'FARE LOCK GUARANTEE',
    title: 'Freeze Fares for 48 Hours',
    desc: 'Lock in current flight prices and protect yourself from sudden surge pricing.',
    code: 'FARELOCK',
  },
  {
    badge: 'ZERO RISK CANCELLATION',
    title: 'Automated Gateway Refunds',
    desc: 'Instant refund calculation with direct bank reimbursement on cancellations.',
    code: 'AUTOREFUND',
  },
];

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [currentHeroIdx, setCurrentHeroIdx] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const [health, setHealth] = useState<HealthData | null>(null);
  const [healthLoading, setHealthLoading] = useState<boolean>(true);

  // Auto carousel rotation
  useEffect(() => {
    if (isPaused) return;
    const interval = setInterval(() => {
      setCurrentHeroIdx((prev) => (prev + 1) % HERO_DESTINATIONS.length);
    }, 6000);
    return () => clearInterval(interval);
  }, [isPaused]);

  const fetchHealth = async () => {
    setHealthLoading(true);
    try {
      const res = await healthService.getHealth();
      setHealth(res.data);
    } catch {
      setHealth(null);
    } finally {
      setHealthLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  const handleQuickRoute = (from: string, to: string) => {
    const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
    navigate(`/flights?origin=${from}&destination=${to}&departureDate=${tomorrow}&cabinClass=ECONOMY&passengers=1`);
  };

  const currentHero = HERO_DESTINATIONS[currentHeroIdx];

  return (
    <div className="space-y-16 pb-16">
      {/* ======================================================== */}
      {/* 1. HERO SECTION WITH TRAVEL SEARCH                      */}
      {/* ======================================================== */}
      <section
        className="relative rounded-3xl overflow-hidden shadow-xl bg-primary text-white p-6 sm:p-10 lg:p-12 border border-slate-800"
        onMouseEnter={() => setIsPaused(true)}
        onMouseLeave={() => setIsPaused(false)}
      >
        {/* Background Image with Dark Navy Gradient Overlay */}
        <div className="absolute inset-0 z-0">
          <img
            src={`${currentHero.image}&fm=webp`}
            alt={currentHero.name}
            className="w-full h-full object-cover object-center opacity-25 scale-105 transition-all duration-1000"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-primary via-primary/80 to-primary/60"></div>
        </div>

        {/* Hero Content */}
        <div className="relative z-10 space-y-8 max-w-5xl mx-auto">
          {/* Top Tag & Featured Location */}
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-900/90 border border-slate-700 text-secondary text-xs font-bold">
              <span className="w-2 h-2 rounded-full bg-secondary animate-pulse" />
              <span>Real-Time Flight & Hotel Ecosystem</span>
            </div>

            <div className="hidden sm:flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-900/80 border border-slate-700 text-xs font-semibold text-slate-300">
              <MapPin className="w-3.5 h-3.5 text-accent" />
              <span>Featured: <strong>{currentHero.name}</strong>, {currentHero.country}</span>
            </div>
          </div>

          {/* Headline & Value Proposition */}
          <div className="text-center space-y-3">
            <h1 className="text-3xl sm:text-5xl lg:text-6xl font-black tracking-tight leading-tight text-white">
              Explore • Book • Journey
            </h1>
            <p className="text-slate-300 text-sm sm:text-base max-w-2xl mx-auto font-normal leading-relaxed">
              Book flights with interactive seat selection, reserve hotels with 3D virtual previews, and track your journeys with real-time live flight radar.
            </p>
          </div>

          {/* Embedded Search Widget */}
          <div className="pt-2 text-left">
            <FlightSearchWidget />
          </div>

          {/* Carousel Dot Indicators */}
          <div className="flex items-center justify-between pt-2 border-t border-slate-800/80 text-xs">
            <div className="flex items-center gap-2">
              {HERO_DESTINATIONS.map((d, i) => (
                <button
                  key={d.name}
                  type="button"
                  onClick={() => setCurrentHeroIdx(i)}
                  className={`h-1.5 rounded-full transition-all duration-200 ${
                    i === currentHeroIdx ? 'w-6 bg-secondary' : 'w-2 bg-slate-700 hover:bg-slate-500'
                  }`}
                  aria-label={`View ${d.name}`}
                />
              ))}
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() =>
                  setCurrentHeroIdx((prev) => (prev - 1 + HERO_DESTINATIONS.length) % HERO_DESTINATIONS.length)
                }
                className="p-1.5 rounded-lg bg-slate-900/80 hover:bg-slate-800 text-white border border-slate-700 transition"
                aria-label="Previous destination"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                type="button"
                onClick={() => setCurrentHeroIdx((prev) => (prev + 1) % HERO_DESTINATIONS.length)}
                className="p-1.5 rounded-lg bg-slate-900/80 hover:bg-slate-800 text-white border border-slate-700 transition"
                aria-label="Next destination"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* ======================================================== */}
      {/* 2. SPECIAL OFFERS & PROMOTIONS                          */}
      {/* ======================================================== */}
      <section id="offers" className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <div className="flex items-center gap-1.5 text-accent text-xs font-bold uppercase tracking-wider">
              <Tag className="w-4 h-4" />
              <span>SmartTravel Perks & Promotions</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-black text-primary tracking-tight mt-1">
              Exclusive Travel Offers
            </h2>
          </div>
          <span className="text-xs text-slate-500 hidden sm:inline font-semibold">Valid on domestic & international routes</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {OFFERS.map((offer, idx) => (
            <div
              key={idx}
              className="rounded-2xl bg-white border border-slate-200 p-6 flex flex-col justify-between space-y-4 hover:border-slate-300 hover:shadow-card transition duration-200"
            >
              <div className="space-y-2">
                <span className="inline-block px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider bg-slate-100 text-slate-700 border border-slate-200">
                  {offer.badge}
                </span>
                <h3 className="font-bold text-primary text-base leading-snug">{offer.title}</h3>
                <p className="text-xs text-slate-500 leading-relaxed">{offer.desc}</p>
              </div>

              <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                <div className="px-3 py-1 rounded-lg bg-slate-50 border border-dashed border-slate-300 text-xs font-mono font-bold text-secondary">
                  <span>{offer.code}</span>
                </div>
                <button
                  type="button"
                  onClick={() => navigate('/flights')}
                  className="text-xs font-bold text-primary hover:text-secondary flex items-center gap-1 transition"
                >
                  <span>Book Flight</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ======================================================== */}
      {/* 3. LIVE AIRSPACE RADAR PREVIEW                          */}
      {/* ======================================================== */}
      <section className="space-y-6">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <div className="flex items-center gap-1.5 text-secondary text-xs font-bold uppercase tracking-wider">
              <Radio className="w-4 h-4 animate-pulse" />
              <span>Real-Time Flight Telemetry</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-black text-primary tracking-tight mt-1">
              Live Airspace Operations Stream
            </h2>
            <p className="text-xs sm:text-sm text-slate-500 mt-1">
              Active commercial flights tracked via real-time WebSocket feeds with live altitude, speed, and gate statuses.
            </p>
          </div>

          <button
            type="button"
            onClick={() => navigate('/live-tracker')}
            className="px-4 py-2.5 rounded-xl bg-primary hover:bg-primary-hover text-white text-xs font-bold flex items-center gap-1.5 transition shadow-sm"
          >
            <span>Open Airspace Radar</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <LiveAirspaceFeed
          compact={false}
          limit={6}
          onSelectFlight={(flightNumber) => navigate(`/tracked-flights?flight=${flightNumber}`)}
        />
      </section>

      {/* ======================================================== */}
      {/* 4. POPULAR DESTINATIONS                                  */}
      {/* ======================================================== */}
      <section id="destinations" className="space-y-6">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <div className="flex items-center gap-1.5 text-secondary text-xs font-bold uppercase tracking-wider">
              <Compass className="w-4 h-4" />
              <span>Popular Routes</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-black text-primary tracking-tight mt-1">
              Trending Flight Destinations
            </h2>
            <p className="text-xs sm:text-sm text-slate-500 mt-1">
              Instant booking with guaranteed seat map selection and zero hidden fees.
            </p>
          </div>

          <button
            type="button"
            onClick={() => navigate('/flights')}
            className="px-4 py-2 rounded-xl bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 text-xs font-bold flex items-center gap-1.5 transition shadow-sm"
          >
            <span>View All Flights</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {POPULAR_DESTINATIONS.map((dest, i) => (
            <div
              key={i}
              onClick={() => handleQuickRoute(dest.from, dest.to)}
              className="rounded-2xl bg-white border border-slate-200 hover:border-slate-300 hover:shadow-card overflow-hidden cursor-pointer group flex flex-col justify-between transition-all duration-200"
            >
              <div className="relative h-44 overflow-hidden bg-slate-100">
                <OptimizedImage
                  src={dest.image}
                  alt={dest.city}
                  aspectRatio="16/10"
                  className="w-full h-full"
                  imageClassName="dest-img group-hover:scale-105 transition-transform duration-300 object-cover"
                />
                <span className="absolute top-3 left-3 px-2.5 py-1 rounded-full bg-primary/85 backdrop-blur-md text-white text-[10px] font-bold">
                  {dest.tag}
                </span>
                <span className="absolute bottom-3 right-3 px-2.5 py-1 rounded-lg bg-accent text-white text-xs font-black shadow-md">
                  From ₹{dest.price}
                </span>
              </div>

              <div className="p-4 space-y-2">
                <div className="flex items-center justify-between">
                  <h3 className="font-black text-primary text-base group-hover:text-secondary transition">
                    {dest.city}
                  </h3>
                  <span className="text-xs font-mono font-bold text-slate-600 bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
                    {dest.to}
                  </span>
                </div>
                <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
                  <span>Direct flight from <strong>{dest.from}</strong></span>
                  <span className="text-secondary font-bold flex items-center gap-1 group-hover:translate-x-1 transition-transform">
                    Search <ArrowRight className="w-3 h-3" />
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ======================================================== */}
      {/* 5. PERSONALIZED RECOMMENDATIONS                          */}
      {/* ======================================================== */}
      <RecommendationsSection />

      {/* ======================================================== */}
      {/* 6. TRUST & ENTERPRISE ARCHITECTURE PILLARS              */}
      {/* ======================================================== */}
      <section className="rounded-3xl bg-primary text-white border border-slate-800 p-8 sm:p-12 shadow-xl space-y-8">
        <div className="text-center space-y-2 max-w-2xl mx-auto">
          <span className="text-xs font-bold uppercase tracking-widest text-secondary">
            Enterprise Architecture & Reliability
          </span>
          <h2 className="text-2xl sm:text-4xl font-black text-white tracking-tight">
            Built for Modern, Confident Travel
          </h2>
          <p className="text-xs sm:text-sm text-slate-400">
            Experience an elevated travel ecosystem designed for instant speed, real-time telemetry, and effortless booking.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 pt-2">
          <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-2.5">
            <div className="w-10 h-10 rounded-xl bg-secondary/10 text-secondary border border-secondary/20 flex items-center justify-center">
              <Zap className="w-5 h-5" />
            </div>
            <h3 className="font-bold text-white text-sm">Atomic Seat & Room Locking</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Interactive physical aircraft seat maps and hotel room reservations with 15-minute concurrency hold locks.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-2.5">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center">
              <RotateCcw className="w-5 h-5" />
            </div>
            <h3 className="font-bold text-white text-sm">Disruption Auto-Refunds</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Automated flight schedule monitoring with direct payment gateway refund reconciliation on cancellations.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-2.5">
            <div className="w-10 h-10 rounded-xl bg-accent/10 text-accent border border-accent/20 flex items-center justify-center">
              <Lock className="w-5 h-5" />
            </div>
            <h3 className="font-bold text-white text-sm">Dynamic Price Freeze</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Lock in current fares for 48 hours to protect your travel plans from surge pricing spikes.
            </p>
          </div>
        </div>
      </section>

      {/* ======================================================== */}
      {/* 7. LIVE PLATFORM HEALTH & STATUS BADGE                  */}
      {/* ======================================================== */}
      <section className="max-w-md mx-auto">
        <div className="p-3.5 rounded-2xl bg-white border border-slate-200 shadow-sm flex items-center justify-between text-xs">
          <div className="flex items-center gap-2.5">
            <div
              className={`w-2.5 h-2.5 rounded-full ${
                health?.status === 'UP' && health?.database === 'CONNECTED'
                  ? 'bg-emerald-500 animate-pulse'
                  : 'bg-amber-500'
              }`}
            ></div>
            <span className="text-slate-600 font-medium">
              API Status: <strong className="text-primary">{health?.status || 'ONLINE'}</strong> • Database:{' '}
              <strong className="text-secondary">{health?.database || 'CONNECTED'}</strong>
            </span>
          </div>

          <button
            onClick={fetchHealth}
            disabled={healthLoading}
            className="text-slate-400 hover:text-primary transition p-1"
            title="Refresh system health"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${healthLoading ? 'animate-spin text-secondary' : ''}`} />
          </button>
        </div>
      </section>
    </div>
  );
};
