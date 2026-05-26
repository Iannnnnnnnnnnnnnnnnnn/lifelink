import { HeartOutlined, LockOutlined, MailOutlined, PhoneOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, message, Typography } from 'antd';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../api/auth';
import { FloatingStickers } from '../components/decorations/FloatingStickers';
import { LanguageSwitcher } from '../components/LanguageSwitcher';
import { SiteFooter } from '../components/SiteFooter';

interface RegisterFormValues {
  username: string;
  email?: string;
  phone?: string;
  password: string;
  confirmPassword: string;
}

export function Register() {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [submitting, setSubmitting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const handleSubmit = async (values: RegisterFormValues) => {
    if (values.password !== values.confirmPassword) {
      messageApi.error(t('auth.passwordNotMatch'));
      return;
    }
    if (!values.email && !values.phone) {
      messageApi.error(t('auth.emailOrPhoneRequired'));
      return;
    }

    setSubmitting(true);
    try {
      await register({
        username: values.username,
        email: values.email || undefined,
        phone: values.phone || undefined,
        password: values.password,
      });
      messageApi.success(t('auth.registerSuccess'));
      navigate('/login');
    } catch (error) {
      messageApi.error(t('auth.registerFailed'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      {contextHolder}
      <FloatingStickers />
      <div className="auth-language">
        <LanguageSwitcher />
      </div>
      <div className="auth-content">
        <Card className="auth-card">
          <div className="auth-brand">
            <div className="auth-brand-mark">
              <HeartOutlined />
            </div>
            <Typography.Title level={2}>LifeLink</Typography.Title>
            <Typography.Text type="secondary">{t('ui.createFirstSpaceHint')}</Typography.Text>
          </div>
          <Typography.Title level={3}>{t('auth.createAccount')}</Typography.Title>
          <Form layout="vertical" onFinish={handleSubmit}>
            <Form.Item
              name="username"
              label={t('auth.username')}
              rules={[
                { required: true, message: t('auth.usernameRequired') },
                { min: 3, max: 50, message: t('auth.usernameLength') },
              ]}
            >
              <Input prefix={<UserOutlined />} placeholder={t('auth.usernamePlaceholder')} />
            </Form.Item>
            <Form.Item name="email" label={t('auth.email')} rules={[{ type: 'email', message: t('auth.emailInvalid') }]}>
              <Input prefix={<MailOutlined />} placeholder={t('auth.emailPlaceholder')} />
            </Form.Item>
            <Form.Item name="phone" label={t('auth.phone')}>
              <Input prefix={<PhoneOutlined />} placeholder={t('auth.phonePlaceholder')} />
            </Form.Item>
            <Form.Item
              name="password"
              label={t('auth.password')}
              rules={[
                { required: true, message: t('auth.passwordRequired') },
                { min: 6, message: t('auth.passwordLength') },
              ]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder={t('auth.passwordPlaceholder')} />
            </Form.Item>
            <Form.Item name="confirmPassword" label={t('auth.confirmPassword')} rules={[{ required: true, message: t('auth.confirmPasswordRequired') }]}>
              <Input.Password prefix={<LockOutlined />} placeholder={t('auth.confirmPasswordPlaceholder')} />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting} block>
              {t('auth.register')}
            </Button>
          </Form>
          <Typography.Paragraph className="auth-link">
            {t('auth.alreadyHaveAccount')} <Link to="/login">{t('auth.login')}</Link>
          </Typography.Paragraph>
        </Card>
      </div>
      <SiteFooter className="auth-footer" />
    </div>
  );
}
