import { Button, Image, Text, View } from '@tarojs/components';
import Taro, { useDidShow } from '@tarojs/taro';
import { PageShell } from '../../components/PageShell';
import { useAppStore } from '../../store/appStore';
import { useAuthStore } from '../../store/authStore';
import { requireLogin } from '../../utils/auth';
import './index.scss';

export default function ProfilePage() {
  const user = useAuthStore((state) => state.user);
  const fetchCurrentUser = useAuthStore((state) => state.fetchCurrentUser);
  const logout = useAuthStore((state) => state.logout);
  const resetAppState = useAppStore((state) => state.resetAppState);
  const unreadNotificationCount = useAppStore((state) => state.unreadNotificationCount);

  useDidShow(() => {
    if (!requireLogin()) return;
    fetchCurrentUser().catch(() => undefined);
  });

  function handleLogout() {
    resetAppState();
    logout();
  }

  return (
    <PageShell>
      <View className="page profile-page">
        <View className="profile-card card">
          {user?.avatarUrl ? (
            <Image className="profile-avatar" src={user.avatarUrl} mode="aspectFill" />
          ) : (
            <View className="profile-avatar profile-avatar--placeholder">L</View>
          )}
          <Text className="profile-name">{user?.username || 'LifeLink 用户'}</Text>
          <Text className="profile-meta">{user?.email || user?.phone || '微信小程序端'}</Text>
        </View>

        <View className="profile-menu card">
          <View className="profile-menu__item" onClick={() => Taro.navigateTo({ url: '/pages/notifications/index' })}>
            <Text>通知中心</Text>
            <Text className="muted">{unreadNotificationCount} 未读</Text>
          </View>
          <View className="profile-menu__item">
            <Text>当前版本</Text>
            <Text className="muted">0.1.0</Text>
          </View>
          <View className="profile-menu__item" onClick={() => Taro.showToast({ title: '设置入口后续完善', icon: 'none' })}>
            <Text>设置</Text>
            <Text className="muted">›</Text>
          </View>
        </View>

        <Button className="ghost-button profile-logout danger-text" onClick={handleLogout}>
          退出登录
        </Button>
      </View>
    </PageShell>
  );
}
