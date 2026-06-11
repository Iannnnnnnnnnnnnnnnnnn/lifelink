import { ArrowRightOutlined, PlusOutlined, UsergroupAddOutlined } from '@ant-design/icons';
import { Button, Card, message, Skeleton, Space, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { getRelationships, RelationshipSummary } from '../api/relationship';
import { EmptyState } from '../components/decorations/EmptyState';
import { ErrorState } from '../components/common/ErrorState';
import { formatDateTime } from '../utils/date';
import { getRelationshipTypeLabel, getRoleLabel } from '../utils/display';
import { getPageErrorType, PageErrorType } from '../utils/error';

export function RelationshipList() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [messageApi, contextHolder] = message.useMessage();

  const loadRelationships = async () => {
    setLoading(true);
    try {
      const response = await getRelationships();
      setRelationships(response.data.data);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRelationships();
  }, []);

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('relationship.title')}</Typography.Title>
          {t('relationship.spacesJoined') && <Typography.Text type="secondary">{t('relationship.spacesJoined')}</Typography.Text>}
        </div>
        <Space>
          <Button icon={<UsergroupAddOutlined />} onClick={() => navigate('/relationships/join')}>
            {t('relationship.join')}
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/relationships/create')}>
            {t('relationship.create')}
          </Button>
        </Space>
      </div>

      {pageError ? (
        <ErrorState type={pageError} onRetry={loadRelationships} />
      ) : loading && relationships.length === 0 ? (
        <div className="relationship-grid">
          {[1, 2, 3].map((item) => (
            <Card key={item}>
              <Skeleton active paragraph={{ rows: 3 }} />
            </Card>
          ))}
        </div>
      ) : relationships.length === 0 ? (
        <Card>
          <EmptyState description={t('ui.createFirstSpace')} />
        </Card>
      ) : (
        <div className="relationship-grid">
          {relationships.map((relationship) => (
            <Card
              key={relationship.id}
              hoverable
              loading={loading}
              title={relationship.name}
              className="relationship-card"
              actions={[
                <Button type="link" icon={<ArrowRightOutlined />} onClick={() => navigate(`/relationships/${relationship.id}`)}>
                  {t('relationship.enterDetail')}
                </Button>,
              ]}
            >
              <Space direction="vertical" size={10}>
                <Space>
                  <Tag color="blue">{getRelationshipTypeLabel(t, relationship.type)}</Tag>
                  <Tag>{getRoleLabel(t, relationship.currentUserRole)}</Tag>
                </Space>
                <Typography.Text type="secondary">
                  {t('relationship.createdAt')}: {formatDateTime(relationship.createdAt, t, i18n.resolvedLanguage)}
                </Typography.Text>
                {relationship.description && (
                  <Typography.Paragraph type="secondary" ellipsis={{ rows: 2 }}>
                    {relationship.description}
                  </Typography.Paragraph>
                )}
              </Space>
            </Card>
          ))}
        </div>
      )}
    </Space>
  );
}
