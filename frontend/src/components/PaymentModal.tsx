import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  CreditCard,
  ShieldCheck,
  Clock,
  AlertCircle,
  CheckCircle2,
  Lock,
  Plane,
  Copy,
  Check,
  X,
  Sparkles,
  Zap
} from 'lucide-react';
import { Booking, PaymentOrder } from '../types/api';
import { paymentService } from '../services/paymentService';
import { AnimatedPrice } from './AnimatedPrice';
import { modalBackdropVariants, modalDialogVariants } from '../lib/motion';

interface PaymentModalProps {
  booking: Booking;
  onPaymentSuccess: () => void;
  onClose: () => void;
}

const loadRazorpayScript = (): Promise<boolean> => {
  return new Promise((resolve) => {
    if ((window as any).Razorpay) {
      resolve(true);
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async = true;
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
};

export const PaymentModal: React.FC<PaymentModalProps> = ({
  booking,
  onPaymentSuccess,
  onClose,
}) => {
  const [payLoading, setPayLoading] = useState<boolean>(false);
  const [order, setOrder] = useState<PaymentOrder | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [paymentSuccess, setPaymentSuccess] = useState<boolean>(false);
  const [copiedPnr, setCopiedPnr] = useState<boolean>(false);
  const [timeLeft, setTimeLeft] = useState<number>(15 * 60); // 15 minutes
  const [paymentMethodTab, setPaymentMethodTab] = useState<'instant' | 'razorpay'>('instant');

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

  // Escape key handler
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && !paymentSuccess) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [paymentSuccess, onClose]);

  const formatTimer = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const handleCopyPnr = () => {
    if (booking.bookingReference) {
      navigator.clipboard.writeText(booking.bookingReference);
      setCopiedPnr(true);
      setTimeout(() => setCopiedPnr(false), 2000);
    }
  };

  const ensureActiveOrder = async (): Promise<PaymentOrder> => {
    if (order) return order;
    const targetBookingId = booking.id || booking.bookingReference;
    const res = await paymentService.createPaymentOrder({
      bookingId: targetBookingId,
      notes: `Booking for PNR ${booking.bookingReference}`,
    });
    if (res.success && res.data) {
      setOrder(res.data);
      return res.data;
    }
    throw new Error(res.message || 'Failed to initialize payment gateway order');
  };

  const handleInstantTestPayment = async () => {
    setPayLoading(true);
    setError(null);
    try {
      const activeOrder = await ensureActiveOrder();
      const mockPayId = `mock_pay_${Date.now()}`;
      const mockSignature = `mock_sig_${Math.random().toString(36).substring(2)}`;

      const verifyRes = await paymentService.verifyPayment({
        razorpayOrderId: activeOrder.razorpayOrderId,
        razorpayPaymentId: mockPayId,
        razorpaySignature: mockSignature,
      });

      if (verifyRes.success) {
        setPaymentSuccess(true);
        setTimeout(() => {
          onPaymentSuccess();
        }, 1200);
      } else {
        setError(verifyRes.message || 'Instant payment verification was declined.');
      }
    } catch (err: any) {
      setError(err.message || 'Instant payment failed');
    } finally {
      setPayLoading(false);
    }
  };

  const handleRazorpayCheckout = async () => {
    setPayLoading(true);
    setError(null);
    try {
      const loaded = await loadRazorpayScript();
      if (!loaded) {
        throw new Error('Razorpay SDK failed to load. Please use Instant 1-Click Pay.');
      }
      const activeOrder = await ensureActiveOrder();
      const options = {
        key: activeOrder.keyId || activeOrder.razorpayKeyId || 'rzp_test_placeholder',
        amount: activeOrder.amount,
        currency: activeOrder.currency || 'INR',
        name: 'SmartTravel Global',
        description: `Booking PNR ${booking.bookingReference}`,
        order_id: activeOrder.razorpayOrderId,
        handler: async function (response: any) {
          try {
            const verifyRes = await paymentService.verifyPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });
            if (verifyRes.success) {
              setPaymentSuccess(true);
              setTimeout(() => {
                onPaymentSuccess();
              }, 1200);
            } else {
              setError(verifyRes.message || 'Signature verification failed.');
            }
          } catch (err: any) {
            setError(err.message || 'Payment verification failed');
          }
        },
        prefill: {
          name: booking.passengers?.[0] ? `${booking.passengers[0].firstName} ${booking.passengers[0].lastName}` : 'Traveler',
          email: booking.userEmail || 'user@smarttravel.com',
          contact: '9876543210',
        },
        theme: {
          color: '#F59E0B',
        },
      };

      const rzp = new (window as any).Razorpay(options);
      rzp.on('payment.failed', function (resp: any) {
        setError(resp.error?.description || 'Gateway transaction declined');
      });
      rzp.open();
    } catch (err: any) {
      setError(err.message || 'Failed to initialize gateway checkout');
    } finally {
      setPayLoading(false);
    }
  };

  const leadPassenger = booking.passengers?.[0]
    ? `${booking.passengers[0].firstName} ${booking.passengers[0].lastName}`
    : 'Lead Traveler';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6">
      {/* Backdrop */}
      <motion.div
        variants={modalBackdropVariants}
        initial="hidden"
        animate="visible"
        exit="exit"
        onClick={() => {
          if (!paymentSuccess) onClose();
        }}
        className="fixed inset-0 bg-black/80 backdrop-blur-md"
      />

      {/* Dialog */}
      <motion.div
        variants={modalDialogVariants}
        initial="hidden"
        animate="visible"
        exit="exit"
        className="relative z-10 w-full max-w-lg rounded-3xl bg-[#14161F] border border-white/10 shadow-2xl p-6 sm:p-7 space-y-5"
      >
        <motion.button
          whileTap={{ scale: 0.9 }}
          type="button"
          onClick={onClose}
          disabled={paymentSuccess}
          className="absolute top-5 right-5 p-2 rounded-xl text-slate-400 hover:text-white bg-[#181A22] hover:bg-[#1F222E] border border-white/10 transition disabled:opacity-30 cursor-pointer z-10"
          aria-label="Close Payment Modal"
        >
          <X className="w-4 h-4" />
        </motion.button>

        {paymentSuccess ? (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="py-10 text-center space-y-4"
          >
            <div className="w-16 h-16 rounded-2xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 flex items-center justify-center mx-auto shadow-glow-emerald">
              <CheckCircle2 className="w-9 h-9 stroke-[2.5]" />
            </div>
            <div className="space-y-1">
              <h2 className="text-2xl font-black text-white tracking-tight">Payment Verified!</h2>
              <p className="text-xs text-slate-400 max-w-xs mx-auto">
                Your flight reservation has been atomically confirmed.
              </p>
            </div>
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-emerald-500/10 text-emerald-400 text-xs font-bold border border-emerald-500/20 shadow-glow-emerald">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Issuing E-Ticket & Boarding Pass...</span>
            </div>
          </motion.div>
        ) : (
          <>
            {/* Modal Header */}
            <div className="flex items-start justify-between pr-8">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-amber-400/10 border border-amber-400/20 text-amber-400 flex items-center justify-center font-bold shadow-glow-gold">
                  <CreditCard className="w-5 h-5 text-amber-400" />
                </div>
                <div>
                  <h2 className="font-black text-white text-base">Secure Checkout</h2>
                  <p className="text-xs text-slate-400 flex items-center gap-1.5 mt-0.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                    256-Bit Encrypted Authoritative Payment
                  </p>
                </div>
              </div>

              {/* Timer Pill */}
              <div className="flex items-center gap-1 text-xs font-mono font-bold text-amber-400 bg-amber-400/10 border border-amber-400/20 px-2.5 py-1 rounded-lg shadow-glow-gold">
                <Clock className="w-3.5 h-3.5" />
                <span>{formatTimer(timeLeft)}</span>
              </div>
            </div>

            {/* Flight Ticket Summary Box */}
            <div className="rounded-2xl bg-[#181A22] border border-white/10 p-4 space-y-3">
              {/* Route & PNR */}
              <div className="flex items-center justify-between pb-3 border-b border-white/5">
                <div className="flex items-center gap-3">
                  <div className="text-left">
                    <div className="text-lg font-black text-white">
                      {booking.departureAirport?.code || 'DEL'}
                    </div>
                    <div className="text-[11px] text-slate-400">
                      {booking.departureAirport?.city || 'Delhi'}
                    </div>
                  </div>
                  <Plane className="w-4 h-4 text-amber-400 rotate-90 mx-1" />
                  <div className="text-left">
                    <div className="text-lg font-black text-white">
                      {booking.arrivalAirport?.code || 'BOM'}
                    </div>
                    <div className="text-[11px] text-slate-400">
                      {booking.arrivalAirport?.city || 'Mumbai'}
                    </div>
                  </div>
                </div>

                {booking.bookingReference && (
                  <div className="flex items-center gap-1.5 bg-[#14161F] px-2.5 py-1 rounded-lg border border-white/10">
                    <span className="text-[10px] text-slate-400 uppercase font-mono">PNR</span>
                    <span className="text-xs font-mono font-black text-amber-400">{booking.bookingReference}</span>
                    <button
                      type="button"
                      onClick={handleCopyPnr}
                      className="p-0.5 text-slate-400 hover:text-white transition"
                      title="Copy PNR"
                    >
                      {copiedPnr ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                    </button>
                  </div>
                )}
              </div>

              {/* Itinerary details */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs">
                <div>
                  <span className="text-[10px] text-slate-400 uppercase block">Flight</span>
                  <span className="font-bold text-white">{booking.flightNumber || 'FLIGHT'}</span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 uppercase block">Class</span>
                  <span className="font-bold text-amber-400">{booking.cabinClass || 'ECONOMY'}</span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 uppercase block">Passengers</span>
                  <span className="font-bold text-white">{booking.passengers?.length || 1} Pax</span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 uppercase block">Seats</span>
                  <span className="font-bold text-white">
                    {booking.passengers?.map((p) => p.seatNumber).filter(Boolean).join(', ') || 'Auto-assigned'}
                  </span>
                </div>
              </div>

              {/* Passenger Name & Total Due */}
              <div className="pt-3 border-t border-white/5 flex items-center justify-between">
                <div>
                  <span className="text-[10px] text-slate-400 block font-medium">Passenger</span>
                  <span className="text-xs font-semibold text-slate-200">{leadPassenger}</span>
                </div>
                <div className="text-right">
                  <span className="text-[10px] text-slate-400 block font-medium">Total Amount Due</span>
                  <div className="text-lg font-black text-amber-400">
                    <AnimatedPrice value={booking.totalAmount || 0} />
                  </div>
                </div>
              </div>
            </div>

            {/* Error Message Alert */}
            <AnimatePresence>
              {error && (
                <motion.div
                  initial={{ opacity: 0, y: -6 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -6 }}
                  className="p-3 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs flex items-center gap-2"
                >
                  <AlertCircle className="w-4 h-4 shrink-0" />
                  <span className="font-medium">{error}</span>
                </motion.div>
              )}
            </AnimatePresence>

            {/* Payment Method Selector Tabs */}
            <div className="grid grid-cols-2 gap-2 p-1 bg-[#181A22] rounded-xl border border-white/10">
              <motion.button
                whileTap={{ scale: 0.96 }}
                type="button"
                onClick={() => setPaymentMethodTab('instant')}
                className={`py-2 px-3 rounded-lg text-xs font-black transition flex items-center justify-center gap-1.5 ${
                  paymentMethodTab === 'instant'
                    ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                <Zap className="w-3.5 h-3.5" />
                <span>Instant 1-Click Pay (Demo)</span>
              </motion.button>
              <motion.button
                whileTap={{ scale: 0.96 }}
                type="button"
                onClick={() => setPaymentMethodTab('razorpay')}
                className={`py-2 px-3 rounded-lg text-xs font-black transition flex items-center justify-center gap-1.5 ${
                  paymentMethodTab === 'razorpay'
                    ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                <Lock className="w-3.5 h-3.5" />
                <span>Razorpay Gateway</span>
              </motion.button>
            </div>

            {/* Action Button */}
            <div className="space-y-2 pt-1">
              {paymentMethodTab === 'instant' ? (
                <motion.button
                  whileTap={{ scale: payLoading || timeLeft === 0 ? 1 : 0.97 }}
                  type="button"
                  disabled={payLoading || timeLeft === 0}
                  onClick={handleInstantTestPayment}
                  className="w-full py-3.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-black text-sm shadow-glow-gold transition flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
                >
                  {payLoading ? (
                    <span className="flex items-center gap-2 text-black">
                      <span className="w-4 h-4 border-2 border-black/30 border-t-black rounded-full animate-spin"></span>
                      <span>Verifying & Issuing Ticket...</span>
                    </span>
                  ) : (
                    <>
                      <Zap className="w-4 h-4 text-black" />
                      <span>Instant 1-Click Pay ₹{booking.totalAmount?.toLocaleString('en-IN')} (Demo Sandbox)</span>
                    </>
                  )}
                </motion.button>
              ) : (
                <motion.button
                  whileTap={{ scale: payLoading || timeLeft === 0 ? 1 : 0.97 }}
                  type="button"
                  disabled={payLoading || timeLeft === 0}
                  onClick={handleRazorpayCheckout}
                  className="w-full py-3.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-black text-sm shadow-glow-gold transition flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
                >
                  {payLoading ? (
                    <span className="flex items-center gap-2 text-black">
                      <span className="w-4 h-4 border-2 border-black/30 border-t-black rounded-full animate-spin"></span>
                      <span>Opening Razorpay Modal...</span>
                    </span>
                  ) : (
                    <>
                      <Lock className="w-4 h-4 text-black" />
                      <span>Pay ₹{booking.totalAmount?.toLocaleString('en-IN')} via Razorpay Gateway</span>
                    </>
                  )}
                </motion.button>
              )}

              <motion.button
                whileTap={{ scale: 0.98 }}
                type="button"
                onClick={onClose}
                disabled={paymentSuccess}
                className="w-full py-2.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-300 hover:text-white text-xs font-bold transition border border-white/10"
              >
                Cancel & Review Itinerary
              </motion.button>
            </div>

            {/* Security Badges */}
            <div className="pt-2 border-t border-white/5 flex items-center justify-center gap-3 text-[10px] text-slate-400">
              <span className="flex items-center gap-1 text-emerald-400">
                <ShieldCheck className="w-3.5 h-3.5" />
                PCI-DSS Level 1
              </span>
              <span>•</span>
              <span>256-Bit SSL Encrypted</span>
              <span>•</span>
              <span>Instant Refund Guarantee</span>
            </div>
          </>
        )}
      </motion.div>
    </div>
  );
};
