import { Alert } from 'antd';
import { useTranslation } from 'react-i18next';

export function PhilosophyDisclaimer() {
  const { t } = useTranslation();

  return (
    <Alert
      className="philosophy-disclaimer"
      type="info"
      showIcon
      message={t('philosophy.disclaimer')}
    />
  );
}
