import { http } from './request';

export interface UserProfile {
  id: number;
  username: string;
  email?: string;
  phone?: string;
  avatarUrl?: string;
  status?: string;
  createdAt?: string;
}

export interface LoginResponse {
  token: string;
  user: UserProfile;
}

export function wechatLogin(code: string) {
  // TODO: backend needs POST /api/auth/wechat-login to exchange wx.login code for JWT.
  return http.post<LoginResponse>('/api/auth/wechat-login', { code }, { skipAuth: true });
}

export function getCurrentUser() {
  return http.get<UserProfile>('/api/user/me');
}
