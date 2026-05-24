import { Button, Card, Input, Space } from 'antd';
import { ClearOutlined, SendOutlined } from '@ant-design/icons';
import type { Philosopher } from '../../api/philosophy';
import { PhilosopherSelector } from './PhilosopherSelector';

const { TextArea } = Input;

interface PhilosophyInputPanelProps {
  question: string;
  selectedCodes: string[];
  philosophers: Philosopher[];
  loading: boolean;
  hasResult: boolean;
  onQuestionChange: (value: string) => void;
  onSelectedCodesChange: (value: string[]) => void;
  onGenerate: () => void;
  onClear: () => void;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function PhilosophyInputPanel({
  question,
  selectedCodes,
  philosophers,
  loading,
  hasResult,
  onQuestionChange,
  onSelectedCodesChange,
  onGenerate,
  onClear,
  t,
}: PhilosophyInputPanelProps) {
  return (
    <Card className="philosophy-input-card">
      <Space direction="vertical" size={16} className="philosophy-input-stack">
        <TextArea
          value={question}
          onChange={(event) => onQuestionChange(event.target.value)}
          placeholder={t('philosophy.questionPlaceholder')}
          maxLength={1000}
          showCount
          autoSize={{ minRows: 4, maxRows: 8 }}
        />
        <PhilosopherSelector
          philosophers={philosophers}
          value={selectedCodes}
          onChange={onSelectedCodesChange}
          placeholder={t('philosophy.selectPhilosophers')}
        />
        <div className="philosophy-input-actions">
          <Button icon={<ClearOutlined />} onClick={onClear}>
            {t('philosophy.clear')}
          </Button>
          <Button type="primary" icon={<SendOutlined />} loading={loading} onClick={onGenerate}>
            {hasResult ? t('philosophy.regenerate') : t('philosophy.generate')}
          </Button>
        </div>
      </Space>
    </Card>
  );
}
