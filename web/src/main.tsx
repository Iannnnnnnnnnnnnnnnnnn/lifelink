import React from 'react';
import ReactDOM from 'react-dom/client';
import { ConfigProvider } from 'antd';
import enUS from 'antd/locale/en_US';
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/en';
import 'dayjs/locale/zh-cn';
import { RouterProvider } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { router } from './router';
import './i18n';
import './styles.css';
import './styles/theme.css';
import './styles/anniversary.css';
import './styles/activity.css';
import './styles/dashboard.css';
import './styles/daily-interaction.css';
import './styles/search.css';
import './styles/timeline.css';
import './styles/calendar.css';
import './styles/cycle.css';
import './styles/philosophy.css';
import './styles/animations.css';
import './styles/stickers.css';
import './styles/ui-polish.css';
import './styles/responsive.css';
import './styles/profile.css';

function AppProviders() {
  const { i18n } = useTranslation();
  const isEnglish = i18n.resolvedLanguage === 'en-US';
  dayjs.locale(isEnglish ? 'en' : 'zh-cn');

  return (
    <ConfigProvider
      locale={isEnglish ? enUS : zhCN}
      theme={{
        token: {
          colorPrimary: '#1677ff',
          borderRadius: 6,
        },
      }}
    >
      <RouterProvider router={router} />
    </ConfigProvider>
  );
}

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <AppProviders />
  </React.StrictMode>,
);
