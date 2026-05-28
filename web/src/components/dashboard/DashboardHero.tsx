import { CalendarOutlined } from '@ant-design/icons';
import { Button, Space, Tag, Typography } from 'antd';
import { useTranslation } from 'react-i18next';
import { formatDashboardDate } from '../../utils/date';

interface DashboardHeroProps {
  onCreateDaily: () => void;
  onOpenSpaces: () => void;
}

export function DashboardHero({ onCreateDaily, onOpenSpaces }: DashboardHeroProps) {
  const { t, i18n } = useTranslation();
  const currentDate = formatDashboardDate(new Date(), i18n.resolvedLanguage);

  return (
    <section className="dashboard-hero-card">
      <div className="dashboard-hero-copy">
        <Space wrap className="dashboard-hero-tags">
          <Tag icon={<CalendarOutlined />}>{currentDate}</Tag>
        </Space>
        <Typography.Title level={1}>{t('dashboard.heroTitle')}</Typography.Title>
        <Typography.Text>{t('dashboard.slogan')}</Typography.Text>
      </div>
      <Space wrap className="dashboard-hero-actions">
        <Button type="primary" size="large" onClick={onOpenSpaces}>
          {t('dashboard.enterMySpace')}
        </Button>
        <Button size="large" onClick={onCreateDaily}>
          {t('dashboard.createDaily')}
        </Button>
      </Space>
    </section>
  );
}
