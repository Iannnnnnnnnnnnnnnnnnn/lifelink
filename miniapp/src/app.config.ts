export default defineAppConfig({
  pages: [
    'pages/login/index',
    'pages/home/index',
    'pages/relationships/index',
    'pages/relationships/detail',
    'pages/daily/index',
    'pages/daily/create',
    'pages/todos/index',
    'pages/anniversaries/index',
    'pages/notifications/index',
    'pages/profile/index'
  ],
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#fff7f4',
    navigationBarTitleText: 'LifeLink',
    navigationBarTextStyle: 'black',
    backgroundColor: '#fff7f4'
  },
  tabBar: {
    color: '#6b7280',
    selectedColor: '#e76f8a',
    backgroundColor: '#ffffff',
    borderStyle: 'white',
    list: [
      {
        pagePath: 'pages/home/index',
        text: '首页'
      },
      {
        pagePath: 'pages/relationships/index',
        text: '关系'
      },
      {
        pagePath: 'pages/daily/index',
        text: '日常'
      },
      {
        pagePath: 'pages/anniversaries/index',
        text: '纪念日'
      },
      {
        pagePath: 'pages/profile/index',
        text: '我的'
      }
    ]
  }
});
