import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Hotel as HotelIcon, Search, Star, MapPin, ArrowRight } from 'lucide-react';
import { Hotel } from '../types/api';
import { hotelService } from '../services/hotelService';
import { StarRating } from '../components/StarRating';
import { HotelCardSkeleton } from '../components/HotelCardSkeleton';
import { ImageWithFallback } from '../components/ImageWithFallback';
import { recommendationService } from '../services/recommendationService';
import { resolveHotelPhotos } from '../utils/hotelImageRegistry';

export const HotelSearchPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialCity = searchParams.get('city') || '';
  const initialStars = searchParams.get('minStars') ? Number(searchParams.get('minStars')) : undefined;

  const [city, setCity] = useState(initialCity);
  const [minStars, setMinStars] = useState<number | undefined>(initialStars);
  const maxPrice = undefined;
  const [hotels, setHotels] = useState<Hotel[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [slowMessage, setSlowMessage] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    let isCurrent = true;
    setLoading(true);
    setSlowMessage(null);

    const stage1Timer = setTimeout(() => {
      if (isCurrent) setSlowMessage('Connecting to live hotel services…');
    }, 3500);

    const stage2Timer = setTimeout(() => {
      if (isCurrent) setSlowMessage('Live hotel services are taking a little longer than usual.');
    }, 8000);

    hotelService.searchHotels({
      city: city.trim() || undefined,
      minStars,
      maxPrice,
      page,
      size: 9,
    }).then((res) => {
      if (isCurrent) {
        clearTimeout(stage1Timer);
        clearTimeout(stage2Timer);
        setSlowMessage(null);
        setHotels(res.content);
        setTotalCount(res.totalElements);
        setTotalPages(res.totalPages);

        if (city.trim()) {
          recommendationService.trackActivity({
            activityType: 'SEARCH_HOTEL',
            targetId: city.trim(),
            targetType: 'HOTEL',
            metadata: { city: city.trim() },
          });
        }
      }
    }).catch((err) => {
      if (isCurrent) {
        clearTimeout(stage1Timer);
        clearTimeout(stage2Timer);
        setSlowMessage(null);
        console.error('Failed to search hotels', err);
      }
    }).finally(() => {
      if (isCurrent) {
        clearTimeout(stage1Timer);
        clearTimeout(stage2Timer);
        setLoading(false);
        setSlowMessage(null);
      }
    });

    return () => {
      isCurrent = false;
      clearTimeout(stage1Timer);
      clearTimeout(stage2Timer);
    };
  }, [city, minStars, maxPrice, page]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    const params: Record<string, string> = {};
    if (city.trim()) params.city = city.trim();
    if (minStars) params.minStars = String(minStars);
    setSearchParams(params);
  };

  const cities = ['Delhi', 'Mumbai', 'Bangalore', 'Chennai', 'Hyderabad', 'Goa', 'Kolkata'];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        {/* Search Header Banner */}
        <div className="relative overflow-hidden rounded-2xl bg-slate-900 border border-slate-800 p-8 mb-10 shadow-lg">
          <div className="relative z-10 max-w-2xl">
            <div className="flex items-center gap-2 text-blue-400 text-xs font-semibold uppercase tracking-wider mb-2">
              <HotelIcon className="w-4 h-4" />
              <span>Smart Travel Stays</span>
            </div>
            <h1 className="text-3xl sm:text-4xl font-bold text-white">
              Discover Luxury Stays Across India
            </h1>
            <p className="text-sm text-slate-400 mt-2">
              Explore 5-star properties, airport lounges, and boutique resorts with transparent pricing and verified reviews.
            </p>

            {/* Quick City Filters */}
            <div className="mt-4 flex flex-wrap items-center gap-2">
              <span className="text-xs text-slate-500 font-medium mr-1">Popular:</span>
              {cities.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => {
                    setCity(c);
                    setPage(0);
                  }}
                  className={`px-2.5 py-1 text-xs rounded-md transition-colors ${
                    city.toLowerCase() === c.toLowerCase()
                      ? 'bg-blue-600 text-white font-semibold'
                      : 'bg-slate-800 text-slate-400 hover:text-white'
                  }`}
                >
                  {c}
                </button>
              ))}
            </div>
          </div>

          {/* Search Form Bar */}
          <form onSubmit={handleSearchSubmit} className="relative z-10 mt-6 grid grid-cols-1 sm:grid-cols-4 gap-3">
            <div className="sm:col-span-2 relative">
              <MapPin className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                placeholder="Search by city (e.g. Mumbai, Delhi, Goa)..."
                value={city}
                onChange={(e) => setCity(e.target.value)}
                className="w-full pl-10 pr-4 py-3 bg-slate-950 border border-slate-700 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500"
              />
            </div>

            <div>
              <select
                value={minStars || ''}
                onChange={(e) => setMinStars(e.target.value ? Number(e.target.value) : undefined)}
                className="w-full px-3.5 py-3 bg-slate-950 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:border-blue-500"
              >
                <option value="">Any Star Rating</option>
                <option value="5">5-Star Luxury Only</option>
                <option value="4">4-Star and Above</option>
                <option value="3">3-Star and Above</option>
              </select>
            </div>

            <button
              type="submit"
              className="flex items-center justify-center gap-2 py-3 px-6 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-xl transition"
            >
              <Search className="w-4 h-4" />
              Search Stays
            </button>
          </form>
        </div>

        {/* Results Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-xl font-bold text-white">
              {city ? `Hotels in ${city}` : 'All Available Stays'}
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              {totalCount} {totalCount === 1 ? 'property' : 'properties'} found
            </p>
          </div>
        </div>

        {/* Hotels Grid */}
        {loading ? (
          <div className="space-y-4">
            {slowMessage && (
              <div className="p-4 rounded-2xl bg-sky-500/10 border border-sky-500/30 text-sky-300 text-xs flex items-center gap-3 animate-fade-in shadow-lg transition-all">
                <div className="w-2.5 h-2.5 rounded-full bg-sky-400 animate-ping shrink-0" />
                <div className="flex-1 font-medium text-sky-200">
                  {slowMessage}
                </div>
              </div>
            )}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {Array.from({ length: 6 }).map((_, i) => (
                <HotelCardSkeleton key={i} />
              ))}
            </div>
          </div>
        ) : hotels.length === 0 ? (
          <div className="py-20 text-center bg-slate-900/40 border border-slate-800 rounded-2xl p-8">
            <HotelIcon className="w-12 h-12 text-slate-700 mx-auto mb-3" />
            <h3 className="text-base font-bold text-white">No Hotels Found</h3>
            <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto mb-4">
              We couldn't find any stays matching your criteria. Try searching for a different city or removing filters.
            </p>
            <button
              onClick={() => {
                setCity('');
                setMinStars(undefined);
              }}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-white rounded-xl transition-colors"
            >
              Reset Filters
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {hotels.map((hotel) => (
              <div
                key={hotel.id}
                className="group bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl overflow-hidden flex flex-col justify-between transition duration-200"
              >
                <div>
                  {/* Hotel Image Card Banner */}
                  <div className="h-48 relative overflow-hidden">
                    <ImageWithFallback
                      src={resolveHotelPhotos(hotel)[0]}
                      alt={`${hotel.name} luxury facade`}
                      containerClassName="w-full h-full"
                      className="group-hover:scale-105 transition-transform duration-300"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/40 to-transparent p-4 flex flex-col justify-between">
                      <div className="flex items-center justify-between">
                        <span className="inline-flex items-center gap-1 text-[11px] font-bold text-amber-400 bg-slate-950/80 px-2.5 py-1 rounded-lg border border-amber-500/20 shadow-md">
                          <Star className="w-3 h-3 fill-amber-400" />
                          {hotel.starRating}-Star Hotel
                        </span>
                        {hotel.nearestAirportCode && (
                          <span className="text-[11px] font-mono text-slate-200 bg-slate-950/80 px-2 py-0.5 rounded border border-slate-700">
                            Near {hotel.nearestAirportCode}
                          </span>
                        )}
                      </div>

                      <div>
                        <h3 className="text-lg font-bold text-white transition-colors">
                          {hotel.name}
                        </h3>
                        <div className="flex items-center gap-1.5 text-xs text-slate-300 mt-0.5">
                          <MapPin className="w-3 h-3 text-blue-400" />
                          <span>{hotel.address?.city}, {hotel.address?.state}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Body Details */}
                  <div className="p-5">
                    {/* Rating bar */}
                    <div className="flex items-center justify-between text-xs mb-3 pb-3 border-b border-slate-800">
                      <div className="flex items-center gap-1.5">
                        <StarRating rating={hotel.averageRating} size="sm" />
                        <span className="font-semibold text-white">{hotel.averageRating.toFixed(1)}</span>
                      </div>
                      <span className="text-slate-400">
                        {hotel.totalReviews} {hotel.totalReviews === 1 ? 'review' : 'reviews'}
                      </span>
                    </div>

                    <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed mb-4">
                      {hotel.description}
                    </p>

                    {/* Amenities tags */}
                    <div className="flex flex-wrap gap-1.5">
                      {hotel.amenities?.slice(0, 3).map((amenity) => (
                        <span
                          key={amenity}
                          className="text-[11px] font-medium text-slate-300 bg-slate-800 px-2 py-0.5 rounded-md border border-slate-700"
                        >
                          {amenity}
                        </span>
                      ))}
                      {hotel.amenities && hotel.amenities.length > 3 && (
                        <span className="text-[11px] text-slate-500 px-1.5 py-0.5">
                          +{hotel.amenities.length - 3} more
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                {/* Footer Price & CTA */}
                <div className="p-5 pt-3 border-t border-slate-800 flex items-center justify-between">
                  <div>
                    <span className="text-[10px] text-slate-500 block">From</span>
                    <span className="text-lg font-bold text-white">
                      ₹{hotel.baseNightlyRate?.toLocaleString()}
                    </span>
                    <span className="text-[10px] text-slate-400"> / night</span>
                  </div>

                  <Link
                    to={`/hotels/${hotel.id}`}
                    className="flex items-center gap-1.5 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-lg transition"
                  >
                    View Rooms
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="mt-8 flex items-center justify-center gap-2">
            <button
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="px-4 py-2 bg-slate-900 border border-slate-800 hover:bg-slate-800 disabled:opacity-40 disabled:cursor-not-allowed text-xs font-medium text-slate-300 rounded-xl transition-colors"
            >
              Previous
            </button>
            <span className="text-xs text-slate-400 px-2">
              Page {page + 1} of {totalPages}
            </span>
            <button
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
              className="px-4 py-2 bg-slate-900 border border-slate-800 hover:bg-slate-800 disabled:opacity-40 disabled:cursor-not-allowed text-xs font-medium text-slate-300 rounded-xl transition-colors"
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
