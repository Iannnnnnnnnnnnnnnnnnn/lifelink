export default {
  defineConstants: {
    API_BASE_URL: JSON.stringify(process.env.TARO_APP_API_BASE_URL || 'http://192.168.1.100:8081')
  },
  mini: {}
};
