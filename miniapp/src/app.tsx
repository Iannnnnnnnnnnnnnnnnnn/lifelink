import { useLaunch } from '@tarojs/taro';
import type React from 'react';
import './app.scss';

function App(props: { children: React.ReactNode }) {
  useLaunch(() => {
    // Auth state is restored lazily by pages because Taro storage is synchronous.
  });

  return props.children;
}

export default App;
