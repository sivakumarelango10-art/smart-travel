import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Sparkles, Plane, Hotel as HotelIcon, ArrowRight, Star, Compass } from 'lucide-react';
import { RecommendationItem } from '../types/api';
import { recommendationService } from '../services/recommendationService';
import { AirlineLogo } from './AirlineLogo';
import { AnimatedPrice } from './AnimatedPrice';
import { useAuth } from '../context/AuthContext';
import { staggerContainerVariants, cardEntranceVariants } from '../lib/motion';

export const RecommendationsSection: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [recommendations, setRecommendations] = useState<RecommendationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'ALL' | 'FLIGHTS' | 'HOTELS'>('ALL');

  useEffect(() => {
    setLoading(true);
    if (filter === 'FLIGHTS') {
      recommendationService
        .getFlightRecommendations(6)
        .then(setRecommendations)
        .catch(console.error)
        .finally(() => setLoading(false));
    } else if (filter === 'HOTELS') {
      recommendationService
        .getHotelRecommendations(6)
        .then(setRecommendations)
        .catch(console.error)
        .finally(() => setLoading(false));
    } else {
      recommendationService
        .getRecommendations(8)
        .then(setRecommendations)
        .catch(console.error)
        .finally(() => setLoading(false));
    }
  }, [filter]);

  if (!loading && recommendations.length === 0) {
    return null;
  }

  return (
    <motion.section
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-80px' }}
      transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
      className="py-12 relative bg-transparent border-t border-white/10"
    >
      <div className="max-w-7xl mx-auto">
        {/* Section Header */}
        <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
          <div>
            <div className="flex items-center gap-1.5 text-amber-400 text-xs font-bold uppercase tracking-wider mb-1">
              <Sparkles className="w-4 h-4 text-amber-400" />
              <span>Smart Travel Recommendations</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
              {isAuthenticated ? 'Handpicked For Your Next Trip' : 'Trending Destinations & Top Travel Deals'}
            </h2>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              {isAuthenticated
                ? 'Personalized recommendations based on your preferences, searches, and activity.'
                : 'Popular domestic routes and luxury stays recommended by our traveler community.'}
            </p>
          </div>

          {/* Filter Pills */}
          <div className="flex items-center p-1 bg-[#14161F] border border-white/10 rounded-xl shadow-lg">
            <motion.button
              whileTap={{ scale: 0.95 }}
              onClick={() => setFilter('ALL')}
              className={`px-3 py-1.5 text-xs font-bold rounded-lg transition ${
                filter === 'ALL'
                  ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                  : 'text-slate-300 hover:text-white'
              }`}
            >
              All Picks
            </motion.button>
            <motion.button
              whileTap={{ scale: 0.95 }}
              onClick={() => setFilter('FLIGHTS')}
              className={`px-3 py-1.5 text-xs font-bold rounded-lg transition flex items-center gap-1.5 ${
                filter === 'FLIGHTS'
                  ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                  : 'text-slate-300 hover:text-white'
              }`}
            >
              <Plane className={`w-3.5 h-3.5 ${filter === 'FLIGHTS' ? 'text-black' : 'text-amber-400'}`} />
              Flights
            </motion.button>
            <motion.button
              whileTap={{ scale: 0.95 }}
              onClick={() => setFilter('HOTELS')}
              className={`px-3 py-1.5 text-xs font-bold rounded-lg transition flex items-center gap-1.5 ${
                filter === 'HOTELS'
                  ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                  : 'text-slate-300 hover:text-white'
              }`}
            >
              <HotelIcon className={`w-3.5 h-3.5 ${filter === 'HOTELS' ? 'text-black' : 'text-amber-400'}`} />
              Hotels
            </motion.button>
          </div>
        </div>

        {/* Recommendations Grid */}
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {Array.from({ length: 4 }).map((_, i) => (
              <div
                key={i}
                className="h-60 bg-[#14161F] border border-white/10 rounded-2xl animate-pulse p-5 flex flex-col justify-between"
              >
                <div className="h-5 bg-[#1A1C24] rounded w-1/2" />
                <div className="space-y-2">
                  <div className="h-4 bg-[#1A1C24] rounded w-3/4" />
                  <div className="h-4 bg-[#1A1C24] rounded w-1/2" />
                </div>
              </div>
            ))}
          </div>
        ) : (
          <motion.div
            variants={staggerContainerVariants}
            initial="hidden"
            animate="visible"
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6"
          >
            {recommendations.map((item) => {
              const isFlight = item.type === 'FLIGHT';
              const isHotel = item.type === 'HOTEL';
              const linkTo = isFlight
                ? `/book/${item.targetId}`
                : isHotel
                ? `/hotels/${item.targetId}`
                : `/flights?destination=${encodeURIComponent(item.title)}`;

              return (
                <motion.div
                  key={item.id}
                  variants={cardEntranceVariants}
                  whileHover={{ y: -4, transition: { duration: 0.2, ease: [0.22, 1, 0.36, 1] } }}
                  className="group relative bg-[#14161F] border border-white/10 hover:border-amber-500/40 hover:shadow-card-hover rounded-2xl p-5 flex flex-col justify-between transition-colors duration-300"
                >
                  <div>
                    {/* Top Row: Type & Reason Badge */}
                    <div className="flex items-center justify-between gap-2 mb-3">
                      <span className="inline-flex items-center gap-1 text-[11px] font-bold text-amber-400 bg-amber-400/10 px-2.5 py-0.5 rounded-full border border-amber-400/20">
                        {isFlight ? (
                          <Plane className="w-3 h-3 text-amber-400" />
                        ) : isHotel ? (
                          <HotelIcon className="w-3 h-3 text-amber-400" />
                        ) : (
                          <Compass className="w-3 h-3 text-amber-400" />
                        )}
                        {item.type}
                      </span>

                      {item.reasonLabel && (
                        <span className="text-[10px] font-bold text-slate-400 bg-[#1A1C24] px-2 py-0.5 rounded-md border border-white/10 truncate max-w-[140px]">
                          {item.reasonLabel}
                        </span>
                      )}
                    </div>

                    {/* Title & Subtitle */}
                    <h3 className="font-black text-white text-base transition-colors line-clamp-1 group-hover:text-amber-400">
                      {item.title}
                    </h3>
                    <p className="text-xs text-slate-400 mt-1 line-clamp-1">
                      {item.subtitle || item.description}
                    </p>

                    {/* Rating / Meta */}
                    {isHotel && item.avgRating && (
                      <div className="mt-2.5 flex items-center gap-1.5 text-xs text-amber-400 font-bold">
                        <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                        <span>{item.avgRating.toFixed(1)}</span>
                        {item.starRating && (
                          <span className="text-[11px] text-slate-400 font-normal">· {item.starRating}-Star Luxury</span>
                        )}
                      </div>
                    )}

                    {isFlight && item.airline && (
                      <div className="mt-2.5 text-xs text-slate-400 flex items-center gap-2">
                        <AirlineLogo airline={item.airline} size="xs" />
                        <span className="font-semibold text-slate-300 truncate">{item.airline}</span>
                        {item.fromCode && item.toCode && (
                          <span className="text-[10px] font-mono text-amber-400 ml-auto bg-[#1A1C24] px-1.5 py-0.5 rounded font-bold border border-white/10">
                            {item.fromCode} → {item.toCode}
                          </span>
                        )}
                      </div>
                    )}
                  </div>

                  {/* Price & Action */}
                  <div className="mt-5 pt-3 border-t border-white/5 flex items-center justify-between gap-2">
                    <div>
                      <span className="text-[10px] text-slate-400 block font-medium">Starting from</span>
                      <div className="text-base font-black text-amber-400">
                        {item.price ? (
                          <AnimatedPrice value={item.price} />
                        ) : (
                          item.priceLabel || 'Check Price'
                        )}
                      </div>
                    </div>

                    <Link
                      to={linkTo}
                      className="p-2 bg-[#1A1C24] group-hover:bg-amber-400 group-hover:text-black text-slate-300 active:scale-90 rounded-xl transition shadow-md"
                      aria-label={`Explore ${item.title}`}
                    >
                      <ArrowRight className="w-4 h-4" />
                    </Link>
                  </div>
                </motion.div>
              );
            })}
          </motion.div>
        )}
      </div>
    </motion.section>
  );
};
