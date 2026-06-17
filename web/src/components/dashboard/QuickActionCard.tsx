import { Card, Typography } from 'antd';
import { ReactNode } from 'react';

interface QuickActionCardProps {
  icon: ReactNode;
  title: string;
  description: string;
  meta?: string;
  tone?: 'default' | 'couple' | 'locked';
  onClick: () => void;
}

export function QuickActionCard({ icon, title, description, meta, tone = 'default', onClick }: QuickActionCardProps) {
  return (
    <Card className={`dashboard-action-card dashboard-action-card-${tone}`} hoverable onClick={onClick}>
      <div className="dashboard-action-icon">{icon}</div>
      <div>
        <Typography.Title level={4}>{title}</Typography.Title>
        <Typography.Text type="secondary">{description}</Typography.Text>
        {meta && <Typography.Text className="dashboard-action-meta">{meta}</Typography.Text>}
      </div>
    </Card>
  );
}
