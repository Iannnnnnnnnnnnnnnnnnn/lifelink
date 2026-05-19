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
    dot: '#4f8df7',
    bg: '#f1f6ff',
    border: '#d9e7ff',
    text: '#275a9f',
    grayDot: '#667085',
    grayBg: '#f5f6f7',
    grayBorder: '#e4e7ec',
    grayText: '#475467',
  },
  TODO_DONE: {
    key: 'TODO_DONE',
    dot: '#58b87a',
    bg: '#f1faf4',
    border: '#d8efdf',
    text: '#2e7146',
    grayDot: '#7a828f',
    grayBg: '#f6f7f8',
    grayBorder: '#e5e7eb',
    grayText: '#667085',
  },
  ANNIVERSARY: {
    key: 'ANNIVERSARY',
    dot: '#c678dd',
    bg: '#fbf3ff',
    border: '#efd8f8',
    text: '#7a3f8f',
    grayDot: '#757b86',
    grayBg: '#f6f6f7',
    grayBorder: '#e2e4e8',
    grayText: '#555f6d',
  },
  DAILY_POST: {
    key: 'DAILY_POST',
    dot: '#e9a34f',
    bg: '#fff7eb',
    border: '#f6dfbd',
    text: '#8a5b1f',
    grayDot: '#8b919b',
    grayBg: '#f7f7f8',
    grayBorder: '#e6e8eb',
    grayText: '#5f6875',
  },
  TRANSACTION: {
    key: 'TRANSACTION',
    dot: '#5bbbc4',
    bg: '#eefbfc',
    border: '#d1edf0',
    text: '#237078',
    grayDot: '#707987',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#525d6a',
  },
  HOLIDAY: {
    key: 'HOLIDAY',
    dot: '#df746c',
    bg: '#fff3f1',
    border: '#f4d8d5',
    text: '#923f38',
    grayDot: '#777f89',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#555f6d',
  },
  FESTIVAL: {
    key: 'FESTIVAL',
    dot: '#df746c',
    bg: '#fff3f1',
    border: '#f4d8d5',
    text: '#923f38',
    grayDot: '#777f89',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#555f6d',
  },
  SOLAR_TERM: {
    key: 'SOLAR_TERM',
    dot: '#56b39b',
    bg: '#effaf7',
    border: '#d4eee7',
    text: '#276e5e',
    grayDot: '#747c87',
    grayBg: '#f6f7f8',
    grayBorder: '#e4e7ec',
    grayText: '#555f6d',
  },
  CUSTOM_EVENT: {
    key: 'CUSTOM_EVENT',
    dot: '#7487a8',
    bg: '#f3f6fb',
    border: '#dce4f1',
    text: '#43536f',
    grayDot: '#697382',
    grayBg: '#f5f6f7',
    grayBorder: '#e1e4e8',
    grayText: '#475467',
  },
};

export function getCalendarEventStyle(type: CalendarVisualType) {
  return calendarEventStyles[type] || calendarEventStyles.CUSTOM_EVENT;
}

export const calendarEventColorOptions = ['#7487a8', '#4f8df7', '#56b39b', '#e9a34f', '#c678dd', '#df746c'];
