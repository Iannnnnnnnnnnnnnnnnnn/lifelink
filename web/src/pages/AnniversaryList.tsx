import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Input, message, Select, Skeleton, Space, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { Anniversary, AnniversaryDisplayType, getAnniversaries } from '../api/anniversary';
import { getRelationships, RelationshipSummary } from '../api/relationship';
import { getAnniversaryDisplayText, getRepeatTypeLabel } from '../utils/anniversary';
import { EmptyState } from '../components/decorations/EmptyState';
import { ErrorState } from '../components/common/ErrorState';
import { RelationshipSubNav } from '../components/navigation/RelationshipSubNav';
import { formatDate } from '../utils/date';
import { getPageErrorType, PageErrorType } from '../utils/error';

export function AnniversaryList() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const params = useParams();
  const routeRelationshipId = params.relationshipId ? Number(params.relationshipId) : undefined;
  const [items, setItems] = useState<Anniversary[]>([]);
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [relationshipId, setRelationshipId] = useState<number | undefined>(routeRelationshipId);
  const [displayType, setDisplayType] = useState<AnniversaryDisplayType | undefined>();
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [messageApi, contextHolder] = message.useMessage();
  const selectedRelationshipId = routeRelationshipId || relationshipId;

  const loadData = async (nextRelationshipId = relationshipId) => {
    setLoading(true);
    try {
      const response = await getAnniversaries({
        relationshipId: nextRelationshipId,
        displayType,
        keyword: keyword || undefined,
        page: 1,
        size: 50,
      });
      setItems(response.data.data);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    getRelationships()
      .then((response) => setRelationships(response.data.data))
      .catch(() => messageApi.error(t('relationship.loadFailed')));
  }, []);

  useEffect(() => {
    setRelationshipId(routeRelationshipId);
    loadData(routeRelationshipId);
  }, [routeRelationshipId, displayType]);

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      {routeRelationshipId && <RelationshipSubNav relationshipId={routeRelationshipId} />}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('anniversary.title')}</Typography.Title>
          <Typography.Text type="secondary">{t('anniversary.subtitle')}</Typography.Text>
        </div>
        <Space wrap className="filter-actions">
          {!routeRelationshipId && (
            <Select
              allowClear
              placeholder={t('anniversary.selectRelationship')}
              className="relationship-filter"
              value={relationshipId}
              options={relationships.map((item) => ({ value: item.id, label: item.name }))}
              onChange={(value) => {
                setRelationshipId(value);
                loadData(value);
              }}
            />
          )}
          <Select
            allowClear
            className="todo-status-filter"
            placeholder={t('anniversary.displayType')}
            value={displayType}
            onChange={setDisplayType}
            options={[
              { value: 'COUNTDOWN', label: t('anniversary.countdown') },
              { value: 'PASSED', label: t('anniversary.passed') },
              { value: 'TODAY', label: t('anniversary.today') },
            ]}
          />
          <Input.Search placeholder={t('anniversary.searchPlaceholder')} allowClear value={keyword} onChange={(event) => setKeyword(event.target.value)} onSearch={() => loadData()} />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={() => loadData()}>
            {t('common.refresh')}
          </Button>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate(selectedRelationshipId ? `/anniversaries/create?relationshipId=${selectedRelationshipId}` : '/anniversaries/create')}
          >
            {t('anniversary.create')}
          </Button>
        </Space>
      </div>

      {pageError ? (
        <ErrorState type={pageError} onRetry={() => loadData()} />
      ) : loading && items.length === 0 ? (
        <div className="anniversary-grid">
          {[1, 2, 3].map((item) => (
            <Card key={item}>
              <Skeleton active paragraph={{ rows: 5 }} />
            </Card>
          ))}
        </div>
      ) : items.length === 0 ? (
        <Card>
          <EmptyState description={t('anniversary.empty')} />
        </Card>
      ) : (
        <div className="anniversary-grid">
          {items.map((item) => (
            <div
              key={item.id}
              className="anniversary-card"
              style={item.backgroundUrl ? { backgroundImage: `url(${item.backgroundUrl})` } : undefined}
              onClick={() => navigate(`/anniversaries/${item.id}`)}
            >
              <div className="anniversary-card-content">
                <div>
                  <Space wrap>
                    <Tag>{item.relationshipName || '-'}</Tag>
                    <Tag>{getRepeatTypeLabel(item.repeatType, t)}</Tag>
                  </Space>
                  <Typography.Title level={3}>{item.title}</Typography.Title>
                  <Typography.Text>{formatDate(item.anniversaryDate, t, i18n.resolvedLanguage)}</Typography.Text>
                </div>
                <div>
                  <div className="anniversary-day-count">{item.dayCount}</div>
                  <div className="anniversary-display-text">{getAnniversaryDisplayText(item, t)}</div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </Space>
  );
}
