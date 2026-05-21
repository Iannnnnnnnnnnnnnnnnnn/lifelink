import {
  CalendarOutlined,
  CheckCircleOutlined,
  EditOutlined,
  IdcardOutlined,
  MailOutlined,
  PhoneOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Button, Card, Col, Form, Input, Modal, Row, Space, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { updateCurrentUser } from '../../api/user';
import { PageLoading } from '../../components/common/PageLoading';
import { AvatarUploader } from '../../components/profile/AvatarUploader';
import { useAuthStore } from '../../store/authStore';
import { formatDateTime } from '../../utils/date';

interface ProfileFormValues {
  username: string;
  email?: string;
  phone?: string;
}

interface InfoItemProps {
  icon: ReactNode;
  label: string;
  value?: ReactNode;
}

function normalizeOptionalValue(value?: string | null) {
  const trimmed = value?.trim();
  return trimmed || null;
}

function InfoItem({ icon, label, value }: InfoItemProps) {
  return (
    <div className="profile-info-item">
      <span className="profile-info-icon">{icon}</span>
      <div className="profile-info-content">
        <Typography.Text type="secondary">{label}</Typography.Text>
        <Typography.Text className="profile-info-value">{value || '-'}</Typography.Text>
      </div>
    </div>
  );
}

export function ProfilePage() {
  const { t, i18n } = useTranslation();
  const user = useAuthStore((state) => state.user);
  const fetchCurrentUser = useAuthStore((state) => state.fetchCurrentUser);
  const setUser = useAuthStore((state) => state.setUser);
  const updateUser = useAuthStore((state) => state.updateUser);
  const [form] = Form.useForm<ProfileFormValues>();
  const [loading, setLoading] = useState(!user);
  const [editOpen, setEditOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    setLoading(true);
    fetchCurrentUser()
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, [fetchCurrentUser]);

  const openEditModal = () => {
    form.setFieldsValue({
      username: user?.username,
      email: user?.email || undefined,
      phone: user?.phone || undefined,
    });
    setEditOpen(true);
  };

  const handleSave = async () => {
    let values: ProfileFormValues;
    try {
      values = await form.validateFields();
    } catch (error) {
      return;
    }
    setSaving(true);
    try {
      const response = await updateCurrentUser({
        username: values.username.trim(),
        email: normalizeOptionalValue(values.email),
        phone: normalizeOptionalValue(values.phone),
      });
      setUser(response.data.data);
      messageApi.success(t('profile.saveSuccess'));
      setEditOpen(false);
    } catch (error) {
      messageApi.error(t('message.operationFailed'));
    } finally {
      setSaving(false);
    }
  };

  const handleAvatarUploaded = (avatarUrl: string) => {
    updateUser({ avatarUrl });
    fetchCurrentUser().catch(() => undefined);
  };

  if (loading && !user) {
    return (
      <Space direction="vertical" size={16} className="page-wide profile-page">
        <PageLoading />
      </Space>
    );
  }

  return (
    <Space direction="vertical" size={18} className="page-wide profile-page">
      {contextHolder}
      <Card className="profile-hero-card">
        <div className="profile-hero">
          <AvatarUploader username={user?.username} avatarUrl={user?.avatarUrl} onUploaded={handleAvatarUploaded} />
          <div className="profile-hero-copy">
            <Typography.Text className="profile-eyebrow">{t('profile.title')}</Typography.Text>
            <Typography.Title level={2}>{user?.username || t('home.defaultUser')}</Typography.Title>
            <Typography.Paragraph type="secondary">{t('profile.subtitle')}</Typography.Paragraph>
            <Space wrap className="profile-meta">
              <Tag icon={<CheckCircleOutlined />} color="success">
                {t(`enum.status.${user?.status || 'ACTIVE'}`, { defaultValue: user?.status || '-' })}
              </Tag>
              <Typography.Text type="secondary">
                {t('profile.createdAt')}: {formatDateTime(user?.createdAt, t, i18n.resolvedLanguage)}
              </Typography.Text>
            </Space>
          </div>
          <Button type="primary" icon={<EditOutlined />} onClick={openEditModal} className="profile-edit-button">
            {t('profile.editProfile')}
          </Button>
        </div>
      </Card>

      <Row gutter={[18, 18]}>
        <Col xs={24} lg={12}>
          <Card title={t('profile.basicInfo')} className="profile-detail-card">
            <div className="profile-info-grid">
              <InfoItem icon={<UserOutlined />} label={t('profile.username')} value={user?.username} />
              <InfoItem icon={<MailOutlined />} label={t('profile.email')} value={user?.email || t('common.notAvailable')} />
              <InfoItem icon={<PhoneOutlined />} label={t('profile.phone')} value={user?.phone || t('common.notAvailable')} />
              <InfoItem icon={<IdcardOutlined />} label={t('profile.userId')} value={user?.id ? `#${user.id}` : t('common.notAvailable')} />
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={t('profile.personalInfo')} className="profile-detail-card">
            <div className="profile-info-grid">
              <InfoItem
                icon={<CheckCircleOutlined />}
                label={t('profile.status')}
                value={t(`enum.status.${user?.status || 'ACTIVE'}`, { defaultValue: user?.status || '-' })}
              />
              <InfoItem
                icon={<CalendarOutlined />}
                label={t('profile.createdAt')}
                value={formatDateTime(user?.createdAt, t, i18n.resolvedLanguage)}
              />
              <InfoItem
                icon={<CalendarOutlined />}
                label={t('profile.updatedAt')}
                value={formatDateTime(user?.updatedAt, t, i18n.resolvedLanguage)}
              />
            </div>
          </Card>
        </Col>
      </Row>

      <Modal
        title={t('profile.editProfile')}
        open={editOpen}
        onCancel={() => setEditOpen(false)}
        onOk={handleSave}
        confirmLoading={saving}
        okText={t('common.save')}
        cancelText={t('common.cancel')}
        destroyOnHidden
      >
        <Form form={form} layout="vertical" className="profile-edit-form">
          <Form.Item
            name="username"
            label={t('profile.username')}
            rules={[
              { required: true, message: t('validation.required', { field: t('profile.username') }) },
              { min: 3, max: 50, message: t('auth.usernameLength') },
            ]}
          >
            <Input prefix={<UserOutlined />} placeholder={t('profile.username')} />
          </Form.Item>
          <Form.Item name="email" label={t('profile.email')} rules={[{ type: 'email', message: t('validation.email') }]}>
            <Input prefix={<MailOutlined />} placeholder={t('profile.email')} />
          </Form.Item>
          <Form.Item
            name="phone"
            label={t('profile.phone')}
            rules={[{ pattern: /^[+0-9][0-9\-\s()]{5,29}$|^$/, message: t('profile.phoneInvalid') }]}
          >
            <Input prefix={<PhoneOutlined />} placeholder={t('profile.phone')} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
