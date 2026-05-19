import {
  CalendarOutlined,
  CheckCircleOutlined,
  CheckSquareOutlined,
  DeleteOutlined,
  DollarOutlined,
  EditOutlined,
  HeartOutlined,
  PlusOutlined,
  ReadOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import {
  Badge,
  Button,
  Calendar as AntCalendar,
  Card,
  Checkbox,
  DatePicker,
  Drawer,
  Empty,
  Form,
  Grid,
  Input,
  InputNumber,
  List,
  message,
  Modal,
  Popconfirm,
  Select,
  Space,
  Tag,
  Typography,
} from 'antd';
import type { CalendarProps } from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import {
  CalendarDay,
  CalendarDayItem,
  CalendarEventType,
  CalendarRepeatType,
  createCalendarEvent,
  deleteCalendarEvent,
  getCalendarMonth,
  updateCalendarEvent,
} from '../../api/calendar';
import { getRelationships, RelationshipSummary } from '../../api/relationship';
import { createSpaceTodo, TodoPriority } from '../../api/spaceTodo';
import { ErrorState } from '../../components/common/ErrorState';
import { formatDateTime } from '../../utils/date';
import { getPageErrorType, PageErrorType } from '../../utils/error';

interface EventFormValues {
  title: string;
  description?: string;
  eventType: CalendarEventType;
  startTime: Dayjs;
  endTime?: Dayjs;
  allDay?: boolean;
  repeatType: CalendarRepeatType;
  reminderMinutes?: number;
  color?: string;
}

interface TodoFormValues {
  title: string;
  content?: string;
  priority: TodoPriority;
  dueTime?: Dayjs;
}

const typeColorMap: Record<string, string> = {
  TODO: 'blue',
  TODO_DONE: 'green',
  ANNIVERSARY: 'magenta',
  DAILY_POST: 'cyan',
  TRANSACTION: 'gold',
  HOLIDAY: 'volcano',
  SOLAR_TERM: 'geekblue',
  CUSTOM_EVENT: 'purple',
};

export function CalendarPage() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const params = useParams();
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.md;
  const relationshipId = Number(params.relationshipId);
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [currentRelationshipId, setCurrentRelationshipId] = useState<number>(relationshipId);
  const [calendarValue, setCalendarValue] = useState<Dayjs>(dayjs());
  const [monthData, setMonthData] = useState<CalendarDay[]>([]);
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs());
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [eventModalOpen, setEventModalOpen] = useState(false);
  const [todoModalOpen, setTodoModalOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<CalendarDayItem | null>(null);
  const [loading, setLoading] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [eventForm] = Form.useForm<EventFormValues>();
  const [todoForm] = Form.useForm<TodoFormValues>();
  const [messageApi, contextHolder] = message.useMessage();

  const locale = i18n.resolvedLanguage === 'en-US' ? 'en-US' : 'zh-CN';

  const dayMap = useMemo(() => {
    const map = new Map<string, CalendarDay>();
    monthData.forEach((day) => map.set(day.date, day));
    return map;
  }, [monthData]);

  const selectedDay = dayMap.get(selectedDate.format('YYYY-MM-DD'));

  const loadRelationships = async () => {
    const response = await getRelationships();
    const items = response.data.data;
    setRelationships(items);
    if (!currentRelationshipId && items[0]) {
      setCurrentRelationshipId(items[0].id);
      navigate(`/relationships/${items[0].id}/calendar`, { replace: true });
    }
  };

  const loadMonth = async () => {
    if (!currentRelationshipId) {
      return;
    }
    setLoading(true);
    try {
      const response = await getCalendarMonth({
        relationshipId: currentRelationshipId,
        year: calendarValue.year(),
        month: calendarValue.month() + 1,
      });
      setMonthData(response.data.data.days);
      setPageError(null);
    } catch (error) {
      setPageError(getPageErrorType(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRelationships().catch(() => undefined);
  }, []);

  useEffect(() => {
    if (relationshipId && relationshipId !== currentRelationshipId) {
      setCurrentRelationshipId(relationshipId);
    }
  }, [relationshipId]);

  useEffect(() => {
    loadMonth();
  }, [currentRelationshipId, calendarValue.year(), calendarValue.month()]);

  const openCreateEvent = (date = selectedDate) => {
    setEditingEvent(null);
    eventForm.resetFields();
    eventForm.setFieldsValue({
      eventType: 'CUSTOM',
      repeatType: 'NONE',
      allDay: false,
      startTime: date.hour(9).minute(0).second(0),
      endTime: undefined,
    });
    setEventModalOpen(true);
  };

  const openEditEvent = (item: CalendarDayItem) => {
    setEditingEvent(item);
    eventForm.setFieldsValue({
      title: item.title,
      description: item.description,
      eventType: (item.metadata?.eventType as CalendarEventType) || 'CUSTOM',
      repeatType: (item.metadata?.repeatType as CalendarRepeatType) || 'NONE',
      allDay: Boolean(item.allDay),
      startTime: item.startTime ? dayjs(item.startTime) : dayjs(item.date),
      endTime: item.endTime ? dayjs(item.endTime) : undefined,
      reminderMinutes: item.metadata?.reminderMinutes as number | undefined,
      color: item.color,
    });
    setEventModalOpen(true);
  };

  const openCreateTodo = (date = selectedDate) => {
    todoForm.resetFields();
    todoForm.setFieldsValue({
      priority: 'NORMAL',
      dueTime: date.hour(9).minute(0).second(0),
    });
    setTodoModalOpen(true);
  };

  const handleRelationshipChange = (id: number) => {
    setCurrentRelationshipId(id);
    navigate(`/relationships/${id}/calendar`);
  };

  const handleDateSelect = (date: Dayjs) => {
    setSelectedDate(date);
    setDrawerOpen(true);
  };

  const handleToday = () => {
    const today = dayjs();
    setCalendarValue(today);
    setSelectedDate(today);
    setDrawerOpen(true);
  };

  const handleSaveEvent = async () => {
    const values = await eventForm.validateFields();
    const payload = {
      relationshipId: currentRelationshipId,
      title: values.title.trim(),
      description: values.description,
      eventType: values.eventType,
      startTime: values.startTime.format('YYYY-MM-DDTHH:mm:ss'),
      endTime: values.endTime ? values.endTime.format('YYYY-MM-DDTHH:mm:ss') : undefined,
      allDay: Boolean(values.allDay),
      repeatType: values.repeatType,
      reminderMinutes: values.reminderMinutes,
      color: values.color,
    };
    try {
      if (editingEvent) {
        await updateCalendarEvent(editingEvent.id, payload);
        messageApi.success(t('calendar.updateSuccess'));
      } else {
        await createCalendarEvent(payload);
        messageApi.success(t('calendar.createSuccess'));
      }
      setEventModalOpen(false);
      await loadMonth();
    } catch (error) {
      messageApi.error(t('common.failed'));
    }
  };

  const handleDeleteEvent = async () => {
    if (!editingEvent) {
      return;
    }
    try {
      await deleteCalendarEvent(editingEvent.id);
      messageApi.success(t('calendar.deleteSuccess'));
      setEventModalOpen(false);
      await loadMonth();
    } catch (error) {
      messageApi.error(t('common.failed'));
    }
  };

  const handleSaveTodo = async () => {
    const values = await todoForm.validateFields();
    try {
      await createSpaceTodo(currentRelationshipId, {
        title: values.title.trim(),
        content: values.content,
        priority: values.priority,
        dueTime: values.dueTime ? values.dueTime.format('YYYY-MM-DDTHH:mm:ss') : undefined,
      });
      messageApi.success(t('todo.createSuccess'));
      setTodoModalOpen(false);
      await loadMonth();
    } catch (error) {
      messageApi.error(t('todo.saveFailed'));
    }
  };

  const getDisplayTitle = (item: CalendarDayItem) => {
    if ((item.type === 'HOLIDAY' || item.type === 'SOLAR_TERM') && locale === 'en-US') {
      return (item.metadata?.nameEn as string) || item.title;
    }
    return item.title;
  };

  const getItemTypeLabel = (item: CalendarDayItem) => {
    if (item.type === 'SOLAR_TERM') return t('calendar.solarTerm');
    if (item.type === 'HOLIDAY') {
      const holidayType = item.metadata?.holidayType;
      if (holidayType === 'LEGAL_HOLIDAY') return t('calendar.legalHoliday');
      if (holidayType === 'WORKDAY') return t('calendar.workday');
      return t('calendar.festival');
    }
    return t(`calendar.itemTypes.${item.type}`);
  };

  const handleItemClick = (item: CalendarDayItem) => {
    if (item.type === 'TODO' || item.type === 'TODO_DONE') {
      navigate(`/relationships/${currentRelationshipId}/todos`);
    } else if (item.type === 'ANNIVERSARY' && item.targetId) {
      navigate(`/anniversaries/${item.targetId}`);
    } else if (item.type === 'DAILY_POST' && item.targetId) {
      navigate(`/daily/${item.targetId}`);
    } else if (item.type === 'TRANSACTION') {
      navigate('/finance/transactions');
    } else if (item.type === 'CUSTOM_EVENT') {
      openEditEvent(item);
    }
  };

  const formatMoney = (value?: number) => {
    return new Intl.NumberFormat(locale, { style: 'currency', currency: 'CNY' }).format(value || 0);
  };

  const renderDateCell = (date: Dayjs) => {
    const key = date.format('YYYY-MM-DD');
    const day = dayMap.get(key);
    const inCurrentMonth = date.month() === calendarValue.month();
    const holidayItems = (day?.items || []).filter((item) => item.type === 'HOLIDAY' || item.type === 'SOLAR_TERM').slice(0, 2);
    const customItems = (day?.items || []).filter((item) => item.type === 'CUSTOM_EVENT').slice(0, 2);
    const anniversaryItems = (day?.items || []).filter((item) => item.type === 'ANNIVERSARY').slice(0, 1);
    const summaryCount = (day?.items?.length || 0) - holidayItems.length - customItems.length - anniversaryItems.length;

    return (
      <div className={`calendar-cell ${inCurrentMonth ? '' : 'calendar-cell-muted'} ${day?.isToday ? 'calendar-cell-today' : ''}`}>
        <div className="calendar-cell-date">
          <span>{date.date()}</span>
          {day?.isToday && <Tag color="blue">{t('calendar.today')}</Tag>}
        </div>
        <div className="calendar-cell-content">
          {holidayItems.map((item) => (
            <Tag key={`${item.type}-${item.id}`} color={item.type === 'SOLAR_TERM' ? 'geekblue' : 'volcano'}>{getDisplayTitle(item)}</Tag>
          ))}
          {day && day.todoCount > 0 && <Badge color="#1677ff" text={t('calendar.todos', { count: day.todoCount })} />}
          {day && day.doneTodoCount > 0 && <Badge color="#52c41a" text={t('calendar.doneTodos', { count: day.doneTodoCount })} />}
          {anniversaryItems.map((item) => (
            <Badge key={`anniversary-${item.id}`} color="#eb2f96" text={getDisplayTitle(item)} />
          ))}
          {day && day.dailyPostCount > 0 && <Badge color="#13c2c2" text={t('calendar.dailyPosts', { count: day.dailyPostCount })} />}
          {day && day.expenseAmount > 0 && <Badge color="#fa8c16" text={`${t('calendar.expense')} ${formatMoney(day.expenseAmount)}`} />}
          {day && day.incomeAmount > 0 && <Badge color="#52c41a" text={`${t('calendar.income')} ${formatMoney(day.incomeAmount)}`} />}
          {customItems.map((item) => (
            <Badge key={`event-${item.id}`} color={item.color || '#722ed1'} text={getDisplayTitle(item)} />
          ))}
          {summaryCount > 0 && <Typography.Text type="secondary" className="calendar-more">{t('calendar.more')}</Typography.Text>}
        </div>
      </div>
    );
  };

  const cellRender: CalendarProps<Dayjs>['cellRender'] = (current, info) => {
    if (info.type !== 'date') {
      return info.originNode;
    }
    return renderDateCell(current);
  };

  const renderItemIcon = (item: CalendarDayItem) => {
    if (item.type === 'TODO') return <CheckSquareOutlined />;
    if (item.type === 'TODO_DONE') return <CheckCircleOutlined />;
    if (item.type === 'ANNIVERSARY') return <HeartOutlined />;
    if (item.type === 'DAILY_POST') return <ReadOutlined />;
    if (item.type === 'TRANSACTION') return <DollarOutlined />;
    return <CalendarOutlined />;
  };

  return (
    <Space direction="vertical" size={16} className="page-wide calendar-page">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('calendar.title')}</Typography.Title>
          <Typography.Text type="secondary">{t('calendar.subtitle')}</Typography.Text>
        </div>
        <Space wrap className="calendar-toolbar">
          <Select
            className="relationship-filter"
            value={currentRelationshipId}
            onChange={handleRelationshipChange}
            options={relationships.map((item) => ({ value: item.id, label: item.name }))}
          />
          <DatePicker
            picker="month"
            allowClear={false}
            value={calendarValue}
            onChange={(value) => value && setCalendarValue(value)}
          />
          <Button onClick={handleToday}>{t('calendar.today')}</Button>
          <Button icon={<ReloadOutlined />} onClick={loadMonth} loading={loading}>{t('common.refresh')}</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => openCreateEvent()}>
            {t('calendar.addEvent')}
          </Button>
        </Space>
      </div>

      <Card className="calendar-card">
        {pageError ? (
          <ErrorState type={pageError} onRetry={loadMonth} />
        ) : (
          <AntCalendar
            value={calendarValue}
            fullscreen={!isMobile}
            headerRender={() => null}
            cellRender={cellRender}
            onSelect={handleDateSelect}
            onPanelChange={(value) => setCalendarValue(value)}
          />
        )}
      </Card>

      <Drawer
        title={`${t('calendar.dayDetail')} · ${selectedDate.format('YYYY-MM-DD')}`}
        width={520}
        height="86vh"
        placement={isMobile ? 'bottom' : 'right'}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={
          <Space>
            <Button size="small" icon={<CheckSquareOutlined />} onClick={() => openCreateTodo(selectedDate)}>
              {t('calendar.quickAddTodo')}
            </Button>
            <Button size="small" type="primary" icon={<PlusOutlined />} onClick={() => openCreateEvent(selectedDate)}>
              {t('calendar.addEvent')}
            </Button>
          </Space>
        }
      >
        {selectedDay?.items.length ? (
          <List
            className="calendar-day-list"
            dataSource={selectedDay.items}
            renderItem={(item) => (
              <List.Item onClick={() => handleItemClick(item)} className={`calendar-day-item ${item.type === 'HOLIDAY' || item.type === 'SOLAR_TERM' ? '' : 'calendar-day-item-clickable'}`}>
                <List.Item.Meta
                  avatar={<span className="calendar-day-item-icon">{renderItemIcon(item)}</span>}
                  title={
                    <Space wrap>
                      <Typography.Text strong>{getDisplayTitle(item)}</Typography.Text>
                      <Tag color={typeColorMap[item.type]}>{getItemTypeLabel(item)}</Tag>
                      {item.status && <Tag>{item.status}</Tag>}
                    </Space>
                  }
                  description={
                    <Space direction="vertical" size={2}>
                      {item.description && <Typography.Text type="secondary">{item.description}</Typography.Text>}
                      <Typography.Text type="secondary">
                        {item.allDay ? t('calendar.allDay') : formatDateTime(item.startTime, t, i18n.resolvedLanguage)}
                        {item.endTime ? ` - ${formatDateTime(item.endTime, t, i18n.resolvedLanguage)}` : ''}
                      </Typography.Text>
                      {item.type === 'TRANSACTION' && (
                        <Typography.Text type="secondary">
                          {item.incomeAmount ? `${t('calendar.income')} ${formatMoney(item.incomeAmount)}` : `${t('calendar.expense')} ${formatMoney(item.expenseAmount)}`}
                        </Typography.Text>
                      )}
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        ) : (
          <Empty description={t('calendar.noItems')} />
        )}
      </Drawer>

      <Modal
        title={editingEvent ? t('calendar.editEvent') : t('calendar.addEvent')}
        open={eventModalOpen}
        onOk={handleSaveEvent}
        onCancel={() => setEventModalOpen(false)}
        okText={t('common.save')}
        cancelText={t('common.cancel')}
        footer={(_, { OkBtn, CancelBtn }) => (
          <Space className="calendar-modal-footer">
            {editingEvent && (
              <Popconfirm title={t('calendar.confirmDeleteEvent')} onConfirm={handleDeleteEvent} okText={t('common.confirm')} cancelText={t('common.cancel')}>
                <Button danger icon={<DeleteOutlined />}>{t('calendar.deleteEvent')}</Button>
              </Popconfirm>
            )}
            <CancelBtn />
            <OkBtn />
          </Space>
        )}
      >
        <Form form={eventForm} layout="vertical">
          <Form.Item name="title" label={t('calendar.eventTitle')} rules={[{ required: true, message: t('validation.required', { field: t('calendar.eventTitle') }) }, { max: 100, message: t('validation.maxLength', { field: t('calendar.eventTitle'), max: 100 }) }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label={t('calendar.eventDescription')}>
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="eventType" label={t('calendar.eventType')} initialValue="CUSTOM">
            <Select
              options={['CUSTOM', 'REMINDER', 'PLAN', 'APPOINTMENT', 'OTHER'].map((value) => ({ value, label: t(`calendar.eventTypes.${value}`) }))}
            />
          </Form.Item>
          <Form.Item name="startTime" label={t('calendar.startTime')} rules={[{ required: true, message: t('validation.required', { field: t('calendar.startTime') }) }]}>
            <DatePicker showTime className="full-width" />
          </Form.Item>
          <Form.Item name="endTime" label={t('calendar.endTime')}>
            <DatePicker showTime className="full-width" />
          </Form.Item>
          <Form.Item name="allDay" valuePropName="checked">
            <Checkbox>{t('calendar.allDay')}</Checkbox>
          </Form.Item>
          <Form.Item name="repeatType" label={t('calendar.repeatType')} initialValue="NONE">
            <Select
              options={['NONE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'].map((value) => ({ value, label: t(`calendar.repeatTypes.${value}`) }))}
            />
          </Form.Item>
          <Form.Item name="reminderMinutes" label={t('calendar.reminder')}>
            <InputNumber min={0} max={10080} className="full-width" addonAfter={t('calendar.minutes')} />
          </Form.Item>
          <Form.Item name="color" label={t('calendar.color')}>
            <Select
              allowClear
              options={['#722ed1', '#1677ff', '#13c2c2', '#52c41a', '#fa8c16', '#eb2f96'].map((value) => ({
                value,
                label: <span className="calendar-color-option"><span style={{ background: value }} />{value}</span>,
              }))}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={t('calendar.quickAddTodo')}
        open={todoModalOpen}
        onOk={handleSaveTodo}
        onCancel={() => setTodoModalOpen(false)}
        okText={t('common.save')}
        cancelText={t('common.cancel')}
      >
        <Form form={todoForm} layout="vertical">
          <Form.Item name="title" label={t('todo.titleField')} rules={[{ required: true, message: t('todo.titleRequired') }, { max: 100, message: t('todo.titleLength') }]}>
            <Input placeholder={t('todo.titlePlaceholder')} />
          </Form.Item>
          <Form.Item name="content" label={t('todo.contentField')}>
            <Input.TextArea rows={3} placeholder={t('todo.contentPlaceholder')} />
          </Form.Item>
          <Form.Item name="priority" label={t('todo.priority')} initialValue="NORMAL">
            <Select
              options={[
                { value: 'LOW', label: t('enum.todoPriority.LOW') },
                { value: 'NORMAL', label: t('enum.todoPriority.NORMAL') },
                { value: 'HIGH', label: t('enum.todoPriority.HIGH') },
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
