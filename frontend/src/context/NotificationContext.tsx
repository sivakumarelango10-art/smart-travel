import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { Notification } from '../types/api';
import { notificationService } from '../services/notificationService';
import { flightStatusWebSocketManager } from '../services/flightStatusWebSocketManager';
import { notify } from '../utils/toast';
import { useAuth } from './AuthContext';

interface NotificationContextType {
  notifications: Notification[];
  unreadCount: number;
  loading: boolean;
  fetchNotifications: () => Promise<void>;
  markAsRead: (id: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, user } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(false);

  const normalizeNotification = useCallback((raw: any): Notification => {
    const isRead = raw.isRead ?? raw.read ?? false;
    const title = raw.title || raw.subject || 'Notification';
    const message = raw.message || raw.content || '';
    const type = raw.type || raw.notificationType || 'BOOKING_CONFIRMED';
    return {
      ...raw,
      id: raw.id,
      userId: raw.userId,
      type,
      notificationType: raw.notificationType || type,
      title,
      subject: raw.subject || title,
      message,
      content: raw.content || message,
      priority: raw.priority || 'MEDIUM',
      isRead,
      read: isRead,
      createdAt: raw.createdAt || new Date().toISOString(),
    };
  }, []);

  const fetchNotifications = useCallback(async () => {
    if (!isAuthenticated) {
      setNotifications([]);
      setUnreadCount(0);
      setLoading(false);
      return;
    }
    try {
      setLoading(true);
      const [listRes, countRes] = await Promise.all([
        notificationService.getNotifications(0, 10),
        notificationService.getUnreadCount(),
      ]);
      if (listRes.success && listRes.data?.content) {
        setNotifications(listRes.data.content.map(normalizeNotification));
      }
      if (countRes.success && countRes.data) {
        setUnreadCount(countRes.data.unreadCount ?? 0);
      }
    } catch {
      // Gracefully ignore notification fetch errors
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, normalizeNotification]);

  useEffect(() => {
    fetchNotifications();
    if (!isAuthenticated) return;

    // Visibility-aware polling: background safety fallback
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        fetchNotifications();
      }
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);

    const interval = setInterval(() => {
      if (document.visibilityState === 'visible') {
        fetchNotifications();
      }
    }, 30000);

    return () => {
      clearInterval(interval);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [fetchNotifications, isAuthenticated]);

  // Ultra Real-Time WebSocket Push Subscription
  useEffect(() => {
    if (!isAuthenticated || !user?.id) return;

    const handleRealtimeNotification = (incoming: any) => {
      if (!incoming) return;
      const normalized = normalizeNotification(incoming);

      setNotifications((prev) => {
        if (prev.some((n) => n.id === normalized.id)) {
          return prev;
        }
        if (!normalized.isRead) {
          setUnreadCount((c) => c + 1);
          notify(normalized.title, normalized.message, 'INFO');
        }
        return [normalized, ...prev];
      });
    };

    const unsubscribe = flightStatusWebSocketManager.subscribeNotifications(
      user.id,
      handleRealtimeNotification
    );

    return () => {
      unsubscribe();
    };
  }, [isAuthenticated, user?.id, normalizeNotification]);

  const markAsRead = useCallback(async (id: string) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true, read: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark notification as read', err);
    }
  }, []);

  const markAllAsRead = useCallback(async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true, read: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to mark all as read', err);
    }
  }, []);

  const value = React.useMemo(() => ({
    notifications,
    unreadCount,
    loading,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
  }), [notifications, unreadCount, loading, fetchNotifications, markAsRead, markAllAsRead]);

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = (): NotificationContextType => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};
