import {
  CalendarOutlined,
  CheckSquareOutlined,
  ClockCircleOutlined,
  DollarOutlined,
  HeartOutlined,
  HomeOutlined,
  TeamOutlined,
  ThunderboltOutlined,
  UsergroupAddOutlined,
} from '@ant-design/icons';
import { Button, Space, Tooltip, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { useLocation, useNavigate } from 'react-router-dom';
import { getRelationshipDetail, type RelationshipDetail } from '../../api/relationship';

interface RelationshipSubNavProps {
  relationshipId: number;
  relationshipName?: string;
  relationshipType?: string;
}

interface SubNavItem {
  key: string;
  label: string;
  icon: ReactNode;
  to: string;
  disabled?: boolean;
  disabledReason?: string;
}

function getActiveKey(pathname: string, search: string, relationshipId: number) {
  const base = `/relationships/${relationshipId}`;
  const searchParams = new URLSearchParams(search);
  if (pathname === base && searchParams.get('tab') === 'members') return 'members';
  if (pathname === base) return 'overview';
  if (pathname === `${base}/todos`) return 'todos';
  if (pathname === `${base}/activities`) return 'activities';
  if (pathname === `${base}/timeline`) return 'timeline';
  if (pathname === `${base}/calendar`) return 'calendar';
  if (pathname === `${base}/anniversaries`) return 'anniversaries';
  if (pathname === `${base}/finance`) return 'finance';
  if (pathname === `${base}/cycle-care`) return 'cycleCare';
  if (pathname.startsWith('/finance') && searchParams.get('scope') === 'space' && searchParams.get('spaceId') === String(relationshipId)) return 'finance';
  return 'overview';
}

export function RelationshipSubNav({ relationshipId, relationshipName, relationshipType }: RelationshipSubNavProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const [detail, setDetail] = useState<RelationshipDetail | null>(null);

  useEffect(() => {
    if (!relationshipName || !relationshipType) {
      getRelationshipDetail(relationshipId)
        .then((response) => setDetail(response.data.data))
        .catch(() => undefined);
    }
  }, [relationshipId, relationshipName, relationshipType]);

  const displayName = relationshipName || detail?.name || t('menu.currentSpace');
  const type = relationshipType || detail?.type;
  const activeKey = getActiveKey(location.pathname, location.search, relationshipId);

  const items = useMemo<SubNavItem[]>(() => {
    const base = `/relationships/${relationshipId}`;
    const cycleDisabled = type ? type !== 'COUPLE' : false;
    return [
      { key: 'overview', label: t('relationship.overview'), icon: <HomeOutlined />, to: base },
      { key: 'members', label: t('member.title'), icon: <UsergroupAddOutlined />, to: `${base}?tab=members` },
      { key: 'todos', label: t('menu.spaceTodos'), icon: <CheckSquareOutlined />, to: `${base}/todos` },
      { key: 'activities', label: t('menu.spaceActivities'), icon: <ThunderboltOutlined />, to: `${base}/activities` },
      { key: 'timeline', label: t('menu.spaceTimeline'), icon: <ClockCircleOutlined />, to: `${base}/timeline` },
      { key: 'calendar', label: t('menu.spaceCalendar'), icon: <CalendarOutlined />, to: `${base}/calendar` },
      { key: 'anniversaries', label: t('menu.spaceAnniversaries'), icon: <CalendarOutlined />, to: `${base}/anniversaries` },
      { key: 'finance', label: t('menu.spaceFinance'), icon: <DollarOutlined />, to: `/finance?scope=space&spaceId=${relationshipId}` },
      {
        key: 'cycleCare',
        label: t('menu.cycleCare'),
        icon: <HeartOutlined />,
        to: `${base}/cycle-care`,
        disabled: cycleDisabled,
        disabledReason: t('relationship.cycleCareUnavailable'),
      },
    ];
  }, [relationshipId, t, type]);

  return (
    <div className="relationship-subnav">
      <div className="relationship-subnav-crumbs">
        <Typography.Text type="secondary">{t('menu.groupRelationships')}</Typography.Text>
        <span>/</span>
        <Typography.Text>{displayName}</Typography.Text>
        <span>/</span>
        <Typography.Text strong>{items.find((item) => item.key === activeKey)?.label || t('relationship.overview')}</Typography.Text>
      </div>
      <div className="relationship-subnav-scroll">
        <Space size={8} wrap={false}>
          {items.map((item) => {
            const button = (
              <Button
                key={item.key}
                type={activeKey === item.key ? 'primary' : 'default'}
                className={`relationship-subnav-item ${activeKey === item.key ? 'is-active' : ''}`}
                icon={item.icon}
                disabled={item.disabled}
                onClick={() => navigate(item.to)}
              >
                {item.label}
              </Button>
            );
            return item.disabled ? (
              <Tooltip key={item.key} title={item.disabledReason}>
                <span>{button}</span>
              </Tooltip>
            ) : button;
          })}
        </Space>
      </div>
    </div>
  );
}
