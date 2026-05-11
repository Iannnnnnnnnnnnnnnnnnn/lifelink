import axios from 'axios';

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export const TOKEN_STORAGE_KEY = 'lifelink_token';

export const request = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 10000,
});

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
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      window.dispatchEvent(new Event('lifelink:unauthorized'));
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  },
);
