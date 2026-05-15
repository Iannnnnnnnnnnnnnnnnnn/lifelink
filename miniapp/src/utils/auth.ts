import Taro from '@tarojs/taro';
import { useAuthStore } from '../store/authStore';

export function requireLogin() {
  const token = useAuthStore.getState().token || Taro.getStorageSync('lifelink_token');
  if (!token) {
    Taro.redirectTo({ url: '/pages/login/index' });
    return false;
  }
  return true;
}

export function redirectHomeIfLoggedIn() {
  const token = useAuthStore.getState().token || Taro.getStorageSync('lifelink_token');
  if (token) {
    Taro.switchTab({ url: '/pages/home/index' });
    return true;
  }
  return false;
}
