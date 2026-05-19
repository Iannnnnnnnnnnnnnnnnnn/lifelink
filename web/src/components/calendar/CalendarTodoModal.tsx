import { DatePicker, Form, Input, Modal, Select } from 'antd';
import type { FormInstance } from 'antd';
import type { TFunction } from 'i18next';
import type { Dayjs } from 'dayjs';
import type { TodoPriority } from '../../api/spaceTodo';

export interface CalendarTodoFormValues {
  title: string;
  content?: string;
  priority: TodoPriority;
  dueTime?: Dayjs;
}

interface CalendarTodoModalProps {
  open: boolean;
  form: FormInstance<CalendarTodoFormValues>;
  saving?: boolean;
  t: TFunction;
  onCancel: () => void;
  onSubmit: () => void;
}

export function CalendarTodoModal({ open, form, saving, t, onCancel, onSubmit }: CalendarTodoModalProps) {
  return (
    <Modal
      className="calendar-event-modal"
      title={t('calendar.addTodo')}
      open={open}
      onOk={onSubmit}
      onCancel={onCancel}
      confirmLoading={saving}
      okText={t('common.save')}
      cancelText={t('common.cancel')}
    >
      <Form form={form} layout="vertical" className="calendar-soft-form">
        <Form.Item name="title" label={t('todo.titleField')} rules={[{ required: true, message: t('todo.titleRequired') }, { max: 100, message: t('todo.titleLength') }]}>
          <Input size="large" placeholder={t('todo.titlePlaceholder')} />
        </Form.Item>
        <Form.Item name="content" label={t('todo.contentField')}>
          <Input.TextArea rows={3} placeholder={t('todo.contentPlaceholder')} />
        </Form.Item>
        <div className="calendar-form-grid">
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
        </div>
      </Form>
    </Modal>
  );
}
