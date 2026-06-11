import { Button, Card, DatePicker, Form, Input, InputNumber, message, Select, Space, Tag, Typography } from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { AccountBook, createTransaction, getAccountBooks, getTransactionCategories, TransactionCategory, TransactionType } from '../api/accounting';
import { getTransactionCategoryLabel } from '../utils/display';

interface TransactionFormValues {
  accountBookId: number;
  type: TransactionType;
  amount: number;
  categoryId?: number;
  title: string;
  note?: string;
  transactionTime: Dayjs;
}

export function FinanceCreateTransaction() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const scope = searchParams.get('scope') === 'space' || searchParams.get('spaceId') || searchParams.get('relationshipId') ? 'space' : 'personal';
  const relationshipId = searchParams.get('spaceId') || searchParams.get('relationshipId');
  const accountBookId = searchParams.get('accountBookId');
  const [books, setBooks] = useState<AccountBook[]>([]);
  const [categories, setCategories] = useState<TransactionCategory[]>([]);
  const [type, setType] = useState<TransactionType>('EXPENSE');
  const [form] = Form.useForm<TransactionFormValues>();
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    getAccountBooks().then((response) => {
      const items = relationshipId
        ? response.data.data.filter((book) => String(book.relationshipId) === relationshipId)
        : response.data.data.filter((book) => book.type === 'PERSONAL');
      setBooks(items);
      const preferredBook = accountBookId ? items.find((item) => String(item.id) === accountBookId) : undefined;
      if (preferredBook || items.length > 0) {
        form.setFieldsValue({ accountBookId: preferredBook?.id || items[0].id });
      }
    });
  }, [accountBookId, relationshipId]);

  useEffect(() => {
    getTransactionCategories(type).then((response) => setCategories(response.data.data));
  }, [type]);

  const handleSubmit = async (values: TransactionFormValues) => {
    await createTransaction({
      accountBookId: values.accountBookId,
      type: values.type,
      amount: values.amount,
      categoryId: values.categoryId,
      title: values.title,
      note: values.note,
      transactionTime: values.transactionTime.format('YYYY-MM-DDTHH:mm:ss'),
    });
    messageApi.success(t('finance.createSuccess'));
    navigate(relationshipId ? `/finance?scope=space&spaceId=${relationshipId}` : '/finance');
  };

  return (
    <div className="page-narrow">
      {contextHolder}
      <Typography.Title level={2}>{scope === 'space' ? t('finance.addSpaceTransaction') : t('finance.addPersonalTransaction')}</Typography.Title>
      <Card>
        <Space wrap className="finance-create-scope">
          <Tag>{t('finance.currentScope')}: {scope === 'space' ? t('finance.spaceLedger') : t('finance.personalLedger')}</Tag>
        </Space>
        <Form form={form} layout="vertical" onFinish={handleSubmit} initialValues={{ type: 'EXPENSE', transactionTime: dayjs() }}>
          <Form.Item name="accountBookId" label={t('finance.accountBook')} rules={[{ required: true, message: t('finance.accountBookRequired') }]}>
            <Select options={books.map((book) => ({ value: book.id, label: book.name }))} />
          </Form.Item>
          <Form.Item name="type" label={t('finance.type')} rules={[{ required: true }]}>
            <Select
              onChange={(value) => {
                setType(value);
                form.setFieldValue('categoryId', undefined);
              }}
              options={[
                { value: 'EXPENSE', label: t('finance.expense') },
                { value: 'INCOME', label: t('finance.income') },
              ]}
            />
          </Form.Item>
          <Form.Item name="amount" label={t('finance.amount')} rules={[{ required: true, message: t('finance.amountRequired') }]}>
            <InputNumber min={0.01} precision={2} className="full-width" />
          </Form.Item>
          <Form.Item name="categoryId" label={t('finance.category')}>
            <Select allowClear options={categories.map((item) => ({ value: item.id, label: getTransactionCategoryLabel(t, item.name, item.icon) }))} />
          </Form.Item>
          <Form.Item name="title" label={t('finance.titleField')} rules={[{ required: true, message: t('finance.titleRequired') }]}>
            <Input />
          </Form.Item>
          <Form.Item name="note" label={t('finance.note')}>
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="transactionTime" label={t('finance.transactionTime')} rules={[{ required: true }]}>
            <DatePicker showTime className="full-width" />
          </Form.Item>
          <Button type="primary" htmlType="submit">{t('common.save')}</Button>
        </Form>
      </Card>
    </div>
  );
}
