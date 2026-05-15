import { http } from './request';

export interface NotificationItem {
  id: number;
  actorUsername?: string;
  notificationType: string;
  title: string;
  content?: string;
  relatedType?: string;
  relatedId?: number;
  relationshipId?: number;
  relationshipName?: string;
  readStatus: 'UNREAD' | 'READ';
  metadata?: Record<string, unknown>;
  createdAt?: string;
}

export interface NotificationQuery {
  readStatus?: 'UNREAD' | 'READ';
  notificationType?: string;
  page?: number;
  size?: number;
}

export function getNotifications(params: NotificationQuery = {}) {
  return http.get<NotificationItem[]>('/api/notifications', params as Record<string, unknown>);
}

export function getUnreadCount() {
  return http.get<{ count: number }>('/api/notifications/unread-count');
}

export function markAsRead(id: number) {
  return http.patch<void>(`/api/notifications/${id}/read`);
}

export function markAllAsRead() {
  return http.patch<void>('/api/notifications/read-all');
}
