export type ReviewTargetType = 'FLIGHT' | 'HOTEL';
export type ReviewStatus = 'PUBLISHED' | 'FLAGGED' | 'REMOVED' | 'PENDING';

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
  flaggedBy?: string[];
  moderationNote?: string;
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
