export type RecommendationItemType = 'FLIGHT' | 'HOTEL' | 'DESTINATION';
export type RecommendationFeedbackType = 'HELPFUL' | 'NOT_RELEVANT' | 'DISMISS';

export interface RecommendationExplanation {
  reasonCode: string;
  headline: string;
  details: string;
  category?: string;
  confidence: number;
  tags?: string[];
  isAiGenerated?: boolean;
}

export interface RecommendationItem {
  id: string;
  type: RecommendationItemType;
  targetId: string;
  title: string;
  subtitle?: string;
  description?: string;
  imageUrl?: string;
  price?: number;
  priceLabel?: string;
  currency?: string;
  score: number;
  reasonCode?: string;
  reasonLabel?: string;

  /** Structured explainability for "Why this recommendation?" */
  explanation?: RecommendationExplanation;

  /** Primary category (BEACH, LUXURY, MOUNTAIN, HERITAGE, METROPOLITAN, NATURE) */
  category?: string;
  tags?: string[];
  badgeText?: string;

  /** User recorded feedback (HELPFUL, NOT_RELEVANT, or null) */
  userFeedback?: RecommendationFeedbackType | null;

  // Flight specific
  fromCity?: string;
  toCity?: string;
  fromCode?: string;
  toCode?: string;
  airline?: string;

  // Hotel specific
  city?: string;
  starRating?: number;
  avgRating?: number;
}

export interface UserPreferenceProfile {
  userId: string;
  topCategories: string[];
  categoryAffinities?: Record<string, number>;
  preferredDestinations: string[];
  preferredAirlines: string[];
  homeAirport?: string;
  preferredCabinClass?: string;
  inferredTravelStyle: string;
  totalActivities: number;
  helpfulFeedbackCount: number;
  confidenceScore: number;
}

export type UserActivityType =
  | 'SEARCH'
  | 'VIEW'
  | 'EXTENDED_VIEW'
  | 'BOOK'
  | 'TRACK'
  | 'REVIEW'
  | 'SEARCH_HOTEL'
  | 'VIEW_HOTEL'
  | 'RECOMMENDATION_CLICK'
  | 'RECOMMENDATION_HELPFUL'
  | 'RECOMMENDATION_IRRELEVANT'
  | 'RECOMMENDATION_DISMISS';

export interface TrackActivityPayload {
  activityType: UserActivityType;
  targetId: string;
  targetType: string;
  metadata?: Record<string, any>;
}

export interface SubmitFeedbackPayload {
  targetId: string;
  targetType: string;
  feedbackType: RecommendationFeedbackType;
  reasonCode?: string;
  category?: string;
}
