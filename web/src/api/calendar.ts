import { ApiResult, request } from './request';

export type CalendarItemType =
  | 'TODO'
  | 'TODO_DONE'
  | 'ANNIVERSARY'
  | 'DAILY_POST'
  | 'TRANSACTION'
  | 'HOLIDAY'
  | 'SOLAR_TERM'
  | 'CUSTOM_EVENT'
  | 'CYCLE_PERIOD_ACTUAL'
  | 'CYCLE_PERIOD_PREDICTED'
  | 'CYCLE_OVULATION_ESTIMATED'
  | 'CYCLE_FERTILE_WINDOW_ESTIMATED'
  | 'CYCLE_WARNING'
  | 'CYCLE_CARE_DAY'
  | 'CYCLE_DAILY_REPORT';

export type CalendarEventType = 'CUSTOM' | 'REMINDER' | 'PLAN' | 'APPOINTMENT' | 'OTHER';
export type CalendarRepeatType = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';

export interface CalendarDayItem {
  id: number;
  type: CalendarItemType;
  title: string;
  description?: string;
  date: string;
  startTime?: string;
  endTime?: string;
  allDay?: boolean;
  relationshipId?: number;
  targetType?: string;
  targetId?: number;
  status?: string;
  priority?: string;
  amount?: number;
  incomeAmount?: number;
  expenseAmount?: number;
  color?: string;
  icon?: string;
  metadata?: Record<string, unknown>;
}

export interface CalendarDay {
  date: string;
  dayOfWeek: number;
  isToday: boolean;
  isWeekend: boolean;
  isHoliday: boolean;
  isWorkday: boolean;
  lunarText?: string;
  holidayNames: string[];
  solarTermNames: string[];
  items: CalendarDayItem[];
  todoCount: number;
  doneTodoCount: number;
  dailyPostCount: number;
  anniversaryCount: number;
  incomeAmount: number;
  expenseAmount: number;
}

export interface CalendarMonth {
  year: number;
  month: number;
  relationshipId: number;
  days: CalendarDay[];
}

export interface CalendarMonthQuery {
  relationshipId: number;
  year: number;
  month: number;
  includeTodos?: boolean;
  includeAnniversaries?: boolean;
  includeDailyPosts?: boolean;
  includeTransactions?: boolean;
  includeHolidays?: boolean;
  includeCustomEvents?: boolean;
  includeCycleCare?: boolean;
}

export interface CalendarDayQuery {
  relationshipId: number;
  date: string;
}

export interface CreateCalendarEventRequest {
  relationshipId: number;
  title: string;
  description?: string;
  eventType?: CalendarEventType;
  startTime: string;
  endTime?: string;
  allDay?: boolean;
  repeatType?: CalendarRepeatType;
  reminderMinutes?: number;
  color?: string;
}

export type UpdateCalendarEventRequest = CreateCalendarEventRequest;

export function getCalendarMonth(params: CalendarMonthQuery) {
  return request.get<ApiResult<CalendarMonth>>('/api/calendar/month', { params });
}

export function getCalendarDay(params: CalendarDayQuery) {
  return request.get<ApiResult<CalendarDay>>('/api/calendar/day', { params });
}

export function createCalendarEvent(data: CreateCalendarEventRequest) {
  return request.post<ApiResult<CalendarDay>>('/api/calendar/events', data);
}

export function updateCalendarEvent(eventId: number, data: UpdateCalendarEventRequest) {
  return request.put<ApiResult<CalendarDay>>(`/api/calendar/events/${eventId}`, data);
}

export function deleteCalendarEvent(eventId: number) {
  return request.delete<ApiResult<void>>(`/api/calendar/events/${eventId}`);
}
