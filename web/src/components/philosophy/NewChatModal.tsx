import { Button, Modal, Space } from 'antd';
import { useState } from 'react';
import type { Philosopher } from '../../api/philosophy';
import { PhilosopherSelector } from './PhilosopherSelector';

interface NewChatModalProps {
  open: boolean;
  philosophers: Philosopher[];
  loading: boolean;
  onCancel: () => void;
  onStart: (code: string) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function NewChatModal({ open, philosophers, loading, onCancel, onStart, t }: NewChatModalProps) {
  const [selectedCodes, setSelectedCodes] = useState<string[]>([]);

  return (
    <Modal
      open={open}
      title={t('philosophy.newChat')}
      onCancel={onCancel}
      footer={null}
      destroyOnClose
    >
      <Space direction="vertical" size={16} className="new-chat-modal-body">
        <PhilosopherSelector
          philosophers={philosophers}
          value={selectedCodes}
          onChange={(value) => setSelectedCodes(value.slice(-1))}
          placeholder={t('philosophy.selectCharacter')}
        />
        <Button
          type="primary"
          block
          loading={loading}
          disabled={!selectedCodes[0]}
          onClick={() => onStart(selectedCodes[0])}
        >
          {t('philosophy.startChat')}
        </Button>
      </Space>
    </Modal>
  );
}
