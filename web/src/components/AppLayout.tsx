import { DownOutlined, HeartOutlined, LogoutOutlined, MenuFoldOutlined, MenuOutlined, MenuUnfoldOutlined, SettingOutlined, UserOutlined } from '@ant-design/icons';
import { Avatar, Button, Dropdown, Grid, Input, Select, Space, Tooltip, Typography } from 'antd';
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

const { useBreakpoint } = Grid;
const SIDEBAR_COLLAPSED_STORAGE_KEY = 'lifelink_sidebar_collapsed';

function getCreatedAtTime(item: RelationshipSummary) {
  const time = new Date(item.createdAt).getTime();
  return Number.isFinite(time) ? time : Number.MAX_SAFE_INTEGER;
}

function sortRelationshipsByCreatedAt(items: RelationshipSummary[]) {
  return [...items].sort((a, b) => {
    const timeDiff = getCreatedAtTime(a) - getCreatedAtTime(b);
    return timeDiff || a.id - b.id;
  });
}

function hasRelationship(items: RelationshipSummary[], id?: number) {
  return Boolean(id && items.some((item) => item.id === id));
}

function buildScopedPathForSelectedSpace(location: ReturnType<typeof useLocation>, relationshipId: number) {
  const { pathname, search, hash } = location;
  const searchParams = new URLSearchParams(search);
  const relationshipPath = pathname.match(/^\/relationships\/\d+/);

  if (relationshipPath) {
    return `${pathname.replace(/^\/relationships\/\d+/, `/relationships/${relationshipId}`)}${search}${hash}`;
  }

  if (pathname === '/activities') {
    return `/relationships/${relationshipId}/activities`;
  }

  if (pathname === '/anniversaries') {
    return `/relationships/${relationshipId}/anniversaries`;
  }

  if (pathname === '/cycle-care') {
    return `/relationships/${relationshipId}/cycle-care`;
  }

  if (pathname === '/daily' || pathname === '/daily/create') {
    searchParams.set('relationshipId', String(relationshipId));
    searchParams.delete('spaceId');
    return `${pathname}?${searchParams.toString()}${hash}`;
  }

  if (
    pathname.startsWith('/finance')
    && (searchParams.get('scope') === 'space' || searchParams.has('spaceId') || searchParams.has('relationshipId'))
  ) {
    searchParams.set('scope', 'space');
    searchParams.set('spaceId', String(relationshipId));
    searchParams.delete('relationshipId');
    return `${pathname}?${searchParams.toString()}${hash}`;
  }

  return null;
}

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
  const [sidebarCollapsed, setSidebarCollapsed] = useState(() => {
    try {
      return localStorage.getItem(SIDEBAR_COLLAPSED_STORAGE_KEY) === 'true';
    } catch {
      return false;
    }
  });
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [selectedRelationshipId, setSelectedRelationshipId] = useState<number | undefined>();
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const philosophyEnabled = Boolean(user?.features?.philosophyEnabled);
  const routeRelationshipId = getRouteRelationshipId(location.pathname, location.search);
  const currentRelationshipId = selectedRelationshipId;
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

  const handleSpaceChange = (value?: number) => {
    if (!value) {
      return;
    }
    setSelectedRelationshipId(value);
    const nextPath = buildScopedPathForSelectedSpace(location, value);
    if (nextPath) {
      navigate(nextPath);
    }
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
    if (isAuthenticated) {
      fetchBackgroundSetting().catch(() => undefined);
    }
  }, [fetchBackgroundSetting, isAuthenticated]);

  useEffect(() => {
    if (!isAuthenticated) {
      setRelationships([]);
      setSelectedRelationshipId(undefined);
      return;
    }

    getRelationships()
      .then((response) => setRelationships(sortRelationshipsByCreatedAt(response.data.data)))
      .catch(() => setRelationships([]));
  }, [isAuthenticated]);

  useEffect(() => {
    if (relationships.length === 0) {
      setSelectedRelationshipId(undefined);
      return;
    }

    setSelectedRelationshipId((current) => {
      if (hasRelationship(relationships, routeRelationshipId)) {
        return routeRelationshipId;
      }
      if (hasRelationship(relationships, current)) {
        return current;
      }
      return relationships[0].id;
    });
  }, [relationships, routeRelationshipId]);

  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname, location.search]);

  const themeClassName = hasCoupleRelationship ? 'theme-colorful' : 'theme-grayscale';
  const sidebarStateClassName = sidebarCollapsed ? 'is-collapsed' : 'is-expanded';
  const sidebarLayoutClassName = isMobile ? 'is-expanded' : sidebarStateClassName;

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
    <div className="app-menu grouped-nav">
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
    </div>
  );

  return (
    <div className={`app-shell ${themeClassName} ${sidebarStateClassName}`}>
      <aside
        className={`app-sidebar sidebar ${sidebarLayoutClassName} ${mobileMenuOpen ? 'is-open' : ''}`}
        aria-label={t('menu.navigation')}
      >
        <div className="sidebar-header">
          {renderBrand()}
        </div>
        <nav className="sidebar-scroll sidebar-nav" aria-label={t('menu.navigation')}>
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
          {!isMobile && renderCollapseButton()}
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
      <main className="app-main app-main-layout">
        <BackgroundLayer />
        <div className="app-main-panel">
          <header className="app-header">
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
                  onChange={handleSpaceChange}
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
          </header>
          <div className="app-content-scroll">
            <div className="app-content">
              <Outlet />
            </div>
            <footer className="app-footer">
              <SiteFooter />
            </footer>
          </div>
        </div>
      </main>
    </div>
  );
}
