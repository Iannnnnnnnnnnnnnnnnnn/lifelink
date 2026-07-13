import { ArrowDownOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Empty, Spin, Typography } from 'antd';
import { useEffect, useRef } from 'react';
import { useState } from 'react';
import { ChatMessageBubble, type ChatDisplayMessage } from './ChatMessageBubble';

interface ChatMessageListProps {
  messages: ChatDisplayMessage[];
  loading: boolean;
  sessionId?: number;
  philosopherName?: string;
  philosopherCode?: string;
  onCopy: (content: string) => void;
  onRetry: (content: string, failedMessageId: number) => void;
  onStartChat: () => void;
  retrying: boolean;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function ChatMessageList({
  messages,
  loading,
  sessionId,
  philosopherName,
  philosopherCode,
  onCopy,
  onRetry,
  onStartChat,
  retrying,
  t,
}: ChatMessageListProps) {
  const listRef = useRef<HTMLDivElement | null>(null);
  const nearBottomRef = useRef(true);
  const [showNewMessages, setShowNewMessages] = useState(false);
  const isCounselor = philosopherCode === 'PSYCHOLOGY_TEACHER';

  useEffect(() => {
    nearBottomRef.current = true;
    setShowNewMessages(false);
    requestAnimationFrame(() => {
      const list = listRef.current;
      if (list) {
        list.scrollTop = list.scrollHeight;
      }
    });
  }, [sessionId]);

  useEffect(() => {
    const list = listRef.current;
    if (!list) {
      return;
    }
    if (nearBottomRef.current) {
      requestAnimationFrame(() => {
        list.scrollTo({ top: list.scrollHeight, behavior: 'smooth' });
      });
      setShowNewMessages(false);
    } else if (messages.length) {
      setShowNewMessages(true);
    }
  }, [messages.length, messages[messages.length - 1]?.id, messages[messages.length - 1]?.deliveryState]);

  const handleScroll = () => {
    const list = listRef.current;
    if (!list) {
      return;
    }
    const nearBottom = list.scrollHeight - list.scrollTop - list.clientHeight < 120;
    nearBottomRef.current = nearBottom;
    if (nearBottom) {
      setShowNewMessages(false);
    }
  };

  const jumpToLatest = () => {
    const list = listRef.current;
    if (!list) {
      return;
    }
    nearBottomRef.current = true;
    setShowNewMessages(false);
    list.scrollTo({ top: list.scrollHeight, behavior: 'smooth' });
  };

  if (loading) {
    return (
      <div className="chat-message-list-shell">
        <div className="chat-message-list center">
          <Spin />
        </div>
      </div>
    );
  }

  if (!messages.length) {
    return (
      <div className="chat-message-list-shell">
        <div className="chat-message-list center">
          <Empty
            description={(
              <div className="chat-empty-content">
                <Typography.Text strong>{t('philosophy.emptyChatTitle')}</Typography.Text>
                <Typography.Text type="secondary">
                  {isCounselor ? t('philosophy.psychologyTeacherIntro') : t('philosophy.emptyChatDesc')}
                </Typography.Text>
                {!sessionId && (
                  <Button type="primary" size="large" icon={<PlusOutlined />} onClick={onStartChat}>
                    {t('philosophy.startChat')}
                  </Button>
                )}
              </div>
            )}
          />
        </div>
      </div>
    );
  }

  return (
    <div className="chat-message-list-shell">
      <div ref={listRef} className="chat-message-list" onScroll={handleScroll}>
        <div className="chat-message-content">
          {messages.map((message) => (
            <ChatMessageBubble
              key={message.id}
              message={message}
              philosopherName={philosopherName}
              philosopherCode={philosopherCode}
              onCopy={onCopy}
              onRetry={onRetry}
              retrying={retrying}
              t={t}
            />
          ))}
        </div>
      </div>
      {showNewMessages && (
        <Button className="chat-new-message-button" icon={<ArrowDownOutlined />} onClick={jumpToLatest}>
          {t('philosophy.newMessages')}
        </Button>
      )}
    </div>
  );
}
