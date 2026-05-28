import type { CalendarItemType } from '../../api/calendar';

export type CalendarVisualType = CalendarItemType | 'FESTIVAL';

export interface CalendarEventStyle {
  key: CalendarVisualType;
  dot: string;
  bg: string;
  border: string;
  text: string;
  grayDot: string;
  grayBg: string;
  grayBorder: string;
  grayText: string;
}

export const calendarEventStyles: Record<CalendarVisualType, CalendarEventStyle> = {
  TODO: {
    key: 'TODO',
    dot: '#4f7fd8',
    bg: '#f3f6fc',
    border: '#dde6f7',
    text: '#345988',
    grayDot: '#667085',
    grayBg: '#f5f6f7',
    grayBorder: '#e4e7ec',
    grayText: '#475467',
  },
  TODO_DONE: {
    key: 'TODO_DONE',
    dot: '#5e9b74',
    bg: '#f4faf6',
    border: '#dceee3',
    text: '#3f7150',
    grayDot: '#7a828f',
    grayBg: '#f6f7f8',
    grayBorder: '#e5e7eb',
    grayText: '#667085',
  },
  ANNIVERSARY: {
    key: 'ANNIVERSARY',
    dot: '#8d7cc3',
    bg: '#f7f5fc',
    border: '#e6e0f4',
    text: '#5d5287',
    grayDot: '#757b86',
    grayBg: '#f6f6f7',
    grayBorder: '#e2e4e8',
    grayText: '#555f6d',
  },
  DAILY_POST: {
    key: 'DAILY_POST',
    dot: '#b98a4a',
    bg: '#fbf7ef',
    border: '#eadfc8',
    text: '#765d35',
    grayDot: '#8b919b',
    grayBg: '#f7f7f8',
    grayBorder: '#e6e8eb',
    grayText: '#5f6875',
  },
  TRANSACTION: {
    key: 'TRANSACTION',
    dot: '#5a9da6',
    bg: '#f1f8f9',
    border: '#d8eaec',
    text: '#3d7077',
    grayDot: '#707987',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#525d6a',
  },
  HOLIDAY: {
    key: 'HOLIDAY',
    dot: '#c06b65',
    bg: '#fbf3f2',
    border: '#ead8d6',
    text: '#7d4945',
    grayDot: '#777f89',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#555f6d',
  },
  FESTIVAL: {
    key: 'FESTIVAL',
    dot: '#c06b65',
    bg: '#fbf3f2',
    border: '#ead8d6',
    text: '#7d4945',
    grayDot: '#777f89',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#555f6d',
  },
  SOLAR_TERM: {
    key: 'SOLAR_TERM',
    dot: '#5b9e89',
    bg: '#f2f9f6',
    border: '#d9ebe5',
    text: '#3f7063',
    grayDot: '#747c87',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#555f6d',
  },
  CUSTOM_EVENT: {
    key: 'CUSTOM_EVENT',
    dot: '#78879d',
    bg: '#f3f6fb',
    border: '#dce4f1',
    text: '#43536f',
    grayDot: '#697382',
    grayBg: '#f5f6f7',
    grayBorder: '#e1e4e8',
    grayText: '#475467',
  },
  CYCLE_PERIOD_ACTUAL: {
    key: 'CYCLE_PERIOD_ACTUAL',
    dot: '#b9829d',
    bg: '#fbf5f8',
    border: '#ecdde4',
    text: '#7a5566',
    grayDot: '#757b86',
    grayBg: '#f6f6f7',
    grayBorder: '#e2e4e8',
    grayText: '#555f6d',
  },
  CYCLE_PERIOD_PREDICTED: {
    key: 'CYCLE_PERIOD_PREDICTED',
    dot: '#b98a4a',
    bg: '#fbf7ef',
    border: '#eadfc8',
    text: '#765d35',
    grayDot: '#8b919b',
    grayBg: '#f7f7f8',
    grayBorder: '#e6e8eb',
    grayText: '#5f6875',
  },
  CYCLE_OVULATION_ESTIMATED: {
    key: 'CYCLE_OVULATION_ESTIMATED',
    dot: '#5b9e89',
    bg: '#f2f9f6',
    border: '#d9ebe5',
    text: '#3f7063',
    grayDot: '#747c87',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#555f6d',
  },
  CYCLE_FERTILE_WINDOW_ESTIMATED: {
    key: 'CYCLE_FERTILE_WINDOW_ESTIMATED',
    dot: '#5a9da6',
    bg: '#f1f8f9',
    border: '#d8eaec',
    text: '#3d7077',
    grayDot: '#707987',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#525d6a',
  },
  CYCLE_WARNING: {
    key: 'CYCLE_WARNING',
    dot: '#c06b65',
    bg: '#fbf3f2',
    border: '#ead8d6',
    text: '#7d4945',
    grayDot: '#777f89',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#555f6d',
  },
  CYCLE_CARE_DAY: {
    key: 'CYCLE_CARE_DAY',
    dot: '#7487a8',
    bg: '#f3f6fb',
    border: '#dce4f1',
    text: '#43536f',
    grayDot: '#697382',
    grayBg: '#f5f6f7',
    grayBorder: '#e1e4e8',
    grayText: '#475467',
  },
  CYCLE_DAILY_REPORT: {
    key: 'CYCLE_DAILY_REPORT',
    dot: '#4f7fd8',
    bg: '#f3f6fc',
    border: '#dde6f7',
    text: '#345988',
    grayDot: '#667085',
    grayBg: '#f5f6f7',
    grayBorder: '#e4e7ec',
    grayText: '#475467',
  },
};

export function getCalendarEventStyle(type: CalendarVisualType) {
  return calendarEventStyles[type] || calendarEventStyles.CUSTOM_EVENT;
}

export const calendarEventColorOptions = ['#78879d', '#4f7fd8', '#5e9b74', '#b98a4a', '#8d7cc3', '#c06b65'];
