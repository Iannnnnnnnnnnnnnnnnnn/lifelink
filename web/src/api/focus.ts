import { ApiResult, request } from './request';

export type FocusPhase = 'FOCUS' | 'SHORT_BREAK' | 'LONG_BREAK';
export type FocusSessionStatus = 'RUNNING' | 'PAUSED' | 'COMPLETED' | 'ABANDONED' | 'EXPIRED';
export type FocusRoomStatus = 'WAITING' | 'RUNNING' | 'COMPLETED' | 'CANCELLED' | 'EXPIRED';

export interface FocusSettings {
  id: number;
  focusMinutes: number;
  shortBreakMinutes: number;
  longBreakMinutes: number;
  longBreakInterval: number;
  autoStartBreak: boolean;
  autoStartNextFocus: boolean;
  soundEnabled: boolean;
  notificationEnabled: boolean;
  strictModeEnabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export type UpdateFocusSettingsRequest = Partial<Omit<FocusSettings, 'id' | 'createdAt' | 'updatedAt'>>;

export interface StartFocusSessionRequest {
  spaceId?: number;
  todoId?: number;
  roomId?: number;
  phase?: FocusPhase;
  plannedMinutes?: number;
  source?: string;
  note?: string;
}

export interface FocusSession {
  sessionId: number;
  userId: number;
  spaceId?: number;
  spaceName?: string;
  todoId?: number;
  todoTitle?: string;
  roomId?: number;
  sessionType: 'PERSONAL' | 'SPACE' | 'COUPLE';
  phase: FocusPhase;
  plannedMinutes: number;
  actualMinutes: number;
  pausedSeconds: number;
  status: FocusSessionStatus;
  source: string;
  note?: string;
  coinsAwarded?: number;
  coinsAwardedAt?: string;
  startedAt: string;
  endedAt?: string;
  expectedEndAt: string;
  remainingSeconds: number;
  createdAt: string;
  updatedAt: string;
}

export interface FocusStats {
  totalFocusMinutes: number;
  completedPomodoros: number;
  abandonedPomodoros: number;
  completionRate: number;
  currentStreak: number;
  weekFocusMinutes: number;
  topTodos: Array<{
    todoId: number;
    todoTitle?: string;
    focusMinutes: number;
    sessionsCount: number;
  }>;
  dailyTrend: Array<{
    date: string;
    focusMinutes: number;
    completedPomodoros: number;
  }>;
}

export interface FocusRoomMember {
  userId: number;
  username?: string;
  avatarUrl?: string;
  memberStatus: 'INVITED' | 'JOINED' | 'DECLINED' | 'FOCUSING' | 'PAUSED' | 'COMPLETED' | 'ABANDONED';
  joinedAt?: string;
  completedAt?: string;
}

export interface FocusRoom {
  id: number;
  creatorUserId: number;
  spaceId: number;
  spaceName?: string;
  title: string;
  plannedMinutes: number;
  status: FocusRoomStatus;
  startedAt?: string;
  endedAt?: string;
  expectedEndAt?: string;
  remainingSeconds: number;
  createdAt: string;
  updatedAt: string;
  members: FocusRoomMember[];
}

export interface CreateFocusRoomRequest {
  spaceId: number;
  title?: string;
  plannedMinutes?: number;
  inviteUserIds?: number[];
}

export function getFocusSettings() {
  return request.get<ApiResult<FocusSettings>>('/api/focus/settings');
}

export function updateFocusSettings(data: UpdateFocusSettingsRequest) {
  return request.put<ApiResult<FocusSettings>>('/api/focus/settings', data);
}

export function startFocusSession(data: StartFocusSessionRequest) {
  return request.post<ApiResult<FocusSession>>('/api/focus/sessions/start', data);
}

export function pauseFocusSession(id: number) {
  return request.post<ApiResult<FocusSession>>(`/api/focus/sessions/${id}/pause`);
}

export function resumeFocusSession(id: number) {
  return request.post<ApiResult<FocusSession>>(`/api/focus/sessions/${id}/resume`);
}

export function completeFocusSession(id: number) {
  return request.post<ApiResult<FocusSession>>(`/api/focus/sessions/${id}/complete`);
}

export function abandonFocusSession(id: number) {
  return request.post<ApiResult<FocusSession>>(`/api/focus/sessions/${id}/abandon`);
}

export function getCurrentFocusSession() {
  return request.get<ApiResult<FocusSession | null>>('/api/focus/sessions/current');
}

export function getFocusSessions(params: { startDate?: string; endDate?: string } = {}) {
  return request.get<ApiResult<FocusSession[]>>('/api/focus/sessions', { params });
}

export function getTodayFocusStats() {
  return request.get<ApiResult<FocusStats>>('/api/focus/stats/today');
}

export function createFocusRoom(data: CreateFocusRoomRequest) {
  return request.post<ApiResult<FocusRoom>>('/api/focus/rooms', data);
}

export function getFocusRoom(id: number) {
  return request.get<ApiResult<FocusRoom>>(`/api/focus/rooms/${id}`);
}

export function joinFocusRoom(id: number) {
  return request.post<ApiResult<FocusRoom>>(`/api/focus/rooms/${id}/join`);
}

export function declineFocusRoom(id: number) {
  return request.post<ApiResult<FocusRoom>>(`/api/focus/rooms/${id}/decline`);
}

export function startFocusRoom(id: number) {
  return request.post<ApiResult<FocusRoom>>(`/api/focus/rooms/${id}/start`);
}

export function completeFocusRoom(id: number) {
  return request.post<ApiResult<FocusRoom>>(`/api/focus/rooms/${id}/complete`);
}

export function abandonFocusRoom(id: number) {
  return request.post<ApiResult<FocusRoom>>(`/api/focus/rooms/${id}/abandon`);
}

export function getCurrentFocusRoom() {
  return request.get<ApiResult<FocusRoom | null>>('/api/focus/rooms/current');
}
