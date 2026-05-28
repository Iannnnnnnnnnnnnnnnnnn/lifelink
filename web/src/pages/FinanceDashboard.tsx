import { DollarOutlined, PlusOutlined, ReloadOutlined, UnorderedListOutlined } from '@ant-design/icons';
import { Button, Card, Col, Form, Input, message, Modal, Row, Select, Space, Statistic, Table, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import {
  AccountBook,
  createAccountBook,
  getAccountBooks,
  getMonthlyFinanceSummary,
  getTransactions,
  MonthlyFinanceSummary,
  Transaction,
} from '../api/accounting';
import { formatDateTime } from '../utils/date';
import { getTransactionCategoryLabel, getTransactionTypeLabel } from '../utils/display';

export function FinanceDashboard() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [books, setBooks] = useState<AccountBook[]>([]);
  const [bookId, setBookId] = useState<number | undefined>();
  const [summary, setSummary] = useState<MonthlyFinanceSummary>({ totalIncome: 0, totalExpense: 0, balance: 0 });
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm<{ name: string }>();
  const [messageApi, contextHolder] = message.useMessage();

  const loadData = async (nextBookId = bookId) => {
    setLoading(true);
    try {
      const [bookRes, summaryRes, transactionRes] = await Promise.all([
        getAccountBooks(),
        getMonthlyFinanceSummary({ accountBookId: nextBookId }),
        getTransactions({ accountBookId: nextBookId, page: 1, size: 8 }),
      ]);
      setBooks(bookRes.data.data);
      setSummary(summaryRes.data.data);
      setTransactions(transactionRes.data.data);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBook = async () => {
    const values = await form.validateFields();
    await createAccountBook({ name: values.name, type: 'PERSONAL' });
    messageApi.success(t('finance.bookCreated'));
    setOpen(false);
    form.resetFields();
    loadData();
  };

  useEffect(() => {
    loadData();
  }, []);

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('finance.title')}</Typography.Title>
          <Typography.Text type="secondary">{t('finance.subtitle')}</Typography.Text>
        </div>
        <Space wrap>
          <Select
            allowClear
            className="relationship-filter"
            placeholder={t('finance.accountBook')}
            value={bookId}
            options={books.map((book) => ({ value: book.id, label: book.name }))}
            onChange={(value) => {
              setBookId(value);
              loadData(value);
            }}
          />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={() => loadData()}>{t('common.refresh')}</Button>
          <Button icon={<UnorderedListOutlined />} onClick={() => navigate('/finance/transactions')}>{t('finance.transactions')}</Button>
          <Button onClick={() => setOpen(true)}>{t('finance.createBook')}</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/finance/create')}>{t('finance.addTransaction')}</Button>
        </Space>
      </div>

      <Row gutter={[16, 16]} className="finance-summary-grid">
        <Col xs={24} md={8}>
          <Card className="finance-metric-card"><Statistic title={t('finance.income')} value={summary.totalIncome} precision={2} prefix={<DollarOutlined />} /></Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="finance-metric-card"><Statistic title={t('finance.expense')} value={summary.totalExpense} precision={2} prefix={<DollarOutlined />} /></Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="finance-metric-card"><Statistic title={t('finance.balance')} value={summary.balance} precision={2} prefix={<DollarOutlined />} /></Card>
        </Col>
      </Row>

      <Card title={t('finance.recentTransactions')}>
        <Table
          rowKey="id"
          loading={loading}
          dataSource={transactions}
          pagination={false}
          columns={[
            { title: t('finance.titleField'), dataIndex: 'title' },
            { title: t('finance.accountBook'), dataIndex: 'accountBookName' },
            { title: t('finance.category'), dataIndex: 'categoryName', render: (value) => getTransactionCategoryLabel(t, value) },
            { title: t('finance.type'), dataIndex: 'type', render: (value) => <Tag color={value === 'INCOME' ? 'green' : 'red'}>{getTransactionTypeLabel(t, value)}</Tag> },
            { title: t('finance.amount'), dataIndex: 'amount' },
            { title: t('finance.transactionTime'), dataIndex: 'transactionTime', render: (value) => formatDateTime(value, t, i18n.resolvedLanguage) },
          ]}
        />
      </Card>

      <Modal title={t('finance.createBook')} open={open} onOk={handleCreateBook} onCancel={() => setOpen(false)} okText={t('common.create')} cancelText={t('common.cancel')}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label={t('finance.bookName')} rules={[{ required: true, message: t('finance.bookNameRequired') }]}>
            <Input placeholder={t('finance.bookName')} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
