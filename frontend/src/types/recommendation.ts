export type RecommendationItemType = 'FLIGHT' | 'HOTEL' | 'DESTINATION';

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

export type UserActivityType =
  | 'SEARCH'
  | 'VIEW'
  | 'EXTENDED_VIEW'
  | 'BOOK'
  | 'TRACK'
  | 'REVIEW'
  | 'SEARCH_HOTEL'
  | 'VIEW_HOTEL';

export interface TrackActivityPayload {
  activityType: UserActivityType;
  targetId: string;
  targetType: string;
  metadata?: Record<string, any>;
}
