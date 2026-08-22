import { apiClient } from './api';

let isWarming = false;
let isWarm = false;
let warmupInterval: any = null;

/**
 * Proactively triggers a backend health check to wake up Render/cloud instance
 * as soon as the user opens the application.
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
 * Starts a 4-minute keep-alive heartbeat while the user is actively browsing
 * to prevent the free-tier backend from spinning down into cold-standby.
 */
export const startKeepAliveHeartbeat = () => {
  // Trigger initial warmup immediately
  warmupBackend();

  if (warmupInterval) return;

  warmupInterval = setInterval(() => {
    // Only send keep-alive when document is visible
    if (document.visibilityState === 'visible') {
      apiClient.get('/v1/health', { timeout: 15000 }).catch(() => {});
    }
  }, 4 * 60 * 1000); // every 4 minutes (Render sleeps at 15 mins)
};

export const stopKeepAliveHeartbeat = () => {
  if (warmupInterval) {
    clearInterval(warmupInterval);
    warmupInterval = null;
  }
};
