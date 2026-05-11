import { ApiResult, request } from './request';

export function getHealth() {
  return request.get<ApiResult<string>>('/api/health');
}
