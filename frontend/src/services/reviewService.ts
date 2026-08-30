import { apiClient } from './api';
import { ApiResponse } from '../types/api';
import {
  Review,
  ReviewReply,
  ReviewTargetType,
  ReviewStatus,
  ReviewSortOption,
  ReviewStats,
  CreateReviewPayload,
  CreateReplyPayload,
  ReviewFilterParams,
} from '../types/review';

export const reviewService = {
  /**
   * Submit a new review for a flight or hotel.
   */
  async createReview(payload: CreateReviewPayload): Promise<Review> {
    const response = await apiClient.post<ApiResponse<Review>>('/v1/reviews', payload);
    return response.data.data;
  },

  /**
   * Get reviews for a target entity with sorting and filtering options.
   */
  async getReviews(
    targetTypeOrParams: ReviewTargetType | ReviewFilterParams,
    targetId?: string,
    page: number = 0,
    size: number = 10,
    sortBy: ReviewSortOption = 'NEWEST',
    rating?: number,
    verifiedOnly?: boolean,
    withPhotosOnly?: boolean
  ): Promise<{ content: Review[]; totalElements: number; totalPages: number }> {
    let params: Record<string, any>;

    if (typeof targetTypeOrParams === 'object') {
      params = {
        targetType: targetTypeOrParams.targetType,
        targetId: targetTypeOrParams.targetId,
        page: targetTypeOrParams.page ?? 0,
        size: targetTypeOrParams.size ?? 10,
        sortBy: targetTypeOrParams.sortBy ?? 'NEWEST',
        rating: targetTypeOrParams.rating,
        verifiedOnly: targetTypeOrParams.verifiedOnly,
        withPhotosOnly: targetTypeOrParams.withPhotosOnly,
      };
    } else {
      params = {
        targetType: targetTypeOrParams,
        targetId,
        page,
        size,
        sortBy,
        rating,
        verifiedOnly,
        withPhotosOnly,
      };
    }

    const response = await apiClient.get<
      ApiResponse<{
        content: Review[];
        totalElements: number;
        totalPages: number;
      }>
    >('/v1/reviews', { params });

    const data = response.data.data;
    return {
      content: data?.content || [],
      totalElements: data?.totalElements || 0,
      totalPages: data?.totalPages || 0,
    };
  },

  /**
   * Get detailed star breakdown and category averages for a flight or hotel.
   */
  async getReviewStats(targetType: ReviewTargetType, targetId: string): Promise<ReviewStats> {
    try {
      const response = await apiClient.get<ApiResponse<ReviewStats>>('/v1/reviews/stats', {
        params: { targetType, targetId },
      });
      return response.data.data;
    } catch {
      return {
        averageRating: 0,
        totalReviews: 0,
        count5Stars: 0,
        count4Stars: 0,
        count3Stars: 0,
        count2Stars: 0,
        count1Star: 0,
        averageCleanliness: 0,
        averageService: 0,
        averageValue: 0,
        ratingDistribution: { '5': 0, '4': 0, '3': 0, '2': 0, '1': 0 },
      };
    }
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
   * Vote a review as helpful (toggle).
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
   * Update own reply.
   */
  async updateReply(reviewId: string, replyId: string, content: string): Promise<ReviewReply> {
    const response = await apiClient.put<ApiResponse<ReviewReply>>(
      `/v1/reviews/${reviewId}/replies/${replyId}`,
      { content }
    );
    return response.data.data;
  },

  /**
   * Delete own reply.
   */
  async deleteReply(reviewId: string, replyId: string): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`/v1/reviews/${reviewId}/replies/${replyId}`);
  },

  // ── Administrator & Moderator APIs ─────────────────────────────────────────

  /**
   * Get flagged reviews requiring moderator attention.
   */
  async getFlaggedReviews(
    page: number = 0,
    size: number = 10
  ): Promise<{ content: Review[]; totalElements: number; totalPages: number }> {
    const response = await apiClient.get<
      ApiResponse<{ content: Review[]; totalElements: number; totalPages: number }>
    >('/v1/admin/reviews/flagged', { params: { page, size } });
    return (
      response.data.data || { content: [], totalElements: 0, totalPages: 0 }
    );
  },

  /**
   * Get all reviews for admin dashboard with filtering by status and targetType.
   */
  async getAdminReviews(
    status?: ReviewStatus,
    targetType?: ReviewTargetType,
    page: number = 0,
    size: number = 10
  ): Promise<{ content: Review[]; totalElements: number; totalPages: number }> {
    const response = await apiClient.get<
      ApiResponse<{ content: Review[]; totalElements: number; totalPages: number }>
    >('/v1/admin/reviews', { params: { status, targetType, page, size } });
    return (
      response.data.data || { content: [], totalElements: 0, totalPages: 0 }
    );
  },

  /**
   * Approve a flagged review and return it to PUBLISHED status.
   */
  async approveReview(reviewId: string): Promise<Review> {
    const response = await apiClient.post<ApiResponse<Review>>(
      `/v1/admin/reviews/${reviewId}/approve`
    );
    return response.data.data;
  },

  /**
   * Hide a review from public listings.
   */
  async hideReview(reviewId: string): Promise<Review> {
    const response = await apiClient.post<ApiResponse<Review>>(
      `/v1/admin/reviews/${reviewId}/hide`
    );
    return response.data.data;
  },

  /**
   * Remove a review for community guidelines violation.
   */
  async removeReview(reviewId: string, reason?: string): Promise<Review> {
    const response = await apiClient.post<ApiResponse<Review>>(
      `/v1/admin/reviews/${reviewId}/remove`,
      { reason }
    );
    return response.data.data;
  },

  /**
   * Restore a previously hidden or removed review.
   */
  async restoreReview(reviewId: string): Promise<Review> {
    const response = await apiClient.post<ApiResponse<Review>>(
      `/v1/admin/reviews/${reviewId}/restore`
    );
    return response.data.data;
  },
};
