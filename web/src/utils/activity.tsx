import type { TFunction } from 'i18next';
import { CalendarOutlined, CheckSquareOutlined, EditOutlined, HomeOutlined, UserAddOutlined } from '@ant-design/icons';
import { SpaceActivity } from '../api/activity';

const translatedActivityTypes = new Set([
  'RELATIONSHIP_CREATED',
  'MEMBER_JOINED',
  'MEMBER_LEFT',
  'MEMBER_REMOVED',
  'MEMBER_ROLE_UPDATED',
  'OWNER_TRANSFERRED',
  'RELATIONSHIP_DELETED',
  'DAILY_POST_CREATED',
  'DAILY_POST_COMMENTED',
  'TODO_CREATED',
  'TODO_COMPLETED',
  'TODO_REOPENED',
  'ANNIVERSARY_CREATED',
]);

function metadataText(activity: SpaceActivity, key: string) {
  const value = activity.metadata?.[key];
  return typeof value === 'string' || typeof value === 'number' ? String(value) : undefined;
}

export function getActivityText(activity: SpaceActivity, t: TFunction) {
  const user = activity.actorUsername || t('activity.someone');
  switch (activity.activityType) {
    case 'RELATIONSHIP_CREATED':
      return t('activity.relationshipCreated', { user, name: metadataText(activity, 'relationshipName') || activity.relationshipName || activity.title });
    case 'MEMBER_JOINED':
      return t('activity.memberJoined', { user });
    case 'MEMBER_LEFT':
      return t('activity.memberLeft', { user });
    case 'MEMBER_REMOVED':
      return t('activity.memberRemoved', { user, target: metadataText(activity, 'username') || activity.title });
    case 'MEMBER_ROLE_UPDATED':
      return t('activity.memberRoleUpdated', { user, target: metadataText(activity, 'username') || activity.title });
    case 'OWNER_TRANSFERRED':
      return t('activity.ownerTransferred', { user, target: metadataText(activity, 'username') || activity.title });
    case 'RELATIONSHIP_DELETED':
      return t('activity.relationshipDeleted', { user, name: metadataText(activity, 'relationshipName') || activity.relationshipName || activity.title });
    case 'DAILY_POST_CREATED':
      return t('activity.dailyPostCreated', { user });
    case 'DAILY_POST_COMMENTED':
      return t('activity.dailyPostCommented', { user });
    case 'TODO_CREATED':
      return t('activity.todoCreated', { user, title: metadataText(activity, 'todoTitle') || activity.title });
    case 'TODO_COMPLETED':
      return t('activity.todoCompleted', { user, title: metadataText(activity, 'todoTitle') || activity.title });
    case 'TODO_REOPENED':
      return t('activity.todoReopened', { user, title: metadataText(activity, 'todoTitle') || activity.title });
    case 'ANNIVERSARY_CREATED':
      return t('activity.anniversaryCreated', { user, title: metadataText(activity, 'anniversaryTitle') || activity.title });
    default:
      return activity.title;
  }
}

export function getActivityTag(activityType: string, t: TFunction) {
  return t(`activity.types.${activityType}`, { defaultValue: activityType });
}

export function shouldShowActivityContent(activityType: string) {
  return !translatedActivityTypes.has(activityType);
}

export function getActivityIcon(activityType: string) {
  if (activityType.startsWith('RELATIONSHIP') || activityType.startsWith('OWNER')) {
    return <HomeOutlined />;
  }
  if (activityType.startsWith('MEMBER')) {
    return <UserAddOutlined />;
  }
  if (activityType.startsWith('DAILY')) {
    return <EditOutlined />;
  }
  if (activityType.startsWith('TODO')) {
    return <CheckSquareOutlined />;
  }
  if (activityType.startsWith('ANNIVERSARY')) {
    return <CalendarOutlined />;
  }
  return <HomeOutlined />;
}
