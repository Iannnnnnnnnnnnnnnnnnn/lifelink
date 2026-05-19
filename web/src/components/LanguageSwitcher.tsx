import { GlobalOutlined } from '@ant-design/icons';
import { Select } from 'antd';
import { useTranslation } from 'react-i18next';
import { LANGUAGE_STORAGE_KEY } from '../i18n';

export function LanguageSwitcher() {
  const { t, i18n } = useTranslation();

  const current = i18n.resolvedLanguage === 'en-US' ? 'en-US' : 'zh-CN';

  const handleChange = async (value: 'zh-CN' | 'en-US') => {
    await i18n.changeLanguage(value);
    localStorage.setItem(LANGUAGE_STORAGE_KEY, value);
  };

  return (
    <Select
      value={current}
      onChange={handleChange}
      prefix={<GlobalOutlined />}
      style={{ width: 130 }}
      options={[
        { value: 'zh-CN', label: t('language.zhCN') },
        { value: 'en-US', label: t('language.enUS') },
      ]}
    />
  );
}
