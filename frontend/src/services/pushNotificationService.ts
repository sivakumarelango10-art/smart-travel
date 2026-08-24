import { apiClient } from './api';
import { ApiResponse } from '../types/api';

export interface PlatformInfo {
  os: 'iOS' | 'Android' | 'Windows' | 'macOS' | 'Linux' | 'Other';
  browser: 'Safari' | 'Chrome' | 'Edge' | 'Firefox' | 'Opera' | 'Samsung Internet' | 'Other';
  isStandalone: boolean;
  isIOS: boolean;
  isAndroid: boolean;
  isWindows: boolean;
  isMacOS: boolean;
  isSafari: boolean;
  isPushSupported: boolean;
  permission: NotificationPermission;
  requiresHomeScreenPWA: boolean;
}

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
   * Comprehensive Platform and Browser Inspector for Push Capabilities
   */
  getPlatformInfo(): PlatformInfo {
    if (typeof window === 'undefined' || typeof navigator === 'undefined') {
      return {
        os: 'Other',
        browser: 'Other',
        isStandalone: false,
        isIOS: false,
        isAndroid: false,
        isWindows: false,
        isMacOS: false,
        isSafari: false,
        isPushSupported: false,
        permission: 'default',
        requiresHomeScreenPWA: false,
      };
    }

    const ua = navigator.userAgent;
    const isIOS = /iPad|iPhone|iPod/.test(ua) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
    const isAndroid = /Android/i.test(ua);
    const isWindows = /Windows/i.test(ua);
    const isMacOS = /Macintosh|Mac OS X/i.test(ua) && !isIOS;
    const isLinux = /Linux/i.test(ua) && !isAndroid;

    let os: PlatformInfo['os'] = 'Other';
    if (isIOS) os = 'iOS';
    else if (isAndroid) os = 'Android';
    else if (isWindows) os = 'Windows';
    else if (isMacOS) os = 'macOS';
    else if (isLinux) os = 'Linux';

    let browser: PlatformInfo['browser'] = 'Other';
    const isSamsung = /SamsungBrowser/i.test(ua);
    const isOpera = /OPR|Opera/i.test(ua);
    const isEdge = /Edg/i.test(ua);
    const isChrome = /Chrome|CriOS/i.test(ua) && !isEdge && !isOpera && !isSamsung;
    const isFirefox = /Firefox|FxiOS/i.test(ua);
    const isSafari = /^((?!chrome|android).)*safari/i.test(ua) || (isIOS && !isChrome && !isFirefox);

    if (isSamsung) browser = 'Samsung Internet';
    else if (isOpera) browser = 'Opera';
    else if (isEdge) browser = 'Edge';
    else if (isChrome) browser = 'Chrome';
    else if (isFirefox) browser = 'Firefox';
    else if (isSafari) browser = 'Safari';

    // Standalone check (PWA installed to home screen or desktop window)
    const isStandalone =
      window.matchMedia('(display-mode: standalone)').matches ||
      (navigator as any).standalone === true ||
      (window as any).navigator.standalone === true;

    // Push support check
    const isPushSupported =
      'serviceWorker' in navigator &&
      'PushManager' in window &&
      'Notification' in window;

    // iOS 16.4+ requires PWA installation ("Add to Home Screen") to enable PushManager
    const requiresHomeScreenPWA = isIOS && !isStandalone;

    const permission: NotificationPermission =
      'Notification' in window ? Notification.permission : 'denied';

    return {
      os,
      browser,
      isStandalone,
      isIOS,
      isAndroid,
      isWindows,
      isMacOS,
      isSafari,
      isPushSupported,
      permission,
      requiresHomeScreenPWA,
    };
  },

  /**
   * Check if current browser supports Web Push notifications.
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
   * Register or verify Service Worker is active.
   */
  async registerServiceWorker(): Promise<ServiceWorkerRegistration | null> {
    if (typeof window === 'undefined' || !('serviceWorker' in navigator)) {
      return null;
    }
    try {
      const registration = await navigator.serviceWorker.register('/sw.js', {
        scope: '/',
      });
      await navigator.serviceWorker.ready;
      return registration;
    } catch (err) {
      console.warn('Service worker registration failed:', err);
      return null;
    }
  },

  /**
   * Request permission and subscribe browser/device to Web Push notifications.
   */
  async subscribe(): Promise<boolean> {
    const platform = this.getPlatformInfo();

    if (platform.requiresHomeScreenPWA) {
      throw new Error(
        'On iOS devices, Web Push requires adding SmartTravel to your Home Screen. Tap Share (square with arrow) and select "Add to Home Screen", then open the app.'
      );
    }

    if (!this.isPushSupported()) {
      throw new Error(
        `Push notifications are not supported in ${platform.browser} on ${platform.os}. In-app live alerts will be active instead.`
      );
    }

    // 1. Request Notification Permission
    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      return false;
    }

    // 2. Ensure Service Worker is registered and active
    const registration = await this.registerServiceWorker();
    if (!registration) {
      throw new Error('Could not initialize service worker for push notifications');
    }

    // 3. Retrieve VAPID Public Key from backend
    const keyRes = await apiClient.get<ApiResponse<{ publicKey: string }>>(
      '/v1/notifications/push/public-key'
    );
    const vapidKey = keyRes.data.data?.publicKey;
    if (!vapidKey) {
      throw new Error('Failed to retrieve VAPID public key from backend server');
    }

    const applicationServerKey = urlBase64ToUint8Array(vapidKey);

    // 4. Register Push Subscription with browser's push service (FCM, Apple APNs Web Push, Mozilla autopush, Windows WNS)
    let subscription = await registration.pushManager.getSubscription();
    if (!subscription) {
      subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: applicationServerKey as unknown as BufferSource,
      });
    }

    const json = subscription.toJSON();
    const endpoint = json.endpoint;
    const p256dhKey = json.keys?.p256dh;
    const authKey = json.keys?.auth;

    if (!endpoint) {
      throw new Error('Subscription did not yield a valid push endpoint');
    }

    // 5. Send registration payload to backend
    await apiClient.post<ApiResponse<any>>('/v1/notifications/push/subscribe', {
      endpoint,
      p256dhKey,
      authKey,
      userAgent: `${navigator.userAgent} [${platform.os} - ${platform.browser}${platform.isStandalone ? ' PWA' : ''}]`,
    });

    return true;
  },

  /**
   * Unsubscribe browser/device from Web Push.
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
   * Trigger a test notification from the backend to verify end-to-end delivery.
   */
  async sendTestPush(): Promise<void> {
    await apiClient.post<ApiResponse<void>>('/v1/notifications/push/test');
  },

  /**
   * Display a local client-side notification if permissions are granted.
   */
  async showLocalNotification(title: string, options?: NotificationOptions): Promise<void> {
    if (typeof window === 'undefined' || !('Notification' in window)) return;
    if (Notification.permission !== 'granted') return;

    try {
      if ('serviceWorker' in navigator) {
        const reg = await navigator.serviceWorker.ready;
        reg.showNotification(title, {
          icon: '/logo.png',
          badge: '/logo.png',
          ...options,
        });
      } else {
        new Notification(title, {
          icon: '/logo.png',
          ...options,
        });
      }
    } catch (e) {
      console.warn('Could not display local notification', e);
    }
  },
};
