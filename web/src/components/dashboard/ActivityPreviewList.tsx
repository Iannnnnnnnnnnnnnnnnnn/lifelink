import { Avatar, List, Skeleton, Space, Tag, Typography } from 'antd';
import { useTranslation } from 'react-i18next';
import { SpaceActivity } from '../../api/activity';
import { getActivityIcon, getActivityTag, getActivityText } from '../../utils/activity';
import { EmptyState } from '../decorations/EmptyState';

interface ActivityPreviewListProps {
  items: SpaceActivity[];
  loading: boolean;
  onOpen: (relationshipId: number) => void;
}

export function ActivityPreviewList({ items, loading, onOpen }: ActivityPreviewListProps) {
  const { t } = useTranslation();

  if (loading) {
    return <Skeleton active paragraph={{ rows: 4 }} />;
  }

  if (items.length === 0) {
    return <EmptyState description={t('dashboard.noActivities')} />;
  }

  return (
    <List
      className="dashboard-list"
      dataSource={items.slice(0, 5)}
      renderItem={(item) => (
        <List.Item className="dashboard-list-item" onClick={() => onOpen(item.relationshipId)}>
          <List.Item.Meta
            avatar={<Avatar src={item.actorAvatarUrl} icon={getActivityIcon(item.activityType)} />}
            title={<Typography.Text>{getActivityText(item, t)}</Typography.Text>}
            description={
              <Space wrap>
                <Tag>{item.relationshipName || '-'}</Tag>
                <Tag color="blue">{getActivityTag(item.activityType, t)}</Tag>
                <Typography.Text type="secondary">{item.createdAt}</Typography.Text>
              </Space>
            }
          />
        </List.Item>
      )}
    />
  );
}
