import { DeleteOutlined, HeartFilled, HeartOutlined, MessageOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Image, message, Popconfirm, Select, Skeleton, Space, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { deleteDailyPost, DailyPost, getDailyPosts, likeDailyPost, unlikeDailyPost } from '../api/daily';
import { getRelationships, RelationshipSummary } from '../api/relationship';
import { useAuthStore } from '../store/authStore';
import { EmptyState } from '../components/decorations/EmptyState';
import { ErrorState } from '../components/common/ErrorState';
import { formatDateTime } from '../utils/date';
import { getPageErrorType, PageErrorType } from '../utils/error';

function getRelationshipIdFromSearch(searchParams: URLSearchParams) {
  const value = searchParams.get('relationshipId') || searchParams.get('spaceId');
  if (!value) {
    return undefined;
  }
  const id = Number(value);
  return Number.isFinite(id) ? id : undefined;
}

export function DailyTimeline() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const routeRelationshipId = getRelationshipIdFromSearch(searchParams);
  const currentUser = useAuthStore((state) => state.user);
  const [posts, setPosts] = useState<DailyPost[]>([]);
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [relationshipId, setRelationshipId] = useState<number | undefined>(routeRelationshipId);
  const [loading, setLoading] = useState(false);
  const [likingIds, setLikingIds] = useState<number[]>([]);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [messageApi, contextHolder] = message.useMessage();

  const loadPosts = async (nextRelationshipId = relationshipId) => {
    setLoading(true);
    try {
      const response = await getDailyPosts({ relationshipId: nextRelationshipId, page: 1, size: 20 });
      setPosts(response.data.data);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
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

  const handleToggleLike = async (post: DailyPost) => {
    setLikingIds((ids) => ids.concat(post.id));
    try {
      const response = post.likedByMe ? await unlikeDailyPost(post.id) : await likeDailyPost(post.id);
      setPosts((items) => items.map((item) => (item.id === post.id ? { ...item, ...response.data.data } : item)));
      messageApi.success(post.likedByMe ? t('dailyInteraction.unlikeSuccess') : t('dailyInteraction.likeSuccess'));
    } catch (error) {
      messageApi.error(t('common.failed'));
    } finally {
      setLikingIds((ids) => ids.filter((id) => id !== post.id));
    }
  };

  const handleRelationshipChange = (value?: number) => {
    const nextParams = new URLSearchParams(searchParams);
    if (value) {
      nextParams.set('relationshipId', String(value));
    } else {
      nextParams.delete('relationshipId');
    }
    nextParams.delete('spaceId');
    setRelationshipId(value);
    setSearchParams(nextParams, { replace: true });
  };

  useEffect(() => {
    loadRelationships();
  }, []);

  useEffect(() => {
    setRelationshipId(routeRelationshipId);
    loadPosts(routeRelationshipId);
  }, [routeRelationshipId]);

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
            onChange={handleRelationshipChange}
          />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={() => loadPosts()}>
            {t('common.refresh')}
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate(relationshipId ? `/daily/create?relationshipId=${relationshipId}` : '/daily/create')}>
            {t('daily.create')}
          </Button>
        </Space>
      </div>

      {pageError ? (
        <ErrorState type={pageError} onRetry={() => loadPosts()} />
      ) : loading && posts.length === 0 ? (
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
                  <Typography.Text type="secondary">{t('daily.createdAt')}: {formatDateTime(post.createdAt, t, i18n.resolvedLanguage)}</Typography.Text>
                </Space>
                <div className="daily-interaction-bar" onClick={(event) => event.stopPropagation()}>
                  <Space className="daily-interaction-actions">
                    <Button
                      size="small"
                      className={`daily-like-button ${post.likedByMe ? 'liked' : ''}`}
                      icon={post.likedByMe ? <HeartFilled /> : <HeartOutlined />}
                      loading={likingIds.includes(post.id)}
                      onClick={() => handleToggleLike(post)}
                    >
                      {post.likeCount || 0}
                    </Button>
                    <Button size="small" icon={<MessageOutlined />} onClick={() => navigate(`/daily/${post.id}`)}>
                      {post.commentCount || 0}
                    </Button>
                  </Space>
                  <Typography.Text type="secondary">{t('dailyInteraction.comments')}</Typography.Text>
                </div>
              </Space>
            </Card>
          ))}
        </Space>
      )}
    </Space>
  );
}
