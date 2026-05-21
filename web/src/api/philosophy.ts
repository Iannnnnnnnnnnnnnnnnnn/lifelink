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
  roleType?: 'PHILOSOPHER' | 'COUNSELOR';
  responseLayout?: 'PHILOSOPHY_CARD' | 'COUNSELOR_CARD';
}

export interface PhilosophyResponseItem {
  philosopherCode: string;
  philosopherName: string;
  responseLayout?: 'PHILOSOPHY_CARD' | 'COUNSELOR_CARD';
  viewpoint?: string;
  questionBack?: string;
  objection?: string;
  summary?: string;
  understanding?: string;
  advice?: string;
  practice?: string;
  support?: string;
  rawResponse?: string;
}

export interface PhilosophySession {
  id: number;
  question: string;
  language: 'zh-CN' | 'en-US';
  responses: PhilosophyResponseItem[];
  createdAt: string;
}

export type PhilosophyChatRole = 'USER' | 'ASSISTANT' | 'SYSTEM';

export interface PhilosophyChatMessage {
  id: number;
  role: PhilosophyChatRole;
  content: string;
  createdAt: string;
}

export interface PhilosophyChatSession {
  id: number;
  philosopherCode: string;
  philosopherName: string;
  title: string;
  language: 'zh-CN' | 'en-US';
  lastMessagePreview?: string;
  lastMessageAt?: string;
  messageCount?: number;
  messages: PhilosophyChatMessage[];
  createdAt: string;
}

export interface SendPhilosophyChatMessageResponse {
  userMessage: PhilosophyChatMessage;
  assistantMessage: PhilosophyChatMessage;
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

export function createChatSession(data: { philosopherCode: string; language: 'zh-CN' | 'en-US' }) {
  return request.post<ApiResult<PhilosophyChatSession>>('/api/philosophy/chat/sessions', data);
}

export function getChatSessions(params?: PhilosophySessionQuery) {
  return request.get<ApiResult<PhilosophyChatSession[]>>('/api/philosophy/chat/sessions', { params });
}

export function getChatSessionDetail(sessionId: number) {
  return request.get<ApiResult<PhilosophyChatSession>>(`/api/philosophy/chat/sessions/${sessionId}`);
}

export function sendChatMessage(sessionId: number, data: { content: string }) {
  return request.post<ApiResult<SendPhilosophyChatMessageResponse>>(
    `/api/philosophy/chat/sessions/${sessionId}/messages`,
    data,
    { timeout: 90000 },
  );
}

export function deleteChatSession(sessionId: number) {
  return request.delete<ApiResult<void>>(`/api/philosophy/chat/sessions/${sessionId}`);
}

export function updateChatSessionTitle(sessionId: number, data: { title: string }) {
  return request.put<ApiResult<PhilosophyChatSession>>(`/api/philosophy/chat/sessions/${sessionId}/title`, data);
}
