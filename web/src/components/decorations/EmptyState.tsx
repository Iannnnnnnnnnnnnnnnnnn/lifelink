import { Empty, Typography } from 'antd';
import { ReactNode } from 'react';

interface EmptyStateProps {
  description: ReactNode;
}

export function EmptyState({ description }: EmptyStateProps) {
  return (
    <div className="polished-empty">
      <div className="empty-illustration" aria-hidden="true">
        <span>✨</span>
        <span>📝</span>
        <span>💌</span>
      </div>
      <Empty
        image={Empty.PRESENTED_IMAGE_SIMPLE}
        description={
          <Typography.Text type="secondary">
            {description}
          </Typography.Text>
        }
      />
    </div>
  );
}
