import { Image, List, Skeleton, Space, Tag, Typography } from 'antd';
import { useTranslation } from 'react-i18next';
import { DailyPost } from '../../api/daily';
import { EmptyState } from '../decorations/EmptyState';

interface RecentDailyListProps {
  items: DailyPost[];
  loading: boolean;
  onOpen: (id: number) => void;
  onCreate: () => void;
}

export function RecentDailyList({ items, loading, onOpen, onCreate }: RecentDailyListProps) {
  const { t } = useTranslation();

  if (loading) {
    return <Skeleton active paragraph={{ rows: 4 }} />;
  }

  if (items.length === 0) {
    return <EmptyState description={<span>{t('dashboard.noDaily')} · <a onClick={onCreate}>{t('dashboard.createDaily')}</a></span>} />;
  }

  return (
    <List
      className="dashboard-list"
      dataSource={items.slice(0, 3)}
      renderItem={(post) => (
        <List.Item className="dashboard-list-item" onClick={() => onOpen(post.id)}>
          <List.Item.Meta
            title={
              <Space wrap>
                <Typography.Text strong>{post.username}</Typography.Text>
                <Tag>{post.relationshipName}</Tag>
                {post.mood && <Tag color="pink">{post.mood}</Tag>}
              </Space>
            }
            description={
              <Space direction="vertical" size={8} className="full-width">
                <Typography.Paragraph ellipsis={{ rows: 2 }} className="list-description">
                  {post.content}
                </Typography.Paragraph>
                {post.images?.length > 0 && (
                  <Space size={6}>
                    {post.images.slice(0, 3).map((image) => (
                      <Image key={image.fileId} src={image.url} alt={image.originalName} width={46} height={46} preview={false} className="dashboard-thumb" />
                    ))}
                  </Space>
                )}
                <Typography.Text type="secondary">{post.createdAt}</Typography.Text>
              </Space>
            }
          />
        </List.Item>
      )}
    />
  );
}
