# LifeLink Web

LifeLink Web 是 LifeLink 的前端项目，基于 React、Vite、TypeScript 和 Ant Design 构建。

## 技术栈

- React 19
- Vite 7
- TypeScript
- Ant Design
- React Router
- Zustand

## 本地开发

安装依赖：

```bash
npm install
```

启动开发服务：

```bash
npm run dev
```

默认访问地址：

```text
http://localhost:5173
```

## 环境变量

前端请求后端接口时读取 `VITE_API_BASE_URL`。

- 留空：使用同源 `/api`，适合容器化部署或 Nginx 反向代理。
- 指定地址：例如 `http://localhost:8081`，适合本地前后端分离调试。

## 构建

```bash
npm run build
```
