import { Button, Card, Result } from 'antd';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { PageErrorType } from '../../utils/error';

interface ErrorStateProps {
  type?: PageErrorType;
  title?: string;
  description?: string;
  onRetry?: () => void;
}

export function ErrorState({ type = '500', title, description, onRetry }: ErrorStateProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const status = type === 'network' ? '500' : type;

  const fallbackTitle =
    type === '403'
      ? t('error.forbiddenTitle')
      : type === '404'
        ? t('error.notFoundTitle')
        : t('error.serverTitle');
  const fallbackDescription =
    type === '403'
      ? t('error.forbidden')
      : type === '404'
        ? t('error.notFound')
        : type === 'network'
          ? t('error.network')
          : t('error.server');

  return (
    <Card className="state-card">
      <Result
        status={status}
        title={title || fallbackTitle}
        subTitle={description || fallbackDescription}
        extra={[
          onRetry ? (
            <Button key="retry" type="primary" onClick={onRetry}>
              {t('error.retry')}
            </Button>
          ) : null,
          <Button key="home" onClick={() => navigate('/')}>
            {t('error.backHome')}
          </Button>,
        ].filter(Boolean)}
      />
    </Card>
  );
}
