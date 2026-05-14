import type { TFunction } from 'i18next';
import type { AxiosError } from 'axios';

interface ErrorResponse {
  message?: string;
}

export type PageErrorType = '403' | '404' | '500' | 'network';

export function getErrorStatus(error: unknown) {
  return (error as AxiosError<ErrorResponse>).response?.status;
}

export function getPageErrorType(error: unknown): PageErrorType {
  const status = getErrorStatus(error);
  if (status === 403) return '403';
  if (status === 404) return '404';
  if (!status) return 'network';
  return '500';
}

export function getRequestErrorMessage(error: unknown, t: TFunction) {
  const axiosError = error as AxiosError<ErrorResponse>;
  const backendMessage = axiosError.response?.data?.message;
  if (backendMessage) {
    return backendMessage;
  }

  const status = axiosError.response?.status;
  if (status === 401) return t('error.unauthorized');
  if (status === 403) return t('error.forbidden');
  if (status === 404) return t('error.notFound');
  if (!status) return t('error.network');
  return t('error.server');
}

export function isRequestErrorHandled(error: unknown) {
  return Boolean((error as { __lifelinkHandled?: boolean }).__lifelinkHandled);
}
