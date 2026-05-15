# LifeLink Miniapp

LifeLink 微信小程序端，基于 Taro + React + TypeScript 实现。第一版重点覆盖移动端高频功能，不迁移 Web 的复杂后台式能力。

## 技术栈

- Taro
- React
- TypeScript
- Zustand
- Taro.request
- Taro.uploadFile
- 自定义轻量样式组件

## 安装依赖

```bash
cd miniapp
npm install
```

## 开发运行

```bash
npm run dev:weapp
```

然后用微信开发者工具打开 `miniapp/dist`。

## 生产构建

```bash
npm run build:weapp
```

## 后端 API 地址

开发和生产 API 地址在 `config/dev.ts` 与 `config/prod.ts` 中配置，也可以通过环境变量覆盖：

```bash
TARO_APP_API_BASE_URL=http://192.168.1.100:8081 npm run dev:weapp
```

PowerShell 示例：

```powershell
$env:TARO_APP_API_BASE_URL="http://192.168.1.100:8081"
npm run dev:weapp
```

注意：

- 微信开发者工具里的 `localhost` 不一定等于本机后端地址，开发时建议使用局域网 IP。
- 正式上线必须使用 HTTPS 域名，并在微信公众平台配置 request/uploadFile 合法域名。
- 小程序端不保存任何生产密钥。

## 微信登录接口

小程序端已预留：

```text
POST /api/auth/wechat-login
```

请求：

```json
{
  "code": "wx.login 返回的 code"
}
```

期望返回：

```json
{
  "token": "JWT",
  "user": {
    "id": 1,
    "username": "Alice"
  }
}
```

当前后端如果尚未实现该接口，登录页会按预期提示失败；其他页面结构和 API 封装已经就绪。

## 第一版已实现页面

- `pages/login/index`：微信登录页
- `pages/home/index`：首页 Dashboard
- `pages/relationships/index`：关系空间列表
- `pages/relationships/detail`：关系空间详情
- `pages/daily/index`：日常时间线
- `pages/daily/create`：发布日常与图片上传
- `pages/todos/index`：空间代办
- `pages/anniversaries/index`：纪念日列表
- `pages/notifications/index`：通知中心
- `pages/profile/index`：我的页面

## 已复用后端接口

- `GET /api/user/me`
- `GET /api/relationships`
- `GET /api/relationships/{id}`
- `GET /api/relationships/{id}/members`
- `GET /api/daily-posts`
- `POST /api/daily-posts`
- `POST /api/daily-posts/{id}/like`
- `DELETE /api/daily-posts/{id}/like`
- `POST /api/files/upload`
- `GET /api/relationships/{relationshipId}/todos`
- `POST /api/relationships/{relationshipId}/todos`
- `PATCH /api/relationships/{relationshipId}/todos/{todoId}/toggle`
- `DELETE /api/relationships/{relationshipId}/todos/{todoId}`
- `GET /api/anniversaries`
- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `PATCH /api/notifications/{id}/read`
- `PATCH /api/notifications/read-all`

## 暂未完整迁移的 Web 功能

- 复杂成员管理
- 全局搜索
- 记账复杂统计
- 空间动态完整筛选
- 关系成长时间轴完整编辑
- 日常评论详情页
- 关系空间创建 / 加入的完整小程序表单

这些功能可以在后续小程序迭代中按移动端使用频率逐步补齐。

## 主题适配

登录后小程序会调用 `GET /api/relationships` 判断是否存在 `ACTIVE` 的 `COUPLE` 空间：

- 存在：使用彩色主题
- 不存在：使用低饱和灰色主题

不会向用户显示任何主题提示文字。

## 测试建议

1. `npm install`
2. `npm run dev:weapp`
3. 用微信开发者工具打开 `miniapp/dist`
4. 配置 `TARO_APP_API_BASE_URL`
5. 打开登录页，确认微信登录请求发往 `/api/auth/wechat-login`
6. 后端支持微信登录后，确认登录成功进入首页
7. 检查首页、关系空间、日常、发布日常、代办、纪念日、通知、我的页面
8. 使用发布日常页选择并上传图片
9. 未登录访问受保护页面应跳转登录页
10. 有 / 无 `ACTIVE COUPLE` 空间时，页面分别呈现彩色 / 灰色低饱和主题
