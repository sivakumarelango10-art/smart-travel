import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  BookmarkCheck,
  Plane,
  Building2,
  RotateCcw,
  ArrowRight,
  RefreshCw,
  XCircle,
  FileText,
  Clock,
  ShieldCheck,
  CheckCircle2,
  X,
  CreditCard,
  Calendar,
  Users,
  BedDouble,
  MapPin,
  Compass,
  AlertCircle,
  Loader2
} from 'lucide-react';
import { Booking, RefundDetails } from '../types/api';
import { HotelBooking, HotelRefundCalculation } from '../types/hotel';
import { bookingService } from '../services/bookingService';
import { hotelService } from '../services/hotelService';
import { paymentService } from '../services/paymentService';
import { BookingSkeleton } from '../components/BookingSkeleton';
import { notify } from '../utils/toast';
import { AirlineLogo } from '../components/AirlineLogo';

export const MyBookingsPage: React.FC = () => {
  const [bookingCategory, setBookingCategory] = useState<'FLIGHTS' | 'HOTELS'>('FLIGHTS');

  // Flight bookings state
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [activeTab, setActiveTab] = useState<'ALL' | 'UPCOMING' | 'COMPLETED' | 'CANCELLED'>('ALL');
  const [error, setError] = useState<string | null>(null);

  // Hotel bookings state
  const [hotelBookings, setHotelBookings] = useState<HotelBooking[]>([]);
  const [hotelLoading, setHotelLoading] = useState<boolean>(false);
  const [cancellingHotel, setCancellingHotel] = useState<HotelBooking | null>(null);
  const [hotelRefundPreview, setHotelRefundPreview] = useState<HotelRefundCalculation | null>(null);
  const [hotelCancelReason, setHotelCancelReason] = useState<string>('Personal schedule change');
  const [hotelCancelLoading, setHotelCancelLoading] = useState<boolean>(false);

  // Flight Cancellation & Refund Modal state
  const [cancellingBooking, setCancellingBooking] = useState<Booking | null>(null);
  const [cancelReason, setCancelReason] = useState<string>('Personal schedule change');
  const [cancelLoading, setCancelLoading] = useState<boolean>(false);
  const [refundInfo, setRefundInfo] = useState<RefundDetails | null>(null);
  const [viewingRefundBooking, setViewingRefundBooking] = useState<Booking | null>(null);

  const CANCELLATION_REASONS = [
    'Personal schedule change',
    'Medical emergency',
    'Business requirement changed',
    'Found a better fare',
    'Visa / travel document issue',
    'Other',
  ];

  const fetchFlightBookings = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await bookingService.getMyBookings(0, 50);
      if (res.success && res.data?.content) {
        setBookings(res.data.content);
      } else {
        setBookings([]);
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to load bookings');
    } finally {
      setLoading(false);
    }
  };

  const fetchHotelBookings = async () => {
    try {
      setHotelLoading(true);
      const res = await hotelService.getMyBookings(0, 50);
      setHotelBookings(res.content || []);
    } catch (err: any) {
      console.warn('Failed to load hotel bookings:', err);
    } finally {
      setHotelLoading(false);
    }
  };

  useEffect(() => {
    fetchFlightBookings();
    fetchHotelBookings();
  }, []);

  const handleCancelFlightBooking = async () => {
    if (!cancellingBooking) return;
    try {
      setCancelLoading(true);
      await bookingService.cancelBooking(cancellingBooking.id, cancelReason);
      setCancellingBooking(null);
      notify('Booking Cancelled', `Booking ${cancellingBooking.bookingReference} was cancelled successfully. Refund initiated.`, 'SUCCESS');
      await fetchFlightBookings();
    } catch (err: any) {
      notify('Cancellation Failed', err?.message || 'Please try again.', 'ERROR');
    } finally {
      setCancelLoading(false);
    }
  };

  const handleOpenHotelCancelModal = async (booking: HotelBooking) => {
    setCancellingHotel(booking);
    setHotelRefundPreview(null);
    try {
      const preview = await hotelService.getRefundPreview(booking.id);
      setHotelRefundPreview(preview);
    } catch {
      // preview calculation fallback
    }
  };

  const handleConfirmHotelCancellation = async () => {
    if (!cancellingHotel) return;
    try {
      setHotelCancelLoading(true);
      await hotelService.cancelBooking(cancellingHotel.id, hotelCancelReason);
      setCancellingHotel(null);
      notify('Hotel Booking Cancelled', `Hotel booking ${cancellingHotel.bookingReference} was cancelled.`, 'SUCCESS');
      await fetchHotelBookings();
    } catch (err: any) {
      notify('Hotel Cancellation Failed', err?.message || 'Please try again.', 'ERROR');
    } finally {
      setHotelCancelLoading(false);
    }
  };

  const handleViewFlightRefund = async (booking: Booking) => {
    setViewingRefundBooking(booking);
    try {
      const res = await paymentService.getRefundByBooking(booking.id);
      if (res.success && res.data) {
        setRefundInfo(res.data);
      } else {
        setRefundInfo(null);
      }
    } catch {
      setRefundInfo(null);
    }
  };

  const filteredFlightBookings = bookings.filter((b) => {
    const now = new Date().getTime();
    const depTime = new Date(b.departureTime).getTime();

    if (activeTab === 'UPCOMING') {
      return (b.status === 'CONFIRMED' || b.status === 'CHECKED_IN') && depTime >= now;
    }
    if (activeTab === 'COMPLETED') {
      return (b.status === 'CONFIRMED' || b.status === 'CHECKED_IN') && depTime < now;
    }
    if (activeTab === 'CANCELLED') {
      return b.status === 'CANCELLED';
    }
    return true;
  });

  const filteredHotelBookings = hotelBookings.filter((hb) => {
    const today = new Date().toISOString().split('T')[0];

    if (activeTab === 'UPCOMING') {
      return hb.status === 'CONFIRMED' && hb.checkInDate >= today;
    }
    if (activeTab === 'COMPLETED') {
      return hb.status === 'CONFIRMED' && hb.checkOutDate < today;
    }
    if (activeTab === 'CANCELLED') {
      return hb.status === 'CANCELLED' || hb.status === 'REFUNDED';
    }
    return true;
  });

  return (
    <div className="space-y-6 pb-16 max-w-5xl mx-auto">
      {/* 1. HEADER */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 text-xs font-bold shadow-glow-gold mb-2">
            <BookmarkCheck className="w-3.5 h-3.5" />
            <span>Manage Your Travel Reservations</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
            My Trips & Reservations
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Access boarding passes, hotel reservation vouchers, and manage cancellations with automated refund processing.
          </p>
        </div>

        <button
          type="button"
          onClick={() => { fetchFlightBookings(); fetchHotelBookings(); }}
          className="p-2.5 rounded-xl bg-[#14161F] hover:bg-[#181A22] text-slate-300 hover:text-amber-400 border border-white/10 text-xs font-bold flex items-center gap-2 transition cursor-pointer"
          title="Refresh Bookings"
        >
          <RefreshCw className="w-4 h-4 text-amber-400" />
          <span>Refresh</span>
        </button>
      </div>

      {/* 2. CATEGORY SWITCHER (FLIGHTS VS HOTELS) */}
      <div className="flex items-center gap-2 p-1.5 rounded-2xl bg-[#14161F] border border-white/10 w-fit">
        <button
          type="button"
          onClick={() => setBookingCategory('FLIGHTS')}
          className={`flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs font-extrabold transition cursor-pointer ${
            bookingCategory === 'FLIGHTS'
              ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Plane className="w-4 h-4" />
          <span>Flight Bookings ({bookings.length})</span>
        </button>
        <button
          type="button"
          onClick={() => setBookingCategory('HOTELS')}
          className={`flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs font-extrabold transition cursor-pointer ${
            bookingCategory === 'HOTELS'
              ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Building2 className="w-4 h-4" />
          <span>Hotels & Stays ({hotelBookings.length})</span>
        </button>
      </div>

      {/* 3. STATUS FILTER TABS */}
      <div className="flex items-center gap-2 border-b border-white/10 pb-3 overflow-x-auto scrollbar-none text-xs font-bold">
        {[
          {
            id: 'ALL',
            label: `All ${bookingCategory === 'FLIGHTS' ? 'Trips' : 'Stays'} (${
              bookingCategory === 'FLIGHTS' ? bookings.length : hotelBookings.length
            })`,
          },
          {
            id: 'UPCOMING',
            label: `Upcoming (${
              bookingCategory === 'FLIGHTS'
                ? bookings.filter(
                    (b) => (b.status === 'CONFIRMED' || b.status === 'CHECKED_IN') && new Date(b.departureTime).getTime() >= Date.now()
                  ).length
                : hotelBookings.filter((hb) => hb.status === 'CONFIRMED' && hb.checkInDate >= new Date().toISOString().split('T')[0]).length
            })`,
          },
          {
            id: 'COMPLETED',
            label: `Completed (${
              bookingCategory === 'FLIGHTS'
                ? bookings.filter(
                    (b) => (b.status === 'CONFIRMED' || b.status === 'CHECKED_IN') && new Date(b.departureTime).getTime() < Date.now()
                  ).length
                : hotelBookings.filter((hb) => hb.status === 'CONFIRMED' && hb.checkOutDate < new Date().toISOString().split('T')[0]).length
            })`,
          },
          {
            id: 'CANCELLED',
            label: `Cancelled (${
              bookingCategory === 'FLIGHTS'
                ? bookings.filter((b) => b.status === 'CANCELLED').length
                : hotelBookings.filter((hb) => hb.status === 'CANCELLED' || hb.status === 'REFUNDED').length
            })`,
          },
        ].map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => setActiveTab(tab.id as any)}
            className={`px-4 py-2 rounded-xl transition cursor-pointer ${
              activeTab === tab.id
                ? 'bg-[#1E222E] text-amber-400 border border-amber-400/30 shadow-glow-gold'
                : 'bg-[#14161F] text-slate-400 hover:text-white border border-white/10'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* 4. FLIGHTS CONTENT */}
      {bookingCategory === 'FLIGHTS' && (
        <>
          {loading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <BookingSkeleton key={i} />
              ))}
            </div>
          ) : error ? (
            <div className="p-12 rounded-2xl bg-[#14161F] border border-white/10 text-center space-y-4 shadow-xl">
              <div className="w-12 h-12 rounded-2xl bg-rose-500/15 text-rose-400 border border-rose-500/30 flex items-center justify-center mx-auto">
                <XCircle className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-white text-base">Unable to Load Bookings</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">{error}</p>
              <button
                onClick={fetchFlightBookings}
                className="px-4 py-2 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black text-xs font-extrabold transition shadow-glow-gold"
              >
                Try Again
              </button>
            </div>
          ) : filteredFlightBookings.length === 0 ? (
            <div className="p-16 rounded-2xl bg-[#14161F] border border-white/10 text-center space-y-4 shadow-xl">
              <div className="w-14 h-14 rounded-2xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center mx-auto shadow-glow-gold">
                <Plane className="w-7 h-7 transform -rotate-45" />
              </div>
              <div className="space-y-1">
                <h3 className="font-black text-white text-lg">No Flight Bookings Found</h3>
                <p className="text-xs text-slate-400 max-w-sm mx-auto">
                  Ready to explore? Book cheap domestic and international flights today.
                </p>
              </div>
              <Link
                to="/flights"
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-glow-gold transition"
              >
                <span>Search Flights Now</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredFlightBookings.map((b) => {
                const isConfirmed = b.status === 'CONFIRMED';
                const isCheckedIn = b.status === 'CHECKED_IN';

                return (
                  <div
                    key={b.id}
                    className="p-6 rounded-2xl bg-[#14161F] border border-white/10 hover:border-amber-500/30 hover:shadow-card-hover space-y-5 transition-all duration-300"
                  >
                    {/* Header Row */}
                    <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-white/10">
                      <div className="flex items-center gap-3">
                        <AirlineLogo airline={b.airline} size="md" />
                        <div>
                          <h3 className="font-bold text-white text-base leading-snug">{b.airline}</h3>
                          <div className="flex items-center gap-2 text-xs text-slate-400 mt-0.5">
                            <span className="font-mono text-amber-400 font-bold bg-[#181A22] px-1.5 py-0.2 rounded border border-white/10">
                              {b.flightNumber}
                            </span>
                            <span className="text-white/20">•</span>
                            <span>PNR: <strong className="font-mono text-amber-400 font-bold">{b.bookingReference}</strong></span>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center gap-2">
                        <span
                          className={`text-xs font-bold px-3 py-1 rounded-full ${
                            isConfirmed
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                              : isCheckedIn
                              ? 'bg-amber-400/10 text-amber-400 border border-amber-400/20'
                              : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                          }`}
                        >
                          {b.status}
                        </span>
                      </div>
                    </div>

                    {/* Timings & Route */}
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 items-center text-center sm:text-left">
                      <div>
                        <p className="text-2xl font-black text-white whitespace-nowrap">
                          {new Date(b.departureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}
                        </p>
                        <p className="text-sm font-black text-amber-400 mt-1">{b.departureAirport.code}</p>
                        <p className="text-xs text-slate-400">{b.departureAirport.city}</p>
                      </div>

                      <div className="flex flex-col items-center px-2">
                        <span className="text-xs text-slate-400 font-semibold">
                          {Math.floor(b.durationMinutes / 60)}h {b.durationMinutes % 60 > 0 ? `${b.durationMinutes % 60}m` : '00m'}
                        </span>
                        <div className="w-full flex items-center my-2">
                          <div className="h-0.5 w-full bg-white/10 relative">
                            <div className="absolute -top-1.5 left-1/2 transform -translate-x-1/2 w-3 h-3 rounded-full bg-amber-400 flex items-center justify-center shadow-glow-gold">
                              <Plane className="w-2 h-2 text-black transform rotate-45" />
                            </div>
                          </div>
                        </div>
                        <span className="text-[10px] font-bold text-amber-400 uppercase tracking-wider px-2 py-0.5 rounded-full bg-amber-400/10 border border-amber-400/20">
                          {b.cabinClass.replace('_', ' ')}
                        </span>
                      </div>

                      <div className="text-left sm:text-right">
                        <p className="text-2xl font-black text-white whitespace-nowrap">
                          {new Date(b.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}
                        </p>
                        <p className="text-sm font-black text-amber-400 mt-1">{b.arrivalAirport.code}</p>
                        <p className="text-xs text-slate-400">{b.arrivalAirport.city}</p>
                      </div>
                    </div>

                    {/* Action Footer */}
                    <div className="pt-4 border-t border-white/10 flex flex-wrap items-center justify-between gap-3">
                      <div>
                        <span className="text-[10px] text-slate-400 block font-medium">Total Fare Paid</span>
                        <span className="text-2xl font-black text-amber-400 tracking-tight">
                          ₹{b.totalAmount.toLocaleString('en-IN')}
                        </span>
                      </div>

                      <div className="flex flex-wrap items-center gap-2">
                        {b.ticketId && (
                          <Link
                            to={`/ticket/${b.id}`}
                            className="px-3.5 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-300 text-xs font-bold flex items-center gap-1.5 border border-white/10 transition"
                          >
                            <FileText className="w-3.5 h-3.5 text-amber-400" />
                            <span>View Ticket</span>
                          </Link>
                        )}
                        {b.status === 'CONFIRMED' && (
                          <button
                            type="button"
                            onClick={() => setCancellingBooking(b)}
                            className="px-3.5 py-2 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 text-xs font-bold border border-rose-500/20 transition cursor-pointer"
                          >
                            Cancel Flight
                          </button>
                        )}
                        {b.status === 'CANCELLED' && (
                          <button
                            type="button"
                            onClick={() => handleViewFlightRefund(b)}
                            className="px-3.5 py-2 rounded-xl bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 text-xs font-bold border border-emerald-500/20 flex items-center gap-1.5 transition cursor-pointer"
                          >
                            <RotateCcw className="w-3.5 h-3.5" />
                            <span>View Refund</span>
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}

      {/* 5. HOTELS & STAYS CONTENT */}
      {bookingCategory === 'HOTELS' && (
        <>
          {hotelLoading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <BookingSkeleton key={i} />
              ))}
            </div>
          ) : filteredHotelBookings.length === 0 ? (
            <div className="p-16 rounded-2xl bg-[#14161F] border border-white/10 text-center space-y-4 shadow-xl">
              <div className="w-14 h-14 rounded-2xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center mx-auto shadow-glow-gold">
                <Building2 className="w-7 h-7" />
              </div>
              <div className="space-y-1">
                <h3 className="font-black text-white text-lg">No Hotel Reservations Found</h3>
                <p className="text-xs text-slate-400 max-w-sm mx-auto">
                  Explore luxury palaces, beachside resorts, and city hotels with 360° virtual tours.
                </p>
              </div>
              <Link
                to="/hotels"
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-glow-gold transition"
              >
                <span>Browse Hotels & Stays</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredHotelBookings.map((hb) => {
                const isConfirmed = hb.status === 'CONFIRMED';
                const isCancelled = hb.status === 'CANCELLED' || hb.status === 'REFUNDED';

                return (
                  <div
                    key={hb.id}
                    className="p-6 rounded-2xl bg-[#14161F] border border-white/10 hover:border-amber-500/30 hover:shadow-card-hover space-y-5 transition-all duration-300"
                  >
                    {/* Hotel Header Row */}
                    <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-white/10">
                      <div className="flex items-center gap-3">
                        <div className="w-12 h-12 rounded-xl bg-[#181A24] border border-white/10 overflow-hidden flex-shrink-0 flex items-center justify-center">
                          {hb.hotelImageUrl ? (
                            <img src={hb.hotelImageUrl} alt={hb.hotelName} className="w-full h-full object-cover" />
                          ) : (
                            <Building2 className="w-6 h-6 text-amber-400" />
                          )}
                        </div>
                        <div>
                          <h3 className="font-bold text-white text-base leading-snug">{hb.hotelName}</h3>
                          <div className="flex items-center gap-2 text-xs text-slate-400 mt-0.5">
                            <span className="text-amber-400 font-semibold">{hb.hotelCity}</span>
                            <span className="text-white/20">•</span>
                            <span>PNR: <strong className="font-mono text-amber-400 font-bold">{hb.bookingReference}</strong></span>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center gap-2">
                        <span
                          className={`text-xs font-bold px-3 py-1 rounded-full ${
                            isConfirmed
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                              : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                          }`}
                        >
                          {hb.status}
                        </span>
                      </div>
                    </div>

                    {/* Room & Stay Details */}
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 items-center text-center sm:text-left">
                      <div>
                        <span className="text-[10px] uppercase font-bold text-amber-400 tracking-wider">Check-in</span>
                        <p className="text-lg font-black text-white mt-0.5">{hb.checkInDate}</p>
                        <p className="text-xs text-slate-400">Primary Guest: {hb.primaryGuestName}</p>
                      </div>

                      <div className="flex flex-col items-center px-2">
                        <span className="text-xs text-slate-400 font-semibold">
                          {hb.nights} Night{hb.nights > 1 ? 's' : ''} Stay
                        </span>
                        <div className="w-full flex items-center my-2">
                          <div className="h-0.5 w-full bg-white/10 relative">
                            <div className="absolute -top-1.5 left-1/2 transform -translate-x-1/2 w-3 h-3 rounded-full bg-amber-400 flex items-center justify-center shadow-glow-gold">
                              <Building2 className="w-2 h-2 text-black" />
                            </div>
                          </div>
                        </div>
                        <span className="text-[10px] font-bold text-amber-400 uppercase tracking-wider px-2 py-0.5 rounded-full bg-amber-400/10 border border-amber-400/20">
                          {hb.roomTypeName || hb.roomCategory}
                        </span>
                      </div>

                      <div className="text-left sm:text-right">
                        <span className="text-[10px] uppercase font-bold text-amber-400 tracking-wider">Check-out</span>
                        <p className="text-lg font-black text-white mt-0.5">{hb.checkOutDate}</p>
                        <p className="text-xs text-slate-400">{hb.guestCount} Guests, {hb.roomCount} Room</p>
                      </div>
                    </div>

                    {/* Action Footer */}
                    <div className="pt-4 border-t border-white/10 flex flex-wrap items-center justify-between gap-3">
                      <div>
                        <span className="text-[10px] text-slate-400 block font-medium">Total Paid (Taxes Included)</span>
                        <span className="text-2xl font-black text-amber-400 tracking-tight">
                          ₹{hb.totalAmount.toLocaleString('en-IN')}
                        </span>
                      </div>

                      <div className="flex flex-wrap items-center gap-2">
                        <Link
                          to={`/hotels/${hb.hotelId}`}
                          className="px-3.5 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-300 text-xs font-bold flex items-center gap-1.5 border border-white/10 transition"
                        >
                          <Compass className="w-3.5 h-3.5 text-amber-400" />
                          <span>View Property / 360°</span>
                        </Link>
                        {isConfirmed && (
                          <button
                            type="button"
                            onClick={() => handleOpenHotelCancelModal(hb)}
                            className="px-3.5 py-2 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 text-xs font-bold border border-rose-500/20 transition cursor-pointer"
                          >
                            Cancel Reservation
                          </button>
                        )}
                        {isCancelled && hb.refundAmount && hb.refundAmount > 0 && (
                          <div className="px-3 py-1.5 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-xs font-bold">
                            Refunded ₹{hb.refundAmount.toLocaleString()}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}

      {/* Flight Cancellation Modal */}
      {cancellingBooking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="w-full max-w-md rounded-3xl bg-[#14161F] border border-white/10 p-6 shadow-2xl space-y-5">
            <div className="flex items-center justify-between border-b border-white/10 pb-3">
              <div className="flex items-center gap-2">
                <AlertCircle className="w-5 h-5 text-rose-400" />
                <h3 className="text-base font-bold text-white">Confirm Flight Cancellation</h3>
              </div>
              <button
                type="button"
                onClick={() => setCancellingBooking(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-white cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-slate-300">
              Are you sure you want to cancel booking <strong className="text-amber-400 font-mono">{cancellingBooking.bookingReference}</strong> ({cancellingBooking.airline} {cancellingBooking.flightNumber})?
            </p>

            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5">Reason for Cancellation</label>
              <select
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-[#181A22] border border-white/10 text-white text-xs focus:outline-none focus:border-amber-400"
              >
                {CANCELLATION_REASONS.map((r) => (
                  <option key={r} value={r}>{r}</option>
                ))}
              </select>
            </div>

            <div className="flex items-center gap-3 pt-2">
              <button
                type="button"
                disabled={cancelLoading}
                onClick={handleCancelFlightBooking}
                className="flex-1 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs shadow-sm transition disabled:opacity-50 cursor-pointer"
              >
                {cancelLoading ? 'Cancelling...' : 'Confirm Cancellation'}
              </button>

              <button
                type="button"
                onClick={() => setCancellingBooking(null)}
                className="px-4 py-2.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-300 text-xs font-bold transition border border-white/10 cursor-pointer"
              >
                Keep Booking
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Hotel Cancellation Modal */}
      {cancellingHotel && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="w-full max-w-md rounded-3xl bg-[#14161F] border border-white/10 p-6 shadow-2xl space-y-5">
            <div className="flex items-center justify-between border-b border-white/10 pb-3">
              <div className="flex items-center gap-2">
                <AlertCircle className="w-5 h-5 text-rose-400" />
                <h3 className="text-base font-bold text-white">Cancel Hotel Reservation</h3>
              </div>
              <button
                type="button"
                onClick={() => setCancellingHotel(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-white cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-3.5 rounded-2xl bg-[#181A24] border border-white/10 space-y-1.5 text-xs">
              <div className="flex justify-between">
                <span className="text-slate-400">Property:</span>
                <span className="font-bold text-white">{cancellingHotel.hotelName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Reference:</span>
                <span className="font-mono font-bold text-amber-400">{cancellingHotel.bookingReference}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Total Paid:</span>
                <span className="font-bold text-white">₹{cancellingHotel.totalAmount.toLocaleString()}</span>
              </div>
              {hotelRefundPreview && (
                <div className="pt-2 border-t border-white/10 flex justify-between items-center">
                  <span className="text-slate-300">Refund Amount ({hotelRefundPreview.refundPercentage}%):</span>
                  <span className="text-emerald-400 font-extrabold text-sm">₹{hotelRefundPreview.refundAmount.toLocaleString()}</span>
                </div>
              )}
            </div>

            {hotelRefundPreview && (
              <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 flex-shrink-0" />
                <span>{hotelRefundPreview.policyApplied}</span>
              </div>
            )}

            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5">Reason for Cancellation</label>
              <select
                value={hotelCancelReason}
                onChange={(e) => setHotelCancelReason(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-[#181A22] border border-white/10 text-white text-xs focus:outline-none focus:border-amber-400"
              >
                {CANCELLATION_REASONS.map((r) => (
                  <option key={r} value={r}>{r}</option>
                ))}
              </select>
            </div>

            <div className="flex items-center gap-3 pt-2">
              <button
                type="button"
                disabled={hotelCancelLoading}
                onClick={handleConfirmHotelCancellation}
                className="flex-1 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs shadow-sm transition disabled:opacity-50 cursor-pointer"
              >
                {hotelCancelLoading ? 'Cancelling...' : 'Confirm Cancellation'}
              </button>

              <button
                type="button"
                onClick={() => setCancellingHotel(null)}
                className="px-4 py-2.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-300 text-xs font-bold transition border border-white/10 cursor-pointer"
              >
                Keep Stay
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Flight Refund Tracker Modal */}
      {viewingRefundBooking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="w-full max-w-md rounded-3xl bg-[#14161F] border border-white/10 p-6 shadow-2xl space-y-5">
            <div className="flex items-center justify-between border-b border-white/10 pb-3">
              <div className="flex items-center gap-2">
                <RotateCcw className="w-4 h-4 text-emerald-400" />
                <h3 className="text-base font-bold text-white">Refund Status & Timeline</h3>
              </div>
              <button
                type="button"
                onClick={() => setViewingRefundBooking(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-white cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3">
              <div className="p-3.5 rounded-xl bg-[#181A22] border border-white/10 space-y-1.5 text-xs">
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">Booking Reference:</span>
                  <span className="font-mono font-bold text-amber-400">{viewingRefundBooking.bookingReference}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">Total Booking Amount:</span>
                  <span className="font-bold text-white">₹{viewingRefundBooking.totalAmount.toLocaleString()}</span>
                </div>
                {refundInfo && (
                  <>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Refund Amount:</span>
                      <span className="font-black text-emerald-400">₹{refundInfo.amount.toLocaleString()}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Refund Status:</span>
                      <span className="font-bold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                        {refundInfo.status}
                      </span>
                    </div>
                  </>
                )}
              </div>
            </div>

            <div className="pt-2">
              <button
                type="button"
                onClick={() => setViewingRefundBooking(null)}
                className="w-full py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black text-xs font-black shadow-glow-gold cursor-pointer"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
