export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED' | 'EXPIRED';

export type RefundStatus = 'INITIATED' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'REJECTED';

export interface PaymentOrder {
  paymentId: string;
  razorpayOrderId: string;
  amount: number;
  currency: string;
  keyId?: string;
  status: PaymentStatus;
}

export interface PaymentOrderRequest {
  bookingId: string;
  notes?: string;
}

export interface RefundDetails {
  id: string;
  refundNumber: string;
  paymentId: string;
  bookingId: string;
  userId: string;
  amount: number;
  currency: string;
  reason: string;
  status: RefundStatus;
  gatewayRefundId?: string;
  processedAt?: string;
  createdAt: string;
}
