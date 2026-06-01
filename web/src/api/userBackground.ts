import { ApiResult, request } from './request';

export type BackgroundScope = 'GLOBAL';

export type BackgroundPresetPosition =
  | 'CENTER'
  | 'TOP'
  | 'BOTTOM'
  | 'LEFT'
  | 'RIGHT'
  | 'TOP_LEFT'
  | 'TOP_RIGHT'
  | 'BOTTOM_LEFT'
  | 'BOTTOM_RIGHT';

export interface UserBackgroundSetting {
  enabled: boolean;
  imageUrl?: string | null;
  objectKey?: string | null;
  scale: number;
  positionX: number;
  positionY: number;
  presetPosition: BackgroundPresetPosition;
  opacity: number;
  blur: number;
  overlayOpacity: number;
  scope: BackgroundScope;
}

export interface SaveUserBackgroundSettingRequest {
  enabled: boolean;
  objectKey?: string | null;
  scale: number;
  positionX: number;
  positionY: number;
  presetPosition: BackgroundPresetPosition;
  opacity: number;
  blur: number;
  overlayOpacity: number;
  scope: BackgroundScope;
}

export interface UserBackgroundUploadResponse {
  imageUrl: string;
  objectKey: string;
}

export function getUserBackgroundSetting() {
  return request.get<ApiResult<UserBackgroundSetting>>('/api/user-background/me');
}

export function saveUserBackgroundSetting(data: SaveUserBackgroundSettingRequest) {
  return request.put<ApiResult<UserBackgroundSetting>>('/api/user-background/me', data);
}

export function resetUserBackgroundSetting() {
  return request.delete<ApiResult<UserBackgroundSetting>>('/api/user-background/me');
}

export function uploadUserBackground(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<ApiResult<UserBackgroundUploadResponse>>('/api/user-background/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}
