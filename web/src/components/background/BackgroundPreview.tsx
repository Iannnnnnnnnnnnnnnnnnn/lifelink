import { Typography } from 'antd';
import { useTranslation } from 'react-i18next';
import type { UserBackgroundSetting } from '../../api/userBackground';

interface BackgroundPreviewProps {
  setting: UserBackgroundSetting;
}

export function BackgroundPreview({ setting }: BackgroundPreviewProps) {
  const { t } = useTranslation();
  const imageStyle = setting.enabled && setting.imageUrl
    ? {
        backgroundImage: `url("${setting.imageUrl}")`,
        backgroundPosition: `${setting.positionX}% ${setting.positionY}%`,
        backgroundSize: `${Math.round(setting.scale * 100)}% auto`,
        opacity: setting.opacity,
        filter: `blur(${setting.blur}px)`,
      }
    : undefined;

  return (
    <div className={`background-preview ${setting.enabled && setting.imageUrl ? 'has-image' : ''}`}>
      <div className="background-preview-image" style={imageStyle} />
      <div className="background-preview-overlay" style={{ opacity: setting.overlayOpacity }} />
      <div className="background-preview-card">
        <Typography.Text className="background-preview-eyebrow">{t('background.preview')}</Typography.Text>
        <Typography.Title level={4}>{t('background.previewTitle')}</Typography.Title>
        <Typography.Paragraph type="secondary">{t('background.previewText')}</Typography.Paragraph>
      </div>
    </div>
  );
}
