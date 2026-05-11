import { CalendarOutlined, DollarOutlined, HeartOutlined, HomeOutlined, LogoutOutlined, ReadOutlined, TeamOutlined, UserOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { Avatar, Button, Layout, Menu, Space, Typography } from 'antd';
import { useTranslation } from 'react-i18next';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { LanguageSwitcher } from './LanguageSwitcher';
import { useAppStore } from '../store/appStore';
import { useAuthStore } from '../store/authStore';
import { useRelationshipThemeStore } from '../store/relationshipThemeStore';
import { FloatingStickers } from './decorations/FloatingStickers';

const { Header, Content, Sider } = Layout;

export function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useTranslation();
  const appName = useAppStore((state) => state.appName);
  const user = useAuthStore((state) => state.user);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logout = useAuthStore((state) => state.logout);
  const hasCoupleRelationship = useRelationshipThemeStore((state) => state.hasCoupleRelationship);
  const fetchRelationshipThemeStatus = useRelationshipThemeStore((state) => state.fetchRelationshipThemeStatus);

  const selectedKey = location.pathname.startsWith('/relationships')
    ? '/relationships'
    : location.pathname.startsWith('/daily')
      ? '/daily'
      : location.pathname.startsWith('/activities')
        ? '/activities'
      : location.pathname.startsWith('/anniversaries')
        ? '/anniversaries'
      : location.pathname.startsWith('/finance')
        ? '/finance'
        : '/';

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchRelationshipThemeStatus();
    }
  }, [fetchRelationshipThemeStatus, isAuthenticated]);

  const themeClassName = hasCoupleRelationship ? 'theme-colorful' : 'theme-grayscale';

  return (
    <Layout className={`app-shell ${themeClassName}`}>
      <FloatingStickers />
      <Sider width={220} className="app-sider">
        <div className="brand">
          <span className="brand-mark">
            <HeartOutlined className="brand-icon" />
          </span>
          <div>
            <Typography.Text strong className="brand-name">{appName}</Typography.Text>
            <Typography.Text type="secondary" className="brand-subtitle">{t('app.subtitle')}</Typography.Text>
          </div>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          className="app-menu"
          items={[
            {
              key: '/',
              icon: <HomeOutlined />,
              label: t('menu.home'),
              onClick: () => navigate('/'),
            },
            {
              key: '/relationships',
              icon: <TeamOutlined />,
              label: t('menu.relationships'),
              onClick: () => navigate('/relationships'),
            },
            {
              key: '/daily',
              icon: <ReadOutlined />,
              label: t('menu.daily'),
              onClick: () => navigate('/daily'),
            },
            {
              key: '/activities',
              icon: <ThunderboltOutlined />,
              label: t('menu.activities'),
              onClick: () => navigate('/activities'),
            },
            {
              key: '/anniversaries',
              icon: <CalendarOutlined />,
              label: t('menu.anniversaries'),
              onClick: () => navigate('/anniversaries'),
            },
            {
              key: '/finance',
              icon: <DollarOutlined />,
              label: t('menu.finance'),
              onClick: () => navigate('/finance'),
            },
          ]}
        />
      </Sider>
      <Layout>
        <Header className="app-header">
          <div>
            <Typography.Title level={4}>{t('app.title')}</Typography.Title>
            <Typography.Text type="secondary" className="header-subtitle">{t('app.headerSubtitle')}</Typography.Text>
          </div>
          <Space className="header-actions">
            <LanguageSwitcher />
            <Space className="user-chip">
              <Avatar size="small" icon={<UserOutlined />} />
              <Typography.Text>{user?.username}</Typography.Text>
            </Space>
            <Button icon={<LogoutOutlined />} onClick={handleLogout} className="logout-button">
              {t('auth.logout')}
            </Button>
          </Space>
        </Header>
        <Content className="app-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
