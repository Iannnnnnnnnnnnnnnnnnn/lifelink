import {
  BellOutlined,
  CheckOutlined,
  DeleteOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { Avatar, Button, Drawer, Empty, Segmented, Spin, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import type { Dispatch, SetStateAction } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import {
  deleteNotification,
  getNotificationUnreadCount,
  getNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
  type NotificationItem,
  type NotificationReadStatus,
} from '../../api/notification';
import { formatDateTime } from '../../utils/date';

type NotificationFilter = 'ALL' | NotificationReadStatus;

interface NotificationDrawerProps {
  open: boolean;
  onClose: () => void;
  onUnreadCountChange: Dispatch<SetStateAction<number>>;
}

function metadataTargetUrl(notification: NotificationItem) {
  const targetUrl = notification.metadata?.targetUrl;
  return typeof targetUrl === 'string' && targetUrl.startsWith('/') ? targetUrl : null;
}

function getNotificationTarget(notification: NotificationItem) {
  const explicitTarget = metadataTargetUrl(notification);
  if (explicitTarget) return explicitTarget;

  const relationshipId = notification.relationshipId;
  const relatedId = notification.relatedId;
  switch (notification.relatedType) {
    case 'DAILY_POST':
      return relatedId ? `/daily/${relatedId}` : '/daily';
    case 'ANNIVERSARY':
      return relatedId ? `/anniversaries/${relatedId}` : '/anniversaries';
    case 'SPACE_TODO':
      return relationshipId ? `/relationships/${relationshipId}/todos` : '/relationships';
    case 'RELATIONSHIP':
      return relatedId || relationshipId ? `/relationships/${relatedId || relationshipId}` : '/relationships';
    case 'FOCUS_ROOM':
    case 'FOCUS_SESSION':
      return '/focus';
    case 'REWARD':
    case 'REWARD_REDEMPTION':
      return '/rewards';
    case 'CYCLE_WARNING':
    case 'CYCLE_DAILY_REPORT':
      return relationshipId ? `/relationships/${relationshipId}/cycle-care` : '/cycle-care';
    default:
      return relationshipId ? `/relationships/${relationshipId}` : null;
  }
}

export function NotificationDrawer({ open, onClose, onUnreadCountChange }: NotificationDrawerProps) {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [filter, setFilter] = useState<NotificationFilter>('ALL');
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [failed, setFailed] = useState(false);
  const [markingAll, setMarkingAll] = useState(false);

  const loadNotifications = async (nextFilter = filter) => {
    setLoading(true);
    setFailed(false);
    try {
      const response = await getNotifications({
        readStatus: nextFilter === 'ALL' ? undefined : nextFilter,
        page: 1,
        size: 50,
      });
      setItems(response.data.data);
    } catch {
      setFailed(true);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open) {
      void loadNotifications(filter);
    }
    // The drawer should refresh whenever it opens or the filter changes.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, filter]);

  const handleOpenNotification = async (notification: NotificationItem) => {
    if (notification.readStatus === 'UNREAD') {
      const nextItems = items.map((item) => (
        item.id === notification.id ? { ...item, readStatus: 'READ' as const } : item
      ));
      setItems(filter === 'UNREAD' ? nextItems.filter((item) => item.readStatus === 'UNREAD') : nextItems);
      onUnreadCountChange((count) => Math.max(0, count - 1));
      try {
        await markNotificationAsRead(notification.id);
      } catch {
        void loadNotifications(filter);
      }
    }

    const target = getNotificationTarget(notification);
    if (target) {
      onClose();
      navigate(target);
    }
  };

  const handleMarkAllAsRead = async () => {
    setMarkingAll(true);
    try {
      await markAllNotificationsAsRead();
      const nextItems = filter === 'UNREAD'
        ? []
        : items.map((item) => ({ ...item, readStatus: 'READ' as const }));
      setItems(nextItems);
      onUnreadCountChange(0);
      message.success(t('notification.markAllSuccess'));
    } catch {
      message.error(t('message.operationFailed'));
    } finally {
      setMarkingAll(false);
    }
  };

  const handleDelete = async (notification: NotificationItem) => {
    try {
      await deleteNotification(notification.id);
      const nextItems = items.filter((item) => item.id !== notification.id);
      setItems(nextItems);
      if (notification.readStatus === 'UNREAD') {
        getNotificationUnreadCount()
          .then((response) => onUnreadCountChange(response.data.data.count))
          .catch(() => undefined);
      }
    } catch {
      message.error(t('message.operationFailed'));
    }
  };

  const unreadItems = items.filter((item) => item.readStatus === 'UNREAD').length;

  return (
    <Drawer
      className="notification-drawer"
      width={440}
      title={t('notification.title')}
      open={open}
      onClose={onClose}
      destroyOnHidden
      extra={(
        <Button
          type="text"
          icon={<CheckOutlined />}
          disabled={unreadItems === 0}
          loading={markingAll}
          onClick={handleMarkAllAsRead}
        >
          {t('notification.markAllRead')}
        </Button>
      )}
    >
      <Segmented
        block
        className="notification-filter"
        value={filter}
        onChange={(value) => setFilter(value as NotificationFilter)}
        options={[
          { label: t('common.all'), value: 'ALL' },
          { label: t('notification.unread'), value: 'UNREAD' },
          { label: t('notification.read'), value: 'READ' },
        ]}
      />

      {loading ? (
        <div className="notification-state"><Spin /></div>
      ) : failed ? (
        <div className="notification-state">
          <Typography.Text type="secondary">{t('notification.loadFailed')}</Typography.Text>
          <Button icon={<ReloadOutlined />} onClick={() => void loadNotifications(filter)}>
            {t('error.retry')}
          </Button>
        </div>
      ) : items.length === 0 ? (
        <div className="notification-state">
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('empty.noNotifications')} />
        </div>
      ) : (
        <div className="notification-list">
          {items.map((notification) => {
            const target = getNotificationTarget(notification);
            return (
              <div
                key={notification.id}
                className={`notification-item ${notification.readStatus === 'UNREAD' ? 'is-unread' : ''} ${target ? 'is-clickable' : ''}`}
                role={target ? 'button' : undefined}
                tabIndex={target ? 0 : undefined}
                onClick={() => void handleOpenNotification(notification)}
                onKeyDown={(event) => {
                  if (target && (event.key === 'Enter' || event.key === ' ')) {
                    event.preventDefault();
                    void handleOpenNotification(notification);
                  }
                }}
              >
                <Avatar
                  className="notification-avatar"
                  src={notification.actorAvatarUrl || undefined}
                  icon={!notification.actorAvatarUrl ? <BellOutlined /> : undefined}
                />
                <div className="notification-copy">
                  <div className="notification-title-row">
                    <Typography.Text strong>{notification.title}</Typography.Text>
                    {notification.readStatus === 'UNREAD' && <span className="notification-unread-dot" />}
                  </div>
                  {notification.content && (
                    <Typography.Paragraph ellipsis={{ rows: 2 }}>{notification.content}</Typography.Paragraph>
                  )}
                  <div className="notification-meta">
                    {notification.relationshipName && <span>{notification.relationshipName}</span>}
                    <span>{formatDateTime(notification.createdAt, t, i18n.resolvedLanguage)}</span>
                  </div>
                </div>
                <Button
                  type="text"
                  danger
                  className="notification-delete"
                  icon={<DeleteOutlined />}
                  aria-label={t('notification.delete')}
                  onClick={(event) => {
                    event.stopPropagation();
                    void handleDelete(notification);
                  }}
                />
              </div>
            );
          })}
        </div>
      )}
    </Drawer>
  );
}
