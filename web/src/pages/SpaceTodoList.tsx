import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Checkbox, DatePicker, Form, Input, List, message, Modal, Popconfirm, Select, Space, Tag, Typography } from 'antd';
import type { CheckboxChangeEvent } from 'antd/es/checkbox';
import type { AxiosError } from 'axios';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
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

interface TodoFormValues {
  title: string;
  content?: string;
  priority: TodoPriority;
  dueTime?: Dayjs;
}

export function SpaceTodoList() {
  const { t } = useTranslation();
  const params = useParams();
  const relationshipId = Number(params.relationshipId);
  const [todos, setTodos] = useState<SpaceTodo[]>([]);
  const [status, setStatus] = useState<'ALL' | 'TODO' | 'DONE'>('ALL');
  const [keyword, setKeyword] = useState('');
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<SpaceTodo | null>(null);
  const [loading, setLoading] = useState(false);
  const [togglingIds, setTogglingIds] = useState<number[]>([]);
  const [form] = Form.useForm<TodoFormValues>();
  const [messageApi, contextHolder] = message.useMessage();

  const loadTodos = async () => {
    setLoading(true);
    try {
      const response = await getSpaceTodos(relationshipId, {
        status: status === 'ALL' ? undefined : status,
        keyword: keyword || undefined,
        page: 1,
        size: 50,
      });
      setTodos(response.data.data);
    } catch (error) {
      messageApi.error(t('todo.loadFailed'));
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

  useEffect(() => {
    if (relationshipId) {
      loadTodos();
    }
  }, [relationshipId, status]);

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
            onChange={setStatus}
            options={[
              { value: 'ALL', label: t('todo.statusAll') },
              { value: 'TODO', label: t('todo.statusTodo') },
              { value: 'DONE', label: t('todo.statusDone') },
            ]}
          />
          <Input.Search placeholder={t('todo.searchPlaceholder')} allowClear value={keyword} onChange={(event) => setKeyword(event.target.value)} onSearch={loadTodos} />
          <Button icon={<ReloadOutlined />} loading={loading} onClick={loadTodos}>
            {t('common.refresh')}
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            {t('todo.newTodo')}
          </Button>
        </Space>
      </div>

      <Card className="todo-list-card">
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
                    <Tag color={todo.priority === 'HIGH' ? 'red' : todo.priority === 'LOW' ? 'default' : 'blue'}>{todo.priority}</Tag>
                    <Tag color={todo.status === 'DONE' ? 'green' : 'gold'}>{todo.status}</Tag>
                  </Space>
                }
                description={
                  <Space direction="vertical" size={4}>
                    <Typography.Text type="secondary">{todo.content || t('todo.noContent')}</Typography.Text>
                    <Typography.Text type="secondary">
                      {todo.createdByUsername || '-'} · {t('common.due')}: {todo.dueTime || '-'}
                    </Typography.Text>
                  </Space>
                }
              />
            </List.Item>
          )}
        />
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
                { value: 'LOW', label: 'LOW' },
                { value: 'NORMAL', label: 'NORMAL' },
                { value: 'HIGH', label: 'HIGH' },
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
