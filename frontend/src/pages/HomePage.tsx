import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Zap,
  RotateCcw,
  ArrowRight,
  RefreshCw,
  Compass,
  ChevronLeft,
  ChevronRight,
  Tag,
  MapPin,
  Radio,
  Lock,
  Sparkles
} from 'lucide-react';
import { FlightSearchWidget } from '../components/FlightSearchWidget';
import { RecommendationsSection } from '../components/RecommendationsSection';
import { OptimizedImage } from '../components/OptimizedImage';
import { LiveAirspaceFeed } from '../components/LiveAirspaceFeed';
import { healthService } from '../services/healthService';
import { HealthData } from '../types/api';
import { staggerContainerVariants, cardEntranceVariants } from '../lib/motion';

const HERO_DESTINATIONS = [
  {
    name: 'BALI',
    country: 'Indonesia',
    tagline: 'Discover tropical paradises, sacred temples, and lush emerald terraces.',
    from: 'DEL',
    to: 'DPS',
    image: 'https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=2560&q=90',
    price: '16,999',
  },
  {
    name: 'GOA',
    country: 'India',
    tagline: 'Dive into golden beaches, sunsets, and unforgettable coastal escapes.',
    from: 'DEL',
    to: 'GOI',
    image: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=2560&q=90',
    price: '3,999',
  },
  {
    name: 'DUBAI',
    country: 'United Arab Emirates',
    tagline: 'Experience futuristic skylines, luxury desert safaris, and world-class shopping.',
    from: 'BOM',
    to: 'DXB',
    image: 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=2560&q=90',
    price: '12,499',
  },
  {
    name: 'SINGAPORE',
    country: 'Singapore',
    tagline: 'Explore a vibrant global crossroads of lush green gardens and modern innovation.',
    from: 'BLR',
    to: 'SIN',
    image: 'https://images.unsplash.com/photo-1525625293386-3f8f99389edd?auto=format&fit=crop&w=2560&q=90',
    price: '14,299',
  },
];

const POPULAR_DESTINATIONS = [
  {
    city: 'Bali',
    country: 'Indonesia',
    from: 'DEL',
    to: 'DPS',
    price: '16,999',
    tag: 'Tropical Island',
    image: 'https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80',
  },
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

  const currentHero = HERO_DESTINATIONS[currentHeroIdx];

  const handleQuickRoute = (from: string, to: string) => {
    const today = new Date();
    const nextWeek = new Date(today);
    nextWeek.setDate(today.getDate() + 7);
    const dateStr = nextWeek.toISOString().split('T')[0];
    navigate(`/flights?origin=${from}&destination=${to}&departureDate=${dateStr}&cabinClass=ECONOMY&passengers=1`);
  };

  return (
    <div className="space-y-0 text-slate-100">
      {/* ======================================================== */}
      {/* 1. FULLSCREEN HERO SECTION                               */}
      {/* ======================================================== */}
      <section
        className="relative min-h-[92vh] sm:min-h-screen flex flex-col justify-between overflow-hidden bg-black"
        onMouseEnter={() => setIsPaused(true)}
        onMouseLeave={() => setIsPaused(false)}
      >
        {/* Dynamic Background Image with Smooth Cross-Fade */}
        <div className="absolute inset-0 z-0">
          <AnimatePresence mode="wait">
            <motion.div
              key={currentHero.image}
              initial={{ opacity: 0, scale: 1.04 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
              className="absolute inset-0"
            >
              <img
                src={currentHero.image}
                alt={currentHero.name}
                className="w-full h-full object-cover object-center"
              />
            </motion.div>
          </AnimatePresence>
          {/* Obsidian Gradient Overlays for High-Contrast Luxury Legibility */}
          <div className="absolute inset-0 bg-gradient-to-t from-[#0B0C10] via-[#0B0C10]/60 to-[#0B0C10]/40" />
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-transparent via-[#0B0C10]/30 to-[#0B0C10]/90" />
        </div>

        {/* Hero Top Content: Badges & Headings */}
        <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-16 sm:pt-24 pb-8 w-full text-center space-y-4">
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
            className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-[#0B0C10]/80 backdrop-blur-md border border-amber-400/30 text-amber-400 text-xs font-bold shadow-glow-gold"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>Next-Gen Smart Travel Platform</span>
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1, ease: [0.22, 1, 0.36, 1] }}
            className="text-4xl sm:text-6xl lg:text-7xl font-black text-white tracking-tight leading-[1.08] max-w-4xl mx-auto"
          >
            Seamless Journeys.{' '}
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-300 via-amber-400 to-amber-500 drop-shadow-sm">
              Smarter Fares.
            </span>
          </motion.h1>

          <motion.p
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2, ease: [0.22, 1, 0.36, 1] }}
            className="text-sm sm:text-base text-slate-300 max-w-2xl mx-auto leading-relaxed"
          >
            {currentHero.tagline}
          </motion.p>
        </div>

        {/* Flight Search Widget Container */}
        <div className="relative z-20 max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 w-full pb-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.25, ease: [0.22, 1, 0.36, 1] }}
          >
            <FlightSearchWidget />
          </motion.div>
        </div>

        {/* Hero Bottom Bar: Destination Switcher & Quick Book Pill */}
        <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-6 w-full flex flex-wrap items-center justify-between gap-4">
          {/* Quick Route Shortcut */}
          <div className="flex items-center gap-3">
            <motion.button
              whileTap={{ scale: 0.96 }}
              type="button"
              onClick={() => handleQuickRoute(currentHero.from, currentHero.to)}
              className="px-4 py-2 rounded-xl bg-[#0B0C10]/80 backdrop-blur-md hover:bg-[#14161F] text-xs font-bold text-white border border-white/15 flex items-center gap-2 transition shadow-xl group"
            >
              <MapPin className="w-3.5 h-3.5 text-amber-400" />
              <span>
                Fly to <strong className="text-amber-400">{currentHero.name}</strong> from ₹{currentHero.price}
              </span>
              <ArrowRight className="w-3.5 h-3.5 text-slate-400 group-hover:translate-x-1 transition-transform" />
            </motion.button>
          </div>

          {/* Carousel Indicators & Controls */}
          <div className="flex items-center gap-2">
            <motion.button
              whileTap={{ scale: 0.92 }}
              type="button"
              onClick={() => setCurrentHeroIdx((prev) => (prev - 1 + HERO_DESTINATIONS.length) % HERO_DESTINATIONS.length)}
              className="p-2.5 rounded-xl bg-[#0B0C10]/80 backdrop-blur-md hover:bg-[#1F222E] text-white border border-white/15 transition shadow-2xl"
              aria-label="Previous destination"
            >
              <ChevronLeft className="w-4 h-4 text-amber-400" />
            </motion.button>

            <div className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-[#0B0C10]/80 backdrop-blur-md border border-white/15">
              {HERO_DESTINATIONS.map((dest, idx) => (
                <button
                  key={dest.name}
                  type="button"
                  onClick={() => setCurrentHeroIdx(idx)}
                  className={`h-2 rounded-full transition-all duration-300 ${
                    currentHeroIdx === idx ? 'w-6 bg-amber-400 shadow-glow-gold' : 'w-2 bg-white/30 hover:bg-white/60'
                  }`}
                  aria-label={`Go to ${dest.name}`}
                />
              ))}
            </div>

            <motion.button
              whileTap={{ scale: 0.92 }}
              type="button"
              onClick={() => setCurrentHeroIdx((prev) => (prev + 1) % HERO_DESTINATIONS.length)}
              className="p-2.5 rounded-xl bg-[#0B0C10]/80 backdrop-blur-md hover:bg-[#1F222E] text-white border border-white/15 transition shadow-2xl"
              aria-label="Next destination"
            >
              <ChevronRight className="w-4 h-4 text-amber-400" />
            </motion.button>
          </div>
        </div>
      </section>

      {/* ======================================================== */}
      {/* 2. BODY CONTENT (BOUNDED MAX-W CONTAINER)                */}
      {/* ======================================================== */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-16 pb-20 pt-12">
        {/* Special Offers & Promotions */}
        <motion.section
          id="offers"
          initial={{ opacity: 0, y: 24 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-80px' }}
          transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
          className="space-y-6"
        >
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center gap-1.5 text-amber-400 text-xs font-bold uppercase tracking-wider">
                <Tag className="w-4 h-4" />
                <span>SmartTravel Perks & Promotions</span>
              </div>
              <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight mt-1">
                Exclusive Travel Offers
              </h2>
            </div>
            <span className="text-xs text-slate-400 hidden sm:inline font-semibold">Valid on domestic & international routes</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {OFFERS.map((offer, idx) => (
              <motion.div
                key={idx}
                whileHover={{ y: -4, transition: { duration: 0.2, ease: [0.22, 1, 0.36, 1] } }}
                className="rounded-2xl bg-[#14161F] border border-white/10 p-6 flex flex-col justify-between space-y-4 hover:border-amber-500/40 hover:shadow-card-hover transition-colors duration-300 group"
              >
                <div className="space-y-2">
                  <span className="inline-block px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider bg-amber-400/10 text-amber-400 border border-amber-400/20">
                    {offer.badge}
                  </span>
                  <h3 className="font-bold text-white text-base leading-snug group-hover:text-amber-400 transition">{offer.title}</h3>
                  <p className="text-xs text-slate-400 leading-relaxed">{offer.desc}</p>
                </div>

                <div className="pt-3 border-t border-white/5 flex items-center justify-between">
                  <div className="px-3 py-1 rounded-lg bg-[#1A1C24] border border-dashed border-amber-500/40 text-xs font-mono font-bold text-amber-400">
                    <span>{offer.code}</span>
                  </div>
                  <motion.button
                    whileTap={{ scale: 0.95 }}
                    type="button"
                    onClick={() => navigate('/flights')}
                    className="text-xs font-bold text-amber-400 hover:text-amber-300 flex items-center gap-1 transition"
                  >
                    <span>Book Flight</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </motion.button>
                </div>
              </motion.div>
            ))}
          </div>
        </motion.section>

        {/* Live Airspace Radar Preview */}
        <motion.section
          initial={{ opacity: 0, y: 24 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-80px' }}
          transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
          className="space-y-6"
        >
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <div className="flex items-center gap-1.5 text-amber-400 text-xs font-bold uppercase tracking-wider">
                <Radio className="w-4 h-4 animate-pulse" />
                <span>Real-Time Flight Telemetry</span>
              </div>
              <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight mt-1">
                Live Airspace Operations Stream
              </h2>
              <p className="text-xs sm:text-sm text-slate-400 mt-1">
                Active commercial flights tracked via real-time WebSocket feeds with live altitude, speed, and gate statuses.
              </p>
            </div>

            <motion.button
              whileTap={{ scale: 0.96 }}
              type="button"
              onClick={() => navigate('/live-tracker')}
              className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black text-xs font-bold flex items-center gap-1.5 transition shadow-glow-gold"
            >
              <span>Open Airspace Radar</span>
              <ArrowRight className="w-3.5 h-3.5 text-black" />
            </motion.button>
          </div>

          <LiveAirspaceFeed
            compact={false}
            limit={6}
            onSelectFlight={(flightNumber) => navigate(`/tracked-flights?flight=${flightNumber}`)}
          />
        </motion.section>

        {/* Popular Destinations */}
        <motion.section
          id="destinations"
          initial={{ opacity: 0, y: 24 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-80px' }}
          transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
          className="space-y-6"
        >
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <div className="flex items-center gap-1.5 text-amber-400 text-xs font-bold uppercase tracking-wider">
                <Compass className="w-4 h-4" />
                <span>Popular Routes</span>
              </div>
              <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight mt-1">
                Trending Flight Destinations
              </h2>
              <p className="text-xs sm:text-sm text-slate-400 mt-1">
                Instant booking with guaranteed seat map selection and zero hidden fees.
              </p>
            </div>

            <motion.button
              whileTap={{ scale: 0.96 }}
              type="button"
              onClick={() => navigate('/flights')}
              className="px-4 py-2 rounded-xl bg-[#14161F] hover:bg-[#1F222E] text-slate-200 border border-white/10 text-xs font-bold flex items-center gap-1.5 transition shadow-sm"
            >
              <span>View All Flights</span>
              <ArrowRight className="w-3.5 h-3.5 text-amber-400" />
            </motion.button>
          </div>

          <motion.div
            variants={staggerContainerVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true, margin: '-60px' }}
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6"
          >
            {POPULAR_DESTINATIONS.map((dest, i) => (
              <motion.div
                key={i}
                variants={cardEntranceVariants}
                whileHover={{ y: -5, transition: { duration: 0.2, ease: [0.22, 1, 0.36, 1] } }}
                onClick={() => handleQuickRoute(dest.from, dest.to)}
                className="rounded-2xl bg-[#14161F] border border-white/10 hover:border-amber-500/40 hover:shadow-card-hover overflow-hidden cursor-pointer group flex flex-col justify-between transition-colors duration-300"
              >
                <div className="relative h-44 overflow-hidden bg-[#1A1C24]">
                  <OptimizedImage
                    src={dest.image}
                    alt={dest.city}
                    aspectRatio="16/10"
                    className="w-full h-full"
                    imageClassName="dest-img group-hover:scale-105 transition-transform duration-500 object-cover"
                  />
                  <span className="absolute top-3 left-3 px-2.5 py-1 rounded-full bg-[#0B0C10]/80 backdrop-blur-md text-amber-400 text-[10px] font-bold border border-white/10">
                    {dest.tag}
                  </span>
                  <span className="absolute bottom-3 right-3 px-2.5 py-1 rounded-lg bg-gradient-to-r from-amber-400 to-amber-500 text-black text-xs font-black shadow-lg">
                    From ₹{dest.price}
                  </span>
                </div>

                <div className="p-4 space-y-2">
                  <div className="flex items-center justify-between">
                    <h3 className="font-black text-white text-base group-hover:text-amber-400 transition">
                      {dest.city}
                    </h3>
                    <span className="text-xs font-mono font-bold text-amber-400 bg-[#1A1C24] px-2 py-0.5 rounded border border-white/10">
                      {dest.to}
                    </span>
                  </div>
                  <div className="pt-2 border-t border-white/5 flex items-center justify-between text-xs text-slate-400">
                    <span>Direct from <strong className="text-slate-200">{dest.from}</strong></span>
                    <span className="text-amber-400 font-bold flex items-center gap-1 group-hover:translate-x-1 transition-transform">
                      Search <ArrowRight className="w-3.5 h-3.5" />
                    </span>
                  </div>
                </div>
              </motion.div>
            ))}
          </motion.div>
        </motion.section>

        {/* Personalized Recommendations */}
        <RecommendationsSection />

        {/* Trust & Enterprise Architecture Pillars */}
        <motion.section
          initial={{ opacity: 0, y: 24 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-80px' }}
          transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
          className="rounded-3xl bg-[#12131A] text-white border border-white/10 p-8 sm:p-12 shadow-2xl space-y-8"
        >
          <div className="text-center space-y-2 max-w-2xl mx-auto">
            <span className="text-xs font-bold uppercase tracking-widest text-amber-400">
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
            <motion.div
              whileHover={{ y: -3, transition: { duration: 0.2 } }}
              className="p-6 rounded-2xl bg-[#1A1C24] border border-white/10 space-y-2.5 transition-colors"
            >
              <div className="w-10 h-10 rounded-xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center shadow-glow-gold">
                <Zap className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-white text-sm">Atomic Seat & Room Locking</h3>
              <p className="text-xs text-slate-400 leading-relaxed">
                Interactive physical aircraft seat maps and hotel room reservations with 15-minute concurrency hold locks.
              </p>
            </motion.div>

            <motion.div
              whileHover={{ y: -3, transition: { duration: 0.2 } }}
              className="p-6 rounded-2xl bg-[#1A1C24] border border-white/10 space-y-2.5 transition-colors"
            >
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center shadow-glow-emerald">
                <RotateCcw className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-white text-sm">Disruption Auto-Refunds</h3>
              <p className="text-xs text-slate-400 leading-relaxed">
                Automated flight schedule monitoring with direct payment gateway refund reconciliation on cancellations.
              </p>
            </motion.div>

            <motion.div
              whileHover={{ y: -3, transition: { duration: 0.2 } }}
              className="p-6 rounded-2xl bg-[#1A1C24] border border-white/10 space-y-2.5 transition-colors"
            >
              <div className="w-10 h-10 rounded-xl bg-accent/10 text-accent border border-accent/20 flex items-center justify-center shadow-glow-coral">
                <Lock className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-white text-sm">Dynamic Price Freeze</h3>
              <p className="text-xs text-slate-400 leading-relaxed">
                Lock in current fares for 48 hours to protect your travel plans from surge pricing spikes.
              </p>
            </motion.div>
          </div>
        </motion.section>

        {/* Live Platform Health & Status Badge */}
        <section className="max-w-md mx-auto">
          <div className="p-3.5 rounded-2xl bg-[#14161F] border border-white/10 shadow-lg flex items-center justify-between text-xs">
            <div className="flex items-center gap-2.5">
              <div
                className={`w-2.5 h-2.5 rounded-full ${
                  health?.status === 'UP' && health?.database === 'CONNECTED'
                    ? 'bg-emerald-400 animate-pulse shadow-glow-emerald'
                    : 'bg-amber-400'
                }`}
              ></div>
              <span className="text-slate-300 font-medium">
                API Status: <strong className="text-white">{health?.status || 'ONLINE'}</strong> • Database:{' '}
                <strong className="text-amber-400">{health?.database || 'CONNECTED'}</strong>
              </span>
            </div>

            <button
              onClick={fetchHealth}
              disabled={healthLoading}
              className="text-slate-400 hover:text-white transition p-1"
              title="Refresh system health"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${healthLoading ? 'animate-spin text-amber-400' : ''}`} />
            </button>
          </div>
        </section>
      </div>
    </div>
  );
};
