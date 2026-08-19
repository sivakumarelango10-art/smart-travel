/**
 * Global Frontend Configuration Constants.
 * - VITE_API_BASE_URL: Backend REST API base URL (defaults to /api for reverse-proxy deployments)
 * - VITE_WS_BASE_URL: Backend WebSocket URL (must be set explicitly in production)
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
export const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || '';

export const APP_NAME = 'SmartTravel Platform';
export const APP_VERSION = '1.0.0-PROD';
