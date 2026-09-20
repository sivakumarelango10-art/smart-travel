export type NotificationType =
  | 'BOOKING_CONFIRMED'
  | 'PAYMENT_SUCCESSFUL'
  | 'PAYMENT_FAILED'
  | 'TICKET_ISSUED'
  | 'FLIGHT_DELAYED'
  | 'FLIGHT_CANCELLED'
  | 'FLIGHT_RESCHEDULED'
  | 'REFUND_PROCESSED'
  | 'CHECK_IN_REMINDER'
  | 'CHECK_IN_SUCCESS';

export type NotificationPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface Notification {
  id: string;
  userId: string;
  type: NotificationType;
  notificationType?: NotificationType;
  title: string;
  subject?: string;
  message: string;
  content?: string;
  priority?: NotificationPriority;
  isRead: boolean;
  read?: boolean;
  referenceId?: string;
  bookingId?: string;
  flightId?: string;
  channel?: string;
  createdAt: string;
}

export interface NotificationPageResponse {
  content: Notification[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
