import { CopyOutlined } from '@ant-design/icons';
import { Button, Card, Space, Tag, Typography } from 'antd';
import type { Philosopher, PhilosophyResponseItem } from '../../api/philosophy';

interface PhilosopherCardProps {
  item: PhilosophyResponseItem;
  philosopher?: Philosopher;
  onCopy: (item: PhilosophyResponseItem) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function PhilosopherCard({ item, philosopher, onCopy, t }: PhilosopherCardProps) {
  return (
    <Card className={`philosopher-card philosopher-card-${item.philosopherCode.toLowerCase()}`}>
      <div className="philosopher-card-header">
        <div>
          <Typography.Title level={4}>{item.philosopherName}</Typography.Title>
          {philosopher?.era && <Typography.Text type="secondary">{philosopher.era}</Typography.Text>}
        </div>
        <Button icon={<CopyOutlined />} onClick={() => onCopy(item)}>
          {t('philosophy.copy')}
        </Button>
      </div>
      {!!philosopher?.tags?.length && (
        <Space size={[6, 6]} wrap className="philosopher-card-tags">
          {philosopher.tags.map((tag) => (
            <Tag key={tag}>{tag}</Tag>
          ))}
        </Space>
      )}
      <div className="philosophy-card-section">
        <Typography.Text className="philosophy-section-label">{t('philosophy.viewpoint')}</Typography.Text>
        <Typography.Paragraph>{item.viewpoint}</Typography.Paragraph>
      </div>
      <div className="philosophy-card-section philosophy-question-back">
        <Typography.Text className="philosophy-section-label">{t('philosophy.questionBack')}</Typography.Text>
        <Typography.Paragraph>{item.questionBack}</Typography.Paragraph>
      </div>
      <div className="philosophy-card-section">
        <Typography.Text className="philosophy-section-label">{t('philosophy.objection')}</Typography.Text>
        <Typography.Paragraph>{item.objection}</Typography.Paragraph>
      </div>
      <div className="philosophy-summary-bar">
        <Typography.Text strong>{t('philosophy.summary')}</Typography.Text>
        <Typography.Text>{item.summary}</Typography.Text>
      </div>
    </Card>
  );
}
