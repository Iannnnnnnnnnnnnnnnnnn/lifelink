import { ApiResult, request } from './request';

export type SearchType = 'RELATIONSHIP' | 'DAILY_POST' | 'TODO' | 'ANNIVERSARY' | 'ACTIVITY' | 'COMMENT' | 'NOTIFICATION' | 'TRANSACTION';

export interface SearchItem {
  id: number;
  type: SearchType | string;
  title: string;
  description?: string;
  highlight?: string;
  relationshipId?: number;
  relationshipName?: string;
  targetUrl?: string;
  createdAt?: string;
  metadata?: Record<string, unknown>;
}

export interface SearchGroup {
  type: SearchType | string;
  title: string;
  count: number;
  items: SearchItem[];
}

export interface SearchResponse {
  keyword: string;
  totalCount: number;
  groups: SearchGroup[];
}

export interface GlobalSearchParams {
  keyword: string;
  types?: string;
  page?: number;
  size?: number;
}

export function globalSearch(params: GlobalSearchParams) {
  return request.get<ApiResult<SearchResponse>>('/api/search', { params });
}
