import Taro from '@tarojs/taro';
import { create } from 'zustand';
import type { UserProfile } from '../api/auth';

export interface AuthState {
  token: string;
  user?: UserProfile;
  isLoggedIn: boolean;
  restore: () => void;
  login: (token: string, user?: UserProfile) => void;
  logout: (redirect?: boolean) => void;
  fetchCurrentUser: () => Promise<void>;
}

export const TOKEN_STORAGE_KEY = 'lifelink_token';

export const useAuthStore = create<AuthState>((set, get) => ({
  token: Taro.getStorageSync(TOKEN_STORAGE_KEY) || '',
  user: undefined,
  isLoggedIn: Boolean(Taro.getStorageSync(TOKEN_STORAGE_KEY)),
  restore: () => {
    const token = Taro.getStorageSync(TOKEN_STORAGE_KEY) || '';
    set({ token, isLoggedIn: Boolean(token) });
  },
  login: (token, user) => {
    Taro.setStorageSync(TOKEN_STORAGE_KEY, token);
    set({ token, user, isLoggedIn: true });
  },
  logout: (redirect = true) => {
    Taro.removeStorageSync(TOKEN_STORAGE_KEY);
    set({ token: '', user: undefined, isLoggedIn: false });
    if (redirect) {
      Taro.redirectTo({ url: '/pages/login/index' });
    }
  },
  fetchCurrentUser: async () => {
    if (!get().token) return;
    const { getCurrentUser } = await import('../api/auth');
    const user = await getCurrentUser();
    set({ user, isLoggedIn: true });
  }
}));
