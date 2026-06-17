import { ApiResult, request } from './request';

export interface UserProfile {
  id: number;
  username: string;
  email?: string | null;
  phone?: string | null;
  avatarUrl?: string | null;
  status: string;
  features?: {
    philosophyEnabled?: boolean;
    rewardAdmin?: boolean;
  };
  createdAt?: string;
  updatedAt?: string;
}

export interface UpdateCurrentUserRequest {
  username: string;
  email?: string | null;
  phone?: string | null;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ChangePasswordResponse {
  message: string;
}

export interface AvatarUploadResponse {
  avatarUrl: string;
}

export function getCurrentUser() {
  return request.get<ApiResult<UserProfile>>('/api/users/me');
}

export function updateCurrentUser(data: UpdateCurrentUserRequest) {
  return request.put<ApiResult<UserProfile>>('/api/users/me', data);
}

export function changePassword(data: ChangePasswordRequest) {
  return request.put<ApiResult<ChangePasswordResponse>>('/api/users/me/password', data);
}

export function uploadAvatar(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<ApiResult<AvatarUploadResponse>>('/api/users/me/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}
