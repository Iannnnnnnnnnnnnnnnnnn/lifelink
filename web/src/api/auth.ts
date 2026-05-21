import { ApiResult, request } from './request';
import type { UserProfile } from './user';
export { getCurrentUser } from './user';
export type { UserProfile } from './user';

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
