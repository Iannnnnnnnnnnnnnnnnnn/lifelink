import type { TFunction } from 'i18next';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import type { CalendarDay, CalendarDayItem, CalendarItemType } from '../../api/calendar';

export interface CalendarGridCell {
  key: string;
  date: Dayjs;
  day?: CalendarDay;
  inCurrentMonth: boolean;
  selected: boolean;
}

export interface CalendarPillData {
  key: string;
  type: CalendarItemType | 'FESTIVAL';
  text: string;
}

export interface CalendarSectionData {
  key: string;
  title: string;
  items: CalendarDayItem[];
}

const weekLabels = {
  'zh-CN': ['一', '二', '三', '四', '五', '六', '日'],
  'en-US': ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
};

export function getLocale(language?: string) {
  return language === 'en-US' ? 'en-US' : 'zh-CN';
}

export function getWeekdayLabels(language?: string) {
  return weekLabels[getLocale(language)];
}

export function formatMonthTitle(value: Dayjs, language?: string) {
  if (getLocale(language) === 'zh-CN') {
    return `${value.year()}年${value.month() + 1}月`;
  }
  return new Intl.DateTimeFormat('en-US', { month: 'long', year: 'numeric' }).format(value.toDate());
}

export function formatFullDate(value: Dayjs, language?: string) {
  const locale = getLocale(language);
  if (locale === 'zh-CN') {
    return new Intl.DateTimeFormat('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'long',
    }).format(value.toDate());
  }
  return new Intl.DateTimeFormat('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  }).format(value.toDate());
}

export function formatShortTime(value?: string | null, language?: string) {
  if (!value) return '';
  const parsed = dayjs(value);
  if (!parsed.isValid()) return '';
  return new Intl.DateTimeFormat(getLocale(language), {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(parsed.toDate());
}

export function formatMoney(value: number | undefined, language?: string) {
  return new Intl.NumberFormat(getLocale(language), {
    style: 'currency',
    currency: 'CNY',
    maximumFractionDigits: 2,
  }).format(value || 0);
}

export function getDisplayTitle(item: CalendarDayItem, language?: string) {
  if ((item.type === 'HOLIDAY' || item.type === 'SOLAR_TERM') && getLocale(language) === 'en-US') {
    return (item.metadata?.nameEn as string) || item.title;
  }
  return item.title;
}

export function getHolidayVisualType(item: CalendarDayItem): CalendarItemType | 'FESTIVAL' {
  if (item.type === 'SOLAR_TERM') return 'SOLAR_TERM';
  return 'FESTIVAL';
}

export function getCalendarItemLabel(t: TFunction, item: CalendarDayItem) {
  if (item.type === 'SOLAR_TERM') return t('calendar.solarTerm');
  if (item.type === 'HOLIDAY') {
    const holidayType = item.metadata?.holidayType;
    if (holidayType === 'LEGAL_HOLIDAY') return t('calendar.legalHoliday');
    if (holidayType === 'WORKDAY') return t('calendar.workday');
    return t('calendar.festival');
  }
  return t(`calendar.itemTypes.${item.type}`);
}

export function buildCalendarCells(month: Dayjs, dayMap: Map<string, CalendarDay>, selectedDate: Dayjs): CalendarGridCell[] {
  const monthStart = month.startOf('month');
  const startOffset = (monthStart.day() + 6) % 7;
  const gridStart = monthStart.subtract(startOffset, 'day');
  return Array.from({ length: 42 }, (_, index) => {
    const date = gridStart.add(index, 'day');
    const key = date.format('YYYY-MM-DD');
    return {
      key,
      date,
      day: dayMap.get(key),
      inCurrentMonth: date.month() === month.month(),
      selected: date.isSame(selectedDate, 'day'),
    };
  });
}

export function buildDayPills(day: CalendarDay | undefined, t: TFunction, language?: string, maxItems = 3): { pills: CalendarPillData[]; moreCount: number } {
  if (!day) return { pills: [], moreCount: 0 };
  const pills: CalendarPillData[] = [];
  const add = (type: CalendarPillData['type'], key: string, text?: string) => {
    if (text && pills.length < maxItems) {
      pills.push({ type, key, text });
    }
  };

  const holidayItems = day.items.filter((item) => item.type === 'HOLIDAY' || item.type === 'SOLAR_TERM');
  holidayItems.slice(0, 2).forEach((item) => add(getHolidayVisualType(item), `${item.type}-${item.id}`, getDisplayTitle(item, language)));

  const anniversary = day.items.find((item) => item.type === 'ANNIVERSARY');
  add('ANNIVERSARY', anniversary ? `anniversary-${anniversary.id}` : 'anniversary-count', anniversary ? getDisplayTitle(anniversary, language) : undefined);

  if (day.todoCount > 0) add('TODO', 'todo-count', t('calendar.todos', { count: day.todoCount }));
  if (day.doneTodoCount > 0) add('TODO_DONE', 'done-todo-count', t('calendar.doneTodos', { count: day.doneTodoCount }));
  if (day.dailyPostCount > 0) add('DAILY_POST', 'daily-count', t('calendar.dailyPosts', { count: day.dailyPostCount }));
  if (day.expenseAmount > 0) add('TRANSACTION', 'expense', `${t('calendar.expense')} ${formatMoney(day.expenseAmount, language)}`);
  if (day.incomeAmount > 0) add('TRANSACTION', 'income', `${t('calendar.income')} ${formatMoney(day.incomeAmount, language)}`);

  const customEvents = day.items.filter((item) => item.type === 'CUSTOM_EVENT');
  customEvents.forEach((item) => add('CUSTOM_EVENT', `event-${item.id}`, getDisplayTitle(item, language)));

  return { pills, moreCount: Math.max(0, day.items.length - pills.length) };
}

export function groupDayItems(items: CalendarDayItem[], t: TFunction): CalendarSectionData[] {
  const sectionOrder = [
    { key: 'todos', title: t('calendar.sectionTodos'), types: ['TODO', 'TODO_DONE'] },
    { key: 'anniversaries', title: t('calendar.sectionAnniversaries'), types: ['ANNIVERSARY'] },
    { key: 'daily', title: t('calendar.sectionDailyPosts'), types: ['DAILY_POST'] },
    { key: 'transactions', title: t('calendar.sectionTransactions'), types: ['TRANSACTION'] },
    { key: 'events', title: t('calendar.sectionEvents'), types: ['CUSTOM_EVENT'] },
    { key: 'holidays', title: t('calendar.holidayAndSolarTerm'), types: ['HOLIDAY', 'SOLAR_TERM'] },
  ];

  return sectionOrder
    .map((section) => ({
      key: section.key,
      title: section.title,
      items: items.filter((item) => section.types.includes(item.type)),
    }))
    .filter((section) => section.items.length > 0);
}
