import {
  CalendarOutlined,
  CheckSquareOutlined,
  EditOutlined,
  HomeOutlined,
  SearchOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { Button, Card, Input, List, Select, Skeleton, Space, Tag, Typography } from 'antd';
import { ReactNode, useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { globalSearch, SearchGroup, SearchItem } from '../api/search';
import { EmptyState } from '../components/common/EmptyState';
import { ErrorState } from '../components/common/ErrorState';
import { formatDateTime } from '../utils/date';
import { highlightKeyword } from '../utils/search';

const SEARCH_TYPES = ['RELATIONSHIP', 'DAILY_POST', 'TODO', 'ANNIVERSARY', 'ACTIVITY'];

export function SearchPage() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const keywordFromUrl = searchParams.get('keyword') || '';
  const typesFromUrl = searchParams.get('types') || '';
  const [keyword, setKeyword] = useState(keywordFromUrl);
  const [selectedTypes, setSelectedTypes] = useState<string[]>(typesFromUrl ? typesFromUrl.split(',').filter(Boolean) : []);
  const [groups, setGroups] = useState<SearchGroup[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  const typeOptions = useMemo(
    () => SEARCH_TYPES.map((type) => ({ value: type, label: getTypeLabel(type, t) })),
    [t],
  );

  const runSearch = (nextKeyword = keyword, nextTypes = selectedTypes) => {
    const trimmed = nextKeyword.trim();
    if (!trimmed) {
      return;
    }
    const params: Record<string, string> = { keyword: trimmed };
    if (nextTypes.length) {
      params.types = nextTypes.join(',');
    }
    setSearchParams(params);
  };

  useEffect(() => {
    setKeyword(keywordFromUrl);
    setSelectedTypes(typesFromUrl ? typesFromUrl.split(',').filter(Boolean) : []);
    const trimmed = keywordFromUrl.trim();
    if (!trimmed) {
      setGroups([]);
      setTotalCount(0);
      return;
    }

    setLoading(true);
    setError(false);
    globalSearch({ keyword: trimmed, types: typesFromUrl || undefined, size: 8 })
      .then((response) => {
        setGroups(response.data.data.groups);
        setTotalCount(response.data.data.totalCount);
      })
      .catch(() => {
        setGroups([]);
        setTotalCount(0);
        setError(true);
      })
      .finally(() => setLoading(false));
  }, [keywordFromUrl, typesFromUrl]);

  const handleOpen = (item: SearchItem) => {
    if (item.targetUrl) {
      navigate(item.targetUrl);
    }
  };

  const visibleGroups = groups.filter((group) => group.items.length > 0);

  return (
    <Space direction="vertical" size={16} className="page-wide search-page">
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('search.title')}</Typography.Title>
          <Typography.Text type="secondary">{keywordFromUrl ? t('search.resultCount', { count: totalCount }) : t('search.startHint')}</Typography.Text>
        </div>
      </div>

      <Card className="search-panel">
        <Space direction="vertical" size={12} className="search-controls">
          <Input.Search
            size="large"
            allowClear
            value={keyword}
            enterButton={t('search.searchButton')}
            placeholder={t('search.inputPlaceholder')}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={() => runSearch()}
          />
          <Select
            mode="multiple"
            allowClear
            value={selectedTypes}
            options={typeOptions}
            placeholder={t('search.all')}
            onChange={(value) => {
              setSelectedTypes(value);
              if (keyword.trim()) {
                runSearch(keyword, value);
              }
            }}
          />
        </Space>
      </Card>

      {error ? (
        <ErrorState type="500" description={t('search.loadFailed')} onRetry={() => runSearch()} />
      ) : loading ? (
        <Card>
          <Skeleton active paragraph={{ rows: 5 }} />
        </Card>
      ) : !keywordFromUrl ? (
        <Card>
          <EmptyState title={t('search.title')} description={t('search.startHint')} />
        </Card>
      ) : visibleGroups.length === 0 ? (
        <Card>
          <EmptyState title={t('empty.noSearchResults')} description={t('search.empty')} />
        </Card>
      ) : (
        visibleGroups.map((group) => (
          <Card
            key={group.type}
            className="search-group-card"
            title={
              <Space>
                {getTypeIcon(group.type)}
                <span>{getTypeLabel(group.type, t)}</span>
                <Tag>{group.count}</Tag>
              </Space>
            }
          >
            <List
              itemLayout="horizontal"
              dataSource={group.items}
              renderItem={(item) => (
                <List.Item className="search-result-item" onClick={() => handleOpen(item)}>
                  <List.Item.Meta
                    avatar={<span className="search-result-icon">{getTypeIcon(item.type)}</span>}
                    title={
                      <Space wrap>
                        <Typography.Text strong>{highlightKeyword(item.title, keywordFromUrl)}</Typography.Text>
                        {item.relationshipName && <Tag>{item.relationshipName}</Tag>}
                      </Space>
                    }
                    description={
                      <Space direction="vertical" size={4}>
                        <Typography.Text type="secondary">{highlightKeyword(item.description, keywordFromUrl)}</Typography.Text>
                        {item.createdAt && <Typography.Text type="secondary">{formatDateTime(item.createdAt, t, i18n.resolvedLanguage)}</Typography.Text>}
                      </Space>
                    }
                  />
                  <Button type="link" icon={<SearchOutlined />}>
                    {t('search.viewDetail')}
                  </Button>
                </List.Item>
              )}
            />
          </Card>
        ))
      )}
    </Space>
  );
}

function getTypeLabel(type: string, t: (key: string) => string) {
  const keyMap: Record<string, string> = {
    RELATIONSHIP: 'search.relationships',
    DAILY_POST: 'search.dailyPosts',
    TODO: 'search.todos',
    ANNIVERSARY: 'search.anniversaries',
    ACTIVITY: 'search.activities',
    COMMENT: 'search.comments',
    NOTIFICATION: 'search.notifications',
    TRANSACTION: 'search.transactions',
  };
  return t(keyMap[type] || 'search.all');
}

function getTypeIcon(type: string): ReactNode {
  if (type === 'RELATIONSHIP') {
    return <HomeOutlined />;
  }
  if (type === 'DAILY_POST') {
    return <EditOutlined />;
  }
  if (type === 'TODO') {
    return <CheckSquareOutlined />;
  }
  if (type === 'ANNIVERSARY') {
    return <CalendarOutlined />;
  }
  if (type === 'ACTIVITY') {
    return <ThunderboltOutlined />;
  }
  return <SearchOutlined />;
}
