import { DeleteOutlined, EditOutlined, PushpinOutlined } from '@ant-design/icons';
import { Button, Empty, Input, List, Modal, Popconfirm, Space, Tooltip, Typography } from 'antd';
import { useMemo, useState } from 'react';
import type { PhilosophyChatSession } from '../../api/philosophy';

interface ChatSessionListProps {
  sessions: PhilosophyChatSession[];
  loading: boolean;
  loadingMore: boolean;
  hasMore: boolean;
  selectedId?: number;
  onOpen: (id: number) => void;
  onDelete: (id: number) => void;
  onRename: (id: number, title: string) => Promise<void>;
  onLoadMore: () => void;
  t: (key: string, options?: Record<string, unknown>) => string;
  formatDate: (value?: string) => string;
}

export function ChatSessionList({
  sessions,
  loading,
  loadingMore,
  hasMore,
  selectedId,
  onOpen,
  onDelete,
  onRename,
  onLoadMore,
  t,
  formatDate,
}: ChatSessionListProps) {
  const [keyword, setKeyword] = useState('');
  const [renamingSession, setRenamingSession] = useState<PhilosophyChatSession | null>(null);
  const [renameValue, setRenameValue] = useState('');
  const [savingRename, setSavingRename] = useState(false);

  const filteredSessions = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLocaleLowerCase();
    if (!normalizedKeyword) {
      return sessions;
    }
    return sessions.filter((session) => [session.title, session.philosopherName, session.lastMessagePreview]
      .some((value) => value?.toLocaleLowerCase().includes(normalizedKeyword)));
  }, [keyword, sessions]);

  const beginRename = (session: PhilosophyChatSession) => {
    setRenamingSession(session);
    setRenameValue(session.title);
  };

  const saveRename = async () => {
    const title = renameValue.trim();
    if (!renamingSession || !title) {
      return;
    }
    setSavingRename(true);
    try {
      await onRename(renamingSession.id, title);
      setRenamingSession(null);
    } catch {
      // The parent surfaces the request error and keeps the editor open for retry.
    } finally {
      setSavingRename(false);
    }
  };

  return (
    <>
      <Input.Search
        className="chat-session-search"
        allowClear
        value={keyword}
        placeholder={t('common.search')}
        onChange={(event) => setKeyword(event.target.value)}
      />
      <List
        className="chat-session-list"
        loading={loading}
        dataSource={filteredSessions}
        locale={{ emptyText: <Empty description={t('empty.noData')} /> }}
        footer={hasMore && !keyword ? (
          <Button block type="text" loading={loadingMore} onClick={onLoadMore}>
            {t('philosophy.loadMoreChats')}
          </Button>
        ) : null}
        renderItem={(session) => (
          <List.Item
            className={selectedId === session.id ? 'chat-session-item active' : 'chat-session-item'}
            actions={[
              <Tooltip key="pin" title={t('philosophy.pinChatComingSoon')}>
                <Button type="text" disabled icon={<PushpinOutlined />} aria-label={t('philosophy.pinChat')} />
              </Tooltip>,
              <Button
                key="rename"
                type="text"
                icon={<EditOutlined />}
                aria-label={t('philosophy.renameChat')}
                onClick={() => beginRename(session)}
              />,
              <Popconfirm
                key="delete"
                title={t('philosophy.deleteChatConfirm')}
                okText={t('common.confirm')}
                cancelText={t('common.cancel')}
                onConfirm={() => onDelete(session.id)}
              >
                <Button type="text" danger icon={<DeleteOutlined />} aria-label={t('common.delete')} />
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

      <Modal
        title={t('philosophy.renameChat')}
        open={!!renamingSession}
        okText={t('common.save')}
        cancelText={t('common.cancel')}
        okButtonProps={{ disabled: !renameValue.trim(), loading: savingRename }}
        onOk={() => void saveRename()}
        onCancel={() => setRenamingSession(null)}
      >
        <Space direction="vertical" className="chat-rename-form">
          <Input
            value={renameValue}
            maxLength={100}
            autoFocus
            onChange={(event) => setRenameValue(event.target.value)}
            onPressEnter={() => void saveRename()}
          />
        </Space>
      </Modal>
    </>
  );
}
