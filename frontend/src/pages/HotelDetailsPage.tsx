import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Star, MapPin, Coffee, Check, ArrowLeft, BedDouble, Users, Maximize2, Lock, AlertCircle, CheckCircle2 } from 'lucide-react';
import { Hotel } from '../types/api';
import { hotelService } from '../services/hotelService';
import { StarRating } from '../components/StarRating';
import { ReviewSection } from '../components/ReviewSection';
import { recommendationService } from '../services/recommendationService';
import { useAuth } from '../context/AuthContext';

export const HotelDetailsPage: React.FC = () => {
  const { hotelId } = useParams<{ hotelId: string }>();
  const { isAuthenticated } = useAuth();

  const [hotel, setHotel] = useState<Hotel | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Room hold state
  const [holdingRoomId, setHoldingRoomId] = useState<string | null>(null);
  const [holdSuccess, setHoldSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!hotelId) return;

    setLoading(true);
    hotelService
      .getHotel(hotelId)
      .then((data) => {
        setHotel(data);
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

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 py-20 text-center text-slate-500">
        <div className="w-8 h-8 border-2 border-cyan-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
        Loading hotel details...
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

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-8">
        {/* Navigation Breadcrumb */}
        <div>
          <Link
            to="/hotels"
            className="inline-flex items-center gap-2 text-xs font-semibold text-slate-400 hover:text-cyan-400 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to All Hotels
          </Link>
        </div>

        {/* Hero Section */}
        <div className="relative overflow-hidden rounded-3xl bg-slate-900 border border-slate-800 p-8 shadow-2xl backdrop-blur-xl">
          <div className="flex flex-wrap items-start justify-between gap-6">
            <div className="max-w-2xl">
              <div className="flex items-center gap-2 mb-2">
                <span className="inline-flex items-center gap-1 text-xs font-bold text-amber-400 bg-amber-500/10 px-2.5 py-1 rounded-lg border border-amber-500/20">
                  <Star className="w-3.5 h-3.5 fill-amber-400" />
                  {hotel.starRating}-Star Luxury Property
                </span>
                {hotel.nearestAirportCode && (
                  <span className="text-xs font-mono text-cyan-300 bg-cyan-950/80 px-2.5 py-1 rounded-lg border border-cyan-500/30">
                    Proximity to {hotel.nearestAirportCode} Airport
                  </span>
                )}
              </div>

              <h1 className="text-3xl sm:text-4xl font-extrabold text-white">
                {hotel.name}
              </h1>

              <div className="flex items-center gap-2 text-sm text-slate-300 mt-2">
                <MapPin className="w-4 h-4 text-cyan-400 flex-shrink-0" />
                <span>
                  {hotel.address?.line1}, {hotel.address?.city}, {hotel.address?.state}, {hotel.address?.country}
                </span>
              </div>

              <p className="text-sm text-slate-400 mt-4 leading-relaxed">
                {hotel.description}
              </p>
            </div>

            {/* Right Card: Quick summary & rating */}
            <div className="p-5 bg-slate-950/80 border border-slate-800 rounded-2xl text-right min-w-[220px]">
              <div className="text-xs text-slate-400 mb-1">Guest Score</div>
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
                <span className="text-2xl font-black text-cyan-400">
                  ₹{hotel.baseNightlyRate?.toLocaleString()}
                </span>
                <span className="text-xs text-slate-400"> / night</span>
              </div>
            </div>
          </div>

          {/* Amenities Grid */}
          <div className="mt-8 pt-6 border-t border-slate-800/80">
            <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-3">
              Property Amenities
            </h3>
            <div className="flex flex-wrap gap-2">
              {hotel.amenities?.map((amenity) => (
                <span
                  key={amenity}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-800/80 border border-slate-700/60 rounded-xl text-xs font-medium text-slate-200"
                >
                  <Check className="w-3.5 h-3.5 text-cyan-400" />
                  {amenity}
                </span>
              ))}
            </div>
          </div>
        </div>

        {/* Room Hold Advisory */}
        {holdSuccess && (
          <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 rounded-2xl text-sm flex items-center gap-3">
            <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-emerald-400" />
            <span>{holdSuccess}</span>
          </div>
        )}

        {/* Room Selection Section */}
        <section>
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-2xl font-extrabold text-white flex items-center gap-2">
                <BedDouble className="w-6 h-6 text-cyan-400" />
                Select Your Room Category
              </h2>
              <p className="text-xs text-slate-400 mt-1">
                Atomic seat/room locking ensures no double-booking during your reservation.
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {hotel.roomTypes?.map((room) => {
              const isAvailable = room.availableRooms > 0;
              const isHoldingThis = holdingRoomId === room.id;

              return (
                <div
                  key={room.id}
                  className="bg-slate-900/60 border border-slate-800 hover:border-slate-700 rounded-2xl p-6 flex flex-col justify-between backdrop-blur-md transition-all duration-200 hover:-translate-y-1 hover:shadow-xl"
                >
                  <div>
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span className="text-[11px] font-semibold text-indigo-400 bg-indigo-500/10 px-2.5 py-0.5 rounded-lg border border-indigo-500/20">
                        {room.category}
                      </span>
                      <span
                        className={`text-xs font-semibold ${
                          room.availableRooms <= 3
                            ? 'text-rose-400 animate-pulse'
                            : 'text-emerald-400'
                        }`}
                      >
                        {room.availableRooms} {room.availableRooms === 1 ? 'room left' : 'rooms available'}
                      </span>
                    </div>

                    <h3 className="text-lg font-bold text-white mb-1">{room.name}</h3>
                    <p className="text-xs text-slate-400 mb-4 leading-relaxed">{room.description}</p>

                    {/* Room Specs */}
                    <div className="grid grid-cols-2 gap-2 text-xs text-slate-300 mb-4 p-3 bg-slate-800/40 rounded-xl border border-slate-800">
                      <div className="flex items-center gap-1.5">
                        <BedDouble className="w-3.5 h-3.5 text-slate-400" />
                        <span>{room.bedType} Bed</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Users className="w-3.5 h-3.5 text-slate-400" />
                        <span>Up to {room.maxOccupancy} Guests</span>
                      </div>
                      {room.sizeInSqFt > 0 && (
                        <div className="flex items-center gap-1.5">
                          <Maximize2 className="w-3.5 h-3.5 text-slate-400" />
                          <span>{room.sizeInSqFt} sq ft</span>
                        </div>
                      )}
                      <div className="flex items-center gap-1.5">
                        <Coffee className="w-3.5 h-3.5 text-slate-400" />
                        <span>{room.breakfastIncluded ? 'Breakfast Incl.' : 'Room Only'}</span>
                      </div>
                    </div>

                    {/* Features checklist */}
                    <div className="space-y-1.5 mb-4 text-xs text-slate-400">
                      {room.amenities?.map((a) => (
                        <div key={a} className="flex items-center gap-1.5">
                          <Check className="w-3 h-3 text-cyan-400" />
                          <span>{a}</span>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Pricing & Hold Button */}
                  <div className="pt-4 border-t border-slate-800 flex items-center justify-between gap-3">
                    <div>
                      <span className="text-[10px] text-slate-500 block">Nightly Rate + GST</span>
                      <span className="text-xl font-extrabold text-white">
                        ₹{room.totalNightlyRate?.toLocaleString()}
                      </span>
                      <span className="text-[10px] text-slate-400"> / night</span>
                    </div>

                    <button
                      type="button"
                      disabled={!isAvailable || isHoldingThis}
                      onClick={() => handleHoldRoom(room.id)}
                      className="flex items-center gap-1.5 px-4 py-2.5 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 disabled:opacity-40 disabled:cursor-not-allowed text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-cyan-500/20"
                    >
                      <Lock className="w-3.5 h-3.5" />
                      {isHoldingThis ? 'Holding...' : isAvailable ? 'Reserve Room' : 'Sold Out'}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        {/* Reviews Section */}
        <section className="pt-6">
          <ReviewSection
            targetType="HOTEL"
            targetId={hotel.id}
            targetName={hotel.name}
          />
        </section>
      </div>
    </div>
  );
};
