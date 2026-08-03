import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Input, message, Select, Skeleton, Space, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { Anniversary, AnniversaryDisplayType, getAnniversaries } from '../api/anniversary';
import { getRelationships, RelationshipSummary } from '../api/relationship';
import { getAnniversaryDisplayText, getRepeatTypeLabel } from '../utils/anniversary';
import { EmptyState } from '../components/decorations/EmptyState';
import { ErrorState } from '../components/common/ErrorState';
import { formatDate } from '../utils/date';
import { getPageErrorType, PageErrorType } from '../utils/error';

function getPositiveNumber(value: string | null) {
  if (!value) {
    return undefined;
  }
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

function getDisplayType(value: string | null): AnniversaryDisplayType | undefined {
  return value === 'COUNTDOWN' || value === 'PASSED' || value === 'TODAY' ? value : undefined;
}

export function AnniversaryList() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const params = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const routeRelationshipId = params.relationshipId ? Number(params.relationshipId) : undefined;
  const queryRelationshipId = getPositiveNumber(searchParams.get('relationshipId'));
  const relationshipId = routeRelationshipId ?? queryRelationshipId;
  const displayType = getDisplayType(searchParams.get('displayType'));
  const activeKeyword = searchParams.get('keyword')?.trim() || '';
  const [items, setItems] = useState<Anniversary[]>([]);
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [keyword, setKeyword] = useState(activeKeyword);
  const [loading, setLoading] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [messageApi, contextHolder] = message.useMessage();

  const loadData = async (nextRelationshipId = relationshipId) => {
    setLoading(true);
    try {
      const response = await getAnniversaries({
        relationshipId: nextRelationshipId,
        displayType,
        keyword: activeKeyword || undefined,
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
    loadData(relationshipId);
  }, [relationshipId, displayType, activeKeyword]);

  useEffect(() => {
    setKeyword(activeKeyword);
  }, [activeKeyword]);

  const updateSearchParams = (updates: { relationshipId?: number; displayType?: AnniversaryDisplayType; keyword?: string }) => {
    const nextParams = new URLSearchParams(searchParams);
    if (updates.relationshipId !== undefined || Object.prototype.hasOwnProperty.call(updates, 'relationshipId')) {
      if (updates.relationshipId) {
        nextParams.set('relationshipId', String(updates.relationshipId));
      } else {
        nextParams.delete('relationshipId');
      }
    }
    if (updates.displayType !== undefined || Object.prototype.hasOwnProperty.call(updates, 'displayType')) {
      if (updates.displayType) {
        nextParams.set('displayType', updates.displayType);
      } else {
        nextParams.delete('displayType');
      }
    }
    if (updates.keyword !== undefined) {
      const nextKeyword = updates.keyword.trim();
      if (nextKeyword) {
        nextParams.set('keyword', nextKeyword);
      } else {
        nextParams.delete('keyword');
      }
    }
    setSearchParams(nextParams, { replace: true });
  };

  const handleKeywordChange = (value: string) => {
    setKeyword(value);
    if (!value) {
      updateSearchParams({ keyword: '' });
    }
  };

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
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
              onChange={(value) => updateSearchParams({ relationshipId: value })}
            />
          )}
          <Select
            allowClear
            className="todo-status-filter"
            placeholder={t('anniversary.displayType')}
            value={displayType}
            onChange={(value) => updateSearchParams({ displayType: value })}
            options={[
              { value: 'COUNTDOWN', label: t('anniversary.countdown') },
              { value: 'PASSED', label: t('anniversary.passed') },
              { value: 'TODAY', label: t('anniversary.today') },
            ]}
          />
          <Input.Search
            placeholder={t('anniversary.searchPlaceholder')}
            allowClear
            value={keyword}
            onChange={(event) => handleKeywordChange(event.target.value)}
            onSearch={(value) => updateSearchParams({ keyword: value })}
          />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={() => loadData()}>
            {t('common.refresh')}
          </Button>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate(relationshipId ? `/anniversaries/create?relationshipId=${relationshipId}` : '/anniversaries/create')}
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
