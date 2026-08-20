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
  Headphones
} from 'lucide-react';
import { FlightSearchWidget } from '../components/FlightSearchWidget';
import { RecommendationsSection } from '../components/RecommendationsSection';
import { healthService } from '../services/healthService';
import { HealthData } from '../types/api';

// Curated high-quality, high-reliability travel imagery
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
  {
    name: 'MANALI',
    country: 'Himachal Pradesh',
    tagline: 'Escape into snow-capped peaks, pine forests, and breathtaking mountain valleys.',
    from: 'DEL',
    to: 'KUU',
    image: 'https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=1920&q=80',
    price: '4,499',
  },
  {
    name: 'PARIS',
    country: 'France',
    tagline: 'Immerse in timeless romance, iconic art, and exquisite culinary heritage.',
    from: 'DEL',
    to: 'CDG',
    image: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1920&q=80',
    price: '28,999',
  },
];

const POPULAR_DESTINATIONS = [
  {
    city: 'Goa',
    country: 'India',
    from: 'DEL',
    to: 'GOI',
    price: '3,999',
    tag: 'Beach & Nightlife',
    image: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=800&q=80',
  },
  {
    city: 'Dubai',
    country: 'UAE',
    from: 'BOM',
    to: 'DXB',
    price: '12,499',
    tag: 'Luxury City',
    image: 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=800&q=80',
  },
  {
    city: 'Singapore',
    country: 'Singapore',
    from: 'BLR',
    to: 'SIN',
    price: '14,299',
    tag: 'Modern Hub',
    image: 'https://images.unsplash.com/photo-1525625293386-3f8f99389edd?auto=format&fit=crop&w=800&q=80',
  },
  {
    city: 'Bali',
    country: 'Indonesia',
    from: 'DEL',
    to: 'DPS',
    price: '16,999',
    tag: 'Tropical Escape',
    image: 'https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80',
  },
  {
    city: 'Manali',
    country: 'India',
    from: 'DEL',
    to: 'KUU',
    price: '4,499',
    tag: 'Himalayan Highs',
    image: 'https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=800&q=80',
  },
  {
    city: 'Kerala',
    country: 'India',
    from: 'BOM',
    to: 'COK',
    price: '4,899',
    tag: 'Backwaters & Palms',
    image: 'https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=800&q=80',
  },
  {
    city: 'Paris',
    country: 'France',
    from: 'DEL',
    to: 'CDG',
    price: '28,999',
    tag: 'Art & Heritage',
    image: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80',
  },
  {
    city: 'London',
    country: 'United Kingdom',
    from: 'BOM',
    to: 'LHR',
    price: '31,500',
    tag: 'Historic Landmark',
    image: 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?auto=format&fit=crop&w=800&q=80',
  },
];

const INSPIRATION_CARDS = [
  {
    title: 'Beach Escapes',
    subtitle: 'Sun, sand & endless turquoise waves',
    image: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80',
    destinations: 'Goa • Maldives • Bali • Phuket',
    from: 'DEL',
    to: 'GOI',
  },
  {
    title: 'Mountain Adventures',
    subtitle: 'Crisp alpine breezes & snow summits',
    image: 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=600&q=80',
    destinations: 'Manali • Leh • Shimla • Srinagar',
    from: 'DEL',
    to: 'BOM',
  },
  {
    title: 'City Breaks',
    subtitle: 'Iconic skylines, cuisine & culture',
    image: 'https://images.unsplash.com/photo-1477959858617-67f30bc75b82?auto=format&fit=crop&w=600&q=80',
    destinations: 'Dubai • Singapore • Tokyo • London',
    from: 'BOM',
    to: 'DEL',
  },
  {
    title: 'Weekend Getaways',
    subtitle: 'Quick recharges within short flights',
    image: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=600&q=80',
    destinations: 'Jaipur • Udaipur • Kochi • Bengaluru',
    from: 'BLR',
    to: 'DEL',
  },
];

const OFFERS = [
  {
    badge: 'DOMESTIC FLIGHTS',
    title: 'Flat 15% OFF on Domestic Flights',
    desc: 'Book your flight across major domestic airports and enjoy instant savings.',
    code: 'SMARTFLY',
    accent: 'from-sky-500 to-indigo-600',
  },
  {
    badge: 'EARLY BIRD DEALS',
    title: 'Up to ₹2,500 OFF with HDFC & ICICI',
    desc: 'Pay seamlessly with leading credit cards and unlock instant cashbacks.',
    code: 'BANKPASS',
    accent: 'from-emerald-500 to-teal-600',
  },
  {
    badge: 'ZERO CANCELLATION',
    title: 'Disruption Auto-Refund Guarantee',
    desc: 'Automatic gateway refunds initiated instantly if your flight gets cancelled.',
    code: 'AUTOSAFE',
    accent: 'from-amber-500 to-orange-600',
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
    <div className="space-y-20 -mt-8 pb-12">
      {/* ======================================================== */}
      {/* 1. CINEMATIC HERO SECTION WITH DESTINATION CAROUSEL     */}
      {/* ======================================================== */}
      <section
        className="relative min-h-[620px] sm:min-h-[680px] lg:min-h-[720px] rounded-3xl overflow-hidden shadow-2xl flex flex-col justify-between p-4 sm:p-8 lg:p-12 border border-white/10"
        onMouseEnter={() => setIsPaused(true)}
        onMouseLeave={() => setIsPaused(false)}
      >
        {/* Background Images Crossfade */}
        {HERO_DESTINATIONS.map((dest, idx) => (
          <div
            key={dest.name}
            className={`absolute inset-0 transition-opacity duration-1000 ease-in-out ${
              idx === currentHeroIdx ? 'opacity-100 scale-100' : 'opacity-0 scale-105 pointer-events-none'
            }`}
            style={{ transition: 'opacity 1s ease-in-out, transform 8s ease-out' }}
          >
            <img
              src={dest.image}
              alt={dest.name}
              className="w-full h-full object-cover object-center"
              loading={idx === 0 ? 'eager' : 'lazy'}
            />
            {/* Dark Cinematic Gradients */}
            <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/60 to-slate-950/40"></div>
            <div className="absolute inset-0 bg-gradient-to-r from-slate-950/80 via-slate-950/40 to-transparent"></div>
          </div>
        ))}

        {/* Hero Top Badges & Destination Tag */}
        <div className="relative z-10 flex flex-wrap items-center justify-between gap-4">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-slate-900/80 border border-white/15 text-sky-300 text-xs font-bold tracking-wide backdrop-blur-xl shadow-lg">
            <Sparkles className="w-4 h-4 text-sky-400" />
            <span>High-Precision Real-Time Flight Engine</span>
          </div>

          {/* Current Destination Pill */}
          <div className="hidden sm:flex items-center gap-2 px-4 py-2 rounded-full bg-slate-900/80 border border-white/15 backdrop-blur-xl text-xs font-bold text-white shadow-lg">
            <MapPin className="w-3.5 h-3.5 text-accent-500" />
            <span>Featured: {currentHero.name}, {currentHero.country}</span>
          </div>
        </div>

        {/* Hero Main Content */}
        <div className="relative z-10 my-auto py-8 text-center space-y-6 max-w-4xl mx-auto">
          <div className="space-y-3">
            <h1 className="text-4xl sm:text-6xl lg:text-7xl font-black tracking-tight text-white leading-[1.1] drop-shadow-lg">
              Explore the World with{' '}
              <span className="bg-gradient-to-r from-sky-400 via-indigo-300 to-accent-400 bg-clip-text text-transparent">
                SmartTravel
              </span>
            </h1>
            <p className="text-slate-200 text-sm sm:text-base lg:text-lg max-w-2xl mx-auto font-medium leading-relaxed drop-shadow-md">
              {currentHero.tagline}
            </p>
          </div>

          {/* Embedded Search Widget */}
          <div className="pt-2 text-left">
            <FlightSearchWidget />
          </div>
        </div>

        {/* Hero Bottom Carousel Controls */}
        <div className="relative z-10 flex items-center justify-between pt-4 border-t border-white/10">
          <div className="flex items-center gap-2">
            {HERO_DESTINATIONS.map((d, i) => (
              <button
                key={d.name}
                type="button"
                onClick={() => setCurrentHeroIdx(i)}
                className={`h-2 rounded-full transition-all duration-300 ${
                  i === currentHeroIdx ? 'w-8 bg-sky-400 shadow-md shadow-sky-400/50' : 'w-2 bg-white/30 hover:bg-white/60'
                }`}
                aria-label={`Go to slide ${d.name}`}
              />
            ))}
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() =>
                setCurrentHeroIdx(
                  (prev) => (prev - 1 + HERO_DESTINATIONS.length) % HERO_DESTINATIONS.length
                )
              }
              className="p-2 rounded-xl bg-slate-900/80 hover:bg-slate-800 border border-white/15 text-white transition backdrop-blur-md shadow-md"
              aria-label="Previous destination"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              type="button"
              onClick={() => setCurrentHeroIdx((prev) => (prev + 1) % HERO_DESTINATIONS.length)}
              className="p-2 rounded-xl bg-slate-900/80 hover:bg-slate-800 border border-white/15 text-white transition backdrop-blur-md shadow-md"
              aria-label="Next destination"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </section>

      {/* ======================================================== */}
      {/* 2. SPECIAL OFFERS & TRAVEL DEALS SECTION                */}
      {/* ======================================================== */}
      <section id="offers" className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <div className="flex items-center gap-2 text-amber-400 text-xs font-bold uppercase tracking-wider">
              <Tag className="w-4 h-4" />
              <span>Exclusive Promotions</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight mt-1">
              Offers for Your Next Adventure
            </h2>
          </div>
          <span className="text-xs text-slate-400 hidden sm:inline font-medium">Valid on all commercial routes</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {OFFERS.map((offer, idx) => (
            <div
              key={idx}
              className="rounded-3xl bg-slate-900/80 border border-slate-800 p-6 flex flex-col justify-between space-y-4 hover:border-slate-700 transition duration-300 relative overflow-hidden group shadow-xl"
            >
              <div className="absolute top-0 right-0 w-32 h-32 bg-sky-500/5 rounded-full blur-2xl group-hover:bg-sky-500/10 transition"></div>

              <div className="space-y-2">
                <span className="inline-block px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider bg-slate-800 text-slate-300 border border-slate-700">
                  {offer.badge}
                </span>
                <h3 className="font-bold text-white text-lg leading-snug">{offer.title}</h3>
                <p className="text-xs text-slate-400 leading-relaxed">{offer.desc}</p>
              </div>

              <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between">
                <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-950 border border-dashed border-slate-700 text-xs font-mono font-bold text-sky-400">
                  <span>{offer.code}</span>
                </div>
                <button
                  type="button"
                  onClick={() => navigate('/flights')}
                  className="text-xs font-bold text-white hover:text-sky-400 flex items-center gap-1 transition group-hover:translate-x-1 duration-200"
                >
                  <span>Book Now</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ======================================================== */}
      {/* 3. POPULAR & TRENDING DESTINATIONS                      */}
      {/* ======================================================== */}
      <section id="destinations" className="space-y-6">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 text-sky-400 text-xs font-bold uppercase tracking-wider">
              <Compass className="w-4 h-4" />
              <span>Trending Destinations</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight mt-1">
              Places Travelers Love to Explore
            </h2>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Top curated flights departing daily with instant atomic seat reservations
            </p>
          </div>

          <button
            type="button"
            onClick={() => navigate('/flights')}
            className="px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 hover:text-white border border-slate-800 text-xs font-bold flex items-center gap-1.5 transition"
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
              className="destination-card rounded-3xl bg-slate-900 border border-slate-800/80 overflow-hidden cursor-pointer group flex flex-col justify-between shadow-xl"
            >
              {/* Image Container */}
              <div className="relative h-48 overflow-hidden">
                <img
                  src={dest.image}
                  alt={dest.city}
                  className="dest-img w-full h-full object-cover"
                  loading="lazy"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/20 to-transparent"></div>
                <span className="absolute top-3 left-3 px-2.5 py-1 rounded-full bg-slate-950/80 backdrop-blur-md text-sky-300 text-[10px] font-bold border border-white/10">
                  {dest.tag}
                </span>
                <span className="absolute bottom-3 right-3 px-2.5 py-1 rounded-xl bg-emerald-500/90 text-white text-xs font-black shadow-lg">
                  ₹{dest.price}
                </span>
              </div>

              {/* Card Body */}
              <div className="p-5 space-y-3">
                <div>
                  <div className="flex items-center justify-between">
                    <h3 className="font-extrabold text-white text-lg group-hover:text-sky-400 transition">
                      {dest.city}
                    </h3>
                    <span className="text-xs font-mono font-bold text-slate-400 bg-slate-800 px-2 py-0.5 rounded border border-slate-700">
                      {dest.to}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 mt-0.5">{dest.country}</p>
                </div>

                <div className="pt-3 border-t border-slate-800 flex items-center justify-between text-xs">
                  <span className="text-slate-400">Direct flight from <strong className="text-slate-200">{dest.from}</strong></span>
                  <span className="text-sky-400 font-bold flex items-center gap-1 group-hover:translate-x-1 transition-transform">
                    Search
                    <ArrowRight className="w-3.5 h-3.5" />
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ======================================================== */}
      {/* 4. TRAVEL INSPIRATION EDITORIAL SECTION                  */}
      {/* ======================================================== */}
      <section className="space-y-6">
        <div>
          <div className="flex items-center gap-2 text-indigo-400 text-xs font-bold uppercase tracking-wider">
            <Sparkles className="w-4 h-4" />
            <span>Curated Travel Moods</span>
          </div>
          <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight mt-1">
            Get Inspired for Your Next Journey
          </h2>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {INSPIRATION_CARDS.map((card, idx) => (
            <div
              key={idx}
              onClick={() => handleQuickRoute(card.from, card.to)}
              className="relative h-80 rounded-3xl overflow-hidden border border-slate-800 shadow-2xl cursor-pointer group flex flex-col justify-end p-6"
            >
              <img
                src={card.image}
                alt={card.title}
                className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
                loading="lazy"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/60 to-transparent"></div>

              <div className="relative z-10 space-y-2">
                <span className="text-[10px] font-bold uppercase tracking-wider text-sky-400">
                  {card.destinations}
                </span>
                <h3 className="font-extrabold text-white text-xl group-hover:text-sky-300 transition">
                  {card.title}
                </h3>
                <p className="text-xs text-slate-300 leading-relaxed line-clamp-2">
                  {card.subtitle}
                </p>

                <div className="pt-2 flex items-center gap-1.5 text-xs font-bold text-white group-hover:text-sky-400 transition">
                  <span>Explore Flights</span>
                  <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ======================================================== */}
      {/* 4. PERSONALIZED RECOMMENDATIONS & TRENDING PICKS        */}
      {/* ======================================================== */}
      <RecommendationsSection />

      {/* ======================================================== */}
      {/* 5. WHY SMARTTRAVEL TRUST & SECURITY SECTION             */}
      {/* ======================================================== */}
      <section className="rounded-3xl bg-gradient-to-br from-slate-900 via-slate-900/90 to-slate-950 border border-slate-800 p-8 sm:p-12 shadow-2xl space-y-8">
        <div className="text-center space-y-2 max-w-2xl mx-auto">
          <span className="text-xs font-bold uppercase tracking-widest text-sky-400">
            Enterprise Architecture & Reliability
          </span>
          <h2 className="text-2xl sm:text-4xl font-black text-white tracking-tight">
            Why Fly with SmartTravel?
          </h2>
          <p className="text-xs sm:text-sm text-slate-400">
            Experience next-generation flight booking built for instant precision, maximum security, and peace of mind.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 pt-4">
          <div className="p-6 rounded-2xl bg-slate-950/80 border border-slate-800/80 space-y-3">
            <div className="w-12 h-12 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center">
              <Zap className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-white text-base">Atomic Seat Locking</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Interactive physical aircraft seat maps with 15-minute concurrency hold locks. Zero double-booking risk.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-slate-950/80 border border-slate-800/80 space-y-3">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center">
              <RotateCcw className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-white text-base">Disruption Auto-Refunds</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Real-time flight schedule monitoring with automatic payment gateway refund triggers on cancellations.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-slate-950/80 border border-slate-800/80 space-y-3">
            <div className="w-12 h-12 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 flex items-center justify-center">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-white text-base">Instant E-Ticket & Pass</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Authoritative PDF ticket issuance, online airport check-in, and instant mobile boarding passes.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-slate-950/80 border border-slate-800/80 space-y-3">
            <div className="w-12 h-12 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20 flex items-center justify-center">
              <CreditCard className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-white text-base">Secure Razorpay Gateway</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              End-to-end encrypted checkout with UPI, Credit/Debit cards, NetBanking, and instant webhook reconciliation.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-slate-950/80 border border-slate-800/80 space-y-3">
            <div className="w-12 h-12 rounded-xl bg-teal-500/10 text-teal-400 border border-teal-500/20 flex items-center justify-center">
              <Award className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-white text-base">Authoritative Pricing</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Transparent fares with taxes and airport fees included upfront. No surprise hidden charges at checkout.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-slate-950/80 border border-slate-800/80 space-y-3">
            <div className="w-12 h-12 rounded-xl bg-rose-500/10 text-rose-400 border border-rose-500/20 flex items-center justify-center">
              <Headphones className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-white text-base">24/7 Flight Support</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Real-time notification alerts on gate changes, delays, disruptions, and instant booking confirmations.
            </p>
          </div>
        </div>
      </section>

      {/* ======================================================== */}
      {/* 6. LIVE PLATFORM HEALTH & STATUS BADGE                  */}
      {/* ======================================================== */}
      <section className="max-w-md mx-auto pt-4">
        <div className="p-4 rounded-2xl bg-slate-900/70 border border-slate-800 shadow-xl flex items-center justify-between text-xs backdrop-blur-md">
          <div className="flex items-center gap-2.5">
            <div
              className={`w-2.5 h-2.5 rounded-full ${
                health?.status === 'UP' && health?.database === 'CONNECTED'
                  ? 'bg-emerald-400 animate-pulse'
                  : 'bg-amber-400'
              }`}
            ></div>
            <span className="text-slate-300 font-medium">
              API Status: <strong className="text-white">{health?.status || 'ONLINE'}</strong> • Database:{' '}
              <strong className="text-sky-400">{health?.database || 'CONNECTED'}</strong>
            </span>
          </div>

          <button
            onClick={fetchHealth}
            disabled={healthLoading}
            className="text-slate-400 hover:text-white transition p-1"
            title="Re-verify health"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${healthLoading ? 'animate-spin text-sky-400' : ''}`} />
          </button>
        </div>
      </section>
    </div>
  );
};

