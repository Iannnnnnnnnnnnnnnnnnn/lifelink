import type { TFunction } from 'i18next';
import type { Dayjs } from 'dayjs';
import type { CalendarDay } from '../../api/calendar';
import { buildCalendarCells, getWeekdayLabels } from './calendarUtils';
import { CalendarCell } from './CalendarCell';

interface CalendarMonthGridProps {
  month: Dayjs;
  selectedDate: Dayjs;
  dayMap: Map<string, CalendarDay>;
  loading?: boolean;
  language?: string;
  isMobile?: boolean;
  t: TFunction;
  onSelectDate: (date: Dayjs) => void;
  onCreateEvent: (date: Dayjs) => void;
}

export function CalendarMonthGrid({
  month,
  selectedDate,
  dayMap,
  loading,
  language,
  isMobile,
  t,
  onSelectDate,
  onCreateEvent,
}: CalendarMonthGridProps) {
  const cells = buildCalendarCells(month, dayMap, selectedDate);
  const weekDays = getWeekdayLabels(language);

  return (
    <div className={`calendar-month-grid ${loading ? 'calendar-month-grid-loading' : ''}`}>
      <div className="calendar-week-row">
        {weekDays.map((item) => (
          <div key={item} className="calendar-week-cell">{item}</div>
        ))}
      </div>
      <div className="calendar-grid-body">
        {cells.map((cell) => (
          <CalendarCell
            key={cell.key}
            cell={cell}
            language={language}
            isMobile={isMobile}
            t={t}
            onSelect={onSelectDate}
            onCreateEvent={onCreateEvent}
          />
        ))}
      </div>
    </div>
  );
}
