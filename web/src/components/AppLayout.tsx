import { DownOutlined, HeartOutlined, LogoutOutlined, MenuOutlined, SettingOutlined, UserOutlined } from '@ant-design/icons';
import { Avatar, Button, Drawer, Dropdown, Grid, Input, Layout, Select, Space, Tooltip, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { useTranslation } from 'react-i18next';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { LanguageSwitcher } from './LanguageSwitcher';
import { SiteFooter } from './SiteFooter';
import { BackgroundLayer } from './background/BackgroundLayer';
import { useAppStore } from '../store/appStore';
import { useAuthStore } from '../store/authStore';
import { useBackgroundStore } from '../store/backgroundStore';
import { useRelationshipThemeStore } from '../store/relationshipThemeStore';
import { getAvatarInitial } from '../utils/avatar';
import { getRelationships, type RelationshipSummary } from '../api/relationship';
import { buildPrimaryNavSections, getPageContext, getRouteRelationshipId, headerActionIcons } from './navigation/navConfig';

const { Header, Content, Footer, Sider } = Layout;
const { useBreakpoint } = Grid;

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
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const philosophyEnabled = Boolean(user?.features?.philosophyEnabled);
  const currentRelationshipId = getRouteRelationshipId(location.pathname, location.search);
  const currentRelationship = relationships.find((item) => item.id === currentRelationshipId);
  const pageContext = useMemo(() => getPageContext(t, location), [t, location]);
  const navSections = useMemo(
    () => buildPrimaryNavSections({
      t,
      location,
      currentRelationship,
      currentRelationshipId,
      hasCoupleRelationship,
      philosophyEnabled,
    }),
    [currentRelationship, currentRelationshipId, hasCoupleRelationship, location, philosophyEnabled, t],
  );

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleOpenProfile = () => {
    navigate('/profile');
  };

  const handleOpenSettings = () => {
    navigate('/profile#settings');
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

  useEffect(() => {
    if (isAuthenticated) {
      getRelationships()
        .then((response) => setRelationships(response.data.data))
        .catch(() => setRelationships([]));
    }
  }, [isAuthenticated]);

  const themeClassName = hasCoupleRelationship ? 'theme-colorful' : 'theme-grayscale';

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: t('profile.title'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: t('menu.settings'),
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
    if (key === 'settings') {
      handleOpenSettings();
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
        {t('app.subtitle') && <Typography.Text type="secondary" className="brand-subtitle">{t('app.subtitle')}</Typography.Text>}
      </div>
    </div>
  );

  const menu = (
    <nav className="app-menu grouped-nav" aria-label={t('menu.navigation')}>
      {navSections.map((section) => (
        <div key={section.key} className={`nav-section ${section.active ? 'is-active' : ''}`}>
          <div className="nav-section-title">{section.label}</div>
          <div className="nav-section-items">
            {section.items.map((item) => {
              const navButton = (
                <button
                  key={item.key}
                  type="button"
                  className={`nav-item ${item.active ? 'is-active' : ''}`}
                  disabled={item.disabled}
                  onClick={() => {
                    if (item.disabled) return;
                    navigate(item.to);
                    setMobileMenuOpen(false);
                  }}
                >
                  <span className="nav-item-icon">{item.icon}</span>
                  <span className="nav-item-label">{item.label}</span>
                </button>
              );
              return item.disabled ? (
                <Tooltip key={item.key} title={t('menu.selectSpaceFirst')} placement="right">
                  <span>{navButton}</span>
                </Tooltip>
              ) : navButton;
            })}
          </div>
        </div>
      ))}
    </nav>
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
            <Typography.Text type="secondary" className="header-breadcrumb">
              {pageContext.crumbs.join(' / ')}
            </Typography.Text>
            <Typography.Title level={4}>{pageContext.title}</Typography.Title>
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
            {relationships.length > 0 && (
              <Select
                className="header-space-switcher"
                placeholder={t('menu.currentSpace')}
                value={currentRelationshipId}
                allowClear
                onChange={(value) => {
                  if (value) {
                    navigate(`/relationships/${value}`);
                  }
                }}
                options={relationships.map((item) => ({ value: item.id, label: item.name }))}
              />
            )}
            <Tooltip title={t('empty.noNotifications')}>
              <Button className="header-icon-button" icon={headerActionIcons.notification} />
            </Tooltip>
            <Tooltip title={t('menu.settings')}>
              <Button className="header-icon-button" icon={<SettingOutlined />} onClick={handleOpenSettings} />
            </Tooltip>
            <LanguageSwitcher />
            <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} trigger={['click']}>
              <button type="button" className="user-chip user-chip-button">
                <Avatar
                  size="small"
                  src={user?.avatarUrl || undefined}
                  className={user?.avatarUrl ? undefined : 'user-avatar-fallback'}
                >
                  {getAvatarInitial(user?.username)}
                </Avatar>
                <Typography.Text>{user?.username}</Typography.Text>
                <DownOutlined className="user-chip-arrow" />
              </button>
            </Dropdown>
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
