export type ReviewTargetType = 'FLIGHT' | 'HOTEL';
export type ReviewStatus = 'PUBLISHED' | 'FLAGGED' | 'HIDDEN' | 'REMOVED' | 'PENDING';
export type ReviewSortOption = 'NEWEST' | 'MOST_HELPFUL' | 'HIGHEST_RATED' | 'LOWEST_RATED' | 'OLDEST';

export interface Review {
  id: string;
  userId: string;
  userFullName: string;
  targetType: ReviewTargetType;
  targetId: string;
  targetName?: string;
  rating: number;
  cleanlinessRating?: number;
  serviceRating?: number;
  valueRating?: number;
  title: string;
  body: string;
  status: ReviewStatus;
  helpfulVoters?: string[];
  helpfulCount?: number;
  flaggedBy?: string[];
  flagCount?: number;
  moderationNote?: string;
  moderatedBy?: string;
  moderatedAt?: string;
  bookingId?: string;
  verifiedPurchase: boolean;
  photos?: string[];
  createdAt: string;
  updatedAt?: string;
}

export interface ReviewReply {
  id: string;
  reviewId: string;
  userId: string;
  userName: string;
  content: string;
  status: ReviewStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface ReviewStats {
  averageRating: number;
  totalReviews: number;
  count5Stars: number;
  count4Stars: number;
  count3Stars: number;
  count2Stars: number;
  count1Star: number;
  averageCleanliness: number;
  averageService: number;
  averageValue: number;
  ratingDistribution: Record<string, number>;
}

export interface CreateReviewPayload {
  targetType: ReviewTargetType;
  targetId: string;
  targetName?: string;
  userDisplayName?: string;
  rating: number;
  cleanlinessRating?: number;
  serviceRating?: number;
  valueRating?: number;
  title: string;
  body: string;
  bookingId?: string;
}

export interface CreateReplyPayload {
  content: string;
  userName?: string;
}

export interface ReviewFilterParams {
  targetType?: ReviewTargetType;
  targetId?: string;
  sortBy?: ReviewSortOption;
  rating?: number;
  verifiedOnly?: boolean;
  withPhotosOnly?: boolean;
  page?: number;
  size?: number;
}
