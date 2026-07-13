import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Checkbox, DatePicker, Form, Input, List, message, Modal, Popconfirm, Select, Space, Tag, Typography } from 'antd';
import type { CheckboxChangeEvent } from 'antd/es/checkbox';
import type { AxiosError } from 'axios';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams, useSearchParams } from 'react-router-dom';
import {
  createSpaceTodo,
  deleteSpaceTodo,
  getSpaceTodos,
  SpaceTodo,
  TodoPriority,
  toggleSpaceTodo,
  updateSpaceTodo,
} from '../api/spaceTodo';
import type { ApiResult } from '../api/request';
import { EmptyState } from '../components/decorations/EmptyState';
import { ErrorState } from '../components/common/ErrorState';
import { formatDateTime } from '../utils/date';
import { getTodoPriorityColor, getTodoPriorityLabel, getTodoStatusColor, getTodoStatusLabel } from '../utils/display';
import { getPageErrorType, PageErrorType } from '../utils/error';

interface TodoFormValues {
  title: string;
  content?: string;
  priority: TodoPriority;
  dueTime?: Dayjs;
}

function getTodoStatus(searchParams: URLSearchParams): 'ALL' | 'TODO' | 'DONE' {
  const value = searchParams.get('status');
  return value === 'TODO' || value === 'DONE' ? value : 'ALL';
}

export function SpaceTodoList() {
  const { t, i18n } = useTranslation();
  const params = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const relationshipId = Number(params.relationshipId);
  const status = getTodoStatus(searchParams);
  const activeKeyword = searchParams.get('keyword')?.trim() || '';
  const [todos, setTodos] = useState<SpaceTodo[]>([]);
  const [keyword, setKeyword] = useState(activeKeyword);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<SpaceTodo | null>(null);
  const [loading, setLoading] = useState(false);
  const [togglingIds, setTogglingIds] = useState<number[]>([]);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [form] = Form.useForm<TodoFormValues>();
  const [messageApi, contextHolder] = message.useMessage();

  const loadTodos = async () => {
    setLoading(true);
    try {
      const response = await getSpaceTodos(relationshipId, {
        status: status === 'ALL' ? undefined : status,
        keyword: activeKeyword || undefined,
        page: 1,
        size: 50,
      });
      setTodos(response.data.data);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
    } finally {
      setLoading(false);
    }
  };

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ priority: 'NORMAL' });
    setOpen(true);
  };

  const openEdit = (todo: SpaceTodo) => {
    setEditing(todo);
    form.setFieldsValue({
      title: todo.title,
      content: todo.content,
      priority: todo.priority,
      dueTime: todo.dueTime ? dayjs(todo.dueTime) : undefined,
    });
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload = {
      title: values.title,
      content: values.content,
      priority: values.priority,
      dueTime: values.dueTime ? values.dueTime.format('YYYY-MM-DDTHH:mm:ss') : undefined,
    };
    try {
      if (editing) {
        await updateSpaceTodo(relationshipId, editing.id, payload);
        messageApi.success(t('todo.updateSuccess'));
      } else {
        await createSpaceTodo(relationshipId, payload);
        messageApi.success(t('todo.createSuccess'));
      }
      setOpen(false);
      loadTodos();
    } catch (error) {
      messageApi.error(t('todo.saveFailed'));
    }
  };

  const handleToggle = async (todo: SpaceTodo, event?: CheckboxChangeEvent) => {
    event?.nativeEvent.stopPropagation();
    setTogglingIds((ids) => [...ids, todo.id]);
    try {
      const response = await toggleSpaceTodo(relationshipId, todo.id);
      setTodos((items) => items.map((item) => (item.id === todo.id ? response.data.data : item)));
      messageApi.success(t('todo.toggleSuccess'));
    } catch (error) {
      const axiosError = error as AxiosError<ApiResult<unknown>>;
      messageApi.error(axiosError.response?.data?.message || t('todo.toggleFailed'));
    } finally {
      setTogglingIds((ids) => ids.filter((id) => id !== todo.id));
    }
  };

  const handleDelete = async (todo: SpaceTodo) => {
    try {
      await deleteSpaceTodo(relationshipId, todo.id);
      messageApi.success(t('todo.deleteSuccess'));
      loadTodos();
    } catch (error) {
      messageApi.error(t('todo.deleteFailed'));
    }
  };

  const updateSearchParams = (updates: { status?: 'ALL' | 'TODO' | 'DONE'; keyword?: string }) => {
    const nextParams = new URLSearchParams(searchParams);
    if (updates.status !== undefined) {
      if (updates.status === 'ALL') {
        nextParams.delete('status');
      } else {
        nextParams.set('status', updates.status);
      }
    }
    if (updates.keyword !== undefined) {
      const nextKeyword = updates.keyword.trim();
      if (nextKeyword) {
        nextParams.set('keyword', nextKeyword);
      } else {
        nextParams.delete('keyword');
      }
    }
    setSearchParams(nextParams, { replace: true });
  };

  const handleKeywordChange = (value: string) => {
    setKeyword(value);
    if (!value) {
      updateSearchParams({ keyword: '' });
    }
  };

  useEffect(() => {
    if (relationshipId) {
      loadTodos();
    }
  }, [relationshipId, status, activeKeyword]);

  useEffect(() => {
    setKeyword(activeKeyword);
  }, [activeKeyword]);

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('todo.title')}</Typography.Title>
          <Typography.Text type="secondary">{t('todo.subtitle')}</Typography.Text>
        </div>
        <Space wrap className="filter-actions">
          <Select
            value={status}
            className="todo-status-filter"
            onChange={(value) => updateSearchParams({ status: value })}
            options={[
              { value: 'ALL', label: t('todo.statusAll') },
              { value: 'TODO', label: t('todo.statusTodo') },
              { value: 'DONE', label: t('todo.statusDone') },
            ]}
          />
          <Input.Search
            placeholder={t('todo.searchPlaceholder')}
            allowClear
            value={keyword}
            onChange={(event) => handleKeywordChange(event.target.value)}
            onSearch={(value) => updateSearchParams({ keyword: value })}
          />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={loadTodos}>
            {t('common.refresh')}
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            {t('todo.newTodo')}
          </Button>
        </Space>
      </div>

      <Card className="todo-list-card">
        {pageError ? (
          <ErrorState type={pageError} onRetry={loadTodos} />
        ) : (
          <List
            loading={loading}
            dataSource={todos}
            locale={{ emptyText: <EmptyState description={t('todo.empty')} /> }}
            renderItem={(todo) => (
            <List.Item
              className="todo-list-item"
              actions={[
                <Button key="edit" size="small" icon={<EditOutlined />} onClick={() => openEdit(todo)}>
                  {t('common.edit')}
                </Button>,
                <Popconfirm key="delete" title={t('todo.deleteConfirm')} okText={t('common.confirm')} cancelText={t('common.cancel')} onConfirm={() => handleDelete(todo)}>
                  <Button size="small" danger icon={<DeleteOutlined />}>
                    {t('common.delete')}
                  </Button>
                </Popconfirm>,
              ]}
            >
              <List.Item.Meta
                avatar={
                  <Checkbox
                    checked={todo.status === 'DONE'}
                    disabled={togglingIds.includes(todo.id)}
                    onClick={(event) => event.stopPropagation()}
                    onChange={(event) => handleToggle(todo, event)}
                  />
                }
                title={
                  <Space wrap>
                    <Typography.Text strong delete={todo.status === 'DONE'}>
                      {todo.title}
                    </Typography.Text>
                    <Tag color={getTodoPriorityColor(todo.priority)}>{getTodoPriorityLabel(t, todo.priority)}</Tag>
                    <Tag color={getTodoStatusColor(todo.status)}>{getTodoStatusLabel(t, todo.status)}</Tag>
                  </Space>
                }
                description={
                  <Space direction="vertical" size={4}>
                    <Typography.Text type="secondary">{todo.content || t('todo.noContent')}</Typography.Text>
                    <Typography.Text type="secondary">
                      {todo.createdByUsername || t('common.notAvailable')} · {t('common.due')}: {formatDateTime(todo.dueTime, t, i18n.resolvedLanguage)}
                    </Typography.Text>
                  </Space>
                }
              />
            </List.Item>
            )}
          />
        )}
      </Card>

      <Modal title={editing ? t('todo.editTodo') : t('todo.newTodo')} open={open} onOk={handleSubmit} onCancel={() => setOpen(false)} okText={t('common.save')} cancelText={t('common.cancel')}>
        <Form form={form} layout="vertical">
          <Form.Item name="title" label={t('todo.titleField')} rules={[{ required: true, message: t('todo.titleRequired') }, { max: 100, message: t('todo.titleLength') }]}>
            <Input placeholder={t('todo.titlePlaceholder')} />
          </Form.Item>
          <Form.Item name="content" label={t('todo.contentField')}>
            <Input.TextArea rows={4} placeholder={t('todo.contentPlaceholder')} />
          </Form.Item>
          <Form.Item name="priority" label={t('todo.priority')} initialValue="NORMAL">
            <Select
              options={[
                { value: 'LOW', label: getTodoPriorityLabel(t, 'LOW') },
                { value: 'NORMAL', label: getTodoPriorityLabel(t, 'NORMAL') },
                { value: 'HIGH', label: getTodoPriorityLabel(t, 'HIGH') },
              ]}
            />
          </Form.Item>
          <Form.Item name="dueTime" label={t('todo.dueTime')}>
            <DatePicker showTime className="full-width" />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
