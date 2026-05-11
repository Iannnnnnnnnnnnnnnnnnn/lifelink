import { ApiResult, request } from './request';

export interface CreateDailyPostRequest {
  relationshipId: number;
  content: string;
  mood?: string;
  visibility?: string;
  imageIds?: number[];
}

export interface DailyPostImage {
  fileId: number;
  url: string;
  originalName: string;
  sortOrder: number;
}

export interface DailyPost {
  id: number;
  relationshipId: number;
  relationshipName: string;
  userId: number;
  username: string;
  content: string;
  mood?: string;
  visibility: string;
  createdAt: string;
  images: DailyPostImage[];
}

export interface DailyPostDetail extends DailyPost {
  status: string;
  updatedAt: string;
}

export interface GetDailyPostsParams {
  relationshipId?: number;
  page?: number;
  size?: number;
}

export function createDailyPost(data: CreateDailyPostRequest) {
  return request.post<ApiResult<DailyPostDetail>>('/api/daily-posts', data);
}

export function getDailyPosts(params: GetDailyPostsParams = {}) {
  return request.get<ApiResult<DailyPost[]>>('/api/daily-posts', { params });
}

export function getDailyPostDetail(id: number) {
  return request.get<ApiResult<DailyPostDetail>>(`/api/daily-posts/${id}`);
}

export function deleteDailyPost(id: number) {
  return request.delete<ApiResult<void>>(`/api/daily-posts/${id}`);
}
