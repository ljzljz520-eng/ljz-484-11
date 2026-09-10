# 开发指南 (Development)

## 1. 环境依赖
- Java 17+
- Node.js 20+
- Maven 3.9+
- Docker & Docker Compose

## 2. 目录结构
```text
novel-system/
├── novel-backend/          # 后端项目 (Maven)
│   ├── src/main/java/com/novel/
│   │   ├── controller/    # 接口定义
│   │   ├── model/         # 数据实体
│   │   └── repository/    # 数据存储逻辑
│   └── Dockerfile
├── novel-frontend/         # 前端项目 (Vite + Vue)
│   ├── src/
│   │   ├── views/         # 页面组件
│   │   ├── assets/        # 全局样式与资源
│   │   └── router/        # 路由配置
│   └── Dockerfile
└── docker-compose.yml
```

## 3. 本地开发启动

### 后端启动:
```bash
cd novel-backend
mvn spring-boot:run
```
访问 Swagger 调试: `http://localhost:8080/swagger-ui/index.html`

### 前端启动:
```bash
cd novel-frontend
npm install
npm run dev
```
访问地址: `http://localhost:3000`

## 4. 关键配置
- **前端 API 地址**: 前端默认使用相对路径 `/api` 请求后端（见 `src/api/index.js`），
  本地开发由 `vite.config.js` 的代理转发到 `http://localhost:8080`，
  生产环境由 `nginx.conf` 将 `/api/` 代理到后端容器，因此远程访问部署页面时无需任何额外配置。
  如需直连其他后端地址，可通过 `VITE_API_URL` 环境变量覆盖。
- **作者身份**: 作者后台接口通过 `X-Author-Id` 请求头识别作者（前端自动生成并持久化在 localStorage），
  不同作者的小说与草稿相互隔离。
- **Docker 镜像**: 后端使用 `eclipse-temurin:17-jre` 基础镜像，前端基于 `nginx:alpine`。

## 5. 发布流程
1. 提交代码至仓库。
2. 确保 `package-lock.json` 已更新。
3. 运行 `docker compose up --build` 进行全量构建。
 Riverside, CA
