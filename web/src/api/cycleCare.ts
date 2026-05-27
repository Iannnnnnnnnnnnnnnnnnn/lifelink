import { ApiResult, request } from './request';

export type CycleShareLevel = 'PRIVATE' | 'BASIC' | 'DETAILED';
export type CycleFlowLevel = 'NONE' | 'LIGHT' | 'MEDIUM' | 'HEAVY' | 'VERY_HEAVY';

export interface CycleCareAccess {
  enabled: boolean;
  reason?: string;
  loverSpaceIds: number[];
}

export interface CycleCareProfile {
  id: number;
  userId: number;
  defaultLoverSpaceId?: number;
  cycleLength: number;
  periodLength: number;
  lastPeriodStartDate?: string;
  reminderEnabled: boolean;
  shareLevel: CycleShareLevel;
  timezone: string;
  createdAt: string;
  updatedAt: string;
}

export interface CycleWarning {
  id: number;
  loverSpaceId?: number;
  warningType: string;
  warningDate: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  title: string;
  message: string;
  status: string;
  createdAt: string;
}

export interface CycleToday {
  phase: string;
  phaseLabel: string;
  daysToNextPeriod?: number;
  predicted: boolean;
  predictedPeriod: boolean;
  predictedNextStartDate?: string;
  predictedNextEndDate?: string;
  title: string;
  reminder: string;
  clothingAdvice: string;
  foodAdvice: string;
  restAdvice: string;
  moodAdvice: string;
  partnerAdvice: string;
  disclaimer: string;
  warnings: CycleWarning[];
}

export interface CyclePeriodRecord {
  id: number;
  loverSpaceId?: number;
  startDate: string;
  endDate?: string;
  cycleLengthSnapshot?: number;
  periodLengthSnapshot?: number;
  note?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CycleDailyLog {
  id: number;
  loverSpaceId?: number;
  logDate: string;
  flowLevel: CycleFlowLevel;
  painLevel?: number;
  mood?: string;
  symptoms: string[];
  temperatureFeeling?: string;
  appetite?: string;
  note?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UpsertCycleCareProfileRequest {
  defaultLoverSpaceId?: number;
  cycleLength?: number;
  periodLength?: number;
  lastPeriodStartDate?: string;
  reminderEnabled?: boolean;
  shareLevel?: CycleShareLevel;
  timezone?: string;
}

export interface CreateCyclePeriodRecordRequest {
  loverSpaceId?: number;
  startDate: string;
  endDate?: string;
  note?: string;
}

export type UpdateCyclePeriodRecordRequest = CreateCyclePeriodRecordRequest;

export interface UpsertCycleDailyLogRequest {
  loverSpaceId?: number;
  flowLevel?: CycleFlowLevel;
  painLevel?: number;
  mood?: string;
  symptoms?: string[];
  temperatureFeeling?: string;
  appetite?: string;
  note?: string;
}

export function getCycleCareAccess() {
  return request.get<ApiResult<CycleCareAccess>>('/api/cycle-care/access');
}

export function getCycleCareProfile() {
  return request.get<ApiResult<CycleCareProfile>>('/api/cycle-care/profile');
}

export function upsertCycleCareProfile(data: UpsertCycleCareProfileRequest) {
  return request.put<ApiResult<CycleCareProfile>>('/api/cycle-care/profile', data);
}

export function getCycleToday(loverSpaceId?: number) {
  return request.get<ApiResult<CycleToday>>('/api/cycle-care/today', { params: { loverSpaceId } });
}

export function getCyclePeriodRecords(params?: { loverSpaceId?: number; page?: number; size?: number }) {
  return request.get<ApiResult<CyclePeriodRecord[]>>('/api/cycle-care/period-records', { params });
}

export function createCyclePeriodRecord(data: CreateCyclePeriodRecordRequest) {
  return request.post<ApiResult<CyclePeriodRecord>>('/api/cycle-care/period-records', data);
}

export function updateCyclePeriodRecord(recordId: number, data: UpdateCyclePeriodRecordRequest) {
  return request.put<ApiResult<CyclePeriodRecord>>(`/api/cycle-care/period-records/${recordId}`, data);
}

export function deleteCyclePeriodRecord(recordId: number) {
  return request.delete<ApiResult<void>>(`/api/cycle-care/period-records/${recordId}`);
}

export function getCycleDailyLog(date: string) {
  return request.get<ApiResult<CycleDailyLog | null>>(`/api/cycle-care/daily-logs/${date}`);
}

export function upsertCycleDailyLog(date: string, data: UpsertCycleDailyLogRequest) {
  return request.put<ApiResult<CycleDailyLog>>(`/api/cycle-care/daily-logs/${date}`, data);
}

export function getCycleWarnings() {
  return request.get<ApiResult<CycleWarning[]>>('/api/cycle-care/warnings');
}

export function dismissCycleWarning(warningId: number) {
  return request.patch<ApiResult<void>>(`/api/cycle-care/warnings/${warningId}/dismiss`);
}
