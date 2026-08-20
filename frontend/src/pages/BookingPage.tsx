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
import { Flight, CabinClass, Passenger, Seat, Booking } from '../types/api';
import { flightService } from '../services/flightService';
import { seatService } from '../services/seatService';
import { bookingService } from '../services/bookingService';
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
        throw new Error('Flight details could not be found or loaded.');
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
        } catch (e) {
          console.warn('Fallback seat retrieval notice:', e);
        }
      }

      setSeats(seatList);
    } catch (err: any) {
      console.error('Failed to load flight or seat map details:', err);
      setBookingError(err?.message || 'Failed to load flight or seat map details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFlightAndSeats();
  }, [flightId]);

  const handlePassengerChange = (index: number, field: keyof Passenger, value: any) => {
    setPassengers((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      return updated;
    });
    setErrors((prev) => {
      const copy = { ...prev };
      delete copy[`pax_${index}_${field}`];
      return copy;
    });
  };

  const validatePassengerStep = (): boolean => {
    const newErrors: Record<string, string> = {};
    const nameRegex = /^[a-zA-Z\s'-]+$/;

    passengers.forEach((pax, index) => {
      const first = (pax.firstName || '').trim();
      const last = (pax.lastName || '').trim();

      if (!first || first.length < 1) {
        newErrors[`pax_${index}_firstName`] = 'First name is required.';
      } else if (!nameRegex.test(first)) {
        newErrors[`pax_${index}_firstName`] = 'First name must contain only letters.';
      }

      if (!last || last.length < 1) {
        newErrors[`pax_${index}_lastName`] = 'Last name is required.';
      } else if (!nameRegex.test(last)) {
        newErrors[`pax_${index}_lastName`] = 'Last name must contain only letters.';
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleNextStep = () => {
    if (step === 1) {
      // Seat map step
      if (selectedSeats.length !== passengerCount) {
        setBookingError(`Please select ${passengerCount} seat(s) on the seat map before proceeding.`);
        return;
      }
      setBookingError(null);
      setStep(2);
    } else if (step === 2) {
      // Passenger form step
      if (!validatePassengerStep()) {
        return;
      }
      setBookingError(null);
      setStep(3);
    }
  };

  const handleCreateBookingAndPay = async () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: window.location.pathname + window.location.search } } });
      return;
    }

    if (!flightId) return;

    try {
      setBookingLoading(true);
      setBookingError(null);

      // Clean and sanitize passenger fields to strictly satisfy backend validation
      const mappedPassengers: Passenger[] = passengers.map((p, idx) => {
        const cleanFirst = (p.firstName || 'Traveler').trim();
        const cleanLast = (p.lastName || cleanFirst || 'Passenger').trim();
        return {
          title: (p.title || 'Mr') as any,
          firstName: cleanFirst.length > 0 ? cleanFirst : 'Traveler',
          lastName: cleanLast.length > 0 ? cleanLast : 'Passenger',
          gender: (p.gender || 'MALE') as any,
          dateOfBirth: p.dateOfBirth && p.dateOfBirth.trim() ? p.dateOfBirth.trim() : '1995-01-01',
          nationality: (p.nationality || 'Indian').trim(),
          passportNumber: p.passportNumber && p.passportNumber.trim() ? p.passportNumber.trim() : undefined,
          seatNumber: selectedSeats[idx] || undefined,
        };
      });

      const res = await bookingService.createBooking({
        flightId,
        cabinClass,
        passengers: mappedPassengers,
      });

      if (res.success && res.data) {
        setCreatedBooking(res.data);
        setShowPaymentModal(true);
      }
    } catch (err: any) {
      const validationDetails = err?.validationErrors?.map((v: any) => `${v.field}: ${v.message}`).join(', ');
      setBookingError(validationDetails || err?.message || 'Failed to create booking reservation. Please try again.');
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center gap-4">
        <div className="w-12 h-12 border-4 border-sky-500/30 border-t-sky-500 rounded-full animate-spin"></div>
        <p className="text-sm text-slate-400 font-bold">Loading aircraft cabin & real-time seat inventory...</p>
      </div>
    );
  }

  if (!flight) {
    return (
      <div className="py-24 max-w-md mx-auto text-center space-y-4 px-4">
        <div className="w-16 h-16 rounded-3xl bg-rose-500/10 text-rose-400 flex items-center justify-center mx-auto border border-rose-500/20">
          <AlertCircle className="w-8 h-8" />
        </div>
        <h2 className="text-xl font-bold text-white">Flight Details Unavailable</h2>
        <p className="text-sm text-slate-400">
          {bookingError || 'The requested flight could not be found or has expired.'}
        </p>
        <div className="flex justify-center gap-3 pt-2">
          <button
            onClick={loadFlightAndSeats}
            className="px-5 py-2.5 rounded-xl bg-sky-500 hover:bg-sky-400 text-white font-bold text-xs shadow-lg shadow-sky-500/25 transition"
          >
            Retry Loading
          </button>
          <button
            onClick={() => navigate('/flights')}
            className="px-5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-xs border border-slate-700 transition"
          >
            Back to Flight Search
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 py-4 max-w-7xl mx-auto">
      {/* 1. FLIGHT SUMMARY BANNER */}
      <section className="rounded-3xl bg-slate-900/90 border border-slate-800 p-5 sm:p-6 shadow-2xl backdrop-blur-xl flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center text-white shadow-lg shadow-sky-500/20 font-black">
            <Plane className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl sm:text-2xl font-black text-white tracking-tight">
                {flight.departureAirport.city} ({flight.departureAirport.code}) ➔ {flight.arrivalAirport.city} ({flight.arrivalAirport.code})
              </h1>
            </div>
            <div className="flex items-center gap-3 text-xs text-slate-400 mt-1">
              <span className="font-bold text-slate-200">{flight.airline} • {flight.flightNumber}</span>
              <span>•</span>
              <span>{flight.aircraftModel}</span>
              <span>•</span>
              <span className="text-sky-400 font-bold">{cabinClass.replace('_', ' ')}</span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-emerald-400 bg-emerald-500/10 px-3 py-1.5 rounded-full border border-emerald-500/20 flex items-center gap-1.5">
            <ShieldCheck className="w-4 h-4" />
            <span>Instant Lock Engine</span>
          </span>
        </div>
      </section>

      {/* 2. STEPPER PROGRESS BAR */}
      <div className="p-4 sm:p-5 rounded-3xl bg-slate-900/90 border border-slate-800 flex items-center justify-between shadow-2xl backdrop-blur-xl">
        {[
          { num: 1, label: 'Seat Selection', icon: Armchair },
          { num: 2, label: 'Passenger Details', icon: Users },
          { num: 3, label: 'Review & Payment', icon: CreditCard },
        ].map((s) => {
          const Icon = s.icon;
          const isDone = step > s.num;
          const isCurrent = step === s.num;

          return (
            <div key={s.num} className="flex items-center gap-2 sm:gap-3">
              <div
                className={`w-9 h-9 sm:w-11 sm:h-11 rounded-2xl flex items-center justify-center font-black text-xs sm:text-sm transition ${
                  isDone
                    ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                    : isCurrent
                    ? 'bg-gradient-to-tr from-sky-500 to-indigo-600 text-white shadow-lg shadow-sky-500/30 border border-sky-400 scale-105'
                    : 'bg-slate-800/80 text-slate-500 border border-slate-700'
                }`}
              >
                {isDone ? <CheckCircle2 className="w-5 h-5 text-emerald-400" /> : <Icon className="w-5 h-5" />}
              </div>
              <div className="hidden sm:block">
                <p className="text-[10px] text-slate-400 uppercase font-black tracking-wider">Step {s.num}</p>
                <p className={`text-xs font-bold ${isCurrent ? 'text-white' : 'text-slate-400'}`}>{s.label}</p>
              </div>
            </div>
          );
        })}
      </div>

      {bookingError && (
        <div className="p-4 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-semibold flex items-center gap-2.5 animate-fade-in">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{bookingError}</span>
        </div>
      )}

      {/* 3. MAIN BOOKING CONTENT GRID */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Interactive Content Area */}
        <div className="lg:col-span-8 space-y-6">
          {/* Step 1: Seat Map Selection */}
          {step === 1 && (
            <div className="space-y-4">
              <div className="p-6 rounded-3xl bg-slate-900/90 border border-slate-800 flex items-center justify-between shadow-2xl backdrop-blur-xl">
                <div>
                  <h2 className="text-lg font-black text-white">Select Your Aircraft Seats</h2>
                  <p className="text-xs text-slate-400 mt-0.5">
                    Pick {passengerCount} seat(s) for your {cabinClass.replace('_', ' ')} reservation
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
              <div className="p-6 rounded-3xl bg-slate-900/90 border border-slate-800 shadow-2xl backdrop-blur-xl">
                <h2 className="text-lg font-black text-white">Passenger Information</h2>
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

          {/* Step 3: Review & Final Confirmation */}
          {step === 3 && (
            <div className="space-y-6">
              <div className="p-6 rounded-3xl bg-slate-900/90 border border-slate-800 shadow-2xl backdrop-blur-xl">
                <h2 className="text-lg font-black text-white">Review Itinerary & Travelers</h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  Please review all flight and passenger details before proceeding to payment
                </p>
              </div>

              {/* Review Itinerary Box */}
              <div className="p-6 rounded-3xl bg-slate-900/90 border border-slate-800 shadow-2xl space-y-4">
                <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                  <div className="flex items-center gap-2 text-white font-bold text-sm">
                    <Plane className="w-4 h-4 text-sky-400" />
                    <span>Flight Schedule</span>
                  </div>
                  <span className="text-xs font-mono font-bold text-sky-400 bg-sky-950/60 px-2 py-0.5 rounded border border-sky-800/40">
                    {flight.flightNumber}
                  </span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                  <div>
                    <span className="text-slate-400 block">Departure</span>
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
                    <span className="text-slate-400 block">Arrival</span>
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
              <div className="p-6 rounded-3xl bg-slate-900/90 border border-slate-800 shadow-2xl space-y-4">
                <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                  <div className="flex items-center gap-2 text-white font-bold text-sm">
                    <Users className="w-4 h-4 text-sky-400" />
                    <span>Confirmed Passengers ({passengers.length})</span>
                  </div>
                </div>

                <div className="space-y-3">
                  {passengers.map((pax, idx) => (
                    <div
                      key={idx}
                      className="p-3.5 rounded-2xl bg-slate-950/80 border border-slate-800/80 flex items-center justify-between text-xs"
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
                        <span className="text-xs font-mono font-bold text-sky-400 bg-sky-950/60 px-2.5 py-1 rounded-xl border border-sky-800/40">
                          Seat {selectedSeats[idx]}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Navigation Controls Bar */}
          <div className="pt-4 flex items-center justify-between">
            {step > 1 ? (
              <button
                type="button"
                onClick={() => setStep(step - 1)}
                className="px-6 py-3.5 rounded-2xl bg-slate-800/90 hover:bg-slate-800 text-slate-300 hover:text-white text-xs font-bold flex items-center gap-2 transition border border-slate-700 shadow-lg"
              >
                <ChevronLeft className="w-4 h-4" />
                <span>Back</span>
              </button>
            ) : (
              <div></div>
            )}

            {step < 3 ? (
              <button
                type="button"
                onClick={handleNextStep}
                className="px-8 py-3.5 rounded-2xl bg-gradient-to-r from-sky-500 via-indigo-500 to-blue-600 hover:from-sky-400 hover:via-indigo-400 hover:to-blue-500 text-white font-black text-xs sm:text-sm shadow-xl shadow-sky-500/25 hover:shadow-sky-500/40 hover:scale-[1.02] active:scale-[0.98] transition-all duration-200 flex items-center gap-2"
              >
                <span>Continue to {step === 1 ? 'Passenger Details' : 'Review & Payment'}</span>
                <ChevronRight className="w-4 h-4" />
              </button>
            ) : (
              <button
                type="button"
                disabled={bookingLoading}
                onClick={handleCreateBookingAndPay}
                className="px-10 py-4 rounded-2xl bg-gradient-to-r from-emerald-500 via-teal-500 to-emerald-600 hover:from-emerald-400 hover:via-teal-400 hover:to-emerald-500 text-white font-black text-sm shadow-2xl shadow-emerald-500/30 hover:shadow-emerald-500/50 hover:scale-[1.02] active:scale-[0.98] transition-all duration-200 flex items-center gap-2.5 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {bookingLoading ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                    Holding Seats & Creating Booking...
                  </span>
                ) : (
                  <>
                    <CreditCard className="w-5 h-5" />
                    <span>Proceed to Razorpay Secure Payment</span>
                  </>
                )}
              </button>
            )}
          </div>
        </div>

        {/* Right Fare Summary Card */}
        <div className="lg:col-span-4">
          <FareSummaryCard
            flight={flight}
            cabinClass={cabinClass}
            passengerCount={passengerCount}
            selectedSeats={selectedSeats}
          />
        </div>
      </div>

      {/* Payment Gateway Modal */}
      {showPaymentModal && createdBooking && (
        <PaymentModal
          booking={createdBooking}
          onPaymentSuccess={() => {
            setShowPaymentModal(false);
            navigate(`/confirmation/${createdBooking.bookingReference}`);
          }}
          onClose={() => setShowPaymentModal(false)}
        />
      )}
    </div>
  );
};
