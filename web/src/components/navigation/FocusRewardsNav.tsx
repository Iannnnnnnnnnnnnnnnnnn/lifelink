import { GiftOutlined } from '@ant-design/icons';
import { Tabs } from 'antd';
import { useTranslation } from 'react-i18next';
import { useLocation, useNavigate } from 'react-router-dom';

function getActiveFocusRewardsKey(pathname: string, search: string) {
  const searchParams = new URLSearchParams(search);
  if (pathname.startsWith('/rewards/admin')) return 'admin';
  if (pathname.startsWith('/rewards')) {
    const tab = searchParams.get('tab');
    if (tab === 'redemptions') return 'redemptions';
    if (tab === 'ledger' || tab === 'coins') return 'coins';
    return 'rewards';
  }
  const focusTab = searchParams.get('tab');
  if (focusTab === 'records') return 'records';
  if (focusTab === 'coins') return 'coins';
  return 'timer';
}

export function FocusRewardsNav() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const activeKey = getActiveFocusRewardsKey(location.pathname, location.search);

  return (
    <Tabs
      className="module-tabs focus-rewards-tabs"
      activeKey={activeKey}
      onChange={(key) => {
        const targetMap: Record<string, string> = {
          timer: '/focus/timer',
          records: '/focus?tab=records',
          coins: '/rewards?tab=ledger',
          rewards: '/rewards',
          redemptions: '/rewards?tab=redemptions',
          admin: '/rewards/admin',
        };
        navigate(targetMap[key] || '/focus');
      }}
      items={[
        { key: 'timer', label: t('focusRewards.timer') },
        { key: 'records', label: t('focusRewards.records') },
        { key: 'coins', label: t('focusRewards.coins') },
        { key: 'rewards', label: t('focusRewards.rewardExchange'), icon: <GiftOutlined /> },
        { key: 'redemptions', label: t('focusRewards.myRedemptions') },
        { key: 'admin', label: t('focusRewards.rewardManagement') },
      ]}
    />
  );
}
