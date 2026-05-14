import { Popconfirm } from 'antd';
import type { ReactElement } from 'react';
import { useTranslation } from 'react-i18next';

interface ConfirmActionProps {
  title: string;
  children: ReactElement;
  onConfirm: () => void | Promise<void>;
  okText?: string;
  cancelText?: string;
}

export function ConfirmAction({ title, children, onConfirm, okText, cancelText }: ConfirmActionProps) {
  const { t } = useTranslation();

  return (
    <Popconfirm
      title={title}
      okText={okText || t('common.confirm')}
      cancelText={cancelText || t('common.cancel')}
      okButtonProps={{ danger: true }}
      onConfirm={onConfirm}
    >
      {children}
    </Popconfirm>
  );
}
