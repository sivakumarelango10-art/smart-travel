import { apiClient } from './api';
import { ApiResponse, CreateReviewPayload, Review, ReviewTargetType, ReviewReply } from '../types/api';

export const reviewService = {
  /**
   * Submit a new review for a flight or hotel.
   */
  async createReview(payload: CreateReviewPayload): Promise<Review> {
    const response = await apiClient.post<ApiResponse<Review>>('/v1/reviews', payload);
    return response.data.data;
  },

  /**
   * Get reviews for a target entity (flight or hotel).
   */
  async getReviews(
    targetType: ReviewTargetType,
    targetId: string,
    page: number = 0,
    size: number = 10
  ): Promise<{ content: Review[]; totalElements: number; totalPages: number }> {
    const response = await apiClient.get<
      ApiResponse<{
        content: Review[];
        totalElements: number;
        totalPages: number;
      }>
    >('/v1/reviews', {
      params: { targetType, targetId, page, size },
    });
    const data = response.data.data;
    return {
      content: data?.content || [],
      totalElements: data?.totalElements || 0,
      totalPages: data?.totalPages || 0,
    };
  },

  /**
   * Get average rating for a flight or hotel.
   */
  async getAverageRating(targetType: ReviewTargetType, targetId: string): Promise<number> {
    try {
      const response = await apiClient.get<ApiResponse<number>>('/v1/reviews/rating', {
        params: { targetType, targetId },
      });
      return response.data.data ?? 0;
    } catch {
      return 0;
    }
  },

  /**
   * Get current user's submitted reviews.
   */
  async getMyReviews(page: number = 0, size: number = 10): Promise<Review[]> {
    const response = await apiClient.get<ApiResponse<{ content: Review[] }>>('/v1/reviews/my', {
      params: { page, size },
    });
    return response.data.data?.content || [];
  },

  /**
   * Vote a review as helpful.
   */
  async voteHelpful(reviewId: string): Promise<Review> {
    const response = await apiClient.post<ApiResponse<Review>>(`/v1/reviews/${reviewId}/helpful`);
    return response.data.data;
  },

  /**
   * Flag a review for moderation.
   */
  async flagReview(reviewId: string): Promise<Review> {
    const response = await apiClient.post<ApiResponse<Review>>(`/v1/reviews/${reviewId}/flag`);
    return response.data.data;
  },

  /**
   * Delete own review.
   */
  async deleteReview(reviewId: string): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`/v1/reviews/${reviewId}`);
  },

  /**
   * Upload and attach a photo to an existing review.
   */
  async uploadPhoto(reviewId: string, file: File): Promise<Review> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post<ApiResponse<Review>>(
      `/v1/reviews/${reviewId}/photos`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  },

  /**
   * Get all replies for a review.
   */
  async getReplies(reviewId: string): Promise<ReviewReply[]> {
    const response = await apiClient.get<ApiResponse<ReviewReply[]>>(`/v1/reviews/${reviewId}/replies`);
    return response.data.data || [];
  },

  /**
   * Submit a reply to a review.
   */
  async createReply(reviewId: string, content: string, userName?: string): Promise<ReviewReply> {
    const response = await apiClient.post<ApiResponse<ReviewReply>>(
      `/v1/reviews/${reviewId}/replies`,
      { content, userName }
    );
    return response.data.data;
  },

  /**
   * Update an existing reply.
   */
  async updateReply(reviewId: string, replyId: string, content: string): Promise<ReviewReply> {
    const response = await apiClient.put<ApiResponse<ReviewReply>>(
      `/v1/reviews/${reviewId}/replies/${replyId}`,
      { content }
    );
    return response.data.data;
  },

  /**
   * Delete an existing reply.
   */
  async deleteReply(reviewId: string, replyId: string): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`/v1/reviews/${reviewId}/replies/${replyId}`);
  },
};
