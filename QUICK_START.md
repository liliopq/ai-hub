# 🚀 AI Hub 快速启动指南

本指南将帮助你在 5 分钟内启动 AI Hub 社区平台。

---

## 方式一：Docker 一键部署（推荐）

### 前置条件
- [Docker](https://docs.docker.com/get-docker/) 24.0+
- [Docker Compose](https://docs.docker.com/compose/install/) v2+

### 1. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env，至少填写 AI_API_KEY（DeepSeek 密钥）
vim .env
```

> 💡 如果只是本地测试，使用默认配置即可启动。但 JWT 密钥和数据库密码请务必在生产环境修改。

### 2. 一键启动

```bash
docker compose up -d --build
```

首次启动会自动：
- 拉取 MySQL 8.0、Redis 7、JDK 21、Node 20 镜像
- 构建后端（Maven 编译打包）和前端（Vite 构建）
- 初始化数据库表结构
- 创建默认管理员账号

### 3. 访问服务

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost |
| 后端 API | http://localhost:8080 |
| Swagger 文档 | http://localhost:8080/swagger-ui.html |
| MySQL（宿主机） | localhost:3307 |
| Redis（宿主机） | localhost:6380 |

### 4. 默认管理员账号

| 用户名 | 密码 |
|--------|------|
| admin | Admin@123456 |

> ⚠️ 首次登录后请立即修改密码！

### 5. 常用命令

```bash
# 查看运行状态
docker compose ps

# 查看后端日志
docker compose logs -f backend

# 查看前端日志
docker compose logs -f frontend

# 查看所有服务日志
docker compose logs -f

# 重启服务
docker compose restart

# 停止并删除容器（保留数据卷）
docker compose down

# 停止并删除容器 + 数据卷（重置数据）
docker compose down -v

# 重新构建并启动
docker compose up -d --build
```

---

## 方式二：本地开发环境

### 前置条件
- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 20+（仅前端开发需要）

### 1. 启动 MySQL 和 Redis

```bash
# 用 Docker 仅启动数据库和缓存
docker compose up -d mysql redis
```

或者使用本地安装的 MySQL/Redis。

### 2. 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ai_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构
mysql -u root -p ai_hub < docker/mysql/init.sql
```

### 3. 启动后端

```bash
cd backend

# 方式 A：使用启动脚本（自动加载 .env）
./start.sh

# 方式 B：使用 Maven
mvn spring-boot:run

# 后端运行在 http://localhost:8080
```

### 4. 启动前端（可选，开发模式）

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 前端运行在 http://localhost:5173
```

---

## 目录结构（Docker 相关）

```
ai-hub/
├── docker-compose.yml           # Docker 编排文件
├── .env.example                 # 环境变量模板 ➜ 复制为 .env
├── docker/
│   ├── .env.example             # Docker 环境变量模板（备用）
│   └── mysql/
│       └── init.sql             # 数据库初始化脚本
├── backend/
│   ├── Dockerfile               # 后端多阶段构建
│   └── .dockerignore
├── frontend/
│   ├── Dockerfile               # 前端多阶段构建（Node → Nginx）
│   ├── .dockerignore
│   └── docker/nginx/
│       └── default.conf         # Nginx 配置（反代 + WebSocket）
```

---

## 常见问题

### 端口被占用

```bash
# 检查端口占用
lsof -i :80
lsof -i :8080
lsof -i :3307
lsof -i :6380

# 修改 docker-compose.yml 中的端口映射
```

### 构建失败

```bash
# 清理缓存重新构建
docker compose build --no-cache

# 检查磁盘空间
docker system df
docker system prune -a  # 清理无用镜像和缓存
```

### 数据库连接失败

```bash
# 检查 MySQL 是否就绪
docker compose logs mysql

# 手动连接测试
mysql -h 127.0.0.1 -P 3307 -u root -p
```

### Token 突然失效

如果你修改了密码，所有已登录设备的 Token 会自动失效（Token 版本号机制），需要重新登录。这是正常的安全行为。

---

## 下一步

- 📖 查看 [OPTIMIZATION_GUIDE.md](./OPTIMIZATION_GUIDE.md) 了解性能优化细节
- 🔧 查看 [README.md](./README.md) 了解完整功能列表
