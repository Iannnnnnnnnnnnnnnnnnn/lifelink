import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Card, DatePicker, Descriptions, Form, Input, message, Modal, Popconfirm, Select, Space, Tag, Typography, Upload } from 'antd';
import type { UploadFile, UploadProps } from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { Anniversary, AnniversaryRepeatType, deleteAnniversary, getAnniversaryDetail, updateAnniversary } from '../api/anniversary';
import { uploadFile, UploadFileResponse } from '../api/file';
import { ErrorState } from '../components/common/ErrorState';
import { PageLoading } from '../components/common/PageLoading';
import { getAnniversaryDisplayText, getRepeatTypeLabel } from '../utils/anniversary';
import { formatDate, formatDateTime } from '../utils/date';
import { getPageErrorType, PageErrorType } from '../utils/error';

interface AnniversaryEditValues {
  title: string;
  description?: string;
  anniversaryDate: Dayjs;
  repeatType: AnniversaryRepeatType;
}

export function AnniversaryDetail() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const params = useParams();
  const anniversaryId = Number(params.id);
  const [item, setItem] = useState<Anniversary | null>(null);
  const [loading, setLoading] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [editing, setEditing] = useState(false);
  const [backgroundFileId, setBackgroundFileId] = useState<number | undefined>();
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [form] = Form.useForm<AnniversaryEditValues>();
  const [messageApi, contextHolder] = message.useMessage();

  const loadDetail = async () => {
    if (!anniversaryId) return;
    setLoading(true);
    try {
      const response = await getAnniversaryDetail(anniversaryId);
      setItem(response.data.data);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDetail();
  }, [anniversaryId]);

  const openEdit = () => {
    if (!item) return;
    setBackgroundFileId(item.backgroundFileId);
    setFileList(
      item.backgroundUrl
        ? [
            {
              uid: String(item.backgroundFileId || item.id),
              name: item.title,
              status: 'done',
              url: item.backgroundUrl,
            },
          ]
        : [],
    );
    form.setFieldsValue({
      title: item.title,
      description: item.description,
      anniversaryDate: dayjs(item.anniversaryDate),
      repeatType: item.repeatType,
    });
    setEditing(true);
  };

  const handleSave = async () => {
    if (!item) return;
    const values = await form.validateFields();
    try {
      const response = await updateAnniversary(item.id, {
        title: values.title,
        description: values.description,
        anniversaryDate: values.anniversaryDate.format('YYYY-MM-DD'),
        repeatType: values.repeatType,
        backgroundFileId,
      });
      setItem(response.data.data);
      setEditing(false);
      messageApi.success(t('anniversary.updateSuccess'));
    } catch (error) {
      messageApi.error(t('anniversary.updateFailed'));
    }
  };

  const handleDelete = async () => {
    if (!item) return;
    try {
      await deleteAnniversary(item.id);
      messageApi.success(t('anniversary.deleteSuccess'));
      navigate('/anniversaries');
    } catch (error) {
      messageApi.error(t('anniversary.deleteFailed'));
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

  if (loading && !item) {
    return (
      <Space direction="vertical" size={16} className="page-wide">
        {contextHolder}
        <PageLoading />
      </Space>
    );
  }

  if (pageError) {
    return (
      <Space direction="vertical" size={16} className="page-wide">
        {contextHolder}
        <ErrorState type={pageError} onRetry={loadDetail} />
      </Space>
    );
  }

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('anniversary.detail')}</Typography.Title>
          <Typography.Text type="secondary">{item?.relationshipName || '-'}</Typography.Text>
        </div>
        <Space>
          <Button icon={<ReloadOutlined />} loading={loading} onClick={loadDetail}>
            {t('common.refresh')}
          </Button>
          <Button icon={<EditOutlined />} onClick={openEdit}>
            {t('common.edit')}
          </Button>
          <Popconfirm title={t('anniversary.confirmDelete')} okText={t('common.confirm')} cancelText={t('common.cancel')} onConfirm={handleDelete}>
            <Button danger icon={<DeleteOutlined />}>
              {t('common.delete')}
            </Button>
          </Popconfirm>
        </Space>
      </div>

      {item && (
        <div
          className="anniversary-card anniversary-detail-hero"
          style={item.backgroundUrl ? { backgroundImage: `url(${item.backgroundUrl})` } : undefined}
        >
          <div className="anniversary-card-content anniversary-detail-hero">
            <div>
              <Space wrap>
                <Tag>{item.relationshipName || '-'}</Tag>
                <Tag>{getRepeatTypeLabel(item.repeatType, t)}</Tag>
              </Space>
              <Typography.Title level={2}>{item.title}</Typography.Title>
              <Typography.Text>{item.description || t('common.noDescription')}</Typography.Text>
            </div>
            <div>
              <div className="anniversary-day-count">{item.dayCount}</div>
              <div className="anniversary-display-text">{getAnniversaryDisplayText(item, t)}</div>
            </div>
          </div>
        </div>
      )}

      <Card loading={loading}>
        <Descriptions bordered column={1}>
          <Descriptions.Item label={t('anniversary.name')}>{item?.title || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('anniversary.description')}>{item?.description || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('anniversary.relationship')}>{item?.relationshipName || '-'}</Descriptions.Item>
          <Descriptions.Item label={t('anniversary.date')}>{formatDate(item?.anniversaryDate, t, i18n.resolvedLanguage)}</Descriptions.Item>
          <Descriptions.Item label={t('anniversary.repeatType')}>{item ? getRepeatTypeLabel(item.repeatType, t) : '-'}</Descriptions.Item>
          <Descriptions.Item label={t('anniversary.displayType')}>{item ? t(`anniversary.${item.displayType.toLowerCase()}`) : '-'}</Descriptions.Item>
          <Descriptions.Item label={t('anniversary.createdAt')}>{formatDateTime(item?.createdAt, t, i18n.resolvedLanguage)}</Descriptions.Item>
          <Descriptions.Item label={t('anniversary.updatedAt')}>{formatDateTime(item?.updatedAt, t, i18n.resolvedLanguage)}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Modal title={t('anniversary.edit')} open={editing} onOk={handleSave} onCancel={() => setEditing(false)} okText={t('common.save')} cancelText={t('common.cancel')}>
        <Form form={form} layout="vertical">
          <Form.Item name="title" label={t('anniversary.name')} rules={[{ required: true, message: t('anniversary.titleRequired') }, { max: 100, message: t('anniversary.titleLength') }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label={t('anniversary.description')}>
            <Input.TextArea rows={4} />
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
        </Form>
      </Modal>
    </Space>
  );
}
