import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, message, Select, Typography, Upload } from 'antd';
import type { UploadFile, UploadProps } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { createDailyPost } from '../api/daily';
import { uploadFile, UploadFileResponse } from '../api/file';
import { getRelationships, RelationshipSummary } from '../api/relationship';

interface CreateDailyPostValues {
  relationshipId: number;
  content: string;
  mood?: string;
}

export function CreateDailyPost() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [imageIds, setImageIds] = useState<number[]>([]);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [messageApi, contextHolder] = message.useMessage();

  const handleSubmit = async (values: CreateDailyPostValues) => {
    try {
      await createDailyPost({
        relationshipId: values.relationshipId,
        content: values.content,
        mood: values.mood,
        visibility: 'RELATIONSHIP',
        imageIds,
      });
      messageApi.success(t('daily.publishSuccess'));
      navigate('/daily');
    } catch (error) {
      messageApi.error(t('daily.publishFailed'));
    }
  };

  useEffect(() => {
    getRelationships().then((response) => setRelationships(response.data.data));
  }, []);

  const uploadProps: UploadProps = {
    listType: 'picture-card',
    fileList,
    maxCount: 9,
    accept: '.jpg,.jpeg,.png,.webp',
    beforeUpload: (file) => {
      const isImage = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type);
      if (!isImage) {
        messageApi.error(t('daily.imageTypeLimit'));
        return Upload.LIST_IGNORE;
      }
      const isLt10M = file.size / 1024 / 1024 <= 10;
      if (!isLt10M) {
        messageApi.error(t('daily.imageSizeLimit'));
        return Upload.LIST_IGNORE;
      }
      return true;
    },
    customRequest: async ({ file, onSuccess, onError }) => {
      try {
        const response = await uploadFile(file as File);
        const payload: UploadFileResponse = response.data.data;
        setImageIds((prev) => prev.concat(payload.fileId));
        onSuccess?.(payload);
      } catch (error) {
        messageApi.error(t('daily.uploadFailed'));
        onError?.(new Error('upload failed'));
      }
    },
    onChange: ({ fileList: nextList }) => {
      setFileList(nextList);
    },
    onRemove: (file) => {
      const response = file.response as UploadFileResponse | undefined;
      if (response?.fileId) {
        setImageIds((prev) => prev.filter((id) => id !== response.fileId));
      }
      return true;
    },
  };

  return (
    <div className="page-narrow">
      {contextHolder}
      <Typography.Title level={2}>{t('daily.createTitle')}</Typography.Title>
      <Card>
        <Form layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="relationshipId"
            label={t('daily.relationship')}
            rules={[{ required: true, message: t('daily.relationshipRequired') }]}
          >
            <Select
              placeholder={t('daily.filterRelationship')}
              options={relationships.map((item) => ({ value: item.id, label: item.name }))}
            />
          </Form.Item>
          <Form.Item name="content" label={t('daily.content')} rules={[{ required: true, message: t('daily.contentRequired') }]}>
            <Input.TextArea rows={6} placeholder={t('daily.content')} />
          </Form.Item>
          <Form.Item name="mood" label={t('daily.mood')}>
            <Input placeholder={t('daily.mood')} />
          </Form.Item>
          <Form.Item label={t('daily.uploadImages')} extra={t('daily.imageLimitHint')}>
            <Upload {...uploadProps}>
              {fileList.length >= 9 ? null : (
                <button type="button" style={{ border: 0, background: 'none' }}>
                  <PlusOutlined />
                  <div style={{ marginTop: 8 }}>{t('daily.uploadImages')}</div>
                </button>
              )}
            </Upload>
          </Form.Item>
          <Button type="primary" htmlType="submit">
            {t('daily.create')}
          </Button>
        </Form>
      </Card>
    </div>
  );
}
