export function formatDateTime(value?: string) {
  if (!value) return '';
  return value.replace('T', ' ').slice(0, 16);
}

export function truncate(text?: string, max = 48) {
  if (!text) return '';
  return text.length > max ? `${text.slice(0, max)}...` : text;
}

export function getAnniversaryDisplayText(item: { title: string; displayType?: string; dayCount?: number }) {
  if (item.displayType === 'TODAY') return `今天是 ${item.title}`;
  if (item.displayType === 'PASSED') return `${item.title} 已经 ${item.dayCount || 0} 天`;
  return `距离 ${item.title} 还有 ${item.dayCount || 0} 天`;
}
