import { ApiResult, request } from './request';

export interface UploadFileResponse {
  fileId: number;
  url: string;
  objectKey: string;
  originalName: string;
  contentType: string;
  fileSize: number;
}

export function uploadFile(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<ApiResult<UploadFileResponse>>('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}
