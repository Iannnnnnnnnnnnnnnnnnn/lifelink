import type { TFunction } from 'i18next';
import { CalendarOutlined, CheckSquareOutlined, EditOutlined, HomeOutlined, UserAddOutlined } from '@ant-design/icons';
import { SpaceActivity } from '../api/activity';

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
    case 'DAILY_POST_CREATED':
      return t('activity.dailyPostCreated', { user });
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

export function getActivityIcon(activityType: string) {
  if (activityType.startsWith('RELATIONSHIP')) {
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
