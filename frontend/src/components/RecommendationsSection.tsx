import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Sparkles,
  Plane,
  Hotel as HotelIcon,
  Compass,
  ArrowRight,
  Star,
  Info,
  ThumbsUp,
  ThumbsDown,
  X,
  Check,
  ShieldCheck,
  Tag,
  Zap,
  ChevronRight,
  TrendingUp,
} from 'lucide-react';
import {
  RecommendationItem,
  RecommendationFeedbackType,
  UserPreferenceProfile,
} from '../types/recommendation';
import { recommendationService } from '../services/recommendationService';
import { AirlineLogo } from './AirlineLogo';
import { AnimatedPrice } from './AnimatedPrice';
import { useAuth } from '../context/AuthContext';
import { staggerContainerVariants, cardEntranceVariants } from '../lib/motion';

interface RecommendationsSectionProps {
  context?: string;
  destination?: string;
  title?: string;
  subtitle?: string;
  limit?: number;
  showCategoryFilters?: boolean;
}

export const RecommendationsSection: React.FC<RecommendationsSectionProps> = ({
  context,
  destination,
  title,
  subtitle,
  limit = 8,
  showCategoryFilters = true,
}) => {
  const { isAuthenticated } = useAuth();
  const [recommendations, setRecommendations] = useState<RecommendationItem[]>([]);
  const [userProfile, setUserProfile] = useState<UserPreferenceProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'ALL' | 'FLIGHTS' | 'HOTELS' | 'DESTINATIONS'>('ALL');

  // "Why this recommendation?" Modal / Popover state
  const [activeExplanationItem, setActiveExplanationItem] = useState<RecommendationItem | null>(null);

  // Feedback recording state: itemId -> feedbackType
  const [feedbackState, setFeedbackState] = useState<Record<string, RecommendationFeedbackType>>({});
  const [dismissedItemIds, setDismissedItemIds] = useState<Set<string>>(new Set());

  // Fetch recommendations and user profile
  const fetchRecommendations = useCallback(async () => {
    setLoading(true);
    try {
      let data: RecommendationItem[] = [];
      if (filter === 'FLIGHTS') {
        data = await recommendationService.getFlightRecommendations(limit);
      } else if (filter === 'HOTELS') {
        data = await recommendationService.getHotelRecommendations(limit);
      } else if (filter === 'DESTINATIONS') {
        data = await recommendationService.getDestinationRecommendations(limit);
      } else {
        data = await recommendationService.getRecommendations(limit, context, destination);
      }
      setRecommendations(data);

      if (isAuthenticated) {
        const profile = await recommendationService.getUserPreferences();
        setUserProfile(profile);
      }
    } catch (err) {
      console.error('Failed to load recommendations', err);
    } finally {
      setLoading(false);
    }
  }, [filter, context, destination, limit, isAuthenticated]);

  useEffect(() => {
    fetchRecommendations();
  }, [fetchRecommendations]);

  const handleFeedback = async (
    item: RecommendationItem,
    type: RecommendationFeedbackType,
    e: React.MouseEvent
  ) => {
    e.stopPropagation();
    e.preventDefault();

    setFeedbackState((prev) => ({ ...prev, [item.id]: type }));

    if (type === 'NOT_RELEVANT' || type === 'DISMISS') {
      setTimeout(() => {
        setDismissedItemIds((prev) => new Set([...prev, item.id]));
      }, 350);
    }

    try {
      await recommendationService.submitFeedback({
        targetId: item.targetId || item.id,
        targetType: item.type,
        feedbackType: type,
        reasonCode: item.reasonCode,
        category: item.category,
      });
    } catch (err) {
      console.error('Error submitting recommendation feedback', err);
    }
  };

  const visibleRecommendations = recommendations.filter((r) => !dismissedItemIds.has(r.id));

  if (!loading && visibleRecommendations.length === 0) {
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
      <div className="max-w-7xl mx-auto px-4 sm:px-6">
        {/* Section Header */}
        <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
          <div>
            <div className="flex items-center gap-2 text-amber-400 text-xs font-bold uppercase tracking-wider mb-1.5">
              <Sparkles className="w-4 h-4 text-amber-400 fill-amber-400" />
              <span>Smart Travel Recommendation Engine</span>
              {userProfile && userProfile.inferredTravelStyle && (
                <span className="hidden sm:inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-amber-400/10 text-amber-400 border border-amber-400/20 text-[10px] normal-case font-bold">
                  <Zap className="w-3 h-3 text-amber-400" />
                  <span>Style: {userProfile.inferredTravelStyle}</span>
                </span>
              )}
            </div>
            <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
              {title ||
                (isAuthenticated
                  ? 'Handpicked For Your Next Trip'
                  : 'Trending Destinations & Top Travel Deals')}
            </h2>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              {subtitle ||
                (isAuthenticated
                  ? 'Tailored to your past bookings, searches, destination styles, and traveler feedback.'
                  : 'Popular domestic routes, luxury stays, and trending getaways curated for travelers.')}
            </p>
          </div>

          {/* Filter Pills */}
          {showCategoryFilters && (
            <div className="flex items-center p-1 bg-[#14161F] border border-white/10 rounded-xl shadow-lg">
              <motion.button
                whileTap={{ scale: 0.95 }}
                type="button"
                onClick={() => setFilter('ALL')}
                className={`px-3 py-1.5 text-xs font-bold rounded-lg transition cursor-pointer ${
                  filter === 'ALL'
                    ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                    : 'text-slate-300 hover:text-white'
                }`}
              >
                All Picks
              </motion.button>
              <motion.button
                whileTap={{ scale: 0.95 }}
                type="button"
                onClick={() => setFilter('FLIGHTS')}
                className={`px-3 py-1.5 text-xs font-bold rounded-lg transition flex items-center gap-1.5 cursor-pointer ${
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
                type="button"
                onClick={() => setFilter('HOTELS')}
                className={`px-3 py-1.5 text-xs font-bold rounded-lg transition flex items-center gap-1.5 cursor-pointer ${
                  filter === 'HOTELS'
                    ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                    : 'text-slate-300 hover:text-white'
                }`}
              >
                <HotelIcon className={`w-3.5 h-3.5 ${filter === 'HOTELS' ? 'text-black' : 'text-amber-400'}`} />
                Hotels
              </motion.button>
              <motion.button
                whileTap={{ scale: 0.95 }}
                type="button"
                onClick={() => setFilter('DESTINATIONS')}
                className={`px-3 py-1.5 text-xs font-bold rounded-lg transition flex items-center gap-1.5 cursor-pointer ${
                  filter === 'DESTINATIONS'
                    ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                    : 'text-slate-300 hover:text-white'
                }`}
              >
                <Compass className={`w-3.5 h-3.5 ${filter === 'DESTINATIONS' ? 'text-black' : 'text-amber-400'}`} />
                Destinations
              </motion.button>
            </div>
          )}
        </div>

        {/* Recommendations Grid */}
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {Array.from({ length: 4 }).map((_, i) => (
              <div
                key={i}
                className="h-72 bg-[#14161F] border border-white/10 rounded-3xl animate-pulse p-6 flex flex-col justify-between"
              >
                <div className="flex justify-between items-center">
                  <div className="h-5 bg-[#1A1C24] rounded w-1/3" />
                  <div className="h-5 bg-[#1A1C24] rounded w-1/4" />
                </div>
                <div className="space-y-3">
                  <div className="h-5 bg-[#1A1C24] rounded w-3/4" />
                  <div className="h-4 bg-[#1A1C24] rounded w-1/2" />
                </div>
                <div className="h-8 bg-[#1A1C24] rounded w-full" />
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
            <AnimatePresence>
              {visibleRecommendations.map((item) => {
                const isFlight = item.type === 'FLIGHT';
                const isHotel = item.type === 'HOTEL';
                const isDestination = item.type === 'DESTINATION';
                const currentFeedback = feedbackState[item.id] || item.userFeedback;

                const linkTo = isFlight
                  ? `/book/${item.targetId}`
                  : isHotel
                  ? `/hotels/${item.targetId}`
                  : `/flights?destination=${encodeURIComponent(item.title)}`;

                return (
                  <motion.div
                    key={item.id}
                    variants={cardEntranceVariants}
                    layout
                    exit={{ opacity: 0, scale: 0.85, transition: { duration: 0.25 } }}
                    whileHover={{ y: -4, transition: { duration: 0.2, ease: [0.22, 1, 0.36, 1] } }}
                    className="group relative bg-[#13151F] border border-white/10 hover:border-amber-400/40 hover:shadow-2xl rounded-3xl p-5 sm:p-6 flex flex-col justify-between transition-all duration-300"
                  >
                    <div>
                      {/* Top Badges & Dismiss Action */}
                      <div className="flex items-center justify-between gap-2 mb-3">
                        <span className="inline-flex items-center gap-1 text-[10px] font-extrabold uppercase tracking-wider text-amber-400 bg-amber-400/10 px-2.5 py-1 rounded-full border border-amber-400/20">
                          {isFlight ? (
                            <Plane className="w-3 h-3 text-amber-400" />
                          ) : isHotel ? (
                            <HotelIcon className="w-3 h-3 text-amber-400" />
                          ) : (
                            <Compass className="w-3 h-3 text-amber-400" />
                          )}
                          <span>{item.category || item.type}</span>
                        </span>

                        <div className="flex items-center gap-1">
                          {item.badgeText && (
                            <span className="text-[10px] font-bold text-slate-300 bg-[#1C1F2E] px-2 py-0.5 rounded-lg border border-white/5 truncate max-w-[120px]">
                              {item.badgeText}
                            </span>
                          )}

                          <button
                            type="button"
                            onClick={(e) => handleFeedback(item, 'DISMISS', e)}
                            title="Dismiss recommendation"
                            className="p-1 text-slate-500 hover:text-rose-400 rounded-md hover:bg-white/5 transition"
                            aria-label="Dismiss recommendation"
                          >
                            <X className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>

                      {/* Title & Subtitle */}
                      <h3 className="font-black text-white text-base transition-colors line-clamp-1 group-hover:text-amber-400">
                        {item.title}
                      </h3>
                      <p className="text-xs text-slate-400 mt-1 line-clamp-2 leading-relaxed">
                        {item.subtitle || item.description}
                      </p>

                      {/* Flight / Hotel Metadata Row */}
                      {isHotel && item.avgRating && (
                        <div className="mt-2.5 flex items-center gap-1.5 text-xs text-amber-400 font-bold">
                          <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                          <span>{item.avgRating.toFixed(1)}</span>
                          {item.starRating && (
                            <span className="text-[11px] text-slate-400 font-normal">
                              · {item.starRating}-Star Luxury
                            </span>
                          )}
                        </div>
                      )}

                      {isFlight && item.airline && (
                        <div className="mt-2.5 text-xs text-slate-400 flex items-center gap-2">
                          <AirlineLogo airline={item.airline} size="xs" />
                          <span className="font-semibold text-slate-300 truncate">{item.airline}</span>
                          {item.fromCode && item.toCode && (
                            <span className="text-[10px] font-mono text-amber-400 ml-auto bg-[#1C1F2E] px-1.5 py-0.5 rounded font-bold border border-white/10">
                              {item.fromCode} → {item.toCode}
                            </span>
                          )}
                        </div>
                      )}

                      {/* Tags */}
                      {item.tags && item.tags.length > 0 && (
                        <div className="flex flex-wrap gap-1 mt-3">
                          {item.tags.slice(0, 2).map((t, idx) => (
                            <span
                              key={idx}
                              className="text-[10px] text-slate-400 bg-white/5 px-2 py-0.5 rounded-md border border-white/5"
                            >
                              {t}
                            </span>
                          ))}
                        </div>
                      )}

                      {/* "Why this recommendation?" Trigger Button */}
                      <div className="mt-4 pt-3 border-t border-white/5">
                        <button
                          type="button"
                          onClick={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            setActiveExplanationItem(item);
                          }}
                          className="w-full py-1.5 px-2.5 rounded-xl bg-white/5 hover:bg-amber-400/10 border border-white/5 hover:border-amber-400/20 text-slate-300 hover:text-amber-400 text-[11px] font-bold flex items-center justify-between transition cursor-pointer group/btn"
                        >
                          <span className="flex items-center gap-1.5 truncate">
                            <Info className="w-3.5 h-3.5 text-amber-400 flex-shrink-0" />
                            <span className="truncate">Why this recommendation?</span>
                          </span>
                          <ChevronRight className="w-3 h-3 text-slate-500 group-hover/btn:translate-x-0.5 transition-transform" />
                        </button>
                      </div>
                    </div>

                    {/* Bottom Row: Price, Feedback Buttons & CTA */}
                    <div className="mt-5 pt-3 border-t border-white/5 space-y-3">
                      <div className="flex items-center justify-between gap-2">
                        <div>
                          <span className="text-[10px] text-slate-500 block font-semibold uppercase tracking-wider">
                            Starting from
                          </span>
                          <div className="text-base font-black text-amber-400">
                            {item.price ? (
                              <AnimatedPrice value={item.price} />
                            ) : (
                              item.priceLabel || 'Check Rates'
                            )}
                          </div>
                        </div>

                        <Link
                          to={linkTo}
                          className="px-3.5 py-2 bg-[#1C1F2E] group-hover:bg-amber-400 group-hover:text-black text-slate-200 active:scale-95 rounded-xl transition font-extrabold text-xs flex items-center gap-1 shadow-md"
                          aria-label={`Explore ${item.title}`}
                        >
                          <span>Explore</span>
                          <ArrowRight className="w-3.5 h-3.5" />
                        </Link>
                      </div>

                      {/* Feedback Loop Action Buttons */}
                      <div className="flex items-center justify-between gap-2 pt-1 text-[11px]">
                        <span className="text-slate-500 text-[10px]">Was this helpful?</span>
                        <div className="flex items-center gap-1.5">
                          <button
                            type="button"
                            onClick={(e) => handleFeedback(item, 'HELPFUL', e)}
                            className={`p-1.5 rounded-lg transition cursor-pointer flex items-center gap-1 text-[10px] font-bold ${
                              currentFeedback === 'HELPFUL'
                                ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 shadow-glow-emerald'
                                : 'bg-[#1C1F2E] hover:bg-white/10 text-slate-400 hover:text-white border border-white/5'
                            }`}
                            title="Mark as helpful"
                          >
                            <ThumbsUp className="w-3 h-3" />
                            {currentFeedback === 'HELPFUL' && <span>Helpful</span>}
                          </button>

                          <button
                            type="button"
                            onClick={(e) => handleFeedback(item, 'NOT_RELEVANT', e)}
                            className={`p-1.5 rounded-lg transition cursor-pointer flex items-center gap-1 text-[10px] font-bold ${
                              currentFeedback === 'NOT_RELEVANT'
                                ? 'bg-rose-500/20 text-rose-400 border border-rose-500/40'
                                : 'bg-[#1C1F2E] hover:bg-white/10 text-slate-400 hover:text-white border border-white/5'
                            }`}
                            title="Not relevant to me"
                          >
                            <ThumbsDown className="w-3 h-3" />
                            {currentFeedback === 'NOT_RELEVANT' && <span>Removed</span>}
                          </button>
                        </div>
                      </div>
                    </div>
                  </motion.div>
                );
              })}
            </AnimatePresence>
          </motion.div>
        )}
      </div>

      {/* "WHY THIS RECOMMENDATION?" MODAL */}
      {activeExplanationItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="relative w-full max-w-lg bg-[#141622] border border-white/15 rounded-3xl p-6 sm:p-8 shadow-2xl space-y-6">
            <button
              onClick={() => setActiveExplanationItem(null)}
              className="absolute top-5 right-5 p-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white transition"
              aria-label="Close explanation modal"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-2xl bg-amber-400/15 border border-amber-400/30 flex items-center justify-center text-amber-400 shadow-glow-gold">
                <Sparkles className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-lg font-black text-white">Why We Picked This For You</h3>
                <p className="text-xs text-slate-400">
                  Transparent reasoning for <strong>{activeExplanationItem.title}</strong>
                </p>
              </div>
            </div>

            {/* Explanation Body */}
            <div className="p-4 rounded-2xl bg-[#1C1F2E] border border-white/5 space-y-3">
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-1 rounded-lg bg-amber-400 text-black text-xs font-black uppercase tracking-wider">
                  {activeExplanationItem.explanation?.category || activeExplanationItem.category || 'Curated'}
                </span>
                {activeExplanationItem.explanation?.confidence && (
                  <span className="text-xs font-mono font-bold text-amber-400">
                    {Math.round(activeExplanationItem.explanation.confidence * 100)}% Match Confidence
                  </span>
                )}
              </div>

              <h4 className="text-sm font-bold text-white leading-snug">
                "{activeExplanationItem.explanation?.headline || activeExplanationItem.reasonLabel || 'Recommended based on your preferences'}"
              </h4>

              <p className="text-xs text-slate-300 leading-relaxed whitespace-pre-line">
                {activeExplanationItem.explanation?.details ||
                  activeExplanationItem.description ||
                  'This travel item aligns with your verified bookings, destination affinities, and traveler feedback.'}
              </p>
            </div>

            {/* Tags & Highlights */}
            {activeExplanationItem.tags && activeExplanationItem.tags.length > 0 && (
              <div className="space-y-1.5">
                <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400 block">
                  Key Attributes & Match Highlights:
                </span>
                <div className="flex flex-wrap gap-2">
                  {activeExplanationItem.tags.map((t, idx) => (
                    <span
                      key={idx}
                      className="px-3 py-1 rounded-xl bg-white/5 border border-white/10 text-xs font-semibold text-slate-200"
                    >
                      ✓ {t}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Feedback within Modal */}
            <div className="p-4 rounded-2xl bg-[#10121A] border border-white/5 flex items-center justify-between gap-4">
              <span className="text-xs text-slate-400">Help improve your suggestions:</span>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={(e) => {
                    handleFeedback(activeExplanationItem, 'HELPFUL', e);
                    setActiveExplanationItem(null);
                  }}
                  className="px-3.5 py-1.5 rounded-xl bg-emerald-500/15 hover:bg-emerald-500/25 border border-emerald-500/30 text-emerald-400 text-xs font-bold flex items-center gap-1.5 transition cursor-pointer"
                >
                  <ThumbsUp className="w-3.5 h-3.5" />
                  <span>Helpful</span>
                </button>
                <button
                  type="button"
                  onClick={(e) => {
                    handleFeedback(activeExplanationItem, 'NOT_RELEVANT', e);
                    setActiveExplanationItem(null);
                  }}
                  className="px-3.5 py-1.5 rounded-xl bg-rose-500/15 hover:bg-rose-500/25 border border-rose-500/30 text-rose-400 text-xs font-bold flex items-center gap-1.5 transition cursor-pointer"
                >
                  <ThumbsDown className="w-3.5 h-3.5" />
                  <span>Not Relevant</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </motion.section>
  );
};
