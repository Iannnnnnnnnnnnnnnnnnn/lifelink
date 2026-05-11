import { ApiResult, request } from './request';

export type AnniversaryRepeatType = 'NONE' | 'YEARLY';
export type AnniversaryDisplayType = 'COUNTDOWN' | 'PASSED' | 'TODAY';

export interface CreateAnniversaryRequest {
  relationshipId: number;
  title: string;
  description?: string;
  anniversaryDate: string;
  repeatType?: AnniversaryRepeatType;
  backgroundFileId?: number;
}

export interface UpdateAnniversaryRequest {
  title: string;
  description?: string;
  anniversaryDate: string;
  repeatType?: AnniversaryRepeatType;
  backgroundFileId?: number;
}

export interface Anniversary {
  id: number;
  relationshipId: number;
  relationshipName?: string;
  title: string;
  description?: string;
  anniversaryDate: string;
  repeatType: AnniversaryRepeatType;
  backgroundFileId?: number;
  backgroundUrl?: string;
  dayCount: number;
  displayType: AnniversaryDisplayType;
  passedYears?: number;
  createdBy: number;
  createdAt: string;
  updatedAt: string;
}

export interface GetAnniversariesParams {
  relationshipId?: number;
  repeatType?: AnniversaryRepeatType;
  displayType?: AnniversaryDisplayType;
  keyword?: string;
  page?: number;
  size?: number;
}

export function createAnniversary(data: CreateAnniversaryRequest) {
  return request.post<ApiResult<Anniversary>>('/api/anniversaries', data);
}

export function getAnniversaries(params: GetAnniversariesParams = {}) {
  return request.get<ApiResult<Anniversary[]>>('/api/anniversaries', { params });
}

export function getAnniversaryDetail(id: number) {
  return request.get<ApiResult<Anniversary>>(`/api/anniversaries/${id}`);
}

export function updateAnniversary(id: number, data: UpdateAnniversaryRequest) {
  return request.put<ApiResult<Anniversary>>(`/api/anniversaries/${id}`, data);
}

export function deleteAnniversary(id: number) {
  return request.delete<ApiResult<void>>(`/api/anniversaries/${id}`);
}
