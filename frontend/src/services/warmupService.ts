import { apiClient } from './api';

let isWarming = false;
let isWarm = false;
let warmupInterval: any = null;

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
 * as soon as the user loads the application.
 */
export const warmupBackend = async (): Promise<boolean> => {
  if (isWarming || isWarm) return isWarm;
  isWarming = true;

  try {
    const res = await apiClient.get('/v1/health', {
      timeout: 60000,
    });
    if (res.status === 200) {
      isWarm = true;
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
 * Starts a 2.5-minute keep-alive heartbeat while the user is actively browsing
 * to prevent cloud server instances from sleeping.
 */
export const startKeepAliveHeartbeat = () => {
  // Trigger initial warmup & SDK preloading immediately
  warmupBackend();
  preloadPaymentSdk();

  if (warmupInterval) return;

  warmupInterval = setInterval(() => {
    // Only send keep-alive when document is visible
    if (document.visibilityState === 'visible') {
      apiClient.get('/v1/health', { timeout: 15000 }).catch(() => {});
    }
  }, 2.5 * 60 * 1000); // every 2.5 minutes
};

export const stopKeepAliveHeartbeat = () => {
  if (warmupInterval) {
    clearInterval(warmupInterval);
    warmupInterval = null;
  }
};
