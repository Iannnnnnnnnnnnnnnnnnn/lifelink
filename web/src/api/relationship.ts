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

export function updateMyRelationshipNickname(id: number, data: { nickname?: string }) {
  return request.patch<ApiResult<void>>(`/api/relationships/${id}/members/me/nickname`, data);
}

export function leaveRelationship(id: number) {
  return request.post<ApiResult<void>>(`/api/relationships/${id}/leave`);
}

export function dissolveRelationship(id: number) {
  return request.delete<ApiResult<void>>(`/api/relationships/${id}`);
}

export function updateMemberRole(id: number, userId: number, data: { role: 'ADMIN' | 'MEMBER' }) {
  return request.patch<ApiResult<void>>(`/api/relationships/${id}/members/${userId}/role`, data);
}

export function removeRelationshipMember(id: number, userId: number) {
  return request.delete<ApiResult<void>>(`/api/relationships/${id}/members/${userId}`);
}

export function transferRelationshipOwner(id: number, targetUserId: number) {
  return request.post<ApiResult<void>>(`/api/relationships/${id}/transfer-owner`, { targetUserId });
}

export function createRelationshipInvite(id: number) {
  return request.post<ApiResult<CreateInviteResponse>>(`/api/relationships/${id}/invite`);
}

export function joinRelationship(data: JoinRelationshipRequest) {
  return request.post<ApiResult<RelationshipDetail>>('/api/relationships/join', data);
}
