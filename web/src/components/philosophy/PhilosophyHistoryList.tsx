import { DeleteOutlined } from '@ant-design/icons';
import { Button, Card, Empty, List, Popconfirm, Typography } from 'antd';
import type { PhilosophySession } from '../../api/philosophy';

interface PhilosophyHistoryListProps {
  sessions: PhilosophySession[];
  loading: boolean;
  selectedId?: number;
  onOpen: (id: number) => void;
  onDelete: (id: number) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
  formatDate: (value: string) => string;
}

export function PhilosophyHistoryList({
  sessions,
  loading,
  selectedId,
  onOpen,
  onDelete,
  t,
  formatDate,
}: PhilosophyHistoryListProps) {
  return (
    <Card className="philosophy-history-card" title={t('philosophy.history')}>
      <List
        loading={loading}
        dataSource={sessions}
        locale={{ emptyText: <Empty description={t('empty.noData')} /> }}
        renderItem={(session) => (
          <List.Item
            className={selectedId === session.id ? 'philosophy-history-item active' : 'philosophy-history-item'}
            actions={[
              <Popconfirm
                key="delete"
                title={t('philosophy.deleteConfirm')}
                onConfirm={() => onDelete(session.id)}
                okText={t('common.confirm')}
                cancelText={t('common.cancel')}
              >
                <Button type="text" danger icon={<DeleteOutlined />} aria-label={t('philosophy.deleteHistory')} />
              </Popconfirm>,
            ]}
          >
            <button type="button" className="philosophy-history-button" onClick={() => onOpen(session.id)}>
              <Typography.Text strong ellipsis>{session.question}</Typography.Text>
              <Typography.Text type="secondary">{formatDate(session.createdAt)}</Typography.Text>
            </button>
          </List.Item>
        )}
      />
    </Card>
  );
}
