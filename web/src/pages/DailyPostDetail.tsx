import { DeleteOutlined, HeartFilled, HeartOutlined, MessageOutlined, SendOutlined } from '@ant-design/icons';
import { Avatar, Button, Card, Descriptions, Form, Image, Input, List, message, Popconfirm, Space, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import {
  commentDailyPost,
  DailyPostComment,
  DailyPostDetail as DailyPostDetailType,
  deleteDailyPostComment,
  getDailyPostComments,
  getDailyPostDetail,
  likeDailyPost,
  unlikeDailyPost,
} from '../api/daily';
import { EmptyState } from '../components/common/EmptyState';
import { ErrorState } from '../components/common/ErrorState';
import { PageLoading } from '../components/common/PageLoading';
import { getPageErrorType, PageErrorType } from '../utils/error';

interface CommentFormValues {
  content: string;
}

export function DailyPostDetail() {
  const { t } = useTranslation();
  const params = useParams();
  const postId = Number(params.id);
  const [post, setPost] = useState<DailyPostDetailType | null>(null);
  const [comments, setComments] = useState<DailyPostComment[]>([]);
  const [loadingPost, setLoadingPost] = useState(false);
  const [loadingComments, setLoadingComments] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [liking, setLiking] = useState(false);
  const [form] = Form.useForm<CommentFormValues>();
  const [messageApi, contextHolder] = message.useMessage();

  const loadPost = async () => {
    if (!postId) return;
    setLoadingPost(true);
    try {
      const response = await getDailyPostDetail(postId);
      setPost(response.data.data);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
    } finally {
      setLoadingPost(false);
    }
  };

  const loadComments = async () => {
    if (!postId) return;
    setLoadingComments(true);
    try {
      const response = await getDailyPostComments(postId, { page: 1, size: 50 });
      setComments(response.data.data);
    } catch (error) {
      messageApi.error(t('common.failed'));
    } finally {
      setLoadingComments(false);
    }
  };

  const handleToggleLike = async () => {
    if (!post) return;
    setLiking(true);
    try {
      const response = post.likedByMe ? await unlikeDailyPost(post.id) : await likeDailyPost(post.id);
      setPost((current) => (current ? { ...current, ...response.data.data } : current));
      messageApi.success(post.likedByMe ? t('dailyInteraction.unlikeSuccess') : t('dailyInteraction.likeSuccess'));
    } catch (error) {
      messageApi.error(t('common.failed'));
    } finally {
      setLiking(false);
    }
  };

  const handleSubmitComment = async (values: CommentFormValues) => {
    const content = values.content.trim();
    if (!content) return;
    setSubmitting(true);
    try {
      await commentDailyPost(postId, { content });
      messageApi.success(t('dailyInteraction.commentSuccess'));
      form.resetFields();
      await Promise.all([loadPost(), loadComments()]);
    } catch (error) {
      messageApi.error(t('common.failed'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    try {
      await deleteDailyPostComment(postId, commentId);
      messageApi.success(t('dailyInteraction.deleteCommentSuccess'));
      await Promise.all([loadPost(), loadComments()]);
    } catch (error) {
      messageApi.error(t('common.failed'));
    }
  };

  useEffect(() => {
    loadPost();
    loadComments();
  }, [postId]);

  if (loadingPost && !post) {
    return (
      <Space direction="vertical" size={16} className="page-wide">
        {contextHolder}
        <PageLoading />
      </Space>
    );
  }

  if (pageError) {
    return (
      <Space direction="vertical" size={16} className="page-wide">
        {contextHolder}
        <ErrorState type={pageError} onRetry={() => {
          loadPost();
          loadComments();
        }} />
      </Space>
    );
  }

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div>
        <Typography.Title level={2}>{t('daily.detail')}</Typography.Title>
        <Typography.Text type="secondary">{post?.relationshipName || '-'}</Typography.Text>
      </div>
      <Card>
        <Typography.Paragraph className="daily-content">{post?.content || '-'}</Typography.Paragraph>
        {post?.images && post.images.length > 0 && (
          <Space direction="vertical" size={8}>
            <Typography.Text strong>{t('daily.previewImage')}</Typography.Text>
            <Image.PreviewGroup>
              <Space wrap>
                {post.images.map((image) => (
                  <Image key={image.fileId} src={image.url} alt={image.originalName} width={140} height={140} style={{ objectFit: 'cover', borderRadius: 6 }} />
                ))}
              </Space>
            </Image.PreviewGroup>
          </Space>
        )}
        <div className="daily-interaction-bar">
          <Space className="daily-interaction-actions">
            <Button className={`daily-like-button ${post?.likedByMe ? 'liked' : ''}`} icon={post?.likedByMe ? <HeartFilled /> : <HeartOutlined />} loading={liking} onClick={handleToggleLike}>
              {post?.likeCount || 0} {t('dailyInteraction.like')}
            </Button>
            <Button icon={<MessageOutlined />}>
              {post?.commentCount || 0} {t('dailyInteraction.comments')}
            </Button>
          </Space>
        </div>
        <Descriptions bordered column={1}>
          <Descriptions.Item label={t('daily.author')}>{post?.username || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('daily.relationship')}>{post?.relationshipName || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('daily.mood')}>{post?.mood ? <Tag color="blue">{post.mood}</Tag> : '-'}</Descriptions.Item>
          <Descriptions.Item label={t('daily.visibility')}>{post?.visibility || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('daily.createdAt')}>{post?.createdAt || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('daily.updatedAt')}>{post?.updatedAt || '-'}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card className="daily-comment-card" title={t('dailyInteraction.comments')}>
        <Form form={form} layout="vertical" onFinish={handleSubmitComment} className="daily-comment-form">
          <Form.Item name="content" rules={[{ required: true, message: t('dailyInteraction.writeComment') }, { max: 1000 }]}>
            <Input.TextArea rows={3} maxLength={1000} showCount placeholder={t('dailyInteraction.writeComment')} />
          </Form.Item>
          <Button type="primary" htmlType="submit" icon={<SendOutlined />} loading={submitting}>
            {t('dailyInteraction.sendComment')}
          </Button>
        </Form>

        <List
          loading={loadingComments}
          dataSource={comments}
          locale={{ emptyText: <EmptyState title={t('empty.noComments')} description={t('dailyInteraction.commentEmpty')} /> }}
          renderItem={(comment) => (
            <List.Item
              className="daily-comment-item"
              actions={
                comment.canDelete
                  ? [
                      <Popconfirm key="delete" title={t('dailyInteraction.confirmDeleteComment')} okText={t('common.confirm')} cancelText={t('common.cancel')} onConfirm={() => handleDeleteComment(comment.id)}>
                        <Button size="small" danger icon={<DeleteOutlined />}>
                          {t('common.delete')}
                        </Button>
                      </Popconfirm>,
                    ]
                  : []
              }
            >
              <List.Item.Meta
                avatar={<Avatar src={comment.avatarUrl}>{comment.username?.[0]}</Avatar>}
                title={
                  <Space wrap>
                    <Typography.Text strong>{comment.username || '-'}</Typography.Text>
                    <Typography.Text type="secondary">{comment.createdAt}</Typography.Text>
                  </Space>
                }
                description={<Typography.Paragraph className="daily-comment-content">{comment.content}</Typography.Paragraph>}
              />
            </List.Item>
          )}
        />
      </Card>
    </Space>
  );
}
