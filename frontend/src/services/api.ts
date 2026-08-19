import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL } from '../config/constants';
import { ErrorResponse } from '../types/api';

export const TOKEN_KEY = 'smarttravel_access_token';
export const REFRESH_TOKEN_KEY = 'smarttravel_refresh_token';
export const USER_KEY = 'smarttravel_user';

/**
 * Configured Axios HTTP Client with JWT interceptors, X-Request-ID propagation, and standardized error normalization.
 */
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request Interceptor: Attach JWT Token & client Correlation ID if available
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Normalize errors & handle 401 token invalidation
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    if (error.response) {
      const status = error.response.status;
      if (status === 401) {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
      }
      return Promise.reject(error.response.data);
    } else if (error.request) {
      return Promise.reject({
        status: 0,
        error: 'NETWORK_ERROR',
        message: 'Unable to communicate with SmartTravel backend service. Please verify your connection or try again.',
        timestamp: new Date().toISOString(),
      });
    }
    return Promise.reject(error);
  }
);
