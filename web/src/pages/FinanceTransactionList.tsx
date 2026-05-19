import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, DatePicker, Form, Input, InputNumber, message, Modal, Popconfirm, Select, Space, Table, Tag, Typography } from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import {
  deleteTransaction,
  getTransactionCategories,
  getTransactions,
  Transaction,
  TransactionCategory,
  TransactionType,
  updateTransaction,
} from '../api/accounting';
import { formatDateTime } from '../utils/date';
import { getTransactionCategoryLabel, getTransactionTypeLabel } from '../utils/display';

interface EditValues {
  type: TransactionType;
  amount: number;
  categoryId?: number;
  title: string;
  note?: string;
  transactionTime: Dayjs;
}

export function FinanceTransactionList() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [items, setItems] = useState<Transaction[]>([]);
  const [type, setType] = useState<TransactionType | undefined>();
  const [month, setMonth] = useState<Dayjs>(dayjs());
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<Transaction | null>(null);
  const [categories, setCategories] = useState<TransactionCategory[]>([]);
  const [form] = Form.useForm<EditValues>();
  const [messageApi, contextHolder] = message.useMessage();

  const loadData = async () => {
    setLoading(true);
    try {
      const startDate = month.startOf('month').format('YYYY-MM-DD');
      const endDate = month.endOf('month').format('YYYY-MM-DD');
      const response = await getTransactions({ type, startDate, endDate, page: 1, size: 50 });
      setItems(response.data.data);
    } finally {
      setLoading(false);
    }
  };

  const openEdit = async (record: Transaction) => {
    setEditing(record);
    const response = await getTransactionCategories(record.type);
    setCategories(response.data.data);
    form.setFieldsValue({
      type: record.type,
      amount: record.amount,
      categoryId: record.categoryId,
      title: record.title,
      note: record.note,
      transactionTime: dayjs(record.transactionTime),
    });
  };

  const handleSave = async () => {
    if (!editing) return;
    const values = await form.validateFields();
    try {
      await updateTransaction(editing.id, {
        type: values.type,
        amount: values.amount,
        categoryId: values.categoryId,
        title: values.title,
        note: values.note,
        transactionTime: values.transactionTime.format('YYYY-MM-DDTHH:mm:ss'),
      });
      messageApi.success(t('finance.updateSuccess'));
      setEditing(null);
      loadData();
    } catch (error) {
      messageApi.error(t('finance.updateFailed'));
    }
  };

  const handleDelete = async (record: Transaction) => {
    try {
      await deleteTransaction(record.id);
      messageApi.success(t('finance.deleteSuccess'));
      loadData();
    } catch (error) {
      messageApi.error(t('finance.deleteFailed'));
    }
  };

  useEffect(() => {
    loadData();
  }, [type, month]);

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('finance.transactions')}</Typography.Title>
          <Typography.Text type="secondary">{t('finance.transactionsSubtitle')}</Typography.Text>
        </div>
        <Space wrap>
          <DatePicker picker="month" value={month} onChange={(value) => value && setMonth(value)} />
          <Select
            allowClear
            className="todo-status-filter"
            placeholder={t('finance.type')}
            value={type}
            onChange={setType}
            options={[
              { value: 'EXPENSE', label: t('finance.expense') },
              { value: 'INCOME', label: t('finance.income') },
            ]}
          />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={loadData}>{t('common.refresh')}</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/finance/create')}>{t('finance.addTransaction')}</Button>
        </Space>
      </div>
      <Table
        rowKey="id"
        loading={loading}
        dataSource={items}
        columns={[
          { title: t('finance.titleField'), dataIndex: 'title' },
          { title: t('finance.accountBook'), dataIndex: 'accountBookName' },
          { title: t('finance.category'), dataIndex: 'categoryName', render: (value) => getTransactionCategoryLabel(t, value) },
          { title: t('finance.type'), dataIndex: 'type', render: (value) => <Tag color={value === 'INCOME' ? 'green' : 'red'}>{getTransactionTypeLabel(t, value)}</Tag> },
          { title: t('finance.amount'), dataIndex: 'amount' },
          { title: t('finance.transactionTime'), dataIndex: 'transactionTime', render: (value) => formatDateTime(value, t, i18n.resolvedLanguage) },
          {
            title: t('common.edit'),
            render: (_, record) => (
              <Space>
                <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(record)}>{t('common.edit')}</Button>
                <Popconfirm title={t('finance.deleteConfirm')} onConfirm={() => handleDelete(record)}>
                  <Button size="small" danger icon={<DeleteOutlined />}>{t('common.delete')}</Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />

      <Modal title={t('finance.editTransaction')} open={!!editing} onOk={handleSave} onCancel={() => setEditing(null)} okText={t('common.save')} cancelText={t('common.cancel')}>
        <Form form={form} layout="vertical">
          <Form.Item name="type" label={t('finance.type')} rules={[{ required: true }]}>
            <Select
              onChange={(value) => {
                getTransactionCategories(value).then((response) => setCategories(response.data.data));
                form.setFieldValue('categoryId', undefined);
              }}
              options={[
                { value: 'EXPENSE', label: t('finance.expense') },
                { value: 'INCOME', label: t('finance.income') },
              ]}
            />
          </Form.Item>
          <Form.Item name="amount" label={t('finance.amount')} rules={[{ required: true }]}>
            <InputNumber min={0.01} precision={2} className="full-width" />
          </Form.Item>
          <Form.Item name="categoryId" label={t('finance.category')}>
            <Select allowClear options={categories.map((item) => ({ value: item.id, label: getTransactionCategoryLabel(t, item.name, item.icon) }))} />
          </Form.Item>
          <Form.Item name="title" label={t('finance.titleField')} rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="note" label={t('finance.note')}>
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="transactionTime" label={t('finance.transactionTime')} rules={[{ required: true }]}>
            <DatePicker showTime className="full-width" />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
