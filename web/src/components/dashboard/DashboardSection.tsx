import { Button, Card, Typography } from 'antd';
import { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';

interface DashboardSectionProps {
  title: string;
  children: ReactNode;
  onViewAll?: () => void;
}

export function DashboardSection({ title, children, onViewAll }: DashboardSectionProps) {
  const { t } = useTranslation();

  return (
    <Card
      className="dashboard-section-card"
      title={<Typography.Text strong>{title}</Typography.Text>}
      extra={onViewAll ? <Button type="link" onClick={onViewAll}>{t('dashboard.viewAll')}</Button> : null}
    >
      {children}
    </Card>
  );
}
