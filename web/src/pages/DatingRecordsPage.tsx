import {
  CalendarOutlined,
  DeleteOutlined,
  EditOutlined,
  EnvironmentOutlined,
  EyeOutlined,
  HeartOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { Button, Card, DatePicker, Descriptions, Form, Input, List, message, Modal, Select, Space, Statistic, Tag, Typography } from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import {
  createDatingRecord,
  DatingRecord,
  deleteDatingRecord,
  getDatingRecord,
  getDatingRecords,
  updateDatingRecord,
} from '../api/datingRecord';
import { ConfirmAction } from '../components/common/ConfirmAction';
import { EmptyState } from '../components/common/EmptyState';
import { ErrorState } from '../components/common/ErrorState';
import { formatDate, formatDateTime } from '../utils/date';
import { getPageErrorType, PageErrorType } from '../utils/error';

interface DatingRecordFormValues {
  datingDate: Dayjs;
  activities: string[];
  location?: string;
  note?: string;
}

export function DatingRecordsPage() {
  const { t, i18n } = useTranslation();
  const params = useParams();
  const relationshipId = Number(params.relationshipId);
  const [records, setRecords] = useState<DatingRecord[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<DatingRecord | null>(null);
  const [viewingRecord, setViewingRecord] = useState<DatingRecord | null>(null);
  const [viewLoading, setViewLoading] = useState(false);
  const [form] = Form.useForm<DatingRecordFormValues>();
  const [messageApi, contextHolder] = message.useMessage();

  const loadData = async () => {
    if (!Number.isFinite(relationshipId)) return;
    setLoading(true);
    try {
      const response = await getDatingRecords(relationshipId);
      setRecords(response.data.data.records);
      setTotal(response.data.data.total);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [relationshipId]);

  const openCreate = () => {
    setEditingRecord(null);
    form.setFieldsValue({ datingDate: dayjs(), activities: [], location: undefined, note: undefined });
    setFormOpen(true);
  };

  const openEdit = (record: DatingRecord) => {
    setEditingRecord(record);
    form.setFieldsValue({
      datingDate: dayjs(record.datingDate),
      activities: record.activities,
      location: record.location,
      note: record.note,
    });
    setFormOpen(true);
  };

  const openView = async (record: DatingRecord) => {
    setViewingRecord(record);
    setViewLoading(true);
    try {
      const response = await getDatingRecord(record.id);
      setViewingRecord(response.data.data);
    } catch (error) {
      messageApi.error(t('datingRecord.loadDetailFailed'));
    } finally {
      setViewLoading(false);
    }
  };

  const handleSubmit = async (values: DatingRecordFormValues) => {
    if (submitting) return;
    const activities = Array.from(new Set(values.activities.map((item) => item.trim()).filter(Boolean)));
    setSubmitting(true);
    try {
      const payload = {
        datingDate: values.datingDate.format('YYYY-MM-DD'),
        activities,
        location: values.location?.trim() || undefined,
        note: values.note?.trim() || undefined,
      };
      if (editingRecord) {
        await updateDatingRecord(editingRecord.id, payload);
        messageApi.success(t('datingRecord.updateSuccess'));
      } else {
        await createDatingRecord({ relationshipId, ...payload });
        messageApi.success(t('datingRecord.createSuccess'));
      }
      setFormOpen(false);
      setEditingRecord(null);
      form.resetFields();
      await loadData();
    } catch (error) {
      messageApi.error(t(editingRecord ? 'datingRecord.updateFailed' : 'datingRecord.createFailed'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (record: DatingRecord) => {
    try {
      await deleteDatingRecord(record.id);
      messageApi.success(t('datingRecord.deleteSuccess'));
      if (viewingRecord?.id === record.id) {
        setViewingRecord(null);
      }
      await loadData();
    } catch (error) {
      messageApi.error(t('datingRecord.deleteFailed'));
    }
  };

  return (
    <Space direction="vertical" size={16} className="page-wide dating-record-page">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('datingRecord.title')}</Typography.Title>
          <Typography.Text type="secondary">{t('datingRecord.subtitle')}</Typography.Text>
        </div>
        <Space wrap>
          <Button icon={<ReloadOutlined />} loading={loading} onClick={loadData}>
            {t('common.refresh')}
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            {t('datingRecord.create')}
          </Button>
        </Space>
      </div>

      <Card className="dating-record-summary">
        <div className="dating-record-summary-content">
          <div className="dating-record-summary-icon"><HeartOutlined /></div>
          <div>
            <Typography.Text type="secondary">{t('datingRecord.totalLabel')}</Typography.Text>
            <Statistic value={total} suffix={t('datingRecord.times')} />
          </div>
          <Typography.Text className="dating-record-summary-copy">{t('datingRecord.totalHint')}</Typography.Text>
        </div>
      </Card>

      {pageError ? (
        <ErrorState type={pageError} onRetry={loadData} />
      ) : (
        <Card loading={loading && records.length === 0} className="dating-record-list-card">
          {records.length === 0 && !loading ? (
            <EmptyState
              title={t('datingRecord.emptyTitle')}
              description={t('datingRecord.emptyDescription')}
              action={<Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>{t('datingRecord.createFirst')}</Button>}
            />
          ) : (
            <List
              dataSource={records}
              renderItem={(record) => (
                <List.Item
                  className="dating-record-item"
                  actions={[
                    <Button key="view" type="link" icon={<EyeOutlined />} onClick={() => openView(record)}>{t('common.view')}</Button>,
                    <Button key="edit" type="link" icon={<EditOutlined />} onClick={() => openEdit(record)}>{t('common.edit')}</Button>,
                    <ConfirmAction key="delete" title={t('datingRecord.confirmDelete')} onConfirm={() => handleDelete(record)}>
                      <Button type="link" danger icon={<DeleteOutlined />}>{t('common.delete')}</Button>
                    </ConfirmAction>,
                  ]}
                >
                  <List.Item.Meta
                    avatar={<div className="dating-record-sequence">{t('datingRecord.sequence', { count: record.sequenceNumber })}</div>}
                    title={
                      <Space wrap>
                        <CalendarOutlined />
                        <span>{formatDate(record.datingDate, t, i18n.resolvedLanguage)}</span>
                        {record.location && <Typography.Text type="secondary"><EnvironmentOutlined /> {record.location}</Typography.Text>}
                      </Space>
                    }
                    description={
                      <Space direction="vertical" size={8} className="full-width">
                        <Space wrap>{record.activities.map((activity) => <Tag key={activity} color="magenta">{activity}</Tag>)}</Space>
                        {record.note && <Typography.Paragraph type="secondary" ellipsis={{ rows: 2 }}>{record.note}</Typography.Paragraph>}
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          )}
        </Card>
      )}

      <Modal
        title={editingRecord ? t('datingRecord.edit') : t('datingRecord.create')}
        open={formOpen}
        confirmLoading={submitting}
        okText={t('common.save')}
        cancelText={t('common.cancel')}
        onOk={() => form.submit()}
        onCancel={() => {
          setFormOpen(false);
          setEditingRecord(null);
          form.resetFields();
        }}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="datingDate" label={t('datingRecord.date')} rules={[{ required: true, message: t('datingRecord.dateRequired') }]}>
            <DatePicker className="full-width" />
          </Form.Item>
          <Form.Item
            name="activities"
            label={t('datingRecord.activities')}
            extra={t('datingRecord.activitiesHint')}
            rules={[
              { required: true, type: 'array', min: 1, message: t('datingRecord.activitiesRequired') },
              {
                validator: (_, value?: string[]) => {
                  if ((value?.length || 0) > 20) return Promise.reject(new Error(t('datingRecord.activitiesCountLimit')));
                  if (value?.some((item) => item.trim().length > 100)) return Promise.reject(new Error(t('datingRecord.activityLengthLimit')));
                  return Promise.resolve();
                },
              },
            ]}
          >
            <Select mode="tags" tokenSeparators={[',', '，']} placeholder={t('datingRecord.activitiesPlaceholder')} />
          </Form.Item>
          <Form.Item name="location" label={t('datingRecord.location')} rules={[{ max: 200, message: t('datingRecord.locationLength') }]}>
            <Input prefix={<EnvironmentOutlined />} placeholder={t('datingRecord.locationPlaceholder')} />
          </Form.Item>
          <Form.Item name="note" label={t('datingRecord.note')} rules={[{ max: 2000, message: t('datingRecord.noteLength') }]}>
            <Input.TextArea rows={4} placeholder={t('datingRecord.notePlaceholder')} showCount maxLength={2000} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={viewingRecord ? t('datingRecord.sequence', { count: viewingRecord.sequenceNumber }) : t('datingRecord.detail')}
        open={Boolean(viewingRecord)}
        footer={viewingRecord ? [
          <Button key="close" onClick={() => setViewingRecord(null)}>{t('common.close')}</Button>,
          <Button key="edit" type="primary" icon={<EditOutlined />} onClick={() => {
            const record = viewingRecord;
            setViewingRecord(null);
            openEdit(record);
          }}>{t('common.edit')}</Button>,
        ] : null}
        loading={viewLoading}
        onCancel={() => setViewingRecord(null)}
      >
        {viewingRecord && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label={t('datingRecord.date')}>{formatDate(viewingRecord.datingDate, t, i18n.resolvedLanguage)}</Descriptions.Item>
            <Descriptions.Item label={t('datingRecord.activities')}>
              <Space wrap>{viewingRecord.activities.map((activity) => <Tag key={activity} color="magenta">{activity}</Tag>)}</Space>
            </Descriptions.Item>
            <Descriptions.Item label={t('datingRecord.location')}>{viewingRecord.location || '-'}</Descriptions.Item>
            <Descriptions.Item label={t('datingRecord.note')}>{viewingRecord.note || '-'}</Descriptions.Item>
            <Descriptions.Item label={t('datingRecord.createdAt')}>{formatDateTime(viewingRecord.createdAt, t, i18n.resolvedLanguage)}</Descriptions.Item>
            <Descriptions.Item label={t('datingRecord.updatedAt')}>{formatDateTime(viewingRecord.updatedAt, t, i18n.resolvedLanguage)}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </Space>
  );
}
