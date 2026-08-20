import { apiClient } from './api';
import { ApiResponse, RecommendationItem, TrackActivityPayload } from '../types/api';

export const recommendationService = {
  /**
   * Get personalized recommendations for user (mixed flights + hotels).
   */
  async getRecommendations(limit: number = 8): Promise<RecommendationItem[]> {
    const response = await apiClient.get<ApiResponse<RecommendationItem[]>>('/v1/recommendations', {
      params: { limit },
    });
    return response.data.data || [];
  },

  /**
   * Get flight recommendations.
   */
  async getFlightRecommendations(limit: number = 6): Promise<RecommendationItem[]> {
    const response = await apiClient.get<ApiResponse<RecommendationItem[]>>('/v1/recommendations/flights', {
      params: { limit },
    });
    return response.data.data || [];
  },

  /**
   * Get hotel recommendations.
   */
  async getHotelRecommendations(limit: number = 6): Promise<RecommendationItem[]> {
    const response = await apiClient.get<ApiResponse<RecommendationItem[]>>('/v1/recommendations/hotels', {
      params: { limit },
    });
    return response.data.data || [];
  },

  /**
   * Get popular destinations (public).
   */
  async getPopularDestinations(limit: number = 6): Promise<RecommendationItem[]> {
    const response = await apiClient.get<ApiResponse<RecommendationItem[]>>('/v1/recommendations/destinations', {
      params: { limit },
    });
    return response.data.data || [];
  },

  /**
   * Track user activity for personalized recommendations.
   */
  async trackActivity(payload: TrackActivityPayload): Promise<void> {
    try {
      await apiClient.post<ApiResponse<void>>('/v1/recommendations/track', payload);
    } catch {
      // Background activity tracking failure shouldn't disrupt UI
    }
  },
};
