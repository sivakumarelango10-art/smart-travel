import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Building2, Search, Star, MapPin, ArrowRight } from 'lucide-react';
import { Hotel } from '../types/api';
import { hotelService } from '../services/hotelService';
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
      if (isCurrent) setSlowMessage('Connecting to live hotel inventory…');
    }, 2500);

    hotelService
      .searchHotels({
        city: city.trim() || undefined,
        minStars,
        maxPrice,
        page,
        size: 9,
      })
      .then((res) => {
        if (isCurrent) {
          clearTimeout(stage1Timer);
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
      })
      .catch((err) => {
        if (isCurrent) {
          clearTimeout(stage1Timer);
          console.error('Failed to search hotels', err);
        }
      })
      .finally(() => {
        if (isCurrent) {
          clearTimeout(stage1Timer);
          setLoading(false);
          setSlowMessage(null);
        }
      });

    return () => {
      isCurrent = false;
      clearTimeout(stage1Timer);
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

  const cities = ['All Cities', 'Delhi', 'Mumbai', 'Bangalore', 'Chennai', 'Hyderabad', 'Goa', 'Kolkata'];

  return (
    <div className="space-y-8 pb-16">
      {/* 1. HERO SEARCH HEADER */}
      <section className="rounded-3xl bg-[#14161F] text-white p-6 sm:p-10 shadow-2xl border border-white/10 space-y-6">
        <div className="max-w-3xl space-y-2">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 text-xs font-bold shadow-glow-gold">
            <Building2 className="w-3.5 h-3.5" />
            <span>Luxury Stays & Business Hotels</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-black tracking-tight text-white">
            Find & Book Premium Accommodations
          </h1>
          <p className="text-xs sm:text-sm text-slate-300">
            Enjoy guaranteed room availability, interactive 3D virtual previews, and instant booking confirmation.
          </p>
        </div>

        {/* Search & Filter Form */}
        <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 sm:grid-cols-12 gap-3 pt-2">
          <div className="sm:col-span-6 relative">
            <input
              type="text"
              placeholder="Search by city, airport or landmark (e.g. Mumbai, Goa)"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              className="w-full bg-[#181A22] border border-white/10 rounded-xl px-4 py-3 pl-10 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-amber-400 transition font-medium"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3.5 pointer-events-none" />
          </div>

          <div className="sm:col-span-4">
            <select
              value={minStars || ''}
              onChange={(e) => setMinStars(e.target.value ? Number(e.target.value) : undefined)}
              className="w-full bg-[#181A22] border border-white/10 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-amber-400 transition cursor-pointer font-medium"
            >
              <option value="" className="bg-[#14161F]">All Star Ratings</option>
              <option value="5" className="bg-[#14161F]">5-Star Luxury Only</option>
              <option value="4" className="bg-[#14161F]">4-Star & Above</option>
              <option value="3" className="bg-[#14161F]">3-Star & Above</option>
            </select>
          </div>

          <div className="sm:col-span-2">
            <button
              type="submit"
              className="w-full h-full min-h-[46px] rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-extrabold text-sm shadow-glow-gold transition flex items-center justify-center gap-2 cursor-pointer"
            >
              <Search className="w-4 h-4 text-black" />
              <span>Search</span>
            </button>
          </div>
        </form>

        {/* Quick City Pills */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none text-xs">
          <span className="text-slate-400 font-semibold shrink-0">Popular:</span>
          {cities.map((c) => {
            const isSelected = (c === 'All Cities' && !city) || city.toLowerCase() === c.toLowerCase();
            return (
              <button
                key={c}
                type="button"
                onClick={() => {
                  setCity(c === 'All Cities' ? '' : c);
                  setPage(0);
                }}
                className={`px-3 py-1 rounded-full text-xs font-bold shrink-0 transition ${
                  isSelected
                    ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                    : 'bg-[#181A22] text-slate-300 hover:bg-[#1F222E] border border-white/10'
                }`}
              >
                {c}
              </button>
            );
          })}
        </div>
      </section>

      {/* 2. RESULTS GRID */}
      <section className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-black text-white tracking-tight">
              {city ? `Hotels in ${city}` : 'All Featured Hotels & Resorts'}
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Showing {totalCount} verified properties with instant confirmation
            </p>
          </div>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <HotelCardSkeleton key={i} />
            ))}
          </div>
        ) : hotels.length === 0 ? (
          <div className="p-12 rounded-2xl bg-[#14161F] border border-white/10 text-center space-y-4 shadow-xl">
            <div className="w-14 h-14 rounded-2xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center mx-auto shadow-glow-gold">
              <Building2 className="w-7 h-7" />
            </div>
            <h3 className="text-lg font-black text-white">No Hotels Found</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              No hotels match your search criteria. Try clearing the filter or choosing another destination city.
            </p>
            <button
              type="button"
              onClick={() => {
                setCity('');
                setMinStars(undefined);
              }}
              className="px-4 py-2 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black text-xs font-extrabold transition shadow-glow-gold"
            >
              Reset Search
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {hotels.map((hotel) => {
              const photos = resolveHotelPhotos(hotel);
              const thumbnail = photos[0];

              return (
                <div
                  key={hotel.id}
                  className="rounded-2xl bg-[#14161F] border border-white/10 hover:border-amber-500/40 hover:shadow-card-hover overflow-hidden group flex flex-col justify-between transition-all duration-300"
                >
                  <div>
                    {/* Hotel Image with Badges */}
                    <div className="relative h-48 overflow-hidden bg-[#181A22]">
                      <ImageWithFallback
                        src={thumbnail}
                        alt={hotel.name}
                        containerClassName="w-full h-full"
                        className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                      />
                      <span className="absolute top-3 left-3 px-2.5 py-1 rounded-full bg-[#0B0C10]/85 backdrop-blur-md text-amber-400 text-[11px] font-bold flex items-center gap-1 border border-white/10 shadow-md">
                        <Star className="w-3 h-3 fill-amber-400 text-amber-400" />
                        {hotel.starRating}-Star
                      </span>
                      {hotel.nearestAirportCode && (
                        <span className="absolute top-3 right-3 px-2 py-0.5 rounded-md bg-[#0B0C10]/85 text-amber-400 font-mono text-[10px] font-bold border border-white/10">
                          Near {hotel.nearestAirportCode}
                        </span>
                      )}
                    </div>

                    {/* Content */}
                    <div className="p-5 space-y-3">
                      <div>
                        <h3 className="text-lg font-black text-white group-hover:text-amber-400 transition line-clamp-1">
                          {hotel.name}
                        </h3>
                        <p className="text-xs text-slate-400 flex items-center gap-1 mt-1">
                          <MapPin className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                          <span>
                            {hotel.address?.city}, {hotel.address?.country}
                          </span>
                        </p>
                      </div>

                      {/* Amenities Preview */}
                      <div className="flex flex-wrap gap-1.5 pt-1">
                        {hotel.amenities?.slice(0, 3).map((amenity) => (
                          <span
                            key={amenity}
                            className="text-[10px] font-semibold text-slate-300 bg-[#181A22] px-2 py-0.5 rounded-md border border-white/5"
                          >
                            {amenity}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Pricing & CTA Footer */}
                  <div className="p-5 pt-3 border-t border-white/5 flex items-center justify-between">
                    <div>
                      <span className="text-[10px] text-slate-400 font-medium block">Starting rate</span>
                      <div className="text-xl font-black text-amber-400">
                        ₹{hotel.baseNightlyRate?.toLocaleString('en-IN')}
                        <span className="text-xs text-slate-400 font-normal"> / night</span>
                      </div>
                    </div>

                    <Link
                      to={`/hotels/${hotel.id}`}
                      className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black text-xs font-black flex items-center gap-1.5 transition shadow-glow-gold"
                    >
                      <span>Select Rooms</span>
                      <ArrowRight className="w-3.5 h-3.5 text-black" />
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 pt-6">
            <button
              type="button"
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="px-4 py-2 rounded-xl bg-[#14161F] border border-white/10 text-xs font-bold text-slate-300 hover:bg-[#1F222E] disabled:opacity-40 transition"
            >
              Previous
            </button>
            <span className="text-xs font-bold text-slate-400 px-3">
              Page {page + 1} of {totalPages}
            </span>
            <button
              type="button"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
              className="px-4 py-2 rounded-xl bg-[#14161F] border border-white/10 text-xs font-bold text-slate-300 hover:bg-[#1F222E] disabled:opacity-40 transition"
            >
              Next
            </button>
          </div>
        )}
      </section>
    </div>
  );
};
