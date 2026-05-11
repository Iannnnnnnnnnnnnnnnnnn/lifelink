import { ReloadOutlined } from '@ant-design/icons';
import { Avatar, Button, Card, message, Select, Space, Spin, Tag, Timeline, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import { getMyActivities, getRelationshipActivities, SpaceActivity } from '../api/activity';
import { getActivityIcon, getActivityTag, getActivityText } from '../utils/activity';
import { EmptyState } from '../components/decorations/EmptyState';

const activityOptions = [
  'RELATIONSHIP_CREATED',
  'MEMBER_JOINED',
  'DAILY_POST_CREATED',
  'TODO_CREATED',
  'TODO_COMPLETED',
  'TODO_REOPENED',
  'ANNIVERSARY_CREATED',
];

export function ActivityTimeline() {
  const { t } = useTranslation();
  const params = useParams();
  const relationshipId = params.relationshipId ? Number(params.relationshipId) : undefined;
  const [items, setItems] = useState<SpaceActivity[]>([]);
  const [activityType, setActivityType] = useState<string | undefined>();
  const [loading, setLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const loadData = async () => {
    setLoading(true);
    try {
      const requestParams = { activityType, page: 1, size: 50 };
      const response = relationshipId
        ? await getRelationshipActivities(relationshipId, requestParams)
        : await getMyActivities(requestParams);
      setItems(response.data.data);
    } catch (error) {
      messageApi.error(t('activity.loadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [relationshipId, activityType]);

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{relationshipId ? t('activity.title') : t('activity.allActivities')}</Typography.Title>
          <Typography.Text type="secondary">{t('activity.subtitle')}</Typography.Text>
        </div>
        <Space wrap className="activity-toolbar">
          <Select
            allowClear
            className="relationship-filter"
            placeholder={t('activity.filterType')}
            value={activityType}
            onChange={setActivityType}
            options={activityOptions.map((value) => ({ value, label: getActivityTag(value, t) }))}
          />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={loadData}>
            {t('common.refresh')}
          </Button>
        </Space>
      </div>

      <Card className="activity-timeline-card">
        <Spin spinning={loading}>
          {items.length === 0 ? (
            <EmptyState description={t('activity.empty')} />
          ) : (
            <Timeline
              items={items.map((item) => ({
                dot: getActivityIcon(item.activityType),
                children: (
                  <Card size="small" className="activity-item-card">
                    <div className="activity-item-head">
                      <div className="activity-actor">
                        <Avatar src={item.actorAvatarUrl}>{item.actorUsername?.[0]}</Avatar>
                        <div>
                          <Typography.Text strong>{item.actorUsername || t('activity.someone')}</Typography.Text>
                          <div className="activity-meta">{item.createdAt}</div>
                        </div>
                      </div>
                      <Space wrap>
                        {item.relationshipName && <Tag>{item.relationshipName}</Tag>}
                        <Tag color="blue">{getActivityTag(item.activityType, t)}</Tag>
                      </Space>
                    </div>
                    <Typography.Paragraph className="activity-text">{getActivityText(item, t)}</Typography.Paragraph>
                    {item.content && <Typography.Text type="secondary">{item.content}</Typography.Text>}
                  </Card>
                ),
              }))}
            />
          )}
        </Spin>
      </Card>
    </Space>
  );
}
