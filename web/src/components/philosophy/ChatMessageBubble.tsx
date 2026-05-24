import { CopyOutlined } from '@ant-design/icons';
import { Avatar, Button, Typography } from 'antd';
import type { PhilosophyChatMessage } from '../../api/philosophy';

interface ChatMessageBubbleProps {
  message: PhilosophyChatMessage;
  philosopherName?: string;
  philosopherCode?: string;
  onCopy: (content: string) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function ChatMessageBubble({ message, philosopherName, philosopherCode, onCopy, t }: ChatMessageBubbleProps) {
  const isUser = message.role === 'USER';
  const isCounselor = philosopherCode === 'PSYCHOLOGY_TEACHER';

  return (
    <div className={`${isUser ? 'chat-message-row user' : 'chat-message-row assistant'}${isCounselor ? ' counselor-chat-row' : ''}`}>
      {!isUser && (
        <Avatar className={isCounselor ? 'chat-message-avatar counselor-chat-avatar' : 'chat-message-avatar'}>
          {(philosopherName || 'AI').slice(0, 1).toUpperCase()}
        </Avatar>
      )}
      <div className="chat-message-bubble">
        <Typography.Paragraph>{message.content}</Typography.Paragraph>
        {!isUser && (
          <Button
            size="small"
            type="text"
            icon={<CopyOutlined />}
            onClick={() => onCopy(message.content)}
          >
            {t('philosophy.copyMessage')}
          </Button>
        )}
      </div>
    </div>
  );
}
