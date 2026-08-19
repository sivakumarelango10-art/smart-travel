/**
 * Global Frontend Configuration Constants.
 * - VITE_API_BASE_URL: Backend REST API base URL. Automatically normalized so whether the environment
 *   variable contains the host origin (https://backend.com) or an API path (https://backend.com/api/v1),
 *   outgoing service requests starting with /v1/... construct clean, non-duplicated endpoints.
 * - VITE_WS_BASE_URL: Backend WebSocket URL
 */
const rawApiUrl = (import.meta.env.VITE_API_BASE_URL || '').trim().replace(/\/+$/, '');
export const API_BASE_URL = rawApiUrl
  ? rawApiUrl.replace(/\/api\/v1\/?$/, '').replace(/\/v1\/?$/, '').replace(/\/api\/?$/, '')
  : '/api';

export const WS_BASE_URL = (import.meta.env.VITE_WS_BASE_URL || '').replace(/\/+$/, '');

export const APP_NAME = 'SmartTravel Platform';
export const APP_VERSION = '1.0.0-PROD';
