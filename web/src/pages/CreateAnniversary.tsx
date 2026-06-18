import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, DatePicker, Form, Input, message, Select, Typography, Upload } from 'antd';
import type { UploadFile, UploadProps } from 'antd';
import type { Dayjs } from 'dayjs';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { createAnniversary, AnniversaryRepeatType } from '../api/anniversary';
import { uploadFile, UploadFileResponse } from '../api/file';
import { getRelationships, RelationshipSummary } from '../api/relationship';

interface AnniversaryFormValues {
  relationshipId: number;
  title: string;
  description?: string;
  anniversaryDate: Dayjs;
  repeatType: AnniversaryRepeatType;
}

export function CreateAnniversary() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [backgroundFileId, setBackgroundFileId] = useState<number | undefined>();
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    getRelationships()
      .then((response) => setRelationships(response.data.data))
      .catch(() => messageApi.error(t('relationship.loadFailed')));
  }, []);

  const handleSubmit = async (values: AnniversaryFormValues) => {
    try {
      const response = await createAnniversary({
        relationshipId: values.relationshipId,
        title: values.title,
        description: values.description,
        anniversaryDate: values.anniversaryDate.format('YYYY-MM-DD'),
        repeatType: values.repeatType,
        backgroundFileId,
      });
      messageApi.success(t('anniversary.createSuccess'));
      navigate(`/anniversaries/${response.data.data.id}`);
    } catch (error) {
      messageApi.error(t('anniversary.createFailed'));
    }
  };

  const uploadProps: UploadProps = {
    listType: 'picture-card',
    fileList,
    maxCount: 1,
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
        setBackgroundFileId(payload.fileId);
        onSuccess?.(payload);
      } catch (error) {
        messageApi.error(t('daily.uploadFailed'));
        onError?.(new Error('upload failed'));
      }
    },
    onChange: ({ fileList: nextList }) => setFileList(nextList),
    onRemove: () => {
      setBackgroundFileId(undefined);
      return true;
    },
  };

  return (
    <div className="page-narrow">
      {contextHolder}
      <Typography.Title level={2}>{t('anniversary.create')}</Typography.Title>
      <Card>
        <Form layout="vertical" initialValues={{ repeatType: 'NONE' }} onFinish={handleSubmit}>
          <Form.Item name="relationshipId" label={t('anniversary.selectRelationship')} rules={[{ required: true, message: t('anniversary.relationshipRequired') }]}>
            <Select options={relationships.map((item) => ({ value: item.id, label: item.name }))} placeholder={t('anniversary.selectRelationship')} />
          </Form.Item>
          <Form.Item name="title" label={t('anniversary.name')} rules={[{ required: true, message: t('anniversary.titleRequired') }, { max: 100, message: t('anniversary.titleLength') }]}>
            <Input placeholder={t('anniversary.name')} />
          </Form.Item>
          <Form.Item name="description" label={t('anniversary.description')}>
            <Input.TextArea rows={4} placeholder={t('anniversary.description')} />
          </Form.Item>
          <Form.Item name="anniversaryDate" label={t('anniversary.date')} rules={[{ required: true, message: t('anniversary.dateRequired') }]}>
            <DatePicker className="full-width" />
          </Form.Item>
          <Form.Item name="repeatType" label={t('anniversary.repeatType')}>
            <Select
              options={[
                { value: 'NONE', label: t('anniversary.noneRepeat') },
                { value: 'YEARLY', label: t('anniversary.yearlyRepeat') },
              ]}
            />
          </Form.Item>
          <Form.Item label={t('anniversary.background')} className="anniversary-form-upload">
            <Upload {...uploadProps}>
              {fileList.length >= 1 ? null : (
                <button type="button" style={{ border: 0, background: 'none' }}>
                  <PlusOutlined />
                  <div style={{ marginTop: 8 }}>{t('anniversary.uploadBackground')}</div>
                </button>
              )}
            </Upload>
          </Form.Item>
          <Button type="primary" htmlType="submit">
            {t('common.create')}
          </Button>
        </Form>
      </Card>
    </div>
  );
}
