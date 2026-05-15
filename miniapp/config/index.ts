import { defineConfig } from '@tarojs/cli';

export default defineConfig(async (merge) => {
  const baseConfig = {
    projectName: 'lifelink-miniapp',
    date: '2026-05-15',
    designWidth: 750,
    deviceRatio: {
      640: 2.34 / 2,
      750: 1,
      828: 1.81 / 2
    },
    sourceRoot: 'src',
    outputRoot: 'dist',
    framework: 'react',
    compiler: 'webpack5',
    mini: {
      postcss: {
        pxtransform: {
          enable: true,
          config: {}
        },
        cssModules: {
          enable: false
        }
      }
    },
    h5: {}
  };

  if (process.env.NODE_ENV === 'production') {
    const prod = await import('./prod');
    return merge({}, baseConfig, prod.default);
  }

  const dev = await import('./dev');
  return merge({}, baseConfig, dev.default);
});
