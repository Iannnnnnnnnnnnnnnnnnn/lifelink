import { CalendarOutlined } from '@ant-design/icons';
import { Button, Space, Tag, Typography } from 'antd';
import { useTranslation } from 'react-i18next';

interface DashboardHeroProps {
  username?: string;
  onCreateDaily: () => void;
  onCreateSpace: () => void;
}

export function DashboardHero({ username, onCreateDaily, onCreateSpace }: DashboardHeroProps) {
  const { t, i18n } = useTranslation();
  const currentDate = new Intl.DateTimeFormat(i18n.resolvedLanguage === 'en-US' ? 'en-US' : 'zh-CN', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
  }).format(new Date());

  return (
    <section className="dashboard-hero-card">
      <div className="dashboard-hero-copy">
        <Space wrap className="dashboard-hero-tags">
          <Tag icon={<CalendarOutlined />}>{currentDate}</Tag>
        </Space>
        <Typography.Title level={1}>{t('dashboard.welcomeBack', { name: username || t('home.defaultUser') })}</Typography.Title>
        <Typography.Text>{t('dashboard.slogan')}</Typography.Text>
      </div>
      <Space wrap className="dashboard-hero-actions">
        <Button type="primary" size="large" onClick={onCreateDaily}>
          {t('dashboard.createDaily')}
        </Button>
        <Button size="large" onClick={onCreateSpace}>
          {t('dashboard.createSpace')}
        </Button>
      </Space>
      <div className="dashboard-hero-stickers" aria-hidden="true">
        <span>✨</span>
        <span>💌</span>
        <span>📅</span>
      </div>
    </section>
  );
}
