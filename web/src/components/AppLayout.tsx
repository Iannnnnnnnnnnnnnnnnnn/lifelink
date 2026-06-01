import { BulbOutlined, CalendarOutlined, ClockCircleOutlined, DollarOutlined, HeartOutlined, HomeOutlined, LogoutOutlined, MenuOutlined, ReadOutlined, TeamOutlined, ThunderboltOutlined, UserOutlined } from '@ant-design/icons';
import { Avatar, Button, Drawer, Dropdown, Grid, Input, Layout, Menu, Space, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { useTranslation } from 'react-i18next';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { LanguageSwitcher } from './LanguageSwitcher';
import { SiteFooter } from './SiteFooter';
import { BackgroundLayer } from './background/BackgroundLayer';
import { useAppStore } from '../store/appStore';
import { useAuthStore } from '../store/authStore';
import { useBackgroundStore } from '../store/backgroundStore';
import { useRelationshipThemeStore } from '../store/relationshipThemeStore';
import { getAvatarInitial } from '../utils/avatar';

const { Header, Content, Footer, Sider } = Layout;
const { useBreakpoint } = Grid;
type MenuItem = Required<MenuProps>['items'][number];

export function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useTranslation();
  const appName = useAppStore((state) => state.appName);
  const user = useAuthStore((state) => state.user);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logout = useAuthStore((state) => state.logout);
  const fetchCurrentUser = useAuthStore((state) => state.fetchCurrentUser);
  const fetchBackgroundSetting = useBackgroundStore((state) => state.fetchSetting);
  const hasCoupleRelationship = useRelationshipThemeStore((state) => state.hasCoupleRelationship);
  const fetchRelationshipThemeStatus = useRelationshipThemeStore((state) => state.fetchRelationshipThemeStatus);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const philosophyEnabled = Boolean(user?.features?.philosophyEnabled);

  const selectedKey = location.pathname.startsWith('/profile')
    ? '/profile'
    : location.pathname.startsWith('/relationships')
    ? '/relationships'
    : location.pathname.startsWith('/daily')
      ? '/daily'
      : location.pathname.startsWith('/activities')
        ? '/activities'
      : location.pathname.startsWith('/anniversaries')
        ? '/anniversaries'
      : location.pathname.startsWith('/cycle-care')
        ? '/cycle-care'
      : location.pathname.startsWith('/finance')
        ? '/finance'
      : location.pathname.startsWith('/focus')
        ? '/focus'
      : location.pathname.startsWith('/philosophy')
        ? '/philosophy'
        : '/';

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleOpenProfile = () => {
    navigate('/profile');
  };

  const handleGlobalSearch = (value: string) => {
    const keyword = value.trim();
    if (!keyword) {
      return;
    }
    setSearchKeyword('');
    navigate(`/search?keyword=${encodeURIComponent(keyword)}`);
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchRelationshipThemeStatus();
    }
  }, [fetchRelationshipThemeStatus, isAuthenticated]);

  useEffect(() => {
    if (isAuthenticated && !user) {
      fetchCurrentUser().catch(() => undefined);
    }
  }, [fetchCurrentUser, isAuthenticated, user]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchBackgroundSetting().catch(() => undefined);
    }
  }, [fetchBackgroundSetting, isAuthenticated]);

  const themeClassName = hasCoupleRelationship ? 'theme-colorful' : 'theme-grayscale';
  const menuItems: MenuItem[] = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: t('menu.home'),
      onClick: () => {
        navigate('/');
        setMobileMenuOpen(false);
      },
    },
    {
      key: '/profile',
      icon: <UserOutlined />,
      label: t('profile.title'),
      onClick: () => {
        navigate('/profile');
        setMobileMenuOpen(false);
      },
    },
    {
      key: '/relationships',
      icon: <TeamOutlined />,
      label: t('menu.relationships'),
      onClick: () => {
        navigate('/relationships');
        setMobileMenuOpen(false);
      },
    },
    {
      key: '/daily',
      icon: <ReadOutlined />,
      label: t('menu.daily'),
      onClick: () => {
        navigate('/daily');
        setMobileMenuOpen(false);
      },
    },
    {
      key: '/activities',
      icon: <ThunderboltOutlined />,
      label: t('menu.activities'),
      onClick: () => {
        navigate('/activities');
        setMobileMenuOpen(false);
      },
    },
    {
      key: '/anniversaries',
      icon: <CalendarOutlined />,
      label: t('menu.anniversaries'),
      onClick: () => {
        navigate('/anniversaries');
        setMobileMenuOpen(false);
      },
    },
    ...(hasCoupleRelationship ? [{
      key: '/cycle-care',
      icon: <HeartOutlined />,
      label: t('menu.cycleCare'),
      onClick: () => {
        navigate('/cycle-care');
        setMobileMenuOpen(false);
      },
    }] : []),
    {
      key: '/focus',
      icon: <ClockCircleOutlined />,
      label: t('menu.focus'),
      onClick: () => {
        navigate('/focus');
        setMobileMenuOpen(false);
      },
    },
    {
      key: '/finance',
      icon: <DollarOutlined />,
      label: t('menu.finance'),
      onClick: () => {
        navigate('/finance');
        setMobileMenuOpen(false);
      },
    },
    ...(philosophyEnabled ? [{
      key: '/philosophy',
      icon: <BulbOutlined />,
      label: t('menu.philosophy'),
      onClick: () => {
        navigate('/philosophy');
        setMobileMenuOpen(false);
      },
    }] : []),
  ];

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: t('profile.title'),
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: t('auth.logout'),
    },
  ];

  const handleUserMenuClick: MenuProps['onClick'] = ({ key }) => {
    if (key === 'profile') {
      handleOpenProfile();
      return;
    }
    if (key === 'logout') {
      handleLogout();
    }
  };

  const brand = (
    <div className="brand">
      <span className="brand-mark">
        <HeartOutlined className="brand-icon" />
      </span>
      <div>
        <Typography.Text strong className="brand-name">{appName}</Typography.Text>
        <Typography.Text type="secondary" className="brand-subtitle">{t('app.subtitle')}</Typography.Text>
      </div>
    </div>
  );

  const menu = (
    <Menu
      mode="inline"
      selectedKeys={[selectedKey]}
      className="app-menu"
      items={menuItems}
    />
  );

  return (
    <Layout className={`app-shell ${themeClassName}`}>
      <BackgroundLayer />
      {!isMobile && (
        <Sider width={220} className="app-sider desktop-sider">
          {brand}
          {menu}
        </Sider>
      )}
      <Drawer
        className="mobile-menu-drawer"
        placement="left"
        width={260}
        open={mobileMenuOpen}
        onClose={() => setMobileMenuOpen(false)}
        closable={false}
      >
        {brand}
        {menu}
      </Drawer>
      <Layout className="app-main-layout">
        <Header className="app-header">
          {isMobile && (
            <Button
              className="mobile-menu-button"
              icon={<MenuOutlined />}
              onClick={() => setMobileMenuOpen(true)}
            />
          )}
          <div className="app-header-title">
            <Typography.Title level={4}>{t('app.title')}</Typography.Title>
            <Typography.Text type="secondary" className="header-subtitle">{t('app.headerSubtitle')}</Typography.Text>
          </div>
          <Input.Search
            allowClear
            className="global-search"
            value={searchKeyword}
            placeholder={t('search.placeholder')}
            onChange={(event) => setSearchKeyword(event.target.value)}
            onSearch={handleGlobalSearch}
          />
          <Space className="header-actions">
            <LanguageSwitcher />
            <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} trigger={['hover']}>
              <button type="button" className="user-chip user-chip-button" onClick={handleOpenProfile}>
                <Avatar
                  size="small"
                  src={user?.avatarUrl || undefined}
                  className={user?.avatarUrl ? undefined : 'user-avatar-fallback'}
                >
                  {getAvatarInitial(user?.username)}
                </Avatar>
                <Typography.Text>{user?.username}</Typography.Text>
              </button>
            </Dropdown>
            <Button icon={<LogoutOutlined />} onClick={handleLogout} className="logout-button">
              {t('auth.logout')}
            </Button>
          </Space>
        </Header>
        <Content className="app-content">
          <Outlet />
        </Content>
        <Footer className="app-footer">
          <SiteFooter />
        </Footer>
      </Layout>
    </Layout>
  );
}
