import React, { useState, useEffect, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Star,
  MapPin,
  Coffee,
  Check,
  ArrowLeft,
  BedDouble,
  Users,
  Maximize2,
  Lock,
  AlertCircle,
  CheckCircle2,
  Radio,
  Eye,
  Sparkles,
  Compass,
  Image as ImageIcon,
  ShieldCheck,
  Calendar,
  Clock,
  Phone,
  Mail,
  ExternalLink,
} from 'lucide-react';
import { Hotel, RoomType, RoomAvailabilityEvent } from '../types/api';
import { hotelService } from '../services/hotelService';
import { StarRating } from '../components/StarRating';
import { ReviewSection } from '../components/ReviewSection';
import { ImageWithFallback } from '../components/ImageWithFallback';
import { Panorama360Viewer } from '../components/Panorama360Viewer';
import { recommendationService } from '../services/recommendationService';
import { useAuth } from '../context/AuthContext';
import { resolveHotelPhotos } from '../utils/hotelImageRegistry';
import { useHotelRoomWebSocket } from '../hooks/useHotelRoomWebSocket';

export const HotelDetailsPage: React.FC = () => {
  const { hotelId } = useParams<{ hotelId: string }>();
  const { user } = useAuth();

  const [hotel, setHotel] = useState<Hotel | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activePhotoIndex, setActivePhotoIndex] = useState(0);

  // Room hold state
  const [holdingRoomId, setHoldingRoomId] = useState<string | null>(null);
  const [holdSuccess, setHoldSuccess] = useState<string | null>(null);

  // 360 Panorama Modal State
  const [active360, setActive360] = useState<{ url: string; title: string; subtitle?: string } | null>(null);

  // Real-time room availability notification
  const [roomUpdateNotice, setRoomUpdateNotice] = useState<string | null>(null);

  // Real-time WebSocket connection to /topic/hotels/{hotelId}/rooms
  useHotelRoomWebSocket({
    hotelId,
    onRoomUpdate: (event: RoomAvailabilityEvent) => {
      if (event && event.roomTypeId) {
        setHotel((prev) => {
          if (!prev) return null;
          return {
            ...prev,
            roomTypes: prev.roomTypes.map((rt) =>
              rt.id === event.roomTypeId
                ? { ...rt, availableRooms: event.availableRooms, totalRooms: event.totalRooms || rt.totalRooms }
                : rt
            ),
          };
        });
        setRoomUpdateNotice(`Live inventory update: ${event.roomTypeName || 'Room'} (${event.availableRooms} left)`);
        setTimeout(() => setRoomUpdateNotice(null), 4000);
      }
    },
    enabled: !!hotelId,
  });

  const cleanHotelId = useMemo(() => {
    return hotelId ? decodeURIComponent(hotelId).trim() : '';
  }, [hotelId]);

  useEffect(() => {
    if (!cleanHotelId) return;

    setLoading(true);
    setError(null);
    hotelService
      .getHotel(cleanHotelId)
      .then((data) => {
        setHotel(data);
        setActivePhotoIndex(0);
        // Track view activity
        recommendationService.trackActivity({
          activityType: 'VIEW_HOTEL',
          targetId: cleanHotelId,
          targetType: 'HOTEL',
          metadata: { name: data.name, city: data.address?.city },
        });
      })
      .catch((err: any) => {
        const msg = err.response?.data?.message || err.message || 'Hotel property not found in catalog.';
        setError(msg);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [cleanHotelId]);

  const handleHoldRoom = async (roomTypeId: string) => {
    if (!hotelId) return;

    setHoldingRoomId(roomTypeId);
    setError(null);
    setHoldSuccess(null);

    try {
      const res = await hotelService.holdRoom(hotelId, roomTypeId, 1);
      setHoldSuccess(
        `Room hold placed successfully for ${res.name || 'room'}! Reserved for 15 minutes.`
      );
    } catch (err: any) {
      setError(err.message || 'Failed to reserve room. It may currently be at full capacity.');
    } finally {
      setHoldingRoomId(null);
    }
  };

  const userPreferredRoomType = user?.preferences?.preferredRoomType;

  const photos = useMemo(() => {
    if (!hotel) return [];
    return resolveHotelPhotos(hotel);
  }, [hotel]);

  // Base price computation for delta display
  const baseRoomPrice = useMemo(() => {
    if (!hotel?.roomTypes || hotel.roomTypes.length === 0) return 0;
    return Math.min(...hotel.roomTypes.map((r) => r.nightlyRate || 0));
  }, [hotel]);

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8 animate-pulse">
        <div className="h-96 bg-[#14161F] border border-white/10 rounded-3xl" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="h-64 bg-[#14161F] border border-white/10 rounded-2xl" />
          <div className="h-64 bg-[#14161F] border border-white/10 rounded-2xl" />
          <div className="h-64 bg-[#14161F] border border-white/10 rounded-2xl" />
        </div>
      </div>
    );
  }

  if (error || !hotel) {
    return (
      <div className="max-w-xl mx-auto my-16 p-8 text-center bg-[#14161F] border border-white/10 rounded-3xl shadow-xl space-y-4">
        <div className="w-12 h-12 rounded-2xl bg-rose-500/15 text-rose-400 border border-rose-500/30 flex items-center justify-center mx-auto">
          <AlertCircle className="w-6 h-6" />
        </div>
        <h2 className="text-xl font-bold text-white">Property Unavailable</h2>
        <p className="text-xs text-slate-400">{error || 'Hotel property not found in catalog.'}</p>
        <Link
          to="/hotels"
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-glow-gold"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Return to Hotel Search</span>
        </Link>
      </div>
    );
  }

  const hasHotel360 = Boolean(hotel.virtualTour?.enabled && hotel.virtualTour?.panoramaUrl);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Navigation & Live Telemetry Banner */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <Link
          to="/hotels"
          className="inline-flex items-center gap-2 text-xs font-bold text-slate-300 hover:text-amber-400 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to All Stays</span>
        </Link>

        {roomUpdateNotice && (
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 text-xs font-semibold animate-fade-in shadow-glow-gold">
            <Radio className="w-3.5 h-3.5 animate-pulse" />
            <span>{roomUpdateNotice}</span>
          </div>
        )}
      </div>

      {/* 1. HERO PHOTO SHOWCASE WITH 360 LAUNCH BUTTON */}
      <section className="space-y-4">
        <div className="relative h-[340px] sm:h-[480px] rounded-3xl overflow-hidden shadow-2xl border border-white/10 bg-[#12131A] group">
          <ImageWithFallback
            src={photos[activePhotoIndex] || photos[0]}
            alt={hotel.name}
            containerClassName="w-full h-full"
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-[#0B0C10] via-transparent to-transparent pointer-events-none" />

          {/* Hero Overlay Info */}
          <div className="absolute inset-0 p-6 sm:p-8 flex flex-col justify-between pointer-events-none">
            <div className="flex items-center justify-between">
              <span className="inline-flex items-center gap-1.5 text-xs font-bold text-amber-400 bg-[#0B0C10]/85 px-3 py-1.5 rounded-lg border border-amber-500/20 shadow-md pointer-events-auto">
                <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                {hotel.starRating}-Star Luxury Property
              </span>

              {/* 360 CTA in Hero Header */}
              {hasHotel360 && (
                <button
                  type="button"
                  onClick={() => {
                    if (hotel.virtualTour?.panoramaUrl) {
                      setActive360({
                        url: hotel.virtualTour.panoramaUrl,
                        title: hotel.name,
                        subtitle: 'Drag in 360° to explore the hotel environment',
                      });
                    }
                  }}
                  className="pointer-events-auto px-4 py-2 rounded-xl bg-amber-400 hover:bg-amber-300 text-black font-extrabold text-xs flex items-center gap-2 shadow-glow-gold transition-all duration-200 hover:scale-105 cursor-pointer"
                >
                  <Compass className="w-4 h-4 animate-spin-slow text-black" />
                  <span>Explore in 360° Virtual Tour</span>
                </button>
              )}
            </div>

            <div>
              <h1 className="text-3xl sm:text-4xl font-black text-white drop-shadow-lg tracking-tight">
                {hotel.name}
              </h1>
              <div className="flex items-center gap-2 text-sm text-slate-200 mt-2">
                <MapPin className="w-4 h-4 text-amber-400 shrink-0" />
                <span>
                  {hotel.address?.line1}, {hotel.address?.city}, {hotel.address?.state}, {hotel.address?.country}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Photo Thumbnail Ribbon */}
        {photos.length > 1 && (
          <div className="flex items-center gap-3 overflow-x-auto pb-2 scrollbar-none">
            {photos.map((imgUrl, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => setActivePhotoIndex(idx)}
                className={`relative w-24 h-16 sm:w-32 sm:h-20 rounded-xl overflow-hidden shrink-0 border-2 transition duration-200 ${
                  activePhotoIndex === idx
                    ? 'border-amber-400 shadow-glow-gold scale-105'
                    : 'border-white/10 opacity-60 hover:opacity-100'
                }`}
              >
                <ImageWithFallback
                  src={imgUrl}
                  alt={`${hotel.name} thumbnail ${idx + 1}`}
                  containerClassName="w-full h-full"
                />
              </button>
            ))}
          </div>
        )}
      </section>

      {/* 2. HOTEL INFORMATION, POLICIES & AMENITIES */}
      <div className="relative overflow-hidden rounded-3xl bg-[#14161F] border border-white/10 p-8 shadow-2xl backdrop-blur-xl">
        <div className="flex flex-wrap items-start justify-between gap-6">
          <div className="max-w-2xl space-y-4">
            <h2 className="text-xl font-extrabold text-white">About the Property</h2>
            <p className="text-sm text-slate-300 leading-relaxed">
              {hotel.description}
            </p>

            {/* Check-in / Policy Badges */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 pt-2">
              <div className="p-3 bg-[#181A22] rounded-xl border border-white/5 flex items-center gap-2.5">
                <Clock className="w-4 h-4 text-amber-400 shrink-0" />
                <div>
                  <span className="text-[10px] text-slate-400 block">Check-in</span>
                  <strong className="text-xs text-white">2:00 PM</strong>
                </div>
              </div>
              <div className="p-3 bg-[#181A22] rounded-xl border border-white/5 flex items-center gap-2.5">
                <Clock className="w-4 h-4 text-amber-400 shrink-0" />
                <div>
                  <span className="text-[10px] text-slate-400 block">Check-out</span>
                  <strong className="text-xs text-white">12:00 PM</strong>
                </div>
              </div>
              <div className="p-3 bg-[#181A22] rounded-xl border border-white/5 flex items-center gap-2.5">
                <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0" />
                <div>
                  <span className="text-[10px] text-slate-400 block">Cancellation</span>
                  <strong className="text-xs text-emerald-400">Free before 24h</strong>
                </div>
              </div>
            </div>
          </div>

          {/* Right Card: Quick summary & rating */}
          <div className="p-5 bg-[#181A22] border border-white/10 rounded-2xl text-right min-w-[240px]">
            <div className="text-xs text-slate-400 mb-1">Guest Review Score</div>
            <div className="flex items-center justify-end gap-2">
              <span className="text-3xl font-black text-amber-400">
                {hotel.averageRating.toFixed(1)}
              </span>
              <div>
                <StarRating rating={hotel.averageRating} size="sm" />
                <span className="text-[11px] text-slate-400 block">
                  {hotel.totalReviews} verified reviews
                </span>
              </div>
            </div>

            <div className="mt-4 pt-3 border-t border-white/10 text-left">
              <span className="text-[10px] text-slate-400 block">Starting Nightly Rate</span>
              <span className="text-2xl font-black text-white">
                ₹{hotel.baseNightlyRate?.toLocaleString()}
              </span>
              <span className="text-xs text-slate-400"> / night</span>
            </div>
          </div>
        </div>

        {/* Amenities Grid */}
        <div className="mt-8 pt-6 border-t border-white/10">
          <h3 className="text-xs font-semibold text-slate-300 uppercase tracking-wider mb-3">
            Included Amenities & Perks
          </h3>
          <div className="flex flex-wrap gap-2">
            {hotel.amenities?.map((amenity) => (
              <span
                key={amenity}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-[#181A22] border border-white/10 rounded-lg text-xs font-medium text-slate-200"
              >
                <Check className="w-3.5 h-3.5 text-amber-400" />
                {amenity}
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Room Hold Advisory */}
      {holdSuccess && (
        <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 rounded-2xl text-sm flex items-center gap-3 animate-fade-in shadow-glow-emerald">
          <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-emerald-400" />
          <span>{holdSuccess}</span>
        </div>
      )}

      {/* 3. ROOM SELECTION GRID WITH 360 TOURS */}
      <section>
        <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
          <div>
            <h2 className="text-2xl font-bold text-white flex items-center gap-2">
              <BedDouble className="w-6 h-6 text-amber-400" />
              Select Your Room Category
            </h2>
            <p className="text-xs text-slate-400 mt-1">
              Atomic room locking guarantees no double-booking during your checkout flow.
            </p>
          </div>

          {userPreferredRoomType && (
            <div className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 text-xs font-semibold shadow-glow-gold">
              <Sparkles className="w-3.5 h-3.5 text-amber-400" />
              Your Saved Preference: <strong className="text-white uppercase">{userPreferredRoomType}</strong>
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {hotel.roomTypes?.map((room) => {
            const isAvailable = room.availableRooms > 0;
            const isHoldingThis = holdingRoomId === room.id;
            const matchesPref = userPreferredRoomType && room.category === userPreferredRoomType;
            const upgradeDelta = (room.nightlyRate || 0) - baseRoomPrice;
            const roomPano = room.virtualTour?.panoramaUrl || hotel.virtualTour?.panoramaUrl;

            return (
              <div
                key={room.id}
                className={`rounded-2xl border flex flex-col justify-between overflow-hidden transition-all duration-300 ${
                  matchesPref
                    ? 'bg-[#14161F] border-2 border-amber-400 shadow-xl shadow-amber-500/10'
                    : isAvailable
                    ? 'bg-[#14161F] hover:border-amber-500/40 border-white/10 hover:shadow-card-hover'
                    : 'bg-[#0B0C10] border-white/5 opacity-60'
                }`}
              >
                <div>
                  {/* Room Category Header */}
                  <div className="p-5 border-b border-white/10 bg-[#181A22]/50">
                    <div className="flex items-center justify-between gap-2 mb-1">
                      <span className="text-[10px] font-semibold uppercase tracking-wider text-amber-400 bg-amber-400/10 px-2.5 py-0.5 rounded border border-amber-400/20">
                        {room.category}
                      </span>
                      <span
                        className={`text-xs font-medium px-2 py-0.5 rounded ${
                          isAvailable
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                            : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                        }`}
                      >
                        {isAvailable ? `${room.availableRooms} left` : 'Sold Out'}
                      </span>
                    </div>

                    {matchesPref && (
                      <div className="mt-2 inline-flex items-center gap-1 text-[11px] font-bold text-amber-400 bg-amber-400/10 border border-amber-400/20 px-2 py-0.5 rounded-md">
                        <Sparkles className="w-3 h-3 text-amber-400" /> Recommended based on your preferences
                      </div>
                    )}

                    <h3 className="text-lg font-bold text-white mt-2">{room.name}</h3>
                    <p className="text-xs text-slate-400 mt-1 line-clamp-2">{room.description}</p>
                  </div>

                  {/* Room Specs */}
                  <div className="p-5 space-y-3 text-xs border-b border-white/10">
                    <div className="grid grid-cols-2 gap-2 text-slate-300">
                      <div className="flex items-center gap-1.5">
                        <Users className="w-3.5 h-3.5 text-amber-400" />
                        <span>Up to {room.maxOccupancy} Guests</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <BedDouble className="w-3.5 h-3.5 text-amber-400" />
                        <span>{room.bedType} Bed</span>
                      </div>
                      {room.sizeInSqFt && (
                        <div className="flex items-center gap-1.5">
                          <Maximize2 className="w-3.5 h-3.5 text-amber-400" />
                          <span>{room.sizeInSqFt} sq. ft.</span>
                        </div>
                      )}
                      {room.breakfastIncluded && (
                        <div className="flex items-center gap-1.5 text-emerald-400 font-medium">
                          <Coffee className="w-3.5 h-3.5" />
                          <span>Free Breakfast</span>
                        </div>
                      )}
                    </div>

                    {/* Amenities List */}
                    <div className="flex flex-wrap gap-1.5 pt-2">
                      {room.amenities?.map((a) => (
                        <span
                          key={a}
                          className="text-[10px] font-medium text-slate-300 bg-[#181A22] px-2 py-0.5 rounded border border-white/5"
                        >
                          {a}
                        </span>
                      ))}
                    </div>

                    {/* 360 Virtual Tour Launch Button */}
                    {roomPano && (
                      <div className="pt-2">
                        <button
                          type="button"
                          onClick={() => {
                            setActive360({
                              url: roomPano,
                              title: `${hotel.name} — ${room.name}`,
                              subtitle: 'Interactive 360° Room Perspective • Drag to look around',
                            });
                          }}
                          className="w-full py-2 px-3 rounded-xl bg-[#181A22] hover:bg-amber-400 hover:text-black border border-amber-400/30 text-amber-400 text-xs font-bold flex items-center justify-center gap-2 transition hover:scale-[1.02] shadow-glow-gold cursor-pointer"
                        >
                          <Compass className="w-4 h-4 animate-spin-slow" />
                          <span>Explore Room in 360°</span>
                        </button>
                      </div>
                    )}
                  </div>
                </div>

                {/* Room Pricing & Lock CTA */}
                <div className="p-5 bg-[#181A22]/70 flex items-center justify-between gap-4">
                  <div>
                    <div className="text-[10px] text-slate-400">Nightly Rate</div>
                    <div className="text-xl font-black text-amber-400">
                      ₹{room.totalNightlyRate ? room.totalNightlyRate.toLocaleString() : room.nightlyRate?.toLocaleString()}
                    </div>
                    <div className="text-[10px] text-slate-400">
                      {upgradeDelta > 0 ? (
                        <span className="text-amber-400 font-semibold">+₹{upgradeDelta.toLocaleString()} upgrade</span>
                      ) : (
                        <span>Base Rate</span>
                      )}
                    </div>
                  </div>

                  <button
                    type="button"
                    disabled={!isAvailable || isHoldingThis}
                    onClick={() => handleHoldRoom(room.id)}
                    className={`px-4 py-2.5 rounded-xl font-extrabold text-xs flex items-center gap-1.5 transition ${
                      isAvailable
                        ? 'bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black shadow-glow-gold'
                        : 'bg-[#181A22] text-slate-500 cursor-not-allowed'
                    }`}
                  >
                    <Lock className="w-3.5 h-3.5 text-black" />
                    <span>{isHoldingThis ? 'Reserving...' : 'Reserve Room'}</span>
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* 4. VERIFIED GUEST REVIEWS */}
      <ReviewSection
        targetId={hotel.id}
        targetType="HOTEL"
        targetName={hotel.name}
      />

      {/* 360° EQUIRECTANGULAR PANORAMA VIEWER MODAL */}
      {active360 && (
        <Panorama360Viewer
          isOpen={!!active360}
          panoramaUrl={active360.url}
          title={active360.title}
          subtitle={active360.subtitle}
          onClose={() => setActive360(null)}
        />
      )}
    </div>
  );
};
