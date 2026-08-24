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
    throw new Error(res.message || 'Failed to initialize payment gateway order.');
  };

  const handleInstantTestPayment = async () => {
    try {
      setPayLoading(true);
      setError(null);

      // Use simulateWebhookPayment to trigger payment completion
      const activeOrder = await ensureActiveOrder();
      const res = await paymentService.simulateWebhookPayment(
        activeOrder.razorpayOrderId,
        Math.round((booking.totalAmount || 0) * 100)
      );

      if (res.success) {
        setPaymentSuccess(true);
        setTimeout(() => {
          onPaymentSuccess();
        }, 1500);
      } else {
        throw new Error(res.message || 'Payment simulation declined.');
      }
    } catch (err: any) {
      setError(err?.message || 'Payment processing failed. Please try again.');
    } finally {
      setPayLoading(false);
    }
  };

  const handleRazorpayCheckout = async () => {
    try {
      setPayLoading(true);
      setError(null);

      const scriptLoaded = await loadRazorpayScript();
      if (!scriptLoaded) {
        throw new Error('Razorpay SDK failed to load. Please use Instant 1-Click Payment.');
      }

      const activeOrder = await ensureActiveOrder();

      const options = {
        key: activeOrder.keyId || import.meta.env.VITE_RAZORPAY_KEY_ID || 'rzp_test_placeholder',
        amount: Math.round((booking.totalAmount || 0) * 100),
        currency: 'INR',
        name: 'SmartTravel Inc.',
        description: `PNR: ${booking.bookingReference} • ${booking.airline}`,
        image: '/logo.png',
        order_id: activeOrder.razorpayOrderId,
        handler: async (response: any) => {
          try {
            setPayLoading(true);
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
              throw new Error(verifyRes.message || 'Payment verification failed.');
            }
          } catch (verErr: any) {
            setError(verErr?.message || 'Payment signature verification failed.');
          } finally {
            setPayLoading(false);
          }
        },
        prefill: {
          name: booking.passengers?.[0]?.firstName ? `${booking.passengers[0].firstName} ${booking.passengers[0].lastName || ''}` : 'Traveler',
          email: 'traveler@smarttravel.com',
          contact: '9999999999',
        },
        theme: {
          color: '#0F172A',
        },
        modal: {
          ondismiss: () => {
            setPayLoading(false);
          },
        },
      };

      const razorpayInstance = new (window as any).Razorpay(options);
      razorpayInstance.on('payment.failed', (resp: any) => {
        setError(resp.error?.description || 'Transaction declined by bank.');
        setPayLoading(false);
      });

      razorpayInstance.open();
    } catch (err: any) {
      setError(err?.message || 'Payment gateway initialization failed.');
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
      className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 bg-slate-950/70 backdrop-blur-sm animate-fade-in"
    >
      <div className="w-full max-w-lg rounded-2xl bg-white border border-slate-200 shadow-2xl p-6 sm:p-7 space-y-5 relative">
        <button
          type="button"
          onClick={onClose}
          disabled={paymentSuccess}
          className="absolute top-5 right-5 p-2 rounded-xl text-slate-400 hover:text-primary bg-slate-100 hover:bg-slate-200 transition disabled:opacity-30 cursor-pointer z-10"
          aria-label="Close Payment Modal"
        >
          <X className="w-4 h-4" />
        </button>

        {paymentSuccess ? (
          <div className="py-10 text-center space-y-4 animate-scale-in">
            <div className="w-16 h-16 rounded-2xl bg-emerald-50 text-emerald-600 border border-emerald-200 flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-9 h-9 stroke-[2.5]" />
            </div>
            <div className="space-y-1">
              <h2 className="text-2xl font-black text-primary tracking-tight">Payment Verified!</h2>
              <p className="text-xs text-slate-600 max-w-xs mx-auto">
                Your flight reservation has been atomically confirmed.
              </p>
            </div>
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-emerald-50 text-emerald-700 text-xs font-bold border border-emerald-200">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Issuing E-Ticket & Boarding Pass...</span>
            </div>
          </div>
        ) : (
          <>
            {/* Modal Header */}
            <div className="flex items-start justify-between pr-8">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-primary text-white flex items-center justify-center font-bold">
                  <CreditCard className="w-5 h-5 text-secondary" />
                </div>
                <div>
                  <h2 className="font-black text-primary text-base">Secure Checkout</h2>
                  <p className="text-xs text-slate-500 flex items-center gap-1.5 mt-0.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                    256-Bit Encrypted Authoritative Payment
                  </p>
                </div>
              </div>

              {/* Timer Pill */}
              <div className="flex items-center gap-1 text-xs font-mono font-bold text-accent bg-accent/10 border border-accent/20 px-2.5 py-1 rounded-lg">
                <Clock className="w-3.5 h-3.5" />
                <span>{formatTimer(timeLeft)}</span>
              </div>
            </div>

            {/* Flight Ticket Summary Box */}
            <div className="rounded-xl bg-slate-50 border border-slate-200 p-4 space-y-3">
              {/* Route & PNR */}
              <div className="flex items-center justify-between pb-3 border-b border-slate-200">
                <div className="flex items-center gap-3">
                  <div className="text-left">
                    <div className="text-lg font-black text-primary">
                      {booking.departureAirport?.code || 'DEL'}
                    </div>
                    <div className="text-[11px] text-slate-500">
                      {booking.departureAirport?.city || 'Delhi'}
                    </div>
                  </div>

                  <div className="flex flex-col items-center px-1">
                    <span className="text-[10px] font-bold text-secondary uppercase">
                      {booking.flightNumber || 'FLIGHT'}
                    </span>
                    <Plane className="w-3.5 h-3.5 text-secondary rotate-90 my-0.5" />
                  </div>

                  <div className="text-right">
                    <div className="text-lg font-black text-primary">
                      {booking.arrivalAirport?.code || 'BOM'}
                    </div>
                    <div className="text-[11px] text-slate-500">
                      {booking.arrivalAirport?.city || 'Mumbai'}
                    </div>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={handleCopyPnr}
                  className="flex items-center gap-1.5 bg-white hover:bg-slate-100 border border-slate-200 px-2.5 py-1 rounded-lg text-xs transition"
                  title="Click to copy PNR"
                >
                  <span className="text-[10px] text-slate-500 uppercase font-semibold">PNR:</span>
                  <span className="font-mono font-bold text-primary">{booking.bookingReference}</span>
                  {copiedPnr ? <Check className="w-3 h-3 text-emerald-600" /> : <Copy className="w-3 h-3 text-slate-400" />}
                </button>
              </div>

              {/* Lead Traveler & Total */}
              <div className="flex items-center justify-between text-xs">
                <span className="text-slate-600">Traveler: <strong className="text-primary">{leadPassenger}</strong></span>
                <span className="font-bold text-primary bg-white px-2 py-0.5 rounded border border-slate-200 uppercase text-[10px]">
                  {booking.cabinClass || 'Economy'}
                </span>
              </div>

              <div className="pt-2 border-t border-slate-200 flex items-baseline justify-between">
                <div>
                  <span className="text-[10px] font-bold uppercase text-slate-400 block">Total Amount Due</span>
                  <span className="text-[10px] text-slate-500 flex items-center gap-1">
                    <ShieldCheck className="w-3 h-3 text-emerald-600" /> All taxes included
                  </span>
                </div>
                <div className="text-2xl font-black text-primary">
                  ₹{booking.totalAmount?.toLocaleString('en-IN')}
                </div>
              </div>
            </div>

            {/* Payment Method Switcher Tabs */}
            <div className="grid grid-cols-2 gap-2 p-1 rounded-xl bg-slate-100 border border-slate-200">
              <button
                type="button"
                onClick={() => {
                  setPaymentMethodTab('instant');
                  setError(null);
                }}
                className={`py-2 px-3 rounded-lg text-xs font-bold flex items-center justify-center gap-1.5 transition ${
                  paymentMethodTab === 'instant'
                    ? 'bg-white text-primary shadow-sm'
                    : 'text-slate-600 hover:text-primary'
                }`}
              >
                <Zap className="w-3.5 h-3.5 text-accent" />
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
                    ? 'bg-white text-primary shadow-sm'
                    : 'text-slate-600 hover:text-primary'
                }`}
              >
                <CreditCard className="w-3.5 h-3.5 text-secondary" />
                <span>Razorpay Gateway</span>
              </button>
            </div>

            {error && (
              <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs space-y-2">
                <div className="flex items-center gap-2 font-medium">
                  <AlertCircle className="w-4 h-4 text-rose-500 shrink-0" />
                  <span>{error}</span>
                </div>
                <button
                  type="button"
                  onClick={handleInstantTestPayment}
                  disabled={payLoading}
                  className="w-full py-2 px-3 rounded-lg bg-primary hover:bg-primary-hover text-white font-bold text-xs flex items-center justify-center gap-2 transition"
                >
                  <Zap className="w-3.5 h-3.5 text-accent" />
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
                  className="w-full py-3.5 rounded-xl bg-accent hover:bg-accent-hover text-white font-black text-sm shadow-md transition flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
                >
                  {payLoading ? (
                    <span className="flex items-center gap-2 text-white">
                      <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                      <span>Verifying & Issuing Ticket...</span>
                    </span>
                  ) : (
                    <>
                      <Zap className="w-4 h-4" />
                      <span>Instant 1-Click Pay ₹{booking.totalAmount?.toLocaleString('en-IN')} (Demo Sandbox)</span>
                    </>
                  )}
                </button>
              ) : (
                <button
                  type="button"
                  disabled={payLoading || timeLeft === 0}
                  onClick={handleRazorpayCheckout}
                  className="w-full py-3.5 rounded-xl bg-primary hover:bg-primary-hover text-white font-black text-sm shadow-md transition flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
                >
                  {payLoading ? (
                    <span className="flex items-center gap-2 text-white">
                      <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                      <span>Opening Razorpay Modal...</span>
                    </span>
                  ) : (
                    <>
                      <Lock className="w-4 h-4" />
                      <span>Pay ₹{booking.totalAmount?.toLocaleString('en-IN')} via Razorpay Gateway</span>
                    </>
                  )}
                </button>
              )}

              <button
                type="button"
                onClick={onClose}
                disabled={paymentSuccess}
                className="w-full py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold transition border border-slate-200"
              >
                Cancel & Review Itinerary
              </button>
            </div>

            {/* Security Badges */}
            <div className="pt-2 border-t border-slate-100 flex items-center justify-center gap-3 text-[10px] text-slate-400">
              <span className="flex items-center gap-1 text-emerald-600">
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
