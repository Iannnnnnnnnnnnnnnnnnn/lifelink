import { ApiResult, request } from './request';

export type RewardStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'SOLD_OUT';
export type LedgerType = 'EARN' | 'SPEND' | 'ADJUST' | 'REFUND';

export interface CoinAccount {
  balance: number;
  totalEarned: number;
  totalSpent: number;
}

export interface CoinLedger {
  id: number;
  changeAmount: number;
  balanceAfter: number;
  type: LedgerType;
  sourceType: string;
  sourceId?: number;
  title?: string;
  description?: string;
  createdAt: string;
}

export interface Reward {
  id: number;
  title: string;
  description?: string;
  coverObjectKey?: string;
  coverUrl?: string;
  coinCost: number;
  stock?: number | null;
  redeemedCount: number;
  status: RewardStatus;
  sortOrder: number;
  available: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RewardRequest {
  title: string;
  description?: string;
  coverObjectKey?: string;
  coverUrl?: string;
  coinCost: number;
  stock?: number | null;
  status: RewardStatus;
  sortOrder: number;
}

export interface RewardRedemption {
  id: number;
  rewardId: number;
  coinCostSnapshot: number;
  rewardTitleSnapshot: string;
  rewardDescriptionSnapshot?: string;
  rewardCoverUrlSnapshot?: string;
  status: string;
  note?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RewardRedeemResponse {
  redemption: RewardRedemption;
  balance: number;
}

export function getCoinAccount() {
  return request.get<ApiResult<CoinAccount>>('/api/coins/me');
}

export function getCoinLedger(params: { startDate?: string; endDate?: string; page?: number; pageSize?: number } = {}) {
  return request.get<ApiResult<CoinLedger[]>>('/api/coins/ledger', { params });
}

export function getRewards(params: { status?: RewardStatus; keyword?: string; sortBy?: string; sortDirection?: string; page?: number; pageSize?: number } = {}) {
  return request.get<ApiResult<Reward[]>>('/api/rewards', { params });
}

export function redeemReward(id: number) {
  return request.post<ApiResult<RewardRedeemResponse>>(`/api/rewards/${id}/redeem`);
}

export function getMyRedemptions(params: { page?: number; pageSize?: number } = {}) {
  return request.get<ApiResult<RewardRedemption[]>>('/api/rewards/redemptions/me', { params });
}

export function getRewardAdminAccess() {
  return request.get<ApiResult<{ enabled: boolean }>>('/api/rewards/admin/access');
}

export function createReward(data: RewardRequest) {
  return request.post<ApiResult<Reward>>('/api/rewards/admin', data);
}

export function updateReward(id: number, data: RewardRequest) {
  return request.put<ApiResult<Reward>>(`/api/rewards/admin/${id}`, data);
}

export function deactivateReward(id: number) {
  return request.delete<ApiResult<void>>(`/api/rewards/admin/${id}`);
}

export function uploadRewardCover(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<ApiResult<{ coverUrl: string; objectKey: string }>>('/api/rewards/admin/upload-cover', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}
