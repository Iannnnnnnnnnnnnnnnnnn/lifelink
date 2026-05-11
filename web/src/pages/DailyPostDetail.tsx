import { Card, Descriptions, Image, Space, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import { DailyPostDetail as DailyPostDetailType, getDailyPostDetail } from '../api/daily';

export function DailyPostDetail() {
  const { t } = useTranslation();
  const params = useParams();
  const postId = Number(params.id);
  const [post, setPost] = useState<DailyPostDetailType | null>(null);

  useEffect(() => {
    if (postId) {
      getDailyPostDetail(postId).then((response) => setPost(response.data.data));
    }
  }, [postId]);

  return (
    <Space direction="vertical" size={16} className="page-wide">
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
                  <Image
                    key={image.fileId}
                    src={image.url}
                    alt={image.originalName}
                    width={140}
                    height={140}
                    style={{ objectFit: 'cover', borderRadius: 6 }}
                  />
                ))}
              </Space>
            </Image.PreviewGroup>
          </Space>
        )}
        <Descriptions bordered column={1}>
          <Descriptions.Item label={t('daily.author')}>{post?.username || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('daily.relationship')}>{post?.relationshipName || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('daily.mood')}>{post?.mood ? <Tag color="blue">{post.mood}</Tag> : '-'}</Descriptions.Item>
          <Descriptions.Item label={t('daily.visibility')}>{post?.visibility || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('daily.createdAt')}>{post?.createdAt || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('daily.updatedAt')}>{post?.updatedAt || '-'}</Descriptions.Item>
        </Descriptions>
      </Card>
    </Space>
  );
}
