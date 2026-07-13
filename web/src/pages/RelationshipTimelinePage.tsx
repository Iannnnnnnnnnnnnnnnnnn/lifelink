import { ReloadOutlined } from '@ant-design/icons';
import { Avatar, Button, Card, Image, Select, Space, Spin, Tag, Timeline, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { getRelationshipTimeline, RelationshipTimelineEvent } from '../api/timeline';
import { EmptyState } from '../components/common/EmptyState';
import { ErrorState } from '../components/common/ErrorState';
import { formatDateTime } from '../utils/date';
import { getTimelineImportanceLabel } from '../utils/display';
import { getPageErrorType, PageErrorType } from '../utils/error';
import { getTimelineEventDescription, getTimelineEventIcon, getTimelineEventTag, getTimelineEventText, getTimelineTargetPath } from '../utils/timeline';

const eventTypeOptions = [
  'RELATIONSHIP_CREATED',
  'MEMBER_JOINED',
  'FIRST_DAILY_POST',
  'ANNIVERSARY_CREATED',
  'IMPORTANT_TODO_COMPLETED',
  'IMPORTANT_COMMENT_INTERACTION',
  'IMAGE_UPLOADED',
  'CUSTOM',
];

export function RelationshipTimelinePage() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const params = useParams();
  const relationshipId = Number(params.relationshipId);
  const [items, setItems] = useState<RelationshipTimelineEvent[]>([]);
  const [eventType, setEventType] = useState<string | undefined>();
  const [importance, setImportance] = useState<string | undefined>();
  const [order, setOrder] = useState<'ASC' | 'DESC'>('ASC');
  const [loading, setLoading] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const response = await getRelationshipTimeline(relationshipId, { eventType, importance, order });
      setItems(response.data.data);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (relationshipId) {
      loadData();
    }
  }, [relationshipId, eventType, importance, order]);

  return (
    <Space direction="vertical" size={16} className="page-wide timeline-page">
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('timeline.title')}</Typography.Title>
          <Typography.Text type="secondary">{t('timeline.subtitle')}</Typography.Text>
        </div>
        <Space wrap className="timeline-toolbar">
          <Select
            allowClear
            className="relationship-filter"
            placeholder={t('timeline.eventType')}
            value={eventType}
            onChange={setEventType}
            options={eventTypeOptions.map((value) => ({ value, label: getTimelineEventTag(value, t) }))}
          />
          <Select
            allowClear
            className="todo-status-filter"
            placeholder={t('timeline.importance')}
            value={importance}
            onChange={setImportance}
            options={[
              { value: 'NORMAL', label: getTimelineImportanceLabel(t, 'NORMAL') },
              { value: 'IMPORTANT', label: getTimelineImportanceLabel(t, 'IMPORTANT') },
            ]}
          />
          <Select
            className="todo-status-filter"
            value={order}
            onChange={setOrder}
            options={[
              { value: 'ASC', label: t('timeline.asc') },
              { value: 'DESC', label: t('timeline.desc') },
            ]}
          />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={loadData}>
            {t('common.refresh')}
          </Button>
        </Space>
      </div>

      <Card className="relationship-timeline-card">
        {pageError ? (
          <ErrorState type={pageError} onRetry={loadData} />
        ) : (
          <Spin spinning={loading}>
            {items.length === 0 ? (
              <EmptyState title={t('empty.noData')} description={t('timeline.empty')} />
            ) : (
              <Timeline
                mode="left"
                items={items.map((item) => {
                  const targetPath = getTimelineTargetPath(item);
                  return {
                    dot: <span className={`timeline-dot ${item.importance === 'IMPORTANT' ? 'important' : ''}`}>{getTimelineEventIcon(item.eventType)}</span>,
                    children: (
                      <Card className={`timeline-event-card ${item.importance === 'IMPORTANT' ? 'is-important' : ''}`} hoverable={Boolean(targetPath)} onClick={() => targetPath && navigate(targetPath)}>
                        {item.coverUrl && (
                          <Image
                            preview={false}
                            src={item.coverUrl}
                            alt={item.title}
                            className="timeline-cover"
                          />
                        )}
                        <Space direction="vertical" size={10} className="full-width">
                          <Space wrap>
                            <Tag color={item.importance === 'IMPORTANT' ? 'gold' : 'blue'}>{getTimelineEventTag(item.eventType, t)}</Tag>
                            <Tag>{getTimelineImportanceLabel(t, item.importance)}</Tag>
                            <Typography.Text type="secondary">{formatDateTime(item.eventDate, t, i18n.resolvedLanguage)}</Typography.Text>
                          </Space>
                          <Typography.Title level={4}>{getTimelineEventText(item, t)}</Typography.Title>
                          {getTimelineEventDescription(item) && (
                            <Typography.Paragraph className="timeline-description">
                              {getTimelineEventDescription(item)}
                            </Typography.Paragraph>
                          )}
                          <div className="timeline-event-footer">
                            <Space>
                              <Avatar src={item.actorAvatarUrl}>{item.actorUsername?.[0]}</Avatar>
                              <Typography.Text>{item.actorUsername || t('common.notAvailable')}</Typography.Text>
                            </Space>
                            {targetPath && (
                              <Button type="link" onClick={(event) => {
                                event.stopPropagation();
                                navigate(targetPath);
                              }}>
                                {t('timeline.viewDetail')}
                              </Button>
                            )}
                          </div>
                        </Space>
                      </Card>
                    ),
                  };
                })}
              />
            )}
          </Spin>
        )}
      </Card>
    </Space>
  );
}
