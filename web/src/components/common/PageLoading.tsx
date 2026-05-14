import { Card, Skeleton, Space, Spin, Typography } from 'antd';
import { useTranslation } from 'react-i18next';

interface PageLoadingProps {
  text?: string;
  skeleton?: boolean;
}

export function PageLoading({ text, skeleton = true }: PageLoadingProps) {
  const { t } = useTranslation();

  return (
    <Card className="state-card">
      {skeleton ? (
        <Skeleton active paragraph={{ rows: 5 }} />
      ) : (
        <Space direction="vertical" align="center" className="state-center">
          <Spin />
          <Typography.Text type="secondary">{text || t('common.loading')}</Typography.Text>
        </Space>
      )}
    </Card>
  );
}
