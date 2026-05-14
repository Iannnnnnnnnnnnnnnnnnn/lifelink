import { Empty, Typography } from 'antd';
import type { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';

interface EmptyStateProps {
  title?: ReactNode;
  description?: ReactNode;
  action?: ReactNode;
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  const { t } = useTranslation();

  return (
    <div className="state-center empty-state">
      <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={false} />
      <Typography.Title level={5}>{title || t('empty.noData')}</Typography.Title>
      {description && <Typography.Text type="secondary">{description}</Typography.Text>}
      {action && <div className="empty-state-action">{action}</div>}
    </div>
  );
}
