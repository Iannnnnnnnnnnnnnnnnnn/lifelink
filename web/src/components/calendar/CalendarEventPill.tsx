import { getCalendarEventStyle } from './calendarTheme';
import type { CalendarVisualType } from './calendarTheme';
import type { CSSProperties } from 'react';

interface CalendarEventPillProps {
  type: CalendarVisualType;
  text: string;
  compact?: boolean;
}

export function CalendarEventPill({ type, text, compact }: CalendarEventPillProps) {
  const style = getCalendarEventStyle(type);

  return (
    <span
      className={`calendar-event-pill calendar-event-pill--${type.toLowerCase()} ${compact ? 'calendar-event-pill--compact' : ''}`}
      style={{
        '--event-dot': style.dot,
        '--event-bg': style.bg,
        '--event-border': style.border,
        '--event-text': style.text,
        '--event-gray-dot': style.grayDot,
        '--event-gray-bg': style.grayBg,
        '--event-gray-border': style.grayBorder,
        '--event-gray-text': style.grayText,
      } as CSSProperties}
      title={text}
    >
      <span className="calendar-event-dot" />
      <span className="calendar-event-text">{text}</span>
    </span>
  );
}
