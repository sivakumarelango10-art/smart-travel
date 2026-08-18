import { apiClient } from './api';
import { ApiResponse, HealthData } from '../types/api';

/**
 * Health Check API Client
 */
export const healthService = {
  getHealth: async (): Promise<ApiResponse<HealthData>> => {
    const response = await apiClient.get<ApiResponse<HealthData>>('/health');
    return response.data;
  },
};
