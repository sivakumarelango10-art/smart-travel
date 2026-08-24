import React, { useState, useEffect } from 'react';
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
    throw new Error(res.message || 'Unable to generate gateway order');
  };

  const handleInstantTestPayment = async () => {
    setPayLoading(true);
    setError(null);
    try {
      const activeOrder = await ensureActiveOrder();
      const res = await paymentService.simulateWebhookPayment(activeOrder.razorpayOrderId, activeOrder.amount);
      if (res.success) {
        setPaymentSuccess(true);
        setTimeout(() => {
          onPaymentSuccess();
        }, 1500);
      } else {
        setError(res.message || 'Payment simulation rejected');
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
              }, 1500);
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
    <div
      onClick={(e) => {
        if (e.target === e.currentTarget && !paymentSuccess) {
          onClose();
        }
      }}
      className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 bg-black/80 backdrop-blur-md animate-fade-in"
    >
      <div className="w-full max-w-lg rounded-3xl bg-[#14161F] border border-white/10 shadow-2xl p-6 sm:p-7 space-y-5 relative">
        <button
          type="button"
          onClick={onClose}
          disabled={paymentSuccess}
          className="absolute top-5 right-5 p-2 rounded-xl text-slate-400 hover:text-white bg-[#181A22] hover:bg-[#1F222E] border border-white/10 transition disabled:opacity-30 cursor-pointer z-10"
          aria-label="Close Payment Modal"
        >
          <X className="w-4 h-4" />
        </button>

        {paymentSuccess ? (
          <div className="py-10 text-center space-y-4 animate-scale-in">
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
          </div>
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

                  <div className="flex flex-col items-center px-1">
                    <span className="text-[10px] font-bold text-amber-400 uppercase">
                      {booking.flightNumber || 'FLIGHT'}
                    </span>
                    <Plane className="w-3.5 h-3.5 text-amber-400 rotate-90 my-0.5" />
                  </div>

                  <div className="text-right">
                    <div className="text-lg font-black text-white">
                      {booking.arrivalAirport?.code || 'BOM'}
                    </div>
                    <div className="text-[11px] text-slate-400">
                      {booking.arrivalAirport?.city || 'Mumbai'}
                    </div>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={handleCopyPnr}
                  className="flex items-center gap-1.5 bg-[#12131A] hover:bg-[#1F222E] border border-white/10 px-2.5 py-1 rounded-lg text-xs transition"
                  title="Click to copy PNR"
                >
                  <span className="text-[10px] text-slate-400 uppercase font-semibold">PNR:</span>
                  <span className="font-mono font-bold text-amber-400">{booking.bookingReference}</span>
                  {copiedPnr ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3 text-slate-400" />}
                </button>
              </div>

              {/* Lead Traveler & Total */}
              <div className="flex items-center justify-between text-xs">
                <span className="text-slate-300">Traveler: <strong className="text-white">{leadPassenger}</strong></span>
                <span className="font-bold text-amber-400 bg-amber-400/10 px-2 py-0.5 rounded border border-amber-400/20 uppercase text-[10px]">
                  {booking.cabinClass || 'Economy'}
                </span>
              </div>

              <div className="pt-2 border-t border-white/5 flex items-baseline justify-between">
                <div>
                  <span className="text-[10px] font-bold uppercase text-slate-400 block">Total Amount Due</span>
                  <span className="text-[10px] text-slate-400 flex items-center gap-1">
                    <ShieldCheck className="w-3 h-3 text-emerald-400" /> All taxes included
                  </span>
                </div>
                <div className="text-2xl font-black text-amber-400">
                  ₹{booking.totalAmount?.toLocaleString('en-IN')}
                </div>
              </div>
            </div>

            {/* Payment Method Switcher Tabs */}
            <div className="grid grid-cols-2 gap-2 p-1 rounded-xl bg-[#181A22] border border-white/10">
              <button
                type="button"
                onClick={() => {
                  setPaymentMethodTab('instant');
                  setError(null);
                }}
                className={`py-2 px-3 rounded-lg text-xs font-bold flex items-center justify-center gap-1.5 transition ${
                  paymentMethodTab === 'instant'
                    ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                    : 'text-slate-300 hover:text-white'
                }`}
              >
                <Zap className={`w-3.5 h-3.5 ${paymentMethodTab === 'instant' ? 'text-black' : 'text-amber-400'}`} />
                <span>Instant 1-Click (Demo)</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  setPaymentMethodTab('razorpay');
                  setError(null);
                }}
                className={`py-2 px-3 rounded-lg text-xs font-bold flex items-center justify-center gap-1.5 transition ${
                  paymentMethodTab === 'razorpay'
                    ? 'bg-gradient-to-r from-amber-400 to-amber-500 text-black shadow-glow-gold'
                    : 'text-slate-300 hover:text-white'
                }`}
              >
                <CreditCard className={`w-3.5 h-3.5 ${paymentMethodTab === 'razorpay' ? 'text-black' : 'text-amber-400'}`} />
                <span>Razorpay Gateway</span>
              </button>
            </div>

            {error && (
              <div className="p-3 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs space-y-2">
                <div className="flex items-center gap-2 font-medium">
                  <AlertCircle className="w-4 h-4 text-rose-400 shrink-0" />
                  <span>{error}</span>
                </div>
                <button
                  type="button"
                  onClick={handleInstantTestPayment}
                  disabled={payLoading}
                  className="w-full py-2 px-3 rounded-lg bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs flex items-center justify-center gap-2 transition shadow-glow-gold"
                >
                  <Zap className="w-3.5 h-3.5 text-black" />
                  <span>Complete with 1-Click Instant Demo Payment</span>
                </button>
              </div>
            )}

            {/* Action Button */}
            <div className="space-y-2 pt-1">
              {paymentMethodTab === 'instant' ? (
                <button
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
                </button>
              ) : (
                <button
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
                </button>
              )}

              <button
                type="button"
                onClick={onClose}
                disabled={paymentSuccess}
                className="w-full py-2.5 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-slate-300 hover:text-white text-xs font-bold transition border border-white/10"
              >
                Cancel & Review Itinerary
              </button>
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
      </div>
    </div>
  );
};
