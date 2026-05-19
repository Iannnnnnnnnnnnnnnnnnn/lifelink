import { DollarOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Card, Col, Form, Input, message, Modal, Row, Space, Statistic, Table, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { AccountBook, createAccountBook, getAccountBooks, getMonthlyFinanceSummary, getTransactions, MonthlyFinanceSummary, Transaction } from '../api/accounting';
import { formatDateTime } from '../utils/date';
import { getTransactionCategoryLabel, getTransactionTypeLabel } from '../utils/display';

export function RelationshipFinance() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const params = useParams();
  const relationshipId = Number(params.relationshipId);
  const [books, setBooks] = useState<AccountBook[]>([]);
  const [summary, setSummary] = useState<MonthlyFinanceSummary>({ totalIncome: 0, totalExpense: 0, balance: 0 });
  const [items, setItems] = useState<Transaction[]>([]);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm<{ name: string }>();
  const [messageApi, contextHolder] = message.useMessage();

  const loadData = async () => {
    const [bookRes, summaryRes, txRes] = await Promise.all([
      getAccountBooks(),
      getMonthlyFinanceSummary({ relationshipId }),
      getTransactions({ relationshipId, page: 1, size: 20 }),
    ]);
    setBooks(bookRes.data.data.filter((book) => book.relationshipId === relationshipId));
    setSummary(summaryRes.data.data);
    setItems(txRes.data.data);
  };

  const handleCreateBook = async () => {
    const values = await form.validateFields();
    await createAccountBook({ name: values.name, type: 'RELATIONSHIP', relationshipId });
    messageApi.success(t('finance.bookCreated'));
    setOpen(false);
    form.resetFields();
    loadData();
  };

  useEffect(() => {
    if (relationshipId) {
      loadData();
    }
  }, [relationshipId]);

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('finance.relationshipFinance')}</Typography.Title>
          <Typography.Text type="secondary">{t('finance.relationshipFinanceSubtitle')}</Typography.Text>
        </div>
        <Space>
          <Button onClick={() => setOpen(true)}>{t('finance.createBook')}</Button>
          <Button type="primary" icon={<PlusOutlined />} disabled={books.length === 0} onClick={() => navigate(`/finance/create?relationshipId=${relationshipId}`)}>
            {t('finance.addTransaction')}
          </Button>
        </Space>
      </div>

      <Row gutter={16}>
        <Col xs={24} md={8}><Card><Statistic title={t('finance.income')} value={summary.totalIncome} precision={2} prefix={<DollarOutlined />} /></Card></Col>
        <Col xs={24} md={8}><Card><Statistic title={t('finance.expense')} value={summary.totalExpense} precision={2} prefix={<DollarOutlined />} /></Card></Col>
        <Col xs={24} md={8}><Card><Statistic title={t('finance.balance')} value={summary.balance} precision={2} prefix={<DollarOutlined />} /></Card></Col>
      </Row>

      <Card title={t('finance.transactions')}>
        <Table
          rowKey="id"
          dataSource={items}
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
