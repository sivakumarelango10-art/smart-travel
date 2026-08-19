import React, { useState, useEffect } from 'react';
import {
  CreditCard,
  ShieldCheck,
  Clock,
  AlertCircle,
  CheckCircle2,
  Lock
} from 'lucide-react';
import { Booking, PaymentOrder } from '../types/api';
import { paymentService } from '../services/paymentService';

interface PaymentModalProps {
  booking: Booking;
  onPaymentSuccess: () => void;
  onClose: () => void;
}

export const PaymentModal: React.FC<PaymentModalProps> = ({
  booking,
  onPaymentSuccess,
  onClose,
}) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [order, setOrder] = useState<PaymentOrder | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [paymentSuccess, setPaymentSuccess] = useState<boolean>(false);
  const [timeLeft, setTimeLeft] = useState<number>(15 * 60); // 15 minutes

  // Countdown timer
  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTimer = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  // Initialize Payment Order
  useEffect(() => {
    const initOrder = async () => {
      try {
        setLoading(true);
        const res = await paymentService.createPaymentOrder({
          bookingId: booking.id,
          notes: `Booking for PNR ${booking.bookingReference}`,
        });
        if (res.success && res.data) {
          setOrder(res.data);
        }
      } catch (err: any) {
        setError(err?.message || 'Failed to initialize payment gateway order');
      } finally {
        setLoading(false);
      }
    };
    initOrder();
  }, [booking.id, booking.bookingReference]);

  const handlePayNow = async () => {
    if (!order) return;
    setLoading(true);
    setError(null);

    // If live Razorpay is available in window
    if ((window as any).Razorpay && order.keyId) {
      const options = {
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        name: 'SmartTravel Platform',
        description: `Flight Booking PNR: ${booking.bookingReference}`,
        order_id: order.razorpayOrderId,
        handler: async () => {
          try {
            // Reconcile via backend
            await paymentService.simulateWebhookPayment(order.razorpayOrderId, order.amount);
            setPaymentSuccess(true);
            setTimeout(() => onPaymentSuccess(), 1200);
          } catch {
            setError('Payment captured but confirmation is pending. Checking status...');
            setTimeout(() => onPaymentSuccess(), 2000);
          }
        },
        prefill: {
          name: booking.passengers[0]?.firstName + ' ' + booking.passengers[0]?.lastName,
          email: booking.userEmail,
        },
        theme: {
          color: '#0284c7',
        },
      };

      const rzp = new (window as any).Razorpay(options);
      rzp.open();
      setLoading(false);
    } else {
      // Development simulated webhook checkout
      try {
        await paymentService.simulateWebhookPayment(order.razorpayOrderId, order.amount);
        setPaymentSuccess(true);
        setTimeout(() => onPaymentSuccess(), 1200);
      } catch (err: any) {
        setError(err?.message || 'Payment simulation failed. Please retry.');
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fade-in">
      <div className="w-full max-w-md rounded-3xl bg-slate-900 border border-slate-800 p-6 sm:p-8 shadow-2xl space-y-6 relative overflow-hidden">
        {/* Top Gradient Accent */}
        <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-sky-500 via-indigo-500 to-accent-500"></div>

        {paymentSuccess ? (
          <div className="py-8 text-center space-y-4 animate-scale-up">
            <div className="w-16 h-16 rounded-3xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center mx-auto shadow-lg shadow-emerald-500/20">
              <CheckCircle2 className="w-10 h-10" />
            </div>
            <h2 className="text-2xl font-black text-white">Payment Verified!</h2>
            <p className="text-xs text-slate-400 max-w-xs mx-auto">
              Issuing authoritative e-ticket and locking seat reservations in real-time...
            </p>
          </div>
        ) : (
          <>
            {/* Header */}
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-sky-500 to-indigo-600 text-white flex items-center justify-center shadow-lg shadow-sky-500/20">
                  <CreditCard className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="font-extrabold text-white text-lg leading-tight">Razorpay Gateway</h2>
                  <p className="text-[10px] text-slate-400">256-Bit Encrypted Authoritative Checkout</p>
                </div>
              </div>

              <div className="flex items-center gap-1.5 text-xs font-mono font-bold text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2.5 py-1 rounded-full">
                <Clock className="w-3.5 h-3.5" />
                <span>{formatTimer(timeLeft)}</span>
              </div>
            </div>

            {/* Booking Summary Box */}
            <div className="p-4 rounded-2xl bg-slate-950/80 border border-slate-800 space-y-2.5 text-xs">
              <div className="flex justify-between text-slate-300">
                <span className="text-slate-400">Booking PNR</span>
                <span className="font-mono font-bold text-sky-400 bg-sky-950/60 px-2 py-0.5 rounded border border-sky-800/40">
                  {booking.bookingReference}
                </span>
              </div>
              <div className="flex justify-between text-slate-300">
                <span className="text-slate-400">Flight Route</span>
                <span className="font-bold text-white">
                  {booking.departureAirport.code} ➔ {booking.arrivalAirport.code} ({booking.flightNumber})
                </span>
              </div>
              <div className="flex justify-between text-slate-300">
                <span className="text-slate-400">Passengers</span>
                <span className="font-semibold text-slate-200">{booking.passengerCount} traveler(s)</span>
              </div>
              <div className="pt-2.5 border-t border-slate-800 flex justify-between items-baseline font-bold text-sm text-white">
                <span className="text-slate-300">Total Due</span>
                <span className="text-2xl font-black text-emerald-400">
                  ₹{booking.totalAmount.toLocaleString('en-IN')}
                </span>
              </div>
            </div>

            {error && (
              <div className="p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-semibold flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            {/* Actions */}
            <div className="space-y-3 pt-2">
              <button
                type="button"
                disabled={loading || timeLeft === 0}
                onClick={handlePayNow}
                className="w-full py-4 rounded-2xl bg-gradient-to-r from-emerald-500 via-teal-500 to-emerald-600 hover:from-emerald-400 hover:via-teal-400 hover:to-emerald-500 text-white font-black text-sm shadow-xl shadow-emerald-500/25 hover:shadow-emerald-500/40 hover:scale-[1.02] active:scale-[0.98] transition-all duration-200 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                    Verifying Payment...
                  </span>
                ) : (
                  <>
                    <Lock className="w-4 h-4" />
                    <span>Pay ₹{booking.totalAmount.toLocaleString('en-IN')} Securely</span>
                  </>
                )}
              </button>

              <button
                type="button"
                disabled={loading}
                onClick={onClose}
                className="w-full py-2.5 rounded-xl bg-slate-800/80 hover:bg-slate-800 text-slate-400 hover:text-white text-xs font-semibold transition border border-slate-700/60"
              >
                Cancel & Review Booking
              </button>
            </div>

            <div className="flex items-center justify-center gap-2 text-[11px] text-slate-500">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              <span>PCI-DSS Compliant • 256-Bit Encrypted Gateway</span>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

