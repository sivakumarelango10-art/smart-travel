import { apiClient } from './api';

let isWarming = false;
let isWarm = false;
let lastWarmTimestamp = 0;
let warmupInterval: any = null;

const WARM_TTL_MS = 3 * 60 * 1000; // 3 minutes before re-verifying

/**
 * Returns true if the backend has responded recently and is believed to be awake.
 */
export const isBackendWarm = (): boolean => {
  return isWarm && Date.now() - lastWarmTimestamp < WARM_TTL_MS;
};

/**
 * Preloads Razorpay checkout script in the background during idle time
 * so payment modals open instantly with zero network script fetch delay.
 */
export const preloadPaymentSdk = () => {
  if (typeof window === 'undefined' || (window as any).Razorpay) return;
  const link = document.createElement('link');
  link.rel = 'preload';
  link.as = 'script';
  link.href = 'https://checkout.razorpay.com/v1/checkout.js';
  document.head.appendChild(link);
};

/**
 * Proactively triggers a backend health check to wake up cloud instances
 * as soon as the user loads the application or focuses an authentication input.
 */
export const warmupBackend = async (force: boolean = false): Promise<boolean> => {
  if (!force && isBackendWarm()) return true;
  if (isWarming) return isWarm;
  isWarming = true;

  try {
    const res = await apiClient.get('/v1/health', {
      timeout: 90000, // 90s — Render cold start can take up to 60–90s
    });
    if (res.status === 200) {
      isWarm = true;
      lastWarmTimestamp = Date.now();
      window.dispatchEvent(new CustomEvent('backend:warm', { detail: { warm: true } }));
      return true;
    }
  } catch {
    // Non-blocking warmup attempt
  } finally {
    isWarming = false;
  }
  return false;
};

/**
 * Fast lightweight fire-and-forget ping triggered on user interaction (input focus, hover)
 * to wake up sleeping instances before the user even finishes typing credentials.
 */
export const warmupFastPing = () => {
  if (!isBackendWarm() && !isWarming) {
    warmupBackend();
  }
};

/**
 * Starts a 2-minute keep-alive heartbeat while the user is actively browsing
 * to prevent cloud server instances from sleeping.
 */
export const startKeepAliveHeartbeat = () => {
  // Trigger initial warmup & SDK preloading immediately
  warmupBackend();
  preloadPaymentSdk();

  // Proactively warm up search APIs in idle time so subsequent navigation is instantaneous
  setTimeout(() => {
    import('./hotelService').then((m) => m.hotelService.searchHotels({ page: 0, size: 12 })).catch(() => {});
    import('./flightService').then((m) => m.flightService.searchFlights({ origin: 'DEL', destination: 'BOM' })).catch(() => {});
  }, 1000);

  if (warmupInterval) return;

  warmupInterval = setInterval(() => {
    // Only send keep-alive when document is visible
    if (document.visibilityState === 'visible') {
      apiClient.get('/v1/health', { timeout: 15000 })
        .then(() => {
          isWarm = true;
          lastWarmTimestamp = Date.now();
        })
        .catch(() => {});
    }
  }, 10 * 60 * 1000); // every 10 minutes — Render sleeps at 15min inactivity

  // Re-ping immediately when user returns to the tab after being away
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible' && !isBackendWarm()) {
      warmupBackend();
    }
  });
};

export const stopKeepAliveHeartbeat = () => {
  if (warmupInterval) {
    clearInterval(warmupInterval);
    warmupInterval = null;
  }
};

