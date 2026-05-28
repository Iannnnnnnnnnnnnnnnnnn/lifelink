import { CheckCircleOutlined, CheckSquareOutlined, DollarOutlined, HeartOutlined, ReadOutlined, CalendarOutlined } from '@ant-design/icons';
import { Typography } from 'antd';
import type { TFunction } from 'i18next';
import type { CSSProperties } from 'react';
import type { CalendarDayItem } from '../../api/calendar';
import { formatMoney, formatShortTime, getCalendarItemLabel, getDisplayTitle, getHolidayVisualType } from './calendarUtils';
import { getCalendarEventStyle } from './calendarTheme';

interface CalendarDetailItemProps {
  item: CalendarDayItem;
  language?: string;
  t: TFunction;
  onClick: (item: CalendarDayItem) => void;
}

function getIcon(item: CalendarDayItem) {
  if (item.type === 'TODO') return <CheckSquareOutlined />;
  if (item.type === 'TODO_DONE') return <CheckCircleOutlined />;
  if (item.type === 'ANNIVERSARY') return <HeartOutlined />;
  if (item.type.startsWith('CYCLE_')) return <HeartOutlined />;
  if (item.type === 'DAILY_POST') return <ReadOutlined />;
  if (item.type === 'TRANSACTION') return <DollarOutlined />;
  return <CalendarOutlined />;
}

function getRightText(item: CalendarDayItem, language?: string, t?: TFunction) {
  if (item.allDay) return t ? t('calendar.allDay') : '';
  if (item.type === 'TRANSACTION') {
    if (item.incomeAmount) return formatMoney(item.incomeAmount, language);
    if (item.expenseAmount) return formatMoney(item.expenseAmount, language);
  }
  return formatShortTime(item.startTime, language);
}

export function CalendarDetailItem({ item, language, t, onClick }: CalendarDetailItemProps) {
  const visualType = item.type === 'HOLIDAY' || item.type === 'SOLAR_TERM' ? getHolidayVisualType(item) : item.type;
  const style = getCalendarEventStyle(visualType);
  const clickable = item.type !== 'HOLIDAY' && item.type !== 'SOLAR_TERM';
  const title = getDisplayTitle(item, language);
  const rightText = getRightText(item, language, t);
  const description = item.description || getCalendarItemLabel(t, item);

  return (
    <button
      type="button"
      className={`calendar-detail-item ${clickable ? 'calendar-detail-item-clickable' : ''}`}
      onClick={() => clickable && onClick(item)}
      style={{
        '--detail-dot': style.dot,
        '--detail-bg': style.bg,
        '--detail-border': style.border,
        '--detail-text': style.text,
        '--detail-gray-dot': style.grayDot,
        '--detail-gray-bg': style.grayBg,
        '--detail-gray-border': style.grayBorder,
        '--detail-gray-text': style.grayText,
      } as CSSProperties}
    >
      <span className="item-color-bar" />
      <span className="item-icon">{getIcon(item)}</span>
      <span className="item-content">
        <Typography.Text strong className="item-title">{title}</Typography.Text>
        <Typography.Text type="secondary" className="item-desc">{description}</Typography.Text>
      </span>
      {rightText && <span className="item-time">{rightText}</span>}
    </button>
  );
}
