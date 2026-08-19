import { apiClient } from './api';
import { ApiResponse } from '../types/api';
import { AdminTicket, PageResponse } from '../types/admin';

/**
 * Admin Ticket Service — integrates with /api/v1/admin/tickets (ROLE_ADMIN required)
 */
export const adminTicketService = {
  async getAllTickets(page = 0, size = 20): Promise<ApiResponse<PageResponse<AdminTicket>>> {
    const res = await apiClient.get<ApiResponse<PageResponse<AdminTicket>>>('/v1/admin/tickets', {
      params: { page, size, sort: 'issuedAt,desc' },
    });
    return res.data;
  },

  async getTicketById(id: string): Promise<ApiResponse<AdminTicket>> {
    const res = await apiClient.get<ApiResponse<AdminTicket>>(`/v1/admin/tickets/${id}`);
    return res.data;
  },

  async getTicketByNumber(ticketNumber: string): Promise<ApiResponse<AdminTicket>> {
    const res = await apiClient.get<ApiResponse<AdminTicket>>(`/v1/admin/tickets/number/${ticketNumber}`);
    return res.data;
  },

  async getTicketByBookingId(bookingId: string): Promise<ApiResponse<AdminTicket>> {
    const res = await apiClient.get<ApiResponse<AdminTicket>>(`/v1/admin/tickets/booking/${bookingId}`);
    return res.data;
  },

  async downloadTicketPdf(ticketId: string): Promise<Blob> {
    const res = await apiClient.get(`/v1/admin/tickets/${ticketId}/pdf`, {
      responseType: 'blob',
    });
    return res.data as Blob;
  },

  async retryIssueTicket(bookingId: string): Promise<ApiResponse<AdminTicket>> {
    const res = await apiClient.post<ApiResponse<AdminTicket>>(`/v1/admin/tickets/${bookingId}/issue`);
    return res.data;
  },
};
