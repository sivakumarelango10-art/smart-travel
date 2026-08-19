/**
 * Standard API Response Structures mirroring Backend DTOs
 */

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  requestId?: string;
  validationErrors?: Array<{
    field: string;
    rejectedValue?: any;
    message: string;
  }>;
}

export interface HealthData {
  status: 'UP' | 'DOWN' | 'DEGRADED';
  service: string;
  environment: string;
  database: 'CONNECTED' | 'DISCONNECTED' | 'UNKNOWN';
  timestamp: string;
}

export * from './auth';
export * from './flight';
export * from './seat';
export * from './booking';
export * from './payment';
export * from './ticket';
export * from './notification';
