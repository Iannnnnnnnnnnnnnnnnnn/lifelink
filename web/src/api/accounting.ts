import { ApiResult, request } from './request';

export type AccountBookType = 'PERSONAL' | 'RELATIONSHIP';
export type TransactionType = 'INCOME' | 'EXPENSE';

export interface AccountBook {
  id: number;
  relationshipId?: number;
  relationshipName?: string;
  ownerId: number;
  name: string;
  type: AccountBookType;
  status: string;
  createdAt: string;
}

export interface CreateAccountBookRequest {
  name: string;
  type: AccountBookType;
  relationshipId?: number;
}

export interface TransactionCategory {
  id: number;
  name: string;
  type: TransactionType;
  icon?: string;
  sortOrder: number;
}

export interface CreateTransactionRequest {
  accountBookId: number;
  type: TransactionType;
  amount: number;
  categoryId?: number;
  title: string;
  note?: string;
  transactionTime: string;
}

export type UpdateTransactionRequest = Omit<CreateTransactionRequest, 'accountBookId'>;

export interface Transaction {
  id: number;
  accountBookId: number;
  accountBookName: string;
  relationshipId?: number;
  userId: number;
  username: string;
  type: TransactionType;
  amount: number;
  categoryId?: number;
  categoryName?: string;
  title: string;
  note?: string;
  transactionTime: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionQuery {
  accountBookId?: number;
  relationshipId?: number;
  type?: TransactionType;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export interface MonthlyFinanceSummary {
  totalIncome: number;
  totalExpense: number;
  balance: number;
}

export interface CategoryStatistic {
  categoryName: string;
  amount: number;
  percentage: number;
}

export function getAccountBooks() {
  return request.get<ApiResult<AccountBook[]>>('/api/account-books');
}

export function createAccountBook(data: CreateAccountBookRequest) {
  return request.post<ApiResult<AccountBook>>('/api/account-books', data);
}

export function getTransactionCategories(type?: TransactionType) {
  return request.get<ApiResult<TransactionCategory[]>>('/api/transaction-categories', { params: { type } });
}

export function createTransaction(data: CreateTransactionRequest) {
  return request.post<ApiResult<Transaction>>('/api/transactions', data);
}

export function getTransactions(params: TransactionQuery = {}) {
  return request.get<ApiResult<Transaction[]>>('/api/transactions', { params });
}

export function updateTransaction(id: number, data: UpdateTransactionRequest) {
  return request.put<ApiResult<Transaction>>(`/api/transactions/${id}`, data);
}

export function deleteTransaction(id: number) {
  return request.delete<ApiResult<void>>(`/api/transactions/${id}`);
}

export function getMonthlyFinanceSummary(params: Pick<TransactionQuery, 'accountBookId' | 'relationshipId'> & { month?: string }) {
  return request.get<ApiResult<MonthlyFinanceSummary>>('/api/statistics/finance/monthly', { params });
}

export function getCategoryFinanceStatistic(params: Pick<TransactionQuery, 'accountBookId' | 'relationshipId' | 'type'> & { month?: string }) {
  return request.get<ApiResult<CategoryStatistic[]>>('/api/statistics/finance/category', { params });
}
