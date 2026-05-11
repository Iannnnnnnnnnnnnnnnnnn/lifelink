import { ApiResult, request } from './request';

export type TodoPriority = 'LOW' | 'NORMAL' | 'HIGH';
export type TodoStatus = 'TODO' | 'DONE' | 'DELETED';

export interface SpaceTodo {
  id: number;
  relationshipId: number;
  title: string;
  content?: string;
  priority: TodoPriority;
  status: TodoStatus;
  dueTime?: string;
  createdBy: number;
  createdByUsername?: string;
  updatedBy?: number;
  completedBy?: number;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSpaceTodoRequest {
  title: string;
  content?: string;
  priority?: TodoPriority;
  dueTime?: string;
}

export interface UpdateSpaceTodoRequest extends CreateSpaceTodoRequest {}

export interface GetSpaceTodosParams {
  status?: 'TODO' | 'DONE';
  keyword?: string;
  page?: number;
  size?: number;
}

export function createSpaceTodo(relationshipId: number, data: CreateSpaceTodoRequest) {
  return request.post<ApiResult<SpaceTodo>>(`/api/relationships/${relationshipId}/todos`, data);
}

export function getSpaceTodos(relationshipId: number, params: GetSpaceTodosParams = {}) {
  return request.get<ApiResult<SpaceTodo[]>>(`/api/relationships/${relationshipId}/todos`, { params });
}

export function getSpaceTodoDetail(relationshipId: number, todoId: number) {
  return request.get<ApiResult<SpaceTodo>>(`/api/relationships/${relationshipId}/todos/${todoId}`);
}

export function updateSpaceTodo(relationshipId: number, todoId: number, data: UpdateSpaceTodoRequest) {
  return request.put<ApiResult<SpaceTodo>>(`/api/relationships/${relationshipId}/todos/${todoId}`, data);
}

export function toggleSpaceTodo(relationshipId: number, todoId: number) {
  return request.patch<ApiResult<SpaceTodo>>(`/api/relationships/${relationshipId}/todos/${todoId}/toggle`);
}

export function deleteSpaceTodo(relationshipId: number, todoId: number) {
  return request.delete<ApiResult<void>>(`/api/relationships/${relationshipId}/todos/${todoId}`);
}
