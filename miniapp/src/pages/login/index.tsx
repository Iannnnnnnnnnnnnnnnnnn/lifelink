import { Button, Text, View } from '@tarojs/components';
import Taro, { useLoad } from '@tarojs/taro';
import { useState } from 'react';
import { wechatLogin } from '../../api/auth';
import { useAppStore } from '../../store/appStore';
import { useAuthStore } from '../../store/authStore';
import { redirectHomeIfLoggedIn } from '../../utils/auth';
import './index.scss';

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const login = useAuthStore((state) => state.login);
  const refreshRelationshipsAndTheme = useAppStore((state) => state.refreshRelationshipsAndTheme);
  const refreshUnreadCount = useAppStore((state) => state.refreshUnreadCount);

  useLoad(() => {
    redirectHomeIfLoggedIn();
  });

  async function handleWechatLogin() {
    setLoading(true);
    try {
      const wxLogin = await Taro.login();
      if (!wxLogin.code) {
        throw new Error('未获取到微信登录凭证');
      }
      const result = await wechatLogin(wxLogin.code);
      login(result.token, result.user);
      await Promise.allSettled([refreshRelationshipsAndTheme(), refreshUnreadCount()]);
      Taro.switchTab({ url: '/pages/home/index' });
    } catch (error) {
      Taro.showToast({
        title: (error as Error).message || '微信登录失败，请确认后端已支持微信登录',
        icon: 'none'
      });
    } finally {
      setLoading(false);
    }
  }

  return (
    <View className="login-page">
      <View className="login-card card">
        <Text className="login-logo">LifeLink</Text>
        <Text className="login-slogan">记录关系里的每一个重要日常</Text>
        <Text className="login-note">使用微信登录后开始记录你们的生活。</Text>
        <Button className="primary-button login-button" loading={loading} onClick={handleWechatLogin}>
          微信登录
        </Button>
        <Text className="login-tip">需要后端提供 /api/auth/wechat-login 接口。</Text>
      </View>
    </View>
  );
}
