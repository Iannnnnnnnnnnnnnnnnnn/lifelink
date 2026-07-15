import { ApiResult, request } from './request';

export interface DatingRecordPayload {
  datingDate: string;
  activities: string[];
  location?: string;
  note?: string;
}

export interface CreateDatingRecordPayload extends DatingRecordPayload {
  relationshipId: number;
}

export interface DatingRecord {
  id: number;
  relationshipId: number;
  relationshipName?: string;
  sequenceNumber: number;
  datingDate: string;
  activities: string[];
  location?: string;
  note?: string;
  createdBy: number;
  createdAt: string;
  updatedAt: string;
}

export interface DatingRecordList {
  total: number;
  records: DatingRecord[];
}

export function createDatingRecord(data: CreateDatingRecordPayload) {
  return request.post<ApiResult<DatingRecord>>('/api/dating-records', data);
}

export function getDatingRecords(relationshipId: number) {
  return request.get<ApiResult<DatingRecordList>>(`/api/relationships/${relationshipId}/dating-records`);
}

export function getDatingRecord(id: number) {
  return request.get<ApiResult<DatingRecord>>(`/api/dating-records/${id}`);
}

export function updateDatingRecord(id: number, data: DatingRecordPayload) {
  return request.put<ApiResult<DatingRecord>>(`/api/dating-records/${id}`, data);
}

export function deleteDatingRecord(id: number) {
  return request.delete<ApiResult<void>>(`/api/dating-records/${id}`);
}
