import { ApiResult, request } from './request';

export type TimelineEventType =
  | 'RELATIONSHIP_CREATED'
  | 'MEMBER_JOINED'
  | 'FIRST_DAILY_POST'
  | 'ANNIVERSARY_CREATED'
  | 'IMPORTANT_TODO_COMPLETED'
  | 'IMPORTANT_COMMENT_INTERACTION'
  | 'IMAGE_UPLOADED'
  | 'CUSTOM';

export interface RelationshipTimelineEvent {
  id: number;
  relationshipId: number;
  relationshipName?: string;
  eventType: TimelineEventType | string;
  title: string;
  description?: string;
  actorUserId?: number;
  actorUsername?: string;
  actorAvatarUrl?: string;
  targetType?: string;
  targetId?: number;
  targetUrl?: string;
  coverFileId?: number;
  coverUrl?: string;
  eventDate: string;
  importance: 'NORMAL' | 'IMPORTANT' | string;
  source: 'AUTO' | 'MANUAL' | string;
  metadata?: Record<string, unknown>;
  createdAt: string;
}

export interface GetTimelineParams {
  eventType?: string;
  importance?: string;
  order?: 'ASC' | 'DESC';
}

export function getRelationshipTimeline(relationshipId: number, params: GetTimelineParams = {}) {
  return request.get<ApiResult<RelationshipTimelineEvent[]>>(`/api/relationships/${relationshipId}/timeline`, { params });
}

export function getRelationshipTimelineDetail(relationshipId: number, eventId: number) {
  return request.get<ApiResult<RelationshipTimelineEvent>>(`/api/relationships/${relationshipId}/timeline/${eventId}`);
}

export interface CreateTimelineEventRequest {
  title: string;
  description?: string;
  eventDate: string;
  coverFileId?: number;
  importance?: 'NORMAL' | 'IMPORTANT';
}

export function createTimelineEvent(relationshipId: number, data: CreateTimelineEventRequest) {
  return request.post<ApiResult<RelationshipTimelineEvent>>(`/api/relationships/${relationshipId}/timeline`, data);
}

export function deleteTimelineEvent(relationshipId: number, eventId: number) {
  return request.delete<ApiResult<void>>(`/api/relationships/${relationshipId}/timeline/${eventId}`);
}
