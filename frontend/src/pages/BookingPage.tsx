import React, { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import {
  Armchair,
  Users,
  CreditCard,
  CheckCircle2,
  ChevronRight,
  ChevronLeft,
  AlertCircle,
  Plane,
  ShieldCheck
} from 'lucide-react';
import { Flight, CabinClass, Passenger, Seat, Booking, PriceFreeze } from '../types/api';
import { flightService } from '../services/flightService';
import { seatService } from '../services/seatService';
import { bookingService } from '../services/bookingService';
import { pricingService } from '../services/pricingService';
import { useAuth } from '../context/AuthContext';
import { SeatMap } from '../components/SeatMap';
import { PassengerForm } from '../components/PassengerForm';
import { FareSummaryCard } from '../components/FareSummaryCard';
import { PaymentModal } from '../components/PaymentModal';

export const BookingPage: React.FC = () => {
  const { flightId } = useParams<{ flightId: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  const cabinClass = (searchParams.get('cabinClass') as CabinClass) || 'ECONOMY';
  const passengerCount = parseInt(searchParams.get('passengers') || '1', 10);
  const initialFreezeId = searchParams.get('priceFreezeId') || searchParams.get('freezeId');

  // Steps: 1: Seats, 2: Passengers, 3: Review & Book
  const [step, setStep] = useState<number>(1);

  const [flight, setFlight] = useState<Flight | null>(null);
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
  const [loading, setLoading] = useState<boolean>(true);
  const [bookingLoading, setBookingLoading] = useState<boolean>(false);
  const [bookingError, setBookingError] = useState<string | null>(null);

  // Price Freeze state
  const [appliedFreeze, setAppliedFreeze] = useState<PriceFreeze | null>(null);
  const [userFreezes, setUserFreezes] = useState<PriceFreeze[]>([]);

  // Post-booking & Payment state
  const [createdBooking, setCreatedBooking] = useState<Booking | null>(null);
  const [showPaymentModal, setShowPaymentModal] = useState<boolean>(false);

  // Fetch Flight & Seat Map
  const loadFlightAndSeats = async () => {
    if (!flightId) return;
    try {
      setLoading(true);
      setBookingError(null);
      const [flightRes, seatsRes] = await Promise.all([
        flightService.getFlightById(flightId),
        seatService.getSeatMap(flightId).catch(() => null),
      ]);

      if (flightRes && flightRes.data) {
        setFlight(flightRes.data);
      } else {
        throw new Error('Flight details could not be loaded.');
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

      // Auto-assign available seats if none selected yet
      if (selectedSeats.length === 0 && seatList.length > 0) {
        const available = seatList.filter((s) => s.status === 'AVAILABLE').slice(0, passengerCount);
        if (available.length > 0) {
          setSelectedSeats(available.map((s) => s.seatNumber));
        }
      }
    } catch (err: any) {
      setBookingError(err.message || 'Failed to load flight or seat information.');
    } finally {
      setLoading(false);
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
      });

      if (res && res.data) {
        setCreatedBooking(res.data);
        setShowPaymentModal(true);
      } else {
        throw new Error('Failed to create booking reservation.');
      }
    } catch (err: any) {
      setBookingError(err.message || 'Seat lock or booking creation failed. Please reselect seats.');
      // If seat conflict occurred, return to step 1
      if (err.message?.includes('Seat') || err.message?.includes('conflict') || err.message?.includes('occupied')) {
        setStep(1);
        loadFlightAndSeats();
      }
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center gap-4">
        <div className="w-10 h-10 border-4 border-amber-400/30 border-t-amber-400 rounded-full animate-spin"></div>
        <p className="text-xs text-slate-400 font-bold">Loading aircraft cabin & real-time seat inventory...</p>
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
          <div className="w-11 h-11 rounded-xl bg-[#181A22] border border-white/10 flex items-center justify-center text-amber-400 font-bold shadow-glow-gold">
            <Plane className="w-6 h-6 transform rotate-45 text-amber-400" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-lg sm:text-xl font-black text-white tracking-tight">
                {flight.departureAirport.city} ({flight.departureAirport.code}) ➔ {flight.arrivalAirport.city} ({flight.arrivalAirport.code})
              </h1>
            </div>
            <div className="flex items-center gap-3 text-xs text-slate-400 mt-1">
              <span className="font-bold text-white">{flight.airline} • {flight.flightNumber}</span>
              <span className="text-white/20">•</span>
              <span>{flight.aircraftModel}</span>
              <span className="text-white/20">•</span>
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
            </div>
          )}

          {/* Navigation Controls */}
          <div className="pt-4 flex items-center justify-between">
            {step > 1 ? (
              <button
                type="button"
                onClick={() => setStep(step - 1)}
                className="px-5 py-2.5 rounded-xl bg-[#14161F] hover:bg-[#1F222E] text-slate-300 text-xs font-bold flex items-center gap-2 transition border border-white/10"
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
