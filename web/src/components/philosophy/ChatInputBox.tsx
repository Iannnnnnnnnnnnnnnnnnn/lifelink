import { SendOutlined } from '@ant-design/icons';
import { Button, Input } from 'antd';
import { useEffect, useState } from 'react';

const { TextArea } = Input;

interface ChatInputBoxProps {
  disabled: boolean;
  loading: boolean;
  sessionId?: number;
  onSend: (content: string) => Promise<void>;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function ChatInputBox({ disabled, loading, sessionId, onSend, t }: ChatInputBoxProps) {
  const [value, setValue] = useState('');
  const draftKey = `lifelink:philosophy-chat-draft:${sessionId || 'new'}`;

  useEffect(() => {
    setValue(localStorage.getItem(draftKey) || '');
  }, [draftKey]);

  const updateValue = (nextValue: string) => {
    setValue(nextValue);
    if (nextValue) {
      localStorage.setItem(draftKey, nextValue);
    } else {
      localStorage.removeItem(draftKey);
    }
  };

  const submit = async () => {
    const content = value.trim();
    if (!content || disabled || loading) {
      return;
    }
    updateValue('');
    await onSend(content);
  };

  return (
    <div className="chat-input-box">
      <TextArea
        value={value}
        disabled={disabled}
        placeholder={t('philosophy.chatPlaceholder')}
        autoSize={{ minRows: 1, maxRows: 4 }}
        maxLength={2000}
        onChange={(event) => updateValue(event.target.value)}
        onPressEnter={(event) => {
          if (!event.shiftKey) {
            event.preventDefault();
            submit();
          }
        }}
      />
      <Button type="primary" icon={<SendOutlined />} loading={loading} disabled={disabled || !value.trim()} onClick={submit}>
        {t('philosophy.send')}
      </Button>
    </div>
  );
}
