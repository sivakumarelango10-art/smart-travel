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
  X,
  Compass
} from 'lucide-react';
import { Hotel, RoomType, RoomAvailabilityEvent } from '../types/api';
import { hotelService } from '../services/hotelService';
import { StarRating } from '../components/StarRating';
import { ReviewSection } from '../components/ReviewSection';
import { ImageWithFallback } from '../components/ImageWithFallback';
import { recommendationService } from '../services/recommendationService';
import { useAuth } from '../context/AuthContext';
import { resolveHotelPhotos } from '../utils/hotelImageRegistry';
import { useHotelRoomWebSocket } from '../hooks/useHotelRoomWebSocket';

export const HotelDetailsPage: React.FC = () => {
  const { hotelId } = useParams<{ hotelId: string }>();
  const { isAuthenticated, user } = useAuth();

  const [hotel, setHotel] = useState<Hotel | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activePhotoIndex, setActivePhotoIndex] = useState(0);

  // Room hold state
  const [holdingRoomId, setHoldingRoomId] = useState<string | null>(null);
  const [holdSuccess, setHoldSuccess] = useState<string | null>(null);

  // 3D Room Preview Modal state
  const [previewRoom, setPreviewRoom] = useState<RoomType | null>(null);
  const [is3DMode, setIs3DMode] = useState<boolean>(false);
  const [rotationAngle, setRotationAngle] = useState<number>(0);

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

  useEffect(() => {
    if (!hotelId) return;

    setLoading(true);
    hotelService
      .getHotel(hotelId)
      .then((data) => {
        setHotel(data);
        setActivePhotoIndex(0);
        // Track view activity
        recommendationService.trackActivity({
          activityType: 'VIEW_HOTEL',
          targetId: hotelId,
          targetType: 'HOTEL',
          metadata: { name: data.name, city: data.address?.city },
        });
      })
      .catch((err: any) => {
        setError(err.message || 'Failed to load hotel details');
      })
      .finally(() => {
        setLoading(false);
      });
  }, [hotelId]);

  const handleHoldRoom = async (roomTypeId: string) => {
    if (!hotelId) return;
    if (!isAuthenticated) {
      alert('Please sign in to reserve a room.');
      return;
    }

    setHoldingRoomId(roomTypeId);
    setHoldSuccess(null);
    try {
      const updatedRoom = await hotelService.holdRoom(hotelId, roomTypeId, 1);
      // Update room in hotel state
      setHotel((prev) => {
        if (!prev) return null;
        return {
          ...prev,
          roomTypes: prev.roomTypes.map((rt) => (rt.id === roomTypeId ? updatedRoom : rt)),
        };
      });
      setHoldSuccess(`Successfully held 1 room in ${updatedRoom.name}! Room inventory atomically locked for checkout.`);
    } catch (err: any) {
      alert(err.message || 'Failed to hold room. Please try another room category.');
    } finally {
      setHoldingRoomId(null);
    }
  };

  // Base starting room price for calculating room upgrade deltas
  const baseRoomPrice = useMemo(() => {
    if (!hotel?.roomTypes || hotel.roomTypes.length === 0) return 0;
    const rates = hotel.roomTypes.map((rt) => rt.nightlyRate || 0).filter((r) => r > 0);
    return rates.length > 0 ? Math.min(...rates) : 0;
  }, [hotel]);

  // Preferred room type from user travel preferences
  const userPreferredRoomType = user?.preferences?.preferredRoomType?.toUpperCase() || '';

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 py-24 text-center text-slate-400">
        <div className="w-10 h-10 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p className="text-sm font-semibold">Retrieving property details & live room inventory...</p>
      </div>
    );
  }

  if (error || !hotel) {
    return (
      <div className="min-h-screen bg-slate-950 py-20 px-4 text-center">
        <div className="max-w-md mx-auto p-6 bg-slate-900 border border-slate-800 rounded-2xl">
          <AlertCircle className="w-10 h-10 text-rose-400 mx-auto mb-3" />
          <h2 className="text-lg font-bold text-white mb-1">Hotel Not Found</h2>
          <p className="text-xs text-slate-400 mb-6">{error || 'The requested property does not exist.'}</p>
          <Link
            to="/hotels"
            className="inline-flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold rounded-xl transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Hotels
          </Link>
        </div>
      </div>
    );
  }

  const photos = resolveHotelPhotos(hotel);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-8">
        {/* Navigation Breadcrumb & Live Inventory Indicator */}
        <div className="flex flex-wrap items-center justify-between gap-4">
          <Link
            to="/hotels"
            className="inline-flex items-center gap-2 text-xs font-semibold text-slate-400 hover:text-slate-200 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to All Hotels
          </Link>

          <div className="flex items-center gap-2 text-xs text-slate-400">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 font-bold text-[11px]">
              <Radio className="w-3 h-3 animate-pulse" /> Live Room Inventory Active
            </span>
            {roomUpdateNotice && (
              <span className="text-sky-400 font-medium animate-fade-in">{roomUpdateNotice}</span>
            )}
          </div>
        </div>

        {/* 1. HERO PHOTO GALLERY SHOWCASE */}
        <section className="space-y-3">
          <div className="relative h-80 sm:h-[450px] rounded-2xl overflow-hidden border border-slate-800 shadow-xl bg-slate-900">
            <ImageWithFallback
              src={photos[activePhotoIndex] || photos[0]}
              alt={`${hotel.name} featured photo`}
              containerClassName="w-full h-full"
              className="w-full h-full object-cover transition-all duration-300"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/20 to-transparent p-6 sm:p-8 flex flex-col justify-between pointer-events-none">
              <div className="flex items-center justify-between">
                <span className="inline-flex items-center gap-1.5 text-xs font-bold text-amber-400 bg-slate-950/80 px-3 py-1.5 rounded-lg border border-amber-500/20 shadow-md pointer-events-auto">
                  <Star className="w-3.5 h-3.5 fill-amber-400" />
                  {hotel.starRating}-Star Luxury Property
                </span>
                {hotel.nearestAirportCode && (
                  <span className="text-xs font-mono text-slate-200 bg-slate-950/80 px-3 py-1.5 rounded-lg border border-slate-700 shadow-md pointer-events-auto">
                    Near {hotel.nearestAirportCode} Airport
                  </span>
                )}
              </div>

              <div>
                <h1 className="text-3xl sm:text-4xl font-bold text-white drop-shadow-lg tracking-tight">
                  {hotel.name}
                </h1>
                <div className="flex items-center gap-2 text-sm text-slate-200 mt-2">
                  <MapPin className="w-4 h-4 text-blue-400 shrink-0" />
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
                  className={`relative w-24 h-16 sm:w-32 sm:h-20 rounded-xl overflow-hidden shrink-0 border-2 transition duration-150 ${
                    activePhotoIndex === idx
                      ? 'border-blue-500 scale-105'
                      : 'border-slate-800 opacity-60 hover:opacity-100'
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

        {/* 2. HOTEL INFORMATION & AMENITIES */}
        <div className="relative overflow-hidden rounded-3xl bg-slate-900/90 border border-slate-800 p-8 shadow-2xl backdrop-blur-xl">
          <div className="flex flex-wrap items-start justify-between gap-6">
            <div className="max-w-2xl">
              <h2 className="text-xl font-extrabold text-white">About the Property</h2>
              <p className="text-sm text-slate-400 mt-3 leading-relaxed">
                {hotel.description}
              </p>
            </div>

            {/* Right Card: Quick summary & rating */}
            <div className="p-5 bg-slate-950/80 border border-slate-800 rounded-2xl text-right min-w-[240px]">
              <div className="text-xs text-slate-400 mb-1">Guest Review Score</div>
              <div className="flex items-center justify-end gap-2">
                <span className="text-3xl font-extrabold text-white">
                  {hotel.averageRating.toFixed(1)}
                </span>
                <div>
                  <StarRating rating={hotel.averageRating} size="sm" />
                  <span className="text-[11px] text-slate-400 block">
                    {hotel.totalReviews} verified reviews
                  </span>
                </div>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-800 text-left">
                <span className="text-[10px] text-slate-500 block">Starting Nightly Rate</span>
                <span className="text-2xl font-bold text-white">
                  ₹{hotel.baseNightlyRate?.toLocaleString()}
                </span>
                <span className="text-xs text-slate-400"> / night</span>
              </div>
            </div>
          </div>

          {/* Amenities Grid */}
          <div className="mt-8 pt-6 border-t border-slate-800">
            <h3 className="text-xs font-semibold text-slate-300 uppercase tracking-wider mb-3">
              Included Amenities & Perks
            </h3>
            <div className="flex flex-wrap gap-2">
              {hotel.amenities?.map((amenity) => (
                <span
                  key={amenity}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-800 border border-slate-700 rounded-lg text-xs font-medium text-slate-200"
                >
                  <Check className="w-3.5 h-3.5 text-blue-400" />
                  {amenity}
                </span>
              ))}
            </div>
          </div>
        </div>

        {/* Room Hold Advisory */}
        {holdSuccess && (
          <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 rounded-2xl text-sm flex items-center gap-3 animate-fade-in">
            <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-emerald-400" />
            <span>{holdSuccess}</span>
          </div>
        )}

        {/* 3. ROOM SELECTION GRID */}
        <section>
          <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
            <div>
              <h2 className="text-2xl font-bold text-white flex items-center gap-2">
                <BedDouble className="w-6 h-6 text-blue-400" />
                Select Your Room Category
              </h2>
              <p className="text-xs text-slate-400 mt-1">
                Atomic room locking guarantees no double-booking during your checkout flow.
              </p>
            </div>

            {userPreferredRoomType && (
              <div className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-indigo-500/15 border border-indigo-500/30 text-indigo-300 text-xs font-semibold">
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

              return (
                <div
                  key={room.id}
                  className={`rounded-2xl border flex flex-col justify-between overflow-hidden transition-all duration-200 ${
                    matchesPref
                      ? 'bg-slate-900 border-2 border-indigo-500/80 shadow-xl shadow-indigo-500/10'
                      : isAvailable
                      ? 'bg-slate-900 hover:bg-slate-900/90 border-slate-800 hover:border-slate-700 shadow-md'
                      : 'bg-slate-950/40 border-slate-900 opacity-60'
                  }`}
                >
                  <div>
                    {/* Room Category Header */}
                    <div className="p-5 border-b border-slate-800 bg-slate-950/40">
                      <div className="flex items-center justify-between gap-2 mb-1">
                        <span className="text-[10px] font-semibold uppercase tracking-wider text-slate-300 bg-slate-800 px-2 py-0.5 rounded border border-slate-700">
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
                          <Sparkles className="w-3 h-3" /> Recommended based on your preferences
                        </div>
                      )}

                      <h3 className="text-lg font-bold text-white mt-2">{room.name}</h3>
                      <p className="text-xs text-slate-400 mt-1 line-clamp-2">{room.description}</p>
                    </div>

                    {/* Room Specs */}
                    <div className="p-5 space-y-3 text-xs border-b border-slate-800">
                      <div className="grid grid-cols-2 gap-2 text-slate-300">
                        <div className="flex items-center gap-1.5">
                          <Users className="w-3.5 h-3.5 text-slate-500" />
                          <span>Up to {room.maxOccupancy} Guests</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <BedDouble className="w-3.5 h-3.5 text-slate-500" />
                          <span>{room.bedType} Bed</span>
                        </div>
                        {room.sizeInSqFt && (
                          <div className="flex items-center gap-1.5">
                            <Maximize2 className="w-3.5 h-3.5 text-slate-500" />
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
                            className="text-[10px] font-medium text-slate-400 bg-slate-800 px-2 py-0.5 rounded"
                          >
                            {a}
                          </span>
                        ))}
                      </div>

                      {/* 3D Preview / Gallery Trigger */}
                      <div className="pt-2">
                        <button
                          type="button"
                          onClick={() => {
                            setPreviewRoom(room);
                            setIs3DMode(false);
                          }}
                          className="w-full py-2 px-3 rounded-xl bg-slate-800/80 hover:bg-slate-750 border border-slate-700 text-sky-400 text-xs font-semibold flex items-center justify-center gap-1.5 transition"
                        >
                          <Eye className="w-3.5 h-3.5" />
                          <span>View 3D Preview & Images</span>
                        </button>
                      </div>
                    </div>
                  </div>

                  {/* Room Pricing & Lock CTA */}
                  <div className="p-5 bg-slate-950/60 flex items-center justify-between gap-4">
                    <div>
                      <div className="text-[10px] text-slate-500">Nightly Rate</div>
                      <div className="text-xl font-bold text-white">
                        ₹{room.totalNightlyRate ? room.totalNightlyRate.toLocaleString() : room.nightlyRate?.toLocaleString()}
                      </div>
                      <div className="text-[10px] text-slate-500">
                        {upgradeDelta > 0 ? (
                          <span className="text-indigo-300 font-semibold">+₹{upgradeDelta.toLocaleString()} upgrade</span>
                        ) : (
                          <span>Base Rate</span>
                        )}
                      </div>
                    </div>

                    <button
                      type="button"
                      disabled={!isAvailable || isHoldingThis}
                      onClick={() => handleHoldRoom(room.id)}
                      className={`px-4 py-2.5 rounded-xl font-semibold text-xs flex items-center gap-1.5 transition ${
                        isAvailable
                          ? 'bg-blue-600 hover:bg-blue-700 text-white shadow-lg shadow-blue-600/30'
                          : 'bg-slate-800 text-slate-500 cursor-not-allowed'
                      }`}
                    >
                      <Lock className="w-3.5 h-3.5" />
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
      </div>

      {/* 3D ROOM PREVIEW & GALLERY MODAL */}
      {previewRoom && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-2xl w-full p-6 shadow-2xl space-y-5 relative">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div>
                <span className="text-[10px] font-bold text-sky-400 uppercase tracking-wider">{previewRoom.category}</span>
                <h3 className="text-xl font-bold text-white">{previewRoom.name} — Room Preview</h3>
              </div>
              <button
                type="button"
                onClick={() => setPreviewRoom(null)}
                className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* 3D / 360 View Toggle & Canvas */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                  <Compass className="w-4 h-4 text-sky-400" />
                  {is3DMode ? '360° Interactive Virtual Perspective' : 'High-Resolution Room Showcase'}
                </span>
                <button
                  type="button"
                  onClick={() => setIs3DMode(!is3DMode)}
                  className={`px-3 py-1 rounded-full text-xs font-bold transition ${
                    is3DMode ? 'bg-sky-500 text-white' : 'bg-slate-800 text-slate-300 hover:bg-slate-750'
                  }`}
                >
                  {is3DMode ? 'Switch to Gallery' : 'Switch to 360° View'}
                </button>
              </div>

              <div className="relative h-64 sm:h-80 rounded-2xl overflow-hidden border border-slate-800 bg-slate-950 flex items-center justify-center">
                <ImageWithFallback
                  src={previewRoom.imageUrls?.[0] || photos[0]}
                  alt={`${previewRoom.name} preview`}
                  containerClassName="w-full h-full"
                  className={`w-full h-full object-cover transition-transform duration-300 ${
                    is3DMode ? 'scale-110' : ''
                  }`}
                  style={is3DMode ? { transform: `scale(1.15) rotate(${rotationAngle}deg)` } : {}}
                />

                {is3DMode && (
                  <div className="absolute bottom-4 left-4 right-4 flex items-center justify-between p-3 rounded-xl bg-slate-950/80 backdrop-blur-md border border-slate-800 text-xs">
                    <span className="text-slate-300 font-medium">Drag or click to rotate view</span>
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => setRotationAngle((a) => a - 15)}
                        className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-white font-bold"
                      >
                        ↶ Rotate Left
                      </button>
                      <button
                        type="button"
                        onClick={() => setRotationAngle((a) => a + 15)}
                        className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-white font-bold"
                      >
                        ↷ Rotate Right
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Room Features Summary in Modal */}
            <div className="grid grid-cols-3 gap-3 text-center text-xs">
              <div className="p-3 bg-slate-950/80 border border-slate-800 rounded-xl">
                <span className="text-slate-500 block text-[10px]">Bed Configuration</span>
                <strong className="text-white">{previewRoom.bedType}</strong>
              </div>
              <div className="p-3 bg-slate-950/80 border border-slate-800 rounded-xl">
                <span className="text-slate-500 block text-[10px]">Room Size</span>
                <strong className="text-white">{previewRoom.sizeInSqFt || 320} sq. ft.</strong>
              </div>
              <div className="p-3 bg-slate-950/80 border border-slate-800 rounded-xl">
                <span className="text-slate-500 block text-[10px]">Nightly Price</span>
                <strong className="text-emerald-400">₹{previewRoom.totalNightlyRate || previewRoom.nightlyRate}</strong>
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={() => setPreviewRoom(null)}
                className="px-5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold text-xs transition"
              >
                Close Preview
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
