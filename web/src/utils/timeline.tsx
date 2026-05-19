import type { TFunction } from 'i18next';
import { CalendarOutlined, CameraOutlined, CheckCircleOutlined, CommentOutlined, EditOutlined, HeartOutlined, HomeOutlined, StarOutlined, UserAddOutlined } from '@ant-design/icons';
import type { RelationshipTimelineEvent } from '../api/timeline';

const userAuthoredEventTypes = new Set(['CUSTOM']);

function metadataText(event: RelationshipTimelineEvent, key: string) {
  const value = event.metadata?.[key];
  return typeof value === 'string' || typeof value === 'number' ? String(value) : undefined;
}

export function getTimelineEventText(event: RelationshipTimelineEvent, t: TFunction) {
  switch (event.eventType) {
    case 'RELATIONSHIP_CREATED':
      return t('timeline.relationshipCreated', { name: metadataText(event, 'relationshipName') || event.relationshipName || event.title });
    case 'MEMBER_JOINED':
      return t('timeline.memberJoined', { user: metadataText(event, 'username') || event.actorUsername || event.title });
    case 'FIRST_DAILY_POST':
      return t('timeline.firstDailyPost');
    case 'ANNIVERSARY_CREATED':
      return t('timeline.anniversaryCreated', { title: metadataText(event, 'anniversaryTitle') || event.title });
    case 'IMPORTANT_TODO_COMPLETED':
      return t('timeline.importantTodoCompleted', { title: metadataText(event, 'todoTitle') || event.title });
    case 'IMPORTANT_COMMENT_INTERACTION':
      return t('timeline.importantCommentInteraction');
    case 'IMAGE_UPLOADED':
      return t('timeline.imageUploaded');
    case 'CUSTOM':
      return event.title || t('timeline.custom');
    default:
      return event.title;
  }
}

export function getTimelineEventTag(eventType: string, t: TFunction) {
  return t(`timeline.types.${eventType}`, { defaultValue: eventType });
}

export function getTimelineEventDescription(event: RelationshipTimelineEvent) {
  if (!userAuthoredEventTypes.has(event.eventType)) {
    return undefined;
  }
  return event.description || undefined;
}

export function getTimelineEventIcon(eventType: string) {
  if (eventType === 'RELATIONSHIP_CREATED') return <HomeOutlined />;
  if (eventType === 'MEMBER_JOINED') return <UserAddOutlined />;
  if (eventType === 'FIRST_DAILY_POST') return <EditOutlined />;
  if (eventType === 'ANNIVERSARY_CREATED') return <CalendarOutlined />;
  if (eventType === 'IMPORTANT_TODO_COMPLETED') return <CheckCircleOutlined />;
  if (eventType === 'IMPORTANT_COMMENT_INTERACTION') return <CommentOutlined />;
  if (eventType === 'IMAGE_UPLOADED') return <CameraOutlined />;
  if (eventType === 'CUSTOM') return <StarOutlined />;
  return <HeartOutlined />;
}

export function getTimelineTargetPath(event: RelationshipTimelineEvent) {
  if (event.targetUrl) return event.targetUrl;
  if (event.targetType === 'RELATIONSHIP') return `/relationships/${event.relationshipId}`;
  if (event.targetType === 'USER') return `/relationships/${event.relationshipId}`;
  if (event.targetType === 'DAILY_POST' && event.targetId) return `/daily/${event.targetId}`;
  if (event.targetType === 'SPACE_TODO') return `/relationships/${event.relationshipId}/todos`;
  if (event.targetType === 'ANNIVERSARY' && event.targetId) return `/anniversaries/${event.targetId}`;
  if (event.targetType === 'DAILY_POST_COMMENT') {
    const postId = metadataText(event, 'postId');
    return postId ? `/daily/${postId}` : undefined;
  }
  return undefined;
}
