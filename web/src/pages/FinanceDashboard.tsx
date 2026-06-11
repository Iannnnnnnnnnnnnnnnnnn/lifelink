import { DollarOutlined, PlusOutlined, ReloadOutlined, UnorderedListOutlined } from '@ant-design/icons';
import { Button, Card, Col, Form, Input, message, Modal, Row, Segmented, Select, Space, Statistic, Table, Tag, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  AccountBook,
  createAccountBook,
  getAccountBooks,
  getMonthlyFinanceSummary,
  getTransactions,
  MonthlyFinanceSummary,
  Transaction,
} from '../api/accounting';
import { getRelationships, type RelationshipSummary } from '../api/relationship';
import { formatDateTime } from '../utils/date';
import { getTransactionCategoryLabel, getTransactionTypeLabel } from '../utils/display';

type FinanceScope = 'personal' | 'space';

function getScopeFromParams(searchParams: URLSearchParams): FinanceScope {
  return searchParams.get('scope') === 'space' || searchParams.has('spaceId') || searchParams.has('relationshipId') ? 'space' : 'personal';
}

function getSpaceIdFromParams(searchParams: URLSearchParams) {
  const value = searchParams.get('spaceId') || searchParams.get('relationshipId');
  return value ? Number(value) : undefined;
}

export function FinanceDashboard() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [scope, setScope] = useState<FinanceScope>(() => getScopeFromParams(searchParams));
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [selectedSpaceId, setSelectedSpaceId] = useState<number | undefined>(() => getSpaceIdFromParams(searchParams));
  const [books, setBooks] = useState<AccountBook[]>([]);
  const [bookId, setBookId] = useState<number | undefined>();
  const [summary, setSummary] = useState<MonthlyFinanceSummary>({ totalIncome: 0, totalExpense: 0, balance: 0 });
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm<{ name: string }>();
  const [messageApi, contextHolder] = message.useMessage();

  const personalBooks = useMemo(() => books.filter((book) => book.type === 'PERSONAL'), [books]);
  const spaceBooks = useMemo(
    () => books.filter((book) => book.type === 'RELATIONSHIP' && (!selectedSpaceId || book.relationshipId === selectedSpaceId)),
    [books, selectedSpaceId],
  );
  const currentBooks = scope === 'space' ? spaceBooks : personalBooks;
  const selectedSpaceName = relationships.find((item) => item.id === selectedSpaceId)?.name;
  const addTransactionText = scope === 'space' ? t('finance.addSpaceTransaction') : t('finance.addPersonalTransaction');
  const pageTitle = scope === 'space' ? t('finance.spaceLedger') : t('finance.personalLedger');
  const pageSubtitle = scope === 'space' ? t('finance.spaceFinanceUnifiedSubtitle') : t('finance.personalFinanceSubtitle');

  const updateScopeQuery = (nextScope: FinanceScope, nextSpaceId?: number) => {
    const nextParams = new URLSearchParams();
    if (nextScope === 'space') {
      nextParams.set('scope', 'space');
      if (nextSpaceId) {
        nextParams.set('spaceId', String(nextSpaceId));
      }
    }
    setSearchParams(nextParams, { replace: true });
  };

  const buildScopedPath = (path: string) => {
    const params = new URLSearchParams();
    if (scope === 'space') {
      params.set('scope', 'space');
      if (selectedSpaceId) {
        params.set('spaceId', String(selectedSpaceId));
      }
    }
    if (bookId) {
      params.set('accountBookId', String(bookId));
    }
    const query = params.toString();
    return query ? `${path}?${query}` : path;
  };

  const loadReferenceData = async () => {
    const [bookRes, relationshipRes] = await Promise.all([getAccountBooks(), getRelationships()]);
    setBooks(bookRes.data.data);
    setRelationships(relationshipRes.data.data);
  };

  const loadData = async () => {
    const query: { accountBookId?: number; relationshipId?: number; page?: number; size?: number } = { page: 1, size: 8 };
    if (bookId) {
      query.accountBookId = bookId;
    } else if (scope === 'space' && selectedSpaceId) {
      query.relationshipId = selectedSpaceId;
    } else if (scope === 'personal') {
      setSummary({ totalIncome: 0, totalExpense: 0, balance: 0 });
      setTransactions([]);
      return;
    }

    setLoading(true);
    try {
      const [summaryRes, transactionRes] = await Promise.all([
        getMonthlyFinanceSummary({ accountBookId: query.accountBookId, relationshipId: query.relationshipId }),
        getTransactions(query),
      ]);
      setSummary(summaryRes.data.data);
      setTransactions(transactionRes.data.data);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = async () => {
    setLoading(true);
    try {
      await loadReferenceData();
      await loadData();
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBook = async () => {
    const values = await form.validateFields();
    const response = await createAccountBook({
      name: values.name,
      type: scope === 'space' ? 'RELATIONSHIP' : 'PERSONAL',
      relationshipId: scope === 'space' ? selectedSpaceId : undefined,
    });
    messageApi.success(t('finance.bookCreated'));
    setOpen(false);
    form.resetFields();
    setBookId(response.data.data.id);
    await loadReferenceData();
  };

  useEffect(() => {
    setScope(getScopeFromParams(searchParams));
    setSelectedSpaceId(getSpaceIdFromParams(searchParams));
    setBookId(searchParams.get('accountBookId') ? Number(searchParams.get('accountBookId')) : undefined);
  }, [searchParams]);

  useEffect(() => {
    loadReferenceData().catch(() => undefined);
  }, []);

  useEffect(() => {
    if (scope === 'personal') {
      if (!personalBooks.some((book) => book.id === bookId)) {
        setBookId(personalBooks[0]?.id);
      }
      return;
    }
    const fallbackSpaceId = selectedSpaceId
      || relationships[0]?.id
      || books.find((book) => book.type === 'RELATIONSHIP' && book.relationshipId)?.relationshipId;
    if (fallbackSpaceId && !selectedSpaceId) {
      setSelectedSpaceId(fallbackSpaceId);
    }
    if (bookId && !spaceBooks.some((book) => book.id === bookId)) {
      setBookId(undefined);
    }
  }, [bookId, books, personalBooks, relationships, scope, selectedSpaceId, spaceBooks]);

  useEffect(() => {
    loadData().catch(() => undefined);
  }, [scope, selectedSpaceId, bookId]);

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{pageTitle}</Typography.Title>
          <Typography.Text type="secondary">{pageSubtitle}</Typography.Text>
        </div>
        <Space wrap>
          <Segmented
            value={scope}
            options={[
              { value: 'personal', label: t('finance.personalLedger') },
              { value: 'space', label: t('finance.spaceLedger') },
            ]}
            onChange={(value) => {
              const nextScope = value as FinanceScope;
              setScope(nextScope);
              setBookId(undefined);
              updateScopeQuery(nextScope, selectedSpaceId);
            }}
          />
          {scope === 'space' && (
            <Select
              className="relationship-filter"
              placeholder={t('finance.selectSpace')}
              value={selectedSpaceId}
              options={relationships.map((item) => ({ value: item.id, label: item.name }))}
              onChange={(value) => {
                setSelectedSpaceId(value);
                setBookId(undefined);
                updateScopeQuery('space', value);
              }}
            />
          )}
          <Select
            allowClear={scope === 'space'}
            className="relationship-filter"
            placeholder={t('finance.selectBook')}
            value={bookId}
            options={currentBooks.map((book) => ({ value: book.id, label: book.name }))}
            onChange={setBookId}
          />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={handleRefresh}>{t('common.refresh')}</Button>
          <Button icon={<UnorderedListOutlined />} onClick={() => navigate(buildScopedPath('/finance/transactions'))}>{t('finance.transactions')}</Button>
          <Button disabled={scope === 'space' && !selectedSpaceId} onClick={() => setOpen(true)}>{t('finance.createBook')}</Button>
          <Button type="primary" icon={<PlusOutlined />} disabled={currentBooks.length === 0} onClick={() => navigate(buildScopedPath('/finance/create'))}>
            {addTransactionText}
          </Button>
        </Space>
      </div>

      <Card className="finance-scope-card">
        <Space wrap>
          <Tag color={scope === 'space' ? 'blue' : 'default'}>{t('finance.currentScope')}: {pageTitle}</Tag>
          {scope === 'space' && <Tag>{selectedSpaceName || t('finance.selectSpace')}</Tag>}
          {currentBooks.length === 0 && <Typography.Text type="secondary">{scope === 'space' ? t('finance.noSpaceBook') : t('finance.noPersonalBook')}</Typography.Text>}
        </Space>
      </Card>

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
          scroll={{ x: 760 }}
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
          <Form.Item label={t('finance.currentScope')}>
            <Tag>{scope === 'space' ? `${t('finance.spaceLedger')}${selectedSpaceName ? ` · ${selectedSpaceName}` : ''}` : t('finance.personalLedger')}</Tag>
          </Form.Item>
          <Form.Item name="name" label={t('finance.bookName')} rules={[{ required: true, message: t('finance.bookNameRequired') }]}>
            <Input placeholder={t('finance.bookName')} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
