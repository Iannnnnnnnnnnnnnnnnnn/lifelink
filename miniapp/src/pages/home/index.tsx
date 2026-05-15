import { Button, Image, Text, View } from '@tarojs/components';
import Taro, { useDidShow } from '@tarojs/taro';
import { useState } from 'react';
import { getAnniversaries, type Anniversary } from '../../api/anniversary';
import { getDailyPosts, type DailyPost } from '../../api/daily';
import { getNotifications, type NotificationItem } from '../../api/notification';
import { getTodos, type SpaceTodo } from '../../api/todo';
import { EmptyState } from '../../components/EmptyState';
import { PageShell } from '../../components/PageShell';
import { useAppStore } from '../../store/appStore';
import { useAuthStore } from '../../store/authStore';
import { requireLogin } from '../../utils/auth';
import { formatDateTime, getAnniversaryDisplayText, truncate } from '../../utils/format';
import { goDailyCreate, goRelationshipDetail, goTodos } from '../../utils/navigation';
import './index.scss';

export default function HomePage() {
  const user = useAuthStore((state) => state.user);
  const fetchCurrentUser = useAuthStore((state) => state.fetchCurrentUser);
  const relationships = useAppStore((state) => state.relationships);
  const refreshRelationshipsAndTheme = useAppStore((state) => state.refreshRelationshipsAndTheme);
  const refreshUnreadCount = useAppStore((state) => state.refreshUnreadCount);
  const [dailyPosts, setDailyPosts] = useState<DailyPost[]>([]);
  const [anniversaries, setAnniversaries] = useState<Anniversary[]>([]);
  const [todos, setTodos] = useState<SpaceTodo[]>([]);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(false);

  async function loadDashboard() {
    if (!requireLogin()) return;
    setLoading(true);
    try {
      await fetchCurrentUser().catch(() => undefined);
      const rels = await refreshRelationshipsAndTheme();
      await refreshUnreadCount().catch(() => undefined);
      const [dailyResult, anniversaryResult, notificationResult] = await Promise.allSettled([
        getDailyPosts({ page: 1, size: 3 }),
        getAnniversaries({ page: 1, size: 3 }),
        getNotifications({ page: 1, size: 5 })
      ]);
      if (dailyResult.status === 'fulfilled') setDailyPosts(dailyResult.value.slice(0, 3));
      if (anniversaryResult.status === 'fulfilled') setAnniversaries(anniversaryResult.value.slice(0, 3));
      if (notificationResult.status === 'fulfilled') setNotifications(notificationResult.value.slice(0, 5));

      const todoResults = await Promise.allSettled(
        rels.slice(0, 3).map((item) => getTodos(item.id, { status: 'TODO', page: 1, size: 5 }))
      );
      const unfinished = todoResults.flatMap((item) => (item.status === 'fulfilled' ? item.value : []));
      setTodos(unfinished.slice(0, 5));
    } finally {
      setLoading(false);
    }
  }

  useDidShow(() => {
    loadDashboard();
  });

  return (
    <PageShell>
      <View className="page home-page">
        <View className="home-hero">
          <Text className="home-hero__hello">欢迎回来，{user?.username || 'LifeLink'}</Text>
          <Text className="home-hero__desc">今天也记录一点属于你们的日常吧</Text>
          <View className="home-hero__stickers">
            <Text>✨</Text>
            <Text>💌</Text>
            <Text>📅</Text>
          </View>
        </View>

        <View className="home-stats">
          <View className="home-stat card" onClick={() => Taro.switchTab({ url: '/pages/relationships/index' })}>
            <Text className="home-stat__num">{relationships.length}</Text>
            <Text className="home-stat__label">关系空间</Text>
          </View>
          <View className="home-stat card" onClick={() => goTodos(relationships[0]?.id)}>
            <Text className="home-stat__num">{todos.length}</Text>
            <Text className="home-stat__label">未完成代办</Text>
          </View>
          <View className="home-stat card" onClick={() => Taro.switchTab({ url: '/pages/anniversaries/index' })}>
            <Text className="home-stat__num">{anniversaries.length}</Text>
            <Text className="home-stat__label">最近纪念日</Text>
          </View>
        </View>

        <View className="section-title">
          <Text>快捷入口</Text>
        </View>
        <View className="home-actions">
          <Button className="ghost-button" onClick={() => goDailyCreate(relationships[0]?.id)}>
            发布日常
          </Button>
          <Button className="ghost-button" onClick={() => Taro.switchTab({ url: '/pages/relationships/index' })}>
            查看关系
          </Button>
          <Button className="ghost-button" onClick={() => goTodos(relationships[0]?.id)}>
            新增代办
          </Button>
          <Button className="ghost-button" onClick={() => Taro.switchTab({ url: '/pages/anniversaries/index' })}>
            纪念日
          </Button>
        </View>

        <View className="section-title">
          <Text>最近日常</Text>
          <Text className="muted" onClick={() => Taro.switchTab({ url: '/pages/daily/index' })}>全部</Text>
        </View>
        {dailyPosts.length ? (
          dailyPosts.map((item) => (
            <View className="home-list-card card" key={item.id}>
              <Text className="home-list-card__title">{item.username || '成员'} · {item.relationshipName || '关系空间'}</Text>
              <Text className="home-list-card__content">{truncate(item.content, 64)}</Text>
              {item.images?.[0]?.url ? <Image className="home-list-card__image" src={item.images[0].url} mode="aspectFill" /> : null}
              <Text className="muted">{formatDateTime(item.createdAt)}</Text>
            </View>
          ))
        ) : (
          <EmptyState title={loading ? '加载中...' : '暂无日常'} actionText="发布日常" onAction={() => goDailyCreate()} />
        )}

        <View className="section-title">
          <Text>未完成代办</Text>
        </View>
        {todos.length ? (
          todos.map((item) => (
            <View className="home-row card" key={item.id}>
              <Text>{item.title}</Text>
              <Text className="home-tag">{item.priority || 'NORMAL'}</Text>
            </View>
          ))
        ) : (
          <EmptyState title={loading ? '加载中...' : '今天没有待办'} />
        )}

        <View className="section-title">
          <Text>最近纪念日</Text>
        </View>
        {anniversaries.length ? (
          anniversaries.map((item) => (
            <View className="home-anniversary card" key={item.id}>
              {item.backgroundUrl ? <Image className="home-anniversary__bg" src={item.backgroundUrl} mode="aspectFill" /> : null}
              <View className="home-anniversary__mask">
                <Text className="home-anniversary__count">{item.dayCount || 0}</Text>
                <Text>{getAnniversaryDisplayText(item)}</Text>
              </View>
            </View>
          ))
        ) : (
          <EmptyState title={loading ? '加载中...' : '暂无纪念日'} />
        )}

        <View className="section-title">
          <Text>最近通知</Text>
          <Text className="muted" onClick={() => Taro.navigateTo({ url: '/pages/notifications/index' })}>全部</Text>
        </View>
        {notifications.length ? (
          notifications.map((item) => (
            <View className={`home-row card ${item.readStatus === 'UNREAD' ? 'home-row--unread' : ''}`} key={item.id}>
              <Text>{item.content || item.title}</Text>
              <Text className="muted">{formatDateTime(item.createdAt)}</Text>
            </View>
          ))
        ) : (
          <EmptyState title={loading ? '加载中...' : '暂无通知'} />
        )}
      </View>
    </PageShell>
  );
}
