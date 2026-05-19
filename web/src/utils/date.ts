import type { TFunction } from 'i18next';

function getLocale(language?: string) {
  return language === 'en-US' ? 'en-US' : 'zh-CN';
}

function parseDate(value?: string | null) {
  if (!value) {
    return null;
  }
  const normalized = /^\d{4}-\d{2}-\d{2}T/.test(value) ? value : value.replace(' ', 'T');
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function formatDateTime(value: string | undefined | null, t: TFunction, language?: string) {
  const date = parseDate(value);
  if (!date) {
    return t('common.notAvailable');
  }
  return new Intl.DateTimeFormat(getLocale(language), {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

export function formatDate(value: string | undefined | null, t: TFunction, language?: string) {
  const date = parseDate(value);
  if (!date) {
    return t('common.notAvailable');
  }
  return new Intl.DateTimeFormat(getLocale(language), {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(date);
}

export function formatMonth(value: Date, language?: string) {
  return new Intl.DateTimeFormat(getLocale(language), {
    year: 'numeric',
    month: 'long',
  }).format(value);
}

export function formatDashboardDate(value: Date, language?: string) {
  return new Intl.DateTimeFormat(getLocale(language), {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
  }).format(value);
}
