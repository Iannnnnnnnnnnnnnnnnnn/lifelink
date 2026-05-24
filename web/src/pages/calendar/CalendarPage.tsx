import { Card, Form, Grid, message, Skeleton } from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import type { CalendarDay, CalendarDayItem } from '../../api/calendar';
import { createCalendarEvent, deleteCalendarEvent, getCalendarMonth, updateCalendarEvent } from '../../api/calendar';
import { getRelationships, type RelationshipSummary } from '../../api/relationship';
import { createSpaceTodo } from '../../api/spaceTodo';
import { CalendarDayDrawer } from '../../components/calendar/CalendarDayDrawer';
import { CalendarEventModal, type CalendarEventFormValues } from '../../components/calendar/CalendarEventModal';
import { CalendarHeader } from '../../components/calendar/CalendarHeader';
import { CalendarMonthGrid } from '../../components/calendar/CalendarMonthGrid';
import { CalendarTodoModal, type CalendarTodoFormValues } from '../../components/calendar/CalendarTodoModal';
import { ErrorState } from '../../components/common/ErrorState';
import { getPageErrorType, type PageErrorType } from '../../utils/error';

export function CalendarPage() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const params = useParams();
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.md;
  const routeRelationshipId = Number(params.relationshipId);
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [currentRelationshipId, setCurrentRelationshipId] = useState<number | undefined>(routeRelationshipId || undefined);
  const [calendarMonth, setCalendarMonth] = useState<Dayjs>(dayjs());
  const [monthData, setMonthData] = useState<CalendarDay[]>([]);
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs());
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [eventModalOpen, setEventModalOpen] = useState(false);
  const [todoModalOpen, setTodoModalOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<CalendarDayItem | null>(null);
  const [loading, setLoading] = useState(false);
  const [pageError, setPageError] = useState<PageErrorType | null>(null);
  const [savingEvent, setSavingEvent] = useState(false);
  const [deletingEvent, setDeletingEvent] = useState(false);
  const [savingTodo, setSavingTodo] = useState(false);
  const [eventForm] = Form.useForm<CalendarEventFormValues>();
  const [todoForm] = Form.useForm<CalendarTodoFormValues>();
  const [messageApi, contextHolder] = message.useMessage();

  const language = i18n.resolvedLanguage;

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
        year: calendarMonth.year(),
        month: calendarMonth.month() + 1,
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
    if (routeRelationshipId && routeRelationshipId !== currentRelationshipId) {
      setCurrentRelationshipId(routeRelationshipId);
    }
  }, [routeRelationshipId]);

  useEffect(() => {
    loadMonth();
  }, [currentRelationshipId, calendarMonth.year(), calendarMonth.month()]);

  const handleRelationshipChange = (id: number) => {
    setCurrentRelationshipId(id);
    navigate(`/relationships/${id}/calendar`);
  };

  const handleSelectDate = (date: Dayjs) => {
    setSelectedDate(date);
    if (!date.isSame(calendarMonth, 'month')) {
      setCalendarMonth(date.startOf('month'));
    }
    setDrawerOpen(true);
  };

  const handleToday = () => {
    const today = dayjs();
    setCalendarMonth(today.startOf('month'));
    setSelectedDate(today);
    setDrawerOpen(true);
  };

  const openCreateEvent = (date = selectedDate) => {
    setEditingEvent(null);
    eventForm.resetFields();
    eventForm.setFieldsValue({
      eventType: 'CUSTOM',
      repeatType: 'NONE',
      allDay: false,
      startTime: date.hour(9).minute(0).second(0),
      endTime: undefined,
      color: '#7487a8',
    });
    setEventModalOpen(true);
  };

  const openEditEvent = (item: CalendarDayItem) => {
    setEditingEvent(item);
    eventForm.setFieldsValue({
      title: item.title,
      description: item.description,
      eventType: (item.metadata?.eventType as CalendarEventFormValues['eventType']) || 'CUSTOM',
      repeatType: (item.metadata?.repeatType as CalendarEventFormValues['repeatType']) || 'NONE',
      allDay: Boolean(item.allDay),
      startTime: item.startTime ? dayjs(item.startTime) : dayjs(item.date),
      endTime: item.endTime ? dayjs(item.endTime) : undefined,
      reminderMinutes: item.metadata?.reminderMinutes as number | undefined,
      color: item.color || '#7487a8',
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

  const handleSaveEvent = async () => {
    if (!currentRelationshipId) {
      return;
    }
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
    setSavingEvent(true);
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
      messageApi.error(t('message.operationFailed'));
    } finally {
      setSavingEvent(false);
    }
  };

  const handleDeleteEvent = async () => {
    if (!editingEvent) {
      return;
    }
    setDeletingEvent(true);
    try {
      await deleteCalendarEvent(editingEvent.id);
      messageApi.success(t('calendar.deleteSuccess'));
      setEventModalOpen(false);
      await loadMonth();
    } catch (error) {
      messageApi.error(t('message.operationFailed'));
    } finally {
      setDeletingEvent(false);
    }
  };

  const handleSaveTodo = async () => {
    if (!currentRelationshipId) {
      return;
    }
    const values = await todoForm.validateFields();
    setSavingTodo(true);
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
    } finally {
      setSavingTodo(false);
    }
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

  return (
    <div className="page-wide calendar-page">
      {contextHolder}
      <CalendarHeader
        relationships={relationships}
        relationshipId={currentRelationshipId}
        month={calendarMonth}
        loading={loading}
        isMobile={isMobile}
        language={language}
        t={t}
        onRelationshipChange={handleRelationshipChange}
        onToday={handleToday}
        onPreviousMonth={() => setCalendarMonth((value) => value.subtract(1, 'month'))}
        onNextMonth={() => setCalendarMonth((value) => value.add(1, 'month'))}
        onRefresh={loadMonth}
        onCreateEvent={() => openCreateEvent()}
      />

      <Card className="calendar-shell">
        {pageError ? (
          <ErrorState type={pageError} onRetry={loadMonth} />
        ) : loading && monthData.length === 0 ? (
          <Skeleton active paragraph={{ rows: 16 }} />
        ) : (
          <CalendarMonthGrid
            month={calendarMonth}
            selectedDate={selectedDate}
            dayMap={dayMap}
            loading={loading}
            language={language}
            isMobile={isMobile}
            t={t}
            onSelectDate={handleSelectDate}
            onCreateEvent={openCreateEvent}
          />
        )}
      </Card>

      <CalendarDayDrawer
        open={drawerOpen}
        day={selectedDay}
        date={selectedDate}
        isMobile={isMobile}
        language={language}
        t={t}
        onClose={() => setDrawerOpen(false)}
        onCreateEvent={openCreateEvent}
        onCreateTodo={openCreateTodo}
        onItemClick={handleItemClick}
      />

      <CalendarEventModal
        open={eventModalOpen}
        editingEvent={editingEvent}
        form={eventForm}
        saving={savingEvent}
        deleting={deletingEvent}
        t={t}
        onCancel={() => setEventModalOpen(false)}
        onSubmit={handleSaveEvent}
        onDelete={handleDeleteEvent}
      />

      <CalendarTodoModal
        open={todoModalOpen}
        form={todoForm}
        saving={savingTodo}
        t={t}
        onCancel={() => setTodoModalOpen(false)}
        onSubmit={handleSaveTodo}
      />
    </div>
  );
}
