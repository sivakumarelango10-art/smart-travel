import { apiClient } from './api';
import { ApiResponse, PaymentOrder, PaymentOrderRequest, RefundDetails } from '../types/api';

export const paymentService = {
  async createPaymentOrder(request: PaymentOrderRequest): Promise<ApiResponse<PaymentOrder>> {
    const res = await apiClient.post<ApiResponse<PaymentOrder>>('/v1/payments/orders', request);
    return res.data;
  },

  async getPaymentByBooking(bookingId: string): Promise<ApiResponse<any>> {
    const res = await apiClient.get<ApiResponse<any>>(`/v1/payments/booking/${bookingId}`);
    return res.data;
  },

  async getRefundByBooking(bookingId: string): Promise<ApiResponse<RefundDetails>> {
    const res = await apiClient.get<ApiResponse<RefundDetails>>(`/v1/bookings/${bookingId}/refund`);
    return res.data;
  },

  async verifyPayment(request: {
    razorpayOrderId: string;
    razorpayPaymentId: string;
    razorpaySignature: string;
  }): Promise<ApiResponse<any>> {
    const res = await apiClient.post<ApiResponse<any>>('/v1/payments/verify', request);
    return res.data;
  },

  async simulateWebhookPayment(razorpayOrderId: string, amountPaise: number): Promise<ApiResponse<any>> {
    const eventId = 'evt_' + Math.random().toString(36).substring(2, 12);
    const paymentId = 'pay_' + Math.random().toString(36).substring(2, 12);
    const payload = {
      event_id: eventId,
      event: 'payment.captured',
      payload: {
        payment: {
          entity: {
            id: paymentId,
            order_id: razorpayOrderId,
            amount: amountPaise,
            currency: 'INR',
            status: 'captured',
          },
        },
      },
    };

    const res = await apiClient.post<ApiResponse<any>>('/v1/payments/webhook', payload, {
      headers: {
        'X-Razorpay-Signature': 'sim_sig_' + paymentId,
      },
    });
    return res.data;
  },
};
