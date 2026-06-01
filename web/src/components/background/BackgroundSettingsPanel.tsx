import {
  BgColorsOutlined,
  PictureOutlined,
  ReloadOutlined,
  SaveOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import { Button, Card, Col, Row, Segmented, Slider, Space, Switch, Typography, Upload, message } from 'antd';
import type { UploadProps } from 'antd';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  BackgroundPresetPosition,
  resetUserBackgroundSetting,
  uploadUserBackground,
} from '../../api/userBackground';
import { useBackgroundStore } from '../../store/backgroundStore';
import { BackgroundPreview } from './BackgroundPreview';

const MAX_BACKGROUND_SIZE_MB = 10;
const ALLOWED_BACKGROUND_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

const presetPositionOptions: BackgroundPresetPosition[] = [
  'CENTER',
  'TOP',
  'BOTTOM',
  'LEFT',
  'RIGHT',
  'TOP_LEFT',
  'TOP_RIGHT',
  'BOTTOM_LEFT',
  'BOTTOM_RIGHT',
];

const presetCoordinates: Record<BackgroundPresetPosition, { x: number; y: number }> = {
  CENTER: { x: 50, y: 50 },
  TOP: { x: 50, y: 0 },
  BOTTOM: { x: 50, y: 100 },
  LEFT: { x: 0, y: 50 },
  RIGHT: { x: 100, y: 50 },
  TOP_LEFT: { x: 0, y: 0 },
  TOP_RIGHT: { x: 100, y: 0 },
  BOTTOM_LEFT: { x: 0, y: 100 },
  BOTTOM_RIGHT: { x: 100, y: 100 },
};

export function BackgroundSettingsPanel() {
  const { t } = useTranslation();
  const setting = useBackgroundStore((state) => state.setting);
  const updateSetting = useBackgroundStore((state) => state.updateSetting);
  const setSetting = useBackgroundStore((state) => state.setSetting);
  const saveSetting = useBackgroundStore((state) => state.saveSetting);
  const [uploading, setUploading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [resetting, setResetting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    if (!ALLOWED_BACKGROUND_TYPES.includes(file.type)) {
      messageApi.error(t('background.invalidImageType'));
      return Upload.LIST_IGNORE;
    }
    if (file.size / 1024 / 1024 > MAX_BACKGROUND_SIZE_MB) {
      messageApi.error(t('background.imageTooLarge', { size: MAX_BACKGROUND_SIZE_MB }));
      return Upload.LIST_IGNORE;
    }
    return true;
  };

  const handleUpload: UploadProps['customRequest'] = async ({ file, onError, onSuccess }) => {
    setUploading(true);
    try {
      const response = await uploadUserBackground(file as File);
      updateSetting({
        enabled: true,
        imageUrl: response.data.data.imageUrl,
        objectKey: response.data.data.objectKey,
      });
      messageApi.success(t('background.uploadSuccess'));
      onSuccess?.(response.data);
    } catch (error) {
      messageApi.error(t('background.uploadFailed'));
      onError?.(error as Error);
    } finally {
      setUploading(false);
    }
  };

  const handlePresetChange = (value: BackgroundPresetPosition) => {
    const coordinates = presetCoordinates[value];
    updateSetting({
      presetPosition: value,
      positionX: coordinates.x,
      positionY: coordinates.y,
    });
  };

  const handleSave = async () => {
    if (setting.enabled && !setting.objectKey) {
      messageApi.warning(t('background.uploadFirst'));
      return;
    }
    setSaving(true);
    try {
      await saveSetting();
      messageApi.success(t('background.saveSuccess'));
    } catch (error) {
      messageApi.error(t('message.operationFailed'));
    } finally {
      setSaving(false);
    }
  };

  const handleReset = async () => {
    setResetting(true);
    try {
      const response = await resetUserBackgroundSetting();
      setSetting(response.data.data);
      messageApi.success(t('background.resetSuccess'));
    } catch (error) {
      messageApi.error(t('message.operationFailed'));
    } finally {
      setResetting(false);
    }
  };

  return (
    <Card
      className="profile-detail-card background-settings-card"
      title={
        <Space>
          <BgColorsOutlined />
          <span>{t('background.personalBackground')}</span>
        </Space>
      }
    >
      {contextHolder}
      <Row gutter={[20, 20]} align="top">
        <Col xs={24} lg={10}>
          <BackgroundPreview setting={setting} />
          <Space wrap className="background-actions">
            <Upload
              accept="image/jpeg,image/png,image/webp"
              beforeUpload={beforeUpload}
              customRequest={handleUpload}
              disabled={uploading}
              showUploadList={false}
            >
              <Button icon={<UploadOutlined />} loading={uploading}>
                {setting.imageUrl ? t('background.changeBackground') : t('background.uploadBackground')}
              </Button>
            </Upload>
            <Button icon={<ReloadOutlined />} loading={resetting} onClick={handleReset}>
              {t('background.resetDefault')}
            </Button>
          </Space>
          <Typography.Paragraph type="secondary" className="background-file-tip">
            {t('background.fileTip')}
          </Typography.Paragraph>
        </Col>
        <Col xs={24} lg={14}>
          <div className="background-control-grid">
            <div className="background-control-row background-switch-row">
              <Space>
                <PictureOutlined />
                <Typography.Text strong>{t('background.enableBackground')}</Typography.Text>
              </Space>
              <Switch
                checked={setting.enabled}
                checkedChildren={t('common.enabled')}
                unCheckedChildren={t('common.disabled')}
                onChange={(enabled) => updateSetting({ enabled })}
              />
            </div>

            <div className="background-control-row">
              <Typography.Text>{t('background.scale')}</Typography.Text>
              <Slider
                min={0.5}
                max={3}
                step={0.05}
                value={setting.scale}
                onChange={(scale) => updateSetting({ scale })}
              />
            </div>

            <div className="background-control-row">
              <Typography.Text>{t('background.position')}</Typography.Text>
              <Segmented
                className="background-position-segmented"
                value={setting.presetPosition}
                options={presetPositionOptions.map((value) => ({
                  value,
                  label: t(`background.positions.${value}`),
                }))}
                onChange={(value) => handlePresetChange(value as BackgroundPresetPosition)}
              />
            </div>

            <Row gutter={[16, 0]}>
              <Col xs={24} md={12}>
                <div className="background-control-row">
                  <Typography.Text>{t('background.positionX')}</Typography.Text>
                  <Slider
                    min={0}
                    max={100}
                    value={setting.positionX}
                    onChange={(positionX) => updateSetting({ positionX })}
                  />
                </div>
              </Col>
              <Col xs={24} md={12}>
                <div className="background-control-row">
                  <Typography.Text>{t('background.positionY')}</Typography.Text>
                  <Slider
                    min={0}
                    max={100}
                    value={setting.positionY}
                    onChange={(positionY) => updateSetting({ positionY })}
                  />
                </div>
              </Col>
            </Row>

            <Row gutter={[16, 0]}>
              <Col xs={24} md={8}>
                <div className="background-control-row">
                  <Typography.Text>{t('background.opacity')}</Typography.Text>
                  <Slider
                    min={0}
                    max={1}
                    step={0.01}
                    value={setting.opacity}
                    onChange={(opacity) => updateSetting({ opacity })}
                  />
                </div>
              </Col>
              <Col xs={24} md={8}>
                <div className="background-control-row">
                  <Typography.Text>{t('background.blur')}</Typography.Text>
                  <Slider
                    min={0}
                    max={30}
                    value={setting.blur}
                    onChange={(blur) => updateSetting({ blur })}
                  />
                </div>
              </Col>
              <Col xs={24} md={8}>
                <div className="background-control-row">
                  <Typography.Text>{t('background.overlay')}</Typography.Text>
                  <Slider
                    min={0}
                    max={1}
                    step={0.01}
                    value={setting.overlayOpacity}
                    onChange={(overlayOpacity) => updateSetting({ overlayOpacity })}
                  />
                </div>
              </Col>
            </Row>

            <div className="background-save-row">
              <Typography.Text type="secondary">{t('background.scopeGlobal')}</Typography.Text>
              <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={handleSave}>
                {t('background.saveSettings')}
              </Button>
            </div>
          </div>
        </Col>
      </Row>
    </Card>
  );
}
