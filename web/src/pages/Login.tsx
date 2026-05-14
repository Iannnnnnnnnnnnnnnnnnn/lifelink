import { HeartOutlined, LockOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, message, Typography } from 'antd';
import { AxiosError } from 'axios';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { ApiResult } from '../api/request';
import { login } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import { FloatingStickers } from '../components/decorations/FloatingStickers';
import { LanguageSwitcher } from '../components/LanguageSwitcher';

interface LoginFormValues {
  account: string;
  password: string;
}

export function Login() {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const saveLogin = useAuthStore((state) => state.login);
  const [submitting, setSubmitting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const handleSubmit = async (values: LoginFormValues) => {
    setSubmitting(true);
    try {
      const response = await login(values);
      saveLogin(response.data.data);
      messageApi.success(t('auth.loginSuccess'));
      navigate('/');
    } catch (error) {
      const axiosError = error as AxiosError<ApiResult<unknown>>;
      messageApi.error(axiosError.response?.data?.message || t('auth.loginFailed'));
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
      <Card className="auth-card">
        <div className="auth-brand">
          <div className="auth-brand-mark">
            <HeartOutlined />
          </div>
          <Typography.Title level={2}>LifeLink</Typography.Title>
          <Typography.Text type="secondary">{t('ui.lifeSlogan')}</Typography.Text>
        </div>
        <Typography.Title level={3}>{t('auth.login')}</Typography.Title>
        <Form layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="account" label={t('auth.account')} rules={[{ required: true, message: t('auth.accountRequired') }]}>
            <Input prefix={<UserOutlined />} placeholder={t('auth.accountPlaceholder')} />
          </Form.Item>
          <Form.Item name="password" label={t('auth.password')} rules={[{ required: true, message: t('auth.passwordRequired') }]}>
            <Input.Password prefix={<LockOutlined />} placeholder={t('auth.passwordPlaceholder')} />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting} block>
            {t('auth.login')}
          </Button>
        </Form>
        <Typography.Paragraph className="auth-link">
          {t('auth.noAccount')} <Link to="/register">{t('auth.register')}</Link>
        </Typography.Paragraph>
      </Card>
    </div>
  );
}
