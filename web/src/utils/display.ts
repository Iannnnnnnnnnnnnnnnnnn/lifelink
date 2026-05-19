import type { TFunction } from 'i18next';
import type { RelationshipType } from '../api/relationship';

export function getEnumLabel(t: TFunction, group: string, value?: string | null) {
  if (!value) {
    return t('common.notAvailable');
  }
  return t(`enum.${group}.${value}`, { defaultValue: value });
}

export function getRelationshipTypeLabel(t: TFunction, type?: string | null) {
  return getEnumLabel(t, 'relationshipType', type);
}

export function getRelationshipTypeOptions(t: TFunction): Array<{ value: RelationshipType; label: string }> {
  return ['COUPLE', 'FAMILY', 'FRIEND', 'TEAM', 'OTHER'].map((value) => ({
    value: value as RelationshipType,
    label: getRelationshipTypeLabel(t, value),
  }));
}

export function getRoleLabel(t: TFunction, role?: string | null) {
  return getEnumLabel(t, 'role', role);
}

export function getRoleColor(role?: string | null) {
  if (role === 'OWNER') return 'gold';
  if (role === 'ADMIN') return 'blue';
  return 'default';
}

export function getStatusLabel(t: TFunction, status?: string | null) {
  return getEnumLabel(t, 'status', status);
}

export function getTodoPriorityLabel(t: TFunction, priority?: string | null) {
  return getEnumLabel(t, 'todoPriority', priority);
}

export function getTodoPriorityColor(priority?: string | null) {
  if (priority === 'HIGH') return 'red';
  if (priority === 'LOW') return 'default';
  return 'blue';
}

export function getTodoStatusLabel(t: TFunction, status?: string | null) {
  return getEnumLabel(t, 'todoStatus', status);
}

export function getTodoStatusColor(status?: string | null) {
  if (status === 'DONE') return 'green';
  if (status === 'DELETED') return 'default';
  return 'gold';
}

export function getTransactionTypeLabel(t: TFunction, type?: string | null) {
  return getEnumLabel(t, 'transactionType', type);
}

export function getVisibilityLabel(t: TFunction, visibility?: string | null) {
  return getEnumLabel(t, 'visibility', visibility);
}

export function getTimelineImportanceLabel(t: TFunction, importance?: string | null) {
  return getEnumLabel(t, 'timelineImportance', importance);
}

export function getTransactionCategoryLabel(t: TFunction, categoryName?: string | null, icon?: string | null) {
  if (!categoryName && !icon) {
    return t('common.notAvailable');
  }
  const categoryKeyMap: Record<string, string> = {
    '餐饮': 'food',
    '交通': 'transport',
    '购物': 'shopping',
    '住房': 'home',
    '娱乐': 'entertainment',
    '医疗': 'medical',
    '学习': 'study',
    '旅行': 'travel',
    '工资': 'salary',
    '奖金': 'bonus',
    '红包': 'gift',
    '兼职': 'part-time',
    '其他': 'other',
  };
  const key = icon || (categoryName ? categoryKeyMap[categoryName] : undefined) || categoryName;
  return t(`enum.transactionCategory.${key}`, { defaultValue: categoryName || key || t('common.notAvailable') });
}
