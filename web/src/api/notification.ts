import { ApiResult, request } from './request';

export type NotificationReadStatus = 'UNREAD' | 'READ';

export interface NotificationItem {
  id: number;
  receiverUserId: number;
  actorUserId?: number | null;
  actorUsername?: string | null;
  actorAvatarUrl?: string | null;
  notificationType: string;
  title: string;
  content?: string | null;
  relatedType?: string | null;
  relatedId?: number | null;
  relationshipId?: number | null;
  relationshipName?: string | null;
  readStatus: NotificationReadStatus;
  status: string;
  metadata: Record<string, unknown>;
  createdAt: string;
  readAt?: string | null;
}

export interface NotificationUnreadCount {
  count: number;
}

export interface NotificationListParams {
  readStatus?: NotificationReadStatus;
  notificationType?: string;
  page?: number;
  size?: number;
}

export function getNotifications(params: NotificationListParams = {}) {
  return request.get<ApiResult<NotificationItem[]>>('/api/notifications', { params });
}

export function getNotificationUnreadCount() {
  return request.get<ApiResult<NotificationUnreadCount>>('/api/notifications/unread-count');
}

export function markNotificationAsRead(id: number) {
  return request.patch<ApiResult<void>>(`/api/notifications/${id}/read`);
}

export function markAllNotificationsAsRead() {
  return request.patch<ApiResult<void>>('/api/notifications/read-all');
}

export function deleteNotification(id: number) {
  return request.delete<ApiResult<void>>(`/api/notifications/${id}`);
}
