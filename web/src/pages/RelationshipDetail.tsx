import { CalendarOutlined, CheckSquareOutlined, DollarOutlined, ReloadOutlined, ShareAltOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Col, Descriptions, Empty, message, Row, Space, Table, Tabs, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import {
  createRelationshipInvite,
  CreateInviteResponse,
  getRelationshipDetail,
  getRelationshipMembers,
  RelationshipDetail as RelationshipDetailType,
  RelationshipMember,
} from '../api/relationship';

export function RelationshipDetail() {
  const { t } = useTranslation();
  const params = useParams();
  const navigate = useNavigate();
  const relationshipId = Number(params.id);
  const [detail, setDetail] = useState<RelationshipDetailType | null>(null);
  const [members, setMembers] = useState<RelationshipMember[]>([]);
  const [invite, setInvite] = useState<CreateInviteResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const canCreateInvite = detail?.currentUserRole === 'OWNER' || detail?.currentUserRole === 'ADMIN';

  const loadDetail = async () => {
    setLoading(true);
    try {
      const [detailResponse, membersResponse] = await Promise.all([
        getRelationshipDetail(relationshipId),
        getRelationshipMembers(relationshipId),
      ]);
      setDetail(detailResponse.data.data);
      setMembers(membersResponse.data.data);
    } catch (error) {
      messageApi.error(t('relationship.loadFailed'));
    } finally {
      setLoading(false);
    }
  };

  const handleCreateInvite = async () => {
    try {
      const response = await createRelationshipInvite(relationshipId);
      setInvite(response.data.data);
      messageApi.success(t('relationship.inviteCreated'));
    } catch (error) {
      messageApi.error(t('relationship.inviteCreateFailed'));
    }
  };

  useEffect(() => {
    if (relationshipId) {
      loadDetail();
    }
  }, [relationshipId]);

  const columns: ColumnsType<RelationshipMember> = [
    { title: t('relationship.user'), dataIndex: 'username' },
    { title: t('relationship.role'), dataIndex: 'role', render: (role) => <Tag>{role}</Tag> },
    { title: t('relationship.nickname'), dataIndex: 'nickname', render: (value) => value || '-' },
    { title: t('relationship.joinedAt'), dataIndex: 'joinedAt' },
  ];

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{detail?.name || t('relationship.detail')}</Typography.Title>
          <Typography.Text type="secondary">{detail?.description || t('common.noDescription')}</Typography.Text>
        </div>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadDetail} loading={loading}>
            {t('common.refresh')}
          </Button>
          {canCreateInvite && (
            <Button type="primary" icon={<ShareAltOutlined />} onClick={handleCreateInvite}>
              {t('relationship.inviteCode')}
            </Button>
          )}
        </Space>
      </div>

      {invite && (
        <Alert
          type="success"
          showIcon
          message={`${t('relationship.inviteCode')}: ${invite.inviteCode}`}
          description={`${t('relationship.expiresAt')}: ${invite.expireAt}`}
        />
      )}

      <Tabs
        className="content-tabs"
        items={[
          {
            key: 'overview',
            label: t('relationship.overview'),
            children: (
              <Space direction="vertical" size={16} className="page-wide">
                <Row gutter={[16, 16]}>
                  <Col xs={24} md={12}>
                    <Card className="action-card" hoverable onClick={() => navigate(`/relationships/${relationshipId}/activities`)}>
                      <Space align="start">
                        <ThunderboltOutlined className="action-icon" />
                        <div>
                          <Typography.Title level={4}>{t('relationship.viewActivities')}</Typography.Title>
                          <Typography.Text type="secondary">{t('relationship.activityEntryDescription')}</Typography.Text>
                        </div>
                      </Space>
                    </Card>
                  </Col>
                  <Col xs={24} md={12}>
                    <Card className="action-card" hoverable onClick={() => navigate(`/relationships/${relationshipId}/todos`)}>
                      <Space align="start">
                        <CheckSquareOutlined className="action-icon" />
                        <div>
                          <Typography.Title level={4}>{t('relationship.viewTodos')}</Typography.Title>
                          <Typography.Text type="secondary">{t('relationship.todoEntryDescription')}</Typography.Text>
                        </div>
                      </Space>
                    </Card>
                  </Col>
                  <Col xs={24} md={12}>
                    <Card className="action-card" hoverable onClick={() => navigate(`/relationships/${relationshipId}/anniversaries`)}>
                      <Space align="start">
                        <CalendarOutlined className="action-icon" />
                        <div>
                          <Typography.Title level={4}>{t('relationship.viewAnniversaries')}</Typography.Title>
                          <Typography.Text type="secondary">{t('relationship.anniversaryEntryDescription')}</Typography.Text>
                        </div>
                      </Space>
                    </Card>
                  </Col>
                  <Col xs={24} md={12}>
                    <Card className="action-card" hoverable onClick={() => navigate(`/relationships/${relationshipId}/finance`)}>
                      <Space align="start">
                        <DollarOutlined className="action-icon" />
                        <div>
                          <Typography.Title level={4}>{t('relationship.viewFinance')}</Typography.Title>
                          <Typography.Text type="secondary">{t('relationship.financeEntryDescription')}</Typography.Text>
                        </div>
                      </Space>
                    </Card>
                  </Col>
                </Row>
                <Card title={t('relationship.relationshipInfo')} loading={loading}>
                  <Descriptions bordered column={1}>
                    <Descriptions.Item label={t('relationship.name')}>{detail?.name || '-'}</Descriptions.Item>
                    <Descriptions.Item label={t('relationship.type')}>{detail?.type || '-'}</Descriptions.Item>
                    <Descriptions.Item label={t('relationship.yourRole')}>{detail?.currentUserRole || '-'}</Descriptions.Item>
                    <Descriptions.Item label={t('relationship.status')}>{detail?.status || '-'}</Descriptions.Item>
                    <Descriptions.Item label={t('relationship.createdAt')}>{detail?.createdAt || '-'}</Descriptions.Item>
                  </Descriptions>
                </Card>
              </Space>
            ),
          },
          {
            key: 'members',
            label: t('relationship.members'),
            children: (
              <Card>
                <Table
                  rowKey="userId"
                  columns={columns}
                  dataSource={members}
                  pagination={false}
                  loading={loading}
                  locale={{ emptyText: <Empty description={t('relationship.noMembers')} /> }}
                />
              </Card>
            ),
          },
        ]}
      />
    </Space>
  );
}
