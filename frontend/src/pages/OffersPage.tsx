import React, { useState, useEffect, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Tag,
  Plane,
  Building2,
  Sparkles,
  Percent,
  Clock,
  Copy,
  Check,
  ArrowRight,
  ShieldCheck,
  TrendingDown,
  Gift,
  Zap,
  Compass,
  Star,
  MapPin,
  RefreshCw
} from 'lucide-react';
import { recommendationService } from '../services/recommendationService';
import { RecommendationItem } from '../types/api';
import { AirlineLogo } from '../components/AirlineLogo';

interface PromotionalCoupon {
  id: string;
  code: string;
  title: string;
  description: string;
  discount: string;
  category: 'FLIGHT' | 'HOTEL' | 'ALL';
  validUntil: string;
  minSpend: number;
  bgGradient: string;
  icon: any;
}

const FEATURED_COUPONS: PromotionalCoupon[] = [
  {
    id: 'c-festive25',
    code: 'SMARTFLY25',
    title: 'Domestic Flight Extravaganza',
    description: 'Flat ₹1,500 off on all domestic non-stop routes with verified e-ticket delivery.',
    discount: '₹1,500 OFF',
    category: 'FLIGHT',
    validUntil: 'Limited Time',
    minSpend: 4999,
    bgGradient: 'from-amber-500/20 via-[#181A22] to-[#14161F]',
    icon: Plane,
  },
  {
    id: 'c-luxuryhotel',
    code: 'STAYROYAL',
    title: 'Luxury Suites & Resorts',
    description: 'Up to 30% savings on 5-Star verified heritage and beachfront luxury properties.',
    discount: '30% OFF',
    category: 'HOTEL',
    validUntil: 'Active Today',
    minSpend: 7500,
    bgGradient: 'from-amber-400/20 via-[#181A22] to-[#14161F]',
    icon: Building2,
  },
  {
    id: 'c-farelock',
    code: 'FREEZELOCK',
    title: 'Zero-Risk Price Freeze Perk',
    description: 'Lock your flight fare for 30 minutes with 100% price-hike protection.',
    discount: 'FREEZE PASS',
    category: 'ALL',
    validUntil: 'Ongoing',
    minSpend: 0,
    bgGradient: 'from-emerald-500/20 via-[#181A22] to-[#14161F]',
    icon: ShieldCheck,
  },
];

// Curated instant-load verified travel deals ensuring 0ms blocking on initial page load
const CURATED_DEFAULT_DEALS: RecommendationItem[] = [
  {
    id: 'deal-del-bom',
    type: 'FLIGHT',
    targetId: 'del-bom-flash',
    title: 'Delhi (DEL) → Mumbai (BOM)',
    subtitle: 'Non-stop Morning Express • IndiGo Airlines',
    description: 'Lowest fare of the season with complimentary web check-in and dynamic price guarantee.',
    imageUrl: 'https://images.unsplash.com/photo-1570168007204-dfb528c6958f?auto=format&fit=crop&w=800&q=80',
    price: 3850,
    score: 0.98,
    reasonLabel: '25% OFF Flash Deal',
    fromCity: 'Delhi',
    toCity: 'Mumbai',
    fromCode: 'DEL',
    toCode: 'BOM',
    airline: 'IndiGo',
  },
  {
    id: 'deal-taj-mumbai',
    type: 'HOTEL',
    targetId: 'taj-mahal-palace-mumbai',
    title: 'The Taj Mahal Palace, Mumbai',
    subtitle: 'Apollo Bunder, Colaba • 5-Star Iconic Heritage Stay',
    description: 'Complimentary high tea, harbour-view suites, and zero-cancellation luxury getaway package.',
    imageUrl: 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80',
    price: 12500,
    score: 0.96,
    reasonLabel: 'Luxury Heritage Deal',
    city: 'Mumbai',
    starRating: 5,
    avgRating: 4.9,
  },
  {
    id: 'deal-blr-del',
    type: 'FLIGHT',
    targetId: 'blr-del-express',
    title: 'Bangalore (BLR) → Delhi (DEL)',
    subtitle: 'Non-stop Prime Departure • Air India',
    description: 'Full-service cabin with complimentary hot meal, 25kg check-in baggage, and fast-track boarding.',
    imageUrl: 'https://images.unsplash.com/photo-1587474260584-136574528ed5?auto=format&fit=crop&w=800&q=80',
    price: 4200,
    score: 0.95,
    reasonLabel: 'Corporate Special',
    fromCity: 'Bangalore',
    toCity: 'Delhi',
    fromCode: 'BLR',
    toCode: 'DEL',
    airline: 'Air India',
  },
  {
    id: 'deal-w-goa',
    type: 'HOTEL',
    targetId: 'w-goa-resort',
    title: 'W Goa Beachfront Resort',
    subtitle: 'Vagator Beach • 5-Star Tropical Luxury',
    description: 'Private cabana access, ocean-view villas, and complimentary sunset cocktail experience.',
    imageUrl: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80',
    price: 8900,
    score: 0.94,
    reasonLabel: 'Beachfront Promo',
    city: 'Goa',
    starRating: 5,
    avgRating: 4.8,
  },
  {
    id: 'deal-bom-goi',
    type: 'FLIGHT',
    targetId: 'bom-goi-getaway',
    title: 'Mumbai (BOM) → Goa (GOI)',
    subtitle: 'Sunset Direct Flight • Vistara',
    description: 'Premium economy seating option with zero price surge guarantee under Price Freeze.',
    imageUrl: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=800&q=80',
    price: 2990,
    score: 0.93,
    reasonLabel: 'Weekend Escape',
    fromCity: 'Mumbai',
    toCity: 'Goa',
    fromCode: 'BOM',
    toCode: 'GOI',
    airline: 'Vistara',
  },
  {
    id: 'deal-oberoi-delhi',
    type: 'HOTEL',
    targetId: 'oberoi-new-delhi',
    title: 'The Oberoi, New Delhi',
    subtitle: 'Dr. Zakir Hussain Marg • 5-Star Luxury Retreat',
    description: 'Clean air technology suites, golf course vistas, and Michelin-inspired culinary dining.',
    imageUrl: 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80',
    price: 11200,
    score: 0.92,
    reasonLabel: 'Weekend Retreat',
    city: 'Delhi',
    starRating: 5,
    avgRating: 4.9,
  },
];

export const OffersPage: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'ALL' | 'FLIGHT' | 'HOTEL'>('ALL');
  const [liveDeals, setLiveDeals] = useState<RecommendationItem[]>(CURATED_DEFAULT_DEALS);
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);
  const [copiedCode, setCopiedCode] = useState<string | null>(null);

  useEffect(() => {
    document.title = 'Deals & Exclusive Offers | SmartTravel';
    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Non-blocking background sync with fast timeout fallback
    let isMounted = true;
    const fetchFreshDeals = async () => {
      try {
        setIsRefreshing(true);
        // Timeout safeguard: don't let cold starts hang the UI
        const timeoutPromise = new Promise<RecommendationItem[]>((_, reject) =>
          setTimeout(() => reject(new Error('Timeout')), 6000)
        );

        const dataPromise = recommendationService.getRecommendations(12);
        const data = await Promise.race([dataPromise, timeoutPromise]);

        if (isMounted && data && Array.isArray(data) && data.length > 0) {
          setLiveDeals(data);
        }
      } catch {
        // Retain curated default deals silently
      } finally {
        if (isMounted) {
          setIsRefreshing(false);
        }
      }
    };

    fetchFreshDeals();
    return () => {
      isMounted = false;
    };
  }, []);

  const handleCopyCode = (code: string) => {
    navigator.clipboard.writeText(code);
    setCopiedCode(code);
    setTimeout(() => setCopiedCode(null), 2500);
  };

  const filteredDeals = useMemo(() => {
    return liveDeals.filter((item) => {
      if (activeTab === 'ALL') return true;
      if (activeTab === 'FLIGHT') return item.type === 'FLIGHT';
      if (activeTab === 'HOTEL') return item.type === 'HOTEL';
      return true;
    });
  }, [liveDeals, activeTab]);

  return (
    <div className="max-w-7xl mx-auto py-8 sm:py-12 px-4 sm:px-6 lg:px-8 space-y-10 animate-fade-in text-slate-300">
      {/* 1. HERO BANNER */}
      <div className="p-6 sm:p-10 rounded-3xl bg-gradient-to-r from-[#14161F] via-[#181A22] to-[#14161F] border border-white/10 shadow-2xl relative overflow-hidden">
        <div className="absolute top-0 right-0 w-96 h-96 bg-amber-400/5 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 space-y-4 max-w-2xl">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 text-xs font-bold shadow-glow-gold">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Exclusive Flash Deals & Fare Locks</span>
          </div>

          <h1 className="text-3xl sm:text-5xl font-black text-white tracking-tight leading-tight">
            Travel Smarter with <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-300 via-amber-400 to-amber-500">Unbeatable Deals</span>
          </h1>

          <p className="text-xs sm:text-sm text-slate-400 leading-relaxed">
            Discover handpicked flight discounts, luxury hotel stay packages, and promo vouchers with instant digital ticket issuance and automated refunds.
          </p>
        </div>

        {/* Quick Route Quick-Links */}
        <div className="mt-8 flex flex-wrap items-center gap-3 pt-4 border-t border-white/5 text-xs">
          <span className="text-slate-500 font-bold uppercase tracking-wider text-[10px]">Popular Flash Routes:</span>
          <Link
            to="/flights?origin=DEL&destination=BOM"
            className="px-3 py-1.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 text-white font-medium transition flex items-center gap-1.5"
          >
            <Plane className="w-3 h-3 text-amber-400" />
            <span>Delhi &rarr; Mumbai (from ₹3,850)</span>
          </Link>
          <Link
            to="/flights?origin=BLR&destination=DEL"
            className="px-3 py-1.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 text-white font-medium transition flex items-center gap-1.5"
          >
            <Plane className="w-3 h-3 text-amber-400" />
            <span>Bangalore &rarr; Delhi (from ₹4,200)</span>
          </Link>
          <Link
            to="/hotels"
            className="px-3 py-1.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] border border-white/10 text-amber-400 font-medium transition flex items-center gap-1.5"
          >
            <Building2 className="w-3 h-3 text-amber-400" />
            <span>Luxury Goa & Mumbai Resorts</span>
          </Link>
        </div>
      </div>

      {/* 2. PROMOTIONAL COUPON VOUCHERS GRID */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <Gift className="w-5 h-5 text-amber-400" />
            <h2 className="text-lg sm:text-xl font-black text-white">Active Promo Codes & Vouchers</h2>
          </div>
          <span className="text-xs text-slate-400 font-mono">Apply at checkout</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {FEATURED_COUPONS.map((coupon) => {
            const IconComponent = coupon.icon;
            const isCopied = copiedCode === coupon.code;
            return (
              <div
                key={coupon.id}
                className={`p-6 rounded-3xl bg-gradient-to-br ${coupon.bgGradient} border border-white/10 shadow-xl space-y-4 hover:border-amber-400/40 transition-all group`}
              >
                <div className="flex items-start justify-between">
                  <div className="w-10 h-10 rounded-2xl bg-[#14161F] border border-white/10 flex items-center justify-center text-amber-400 shadow-glow-gold">
                    <IconComponent className="w-5 h-5" />
                  </div>
                  <span className="px-3 py-1 rounded-full bg-amber-400/15 border border-amber-400/30 text-amber-400 text-xs font-black">
                    {coupon.discount}
                  </span>
                </div>

                <div className="space-y-1">
                  <h3 className="font-black text-white text-base group-hover:text-amber-400 transition">
                    {coupon.title}
                  </h3>
                  <p className="text-xs text-slate-400 leading-relaxed">
                    {coupon.description}
                  </p>
                </div>

                <div className="pt-2 flex items-center justify-between border-t border-white/5">
                  <div className="flex items-center gap-2">
                    <span className="text-[11px] font-mono font-bold text-slate-300 bg-[#12131A] px-2.5 py-1 rounded-lg border border-dashed border-white/20">
                      {coupon.code}
                    </span>
                    <button
                      type="button"
                      onClick={() => handleCopyCode(coupon.code)}
                      className="p-1.5 rounded-lg bg-[#181A22] hover:bg-[#1F222E] text-slate-300 hover:text-white transition border border-white/10 cursor-pointer"
                      title="Copy promo code"
                    >
                      {isCopied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    </button>
                  </div>

                  <span className="text-[10px] text-slate-500 font-medium">{coupon.validUntil}</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* 3. CATEGORY SWITCHER TABS */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 p-1.5 rounded-2xl bg-[#14161F] border border-white/10 w-fit">
          <button
            type="button"
            onClick={() => setActiveTab('ALL')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 cursor-pointer ${
              activeTab === 'ALL'
                ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>All Deals ({liveDeals.length})</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('FLIGHT')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 cursor-pointer ${
              activeTab === 'FLIGHT'
                ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Plane className="w-3.5 h-3.5" />
            <span>Flight Offers</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('HOTEL')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 cursor-pointer ${
              activeTab === 'HOTEL'
                ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Building2 className="w-3.5 h-3.5" />
            <span>Hotel Deals</span>
          </button>
        </div>

        {isRefreshing && (
          <span className="hidden sm:flex items-center gap-1.5 text-[11px] text-amber-400 font-mono">
            <RefreshCw className="w-3 h-3 animate-spin text-amber-400" />
            <span>Syncing live fares...</span>
          </span>
        )}
      </div>

      {/* 4. CURATED & LIVE INVENTORY CARDS */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg sm:text-xl font-black text-white flex items-center gap-2">
              <Zap className="w-5 h-5 text-amber-400" />
              Live Curated Travel Deals
            </h2>
            <p className="text-xs text-slate-400">
              Verified inventory matching lowest available base fares and luxury hotel rates
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredDeals.map((item) => (
            <div
              key={item.id}
              className="rounded-3xl bg-[#14161F] border border-white/10 shadow-xl overflow-hidden hover:border-amber-400/40 hover:shadow-card-hover transition-all flex flex-col justify-between group"
            >
              {/* Media Image / Header */}
              <div className="relative h-48 w-full overflow-hidden bg-[#181A22]">
                {item.imageUrl ? (
                  <img
                    src={item.imageUrl}
                    alt={item.title}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    loading="lazy"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-[#181A22] text-amber-400">
                    {item.type === 'FLIGHT' ? <Plane className="w-12 h-12" /> : <Building2 className="w-12 h-12" />}
                  </div>
                )}

                {/* Badges */}
                <div className="absolute top-3 left-3 flex items-center gap-2">
                  <span className="px-2.5 py-1 rounded-full bg-black/75 backdrop-blur-md border border-white/10 text-white text-[10px] font-black uppercase tracking-wider flex items-center gap-1">
                    {item.type === 'FLIGHT' ? <Plane className="w-3 h-3 text-amber-400" /> : <Building2 className="w-3 h-3 text-amber-400" />}
                    {item.type}
                  </span>
                  {item.reasonLabel && (
                    <span className="px-2.5 py-1 rounded-full bg-gradient-to-r from-amber-400 to-amber-500 text-black text-[10px] font-black shadow-glow-gold">
                      {item.reasonLabel}
                    </span>
                  )}
                </div>

                {/* Price Tag Overlay */}
                <div className="absolute bottom-3 right-3 px-3 py-1.5 rounded-xl bg-black/85 backdrop-blur-md border border-white/10 text-right shadow-lg">
                  <span className="text-[10px] text-slate-400 block font-medium">Starting from</span>
                  <span className="text-base font-black text-amber-400 font-mono">
                    ₹{item.price?.toLocaleString('en-IN')}
                  </span>
                </div>
              </div>

              {/* Content */}
              <div className="p-5 space-y-3 flex-1 flex flex-col justify-between">
                <div className="space-y-1.5">
                  <h3 className="font-black text-white text-base leading-snug group-hover:text-amber-400 transition line-clamp-1">
                    {item.title}
                  </h3>
                  <p className="text-xs text-slate-400 leading-relaxed line-clamp-2">
                    {item.subtitle || item.description}
                  </p>

                  {item.airline && (
                    <div className="flex items-center gap-2 pt-1">
                      <AirlineLogo airline={item.airline} size="sm" />
                      <span className="text-xs text-slate-300 font-semibold">{item.airline}</span>
                    </div>
                  )}

                  {item.starRating && (
                    <div className="flex items-center gap-1.5 text-xs text-amber-400 font-bold pt-1">
                      <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                      <span>{item.avgRating ? item.avgRating.toFixed(1) : '5.0'}</span>
                      <span className="text-[11px] text-slate-400 font-normal">· {item.starRating}-Star Verified</span>
                    </div>
                  )}
                </div>

                <div className="pt-4 border-t border-white/5 flex items-center justify-between">
                  <div className="text-[11px] text-emerald-400 font-bold flex items-center gap-1">
                    <ShieldCheck className="w-3.5 h-3.5" />
                    <span>Instant Booking</span>
                  </div>

                  <Link
                    to={
                      item.type === 'FLIGHT'
                        ? item.fromCode && item.toCode
                          ? `/flights?origin=${item.fromCode}&destination=${item.toCode}`
                          : `/flights`
                        : `/hotels`
                    }
                    className="px-4 py-2 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black text-xs font-black flex items-center gap-1.5 shadow-glow-gold transition"
                  >
                    <span>Book Deal</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 5. VALUE PROPOSITIONS BAR */}
      <div className="p-6 sm:p-8 rounded-3xl bg-[#14161F] border border-white/10 grid grid-cols-1 sm:grid-cols-3 gap-6">
        <div className="flex items-start gap-3.5">
          <div className="w-10 h-10 rounded-2xl bg-amber-400/10 border border-amber-400/20 text-amber-400 flex items-center justify-center shrink-0 shadow-glow-gold">
            <TrendingDown className="w-5 h-5" />
          </div>
          <div>
            <h4 className="font-bold text-white text-sm">Dynamic Price Alerts</h4>
            <p className="text-xs text-slate-400 mt-0.5">Real-time demand monitoring locks the lowest available fares.</p>
          </div>
        </div>

        <div className="flex items-start gap-3.5">
          <div className="w-10 h-10 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 shadow-glow-emerald">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <h4 className="font-bold text-white text-sm">Automated Refunds</h4>
            <p className="text-xs text-slate-400 mt-0.5">Transparent time-based cancellation policy with zero hidden deduction fees.</p>
          </div>
        </div>

        <div className="flex items-start gap-3.5">
          <div className="w-10 h-10 rounded-2xl bg-amber-400/10 border border-amber-400/20 text-amber-400 flex items-center justify-center shrink-0 shadow-glow-gold">
            <Compass className="w-5 h-5" />
          </div>
          <div>
            <h4 className="font-bold text-white text-sm">Live Airspace Radar</h4>
            <p className="text-xs text-slate-400 mt-0.5">Track your booked flight in real-time with digital QR passes.</p>
          </div>
        </div>
      </div>
    </div>
  );
};
