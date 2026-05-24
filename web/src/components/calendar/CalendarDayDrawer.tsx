import { CalendarOutlined, CheckSquareOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Drawer, Space, Tag, Typography } from 'antd';
import type { TFunction } from 'i18next';
import type { Dayjs } from 'dayjs';
import type { CalendarDay, CalendarDayItem } from '../../api/calendar';
import { CalendarSection } from './CalendarSection';
import { CalendarDetailItem } from './CalendarDetailItem';
import { formatFullDate, getDisplayTitle, groupDayItems } from './calendarUtils';

interface CalendarDayDrawerProps {
  open: boolean;
  day?: CalendarDay;
  date: Dayjs;
  isMobile?: boolean;
  language?: string;
  t: TFunction;
  onClose: () => void;
  onCreateEvent: (date: Dayjs) => void;
  onCreateTodo: (date: Dayjs) => void;
  onItemClick: (item: CalendarDayItem) => void;
}

export function CalendarDayDrawer({
  open,
  day,
  date,
  isMobile,
  language,
  t,
  onClose,
  onCreateEvent,
  onCreateTodo,
  onItemClick,
}: CalendarDayDrawerProps) {
  const sections = groupDayItems(day?.items || [], t);
  const tagItems = (day?.items || []).filter((item) => item.type === 'HOLIDAY' || item.type === 'SOLAR_TERM').slice(0, 4);

  return (
    <Drawer
      className="calendar-day-drawer"
      title={null}
      width={540}
      height="88vh"
      placement={isMobile ? 'bottom' : 'right'}
      open={open}
      onClose={onClose}
      destroyOnClose={false}
    >
      <div className="day-detail-header">
        <div className="day-large">{date.date()}</div>
        <div className="day-detail-copy">
          <Typography.Text className="day-week">{formatFullDate(date, language)}</Typography.Text>
          <div className="day-tags">
            {day?.isToday && <Tag>{t('calendar.today')}</Tag>}
            {tagItems.map((item) => (
              <Tag key={`${item.type}-${item.id}`}>{getDisplayTitle(item, language)}</Tag>
            ))}
          </div>
        </div>
      </div>

      <Space className="day-detail-actions" wrap>
        <Button icon={<CheckSquareOutlined />} onClick={() => onCreateTodo(date)}>{t('calendar.addTodo')}</Button>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => onCreateEvent(date)}>{t('calendar.addSchedule')}</Button>
      </Space>

      {sections.length ? (
        <div className="calendar-sections">
          {sections.map((section) => (
            <CalendarSection key={section.key} title={section.title} count={section.items.length}>
              {section.items.map((item) => (
                <CalendarDetailItem key={`${item.type}-${item.id}`} item={item} language={language} t={t} onClick={onItemClick} />
              ))}
            </CalendarSection>
          ))}
        </div>
      ) : (
        <div className="calendar-empty-day">
          <span className="calendar-empty-icon"><CalendarOutlined /></span>
          <Typography.Title level={4}>{t('calendar.emptyDayTitle')}</Typography.Title>
          <Typography.Text type="secondary">{t('calendar.emptyDayDesc')}</Typography.Text>
          <Space wrap className="calendar-empty-actions">
            <Button onClick={() => onCreateTodo(date)}>{t('calendar.addTodo')}</Button>
            <Button type="primary" onClick={() => onCreateEvent(date)}>{t('calendar.addSchedule')}</Button>
          </Space>
        </div>
      )}
    </Drawer>
  );
}
