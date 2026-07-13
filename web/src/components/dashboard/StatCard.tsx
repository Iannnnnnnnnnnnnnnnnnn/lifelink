import { Card, Typography } from 'antd';
import { KeyboardEventHandler, ReactNode } from 'react';

interface StatCardProps {
  icon: ReactNode;
  title: string;
  value: number;
  description: string;
  onClick: () => void;
}

export function StatCard({ icon, title, value, description, onClick }: StatCardProps) {
  const handleKeyDown: KeyboardEventHandler<HTMLDivElement> = (event) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      onClick();
    }
  };

  return (
    <Card
      className="dashboard-stat-card"
      hoverable
      role="button"
      tabIndex={0}
      aria-label={title}
      onClick={onClick}
      onKeyDown={handleKeyDown}
    >
      <div className="dashboard-stat-icon">{icon}</div>
      <div>
        <Typography.Text type="secondary">{title}</Typography.Text>
        <div className="dashboard-stat-value">{value}</div>
        <Typography.Text type="secondary">{description}</Typography.Text>
      </div>
    </Card>
  );
}
