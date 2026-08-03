import { ApiResult, request } from './request';

export interface MemoryReference {
  sourceType: string;
  sourceId: number;
  content: string;
}

export interface MemoryAiChatResponse {
  answer: string;
  references: MemoryReference[];
}

export interface MemoryBuildResponse {
  sourceCount: number;
  chunkCount: number;
}

export function buildMemoryKnowledgeBase(spaceId: number) {
  return request.post<ApiResult<MemoryBuildResponse>>(`/api/ai/memory/build/${spaceId}`);
}

export function askMemoryAssistant(spaceId: number, question: string) {
  return request.post<ApiResult<MemoryAiChatResponse>>('/api/ai/chat', { spaceId, question });
}
