import { Avatar, Card, Space, Tag, Typography } from 'antd';
import type { Philosopher } from '../../api/philosophy';

interface PhilosopherProfileCardProps {
  philosopher?: Philosopher;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function PhilosopherProfileCard({ philosopher, t }: PhilosopherProfileCardProps) {
  if (!philosopher) {
    return (
      <Card className="philosopher-profile-card">
        <Typography.Text type="secondary">{t('philosophy.emptyChatTitle')}</Typography.Text>
      </Card>
    );
  }

  const initial = philosopher.name.slice(0, 1).toUpperCase();
  const isCounselor = philosopher.responseLayout === 'COUNSELOR_CARD' || philosopher.code === 'PSYCHOLOGY_TEACHER';

  return (
    <Card className={isCounselor ? 'philosopher-profile-card counselor-profile-card' : 'philosopher-profile-card'}>
      <Space align="start" size={12}>
        <Avatar size={48} className={`philosopher-avatar philosopher-avatar-${philosopher.code.toLowerCase()}`}>
          {initial}
        </Avatar>
        <div>
          <Typography.Title level={4}>{philosopher.name}</Typography.Title>
          <Typography.Text type="secondary">
            {isCounselor ? t('philosophy.psychologyTeacherSubtitle') : philosopher.era}
          </Typography.Text>
        </div>
      </Space>
      <Typography.Paragraph className="philosopher-profile-desc">{philosopher.description}</Typography.Paragraph>
      {!isCounselor && (
        <Space size={[6, 6]} wrap>
          {philosopher.tags.map((tag) => (
            <Tag key={tag}>{tag}</Tag>
          ))}
        </Space>
      )}
    </Card>
  );
}
