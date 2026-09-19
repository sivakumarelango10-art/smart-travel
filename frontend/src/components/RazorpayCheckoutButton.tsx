import React, { useState } from 'react';
import { CreditCard, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';
import { paymentService } from '../services/paymentService';

interface RazorpayCheckoutButtonProps {
  amountPaise?: number; // default: 100 paise (1 INR)
  currency?: string;
  name?: string;
  description?: string;
  receipt?: string;
  prefill?: {
    name?: string;
    email?: string;
    contact?: string;
  };
  onSuccess?: (paymentData: {
    razorpay_order_id: string;
    razorpay_payment_id: string;
    razorpay_signature: string;
  }) => void;
  onError?: (errorMessage: string) => void;
  className?: string;
  buttonText?: string;
}

/**
 * Razorpay Standard Web Checkout Component.
 * Implements the 3-step Standard Checkout integration flow:
 * 1. Calls POST /api/create-order on backend to provision Razorpay order
 * 2. Opens official Razorpay modal via https://checkout.razorpay.com/v1/checkout.js
 * 3. On success, calls POST /api/verify-payment to cryptographically verify HMAC-SHA256 signature
 */
export const RazorpayCheckoutButton: React.FC<RazorpayCheckoutButtonProps> = ({
  amountPaise = 100, // 100 paise = ₹1.00
  currency = 'INR',
  name = 'SmartTravel Platform',
  description = 'Standard Web Checkout Test',
  receipt,
  prefill = {
    name: 'Test Traveler',
    email: 'traveler@smarttravel.com',
    contact: '9876543210',
  },
  onSuccess,
  onError,
  className = '',
  buttonText,
}) => {
  const [loading, setLoading] = useState(false);
  const [paymentStatus, setPaymentStatus] = useState<'idle' | 'success' | 'failed'>('idle');
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [paymentDetails, setPaymentDetails] = useState<{
    orderId?: string;
    paymentId?: string;
  } | null>(null);

  const displayAmount = (amountPaise / 100).toLocaleString('en-IN', {
    style: 'currency',
    currency: currency,
  });

  const loadRazorpayScript = (): Promise<boolean> => {
    return new Promise((resolve) => {
      if ((window as any).Razorpay) {
        resolve(true);
        return;
      }
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });
  };

  const handleCheckout = async () => {
    setLoading(true);
    setPaymentStatus('idle');
    setStatusMessage(null);

    try {
      // 1. Ensure Razorpay Checkout SDK script is loaded
      const scriptLoaded = await loadRazorpayScript();
      if (!scriptLoaded) {
        throw new Error('Razorpay Checkout SDK failed to load. Please check your internet connection.');
      }

      // 2. Step 1 (Backend): Create Order on /api/create-order
      const order = await paymentService.createStandardOrder({
        amount: Math.max(100, amountPaise), // minimum 100 paise
        currency,
        receipt: receipt || `rcpt_${Date.now()}`,
        notes: {
          platform: 'SmartTravel',
          checkoutType: 'Standard Web Checkout',
        },
      });

      const keyId =
        order.key_id ||
        (import.meta.env.VITE_RAZORPAY_KEY_ID as string) ||
        'rzp_test_TdmwlBNwLKKPnN';

      // 3. Step 2 (Frontend): Open Razorpay Standard Checkout Modal
      const options = {
        key: keyId,
        amount: order.amount,
        currency: order.currency || 'INR',
        name: name,
        description: description,
        order_id: order.order_id,
        prefill: {
          name: prefill.name || 'Test Traveler',
          email: prefill.email || 'traveler@smarttravel.com',
          contact: prefill.contact || '9876543210',
        },
        theme: {
          color: '#F59E0B',
        },
        modal: {
          ondismiss: function () {
            setLoading(false);
            setStatusMessage('Checkout window closed by user.');
          },
        },
        handler: async function (response: {
          razorpay_order_id: string;
          razorpay_payment_id: string;
          razorpay_signature: string;
        }) {
          try {
            // 4. Step 3 (Backend): Verify Signature on /api/verify-payment
            const verifyResult = await paymentService.verifyStandardPayment({
              razorpay_order_id: response.razorpay_order_id,
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_signature: response.razorpay_signature,
            });

            if (verifyResult.success) {
              setPaymentStatus('success');
              setStatusMessage('Payment verified successfully!');
              setPaymentDetails({
                orderId: response.razorpay_order_id,
                paymentId: response.razorpay_payment_id,
              });
              if (onSuccess) {
                onSuccess(response);
              }
            } else {
              setPaymentStatus('failed');
              const msg = verifyResult.message || 'Signature verification failed.';
              setStatusMessage(msg);
              if (onError) onError(msg);
            }
          } catch (verifyErr: any) {
            setPaymentStatus('failed');
            const msg = verifyErr?.response?.data?.message || verifyErr.message || 'Payment signature verification failed';
            setStatusMessage(msg);
            if (onError) onError(msg);
          } finally {
            setLoading(false);
          }
        },
      };

      const razorpayInstance = new (window as any).Razorpay(options);

      // Handle payment decline / failed event
      razorpayInstance.on('payment.failed', function (resp: any) {
        setPaymentStatus('failed');
        const errDesc = resp?.error?.description || 'Payment was declined by payment gateway.';
        setStatusMessage(errDesc);
        if (onError) onError(errDesc);
        setLoading(false);
      });

      razorpayInstance.open();
    } catch (err: any) {
      setLoading(false);
      setPaymentStatus('failed');
      const errorMsg =
        err?.response?.data?.error ||
        err?.response?.data?.message ||
        err.message ||
        'Failed to initialize Razorpay checkout';
      setStatusMessage(errorMsg);
      if (onError) onError(errorMsg);
    }
  };

  return (
    <div className="inline-flex flex-col items-start gap-2">
      <button
        type="button"
        onClick={handleCheckout}
        disabled={loading}
        className={
          className ||
          'px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-bold text-xs sm:text-sm shadow-md transition flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer'
        }
      >
        {loading ? (
          <>
            <Loader2 className="w-4 h-4 animate-spin text-black" />
            <span>Connecting to Razorpay...</span>
          </>
        ) : (
          <>
            <CreditCard className="w-4 h-4 text-black" />
            <span>{buttonText || `Pay with Razorpay (${displayAmount})`}</span>
          </>
        )}
      </button>

      {/* Success Notification */}
      {paymentStatus === 'success' && (
        <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-400" />
          <div>
            <p className="font-bold">{statusMessage}</p>
            {paymentDetails && (
              <p className="text-[10px] text-emerald-300/80 font-mono">
                Order: {paymentDetails.orderId} | Payment: {paymentDetails.paymentId}
              </p>
            )}
          </div>
        </div>
      )}

      {/* Error Notification */}
      {paymentStatus === 'failed' && statusMessage && (
        <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
          <span>{statusMessage}</span>
        </div>
      )}
    </div>
  );
};
