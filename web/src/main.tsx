import React from 'react';
import ReactDOM from 'react-dom/client';
import { ConfigProvider } from 'antd';
import { RouterProvider } from 'react-router-dom';
import { router } from './router';
import './i18n';
import './styles.css';
import './styles/theme.css';
import './styles/anniversary.css';
import './styles/activity.css';
import './styles/dashboard.css';
import './styles/animations.css';
import './styles/stickers.css';
import './styles/ui-polish.css';

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#1677ff',
          borderRadius: 6,
        },
      }}
    >
      <RouterProvider router={router} />
    </ConfigProvider>
  </React.StrictMode>,
);
