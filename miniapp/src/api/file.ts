import Taro from '@tarojs/taro';
import { config } from '../config';
import { TOKEN_STORAGE_KEY } from '../store/authStore';
import type { ApiResult } from './request';

export interface UploadFileResponse {
  fileId: number;
  url: string;
  objectKey?: string;
  originalName?: string;
  contentType?: string;
  fileSize?: number;
}

export async function uploadFile(filePath: string) {
  const token = Taro.getStorageSync(TOKEN_STORAGE_KEY);
  const response = await Taro.uploadFile({
    url: `${config.apiBaseUrl}${config.uploadPath}`,
    filePath,
    name: 'file',
    header: token ? { Authorization: `Bearer ${token}` } : undefined
  });

  if (response.statusCode < 200 || response.statusCode >= 300) {
    throw new Error('图片上传失败');
  }

  const parsed = JSON.parse(response.data) as ApiResult<UploadFileResponse> | UploadFileResponse;
  return 'data' in parsed && 'code' in parsed ? parsed.data : parsed;
}
