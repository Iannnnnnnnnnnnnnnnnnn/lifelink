import { SendOutlined } from '@ant-design/icons';
import { Button, Input } from 'antd';
import { useState } from 'react';

const { TextArea } = Input;

interface ChatInputBoxProps {
  disabled: boolean;
  loading: boolean;
  onSend: (content: string) => Promise<void>;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function ChatInputBox({ disabled, loading, onSend, t }: ChatInputBoxProps) {
  const [value, setValue] = useState('');

  const submit = async () => {
    const content = value.trim();
    if (!content || disabled || loading) {
      return;
    }
    await onSend(content);
    setValue('');
  };

  return (
    <div className="chat-input-box">
      <TextArea
        value={value}
        disabled={disabled || loading}
        placeholder={t('philosophy.chatPlaceholder')}
        autoSize={{ minRows: 1, maxRows: 4 }}
        maxLength={2000}
        onChange={(event) => setValue(event.target.value)}
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
