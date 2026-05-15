import Taro from '@tarojs/taro';
import { config } from '../config';
import { useAuthStore } from '../store/authStore';

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface RequestOptions {
  method?: HttpMethod;
  data?: unknown;
  params?: Record<string, unknown>;
  header?: Record<string, string>;
  skipAuth?: boolean;
}

let handlingUnauthorized = false;

function buildUrl(path: string, params?: Record<string, unknown>) {
  const basePath = path.startsWith('http') ? path : `${config.apiBaseUrl}${path}`;
  if (!params) {
    return basePath;
  }

  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&');

  return query ? `${basePath}${basePath.includes('?') ? '&' : '?'}${query}` : basePath;
}

function normalizeResponse<T>(responseData: ApiResult<T> | T): T {
  const maybeResult = responseData as ApiResult<T>;
  if (typeof maybeResult === 'object' && maybeResult !== null && 'data' in maybeResult && 'code' in maybeResult) {
    return maybeResult.data;
  }
  return responseData as T;
}

function getErrorMessage(statusCode?: number, fallback?: string) {
  if (fallback) return fallback;
  if (statusCode === 401) return '登录已过期，请重新登录';
  if (statusCode === 403) return '没有权限执行此操作';
  if (statusCode === 404) return '内容不存在或已被删除';
  if (statusCode && statusCode >= 500) return '服务暂时不可用，请稍后重试';
  return '网络异常，请稍后重试';
}

async function handleUnauthorized() {
  if (handlingUnauthorized) return;
  handlingUnauthorized = true;
  useAuthStore.getState().logout(false);
  Taro.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
  setTimeout(() => {
    handlingUnauthorized = false;
  }, 1200);
  await Taro.redirectTo({ url: '/pages/login/index' });
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const token = useAuthStore.getState().token || Taro.getStorageSync('lifelink_token');
  const header: Record<string, string> = {
    'content-type': 'application/json',
    ...options.header
  };

  if (token && !options.skipAuth) {
    header.Authorization = `Bearer ${token}`;
  }

  try {
    const response = await Taro.request<ApiResult<T> | T>({
      url: buildUrl(path, options.params),
      method: options.method || 'GET',
      data: options.data,
      header
    });

    if (response.statusCode === 401) {
      await handleUnauthorized();
      throw new Error('Unauthorized');
    }

    if (response.statusCode < 200 || response.statusCode >= 300) {
      const data = response.data as Partial<ApiResult<T>>;
      const message = getErrorMessage(response.statusCode, data?.message);
      Taro.showToast({ title: message, icon: 'none' });
      const handledError = new Error(message) as Error & { handled?: boolean };
      handledError.handled = true;
      throw handledError;
    }

    return normalizeResponse<T>(response.data);
  } catch (error) {
    if ((error as Error).message !== 'Unauthorized' && !(error as Error & { handled?: boolean }).handled) {
      Taro.showToast({ title: (error as Error).message || '网络异常，请稍后重试', icon: 'none' });
    }
    throw error;
  }
}

export const http = {
  get: <T>(path: string, params?: Record<string, unknown>, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'GET', params }),
  post: <T>(path: string, data?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'POST', data }),
  put: <T>(path: string, data?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'PUT', data }),
  patch: <T>(path: string, data?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'PATCH', data }),
  delete: <T>(path: string, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'DELETE' })
};
