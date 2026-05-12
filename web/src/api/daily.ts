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
  likeCount: number;
  commentCount: number;
  likedByMe: boolean;
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

export interface DailyPostInteraction {
  dailyPostId: number;
  likeCount: number;
  commentCount: number;
  likedByMe: boolean;
}

export interface DailyPostComment {
  id: number;
  dailyPostId: number;
  userId: number;
  username?: string;
  avatarUrl?: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  canDelete: boolean;
}

export interface GetDailyPostCommentsParams {
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

export function likeDailyPost(postId: number) {
  return request.post<ApiResult<DailyPostInteraction>>(`/api/daily-posts/${postId}/like`);
}

export function unlikeDailyPost(postId: number) {
  return request.delete<ApiResult<DailyPostInteraction>>(`/api/daily-posts/${postId}/like`);
}

export function getDailyPostComments(postId: number, params: GetDailyPostCommentsParams = {}) {
  return request.get<ApiResult<DailyPostComment[]>>(`/api/daily-posts/${postId}/comments`, { params });
}

export function commentDailyPost(postId: number, data: { content: string }) {
  return request.post<ApiResult<DailyPostComment>>(`/api/daily-posts/${postId}/comments`, data);
}

export function deleteDailyPostComment(postId: number, commentId: number) {
  return request.delete<ApiResult<void>>(`/api/daily-posts/${postId}/comments/${commentId}`);
}

export function getDailyPostInteractions(postId: number) {
  return request.get<ApiResult<DailyPostInteraction>>(`/api/daily-posts/${postId}/interactions`);
}
