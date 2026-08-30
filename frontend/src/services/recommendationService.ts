import { apiClient } from './api';
import { ApiResponse } from '../types/api';
import {
  RecommendationItem,
  UserPreferenceProfile,
  TrackActivityPayload,
  SubmitFeedbackPayload,
  RecommendationFeedbackType,
} from '../types/recommendation';

export const recommendationService = {
  /**
   * Get personalized recommendations for user (mixed flights + hotels + destinations).
   */
  async getRecommendations(
    limit: number = 8,
    context?: string,
    destination?: string
  ): Promise<RecommendationItem[]> {
    const response = await apiClient.get<ApiResponse<RecommendationItem[]>>('/v1/recommendations', {
      params: { limit, context, destination },
    });
    return response.data.data || [];
  },

  /**
   * Get flight recommendations with explainability.
   */
  async getFlightRecommendations(limit: number = 6): Promise<RecommendationItem[]> {
    const response = await apiClient.get<ApiResponse<RecommendationItem[]>>('/v1/recommendations/flights', {
      params: { limit },
    });
    return response.data.data || [];
  },

  /**
   * Get hotel recommendations with explainability.
   */
  async getHotelRecommendations(limit: number = 6): Promise<RecommendationItem[]> {
    const response = await apiClient.get<ApiResponse<RecommendationItem[]>>('/v1/recommendations/hotels', {
      params: { limit },
    });
    return response.data.data || [];
  },

  /**
   * Get personalized and trending destinations.
   */
  async getDestinationRecommendations(limit: number = 6): Promise<RecommendationItem[]> {
    const response = await apiClient.get<ApiResponse<RecommendationItem[]>>('/v1/recommendations/destinations', {
      params: { limit },
    });
    return response.data.data || [];
  },

  /**
   * Get popular destinations (public alias).
   */
  async getPopularDestinations(limit: number = 6): Promise<RecommendationItem[]> {
    return this.getDestinationRecommendations(limit);
  },

  /**
   * Submit feedback (Helpful, Not Relevant, Dismiss) on a recommendation.
   */
  async submitFeedback(payload: SubmitFeedbackPayload): Promise<void> {
    try {
      await apiClient.post<ApiResponse<any>>('/v1/recommendations/feedback', payload);
    } catch (err) {
      console.error('Failed to submit recommendation feedback', err);
    }
  },

  /**
   * Get current user's inferred travel preference profile.
   */
  async getUserPreferences(): Promise<UserPreferenceProfile | null> {
    try {
      const response = await apiClient.get<ApiResponse<UserPreferenceProfile>>('/v1/recommendations/preferences');
      return response.data.data || null;
    } catch {
      return null;
    }
  },

  /**
   * Get recent user activity history.
   */
  async getUserHistory(limit: number = 20): Promise<any[]> {
    try {
      const response = await apiClient.get<ApiResponse<any[]>>('/v1/recommendations/history', {
        params: { limit },
      });
      return response.data.data || [];
    } catch {
      return [];
    }
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
