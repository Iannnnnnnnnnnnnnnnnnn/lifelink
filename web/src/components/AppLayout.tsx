<<<<<<< HEAD
import { BulbOutlined, CalendarOutlined, DollarOutlined, HeartOutlined, HomeOutlined, LogoutOutlined, MenuFoldOutlined, MenuOutlined, MenuUnfoldOutlined, ReadOutlined, TeamOutlined, ThunderboltOutlined, UserOutlined } from '@ant-design/icons';
import { Avatar, Button, Dropdown, Grid, Input, Menu, Space, Typography } from 'antd';
=======
import { DownOutlined, HeartOutlined, LogoutOutlined, MenuFoldOutlined, MenuOutlined, MenuUnfoldOutlined, SettingOutlined, UserOutlined } from '@ant-design/icons';
import { Avatar, Button, Drawer, Dropdown, Grid, Input, Layout, Select, Space, Tooltip, Typography } from 'antd';
>>>>>>> 6529f625afb39156fe842b5f2632498ae2ddf6af
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

<<<<<<< HEAD
=======
const { Header, Content, Footer, Sider } = Layout;
>>>>>>> 6529f625afb39156fe842b5f2632498ae2ddf6af
const { useBreakpoint } = Grid;
const SIDEBAR_COLLAPSED_STORAGE_KEY = 'lifelink_sidebar_collapsed';

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
<<<<<<< HEAD
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
=======
  const [sidebarCollapsed, setSidebarCollapsed] = useState(() => {
    try {
      return localStorage.getItem(SIDEBAR_COLLAPSED_STORAGE_KEY) === 'true';
    } catch {
      return false;
    }
  });
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
>>>>>>> 6529f625afb39156fe842b5f2632498ae2ddf6af
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

  const handleToggleSidebar = () => {
    setSidebarCollapsed((value) => {
      const nextValue = !value;
      try {
        localStorage.setItem(SIDEBAR_COLLAPSED_STORAGE_KEY, String(nextValue));
      } catch {
        // Ignore storage failures so the layout remains interactive.
      }
      return nextValue;
    });
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
<<<<<<< HEAD
    setMobileMenuOpen(false);
  }, [location.pathname]);

  const themeClassName = hasCoupleRelationship ? 'theme-colorful' : 'theme-grayscale';
  const shellStateClassName = sidebarCollapsed && !isMobile ? 'is-collapsed' : 'is-expanded';
  const menuItems: MenuProps['items'] = [
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
    {
      key: '/finance',
      icon: <DollarOutlined />,
      label: t('menu.finance'),
      onClick: () => {
        navigate('/finance');
        setMobileMenuOpen(false);
      },
    },
    {
      key: '/philosophy',
      icon: <BulbOutlined />,
      label: t('menu.philosophy'),
      onClick: () => {
        navigate('/philosophy');
        setMobileMenuOpen(false);
      },
    },
  ];
=======
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
  const sidebarStateClassName = sidebarCollapsed ? 'is-collapsed' : 'is-expanded';
>>>>>>> 6529f625afb39156fe842b5f2632498ae2ddf6af

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

  const renderBrand = () => (
    <div className="brand sidebar-brand">
      <span className="brand-mark">
        <HeartOutlined className="brand-icon" />
      </span>
      <div className="brand-copy">
        <Typography.Text strong className="brand-name">{appName}</Typography.Text>
        {t('app.subtitle') && <Typography.Text type="secondary" className="brand-subtitle">{t('app.subtitle')}</Typography.Text>}
      </div>
    </div>
  );

  const renderCollapseButton = () => (
    <div className="sidebar-collapse-row">
      <Button
        type="text"
        className="sidebar-collapse-trigger"
        icon={sidebarCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
        onClick={handleToggleSidebar}
        aria-label={sidebarCollapsed ? t('menu.expandSidebar') : t('menu.collapseSidebar')}
        aria-expanded={!sidebarCollapsed}
      >
        <span className="sidebar-collapse-label">
          {sidebarCollapsed ? t('menu.expandSidebar') : t('menu.collapseSidebar')}
        </span>
      </Button>
    </div>
  );

  const menu = (
<<<<<<< HEAD
    <Menu
      mode="inline"
      selectedKeys={[selectedKey]}
      className="app-menu"
      inlineCollapsed={!isMobile && sidebarCollapsed}
      items={menuItems}
    />
  );

  return (
    <div className={`app-shell ${shellStateClassName} ${themeClassName}`}>
      <aside className={`app-sidebar ${mobileMenuOpen ? 'is-open' : ''}`} aria-label={t('menu.home')}>
        <div className="sidebar-header">
          {brand}
        </div>
        <nav className="sidebar-scroll" aria-label={t('app.title')}>
          {menu}
        </nav>
        <div className="sidebar-footer">
          <button type="button" className="sidebar-user" onClick={handleOpenProfile}>
            <Avatar
              size="small"
              src={user?.avatarUrl || undefined}
              className={user?.avatarUrl ? undefined : 'user-avatar-fallback'}
            >
              {getAvatarInitial(user?.username)}
            </Avatar>
            <span className="sidebar-user-name">{user?.username}</span>
          </button>
          <Button
            className="sidebar-logout"
            icon={<LogoutOutlined />}
            onClick={handleLogout}
            aria-label={t('auth.logout')}
          />
        </div>
      </aside>
      {isMobile && mobileMenuOpen && (
        <button
          type="button"
          className="sidebar-backdrop"
          aria-label="Close menu"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}
      <main className="app-main">
        <div className="app-background-layer">
          <FloatingStickers />
        </div>
        <div className="app-main-panel">
          <header className="app-header">
            {isMobile && (
              <Button
                className="mobile-menu-button"
                icon={<MenuOutlined />}
                onClick={() => setMobileMenuOpen(true)}
              />
            )}
            {!isMobile && (
              <Button
                className="sidebar-toggle-button"
                icon={sidebarCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                onClick={() => setSidebarCollapsed((value) => !value)}
                aria-label={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
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
          </header>
          <div className="app-content app-content-scroll">
            <Outlet />
          </div>
        </div>
      </main>
=======
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
    <div className={`app-shell ${themeClassName} ${sidebarStateClassName}`}>
      {!isMobile && (
        <Sider
          width={240}
          collapsedWidth={72}
          collapsed={sidebarCollapsed}
          trigger={null}
          className={`app-sider desktop-sider sidebar ${sidebarStateClassName}`}
        >
          <div className="sidebar-header">
            {renderBrand()}
          </div>
          <div className="sidebar-nav">
            {menu}
            {renderCollapseButton()}
          </div>
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
        <div className="sidebar-header">
          {renderBrand()}
        </div>
        <div className="sidebar-nav">
          {menu}
        </div>
      </Drawer>
      <Layout className="app-main-layout">
        <BackgroundLayer />
        <Header className="app-header">
          {isMobile && (
            <Button
              className="mobile-menu-button"
              icon={<MenuOutlined />}
              onClick={() => setMobileMenuOpen(true)}
            />
          )}
          <div className="app-header-title">
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
>>>>>>> 6529f625afb39156fe842b5f2632498ae2ddf6af
    </div>
  );
}
