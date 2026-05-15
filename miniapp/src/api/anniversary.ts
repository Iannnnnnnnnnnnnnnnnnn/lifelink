import { http } from './request';

export interface Anniversary {
  id: number;
  relationshipId: number;
  relationshipName?: string;
  title: string;
  description?: string;
  anniversaryDate: string;
  repeatType?: 'NONE' | 'YEARLY';
  backgroundUrl?: string;
  dayCount?: number;
  displayType?: 'COUNTDOWN' | 'PASSED' | 'TODAY';
  createdAt?: string;
  updatedAt?: string;
}

export interface AnniversaryQuery {
  relationshipId?: number | string;
  displayType?: 'COUNTDOWN' | 'PASSED' | 'TODAY';
  repeatType?: 'NONE' | 'YEARLY';
  page?: number;
  size?: number;
}

export function getAnniversaries(params: AnniversaryQuery = {}) {
  return http.get<Anniversary[]>('/api/anniversaries', params as Record<string, unknown>);
}
