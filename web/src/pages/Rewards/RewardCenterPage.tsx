import {
  EditOutlined,
  GiftOutlined,
  PlusOutlined,
  ReloadOutlined,
  StarOutlined,
} from '@ant-design/icons';
import { Button, Card, Col, Empty, Form, Grid, Image, Input, InputNumber, List, message, Modal, Row, Select, Skeleton, Space, Table, Tabs, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  CoinAccount,
  CoinLedger,
  createReward,
  deactivateReward,
  getCoinAccount,
  getCoinLedger,
  getMyRedemptions,
  getRewardAdminAccess,
  getRewards,
  redeemReward,
  Reward,
  RewardRedemption,
  RewardRequest,
  RewardStatus,
  updateReward,
  uploadRewardCover,
} from '../../api/rewards';
import { ForbiddenPage } from '../error/ForbiddenPage';

const statusOptions: RewardStatus[] = ['DRAFT', 'ACTIVE', 'INACTIVE', 'SOLD_OUT'];

interface RewardFormValues {
  title: string;
  description?: string;
  coverObjectKey?: string;
  coverUrl?: string;
  coinCost: number;
  stock?: number | null;
  status: RewardStatus;
  sortOrder: number;
}

function coinLabel(value?: number | null) {
  return value == null ? 0 : value;
}

function RewardCover({ reward }: { reward: Reward }) {
  if (reward.coverUrl) {
    return <Image src={reward.coverUrl} preview={false} className="reward-card-cover" />;
  }
  return (
    <div className="reward-card-cover reward-card-cover-empty">
      <GiftOutlined />
    </div>
  );
}

export function RewardCenterPage() {
  const { t, i18n } = useTranslation();
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.md;
  const [messageApi, contextHolder] = message.useMessage();
  const [account, setAccount] = useState<CoinAccount | null>(null);
  const [rewards, setRewards] = useState<Reward[]>([]);
  const [ledger, setLedger] = useState<CoinLedger[]>([]);
  const [redemptions, setRedemptions] = useState<RewardRedemption[]>([]);
  const [adminEnabled, setAdminEnabled] = useState(false);
  const [selectedReward, setSelectedReward] = useState<Reward | null>(null);
  const [redeeming, setRedeeming] = useState(false);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const [accountResponse, rewardsResponse, ledgerResponse, redemptionsResponse, accessResponse] = await Promise.all([
        getCoinAccount(),
        getRewards({ sortBy: 'sortOrder', sortDirection: 'asc', pageSize: 100 }),
        getCoinLedger({ pageSize: 50 }),
        getMyRedemptions({ pageSize: 50 }),
        getRewardAdminAccess(),
      ]);
      setAccount(accountResponse.data.data);
      setRewards(rewardsResponse.data.data);
      setLedger(ledgerResponse.data.data);
      setRedemptions(redemptionsResponse.data.data);
      setAdminEnabled(Boolean(accessResponse.data.data.enabled));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData().catch(() => setLoading(false));
  }, []);

  const handleRedeem = async (reward: Reward) => {
    Modal.confirm({
      title: t('rewards.confirmRedeem'),
      content: t('rewards.confirmRedeemText', { coinCost: reward.coinCost, title: reward.title }),
      okText: t('rewards.redeem'),
      cancelText: t('common.cancel'),
      onOk: async () => {
        setRedeeming(true);
        try {
          const response = await redeemReward(reward.id);
          setAccount((current) => current ? { ...current, balance: response.data.data.balance, totalSpent: current.totalSpent + reward.coinCost } : current);
          messageApi.success(t('rewards.redeemedSuccessfully'));
          await loadData();
        } finally {
          setRedeeming(false);
        }
      },
    });
  };

  const ledgerColumns: ColumnsType<CoinLedger> = [
    {
      title: t('rewards.time'),
      dataIndex: 'createdAt',
      render: (value: string) => dayjs(value).format(i18n.resolvedLanguage === 'en-US' ? 'MMM D HH:mm' : 'M月D日 HH:mm'),
    },
    {
      title: t('rewards.source'),
      dataIndex: 'title',
      render: (value: string, record) => value || record.sourceType,
    },
    {
      title: t('rewards.changeAmount'),
      dataIndex: 'changeAmount',
      render: (value: number) => <Typography.Text type={value >= 0 ? 'success' : 'danger'}>{value > 0 ? `+${value}` : value}</Typography.Text>,
    },
    {
      title: t('rewards.balance'),
      dataIndex: 'balanceAfter',
    },
  ];

  const redemptionColumns: ColumnsType<RewardRedemption> = [
    {
      title: t('rewards.rewardTitle'),
      dataIndex: 'rewardTitleSnapshot',
    },
    {
      title: t('rewards.requiredCoins'),
      dataIndex: 'coinCostSnapshot',
    },
    {
      title: t('rewards.time'),
      dataIndex: 'createdAt',
      render: (value: string) => dayjs(value).format(i18n.resolvedLanguage === 'en-US' ? 'MMM D HH:mm' : 'M月D日 HH:mm'),
    },
    {
      title: t('rewards.statusLabel'),
      dataIndex: 'status',
    },
  ];

  if (loading && !account) {
    return <Skeleton active paragraph={{ rows: 12 }} />;
  }

  return (
    <div className="page-wide rewards-page">
      {contextHolder}
      <section className="rewards-hero">
        <div>
          <Typography.Title level={1}>{t('rewards.center')}</Typography.Title>
          <Typography.Text>{t('rewards.subtitle')}</Typography.Text>
        </div>
        <Space wrap>
          <div className="coin-balance-pill">
            <StarOutlined />
            <span>{t('rewards.myCoins')}</span>
            <strong>{account?.balance || 0}</strong>
          </div>
          <Button icon={<ReloadOutlined />} onClick={loadData}>{t('common.refresh')}</Button>
        </Space>
      </section>

      <Tabs
        items={[
          {
            key: 'rewards',
            label: t('rewards.rewards'),
            children: (
              <Row gutter={[18, 18]}>
                {rewards.length === 0 && <Col span={24}><Empty description={t('rewards.emptyRewards')} /></Col>}
                {rewards.map((reward) => {
                  const soldOut = reward.status === 'SOLD_OUT' || (reward.stock != null && reward.redeemedCount >= reward.stock);
                  const notEnough = (account?.balance || 0) < reward.coinCost;
                  return (
                    <Col xs={24} md={12} xl={8} key={reward.id}>
                      <Card className="reward-card" hoverable onClick={() => setSelectedReward(reward)}>
                        <RewardCover reward={reward} />
                        <div className="reward-card-body">
                          <Space className="reward-card-title" align="start">
                            <Typography.Title level={4}>{reward.title}</Typography.Title>
                            <Tag color={soldOut ? 'default' : notEnough ? 'warning' : 'success'}>
                              {soldOut ? t('rewards.soldOut') : notEnough ? t('rewards.notEnoughCoins') : t('rewards.available')}
                            </Tag>
                          </Space>
                          <Typography.Paragraph ellipsis={{ rows: 2 }}>{reward.description || t('rewards.noDescription')}</Typography.Paragraph>
                          <div className="reward-card-footer">
                            <span><StarOutlined /> {reward.coinCost}</span>
                            <Button
                              type="primary"
                              disabled={soldOut || notEnough}
                              loading={redeeming}
                              onClick={(event) => {
                                event.stopPropagation();
                                handleRedeem(reward);
                              }}
                            >
                              {t('rewards.redeem')}
                            </Button>
                          </div>
                        </div>
                      </Card>
                    </Col>
                  );
                })}
              </Row>
            ),
          },
          {
            key: 'ledger',
            label: t('rewards.coinLedger'),
            children: isMobile ? (
              <List
                dataSource={ledger}
                renderItem={(item) => (
                  <List.Item>
                    <Space direction="vertical" size={2}>
                      <Typography.Text strong>{item.title || item.sourceType}</Typography.Text>
                      <Typography.Text type="secondary">{dayjs(item.createdAt).format('M月D日 HH:mm')}</Typography.Text>
                    </Space>
                    <Typography.Text type={item.changeAmount >= 0 ? 'success' : 'danger'}>{item.changeAmount > 0 ? `+${item.changeAmount}` : item.changeAmount}</Typography.Text>
                  </List.Item>
                )}
              />
            ) : <Table rowKey="id" columns={ledgerColumns} dataSource={ledger} pagination={false} />,
          },
          {
            key: 'redemptions',
            label: t('rewards.myRedemptions'),
            children: isMobile ? (
              <List
                dataSource={redemptions}
                renderItem={(item) => (
                  <List.Item>
                    <Space direction="vertical" size={2}>
                      <Typography.Text strong>{item.rewardTitleSnapshot}</Typography.Text>
                      <Typography.Text type="secondary">{dayjs(item.createdAt).format('M月D日 HH:mm')}</Typography.Text>
                    </Space>
                    <Tag>{item.coinCostSnapshot}</Tag>
                  </List.Item>
                )}
              />
            ) : <Table rowKey="id" columns={redemptionColumns} dataSource={redemptions} pagination={false} />,
          },
          ...(adminEnabled ? [{
            key: 'admin',
            label: t('rewards.management'),
            children: <RewardAdminPanel onChanged={loadData} />,
          }] : []),
        ]}
      />

      <Modal
        open={Boolean(selectedReward)}
        title={selectedReward?.title}
        footer={null}
        onCancel={() => setSelectedReward(null)}
      >
        {selectedReward && (
          <Space direction="vertical" size={16} className="full-width">
            <RewardCover reward={selectedReward} />
            <Typography.Paragraph>{selectedReward.description || t('rewards.noDescription')}</Typography.Paragraph>
            <Space>
              <Tag icon={<StarOutlined />}>{selectedReward.coinCost} {t('rewards.focusCoins')}</Tag>
              <Tag>{t('rewards.currentBalance')}: {account?.balance || 0}</Tag>
            </Space>
            <Button
              type="primary"
              block
              disabled={(account?.balance || 0) < selectedReward.coinCost || selectedReward.status !== 'ACTIVE'}
              loading={redeeming}
              onClick={() => handleRedeem(selectedReward)}
            >
              {t('rewards.confirmRedemption')}
            </Button>
          </Space>
        )}
      </Modal>
    </div>
  );
}

export function RewardAdminPage() {
  const { t } = useTranslation();
  const [allowed, setAllowed] = useState<boolean | null>(null);

  useEffect(() => {
    getRewardAdminAccess()
      .then((response) => setAllowed(Boolean(response.data.data.enabled)))
      .catch(() => setAllowed(false));
  }, []);

  if (allowed == null) {
    return <Skeleton active paragraph={{ rows: 8 }} />;
  }
  if (!allowed) {
    return <ForbiddenPage />;
  }
  return (
    <div className="page-wide rewards-page">
      <section className="rewards-hero">
        <div>
          <Typography.Title level={1}>{t('rewards.management')}</Typography.Title>
        </div>
      </section>
      <RewardAdminPanel />
    </div>
  );
}

function RewardAdminPanel({ onChanged }: { onChanged?: () => Promise<void> }) {
  const { t } = useTranslation();
  const [form] = Form.useForm<RewardFormValues>();
  const [messageApi, contextHolder] = message.useMessage();
  const [items, setItems] = useState<Reward[]>([]);
  const [editing, setEditing] = useState<Reward | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  const loadItems = async () => {
    const response = await getRewards({ pageSize: 100, sortBy: 'sortOrder', sortDirection: 'asc' });
    setItems(response.data.data);
  };

  useEffect(() => {
    loadItems().catch(() => undefined);
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.setFieldsValue({ coinCost: 10, status: 'DRAFT', sortOrder: 0, stock: null });
    setModalOpen(true);
  };

  const openEdit = (reward: Reward) => {
    setEditing(reward);
    form.setFieldsValue({
      title: reward.title,
      description: reward.description,
      coverObjectKey: reward.coverObjectKey,
      coverUrl: reward.coverUrl,
      coinCost: reward.coinCost,
      stock: reward.stock,
      status: reward.status,
      sortOrder: reward.sortOrder,
    });
    setModalOpen(true);
  };

  const saveReward = async () => {
    const values = await form.validateFields();
    const request: RewardRequest = {
      ...values,
      stock: values.stock == null ? null : values.stock,
      status: values.status || 'DRAFT',
      sortOrder: values.sortOrder || 0,
    };
    setLoading(true);
    try {
      if (editing) {
        await updateReward(editing.id, request);
      } else {
        await createReward(request);
      }
      setModalOpen(false);
      messageApi.success(t('common.success'));
      await loadItems();
      await onChanged?.();
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (file?: File) => {
    if (!file) return;
    setLoading(true);
    try {
      const response = await uploadRewardCover(file);
      form.setFieldsValue({
        coverUrl: response.data.data.coverUrl,
        coverObjectKey: response.data.data.objectKey,
      });
      messageApi.success(t('common.success'));
    } finally {
      setLoading(false);
    }
  };

  const columns = useMemo<ColumnsType<Reward>>(() => [
    {
      title: t('rewards.rewardTitle'),
      dataIndex: 'title',
    },
    {
      title: t('rewards.requiredCoins'),
      dataIndex: 'coinCost',
    },
    {
      title: t('rewards.stock'),
      render: (_, record) => record.stock == null ? t('rewards.unlimitedStock') : `${record.redeemedCount}/${record.stock}`,
    },
    {
      title: t('rewards.statusLabel'),
      dataIndex: 'status',
      render: (value: RewardStatus) => <Tag>{t(`rewards.status.${value}`)}</Tag>,
    },
    {
      title: t('common.actions'),
      render: (_, record) => (
        <Space>
          <Button icon={<EditOutlined />} onClick={() => openEdit(record)}>{t('rewards.editReward')}</Button>
          <Button danger onClick={() => deactivateReward(record.id).then(loadItems)}>{t('rewards.unpublish')}</Button>
        </Space>
      ),
    },
  ], [t]);

  return (
    <div className="reward-admin-panel">
      {contextHolder}
      <div className="reward-admin-toolbar">
        <Typography.Title level={3}>{t('rewards.management')}</Typography.Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>{t('rewards.createReward')}</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={items} pagination={false} />
      <Modal
        open={modalOpen}
        title={editing ? t('rewards.editReward') : t('rewards.createReward')}
        okText={t('common.save')}
        cancelText={t('common.cancel')}
        confirmLoading={loading}
        onOk={saveReward}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label={t('rewards.rewardTitle')} rules={[{ required: true }]}>
            <Input maxLength={120} />
          </Form.Item>
          <Form.Item name="description" label={t('rewards.rewardDescription')}>
            <Input.TextArea rows={4} maxLength={1000} />
          </Form.Item>
          <Form.Item label={t('rewards.uploadCover')}>
            <Space direction="vertical" className="full-width">
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp"
                onChange={(event) => handleUpload(event.target.files?.[0])}
              />
              <Form.Item name="coverUrl" noStyle><Input placeholder="coverUrl" /></Form.Item>
              <Form.Item name="coverObjectKey" noStyle><Input placeholder="objectKey" /></Form.Item>
            </Space>
          </Form.Item>
          <Row gutter={12}>
            <Col span={12}>
              <Form.Item name="coinCost" label={t('rewards.requiredCoins')} rules={[{ required: true }]}>
                <InputNumber min={1} className="full-width" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="stock" label={t('rewards.stock')}>
                <InputNumber min={0} className="full-width" placeholder={t('rewards.unlimitedStock')} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label={t('rewards.statusLabel')} rules={[{ required: true }]}>
                <Select options={statusOptions.map((status) => ({ value: status, label: t(`rewards.status.${status}`) }))} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="sortOrder" label={t('rewards.sortOrder')}>
                <InputNumber className="full-width" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  );
}
