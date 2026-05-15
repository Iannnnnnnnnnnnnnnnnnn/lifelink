import { http } from './request';

export type RelationshipType = 'COUPLE' | 'FAMILY' | 'FRIEND' | 'ROOMMATE' | 'CUSTOM';

export interface RelationshipSummary {
  id: number;
  name: string;
  type: RelationshipType;
  description?: string;
  ownerId?: number;
  status: string;
  currentUserRole?: string;
  createdAt?: string;
  updatedAt?: string;
  memberCount?: number;
}

export interface RelationshipDetail extends RelationshipSummary {}

export interface RelationshipMember {
  userId: number;
  username: string;
  avatarUrl?: string;
  role: string;
  nickname?: string;
  joinedAt?: string;
}

export function getRelationships() {
  return http.get<RelationshipSummary[]>('/api/relationships');
}

export function getRelationshipDetail(id: number | string) {
  return http.get<RelationshipDetail>(`/api/relationships/${id}`);
}

export function getRelationshipMembers(id: number | string) {
  return http.get<RelationshipMember[]>(`/api/relationships/${id}/members`);
}
