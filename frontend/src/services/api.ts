import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL } from '../config/constants';
import { ErrorResponse } from '../types/api';

export const TOKEN_KEY = 'smarttravel_access_token';
export const REFRESH_TOKEN_KEY = 'smarttravel_refresh_token';
export const USER_KEY = 'smarttravel_user';

/**
 * Hardened Axios HTTP Client with JWT interceptors, X-Request-ID propagation,
 * request timeout safeguards, and standardized error normalization.
 */
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 45000,
});

// Request Interceptor: Attach JWT Token & Correlation headers
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (config.url) {
      // Defensive path normalization: strip duplicate /api/v1/v1 or /v1/v1 prefixes
      config.url = config.url
        .replace(/^\/api\/v1\/v1\//, '/v1/')
        .replace(/^\/v1\/v1\//, '/v1/')
        .replace(/^\/api\/v1\/api\/v1\//, '/v1/');
    }

    const url = config.url || '';
    const isPublicAuthEndpoint =
      url.includes('/auth/register') ||
      url.includes('/auth/login') ||
      url.includes('/auth/google') ||
      url.includes('/auth/refresh') ||
      url.includes('/auth/refresh-token') ||
      url.includes('/auth/forgot-password') ||
      url.includes('/auth/reset-password') ||
      url.includes('/health');

    const token = localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);

    if (isPublicAuthEndpoint && !token) {
      if (config.headers) {
        delete config.headers.Authorization;
        delete (config.headers as any)['Authorization'];
        delete (config.headers as any)['authorization'];
      }
      return config;
    }

    if (token && token.trim() !== '' && token !== 'null' && token !== 'undefined' && config.headers) {
      config.headers.Authorization = `Bearer ${token.trim()}`;
    } else if (config.headers) {
      delete config.headers.Authorization;
      delete (config.headers as any)['Authorization'];
      delete (config.headers as any)['authorization'];
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Normalize RFC errors & handle 401 token invalidation
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    if (error.response) {
      const status = error.response.status;
      if (status === 401) {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        sessionStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(REFRESH_TOKEN_KEY);
        sessionStorage.removeItem(USER_KEY);
        window.dispatchEvent(new Event('auth:unauthorized'));
      }
      return Promise.reject(error.response.data);
    } else if (error.code === 'ERR_CANCELED') {
      // Gracefully handle aborted requests
      return Promise.reject({
        status: 499,
        error: 'REQUEST_ABORTED',
        message: 'Request was cancelled by client.',
        timestamp: new Date().toISOString(),
      });
    } else if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      return Promise.reject({
        status: 408,
        error: 'REQUEST_TIMEOUT',
        message: 'The request took too long to complete. Please verify your connection and try again.',
        timestamp: new Date().toISOString(),
      });
    } else if (error.request) {
      return Promise.reject({
        status: 0,
        error: 'NETWORK_ERROR',
        message: 'Unable to communicate with SmartTravel backend services. Please verify your connection or try again.',
        timestamp: new Date().toISOString(),
      });
    }
    return Promise.reject(error);
  }
);

// ─────────────────────────────────────────────────────────────────────────────
// Request Deduplication & Abort Controller Management
// ─────────────────────────────────────────────────────────────────────────────

const activeAbortControllers = new Map<string, AbortController>();
const inFlightRequests = new Map<string, Promise<any>>();

/**
 * Gets a cancellation signal for a specific key (cancelling previous pending request if any).
 */
export function getAbortSignal(key: string): AbortSignal {
  if (activeAbortControllers.has(key)) {
    activeAbortControllers.get(key)!.abort();
  }
  const controller = new AbortController();
  activeAbortControllers.set(key, controller);
  return controller.signal;
}

/**
 * Executes a deduped GET request: if an identical request is already pending, shares its promise.
 */
export async function dedupedGet<T>(url: string, config?: any): Promise<T> {
  const requestKey = `GET:${url}:${JSON.stringify(config?.params || {})}`;
  if (inFlightRequests.has(requestKey)) {
    return inFlightRequests.get(requestKey) as Promise<T>;
  }

  const promise = apiClient.get<T>(url, config)
    .then((res) => res.data)
    .finally(() => {
      inFlightRequests.delete(requestKey);
    });

  inFlightRequests.set(requestKey, promise);
  return promise;
}

