import { CameraOutlined, EyeOutlined, LoadingOutlined } from '@ant-design/icons';
import { Avatar, Button, Image, Space, Upload, message } from 'antd';
import type { UploadProps } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { uploadAvatar } from '../../api/user';
import { getAvatarInitial } from '../../utils/avatar';

interface AvatarUploaderProps {
  username?: string | null;
  avatarUrl?: string | null;
  onUploaded: (avatarUrl: string) => void;
}

const MAX_AVATAR_SIZE_MB = 5;
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

export function AvatarUploader({ username, avatarUrl, onUploaded }: AvatarUploaderProps) {
  const { t } = useTranslation();
  const [uploading, setUploading] = useState(false);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [imageLoadError, setImageLoadError] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const displayAvatarUrl = avatarUrl && !imageLoadError ? avatarUrl : undefined;

  useEffect(() => {
    setImageLoadError(false);
  }, [avatarUrl]);

  const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
      messageApi.error(t('profile.invalidImageType'));
      return Upload.LIST_IGNORE;
    }
    if (file.size / 1024 / 1024 > MAX_AVATAR_SIZE_MB) {
      messageApi.error(t('profile.imageTooLarge', { size: MAX_AVATAR_SIZE_MB }));
      return Upload.LIST_IGNORE;
    }
    return true;
  };

  const handleUpload: UploadProps['customRequest'] = async ({ file, onError, onSuccess }) => {
    setUploading(true);
    try {
      const response = await uploadAvatar(file as File);
      const nextAvatarUrl = response.data.data.avatarUrl;
      onUploaded(nextAvatarUrl);
      messageApi.success(t('profile.avatarUploadSuccess'));
      onSuccess?.(response.data);
    } catch (error) {
      messageApi.error(t('profile.avatarUploadFailed'));
      onError?.(error as Error);
    } finally {
      setUploading(false);
    }
  };

  const uploadProps: UploadProps = {
    accept: 'image/jpeg,image/png,image/webp',
    beforeUpload,
    customRequest: handleUpload,
    disabled: uploading,
    showUploadList: false,
  };

  return (
    <div className="avatar-uploader">
      {contextHolder}
      <Upload {...uploadProps}>
        <button type="button" className="avatar-upload-trigger" disabled={uploading}>
          <Avatar
            size={128}
            src={displayAvatarUrl}
            className={displayAvatarUrl ? 'profile-avatar-image' : 'profile-avatar-fallback'}
            onError={() => {
              setImageLoadError(true);
              return false;
            }}
          >
            {getAvatarInitial(username)}
          </Avatar>
          <span className="avatar-upload-mask">
            {uploading ? <LoadingOutlined /> : <CameraOutlined />}
            <span>{uploading ? t('common.loading') : t('profile.changeAvatar')}</span>
          </span>
        </button>
      </Upload>
      <Space className="avatar-uploader-actions">
        <Upload {...uploadProps}>
          <Button disabled={uploading} icon={<CameraOutlined />} size="small" type="text">
            {t('profile.uploadAvatar')}
          </Button>
        </Upload>
        <Button
          disabled={!displayAvatarUrl}
          icon={<EyeOutlined />}
          onClick={() => setPreviewOpen(true)}
          size="small"
          type="text"
        >
          {t('profile.previewAvatar')}
        </Button>
      </Space>
      {displayAvatarUrl && (
        <Image
          preview={{
            visible: previewOpen,
            onVisibleChange: (visible) => setPreviewOpen(visible),
          }}
          src={displayAvatarUrl}
          style={{ display: 'none' }}
        />
      )}
    </div>
  );
}
