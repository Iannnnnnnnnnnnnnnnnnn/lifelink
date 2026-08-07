import { CloudUploadOutlined, DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Col, Descriptions, Drawer, Empty, Form, Input, Modal, Pagination, Popconfirm, Row, Select, Space, Spin, Statistic, Table, Tag, Typography, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { Navigate, useParams } from 'react-router-dom';
import {
  createOfferQuestion,
  deleteOfferQuestion,
  getOfferAccess,
  getOfferCategories,
  getOfferQuestionBanks,
  getOfferQuestions,
  getOfferStatistics,
  importOfferQuestions,
  OfferImportResult,
  OfferCategory,
  OfferDifficulty,
  OfferQuestion,
  OfferQuestionRequest,
  OfferQuestionType,
  OfferStatistics,
  previewOfferImport,
  updateOfferQuestion,
} from '../api/offer';
import { ErrorState } from '../components/common/ErrorState';
import { getPageErrorType, PageErrorType } from '../utils/error';

const DEFAULT_PAGE_SIZE = 20;
const IMPORT_EXAMPLE = `===QUESTION===

type: THEORY
bank: Java
category: Java集合
difficulty: MEDIUM
title: HashMap 为什么线程不安全？
source: GPT

===CONTENT===

HashMap 为什么线程不安全？

===ANSWER===

HashMap 本身没有提供线程同步机制。

\`\`\`java
ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
\`\`\`

===END===`;

const typeOptions: { value: OfferQuestionType; label: string }[] = [
  { value: 'THEORY', label: '理论题' },
  { value: 'ALGORITHM', label: '算法题' },
];

const difficultyOptions: { value: OfferDifficulty; label: string }[] = [
  { value: 'EASY', label: '简单' },
  { value: 'MEDIUM', label: '中等' },
  { value: 'HARD', label: '困难' },
];

export function OfferQuestionBankPage() {
  const { relationshipId: relationshipIdParam } = useParams();
  const relationshipId = Number(relationshipIdParam);
  const [form] = Form.useForm<OfferQuestionRequest>();
  const [statistics, setStatistics] = useState<OfferStatistics>();
  const [banks, setBanks] = useState<{ id: number; name: string; code: string }[]>([]);
  const [categories, setCategories] = useState<OfferCategory[]>([]);
  const [questions, setQuestions] = useState<OfferQuestion[]>([]);
  const [selectedQuestion, setSelectedQuestion] = useState<OfferQuestion>();
  const [canManage, setCanManage] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [bankId, setBankId] = useState<number>();
  const [type, setType] = useState<OfferQuestionType>('THEORY');
  const [categoryId, setCategoryId] = useState<number>();
  const [difficulty, setDifficulty] = useState<OfferDifficulty>();
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [editorOpen, setEditorOpen] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState<OfferQuestion>();
  const [importOpen, setImportOpen] = useState(false);
  const [importContent, setImportContent] = useState('');
  const [importResult, setImportResult] = useState<OfferImportResult>();
  const [importing, setImporting] = useState(false);

  const filteredCategories = useMemo(
    () => categories.filter((item) => item.bankId === bankId && item.type === type),
    [bankId, categories, type],
  );

  const loadQuestions = async () => {
    const response = await getOfferQuestions({ bankId, type, categoryId, difficulty, keyword: keyword || undefined, page, size: DEFAULT_PAGE_SIZE });
    setQuestions(response.data.data.records);
    setTotal(response.data.data.total);
  };

  const loadData = async () => {
    setLoading(true);
    try {
      const [accessResponse, banksResponse, categoriesResponse, statisticsResponse, questionsResponse] = await Promise.all([
        getOfferAccess(),
        getOfferQuestionBanks(),
        getOfferCategories(),
        getOfferStatistics(),
        getOfferQuestions({ bankId, type, categoryId, difficulty, keyword: keyword || undefined, page, size: DEFAULT_PAGE_SIZE }),
      ]);
      setCanManage(accessResponse.data.data.canManage);
      setBanks(banksResponse.data.data);
      setCategories(categoriesResponse.data.data);
      setStatistics(statisticsResponse.data.data);
      setQuestions(questionsResponse.data.data.records);
      setTotal(questionsResponse.data.data.total);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (relationshipId === 1) {
      loadData();
    }
  }, [bankId, type, categoryId, difficulty, keyword, page]);

  useEffect(() => {
    setCategoryId(undefined);
    setPage(1);
  }, [bankId, type]);

  if (relationshipId !== 1) {
    return <Navigate to="/403" replace />;
  }

  const openEditor = (question?: OfferQuestion) => {
    setEditingQuestion(question);
    form.setFieldsValue(question || {
      bankId: bankId || banks[0]?.id,
      type,
      categoryId,
      difficulty: 'MEDIUM',
      title: '',
      content: '',
      answer: '',
      source: '',
      remark: '',
    });
    setEditorOpen(true);
  };

  const saveQuestion = async (values: OfferQuestionRequest) => {
    setSaving(true);
    try {
      if (editingQuestion) {
        await updateOfferQuestion(editingQuestion.id, values);
      } else {
        await createOfferQuestion(values);
      }
      message.success(editingQuestion ? '题目已更新' : '题目已新增');
      setEditorOpen(false);
      setPage(1);
      await Promise.all([loadQuestions(), getOfferStatistics().then((response) => setStatistics(response.data.data))]);
    } catch (error) {
      if (!(error as { __lifelinkHandled?: boolean }).__lifelinkHandled) {
        message.error((error as Error).message);
      }
    } finally {
      setSaving(false);
    }
  };

  const removeQuestion = async (id: number) => {
    await deleteOfferQuestion(id);
    message.success('题目已删除');
    if (questions.length === 1 && page > 1) {
      setPage(page - 1);
    } else {
      await Promise.all([loadQuestions(), getOfferStatistics().then((response) => setStatistics(response.data.data))]);
    }
  };

  const previewImport = async () => {
    if (!importContent.trim()) {
      message.warning('请先粘贴导入文本');
      return;
    }
    setImporting(true);
    try {
      const response = await previewOfferImport(importContent);
      setImportResult(response.data.data);
    } finally {
      setImporting(false);
    }
  };

  const executeImport = async () => {
    if (!importContent.trim()) {
      return;
    }
    setImporting(true);
    try {
      const response = await importOfferQuestions(importContent);
      setImportResult(response.data.data);
      message.success(`导入完成：成功 ${response.data.data.imported} 道`);
      await loadData();
    } finally {
      setImporting(false);
    }
  };

  return (
    <Space direction="vertical" size={16} className="page-wide">
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>Easy Offer 题库</Typography.Title>
          <Typography.Text type="secondary">空间 1 的专属学习题库，仅该空间的有效成员可访问。</Typography.Text>
        </div>
        {canManage && <Space>
          <Button icon={<CloudUploadOutlined />} onClick={() => setImportOpen(true)}>批量导入</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => openEditor()}>新增题目</Button>
        </Space>}
      </div>

      {pageError ? <ErrorState type={pageError} onRetry={loadData} /> : (
        <Spin spinning={loading}>
          <Row gutter={[16, 16]}>
            <Col xs={12} md={6}><Card><Statistic title="总题数" value={statistics?.total ?? 0} /></Card></Col>
            <Col xs={12} md={6}><Card><Statistic title="理论题" value={statistics?.theory ?? 0} /></Card></Col>
            <Col xs={12} md={6}><Card><Statistic title="算法题" value={statistics?.algorithm ?? 0} /></Card></Col>
            <Col xs={12} md={6}><Card><Statistic title="分类数量" value={statistics?.categories ?? 0} /></Card></Col>
          </Row>

          <Card>
            <Space wrap>
              <Select allowClear placeholder="题库" value={bankId} onChange={(value) => { setBankId(value); setPage(1); }} options={banks.map((item) => ({ value: item.id, label: item.name }))} />
              <Select value={type} onChange={(value) => { setType(value); setPage(1); }} options={typeOptions} />
              <Select allowClear placeholder="分类" value={categoryId} onChange={(value) => { setCategoryId(value); setPage(1); }} options={filteredCategories.map((item) => ({ value: item.id, label: item.name }))} />
              <Select allowClear placeholder="难度" value={difficulty} onChange={(value) => { setDifficulty(value); setPage(1); }} options={difficultyOptions} />
              <Input allowClear prefix={<SearchOutlined />} placeholder="搜索题目、正文或答案" value={keyword} onChange={(event) => { setKeyword(event.target.value); setPage(1); }} />
            </Space>
          </Card>

          <Space direction="vertical" size={12} className="full-width">
            {questions.length === 0 ? <Empty description="暂无匹配题目" /> : questions.map((question) => {
              const category = categories.find((item) => item.id === question.categoryId);
              const bank = banks.find((item) => item.id === question.bankId);
              return <Card key={question.id} hoverable onClick={() => setSelectedQuestion(question)}>
                <Space direction="vertical" size={8} className="full-width">
                  <Space wrap>
                    <Typography.Title level={4} className="no-margin">{question.title}</Typography.Title>
                    <Tag color={question.type === 'THEORY' ? 'blue' : 'purple'}>{question.type === 'THEORY' ? '理论题' : '算法题'}</Tag>
                    <Tag>{category?.name || '未分类'}</Tag>
                    <Tag color={question.difficulty === 'HARD' ? 'red' : question.difficulty === 'MEDIUM' ? 'orange' : 'green'}>{difficultyOptions.find((item) => item.value === question.difficulty)?.label}</Tag>
                  </Space>
                  <Typography.Paragraph ellipsis={{ rows: 2 }}>{question.content}</Typography.Paragraph>
                  <Space className="full-width" style={{ justifyContent: 'space-between' }}>
                    <Typography.Text type="secondary">{bank?.name}{question.source ? ` · ${question.source}` : ''}</Typography.Text>
                    {canManage && <Space onClick={(event) => event.stopPropagation()}>
                      <Button type="link" icon={<EditOutlined />} onClick={() => openEditor(question)}>编辑</Button>
                      <Popconfirm title="删除题目" description="删除后无法恢复。" okButtonProps={{ danger: true }} onConfirm={() => removeQuestion(question.id)}>
                        <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
                      </Popconfirm>
                    </Space>}
                  </Space>
                </Space>
              </Card>;
            })}
            {total > DEFAULT_PAGE_SIZE && <Pagination current={page} total={total} pageSize={DEFAULT_PAGE_SIZE} showSizeChanger={false} onChange={setPage} />}
          </Space>
        </Spin>
      )}

      <Drawer title={selectedQuestion?.title} open={Boolean(selectedQuestion)} onClose={() => setSelectedQuestion(undefined)} width={720}>
        {selectedQuestion && <Descriptions column={1} bordered size="small">
          <Descriptions.Item label="题型">{selectedQuestion.type === 'THEORY' ? '理论题' : '算法题'}</Descriptions.Item>
          <Descriptions.Item label="难度">{difficultyOptions.find((item) => item.value === selectedQuestion.difficulty)?.label}</Descriptions.Item>
          <Descriptions.Item label="题目"><Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>{selectedQuestion.content}</Typography.Paragraph></Descriptions.Item>
          <Descriptions.Item label="答案"><Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>{selectedQuestion.answer}</Typography.Paragraph></Descriptions.Item>
          {selectedQuestion.source && <Descriptions.Item label="来源">{selectedQuestion.source}</Descriptions.Item>}
          {selectedQuestion.remark && <Descriptions.Item label="备注">{selectedQuestion.remark}</Descriptions.Item>}
        </Descriptions>}
      </Drawer>

      <Modal title={editingQuestion ? '编辑题目' : '新增题目'} open={editorOpen} width={760} confirmLoading={saving} okText="保存" cancelText="取消" onCancel={() => setEditorOpen(false)} onOk={() => form.submit()}>
        <Form form={form} layout="vertical" onFinish={saveQuestion} onValuesChange={(changed) => {
          if (changed.bankId || changed.type) {
            form.setFieldValue('categoryId', undefined);
          }
        }}>
          <Row gutter={12}>
            <Col span={8}><Form.Item label="题库" name="bankId" rules={[{ required: true, message: '请选择题库' }]}><Select options={banks.map((item) => ({ value: item.id, label: item.name }))} /></Form.Item></Col>
            <Col span={8}><Form.Item label="题型" name="type" rules={[{ required: true }]}><Select options={typeOptions} /></Form.Item></Col>
            <Col span={8}><Form.Item label="难度" name="difficulty" rules={[{ required: true }]}><Select options={difficultyOptions} /></Form.Item></Col>
          </Row>
          <Form.Item noStyle shouldUpdate={(previous, current) => previous.bankId !== current.bankId || previous.type !== current.type}>
            {({ getFieldValue }) => <Form.Item label="分类" name="categoryId" rules={[{ required: true, message: '请选择分类' }]}><Select options={categories.filter((item) => item.bankId === getFieldValue('bankId') && item.type === getFieldValue('type')).map((item) => ({ value: item.id, label: item.name }))} /></Form.Item>}
          </Form.Item>
          <Form.Item label="标题" name="title" rules={[{ required: true, whitespace: true, message: '请输入标题' }]}><Input maxLength={255} /></Form.Item>
          <Form.Item label="题目内容" name="content" rules={[{ required: true, whitespace: true, message: '请输入题目内容' }]}><Input.TextArea rows={5} /></Form.Item>
          <Form.Item label="答案" name="answer" rules={[{ required: true, whitespace: true, message: '请输入答案' }]}><Input.TextArea rows={7} /></Form.Item>
          <Form.Item label="来源" name="source"><Input maxLength={100} /></Form.Item>
          <Form.Item label="备注" name="remark"><Input.TextArea rows={2} maxLength={500} /></Form.Item>
        </Form>
      </Modal>

      <Modal
        title="批量导入题目"
        open={importOpen}
        width={960}
        footer={null}
        onCancel={() => setImportOpen(false)}
      >
        <Space direction="vertical" size={16} className="full-width">
          <Alert
            type="info"
            showIcon
            message="导入规则"
            description="使用明确的题目块格式。bank 可填 Java、Python、前端，省略时默认 Java；不存在的分类会自动创建。预览不会写库，同一题库、同一题型下的同标题题目会跳过。"
          />
          <Card size="small" title="导入文本" extra={<Button type="link" onClick={() => { setImportContent(IMPORT_EXAMPLE); setImportResult(undefined); }}>填入示例</Button>}>
            <Input.TextArea rows={16} value={importContent} placeholder="粘贴题目文本" onChange={(event) => { setImportContent(event.target.value); setImportResult(undefined); }} />
            <Space style={{ marginTop: 12 }}>
              <Button type="primary" loading={importing} onClick={previewImport}>预览并校验</Button>
              <Button icon={<CloudUploadOutlined />} loading={importing} disabled={!importResult || importResult.valid === 0} onClick={executeImport}>确认导入有效题目</Button>
            </Space>
          </Card>
          {importResult && <Card size="small" title="导入预览">
            <Space wrap style={{ marginBottom: 12 }}>
              <Tag>总计 {importResult.total}</Tag>
              <Tag color="blue">有效 {importResult.valid}</Tag>
              <Tag color="orange">重复 {importResult.duplicate}</Tag>
              <Tag color="red">失败 {importResult.invalid}</Tag>
              {importResult.imported > 0 && <Tag color="green">已写入 {importResult.imported}</Tag>}
            </Space>
            <Table
              rowKey="index"
              size="small"
              pagination={false}
              dataSource={importResult.questions}
              columns={[
                { title: '#', dataIndex: 'index', width: 56 },
                { title: '标题', dataIndex: 'title', render: (value) => value || '—' },
                { title: '题库', dataIndex: 'bank', width: 100 },
                { title: '分类', dataIndex: 'category', width: 130 },
                { title: '状态', dataIndex: 'status', width: 120, render: (status) => <Tag color={status === 'INVALID' ? 'red' : status === 'DUPLICATE' ? 'orange' : status === 'IMPORTED' ? 'green' : 'blue'}>{status === 'INVALID' ? '失败' : status === 'DUPLICATE' ? '已存在，跳过' : status === 'IMPORTED' ? '已导入' : '有效'}</Tag> },
                { title: '原因', dataIndex: 'errors', render: (errors?: string[]) => errors?.join('；') || '—' },
              ]}
            />
          </Card>}
        </Space>
      </Modal>
    </Space>
  );
}
