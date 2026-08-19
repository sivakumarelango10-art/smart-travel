import React, { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import {
  Armchair,
  Users,
  CreditCard,
  CheckCircle2,
  ChevronRight,
  ChevronLeft,
  AlertCircle
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
    Array.from({ length: passengerCount }, (_, i) => ({
      title: 'Mr',
      firstName: i === 0 && user?.fullName ? user.fullName.split(' ')[0] : '',
      lastName: i === 0 && user?.fullName ? user.fullName.split(' ').slice(1).join(' ') || '' : '',
      gender: 'MALE',
      nationality: 'Indian',
      passportNumber: '',
    }))
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
        seatService.getSeatMap(flightId),
      ]);

      if (flightRes.success && flightRes.data) {
        setFlight(flightRes.data);
      }
      if (seatsRes.success && seatsRes.data) {
        setSeats(seatsRes.data);
      }
    } catch (err: any) {
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
    passengers.forEach((pax, index) => {
      if (!pax.firstName || pax.firstName.trim().length < 2) {
        newErrors[`pax_${index}_firstName`] = 'First name must be at least 2 characters.';
      }
      if (!pax.lastName || pax.lastName.trim().length < 1) {
        newErrors[`pax_${index}_lastName`] = 'Last name is required.';
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

      // Attach seat numbers to passenger array
      const mappedPassengers: Passenger[] = passengers.map((p, idx) => ({
        ...p,
        seatNumber: selectedSeats[idx] || undefined,
      }));

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
      setBookingError(err?.message || 'Failed to create booking reservation. Please try again.');
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading || !flight) {
    return (
      <div className="py-20 flex flex-col items-center justify-center gap-3">
        <div className="w-10 h-10 border-4 border-sky-500/30 border-t-sky-500 rounded-full animate-spin"></div>
        <p className="text-sm text-slate-400 font-medium">Loading flight cabin & physical seat map...</p>
      </div>
    );
  }

  return (
    <div className="space-y-8 py-4 max-w-7xl mx-auto">
      {/* Stepper Progress Bar */}
      <div className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800 flex items-center justify-between shadow-xl">
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
                className={`w-8 h-8 sm:w-10 sm:h-10 rounded-xl flex items-center justify-center font-bold text-xs sm:text-sm transition ${
                  isDone
                    ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                    : isCurrent
                    ? 'bg-gradient-to-tr from-sky-500 to-indigo-600 text-white shadow-lg shadow-sky-500/25 border border-sky-400'
                    : 'bg-slate-800 text-slate-500 border border-slate-700'
                }`}
              >
                {isDone ? <CheckCircle2 className="w-5 h-5 text-emerald-400" /> : <Icon className="w-4 h-4" />}
              </div>
              <div className="hidden sm:block">
                <p className="text-[10px] text-slate-400 uppercase font-semibold">Step {s.num}</p>
                <p className={`text-xs font-bold ${isCurrent ? 'text-white' : 'text-slate-400'}`}>{s.label}</p>
              </div>
            </div>
          );
        })}
      </div>

      {bookingError && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-medium flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{bookingError}</span>
        </div>
      )}

      {/* Main Booking Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Interactive Content Area */}
        <div className="lg:col-span-8 space-y-6">
          {/* Step 1: Seat Map Selection */}
          {step === 1 && (
            <div className="space-y-4">
              <div className="p-5 rounded-2xl bg-slate-900/90 border border-slate-800 flex items-center justify-between">
                <div>
                  <h2 className="text-lg font-bold text-white">Select Your Aircraft Seats</h2>
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
              <div className="p-5 rounded-2xl bg-slate-900/90 border border-slate-800">
                <h2 className="text-lg font-bold text-white">Passenger Information</h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  Enter details as per government-issued photo identification / passport
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

          {/* Step 3: Final Review Before Creation */}
          {step === 3 && (
            <div className="space-y-6">
              <div className="p-6 rounded-2xl bg-slate-900/90 border border-slate-800 space-y-4 shadow-xl">
                <h2 className="text-lg font-bold text-white">Review Flight & Travelers</h2>

                {/* Flight Card Mini */}
                <div className="p-4 rounded-xl bg-slate-950/70 border border-slate-800 space-y-2 text-xs">
                  <div className="flex justify-between items-center text-white font-bold text-sm">
                    <span>
                      {flight.airline} ({flight.flightNumber})
                    </span>
                    <span className="text-sky-400 font-mono">{flight.aircraftModel}</span>
                  </div>
                  <div className="flex justify-between text-slate-300">
                    <span>Route:</span>
                    <span>
                      {flight.departureAirport.city} ({flight.departureAirport.code}) ➔{' '}
                      {flight.arrivalAirport.city} ({flight.arrivalAirport.code})
                    </span>
                  </div>
                  <div className="flex justify-between text-slate-300">
                    <span>Departure Time:</span>
                    <span>{new Date(flight.departureTime).toLocaleString()}</span>
                  </div>
                </div>

                {/* Travelers List */}
                <div className="space-y-2">
                  <h4 className="text-xs font-semibold text-slate-300">Travelers & Seat Assignments:</h4>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    {passengers.map((p, idx) => (
                      <div
                        key={idx}
                        className="p-3 rounded-xl bg-slate-950/50 border border-slate-800 text-xs flex justify-between items-center"
                      >
                        <span className="font-semibold text-white">
                          {p.title} {p.firstName} {p.lastName}
                        </span>
                        <span className="font-mono text-sky-400 font-bold bg-sky-950/40 px-2 py-0.5 rounded border border-sky-800/40">
                          Seat: {selectedSeats[idx]}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Stepper Navigation Buttons */}
          <div className="flex items-center justify-between pt-4 border-t border-slate-800">
            {step > 1 ? (
              <button
                type="button"
                onClick={() => setStep(step - 1)}
                className="px-5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-300 hover:text-white text-xs font-semibold flex items-center gap-2 border border-slate-700 transition"
              >
                <ChevronLeft className="w-4 h-4" />
                Previous Step
              </button>
            ) : (
              <div></div>
            )}

            {step < 3 ? (
              <button
                type="button"
                onClick={handleNextStep}
                className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white text-xs font-bold flex items-center gap-2 shadow-lg shadow-sky-500/20 transition"
              >
                <span>Continue to {step === 1 ? 'Passenger Details' : 'Review & Pay'}</span>
                <ChevronRight className="w-4 h-4" />
              </button>
            ) : (
              <button
                type="button"
                disabled={bookingLoading}
                onClick={handleCreateBookingAndPay}
                className="px-8 py-3 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white text-sm font-bold flex items-center gap-2 shadow-xl shadow-emerald-500/25 transition disabled:opacity-50"
              >
                {bookingLoading ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                    Holding Reservation...
                  </span>
                ) : (
                  <>
                    <CreditCard className="w-4 h-4" />
                    <span>Proceed to Payment</span>
                  </>
                )}
              </button>
            )}
          </div>
        </div>

        {/* Right Authoritative Fare Summary Column */}
        <aside className="lg:col-span-4">
          <FareSummaryCard
            flight={flight}
            cabinClass={cabinClass}
            passengerCount={passengerCount}
            selectedSeats={selectedSeats}
          />
        </aside>
      </div>

      {/* Razorpay Payment Modal */}
      {showPaymentModal && createdBooking && (
        <PaymentModal
          booking={createdBooking}
          onClose={() => setShowPaymentModal(false)}
          onPaymentSuccess={() => {
            setShowPaymentModal(false);
            navigate(`/booking-confirmation/${createdBooking.id}`);
          }}
        />
      )}
    </div>
  );
};
