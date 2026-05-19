import { DeleteOutlined } from '@ant-design/icons';
import { Button, Checkbox, DatePicker, Form, Input, InputNumber, Modal, Popconfirm, Select, Space } from 'antd';
import type { FormInstance } from 'antd';
import type { TFunction } from 'i18next';
import type { Dayjs } from 'dayjs';
import type { CalendarDayItem, CalendarEventType, CalendarRepeatType } from '../../api/calendar';
import { calendarEventColorOptions } from './calendarTheme';

export interface CalendarEventFormValues {
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

interface CalendarEventModalProps {
  open: boolean;
  editingEvent: CalendarDayItem | null;
  form: FormInstance<CalendarEventFormValues>;
  saving?: boolean;
  deleting?: boolean;
  t: TFunction;
  onCancel: () => void;
  onSubmit: () => void;
  onDelete: () => void;
}

export function CalendarEventModal({
  open,
  editingEvent,
  form,
  saving,
  deleting,
  t,
  onCancel,
  onSubmit,
  onDelete,
}: CalendarEventModalProps) {
  return (
    <Modal
      className="calendar-event-modal"
      title={editingEvent ? t('calendar.editEvent') : t('calendar.addEvent')}
      open={open}
      onOk={onSubmit}
      onCancel={onCancel}
      confirmLoading={saving}
      okText={t('common.save')}
      cancelText={t('common.cancel')}
      footer={(_, { OkBtn, CancelBtn }) => (
        <Space className="calendar-modal-footer">
          {editingEvent && (
            <Popconfirm title={t('calendar.confirmDeleteEvent')} onConfirm={onDelete} okText={t('common.confirm')} cancelText={t('common.cancel')}>
              <Button danger icon={<DeleteOutlined />} loading={deleting}>{t('calendar.deleteEvent')}</Button>
            </Popconfirm>
          )}
          <CancelBtn />
          <OkBtn />
        </Space>
      )}
    >
      <Form form={form} layout="vertical" className="calendar-soft-form">
        <Form.Item name="title" label={t('calendar.eventTitle')} rules={[{ required: true, message: t('validation.required', { field: t('calendar.eventTitle') }) }, { max: 100, message: t('validation.maxLength', { field: t('calendar.eventTitle'), max: 100 }) }]}>
          <Input size="large" placeholder={t('calendar.eventTitlePlaceholder')} />
        </Form.Item>
        <Form.Item name="description" label={t('calendar.eventDescription')}>
          <Input.TextArea rows={3} placeholder={t('calendar.eventDescriptionPlaceholder')} />
        </Form.Item>
        <Form.Item name="allDay" valuePropName="checked" className="calendar-all-day-row">
          <Checkbox>{t('calendar.allDay')}</Checkbox>
        </Form.Item>
        <div className="calendar-form-grid">
          <Form.Item name="startTime" label={t('calendar.startTime')} rules={[{ required: true, message: t('validation.required', { field: t('calendar.startTime') }) }]}>
            <DatePicker showTime className="full-width" />
          </Form.Item>
          <Form.Item name="endTime" label={t('calendar.endTime')}>
            <DatePicker showTime className="full-width" />
          </Form.Item>
        </div>
        <div className="calendar-form-grid">
          <Form.Item name="eventType" label={t('calendar.eventType')} initialValue="CUSTOM">
            <Select options={['CUSTOM', 'REMINDER', 'PLAN', 'APPOINTMENT', 'OTHER'].map((value) => ({ value, label: t(`calendar.eventTypes.${value}`) }))} />
          </Form.Item>
          <Form.Item name="repeatType" label={t('calendar.repeatType')} initialValue="NONE">
            <Select options={['NONE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'].map((value) => ({ value, label: t(`calendar.repeatTypes.${value}`) }))} />
          </Form.Item>
        </div>
        <Form.Item name="color" label={t('calendar.color')}>
          <Select
            allowClear
            options={calendarEventColorOptions.map((value) => ({
              value,
              label: <span className="calendar-color-option"><span style={{ background: value }} />{value}</span>,
            }))}
          />
        </Form.Item>
        <Form.Item name="reminderMinutes" label={t('calendar.reminder')}>
          <InputNumber min={0} max={10080} className="full-width" addonAfter={t('calendar.minutes')} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
