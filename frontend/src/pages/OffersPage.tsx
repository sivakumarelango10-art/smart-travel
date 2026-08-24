import React, { useState, useEffect } from 'react';
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
  MapPin
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

export const OffersPage: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'ALL' | 'FLIGHT' | 'HOTEL' | 'COUPONS'>('ALL');
  const [liveDeals, setLiveDeals] = useState<RecommendationItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [copiedCode, setCopiedCode] = useState<string | null>(null);

  useEffect(() => {
    document.title = 'Deals & Exclusive Offers | SmartTravel';
    window.scrollTo({ top: 0, behavior: 'smooth' });

    const fetchDeals = async () => {
      try {
        setLoading(true);
        const data = await recommendationService.getRecommendations(12);
        if (data && data.length > 0) {
          setLiveDeals(data);
        }
      } catch {
        // Fallback to empty or popular recommendations
      } finally {
        setLoading(false);
      }
    };
    fetchDeals();
  }, []);

  const handleCopyCode = (code: string) => {
    navigator.clipboard.writeText(code);
    setCopiedCode(code);
    setTimeout(() => setCopiedCode(null), 2500);
  };

  const filteredDeals = liveDeals.filter((item) => {
    if (activeTab === 'ALL') return true;
    if (activeTab === 'FLIGHT') return item.type === 'FLIGHT';
    if (activeTab === 'HOTEL') return item.type === 'HOTEL';
    return true;
  });

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
                      className="p-1.5 rounded-lg bg-[#181A22] hover:bg-[#1F222E] text-slate-300 hover:text-white transition border border-white/10"
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
      <div className="flex items-center gap-2 p-1.5 rounded-2xl bg-[#14161F] border border-white/10 w-fit">
        <button
          type="button"
          onClick={() => setActiveTab('ALL')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
            activeTab === 'ALL'
              ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Sparkles className="w-3.5 h-3.5" />
          <span>All Deals</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('FLIGHT')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
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
          className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
            activeTab === 'HOTEL'
              ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Building2 className="w-3.5 h-3.5" />
          <span>Hotel Deals</span>
        </button>
      </div>

      {/* 4. LIVE DISCOUNTED INVENTORY CARDS */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg sm:text-xl font-black text-white flex items-center gap-2">
              <Zap className="w-5 h-5 text-amber-400" />
              Live Curated Travel Deals
            </h2>
            <p className="text-xs text-slate-400">
              Verified inventory matching lowest available base fares and hotel rates
            </p>
          </div>
        </div>

        {loading ? (
          <div className="py-16 flex flex-col items-center justify-center gap-3">
            <div className="w-10 h-10 border-4 border-amber-400/30 border-t-amber-400 rounded-full animate-spin" />
            <p className="text-xs text-slate-400 font-bold">Scanning live pricing engines for latest deals...</p>
          </div>
        ) : filteredDeals.length === 0 ? (
          <div className="p-10 rounded-3xl bg-[#14161F] border border-white/10 text-center space-y-3">
            <Tag className="w-10 h-10 text-amber-400 mx-auto" />
            <h3 className="text-base font-bold text-white">No Specific Deals Found for this Filter</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              Explore our full flight search catalog or luxury hotel directory for seasonal promotions.
            </p>
            <div className="pt-2 flex justify-center gap-3">
              <Link
                to="/flights"
                className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black text-xs font-black shadow-glow-gold"
              >
                Search Flights
              </Link>
              <Link
                to="/hotels"
                className="px-5 py-2.5 rounded-xl bg-[#181A22] text-slate-200 text-xs font-bold border border-white/10"
              >
                Browse Hotels
              </Link>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredDeals.map((item) => (
              <div
                key={item.id}
                className="rounded-3xl bg-[#14161F] border border-white/10 shadow-xl overflow-hidden hover:border-amber-400/30 transition-all flex flex-col justify-between group"
              >
                {/* Media Image / Header */}
                <div className="relative h-44 w-full overflow-hidden bg-[#181A22]">
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
                    <span className="px-2.5 py-1 rounded-full bg-black/70 backdrop-blur-md border border-white/10 text-white text-[10px] font-black uppercase tracking-wider flex items-center gap-1">
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
                  <div className="absolute bottom-3 right-3 px-3 py-1.5 rounded-xl bg-black/80 backdrop-blur-md border border-white/10 text-right">
                    <span className="text-[10px] text-slate-400 block font-medium">Starting from</span>
                    <span className="text-base font-black text-amber-400">
                      ₹{item.price?.toLocaleString('en-IN')}
                    </span>
                  </div>
                </div>

                {/* Content */}
                <div className="p-5 space-y-3 flex-1 flex flex-col justify-between">
                  <div className="space-y-1.5">
                    <h3 className="font-black text-white text-base leading-snug group-hover:text-amber-400 transition">
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
                  </div>

                  <div className="pt-4 border-t border-white/5 flex items-center justify-between">
                    <div className="text-[11px] text-emerald-400 font-bold flex items-center gap-1">
                      <ShieldCheck className="w-3.5 h-3.5" />
                      <span>Instant Booking</span>
                    </div>

                    <Link
                      to={item.type === 'FLIGHT' ? `/book/${item.targetId}` : `/hotels/${item.targetId}`}
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
        )}
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
