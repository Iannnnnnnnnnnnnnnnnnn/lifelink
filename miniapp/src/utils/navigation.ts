import Taro from '@tarojs/taro';

export function goRelationshipDetail(id: number) {
  Taro.navigateTo({ url: `/pages/relationships/detail?id=${id}` });
}

export function goTodos(relationshipId?: number) {
  const query = relationshipId ? `?relationshipId=${relationshipId}` : '';
  Taro.navigateTo({ url: `/pages/todos/index${query}` });
}

export function goDailyCreate(relationshipId?: number) {
  const query = relationshipId ? `?relationshipId=${relationshipId}` : '';
  Taro.navigateTo({ url: `/pages/daily/create${query}` });
}
