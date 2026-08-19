import { apiClient } from './api';
import { ApiResponse } from '../types/api';
import { AdminRefund, RefundStatus, RefundReason, RefundProcessRequest, RefundEligibilityResponse, PageResponse } from '../types/admin';

/**
 * Admin Refund Service — integrates with /api/v1/admin/refunds (ROLE_ADMIN required)
 */
export const adminRefundService = {
  async getAllRefunds(page = 0, size = 20, status?: RefundStatus): Promise<ApiResponse<PageResponse<AdminRefund>>> {
    const params: Record<string, unknown> = { page, size, sort: 'createdAt,desc' };
    if (status) params.status = status;
    const res = await apiClient.get<ApiResponse<PageResponse<AdminRefund>>>('/v1/admin/refunds', { params });
    return res.data;
  },

  async checkEligibility(paymentId: string, reason: RefundReason = 'FLIGHT_CANCELLED'): Promise<ApiResponse<RefundEligibilityResponse>> {
    const res = await apiClient.get<ApiResponse<RefundEligibilityResponse>>(`/v1/admin/refunds/${paymentId}/eligibility`, {
      params: { reason },
    });
    return res.data;
  },

  async processRefund(paymentId: string, request?: RefundProcessRequest): Promise<ApiResponse<AdminRefund>> {
    const res = await apiClient.post<ApiResponse<AdminRefund>>(`/v1/admin/refunds/${paymentId}/process`, request ?? {});
    return res.data;
  },
};
