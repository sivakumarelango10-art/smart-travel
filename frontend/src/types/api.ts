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
  validationErrors?: Array<{
    field: string;
    rejectedValue?: any;
    message: string;
  }>;
}

export interface HealthData {
  status: 'UP' | 'DOWN';
  service: string;
  environment: string;
  database: 'CONNECTED' | 'DISCONNECTED' | 'UNKNOWN';
  timestamp: string;
}

export interface UserSummary {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}
