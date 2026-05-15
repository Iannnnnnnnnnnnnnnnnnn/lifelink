import { http } from './request';

export interface DailyPostImage {
  fileId: number;
  url: string;
  originalName?: string;
  sortOrder?: number;
}

export interface DailyPost {
  id: number;
  relationshipId: number;
  relationshipName?: string;
  userId?: number;
  username?: string;
  content: string;
  mood?: string;
  visibility?: string;
  createdAt?: string;
  images?: DailyPostImage[];
  likeCount?: number;
  commentCount?: number;
  likedByMe?: boolean;
}

export interface DailyPostQuery {
  relationshipId?: number | string;
  page?: number;
  size?: number;
}

export interface CreateDailyPostRequest {
  relationshipId: number | string;
  content: string;
  mood?: string;
  visibility?: string;
  imageIds?: number[];
}

export interface DailyPostInteraction {
  dailyPostId: number;
  likeCount: number;
  commentCount: number;
  likedByMe: boolean;
}

export function getDailyPosts(params: DailyPostQuery = {}) {
  return http.get<DailyPost[]>('/api/daily-posts', params as Record<string, unknown>);
}

export function createDailyPost(data: CreateDailyPostRequest) {
  return http.post<DailyPost>('/api/daily-posts', data);
}

export function likeDailyPost(id: number) {
  return http.post<DailyPostInteraction>(`/api/daily-posts/${id}/like`);
}

export function unlikeDailyPost(id: number) {
  return http.delete<DailyPostInteraction>(`/api/daily-posts/${id}/like`);
}
