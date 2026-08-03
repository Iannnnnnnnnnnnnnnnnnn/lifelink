import { CopyOutlined, ReloadOutlined } from '@ant-design/icons';
import { Avatar, Button, Space, Typography } from 'antd';
import type { PhilosophyChatMessage } from '../../api/philosophy';

export type ChatDisplayMessage = PhilosophyChatMessage & {
  deliveryState?: 'pending' | 'failed';
  retryContent?: string;
};

interface ChatMessageBubbleProps {
  message: ChatDisplayMessage;
  philosopherName?: string;
  philosopherCode?: string;
  onCopy: (content: string) => void;
  onRetry: (content: string, failedMessageId: number) => void;
  retrying: boolean;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function ChatMessageBubble({ message, philosopherName, philosopherCode, onCopy, onRetry, retrying, t }: ChatMessageBubbleProps) {
  const isUser = message.role === 'USER';
  const isCounselor = philosopherCode === 'PSYCHOLOGY_TEACHER';

  return (
    <div className={`${isUser ? 'chat-message-row user' : 'chat-message-row assistant'}${isCounselor ? ' counselor-chat-row' : ''}`}>
      {!isUser && (
        <Avatar className={isCounselor ? 'chat-message-avatar counselor-chat-avatar' : 'chat-message-avatar'}>
          {(philosopherName || 'AI').slice(0, 1).toUpperCase()}
        </Avatar>
      )}
      <div className={`chat-message-bubble${message.deliveryState ? ` ${message.deliveryState}` : ''}`}>
        <Typography.Paragraph>{message.content}</Typography.Paragraph>
        {message.deliveryState !== 'pending' && (
          <Space size={4} className="chat-message-actions">
          <Button
            size="small"
            type="text"
            icon={<CopyOutlined />}
            onClick={() => onCopy(message.content)}
          >
            {t('philosophy.copyMessage')}
          </Button>
          {message.deliveryState === 'failed' && message.retryContent && (
            <Button
              size="small"
              type="text"
              danger
              icon={<ReloadOutlined />}
              loading={retrying}
              onClick={() => onRetry(message.retryContent || '', message.id)}
            >
              {t('error.retry')}
            </Button>
          )}
          </Space>
        )}
      </div>
    </div>
  );
}
