import { CalendarOutlined, CheckSquareOutlined, DollarOutlined, ReloadOutlined, ShareAltOutlined, ThunderboltOutlined, UserOutlined } from '@ant-design/icons';
import { Alert, Avatar, Button, Card, Col, Descriptions, Empty, Input, message, Modal, Popconfirm, Row, Space, Table, Tabs, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import {
  createRelationshipInvite,
  CreateInviteResponse,
  dissolveRelationship,
  getRelationshipDetail,
  getRelationshipMembers,
  leaveRelationship,
  removeRelationshipMember,
  RelationshipDetail as RelationshipDetailType,
  RelationshipMember,
  transferRelationshipOwner,
  updateMemberRole,
  updateMyRelationshipNickname,
} from '../api/relationship';
import { ErrorState } from '../components/common/ErrorState';
import { PageLoading } from '../components/common/PageLoading';
import { useAuthStore } from '../store/authStore';
import { useRelationshipThemeStore } from '../store/relationshipThemeStore';
import { getPageErrorType, PageErrorType } from '../utils/error';

export function RelationshipDetail() {
  const { t } = useTranslation();
  const params = useParams();
  const navigate = useNavigate();
  const relationshipId = Number(params.id);
  const user = useAuthStore((state) => state.user);
  const fetchRelationshipThemeStatus = useRelationshipThemeStore((state) => state.fetchRelationshipThemeStatus);
  const [detail, setDetail] = useState<RelationshipDetailType | null>(null);
  const [members, setMembers] = useState<RelationshipMember[]>([]);
  const [invite, setInvite] = useState<CreateInviteResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [nicknameModalOpen, setNicknameModalOpen] = useState(false);
  const [nickname, setNickname] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const canCreateInvite = detail?.currentUserRole === 'OWNER' || detail?.currentUserRole === 'ADMIN';
  const isOwner = detail?.currentUserRole === 'OWNER';

  const loadDetail = async () => {
    setLoading(true);
    try {
      const [detailResponse, membersResponse] = await Promise.all([
        getRelationshipDetail(relationshipId),
        getRelationshipMembers(relationshipId),
      ]);
      setDetail(detailResponse.data.data);
      setMembers(membersResponse.data.data);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
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

  const refreshAfterMemberChange = async () => {
    await Promise.all([
      loadDetail(),
      fetchRelationshipThemeStatus().catch(() => undefined),
    ]);
  };

  const handleUpdateNickname = async () => {
    setActionLoading(true);
    try {
      await updateMyRelationshipNickname(relationshipId, { nickname });
      messageApi.success(t('member.updateSuccess'));
      setNicknameModalOpen(false);
      await refreshAfterMemberChange();
    } catch (error) {
      messageApi.error(t('common.failed'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleLeave = async () => {
    setActionLoading(true);
    try {
      await leaveRelationship(relationshipId);
      await fetchRelationshipThemeStatus().catch(() => undefined);
      messageApi.success(t('member.leaveSuccess'));
      navigate('/relationships');
    } catch (error) {
      messageApi.error(t('common.failed'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleDissolve = async () => {
    setActionLoading(true);
    try {
      await dissolveRelationship(relationshipId);
      await fetchRelationshipThemeStatus().catch(() => undefined);
      messageApi.success(t('member.dissolveSuccess'));
      navigate('/relationships');
    } catch (error) {
      messageApi.error(t('common.failed'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleRoleChange = async (member: RelationshipMember, role: 'ADMIN' | 'MEMBER') => {
    setActionLoading(true);
    try {
      await updateMemberRole(relationshipId, member.userId, { role });
      messageApi.success(t('member.updateSuccess'));
      await refreshAfterMemberChange();
    } catch (error) {
      messageApi.error(t('common.failed'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleRemoveMember = async (member: RelationshipMember) => {
    setActionLoading(true);
    try {
      await removeRelationshipMember(relationshipId, member.userId);
      messageApi.success(t('member.removeSuccess'));
      await refreshAfterMemberChange();
    } catch (error) {
      messageApi.error(t('common.failed'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleTransferOwner = async (member: RelationshipMember) => {
    setActionLoading(true);
    try {
      await transferRelationshipOwner(relationshipId, member.userId);
      messageApi.success(t('member.transferSuccess'));
      await refreshAfterMemberChange();
    } catch (error) {
      messageApi.error(t('common.failed'));
    } finally {
      setActionLoading(false);
    }
  };

  const openNicknameModal = () => {
    const currentMember = members.find((item) => item.userId === user?.id);
    setNickname(currentMember?.nickname || '');
    setNicknameModalOpen(true);
  };

  useEffect(() => {
    if (relationshipId) {
      loadDetail();
    }
  }, [relationshipId]);

  const renderRole = (role: string) => {
    const color = role === 'OWNER' ? 'gold' : role === 'ADMIN' ? 'blue' : 'default';
    const label = role === 'OWNER' ? t('member.owner') : role === 'ADMIN' ? t('member.admin') : t('member.member');
    return <Tag color={color}>{label}</Tag>;
  };

  const columns: ColumnsType<RelationshipMember> = [
    {
      title: t('relationship.user'),
      dataIndex: 'username',
      render: (_, member) => (
        <Space>
          <Avatar src={member.avatarUrl} icon={<UserOutlined />} />
          <span>{member.username}</span>
        </Space>
      ),
    },
    { title: t('member.nickname'), dataIndex: 'nickname', render: (value) => value || '-' },
    { title: t('member.role'), dataIndex: 'role', render: renderRole },
    { title: t('relationship.joinedAt'), dataIndex: 'joinedAt' },
    {
      title: t('common.edit'),
      key: 'actions',
      render: (_, member) => {
        const isSelf = member.userId === user?.id;
        const canOperateMember = isOwner && !isSelf && member.role !== 'OWNER';
        return (
          <Space wrap>
            {isSelf && (
              <Button size="small" onClick={openNicknameModal}>
                {t('member.editNickname')}
              </Button>
            )}
            {canOperateMember && member.role === 'MEMBER' && (
              <Button size="small" onClick={() => handleRoleChange(member, 'ADMIN')} loading={actionLoading}>
                {t('member.setAdmin')}
              </Button>
            )}
            {canOperateMember && member.role === 'ADMIN' && (
              <Button size="small" onClick={() => handleRoleChange(member, 'MEMBER')} loading={actionLoading}>
                {t('member.removeAdmin')}
              </Button>
            )}
            {canOperateMember && (
              <Popconfirm title={t('member.confirmTransfer')} onConfirm={() => handleTransferOwner(member)}>
                <Button size="small" loading={actionLoading}>
                  {t('member.transferOwner')}
                </Button>
              </Popconfirm>
            )}
            {canOperateMember && (
              <Popconfirm title={t('member.confirmRemove')} onConfirm={() => handleRemoveMember(member)}>
                <Button size="small" danger loading={actionLoading}>
                  {t('member.removeMember')}
                </Button>
              </Popconfirm>
            )}
          </Space>
        );
      },
    },
  ];

  if (loading && !detail) {
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
        <ErrorState type={pageError} onRetry={loadDetail} />
      </Space>
    );
  }

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
            label: t('member.title'),
            children: (
              <Card
                title={t('member.title')}
                extra={
                  <Space wrap>
                    <Popconfirm title={t('member.confirmLeave')} onConfirm={handleLeave}>
                      <Button danger loading={actionLoading}>
                        {t('member.leaveSpace')}
                      </Button>
                    </Popconfirm>
                    {isOwner && (
                      <Popconfirm title={t('member.confirmDissolve')} onConfirm={handleDissolve}>
                        <Button danger type="primary" loading={actionLoading}>
                          {t('member.dissolveSpace')}
                        </Button>
                      </Popconfirm>
                    )}
                  </Space>
                }
              >
                <Table
                  rowKey="userId"
                  columns={columns}
                  dataSource={members}
                  pagination={false}
                  loading={loading}
                  scroll={{ x: 920 }}
                  locale={{ emptyText: <Empty description={t('relationship.noMembers')} /> }}
                />
              </Card>
            ),
          },
        ]}
      />
      <Modal
        title={t('member.editNickname')}
        open={nicknameModalOpen}
        confirmLoading={actionLoading}
        onOk={handleUpdateNickname}
        onCancel={() => setNicknameModalOpen(false)}
        okText={t('common.save')}
        cancelText={t('common.cancel')}
      >
        <Input
          value={nickname}
          maxLength={50}
          showCount
          placeholder={t('member.nickname')}
          onChange={(event) => setNickname(event.target.value)}
        />
      </Modal>
    </Space>
  );
}
