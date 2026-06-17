import {
  BellOutlined,
  BulbOutlined,
  CalendarOutlined,
  CheckSquareOutlined,
  ClockCircleOutlined,
  DollarOutlined,
  GiftOutlined,
  HeartOutlined,
  HomeOutlined,
  ReadOutlined,
  SearchOutlined,
  SettingOutlined,
  TeamOutlined,
  ThunderboltOutlined,
  UserOutlined,
} from '@ant-design/icons';
import type { TFunction } from 'i18next';
import type { Location } from 'react-router-dom';
import type { ReactNode } from 'react';
import type { RelationshipSummary } from '../../api/relationship';

export interface PrimaryNavItem {
  key: string;
  label: string;
  icon: ReactNode;
  to: string;
  active: boolean;
  disabled?: boolean;
}

export interface PrimaryNavSection {
  key: string;
  label: string;
  active: boolean;
  items: PrimaryNavItem[];
}

interface BuildPrimaryNavParams {
  t: TFunction;
  location: Location;
  currentRelationship?: RelationshipSummary;
  currentRelationshipId?: number;
  hasCoupleRelationship: boolean;
  philosophyEnabled: boolean;
}

export function getRouteRelationshipId(pathname: string, search = '') {
  const match = pathname.match(/^\/relationships\/(\d+)/);
  if (match) {
    return Number(match[1]);
  }
  const searchParams = new URLSearchParams(search);
  const spaceId = searchParams.get('spaceId') || searchParams.get('relationshipId');
  return spaceId ? Number(spaceId) : undefined;
}

function isRelationshipChild(pathname: string, child: string) {
  return new RegExp(`^/relationships/\\d+/${child}(?:/|$)`).test(pathname);
}

function isSpaceFinance(location: Location) {
  const searchParams = new URLSearchParams(location.search);
  return location.pathname === '/relationships/:relationshipId/finance'
    || isRelationshipChild(location.pathname, 'finance')
    || (location.pathname.startsWith('/finance') && (searchParams.get('scope') === 'space' || searchParams.has('spaceId') || searchParams.has('relationshipId')));
}

export function buildPrimaryNavSections({
  t,
  location,
  currentRelationship,
  currentRelationshipId,
  hasCoupleRelationship,
  philosophyEnabled,
}: BuildPrimaryNavParams): PrimaryNavSection[] {
  const pathname = location.pathname;
  const relationshipBase = currentRelationshipId ? `/relationships/${currentRelationshipId}` : '/relationships';
  const spaceName = currentRelationship?.name || t('menu.currentSpace');
  const spaceFinanceActive = isSpaceFinance(location);
  const personalFinanceActive = pathname.startsWith('/finance') && !spaceFinanceActive;

  const overviewItems: PrimaryNavItem[] = [
    {
      key: 'home',
      label: t('menu.home'),
      icon: <HomeOutlined />,
      to: '/',
      active: pathname === '/',
    },
    {
      key: 'search',
      label: t('menu.search'),
      icon: <SearchOutlined />,
      to: '/search',
      active: pathname.startsWith('/search'),
    },
  ];

  const relationshipItems: PrimaryNavItem[] = [
    {
      key: 'spaces',
      label: t('menu.spaceList'),
      icon: <TeamOutlined />,
      to: '/relationships',
      active: pathname === '/relationships' || pathname === '/relationships/create' || pathname === '/relationships/join',
    },
    ...(currentRelationshipId
      ? [{
        key: 'space-current',
        label: spaceName,
        icon: <HeartOutlined />,
        to: relationshipBase,
        active: pathname === relationshipBase,
      }]
      : []),
    {
      key: 'space-todos',
      label: t('menu.spaceTodos'),
      icon: <CheckSquareOutlined />,
      to: currentRelationshipId ? `${relationshipBase}/todos` : '/relationships',
      active: isRelationshipChild(pathname, 'todos'),
      disabled: !currentRelationshipId,
    },
    {
      key: 'space-activities',
      label: t('menu.spaceActivities'),
      icon: <ThunderboltOutlined />,
      to: currentRelationshipId ? `${relationshipBase}/activities` : '/activities',
      active: isRelationshipChild(pathname, 'activities') || pathname === '/activities',
    },
    {
      key: 'daily',
      label: t('menu.daily'),
      icon: <ReadOutlined />,
      to: '/daily',
      active: pathname.startsWith('/daily'),
    },
    {
      key: 'space-timeline',
      label: t('menu.spaceTimeline'),
      icon: <ClockCircleOutlined />,
      to: currentRelationshipId ? `${relationshipBase}/timeline` : '/relationships',
      active: isRelationshipChild(pathname, 'timeline'),
      disabled: !currentRelationshipId,
    },
    {
      key: 'space-calendar',
      label: t('menu.spaceCalendar'),
      icon: <CalendarOutlined />,
      to: currentRelationshipId ? `${relationshipBase}/calendar` : '/relationships',
      active: isRelationshipChild(pathname, 'calendar'),
      disabled: !currentRelationshipId,
    },
    {
      key: 'space-anniversaries',
      label: t('menu.spaceAnniversaries'),
      icon: <CalendarOutlined />,
      to: currentRelationshipId ? `${relationshipBase}/anniversaries` : '/anniversaries',
      active: isRelationshipChild(pathname, 'anniversaries') || pathname.startsWith('/anniversaries'),
    },
    {
      key: 'space-finance',
      label: t('menu.spaceFinance'),
      icon: <DollarOutlined />,
      to: currentRelationshipId ? `/finance?scope=space&spaceId=${currentRelationshipId}` : '/finance?scope=space',
      active: spaceFinanceActive,
    },
    {
      key: 'space-cycle-care',
      label: t('menu.cycleCare'),
      icon: <HeartOutlined />,
      to: currentRelationshipId ? `${relationshipBase}/cycle-care` : '/cycle-care',
      active: pathname.startsWith('/cycle-care') || isRelationshipChild(pathname, 'cycle-care'),
      disabled: Boolean(currentRelationshipId && currentRelationship?.type && currentRelationship.type !== 'COUPLE'),
    },
  ];

  const personalItems: PrimaryNavItem[] = [
    {
      key: 'focus-rewards',
      label: t('menu.focusRewards'),
      icon: <GiftOutlined />,
      to: '/focus',
      active: pathname.startsWith('/focus') || pathname.startsWith('/focus-timer') || pathname.startsWith('/rewards'),
    },
    {
      key: 'finance',
      label: t('menu.finance'),
      icon: <DollarOutlined />,
      to: '/finance',
      active: personalFinanceActive,
    },
    ...(philosophyEnabled
      ? [{
        key: 'philosophy',
        label: t('menu.philosophy'),
        icon: <BulbOutlined />,
        to: '/philosophy',
        active: pathname.startsWith('/philosophy'),
      }]
      : []),
  ];

  const accountItems: PrimaryNavItem[] = [
    {
      key: 'profile',
      label: t('profile.title'),
      icon: <UserOutlined />,
      to: '/profile',
      active: pathname.startsWith('/profile') && location.hash !== '#settings',
    },
    {
      key: 'settings',
      label: t('menu.settings'),
      icon: <SettingOutlined />,
      to: '/profile#settings',
      active: pathname.startsWith('/profile') && location.hash === '#settings',
    },
  ];

  const sections: PrimaryNavSection[] = [
    { key: 'overview', label: t('menu.groupOverview'), items: overviewItems, active: overviewItems.some((item) => item.active) },
    { key: 'relationships', label: t('menu.groupRelationships'), items: relationshipItems, active: relationshipItems.some((item) => item.active) },
    { key: 'personal', label: t('menu.groupPersonalTools'), items: personalItems, active: personalItems.some((item) => item.active) },
    { key: 'account', label: t('menu.groupAccount'), items: accountItems, active: accountItems.some((item) => item.active) },
  ];

  return sections;
}

export function getPageContext(t: TFunction, location: Location) {
  const pathname = location.pathname;
  const searchParams = new URLSearchParams(location.search);
  const isSpaceFinancePage = pathname.startsWith('/finance') && (searchParams.get('scope') === 'space' || searchParams.has('spaceId') || searchParams.has('relationshipId'));

  if (pathname === '/') return { title: t('dashboard.title'), crumbs: [t('menu.home')] };
  if (pathname.startsWith('/search')) return { title: t('search.title'), crumbs: [t('menu.search')] };
  if (pathname === '/relationships') return { title: t('relationship.title'), crumbs: [t('menu.groupRelationships'), t('menu.spaceList')] };
  if (pathname === '/relationships/create') return { title: t('relationship.create'), crumbs: [t('menu.groupRelationships'), t('relationship.create')] };
  if (pathname === '/relationships/join') return { title: t('relationship.join'), crumbs: [t('menu.groupRelationships'), t('relationship.join')] };
  if (isRelationshipChild(pathname, 'todos')) return { title: t('todo.title'), crumbs: [t('menu.groupRelationships'), t('menu.spaceTodos')] };
  if (isRelationshipChild(pathname, 'activities')) return { title: t('activity.title'), crumbs: [t('menu.groupRelationships'), t('menu.spaceActivities')] };
  if (isRelationshipChild(pathname, 'timeline')) return { title: t('timeline.title'), crumbs: [t('menu.groupRelationships'), t('menu.spaceTimeline')] };
  if (isRelationshipChild(pathname, 'calendar')) return { title: t('calendar.title'), crumbs: [t('menu.groupRelationships'), t('menu.spaceCalendar')] };
  if (isRelationshipChild(pathname, 'anniversaries')) return { title: t('anniversary.title'), crumbs: [t('menu.groupRelationships'), t('menu.spaceAnniversaries')] };
  if (isRelationshipChild(pathname, 'cycle-care') || pathname.startsWith('/cycle-care')) return { title: t('cycle.title'), crumbs: [t('menu.groupRelationships'), t('menu.cycleCare')] };
  if (/^\/relationships\/\d+/.test(pathname)) return { title: t('relationship.detail'), crumbs: [t('menu.groupRelationships'), t('menu.currentSpace')] };
  if (pathname.startsWith('/daily')) return { title: t('daily.title'), crumbs: [t('menu.groupRelationships'), t('menu.daily')] };
  if (pathname.startsWith('/activities')) return { title: t('activity.allActivities'), crumbs: [t('menu.groupRelationships'), t('menu.spaceActivities')] };
  if (pathname.startsWith('/anniversaries')) return { title: t('anniversary.title'), crumbs: [t('menu.groupRelationships'), t('menu.spaceAnniversaries')] };
  if (pathname.startsWith('/finance')) return { title: isSpaceFinancePage ? t('finance.spaceLedger') : t('finance.personalLedger'), crumbs: [t('menu.finance')] };
  if (pathname.startsWith('/focus') || pathname.startsWith('/focus-timer') || pathname.startsWith('/rewards')) return { title: t('focusRewards.title'), crumbs: [t('menu.groupPersonalTools'), t('menu.focusRewards')] };
  if (pathname.startsWith('/philosophy')) return { title: t('philosophy.title'), crumbs: [t('menu.groupPersonalTools'), t('menu.philosophy')] };
  if (pathname.startsWith('/profile')) return { title: location.hash === '#settings' ? t('menu.settings') : t('profile.title'), crumbs: [t('menu.groupAccount')] };
  if (pathname.startsWith('/403')) return { title: t('error.forbiddenTitle'), crumbs: [t('error.forbiddenTitle')] };
  if (pathname.startsWith('/404')) return { title: t('error.notFoundTitle'), crumbs: [t('error.notFoundTitle')] };
  return { title: t('app.name'), crumbs: [t('app.name')] };
}

export const headerActionIcons = {
  notification: <BellOutlined />,
  settings: <SettingOutlined />,
  search: <SearchOutlined />,
  user: <UserOutlined />,
};
