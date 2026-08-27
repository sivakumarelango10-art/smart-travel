import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  X,
  Calendar,
  Users,
  BedDouble,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  CreditCard,
  Sparkles,
  Tag,
  ArrowRight,
  Clock,
  Building2,
  Lock,
  Loader2,
  MapPin
} from 'lucide-react';
import { Hotel, RoomType, HotelPriceCalculateResponse, HotelBooking } from '../types/hotel';
import { hotelService } from '../services/hotelService';
import { useAuth } from '../context/AuthContext';

interface HotelReservationModalProps {
  hotel: Hotel;
  room: RoomType;
  checkInDate: string;
  checkOutDate: string;
  guestCount: number;
  roomCount: number;
  onClose: () => void;
}

export const HotelReservationModal: React.FC<HotelReservationModalProps> = ({
  hotel,
  room,
  checkInDate,
  checkOutDate,
  guestCount,
  roomCount,
  onClose,
}) => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [step, setStep] = useState<'DETAILS' | 'PAYMENT' | 'CONFIRMED'>('DETAILS');

  // Guest Details Form
  const [guestName, setGuestName] = useState(user?.fullName || '');
  const [guestEmail, setGuestEmail] = useState(user?.email || '');
  const [guestPhone, setGuestPhone] = useState(user?.phoneNumber || '+91 98765 43210');
  const [specialRequests, setSpecialRequests] = useState('');
  const [couponCode, setCouponCode] = useState('');
  const [couponApplied, setCouponApplied] = useState(false);

  // Price Calculation State
  const [priceData, setPriceData] = useState<HotelPriceCalculateResponse | null>(null);
  const [priceLoading, setPriceLoading] = useState(true);
  const [priceError, setPriceError] = useState<string | null>(null);

  // Booking Execution State
  const [submitting, setSubmitting] = useState(false);
  const [bookingResult, setBookingResult] = useState<HotelBooking | null>(null);
  const [bookingError, setBookingError] = useState<string | null>(null);

  // Payment Method
  const [paymentMethod, setPaymentMethod] = useState<'CARD' | 'UPI' | 'NET_BANKING'>('CARD');

  // Fetch authoritative server price breakdown
  const fetchPrice = async (promo?: string) => {
    try {
      setPriceLoading(true);
      setPriceError(null);
      const res = await hotelService.calculatePrice({
        hotelId: hotel.id,
        roomTypeId: room.id,
        checkInDate,
        checkOutDate,
        guestCount,
        roomCount,
        couponCode: promo || couponCode,
      });
      setPriceData(res);
    } catch (err: any) {
      setPriceError(err?.message || 'Failed to calculate stay price');
    } finally {
      setPriceLoading(false);
    }
  };

  useEffect(() => {
    fetchPrice();
  }, [hotel.id, room.id, checkInDate, checkOutDate, guestCount, roomCount]);

  const handleApplyCoupon = () => {
    if (!couponCode.trim()) return;
    fetchPrice(couponCode.trim());
    setCouponApplied(true);
  };

  const handleCompleteBooking = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!guestName.trim() || !guestEmail.trim()) {
      setBookingError('Please enter primary guest name and email address');
      return;
    }

    try {
      setSubmitting(true);
      setBookingError(null);

      const booking = await hotelService.createBooking({
        hotelId: hotel.id,
        roomTypeId: room.id,
        checkInDate,
        checkOutDate,
        guestCount,
        roomCount,
        primaryGuestName: guestName.trim(),
        primaryGuestEmail: guestEmail.trim(),
        primaryGuestPhone: guestPhone.trim(),
        specialRequests: specialRequests.trim() || undefined,
        couponCode: couponApplied ? couponCode.trim() : undefined,
        paymentMethod,
      });

      setBookingResult(booking);
      setStep('CONFIRMED');
    } catch (err: any) {
      setBookingError(err?.message || 'Failed to complete hotel reservation. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in overflow-y-auto">
      <div className="relative w-full max-w-2xl bg-[#14161F] border border-white/15 rounded-3xl shadow-2xl overflow-hidden my-8">
        {/* Modal Header */}
        <div className="p-6 bg-[#181A24] border-b border-white/10 flex items-center justify-between">
          <div>
            <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 text-[11px] font-bold">
              <Building2 className="w-3.5 h-3.5" />
              <span>
                {step === 'CONFIRMED' ? 'Reservation Confirmed' : step === 'PAYMENT' ? 'Step 2: Secure Payment' : 'Step 1: Reservation Details'}
              </span>
            </div>
            <h2 className="text-xl font-bold text-white mt-1">
              {step === 'CONFIRMED' ? 'Your Stay is Booked!' : `Reserve ${room.name}`}
            </h2>
            <p className="text-xs text-slate-400 flex items-center gap-1 mt-0.5">
              <MapPin className="w-3 h-3 text-amber-400" />
              <span>{hotel.name}, {hotel.address?.city}</span>
            </p>
          </div>

          {step !== 'CONFIRMED' && (
            <button
              onClick={onClose}
              className="p-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white transition"
            >
              <X className="w-5 h-5" />
            </button>
          )}
        </div>

        {/* Step 1: Stay Details & Guest Information */}
        {step === 'DETAILS' && (
          <form onSubmit={(e) => { e.preventDefault(); setStep('PAYMENT'); }} className="p-6 space-y-6">
            {/* Stay Summary Badge */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 rounded-2xl bg-[#1A1D28] border border-white/5 text-xs">
              <div>
                <div className="text-slate-400 text-[10px]">Check-in</div>
                <div className="text-white font-bold">{checkInDate}</div>
              </div>
              <div>
                <div className="text-slate-400 text-[10px]">Check-out</div>
                <div className="text-white font-bold">{checkOutDate}</div>
              </div>
              <div>
                <div className="text-slate-400 text-[10px]">Guests & Rooms</div>
                <div className="text-white font-bold">{guestCount} Guests, {roomCount} Room</div>
              </div>
              <div>
                <div className="text-slate-400 text-[10px]">Duration</div>
                <div className="text-amber-400 font-extrabold">{priceData?.nights || 1} Night(s)</div>
              </div>
            </div>

            {/* Guest Form */}
            <div className="space-y-4">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <Users className="w-4 h-4 text-amber-400" />
                <span>Primary Guest Information</span>
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs text-slate-300 font-semibold mb-1">Full Name *</label>
                  <input
                    type="text"
                    required
                    value={guestName}
                    onChange={(e) => setGuestName(e.target.value)}
                    placeholder="e.g. Jane Doe"
                    className="w-full px-3.5 py-2.5 rounded-xl bg-[#1E222E] border border-white/10 text-white text-xs focus:outline-none focus:border-amber-400"
                  />
                </div>
                <div>
                  <label className="block text-xs text-slate-300 font-semibold mb-1">Email Address *</label>
                  <input
                    type="email"
                    required
                    value={guestEmail}
                    onChange={(e) => setGuestEmail(e.target.value)}
                    placeholder="e.g. jane@example.com"
                    className="w-full px-3.5 py-2.5 rounded-xl bg-[#1E222E] border border-white/10 text-white text-xs focus:outline-none focus:border-amber-400"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs text-slate-300 font-semibold mb-1">Mobile Phone</label>
                  <input
                    type="tel"
                    value={guestPhone}
                    onChange={(e) => setGuestPhone(e.target.value)}
                    placeholder="+91 98765 43210"
                    className="w-full px-3.5 py-2.5 rounded-xl bg-[#1E222E] border border-white/10 text-white text-xs focus:outline-none focus:border-amber-400"
                  />
                </div>
                <div>
                  <label className="block text-xs text-slate-300 font-semibold mb-1">Special Requests (Optional)</label>
                  <input
                    type="text"
                    value={specialRequests}
                    onChange={(e) => setSpecialRequests(e.target.value)}
                    placeholder="e.g. High floor, Quiet room"
                    className="w-full px-3.5 py-2.5 rounded-xl bg-[#1E222E] border border-white/10 text-white text-xs focus:outline-none focus:border-amber-400"
                  />
                </div>
              </div>
            </div>

            {/* Coupon Code Bar */}
            <div className="p-3.5 rounded-2xl bg-[#181A24] border border-white/10 flex items-center gap-2">
              <Tag className="w-4 h-4 text-amber-400 flex-shrink-0" />
              <input
                type="text"
                value={couponCode}
                onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                placeholder="PROMO CODE (e.g. SMARTSTAY20)"
                className="flex-1 bg-transparent text-xs text-white uppercase focus:outline-none placeholder:text-slate-500 font-semibold"
              />
              <button
                type="button"
                onClick={handleApplyCoupon}
                className="px-3 py-1.5 rounded-xl bg-amber-400/20 hover:bg-amber-400/30 text-amber-300 text-xs font-bold transition"
              >
                Apply
              </button>
            </div>

            {/* Price Breakdown Preview */}
            <div className="p-4 rounded-2xl bg-[#1A1D28] border border-white/5 space-y-2 text-xs">
              <div className="flex justify-between text-slate-300">
                <span>Room Rate ({priceData?.nights || 1} night{priceData && priceData.nights > 1 ? 's' : ''} × {roomCount} room)</span>
                <span className="font-semibold">₹{priceData?.baseAmount?.toLocaleString() || '...'}</span>
              </div>
              <div className="flex justify-between text-slate-300">
                <span>Taxes & Service Fees (12% GST)</span>
                <span className="font-semibold">₹{priceData?.taxAmount?.toLocaleString() || '...'}</span>
              </div>
              {priceData && priceData.discountAmount > 0 && (
                <div className="flex justify-between text-emerald-400 font-semibold">
                  <span>Promo Discount Applied</span>
                  <span>-₹{priceData.discountAmount.toLocaleString()}</span>
                </div>
              )}
              <div className="pt-2 border-t border-white/10 flex justify-between items-center text-sm font-extrabold text-white">
                <span>Authoritative Total</span>
                <span className="text-amber-400 text-lg">
                  {priceLoading ? <Loader2 className="w-4 h-4 animate-spin inline" /> : `₹${priceData?.totalAmount?.toLocaleString() || '...'}`}
                </span>
              </div>
            </div>

            {/* Continue CTA */}
            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={onClose}
                className="px-5 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 text-xs font-bold transition"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={priceLoading || !guestName.trim() || !guestEmail.trim()}
                className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-extrabold text-xs shadow-glow-gold flex items-center gap-2 transition disabled:opacity-50"
              >
                <span>Proceed to Payment</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </form>
        )}

        {/* Step 2: Payment */}
        {step === 'PAYMENT' && (
          <form onSubmit={handleCompleteBooking} className="p-6 space-y-6">
            {bookingError && (
              <div className="p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                <span>{bookingError}</span>
              </div>
            )}

            {/* Payment Options */}
            <div className="space-y-3">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <CreditCard className="w-4 h-4 text-amber-400" />
                <span>Select Payment Method</span>
              </h3>

              <div className="grid grid-cols-3 gap-3">
                <button
                  type="button"
                  onClick={() => setPaymentMethod('CARD')}
                  className={`p-3.5 rounded-2xl border text-left transition flex flex-col justify-between h-24 ${
                    paymentMethod === 'CARD'
                      ? 'bg-amber-400/10 border-amber-400 text-white'
                      : 'bg-[#181A24] border-white/10 text-slate-400 hover:bg-[#1E222E]'
                  }`}
                >
                  <CreditCard className="w-5 h-5 text-amber-400" />
                  <div>
                    <div className="text-xs font-bold">Credit / Debit</div>
                    <div className="text-[10px] text-slate-500">Instant Confirm</div>
                  </div>
                </button>

                <button
                  type="button"
                  onClick={() => setPaymentMethod('UPI')}
                  className={`p-3.5 rounded-2xl border text-left transition flex flex-col justify-between h-24 ${
                    paymentMethod === 'UPI'
                      ? 'bg-amber-400/10 border-amber-400 text-white'
                      : 'bg-[#181A24] border-white/10 text-slate-400 hover:bg-[#1E222E]'
                  }`}
                >
                  <Sparkles className="w-5 h-5 text-amber-400" />
                  <div>
                    <div className="text-xs font-bold">UPI / QR</div>
                    <div className="text-[10px] text-slate-500">GooglePay/PhonePe</div>
                  </div>
                </button>

                <button
                  type="button"
                  onClick={() => setPaymentMethod('NET_BANKING')}
                  className={`p-3.5 rounded-2xl border text-left transition flex flex-col justify-between h-24 ${
                    paymentMethod === 'NET_BANKING'
                      ? 'bg-amber-400/10 border-amber-400 text-white'
                      : 'bg-[#181A24] border-white/10 text-slate-400 hover:bg-[#1E222E]'
                  }`}
                >
                  <ShieldCheck className="w-5 h-5 text-amber-400" />
                  <div>
                    <div className="text-xs font-bold">Net Banking</div>
                    <div className="text-[10px] text-slate-500">All Major Banks</div>
                  </div>
                </button>
              </div>
            </div>

            {/* Final Order Review Card */}
            <div className="p-4 rounded-2xl bg-[#1A1D28] border border-white/5 space-y-2 text-xs">
              <div className="flex justify-between text-slate-300">
                <span>Guest: <strong className="text-white">{guestName}</strong></span>
                <span>{checkInDate} to {checkOutDate}</span>
              </div>
              <div className="flex justify-between text-slate-300">
                <span>Cancellation Policy</span>
                <span className="text-emerald-400 font-semibold">100% Free Refund up to 7 days</span>
              </div>
              <div className="pt-2 border-t border-white/10 flex justify-between items-center text-sm font-extrabold text-white">
                <span>Total Amount to Pay</span>
                <span className="text-amber-400 text-xl font-black">
                  ₹{priceData?.totalAmount?.toLocaleString() || '...'}
                </span>
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center justify-between gap-3 pt-2">
              <button
                type="button"
                onClick={() => setStep('DETAILS')}
                className="px-5 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 text-xs font-bold transition"
              >
                Back to Details
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-extrabold text-xs shadow-glow-gold flex items-center gap-2 transition disabled:opacity-50"
              >
                {submitting ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>Processing Payment...</span>
                  </>
                ) : (
                  <>
                    <Lock className="w-4 h-4" />
                    <span>Pay ₹{priceData?.totalAmount?.toLocaleString()} & Confirm</span>
                  </>
                )}
              </button>
            </div>
          </form>
        )}

        {/* Step 3: Instant Confirmation */}
        {step === 'CONFIRMED' && bookingResult && (
          <div className="p-8 text-center space-y-6">
            <div className="w-16 h-16 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center mx-auto animate-bounce-short">
              <CheckCircle2 className="w-8 h-8" />
            </div>

            <div>
              <span className="text-xs font-bold uppercase tracking-wider text-emerald-400">Reservation Confirmed</span>
              <h3 className="text-2xl font-black text-white mt-1">Ready for Your Stay!</h3>
              <p className="text-xs text-slate-400 mt-1">
                A confirmation email has been dispatched to <strong>{bookingResult.primaryGuestEmail}</strong>.
              </p>
            </div>

            {/* Reference Badge */}
            <div className="p-4 rounded-2xl bg-[#1A1D28] border border-white/10 max-w-md mx-auto space-y-2">
              <div className="text-xs text-slate-400">Booking Reference (PNR)</div>
              <div className="text-2xl font-black text-amber-400 font-mono tracking-widest">
                {bookingResult.bookingReference}
              </div>
              <div className="text-[11px] text-slate-300 pt-2 border-t border-white/5 flex justify-between">
                <span>{bookingResult.hotelName}</span>
                <span>{bookingResult.checkInDate} — {bookingResult.checkOutDate}</span>
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
              <Link
                to="/my-bookings"
                className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-glow-gold hover:scale-105 transition-transform"
              >
                View in My Bookings
              </Link>
              <button
                type="button"
                onClick={onClose}
                className="px-5 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-200 font-bold text-xs border border-white/10 transition"
              >
                Close & Explore More
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
