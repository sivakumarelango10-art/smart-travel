import React, { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate, useLocation, Link } from 'react-router-dom';
import {
  Armchair,
  Users,
  CreditCard,
  CheckCircle2,
  ChevronRight,
  ChevronLeft,
  AlertCircle,
  Plane,
  ShieldCheck,
  Lock,
  Tag,
  Sparkles,
  Percent,
  Copy,
  Check,
  X
} from 'lucide-react';
import { Flight, CabinClass, Passenger, Seat, Booking, PriceFreeze } from '../types/api';
import { flightService } from '../services/flightService';
import { seatService } from '../services/seatService';
import { bookingService } from '../services/bookingService';
import { pricingService } from '../services/pricingService';
import { useAuth } from '../context/AuthContext';
import { SeatMap } from '../components/SeatMap';
import { PassengerForm } from '../components/PassengerForm';
import { FareSummaryCard, AppliedOffer } from '../components/FareSummaryCard';
import { PaymentModal } from '../components/PaymentModal';
import { AirlineLogo } from '../components/AirlineLogo';
import { AircraftBadge } from '../components/AircraftBadge';

interface FlightOfferCoupon {
  code: string;
  title: string;
  description: string;
  discountLabel: string;
  minSpend: number;
  badge: string;
  calcDiscount: (subtotal: number) => number;
}

const VERIFIED_FLIGHT_OFFERS: FlightOfferCoupon[] = [
  {
    code: 'SMARTFLY25',
    title: 'Domestic & International Special',
    description: 'Flat ₹1,500 instant discount on flights with booking value ₹4,999 and above.',
    discountLabel: 'FLAT ₹1,500 OFF',
    minSpend: 4999,
    calcDiscount: () => 1500,
    badge: 'Most Popular',
  },
  {
    code: 'FLYSMART10',
    title: 'Smart Traveler Instant Savings',
    description: 'Get 10% instant discount (up to ₹1,000) on bookings above ₹2,500.',
    discountLabel: '10% INSTANT OFF',
    minSpend: 2500,
    calcDiscount: (subtotal) => Math.min(1000, Math.round(subtotal * 0.1)),
    badge: 'Best Value',
  },
  {
    code: 'DOMESTIC500',
    title: 'Domestic Express Discount',
    description: 'Flat ₹500 off on all domestic non-stop routes above ₹2,000.',
    discountLabel: 'FLAT ₹500 OFF',
    minSpend: 2000,
    calcDiscount: () => 500,
    badge: 'Quick Saver',
  },
  {
    code: 'FESTIVE20',
    title: 'Festive Season Bonanza',
    description: '20% off (up to ₹2,000) on flights above ₹5,000 for peak holiday travel.',
    discountLabel: '20% HOLIDAY OFF',
    minSpend: 5000,
    calcDiscount: (subtotal) => Math.min(2000, Math.round(subtotal * 0.2)),
    badge: 'Festive Perk',
  },
  {
    code: 'FIRSTTRIP',
    title: 'First Flight Traveler Welcome',
    description: 'Flat ₹750 off on your flight reservation above ₹3,000.',
    discountLabel: 'FLAT ₹750 OFF',
    minSpend: 3000,
    calcDiscount: () => 750,
    badge: 'New User',
  },
];

export const BookingPage: React.FC = () => {
  const { flightId } = useParams<{ flightId: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user, isAuthenticated } = useAuth();

  const cabinClass = (searchParams.get('cabinClass') as CabinClass) || 'ECONOMY';
  const passengerCount = parseInt(searchParams.get('passengers') || '1', 10);
  const initialFreezeId = searchParams.get('priceFreezeId') || searchParams.get('freezeId');

  // Enforce authentication upfront before user starts the booking process
  useEffect(() => {
    if (!isAuthenticated) {
      const returnUrl = `${location.pathname}${location.search}`;
      navigate(`/login?redirect=${encodeURIComponent(returnUrl)}`, { replace: true });
    }
  }, [isAuthenticated, location.pathname, location.search, navigate]);

  // Steps: 1: Seats, 2: Passengers, 3: Review & Book
  const [step, setStep] = useState<number>(1);

  // Instant 0ms hydration from location state or in-memory flight cache
  const passedFlight = (location.state as any)?.flight as Flight | undefined;
  const initialFlight = passedFlight || (flightId ? flightService.getCachedFlightById(flightId) : null);

  const [flight, setFlight] = useState<Flight | null>(initialFlight);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
  const [passengers, setPassengers] = useState<Passenger[]>(() =>
    Array.from({ length: passengerCount }, (_, i) => {
      const parts = user?.fullName ? user.fullName.trim().split(/\s+/) : [];
      return {
        title: 'Mr',
        firstName: i === 0 && parts.length > 0 ? parts[0] : '',
        lastName: i === 0 && parts.length > 1 ? parts.slice(1).join(' ') : i === 0 && parts.length === 1 ? parts[0] : '',
        dateOfBirth: '1995-01-01',
        gender: 'MALE',
        nationality: 'Indian',
        passportNumber: '',
      };
    })
  );

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState<boolean>(!initialFlight);
  const [seatsLoading, setSeatsLoading] = useState<boolean>(true);
  const [bookingLoading, setBookingLoading] = useState<boolean>(false);
  const [bookingError, setBookingError] = useState<string | null>(null);

  // Price Freeze state
  const [appliedFreeze, setAppliedFreeze] = useState<PriceFreeze | null>(null);
  const [userFreezes, setUserFreezes] = useState<PriceFreeze[]>([]);

  // Post-booking & Payment state
  const [createdBooking, setCreatedBooking] = useState<Booking | null>(null);
  const [showPaymentModal, setShowPaymentModal] = useState<boolean>(false);

  // Deals & Offers State
  const [appliedOffer, setAppliedOffer] = useState<AppliedOffer | null>(null);
  const [couponInput, setCouponInput] = useState<string>('');
  const [couponError, setCouponError] = useState<string | null>(null);
  const [couponSuccess, setCouponSuccess] = useState<string | null>(null);
  const [copiedCode, setCopiedCode] = useState<string | null>(null);

  const calculateCurrentSubtotal = (): number => {
    if (!flight) return 0;
    const cabinInv =
      flight.cabinInventories?.find((c) => c.cabinClass === cabinClass) ||
      flight.cabinInventories?.[0];
    const unitPrice = cabinInv ? cabinInv.totalPrice : flight.basePrice;
    const base = unitPrice * passengerCount;
    const seatSelectionTotal = selectedSeats.reduce((acc, seatNum) => {
      const s = seats.find((seat) => seat.seatNumber === seatNum);
      return acc + (s?.price || 0);
    }, 0);
    return base + seatSelectionTotal;
  };

  const handleApplyCoupon = (codeToApply?: string) => {
    const rawCode = (codeToApply || couponInput).trim().toUpperCase();
    setCouponError(null);
    setCouponSuccess(null);

    if (!rawCode) {
      setCouponError('Please enter a coupon code.');
      return;
    }

    const offer = VERIFIED_FLIGHT_OFFERS.find((o) => o.code === rawCode);
    if (!offer) {
      setCouponError(`Coupon code "${rawCode}" is not recognized.`);
      return;
    }

    const subtotal = calculateCurrentSubtotal();
    if (subtotal < offer.minSpend) {
      setCouponError(
        `Minimum booking value of ₹${offer.minSpend.toLocaleString('en-IN')} required for ${offer.code} (Current: ₹${subtotal.toLocaleString('en-IN')}).`
      );
      return;
    }

    const discount = offer.calcDiscount(subtotal);
    setAppliedOffer({
      code: offer.code,
      discountAmount: discount,
      title: offer.title,
    });
    setCouponInput(offer.code);
    setCouponSuccess(`Coupon ${offer.code} applied successfully! You saved ₹${discount.toLocaleString('en-IN')}.`);
  };

  const handleRemoveCoupon = () => {
    setAppliedOffer(null);
    setCouponInput('');
    setCouponError(null);
    setCouponSuccess(null);
  };

  const handleCopyCode = (code: string) => {
    if (navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(code);
    }
    setCopiedCode(code);
    setTimeout(() => setCopiedCode(null), 2000);
  };

  // Fetch Flight & Seat Map with background hydration
  const loadFlightAndSeats = async () => {
    if (!flightId) return;
    try {
      if (!flight) setLoading(true);
      setSeatsLoading(true);
      setBookingError(null);

      const [flightRes, seatsRes] = await Promise.all([
        flightService.getFlightById(flightId).catch((err) => {
          const cachedFallback = flightService.getCachedFlightById(flightId);
          if (cachedFallback) {
            return {
              success: true,
              message: 'Flight recovered from cache',
              data: cachedFallback,
              timestamp: new Date().toISOString(),
            };
          }
          throw err;
        }),
        seatService.getSeatMap(flightId).catch(() => null),
      ]);

      if (flightRes && flightRes.data) {
        setFlight(flightRes.data);
      } else if (!flight) {
        const cachedFallback = flightService.getCachedFlightById(flightId);
        if (cachedFallback) {
          setFlight(cachedFallback);
        } else {
          throw new Error('Flight details could not be loaded.');
        }
      }

      let seatList: Seat[] = [];
      if (seatsRes && seatsRes.data) {
        const raw = seatsRes.data;
        seatList = Array.isArray(raw)
          ? raw
          : Array.isArray(raw.seats)
          ? raw.seats
          : [];
      }

      // If seat map was empty, try direct seats API as fallback
      if (seatList.length === 0) {
        try {
          const fallbackSeats = await seatService.getSeats(flightId, cabinClass);
          if (fallbackSeats && Array.isArray(fallbackSeats.data)) {
            seatList = fallbackSeats.data;
          }
        } catch {
          // ignore
        }
      }

      setSeats(seatList);

      // Auto-assign available seats matching selected cabin class if none selected yet
      if (selectedSeats.length === 0 && seatList.length > 0) {
        const matchingCabinSeats = seatList.filter(
          (s) => s.status === 'AVAILABLE' && (!cabinClass || s.cabinClass === cabinClass)
        );
        const available = (matchingCabinSeats.length > 0 ? matchingCabinSeats : seatList.filter((s) => s.status === 'AVAILABLE')).slice(0, passengerCount);
        if (available.length > 0) {
          setSelectedSeats(available.map((s) => s.seatNumber));
        }
      }
    } catch (err: any) {
      const recovered = flightService.getCachedFlightById(flightId);
      if (recovered) {
        setFlight(recovered);
        setBookingError(null);
      } else if (!flight) {
        setBookingError(err.message || 'Failed to load flight or seat information.');
      }
    } finally {
      setLoading(false);
      setSeatsLoading(false);
    }
  };

  useEffect(() => {
    loadFlightAndSeats();
  }, [flightId]);

  // Load user's active price freezes if authenticated
  useEffect(() => {
    if (isAuthenticated && flightId) {
      pricingService
        .getUserPriceFreezes()
        .then((res: PriceFreeze[]) => {
          if (Array.isArray(res)) {
            const active = res.filter(
              (f: PriceFreeze) => f.flightId === flightId && f.status === 'ACTIVE' && new Date(f.expiresAt) > new Date()
            );
            setUserFreezes(active);
            if (initialFreezeId) {
              const matched = active.find((f: PriceFreeze) => f.id === initialFreezeId);
              if (matched) setAppliedFreeze(matched);
            }
          }
        })
        .catch(() => {});
    }
  }, [isAuthenticated, flightId, initialFreezeId]);

  const handlePassengerChange = (index: number, updated: Passenger) => {
    const next = [...passengers];
    next[index] = updated;
    setPassengers(next);

    // Clear field-level error
    const nextErrors = { ...errors };
    delete nextErrors[`firstName_${index}`];
    delete nextErrors[`lastName_${index}`];
    setErrors(nextErrors);
  };

  const validatePassengerDetails = (): boolean => {
    const newErrors: Record<string, string> = {};
    passengers.forEach((pax, idx) => {
      if (!pax.firstName || pax.firstName.trim().length === 0) {
        newErrors[`firstName_${idx}`] = 'First name is required';
      }
      if (!pax.lastName || pax.lastName.trim().length === 0) {
        newErrors[`lastName_${idx}`] = 'Last name is required';
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleNextStep = () => {
    setBookingError(null);
    if (step === 1) {
      if (selectedSeats.length < passengerCount) {
        setBookingError(`Please select ${passengerCount} seat(s) on the aircraft before continuing.`);
        return;
      }
      setStep(2);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else if (step === 2) {
      if (!validatePassengerDetails()) {
        setBookingError('Please resolve passenger detail errors before continuing.');
        return;
      }
      setStep(3);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handleCreateBookingAndPay = async () => {
    if (!flight) return;
    setBookingLoading(true);
    setBookingError(null);

    try {
      const payloadPassengers = passengers.map((p, i) => ({
        ...p,
        seatNumber: selectedSeats[i],
      }));

      const res = await bookingService.createBooking({
        flightId: flight.id,
        cabinClass,
        passengers: payloadPassengers,
        priceFreezeId: appliedFreeze ? appliedFreeze.id : undefined,
        couponCode: appliedOffer ? appliedOffer.code : undefined,
        discountAmount: appliedOffer ? appliedOffer.discountAmount : undefined,
      });

      if (res && res.data) {
        setCreatedBooking(res.data);
        setShowPaymentModal(true);
      } else {
        throw new Error('Failed to create booking reservation.');
      }
    } catch (err: any) {
      setBookingError(err.message || 'Booking reservation failed. Please review your details.');
      // Only return to step 1 if there is a real seat occupancy collision (HTTP 409 conflict)
      if (err.status === 409 || err.response?.status === 409 || err.message?.toLowerCase().includes('already booked')) {
        setStep(1);
        loadFlightAndSeats();
      }
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading && !flight) {
    return (
      <div className="space-y-6 pb-16 max-w-7xl mx-auto animate-pulse">
        <div className="rounded-2xl bg-[#14161F] p-6 h-28 border border-white/10 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-white/5" />
          <div className="space-y-2 flex-1">
            <div className="h-5 w-64 bg-white/10 rounded" />
            <div className="h-3 w-40 bg-white/5 rounded" />
          </div>
        </div>
        <div className="rounded-2xl bg-[#14161F] p-5 h-16 border border-white/10" />
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          <div className="lg:col-span-8 rounded-2xl bg-[#14161F] h-[480px] border border-white/10 p-6" />
          <div className="lg:col-span-4 rounded-2xl bg-[#14161F] h-96 border border-white/10 p-6" />
        </div>
      </div>
    );
  }

  if (!flight) {
    return (
      <div className="py-24 max-w-md mx-auto text-center space-y-4 px-4">
        <div className="w-14 h-14 rounded-2xl bg-rose-500/15 text-rose-400 flex items-center justify-center mx-auto border border-rose-500/30">
          <AlertCircle className="w-7 h-7" />
        </div>
        <h2 className="text-lg font-bold text-white">Flight Details Unavailable</h2>
        <p className="text-xs text-slate-400">
          {bookingError || 'The requested flight could not be found or has expired.'}
        </p>
        <div className="flex justify-center gap-3 pt-2">
          <button
            onClick={loadFlightAndSeats}
            className="px-4 py-2 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-glow-gold transition"
          >
            Retry Loading
          </button>
          <button
            onClick={() => navigate('/flights')}
            className="px-4 py-2 rounded-xl bg-[#14161F] text-slate-300 font-bold text-xs border border-white/10 transition"
          >
            Back to Flight Search
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 pb-16 max-w-7xl mx-auto">
      {/* 1. FLIGHT SUMMARY BANNER */}
      <section className="rounded-2xl bg-[#14161F] text-white p-5 sm:p-6 shadow-xl border border-white/10 flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <AirlineLogo airline={flight.airline} airlineCode={flight.airlineCode} size="lg" />
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-lg sm:text-xl font-black text-white tracking-tight">
                {flight.departureAirport.city} ({flight.departureAirport.code}) ➔ {flight.arrivalAirport.city} ({flight.arrivalAirport.code})
              </h1>
            </div>
            <div className="flex items-center gap-3 text-xs text-slate-400 mt-1.5 flex-wrap">
              <span className="font-bold text-white">{flight.airline}</span>
              <span className="font-mono text-amber-400 font-bold bg-[#12131A] px-1.5 py-0.5 rounded border border-white/10 text-[11px]">
                {flight.flightNumber}
              </span>
              <AircraftBadge aircraftModel={flight.aircraftModel} />
              <span className="text-amber-400 font-bold uppercase">{cabinClass.replace('_', ' ')}</span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-emerald-400 bg-emerald-500/10 px-3 py-1 rounded-full border border-emerald-500/20 flex items-center gap-1.5 shadow-glow-emerald">
            <ShieldCheck className="w-4 h-4" />
            <span>Instant Lock Engine</span>
          </span>
        </div>
      </section>

      {/* 2. STEPPER PROGRESS BAR */}
      <div className="p-4 sm:p-5 rounded-2xl bg-[#14161F] border border-white/10 shadow-xl flex items-center justify-between">
        {[
          { num: 1, label: 'Seat Selection', icon: Armchair },
          { num: 2, label: 'Passenger Details', icon: Users },
          { num: 3, label: 'Review & Payment', icon: CreditCard },
        ].map((s) => {
          const Icon = s.icon;
          const isDone = step > s.num;
          const isCurrent = step === s.num;

          return (
            <div key={s.num} className="flex items-center gap-2.5">
              <div
                className={`w-9 h-9 rounded-xl flex items-center justify-center font-bold text-xs transition ${
                  isDone
                    ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                    : isCurrent
                    ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold scale-105'
                    : 'bg-[#181A22] text-slate-500 border border-white/10'
                }`}
              >
                {isDone ? <CheckCircle2 className="w-4 h-4 text-emerald-400" /> : <Icon className={`w-4 h-4 ${isCurrent ? 'text-black' : 'text-slate-400'}`} />}
              </div>
              <div className="hidden sm:block">
                <p className="text-[10px] text-slate-400 uppercase font-black tracking-wider">Step {s.num}</p>
                <p className={`text-xs font-bold ${isCurrent ? 'text-amber-400' : 'text-slate-400'}`}>{s.label}</p>
              </div>
            </div>
          );
        })}
      </div>

      {bookingError && (
        <div className="p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-semibold flex items-center gap-2.5 animate-fade-in">
          <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
          <span>{bookingError}</span>
        </div>
      )}

      {/* 3. MAIN CONTENT GRID */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Form / Steps */}
        <div className="lg:col-span-8 space-y-6">
          {/* Step 1: Seat Map Selection */}
          {step === 1 && (
            <div className="space-y-4">
              <div className="p-5 rounded-2xl bg-[#14161F] border border-white/10 shadow-xl flex items-center justify-between">
                <div>
                  <h2 className="text-base font-black text-white">Select Your Aircraft Seats</h2>
                  <p className="text-xs text-slate-400 mt-0.5">
                    Pick {passengerCount} seat(s) for your {cabinClass.replace('_', ' ')} flight
                  </p>
                </div>
              </div>

              <SeatMap
                flightId={flight.id}
                cabinClass={cabinClass}
                seats={seats}
                requiredCount={passengerCount}
                selectedSeats={selectedSeats}
                onSeatSelect={setSelectedSeats}
                onRefreshSeats={loadFlightAndSeats}
              />
            </div>
          )}

          {/* Step 2: Passenger Details Form */}
          {step === 2 && (
            <div className="space-y-4">
              <div className="p-5 rounded-2xl bg-[#14161F] border border-white/10 shadow-xl">
                <h2 className="text-base font-black text-white">Passenger Information</h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  Enter details exactly as they appear on passenger government ID cards
                </p>
              </div>

              <PassengerForm
                passengers={passengers}
                selectedSeats={selectedSeats}
                onChange={handlePassengerChange}
                errors={errors}
              />

              {/* DEALS & OFFERS SECTION */}
              <div className="p-6 rounded-2xl bg-[#14161F] border border-white/10 shadow-xl space-y-5">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2.5">
                    <div className="w-8 h-8 rounded-lg bg-amber-400/10 border border-amber-400/20 text-amber-400 flex items-center justify-center font-bold">
                      <Tag className="w-4 h-4" />
                    </div>
                    <div>
                      <h3 className="text-sm font-black text-white flex items-center gap-2">
                        <span>Flight Deals & Offers</span>
                        <span className="text-[10px] font-bold text-amber-400 bg-amber-400/10 px-2 py-0.5 rounded-full border border-amber-400/20">
                          Instant Discounts
                        </span>
                      </h3>
                      <p className="text-xs text-slate-400 mt-0.5">
                        Apply a promo code to unlock instant airline discounts before payment
                      </p>
                    </div>
                  </div>
                  {appliedOffer && (
                    <button
                      type="button"
                      onClick={handleRemoveCoupon}
                      className="text-xs font-semibold text-rose-400 hover:text-rose-300 flex items-center gap-1 transition cursor-pointer"
                    >
                      <X className="w-3.5 h-3.5" />
                      <span>Remove Offer</span>
                    </button>
                  )}
                </div>

                {/* Coupon Input Box */}
                <div className="flex flex-col sm:flex-row gap-2">
                  <div className="relative flex-1">
                    <input
                      type="text"
                      value={couponInput}
                      onChange={(e) => setCouponInput(e.target.value.toUpperCase())}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          e.preventDefault();
                          handleApplyCoupon();
                        }
                      }}
                      placeholder="ENTER PROMO CODE (e.g. SMARTFLY25)"
                      className="w-full bg-[#181A22] border border-white/10 rounded-xl px-4 py-2.5 text-xs text-white font-mono uppercase placeholder:text-slate-500 focus:outline-none focus:border-amber-400 transition tracking-wider"
                    />
                    {couponInput && (
                      <button
                        type="button"
                        onClick={() => setCouponInput('')}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 cursor-pointer"
                      >
                        <X className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </div>
                  <button
                    type="button"
                    onClick={() => handleApplyCoupon()}
                    className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-extrabold text-xs shadow-glow-gold transition flex items-center justify-center gap-1.5 cursor-pointer"
                  >
                    <Sparkles className="w-3.5 h-3.5 text-black" />
                    <span>Apply Code</span>
                  </button>
                </div>

                {/* Feedback Alerts */}
                {couponSuccess && (
                  <div className="p-3 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-xs font-semibold flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-400" />
                    <span>{couponSuccess}</span>
                  </div>
                )}
                {couponError && (
                  <div className="p-3 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-semibold flex items-center gap-2">
                    <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
                    <span>{couponError}</span>
                  </div>
                )}

                {/* Available Coupons Grid */}
                <div className="space-y-3 pt-2">
                  <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">
                    Available Verified Airline Coupons
                  </span>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {VERIFIED_FLIGHT_OFFERS.map((offer) => {
                      const isApplied = appliedOffer?.code === offer.code;
                      const subtotal = calculateCurrentSubtotal();
                      const isEligible = subtotal >= offer.minSpend;

                      return (
                        <div
                          key={offer.code}
                          className={`p-3.5 rounded-xl border transition relative flex flex-col justify-between ${
                            isApplied
                              ? 'bg-emerald-500/10 border-emerald-500/40 shadow-glow-emerald'
                              : 'bg-[#181A22] border-white/10 hover:border-amber-400/40'
                          }`}
                        >
                          <div className="space-y-2">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center gap-2">
                                <span className="font-mono font-black text-xs px-2 py-0.5 rounded bg-[#12131A] text-amber-400 border border-amber-400/30 tracking-wider">
                                  {offer.code}
                                </span>
                                <button
                                  type="button"
                                  onClick={() => handleCopyCode(offer.code)}
                                  className="text-slate-500 hover:text-white transition p-0.5 cursor-pointer"
                                  title="Copy Code"
                                >
                                  {copiedCode === offer.code ? (
                                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                                  ) : (
                                    <Copy className="w-3.5 h-3.5" />
                                  )}
                                </button>
                              </div>
                              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-white/5 text-slate-300 border border-white/10">
                                {offer.badge}
                              </span>
                            </div>

                            <div>
                              <div className="text-xs font-bold text-white flex items-center gap-1.5">
                                <Percent className="w-3.5 h-3.5 text-amber-400" />
                                <span>{offer.discountLabel}</span>
                              </div>
                              <p className="text-[11px] text-slate-400 mt-1 leading-relaxed">
                                {offer.description}
                              </p>
                            </div>
                          </div>

                          <div className="pt-3 mt-2 border-t border-white/5 flex items-center justify-between">
                            <span className="text-[10px] text-slate-400">
                              Min. spend: ₹{offer.minSpend.toLocaleString('en-IN')}
                            </span>

                            {isApplied ? (
                              <button
                                type="button"
                                onClick={handleRemoveCoupon}
                                className="px-3 py-1 rounded-lg bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 text-xs font-bold flex items-center gap-1 cursor-pointer"
                              >
                                <Check className="w-3 h-3 text-emerald-400" />
                                <span>Applied</span>
                              </button>
                            ) : (
                              <button
                                type="button"
                                onClick={() => handleApplyCoupon(offer.code)}
                                className={`px-3 py-1 rounded-lg text-xs font-bold transition flex items-center gap-1 cursor-pointer ${
                                  isEligible
                                    ? 'bg-amber-400 text-black hover:bg-amber-300 font-extrabold shadow-glow-gold'
                                    : 'bg-white/5 text-slate-400 hover:bg-white/10 border border-white/10'
                                }`}
                              >
                                <span>Apply</span>
                              </button>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 3: Review & Confirm */}
          {step === 3 && (
            <div className="space-y-6">
              <div className="p-5 rounded-2xl bg-[#14161F] border border-white/10 shadow-xl">
                <h2 className="text-base font-black text-white">Review Itinerary & Travelers</h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  Please review all flight and passenger details before proceeding to payment
                </p>
              </div>

              {/* Review Itinerary Box */}
              <div className="p-6 rounded-2xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
                <div className="flex items-center justify-between pb-3 border-b border-white/10">
                  <div className="flex items-center gap-2 text-white font-bold text-sm">
                    <Plane className="w-4 h-4 text-amber-400" />
                    <span>Flight Schedule</span>
                  </div>
                  <span className="text-xs font-mono font-bold text-amber-400 bg-[#181A22] px-2 py-0.5 rounded border border-white/10">
                    {flight.flightNumber}
                  </span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                  <div>
                    <span className="text-slate-400 block font-medium">Departure</span>
                    <strong className="text-white text-sm">
                      {new Date(flight.departureTime).toLocaleDateString('en-US', {
                        weekday: 'short',
                        month: 'short',
                        day: 'numeric',
                      })}{' '}
                      at {new Date(flight.departureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </strong>
                    <p className="text-slate-400 mt-0.5">
                      {flight.departureAirport.name} ({flight.departureAirport.code})
                    </p>
                  </div>

                  <div>
                    <span className="text-slate-400 block font-medium">Arrival</span>
                    <strong className="text-white text-sm">
                      {new Date(flight.arrivalTime).toLocaleDateString('en-US', {
                        weekday: 'short',
                        month: 'short',
                        day: 'numeric',
                      })}{' '}
                      at {new Date(flight.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </strong>
                    <p className="text-slate-400 mt-0.5">
                      {flight.arrivalAirport.name} ({flight.arrivalAirport.code})
                    </p>
                  </div>
                </div>
              </div>

              {/* Review Passenger List Box */}
              <div className="p-6 rounded-2xl bg-[#14161F] border border-white/10 shadow-xl space-y-4">
                <div className="flex items-center justify-between pb-3 border-b border-white/10">
                  <div className="flex items-center gap-2 text-white font-bold text-sm">
                    <Users className="w-4 h-4 text-amber-400" />
                    <span>Confirmed Passengers ({passengers.length})</span>
                  </div>
                </div>

                <div className="space-y-3">
                  {passengers.map((pax, idx) => (
                    <div
                      key={idx}
                      className="p-3.5 rounded-xl bg-[#181A22] border border-white/10 flex items-center justify-between text-xs"
                    >
                      <div className="space-y-0.5">
                        <strong className="text-white font-bold">
                          {pax.title} {pax.firstName} {pax.lastName}
                        </strong>
                        <p className="text-[11px] text-slate-400">
                          {pax.gender} • {pax.nationality || 'Indian'}
                        </p>
                      </div>

                      <div className="flex items-center gap-2">
                        <span className="text-xs font-mono font-bold text-amber-400 bg-amber-400/10 px-2.5 py-1 rounded-lg border border-amber-400/20">
                          Seat {selectedSeats[idx]}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Applied Discount Banner in Step 3 */}
              {appliedOffer && (
                <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-xs flex items-center justify-between shadow-glow-emerald">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold">
                      <Tag className="w-4 h-4" />
                    </div>
                    <div>
                      <span className="text-white font-bold flex items-center gap-2">
                        <span>Applied Offer:</span>
                        <span className="font-mono text-emerald-400 font-black">{appliedOffer.code}</span>
                      </span>
                      <p className="text-[11px] text-slate-400">{appliedOffer.title}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className="text-[10px] text-slate-400 uppercase block font-semibold">Total Savings</span>
                    <span className="text-sm font-black text-emerald-400 font-mono">
                      -₹{appliedOffer.discountAmount.toLocaleString('en-IN')}
                    </span>
                  </div>
                </div>
              )}

              {/* Pre-Checkout Legal Consent & Cancellation Notice */}
              <div className="p-4 rounded-2xl bg-[#14161F] border border-white/10 text-xs space-y-2 text-slate-300">
                <div className="flex items-center gap-2 font-bold text-amber-400">
                  <ShieldCheck className="w-4 h-4 text-amber-400 shrink-0" />
                  <span>Fare & Cancellation Terms Confirmation</span>
                </div>
                <p className="text-[11px] text-slate-400 leading-relaxed">
                  By clicking <strong>&quot;Proceed to Secure Payment&quot;</strong>, you confirm the passenger details above and agree to SmartTravel&apos;s{' '}
                  <Link to="/terms-and-conditions" target="_blank" className="text-amber-400 font-bold hover:underline">
                    Terms &amp; Conditions
                  </Link>
                  ,{' '}
                  <Link to="/terms-and-conditions#cancellation-policy" target="_blank" className="text-amber-400 font-bold hover:underline">
                    Cancellation &amp; Refund Policy
                  </Link>
                  , and acknowledge our{' '}
                  <Link to="/privacy-policy" target="_blank" className="text-amber-400 font-bold hover:underline">
                    Privacy Policy
                  </Link>.
                </p>
              </div>
            </div>
          )}

          {/* Navigation Controls */}
          <div className="pt-4 flex items-center justify-between">
            {step > 1 ? (
              <button
                type="button"
                onClick={() => setStep(step - 1)}
                className="px-5 py-2.5 rounded-xl bg-[#14161F] hover:bg-[#1F222E] text-slate-300 text-xs font-bold flex items-center gap-2 transition border border-white/10 cursor-pointer"
              >
                <ChevronLeft className="w-4 h-4 text-amber-400" />
                <span>Back</span>
              </button>
            ) : (
              <div></div>
            )}

            {step < 3 ? (
              <button
                type="button"
                onClick={handleNextStep}
                className="px-7 py-3 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-extrabold text-xs sm:text-sm shadow-glow-gold transition flex items-center gap-2 cursor-pointer"
              >
                <span>Continue to {step === 1 ? 'Passenger Details' : 'Review & Payment'}</span>
                <ChevronRight className="w-4 h-4 text-black" />
              </button>
            ) : (
              <button
                type="button"
                disabled={bookingLoading}
                onClick={handleCreateBookingAndPay}
                className="px-8 py-3.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-black text-sm shadow-glow-gold transition flex items-center gap-2.5 disabled:opacity-50 cursor-pointer"
              >
                {bookingLoading ? (
                  <span className="flex items-center gap-2 text-black">
                    <span className="w-4 h-4 border-2 border-black/30 border-t-black rounded-full animate-spin"></span>
                    Holding Seats & Reserving...
                  </span>
                ) : (
                  <>
                    <CreditCard className="w-4 h-4 text-black" />
                    <span>Proceed to Secure Payment</span>
                  </>
                )}
              </button>
            )}
          </div>
        </div>

        {/* Right Sticky Fare Summary Card */}
        <div className="lg:col-span-4 space-y-4">
          {userFreezes.length > 0 && (
            <div className="p-4 rounded-2xl bg-amber-400/10 border border-amber-400/20 text-xs text-slate-300 space-y-2 shadow-glow-gold">
              <div className="flex items-center justify-between">
                <span className="font-bold text-amber-400 flex items-center gap-1.5">
                  <ShieldCheck className="w-4 h-4 text-amber-400" />
                  Active Price Freeze Found
                </span>
                <span className="text-xs text-white font-mono font-bold">
                  ₹{userFreezes[0].lockedTotalPrice.toLocaleString('en-IN')}
                </span>
              </div>
              <p className="text-[11px] text-slate-400">
                You have a locked price freeze expiring at{' '}
                <strong className="text-white">
                  {new Date(userFreezes[0].expiresAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </strong>.
              </p>
              <div className="pt-1">
                <button
                  type="button"
                  onClick={() => setAppliedFreeze(appliedFreeze ? null : userFreezes[0])}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                    appliedFreeze
                      ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                      : 'bg-[#181A22] hover:bg-[#1F222E] text-amber-400 border border-amber-400/30'
                  }`}
                >
                  {appliedFreeze ? '✓ Locked Fare Applied' : 'Apply Locked Fare'}
                </button>
              </div>
            </div>
          )}

          <FareSummaryCard
            flight={flight}
            cabinClass={cabinClass}
            passengerCount={passengerCount}
            selectedSeats={selectedSeats}
            appliedFreeze={appliedFreeze}
            appliedOffer={appliedOffer}
          />
        </div>
      </div>

      {/* Payment Gateway Modal */}
      {showPaymentModal && createdBooking && (
        <PaymentModal
          booking={createdBooking}
          onPaymentSuccess={() => {
            setShowPaymentModal(false);
            navigate(`/confirmation/${createdBooking.bookingReference}`, {
              state: { booking: createdBooking }
            });
          }}
          onClose={() => setShowPaymentModal(false)}
        />
      )}
    </div>
  );
};
