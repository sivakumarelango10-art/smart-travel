import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Sparkles, Plane, Hotel as HotelIcon, ArrowRight, Star, Compass } from 'lucide-react';
import { RecommendationItem } from '../types/api';
import { recommendationService } from '../services/recommendationService';
import { AirlineLogo } from './AirlineLogo';
import { useAuth } from '../context/AuthContext';

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
    <section className="py-12 relative">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
          <div>
            <div className="flex items-center gap-2 text-blue-400 text-xs font-semibold uppercase tracking-wider mb-1">
              <Sparkles className="w-4 h-4" />
              <span>Smart Travel Recommendations</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-bold text-white">
              {isAuthenticated ? 'Handpicked For You' : 'Trending Destinations & Top Deals'}
            </h2>
            <p className="text-sm text-slate-400 mt-1">
              {isAuthenticated
                ? 'Personalized recommendations based on your preferences, searches, and activity.'
                : 'Popular domestic routes and luxury stays recommended by our community.'}
            </p>
          </div>

          {/* Filter Pills */}
          <div className="flex items-center p-1 bg-slate-900 border border-slate-800 rounded-lg">
            <button
              onClick={() => setFilter('ALL')}
              className={`px-3 py-1.5 text-xs font-medium rounded-md transition ${
                filter === 'ALL'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              All Picks
            </button>
            <button
              onClick={() => setFilter('FLIGHTS')}
              className={`px-3 py-1.5 text-xs font-medium rounded-md transition flex items-center gap-1.5 ${
                filter === 'FLIGHTS'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Plane className="w-3.5 h-3.5" />
              Flights
            </button>
            <button
              onClick={() => setFilter('HOTELS')}
              className={`px-3 py-1.5 text-xs font-medium rounded-md transition flex items-center gap-1.5 ${
                filter === 'HOTELS'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <HotelIcon className="w-3.5 h-3.5" />
              Hotels
            </button>
          </div>
        </div>

        {/* Recommendations Grid */}
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {Array.from({ length: 4 }).map((_, i) => (
              <div
                key={i}
                className="h-64 bg-slate-900 border border-slate-800 rounded-xl animate-pulse p-4 flex flex-col justify-between"
              >
                <div className="h-6 bg-slate-800 rounded w-1/2" />
                <div className="space-y-2">
                  <div className="h-4 bg-slate-800 rounded w-3/4" />
                  <div className="h-4 bg-slate-800 rounded w-1/2" />
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {recommendations.map((item) => {
              const isFlight = item.type === 'FLIGHT';
              const isHotel = item.type === 'HOTEL';
              const linkTo = isFlight
                ? `/book/${item.targetId}`
                : isHotel
                ? `/hotels/${item.targetId}`
                : `/flights?destination=${encodeURIComponent(item.title)}`;

              return (
                <div
                  key={item.id}
                  className="group relative bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl p-5 flex flex-col justify-between transition duration-200"
                >
                  <div>
                    {/* Top Row: Type & Reason Badge */}
                    <div className="flex items-center justify-between gap-2 mb-3">
                      <span className="inline-flex items-center gap-1 text-[11px] font-medium text-slate-400 bg-slate-800 px-2 py-0.5 rounded border border-slate-700">
                        {isFlight ? (
                          <Plane className="w-3 h-3 text-blue-400" />
                        ) : isHotel ? (
                          <HotelIcon className="w-3 h-3 text-blue-400" />
                        ) : (
                          <Compass className="w-3 h-3 text-blue-400" />
                        )}
                        {item.type}
                      </span>

                      {item.reasonLabel && (
                        <span className="text-[10px] font-medium text-slate-300 bg-slate-800 px-2 py-0.5 rounded border border-slate-700 truncate max-w-[140px]">
                          {item.reasonLabel}
                        </span>
                      )}
                    </div>

                    {/* Title & Subtitle */}
                    <h3 className="font-bold text-white text-base transition-colors line-clamp-1">
                      {item.title}
                    </h3>
                    <p className="text-xs text-slate-400 mt-1 line-clamp-1">
                      {item.subtitle || item.description}
                    </p>

                    {/* Rating / Meta */}
                    {isHotel && item.avgRating && (
                      <div className="mt-2 flex items-center gap-1.5 text-xs text-amber-400">
                        <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                        <span className="font-semibold">{item.avgRating.toFixed(1)}</span>
                        {item.starRating && (
                          <span className="text-[11px] text-slate-500">· {item.starRating}-Star</span>
                        )}
                      </div>
                    )}

                    {isFlight && item.airline && (
                      <div className="mt-2 text-xs text-slate-400 flex items-center gap-2">
                        <AirlineLogo airline={item.airline} size="xs" />
                        <span className="font-medium text-slate-300">{item.airline}</span>
                        {item.fromCode && item.toCode && (
                          <span className="text-[11px] font-mono text-slate-400 ml-auto bg-slate-800 px-1.5 py-0.5 rounded">
                            {item.fromCode} → {item.toCode}
                          </span>
                        )}
                      </div>
                    )}
                  </div>

                  {/* Price & Action */}
                  <div className="mt-5 pt-3 border-t border-slate-800 flex items-center justify-between gap-2">
                    <div>
                      <span className="text-[10px] text-slate-500 block">Starting from</span>
                      <span className="text-base font-bold text-white">
                        {item.priceLabel || (item.price ? `₹${item.price.toLocaleString()}` : 'Check Price')}
                      </span>
                    </div>

                    <Link
                      to={linkTo}
                      className="p-2 bg-slate-800 group-hover:bg-blue-600 group-hover:text-white text-slate-300 rounded-lg transition"
                      aria-label={`Explore ${item.title}`}
                    >
                      <ArrowRight className="w-4 h-4" />
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </section>
  );
};
