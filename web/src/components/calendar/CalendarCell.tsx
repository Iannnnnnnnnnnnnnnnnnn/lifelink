import type { TFunction } from 'i18next';
import type { CalendarGridCell } from './calendarUtils';
import { buildDayPills } from './calendarUtils';
import { CalendarEventPill } from './CalendarEventPill';

interface CalendarCellProps {
  cell: CalendarGridCell;
  language?: string;
  isMobile?: boolean;
  t: TFunction;
  onSelect: (date: CalendarGridCell['date']) => void;
  onCreateEvent: (date: CalendarGridCell['date']) => void;
}

export function CalendarCell({ cell, language, isMobile, t, onSelect, onCreateEvent }: CalendarCellProps) {
  const maxItems = isMobile ? 2 : 3;
  const { pills, moreCount } = buildDayPills(cell.day, t, language, maxItems);
  const hasContent = Boolean(cell.day?.items.length);

  return (
    <button
      type="button"
      className={[
        'calendar-cell',
        cell.inCurrentMonth ? '' : 'calendar-cell-muted',
        cell.day?.isWeekend ? 'calendar-cell-weekend' : '',
        cell.day?.isToday ? 'calendar-cell-has-today' : '',
        cell.selected ? 'calendar-cell-selected' : '',
      ].filter(Boolean).join(' ')}
      onClick={() => onSelect(cell.date)}
      onDoubleClick={() => onCreateEvent(cell.date)}
      aria-label={cell.key}
    >
      <span className="calendar-cell-header">
        <span className={`day-number ${cell.day?.isToday ? 'today' : ''}`}>{cell.date.date()}</span>
        {cell.day?.isToday && <span className="today-label">{t('calendar.today')}</span>}
      </span>
      <span className="calendar-cell-items">
        {pills.map((pill) => (
          <CalendarEventPill key={pill.key} type={pill.type} text={pill.text} compact={isMobile} />
        ))}
        {moreCount > 0 && <span className="calendar-more">{t('calendar.moreItems', { count: moreCount })}</span>}
      </span>
      {isMobile && hasContent && <span className="calendar-mobile-dots" aria-hidden="true" />}
    </button>
  );
}
