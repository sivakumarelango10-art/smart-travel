import { apiClient } from './api';
import { ApiResponse, Ticket } from '../types/api';

export const ticketService = {
  async getTicketById(ticketId: string): Promise<ApiResponse<Ticket>> {
    const res = await apiClient.get<ApiResponse<Ticket>>(`/v1/tickets/${ticketId}`);
    return res.data;
  },

  async getTicketByBookingId(bookingId: string): Promise<ApiResponse<Ticket>> {
    const res = await apiClient.get<ApiResponse<Ticket>>(`/v1/tickets/booking/${bookingId}`);
    return res.data;
  },

  async getTicketByNumber(ticketNumber: string): Promise<ApiResponse<Ticket>> {
    const res = await apiClient.get<ApiResponse<Ticket>>(`/v1/tickets/number/${ticketNumber}`);
    return res.data;
  },

  async downloadTicketPdf(ticketId: string): Promise<Blob> {
    const res = await apiClient.get(`/v1/tickets/${ticketId}/pdf`, {
      responseType: 'blob',
    });
    return res.data;
  },
};
