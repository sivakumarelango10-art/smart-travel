import { apiClient } from './api';
import { ApiResponse, Notification, NotificationPageResponse } from '../types/api';

export interface UnreadCountDto {
  unreadCount: number;
}

export const notificationService = {
  async getNotifications(page = 0, size = 10): Promise<ApiResponse<NotificationPageResponse>> {
    const res = await apiClient.get<ApiResponse<NotificationPageResponse>>('/v1/notifications', {
      params: { page, size },
    });
    return res.data;
  },

  async getUnreadCount(): Promise<ApiResponse<UnreadCountDto>> {
    const res = await apiClient.get<ApiResponse<UnreadCountDto>>('/v1/notifications/unread-count');
    return res.data;
  },

  async markAsRead(notificationId: string): Promise<ApiResponse<Notification>> {
    const res = await apiClient.patch<ApiResponse<Notification>>(`/v1/notifications/${notificationId}/read`);
    return res.data;
  },

  async markAllAsRead(): Promise<ApiResponse<number>> {
    const res = await apiClient.patch<ApiResponse<number>>('/v1/notifications/read-all');
    return res.data;
  },
};
