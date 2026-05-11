import { Card, Typography } from 'antd';
import { ReactNode } from 'react';

interface StatCardProps {
  icon: ReactNode;
  title: string;
  value: number;
  description: string;
  onClick: () => void;
}

export function StatCard({ icon, title, value, description, onClick }: StatCardProps) {
  return (
    <Card className="dashboard-stat-card" hoverable onClick={onClick}>
      <div className="dashboard-stat-icon">{icon}</div>
      <div>
        <Typography.Text type="secondary">{title}</Typography.Text>
        <div className="dashboard-stat-value">{value}</div>
        <Typography.Text type="secondary">{description}</Typography.Text>
      </div>
    </Card>
  );
}
