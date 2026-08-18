import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL } from '../config/constants';
import { ErrorResponse } from '../types/api';

/**
 * Configured Axios HTTP Client with JWT interceptors and standardized error handling.
 */
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Request Interceptor: Attach JWT Token if available
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('smarttravel_access_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Normalize errors
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    if (error.response) {
      // Server returned standard RFC 7807 error
      const status = error.response.status;
      if (status === 401) {
        // Token expired or invalid
        localStorage.removeItem('smarttravel_access_token');
      }
      return Promise.reject(error.response.data);
    } else if (error.request) {
      // Network connectivity error
      return Promise.reject({
        status: 0,
        error: 'NETWORK_ERROR',
        message: 'Unable to communicate with SmartTravel backend service. Please check your network connection.',
        timestamp: new Date().toISOString(),
      });
    }
    return Promise.reject(error);
  }
);
