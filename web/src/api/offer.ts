import { ApiResult, request } from './request';

export type OfferQuestionType = 'THEORY' | 'ALGORITHM';
export type OfferDifficulty = 'EASY' | 'MEDIUM' | 'HARD';

export interface OfferAccess {
  canManage: boolean;
}

export interface OfferQuestionBank {
  id: number;
  name: string;
  code: string;
  sort: number;
}

export interface OfferCategory {
  id: number;
  bankId: number;
  name: string;
  type: OfferQuestionType;
  sort: number;
}

export interface OfferQuestion {
  id: number;
  title: string;
  type: OfferQuestionType;
  bankId: number;
  categoryId: number;
  difficulty: OfferDifficulty;
  content: string;
  answer: string;
  source?: string;
  remark?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OfferQuestionPage {
  records: OfferQuestion[];
  total: number;
  page: number;
  size: number;
}

export interface OfferStatistics {
  total: number;
  theory: number;
  algorithm: number;
  categories: number;
}

export interface OfferQuestionRequest {
  title: string;
  type: OfferQuestionType;
  bankId: number;
  categoryId: number;
  difficulty: OfferDifficulty;
  content: string;
  answer: string;
  source?: string;
  remark?: string;
}

export function getOfferAccess() {
  return request.get<ApiResult<OfferAccess>>('/api/offer/access');
}

export function getOfferQuestionBanks() {
  return request.get<ApiResult<OfferQuestionBank[]>>('/api/offer/question-banks');
}

export function getOfferCategories(params?: { bankId?: number; type?: OfferQuestionType }) {
  return request.get<ApiResult<OfferCategory[]>>('/api/offer/categories', { params });
}

export function getOfferQuestions(params?: {
  bankId?: number;
  type?: OfferQuestionType;
  categoryId?: number;
  difficulty?: OfferDifficulty;
  keyword?: string;
  page?: number;
  size?: number;
}) {
  return request.get<ApiResult<OfferQuestionPage>>('/api/offer/questions', { params });
}

export function getOfferStatistics() {
  return request.get<ApiResult<OfferStatistics>>('/api/offer/statistics');
}

export function createOfferQuestion(data: OfferQuestionRequest) {
  return request.post<ApiResult<OfferQuestion>>('/api/offer/questions', data);
}

export function updateOfferQuestion(id: number, data: OfferQuestionRequest) {
  return request.put<ApiResult<OfferQuestion>>(`/api/offer/questions/${id}`, data);
}

export function deleteOfferQuestion(id: number) {
  return request.delete<ApiResult<void>>(`/api/offer/questions/${id}`);
}
