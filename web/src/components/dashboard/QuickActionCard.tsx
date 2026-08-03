import { Card, Typography } from 'antd';
import { KeyboardEventHandler, ReactNode } from 'react';

interface QuickActionCardProps {
  icon: ReactNode;
  title: string;
  description: string;
  meta?: string;
  tone?: 'default' | 'couple' | 'locked';
  onClick: () => void;
}

export function QuickActionCard({ icon, title, description, meta, tone = 'default', onClick }: QuickActionCardProps) {
  const handleKeyDown: KeyboardEventHandler<HTMLDivElement> = (event) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      onClick();
    }
  };

  return (
    <Card
      className={`dashboard-action-card dashboard-action-card-${tone}`}
      hoverable
      role="button"
      tabIndex={0}
      aria-label={title}
      onClick={onClick}
      onKeyDown={handleKeyDown}
    >
      <div className="dashboard-action-icon">{icon}</div>
      <div>
        <Typography.Title level={4}>{title}</Typography.Title>
        <Typography.Text type="secondary">{description}</Typography.Text>
        {meta && <Typography.Text className="dashboard-action-meta">{meta}</Typography.Text>}
      </div>
    </Card>
  );
}
