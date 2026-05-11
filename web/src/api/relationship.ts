import { ApiResult, request } from './request';

export type RelationshipType = 'COUPLE' | 'FAMILY' | 'FRIEND' | 'ROOMMATE' | 'CUSTOM';

export interface CreateRelationshipRequest {
  name: string;
  type: RelationshipType;
  description?: string;
}

export interface RelationshipSummary {
  id: number;
  name: string;
  type: RelationshipType;
  description?: string;
  ownerId: number;
  status: string;
  currentUserRole: string;
  createdAt: string;
}

export interface RelationshipDetail extends RelationshipSummary {
  updatedAt: string;
}

export interface RelationshipMember {
  userId: number;
  username: string;
  avatarUrl?: string;
  role: string;
  nickname?: string;
  joinedAt: string;
}

export interface CreateInviteResponse {
  inviteCode: string;
  expireAt: string;
}

export interface JoinRelationshipRequest {
  inviteCode: string;
}

export function createRelationship(data: CreateRelationshipRequest) {
  return request.post<ApiResult<RelationshipDetail>>('/api/relationships', data);
}

export function getRelationships() {
  return request.get<ApiResult<RelationshipSummary[]>>('/api/relationships');
}

export function getRelationshipDetail(id: number) {
  return request.get<ApiResult<RelationshipDetail>>(`/api/relationships/${id}`);
}

export function getRelationshipMembers(id: number) {
  return request.get<ApiResult<RelationshipMember[]>>(`/api/relationships/${id}/members`);
}

export function createRelationshipInvite(id: number) {
  return request.post<ApiResult<CreateInviteResponse>>(`/api/relationships/${id}/invite`);
}

export function joinRelationship(data: JoinRelationshipRequest) {
  return request.post<ApiResult<RelationshipDetail>>('/api/relationships/join', data);
}
