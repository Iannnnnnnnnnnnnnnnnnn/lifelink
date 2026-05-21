import { DeleteOutlined } from '@ant-design/icons';
import { Button, Empty, List, Popconfirm, Typography } from 'antd';
import type { PhilosophyChatSession } from '../../api/philosophy';

interface ChatSessionListProps {
  sessions: PhilosophyChatSession[];
  loading: boolean;
  selectedId?: number;
  onOpen: (id: number) => void;
  onDelete: (id: number) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
  formatDate: (value?: string) => string;
}

export function ChatSessionList({ sessions, loading, selectedId, onOpen, onDelete, t, formatDate }: ChatSessionListProps) {
  return (
    <List
      className="chat-session-list"
      loading={loading}
      dataSource={sessions}
      locale={{ emptyText: <Empty description={t('empty.noData')} /> }}
      renderItem={(session) => (
        <List.Item
          className={selectedId === session.id ? 'chat-session-item active' : 'chat-session-item'}
          actions={[
            <Popconfirm
              key="delete"
              title={t('philosophy.deleteChatConfirm')}
              okText={t('common.confirm')}
              cancelText={t('common.cancel')}
              onConfirm={() => onDelete(session.id)}
            >
              <Button type="text" danger icon={<DeleteOutlined />} />
            </Popconfirm>,
          ]}
        >
          <button type="button" className="chat-session-button" onClick={() => onOpen(session.id)}>
            <Typography.Text strong ellipsis>{session.title}</Typography.Text>
            <Typography.Text type="secondary">{session.philosopherName} · {formatDate(session.lastMessageAt || session.createdAt)}</Typography.Text>
            {!!session.lastMessagePreview && <Typography.Text type="secondary" ellipsis>{session.lastMessagePreview}</Typography.Text>}
          </button>
        </List.Item>
      )}
    />
  );
}
