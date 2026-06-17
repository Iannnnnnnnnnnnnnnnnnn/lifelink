import axios from 'axios';
import { message } from 'antd';
import i18n from '../i18n';

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export const TOKEN_STORAGE_KEY = 'lifelink_token';
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

let unauthorizedHandled = false;
let lastErrorMessageAt = 0;
let lastErrorMessage = '';

function showErrorOnce(content: string) {
  const now = Date.now();
  if (content === lastErrorMessage && now - lastErrorMessageAt < 1500) {
    return;
  }
  lastErrorMessage = content;
  lastErrorMessageAt = now;
  message.error(content);
}

function getFallbackMessage(status?: number, url?: string) {
  if (status === 401) return i18n.t('error.unauthorized');
  if (status === 403 && url?.includes('/api/philosophy')) return i18n.t('philosophy.accessDenied');
  if (status === 403) return i18n.t('error.forbidden');
  if (status === 404) return i18n.t('error.notFound');
  if (!status) return i18n.t('error.network');
  return i18n.t('error.server');
}

request.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const backendMessage = error.response?.data?.message;
    const requestUrl = error.config?.url;
    const fallbackMessage = getFallbackMessage(status, requestUrl);
    const displayMessage = status === 403 && requestUrl?.includes('/api/philosophy')
      ? fallbackMessage
      : backendMessage || fallbackMessage;
    error.__lifelinkHandled = true;

    if (status === 401) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      window.dispatchEvent(new Event('lifelink:unauthorized'));

      const isAuthPage = ['/login', '/register'].includes(window.location.pathname);
      if (!unauthorizedHandled) {
        unauthorizedHandled = true;
        if (!isAuthPage) {
          showErrorOnce(displayMessage);
        }
      }
      if (!isAuthPage) {
        window.location.href = '/login';
      }
    } else if (status === 403 || status === 404 || status >= 500 || !status) {
      showErrorOnce(displayMessage);
    }
    return Promise.reject(error);
  },
);
