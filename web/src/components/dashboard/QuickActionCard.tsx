import { Card, Typography } from 'antd';
import { ReactNode } from 'react';

interface QuickActionCardProps {
  icon: ReactNode;
  title: string;
  description: string;
  onClick: () => void;
}

export function QuickActionCard({ icon, title, description, onClick }: QuickActionCardProps) {
  return (
    <Card className="dashboard-action-card" hoverable onClick={onClick}>
      <div className="dashboard-action-icon">{icon}</div>
      <div>
        <Typography.Title level={4}>{title}</Typography.Title>
        <Typography.Text type="secondary">{description}</Typography.Text>
      </div>
    </Card>
  );
}
