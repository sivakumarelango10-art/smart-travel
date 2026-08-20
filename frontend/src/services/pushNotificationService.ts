import { apiClient } from './api';
import { ApiResponse } from '../types/api';

function urlBase64ToUint8Array(base64String: string): Uint8Array {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const rawData = window.atob(base64);
  const outputArray = new Uint8Array(rawData.length);
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
}

export const pushNotificationService = {
  /**
   * Check if the browser supports Service Worker and Push API.
   */
  isPushSupported(): boolean {
    return (
      typeof window !== 'undefined' &&
      'serviceWorker' in navigator &&
      'PushManager' in window &&
      'Notification' in window
    );
  },

  /**
   * Check current notification permission status.
   */
  getPermission(): NotificationPermission {
    if (typeof window === 'undefined' || !('Notification' in window)) {
      return 'denied';
    }
    return Notification.permission;
  },

  /**
   * Request permission and subscribe browser to Web Push notifications.
   */
  async subscribe(): Promise<boolean> {
    if (!this.isPushSupported()) {
      throw new Error('Push notifications are not supported by your browser');
    }

    // 1. Request permission
    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      return false;
    }

    // 2. Register Service Worker
    const registration = await navigator.serviceWorker.register('/sw.js');
    await navigator.serviceWorker.ready;

    // 3. Fetch VAPID Public Key from backend
    const keyRes = await apiClient.get<ApiResponse<{ publicKey: string }>>(
      '/v1/notifications/push/public-key'
    );
    const vapidKey = keyRes.data.data?.publicKey;
    if (!vapidKey) {
      throw new Error('Failed to retrieve VAPID public key from backend');
    }

    const applicationServerKey = urlBase64ToUint8Array(vapidKey);

    // 4. Subscribe with PushManager
    const subscription = await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: applicationServerKey as unknown as BufferSource,
    });

    const json = subscription.toJSON();
    const endpoint = json.endpoint;
    const p256dhKey = json.keys?.p256dh;
    const authKey = json.keys?.auth;

    if (!endpoint) {
      throw new Error('Subscription did not yield an endpoint');
    }

    // 5. Send subscription to backend
    await apiClient.post<ApiResponse<any>>('/v1/notifications/push/subscribe', {
      endpoint,
      p256dhKey,
      authKey,
      userAgent: navigator.userAgent,
    });

    return true;
  },

  /**
   * Unsubscribe browser from Web Push.
   */
  async unsubscribe(): Promise<void> {
    if (!this.isPushSupported()) return;

    try {
      const registration = await navigator.serviceWorker.ready;
      const subscription = await registration.pushManager.getSubscription();
      if (subscription) {
        await apiClient.post<ApiResponse<void>>(
          `/v1/notifications/push/unsubscribe?endpoint=${encodeURIComponent(subscription.endpoint)}`
        );
        await subscription.unsubscribe();
      }
    } catch (err) {
      console.warn('Failed to cleanly unsubscribe from push', err);
    }
  },

  /**
   * Check if current browser is actively subscribed.
   */
  async isSubscribed(): Promise<boolean> {
    if (!this.isPushSupported()) return false;
    try {
      const registration = await navigator.serviceWorker.ready;
      const subscription = await registration.pushManager.getSubscription();
      return !!subscription;
    } catch {
      return false;
    }
  },

  /**
   * Trigger a test notification from the backend.
   */
  async sendTestPush(): Promise<void> {
    await apiClient.post<ApiResponse<void>>('/v1/notifications/push/test');
  },
};
