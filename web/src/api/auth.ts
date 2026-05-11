import { ApiResult, request } from './request';

export interface RegisterRequest {
  username: string;
  email?: string;
  phone?: string;
  password: string;
}

export interface LoginRequest {
  account: string;
  password: string;
}

export interface UserProfile {
  id: number;
  username: string;
  email?: string;
  phone?: string;
  avatarUrl?: string;
  status: string;
  createdAt?: string;
}

export interface LoginResponse {
  token: string;
  user: UserProfile;
}

export function register(data: RegisterRequest) {
  return request.post<ApiResult<UserProfile>>('/api/auth/register', data);
}

export function login(data: LoginRequest) {
  return request.post<ApiResult<LoginResponse>>('/api/auth/login', data);
}

export function getCurrentUser() {
  return request.get<ApiResult<UserProfile>>('/api/user/me');
}
