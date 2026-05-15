import type { NotificationItem } from '../api/notification';

export function getNotificationTargetUrl(item: NotificationItem) {
  if (item.relatedType === 'RELATIONSHIP' && item.relatedId) {
    return `/pages/relationships/detail?id=${item.relatedId}`;
  }
  if (item.relatedType === 'SPACE_TODO' && item.relationshipId) {
    return `/pages/todos/index?relationshipId=${item.relationshipId}`;
  }
  if (item.relatedType === 'ANNIVERSARY') {
    return '/pages/anniversaries/index';
  }
  if (item.relatedType === 'DAILY_POST' || item.relatedType === 'DAILY_POST_COMMENT') {
    return '/pages/daily/index';
  }
  return '';
}
