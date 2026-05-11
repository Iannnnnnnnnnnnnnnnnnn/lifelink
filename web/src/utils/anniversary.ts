import type { TFunction } from 'i18next';
import { Anniversary } from '../api/anniversary';

export function getAnniversaryDisplayText(item: Anniversary, t: TFunction) {
  if (item.displayType === 'TODAY') {
    return t('anniversary.todayIs', { title: item.title });
  }
  if (item.displayType === 'PASSED') {
    return t('anniversary.daysPassed', { title: item.title, count: item.dayCount });
  }
  return t('anniversary.daysLeft', { title: item.title, count: item.dayCount });
}

export function getRepeatTypeLabel(repeatType: Anniversary['repeatType'], t: TFunction) {
  return repeatType === 'YEARLY' ? t('anniversary.yearlyRepeat') : t('anniversary.noneRepeat');
}
