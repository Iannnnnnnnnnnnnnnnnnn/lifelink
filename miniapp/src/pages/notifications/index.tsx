import { Button, Text, View } from '@tarojs/components';
import Taro, { useDidShow } from '@tarojs/taro';
import { useState } from 'react';
import { getNotifications, markAllAsRead, markAsRead, type NotificationItem } from '../../api/notification';
import { EmptyState } from '../../components/EmptyState';
import { PageShell } from '../../components/PageShell';
import { useAppStore } from '../../store/appStore';
import { requireLogin } from '../../utils/auth';
import { formatDateTime } from '../../utils/format';
import { getNotificationTargetUrl } from '../../utils/notification';
import './index.scss';

export default function NotificationsPage() {
  const refreshUnreadCount = useAppStore((state) => state.refreshUnreadCount);
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(false);

  async function loadItems() {
    if (!requireLogin()) return;
    setLoading(true);
    try {
      const data = await getNotifications({ page: 1, size: 50 });
      setItems(data);
      await refreshUnreadCount().catch(() => undefined);
    } finally {
      setLoading(false);
    }
  }

  async function handleClick(item: NotificationItem) {
    if (item.readStatus === 'UNREAD') {
      await markAsRead(item.id);
      await refreshUnreadCount().catch(() => undefined);
    }
    const url = getNotificationTargetUrl(item);
    if (url) {
      Taro.navigateTo({ url });
    }
  }

  async function handleReadAll() {
    await markAllAsRead();
    Taro.showToast({ title: '已全部标记为已读', icon: 'success' });
    loadItems();
  }

  useDidShow(() => {
    loadItems();
  });

  return (
    <PageShell>
      <View className="page notifications-page">
        <View className="notifications-header">
          <Text className="notifications-title">通知中心</Text>
          <Button className="ghost-button notifications-read-all" onClick={handleReadAll}>
            全部已读
          </Button>
        </View>

        {items.length ? (
          items.map((item) => (
            <View
              className={`notification-card card ${item.readStatus === 'UNREAD' ? 'notification-card--unread' : ''}`}
              key={item.id}
              onClick={() => handleClick(item)}
            >
              <View className="notification-card__top">
                <Text className="notification-card__title">{item.content || item.title}</Text>
                {item.readStatus === 'UNREAD' ? <Text className="notification-dot" /> : null}
              </View>
              <Text className="notification-card__meta">{item.relationshipName || item.notificationType}</Text>
              <Text className="notification-card__time">{formatDateTime(item.createdAt)}</Text>
            </View>
          ))
        ) : (
          <EmptyState title={loading ? '加载中...' : '暂无通知'} />
        )}
      </View>
    </PageShell>
  );
}
