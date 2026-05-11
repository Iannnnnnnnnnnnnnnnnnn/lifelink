import { DeleteOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Image, message, Popconfirm, Select, Skeleton, Space, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { deleteDailyPost, DailyPost, getDailyPosts } from '../api/daily';
import { getRelationships, RelationshipSummary } from '../api/relationship';
import { useAuthStore } from '../store/authStore';
import { EmptyState } from '../components/decorations/EmptyState';

export function DailyTimeline() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const currentUser = useAuthStore((state) => state.user);
  const [posts, setPosts] = useState<DailyPost[]>([]);
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [relationshipId, setRelationshipId] = useState<number | undefined>();
  const [loading, setLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const loadPosts = async (nextRelationshipId = relationshipId) => {
    setLoading(true);
    try {
      const response = await getDailyPosts({ relationshipId: nextRelationshipId, page: 1, size: 20 });
      setPosts(response.data.data);
    } catch (error) {
      messageApi.error(t('daily.loadFailed'));
    } finally {
      setLoading(false);
    }
  };

  const loadRelationships = async () => {
    try {
      const response = await getRelationships();
      setRelationships(response.data.data);
    } catch (error) {
      messageApi.error(t('relationship.loadFailed'));
    }
  };

  const handleDelete = async (postId: number) => {
    try {
      await deleteDailyPost(postId);
      messageApi.success(t('daily.deleteSuccess'));
      loadPosts();
    } catch (error) {
      messageApi.error(t('daily.deleteFailed'));
    }
  };

  useEffect(() => {
    loadRelationships();
    loadPosts();
  }, []);

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('daily.title')}</Typography.Title>
          <Typography.Text type="secondary">{t('daily.subtitle')}</Typography.Text>
        </div>
        <Space>
          <Select
            allowClear
            placeholder={t('daily.filterRelationship')}
            className="relationship-filter"
            value={relationshipId}
            options={relationships.map((item) => ({ value: item.id, label: item.name }))}
            onChange={(value) => {
              setRelationshipId(value);
              loadPosts(value);
            }}
          />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={() => loadPosts()}>
            {t('common.refresh')}
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/daily/create')}>
            {t('daily.create')}
          </Button>
        </Space>
      </div>

      {loading && posts.length === 0 ? (
        <Card>
          <Skeleton active paragraph={{ rows: 5 }} />
        </Card>
      ) : posts.length === 0 ? (
        <Card>
          <EmptyState description={t('daily.empty')} />
        </Card>
      ) : (
        <Space direction="vertical" size={12} className="page-wide">
          {posts.map((post) => (
            <Card
              key={post.id}
              hoverable
              className="daily-card"
              title={
                <Space wrap>
                  <Typography.Text strong>{post.relationshipName}</Typography.Text>
                  {post.mood && <Tag color="processing">{post.mood}</Tag>}
                </Space>
              }
              extra={
                currentUser?.id === post.userId ? (
                  <Popconfirm
                    title={t('daily.deleteConfirm')}
                    okText={t('common.confirm')}
                    cancelText={t('common.cancel')}
                    onConfirm={(event) => {
                      event?.stopPropagation();
                      handleDelete(post.id);
                    }}
                  >
                    <Button danger size="small" icon={<DeleteOutlined />} onClick={(event) => event.stopPropagation()}>
                      {t('common.delete')}
                    </Button>
                  </Popconfirm>
                ) : null
              }
              onClick={() => navigate(`/daily/${post.id}`)}
            >
              <Space direction="vertical" size={12} className="full-width">
                <Typography.Paragraph className="daily-content">{post.content}</Typography.Paragraph>
                {post.images && post.images.length > 0 && (
                  <Image.PreviewGroup>
                    <Space wrap>
                      {post.images.map((image) => (
                        <Image
                          key={image.fileId}
                          src={image.url}
                          alt={image.originalName}
                          width={96}
                          height={96}
                          style={{ objectFit: 'cover', borderRadius: 6 }}
                        />
                      ))}
                    </Space>
                  </Image.PreviewGroup>
                )}
                <Space wrap className="daily-meta">
                  <Tag>{t('daily.author')}: {post.username}</Tag>
                  <Tag>{t('daily.relationship')}: {post.relationshipName}</Tag>
                  <Typography.Text type="secondary">{t('daily.createdAt')}: {post.createdAt}</Typography.Text>
                </Space>
              </Space>
            </Card>
          ))}
        </Space>
      )}
    </Space>
  );
}
