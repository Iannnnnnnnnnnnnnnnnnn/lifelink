import { ApiResult, request } from './request';

export type SpaceActivityType =
  | 'RELATIONSHIP_CREATED'
  | 'MEMBER_JOINED'
  | 'DAILY_POST_CREATED'
  | 'TODO_CREATED'
  | 'TODO_COMPLETED'
  | 'TODO_REOPENED'
  | 'ANNIVERSARY_CREATED';

export interface SpaceActivity {
  id: number;
  relationshipId: number;
  relationshipName?: string;
  actorUserId: number;
  actorUsername?: string;
  actorAvatarUrl?: string;
  activityType: SpaceActivityType | string;
  targetType?: string;
  targetId?: number;
  title: string;
  content?: string;
  metadata?: Record<string, unknown>;
  createdAt: string;
}

export interface GetActivitiesParams {
  activityType?: string;
  page?: number;
  size?: number;
}

export function getRelationshipActivities(relationshipId: number, params: GetActivitiesParams = {}) {
  return request.get<ApiResult<SpaceActivity[]>>(`/api/relationships/${relationshipId}/activities`, { params });
}

export function getMyActivities(params: GetActivitiesParams = {}) {
  return request.get<ApiResult<SpaceActivity[]>>('/api/activities', { params });
}
