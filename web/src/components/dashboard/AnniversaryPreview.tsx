import { Skeleton, Space, Tag, Typography } from 'antd';
import { useTranslation } from 'react-i18next';
import { Anniversary } from '../../api/anniversary';
import { getAnniversaryDisplayText, getRepeatTypeLabel } from '../../utils/anniversary';
import { EmptyState } from '../decorations/EmptyState';

interface AnniversaryPreviewProps {
  items: Anniversary[];
  loading: boolean;
  onOpen: (id: number) => void;
}

export function AnniversaryPreview({ items, loading, onOpen }: AnniversaryPreviewProps) {
  const { t } = useTranslation();

  if (loading) {
    return <Skeleton active paragraph={{ rows: 4 }} />;
  }

  if (items.length === 0) {
    return <EmptyState description={t('dashboard.noAnniversaries')} />;
  }

  return (
    <div className="dashboard-anniversary-grid">
      {items.slice(0, 3).map((item) => (
        <button
          key={item.id}
          type="button"
          className={`dashboard-anniversary-card dashboard-anniversary-${item.displayType.toLowerCase()}`}
          style={item.backgroundUrl ? { backgroundImage: `url(${item.backgroundUrl})` } : undefined}
          onClick={() => onOpen(item.id)}
        >
          <span className="dashboard-anniversary-overlay" />
          <span className="dashboard-anniversary-content">
            <Space wrap>
              <Tag>{item.relationshipName || '-'}</Tag>
              <Tag>{getRepeatTypeLabel(item.repeatType, t)}</Tag>
            </Space>
            <Typography.Title level={4}>{item.title}</Typography.Title>
            <strong>{item.dayCount}</strong>
            <span>{getAnniversaryDisplayText(item, t)}</span>
          </span>
        </button>
      ))}
    </div>
  );
}
