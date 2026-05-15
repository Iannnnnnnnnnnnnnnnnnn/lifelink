import { http } from './request';

export type TodoPriority = 'LOW' | 'NORMAL' | 'HIGH';
export type TodoStatus = 'TODO' | 'DONE' | 'DELETED';

export interface SpaceTodo {
  id: number;
  relationshipId: number;
  title: string;
  content?: string;
  priority?: TodoPriority;
  status: TodoStatus;
  dueTime?: string;
  createdByUsername?: string;
  completedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface TodoQuery {
  status?: 'TODO' | 'DONE';
  keyword?: string;
  page?: number;
  size?: number;
}

export interface CreateTodoRequest {
  title: string;
  content?: string;
  priority?: TodoPriority;
  dueTime?: string;
}

export function getTodos(relationshipId: number | string, params: TodoQuery = {}) {
  return http.get<SpaceTodo[]>(`/api/relationships/${relationshipId}/todos`, params as Record<string, unknown>);
}

export function createTodo(relationshipId: number | string, data: CreateTodoRequest) {
  return http.post<SpaceTodo>(`/api/relationships/${relationshipId}/todos`, data);
}

export function toggleTodo(relationshipId: number | string, todoId: number) {
  return http.patch<SpaceTodo>(`/api/relationships/${relationshipId}/todos/${todoId}/toggle`);
}

export function deleteTodo(relationshipId: number | string, todoId: number) {
  return http.delete<void>(`/api/relationships/${relationshipId}/todos/${todoId}`);
}
