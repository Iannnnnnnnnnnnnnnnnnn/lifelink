import { Empty, Spin, Typography } from 'antd';
import { useEffect, useRef } from 'react';
import type { PhilosophyChatMessage } from '../../api/philosophy';
import { ChatMessageBubble } from './ChatMessageBubble';

interface ChatMessageListProps {
  messages: PhilosophyChatMessage[];
  loading: boolean;
  thinking: boolean;
  philosopherName?: string;
  philosopherCode?: string;
  onCopy: (content: string) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function ChatMessageList({ messages, loading, thinking, philosopherName, philosopherCode, onCopy, t }: ChatMessageListProps) {
  const bottomRef = useRef<HTMLDivElement | null>(null);
  const isCounselor = philosopherCode === 'PSYCHOLOGY_TEACHER';

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ block: 'end' });
  }, [messages.length, thinking]);

  if (loading) {
    return (
      <div className="chat-message-list center">
        <Spin />
      </div>
    );
  }

  if (!messages.length) {
    return (
      <div className="chat-message-list center">
        <Empty
          description={(
            <div>
              <Typography.Text strong>{t('philosophy.emptyChatTitle')}</Typography.Text>
              <br />
              <Typography.Text type="secondary">
                {isCounselor ? t('philosophy.psychologyTeacherIntro') : t('philosophy.emptyChatDesc')}
              </Typography.Text>
            </div>
          )}
        />
      </div>
    );
  }

  return (
    <div className="chat-message-list">
      {messages.map((message) => (
        <ChatMessageBubble
          key={message.id}
          message={message}
          philosopherName={philosopherName}
          philosopherCode={philosopherCode}
          onCopy={onCopy}
          t={t}
        />
      ))}
      {thinking && (
        <div className={`chat-message-row assistant${isCounselor ? ' counselor-chat-row' : ''}`}>
          <div className="chat-message-bubble thinking">{t('philosophy.thinking')}</div>
        </div>
      )}
      <div ref={bottomRef} />
    </div>
  );
}
