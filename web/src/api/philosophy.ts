import { ApiResult, request } from './request';

export interface Philosopher {
  code: string;
  name: string;
  nameZh: string;
  nameEn: string;
  era: string;
  eraZh: string;
  eraEn: string;
  description: string;
  descriptionZh: string;
  descriptionEn: string;
  avatarUrl?: string;
  tags: string[];
  sortOrder: number;
}

export interface PhilosophyResponseItem {
  philosopherCode: string;
  philosopherName: string;
  viewpoint: string;
  questionBack: string;
  objection: string;
  summary: string;
  rawResponse?: string;
}

export interface PhilosophySession {
  id: number;
  question: string;
  language: 'zh-CN' | 'en-US';
  responses: PhilosophyResponseItem[];
  createdAt: string;
}

export interface CreatePhilosophySessionRequest {
  question: string;
  philosopherCodes: string[];
  language: 'zh-CN' | 'en-US';
}

export interface PhilosophySessionQuery {
  page?: number;
  size?: number;
}

export function getPhilosophers(language?: string) {
  return request.get<ApiResult<Philosopher[]>>('/api/philosophy/philosophers', { params: { language } });
}

export function createPhilosophySession(data: CreatePhilosophySessionRequest) {
  return request.post<ApiResult<PhilosophySession>>('/api/philosophy/sessions', data, { timeout: 90000 });
}

export function getPhilosophySessions(params?: PhilosophySessionQuery) {
  return request.get<ApiResult<PhilosophySession[]>>('/api/philosophy/sessions', { params });
}

export function getPhilosophySessionDetail(id: number) {
  return request.get<ApiResult<PhilosophySession>>(`/api/philosophy/sessions/${id}`);
}

export function deletePhilosophySession(id: number) {
  return request.delete<ApiResult<void>>(`/api/philosophy/sessions/${id}`);
}
