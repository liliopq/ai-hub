# AI Hub 社区平台

一个基于 Spring Boot 3 + Vue 3 的 AI 社区平台，支持帖子发布、评论、点赞、收藏、AI 对话等功能。

## 技术栈

### 后端
- Spring Boot 3.2.5
- MyBatis-Plus 3.5.7
- MySQL 8.0
- Redis
- Spring Security + JWT
- Spring AI (DeepSeek)
- 阿里云 OSS

### 前端
- Vue 3
- Vite
- Vue Router
- Axios
- Tailwind CSS
- Lucide Icons

## 环境要求

- JDK 21+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

## 快速开始

### 1. 数据库准备

创建 MySQL 数据库：
```sql
CREATE DATABASE ai_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 后端配置

#### 2.1 复制环境变量模板
```bash
cd backend
cp .env.example .env
```

#### 2.2 配置环境变量

编辑 `.env` 文件，填入你的配置：

```bash
# 数据库配置
DB_USERNAME=root
DB_PASSWORD=your_database_password

# 阿里云OSS配置
ALIYUN_OSS_ACCESS_KEY_ID=your_access_key_id
ALIYUN_OSS_ACCESS_KEY_SECRET=your_access_key_secret
ALIYUN_OSS_BUCKET_NAME=your_bucket_name
ALIYUN_OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com

# JWT密钥（至少32位字符）
JWT_SECRET=your_jwt_secret_at_least_32_characters

# AI API Key（DeepSeek）
AI_API_KEY=your_deepseek_api_key
```

**重要提示：**
- `JWT_SECRET` 必须是至少 32 位的字符串
- `AI_API_KEY` 需要从 DeepSeek 官方获取
- OSS 配置需要从阿里云控制台获取

#### 2.3 启动后端

使用启动脚本（推荐）：
```bash
./start.sh
```

或者使用 Maven：
```bash
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 3. 前端配置

#### 3.1 安装依赖
```bash
cd frontend
npm install
```

#### 3.2 配置环境变量（可选）

开发环境默认使用 `http://localhost:8080/api`，如需修改可创建 `.env` 文件：

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

生产环境配置 `.env.production`：
```bash
VITE_API_BASE_URL=https://your-api-domain.com/api
```

#### 3.3 启动前端

开发模式：
```bash
npm run dev
```

构建生产版本：
```bash
npm run build
```

前端将在 http://localhost:5173 启动

## 项目结构

```
ai-hub/
├── backend/                    # 后端项目
│   ├── src/main/java/
│   │   └── com/ai_hub/
│   │       ├── config/        # 配置类
│   │       ├── controller/    # 控制器
│   │       ├── dto/           # 数据传输对象
│   │       ├── entity/        # 实体类
│   │       ├── mapper/        # MyBatis Mapper
│   │       ├── security/      # 安全配置
│   │       ├── service/       # 服务层
│   │       └── utils/         # 工具类
│   ├── src/main/resources/
│   │   └── application.yaml   # 应用配置
│   ├── .env                   # 环境变量（不提交到Git）
│   ├── .env.example          # 环境变量模板
│   └── pom.xml
│
└── frontend/                   # 前端项目
    ├── src/
    │   ├── api/               # API 接口
    │   ├── components/        # 组件
    │   ├── router/            # 路由配置
    │   ├── utils/             # 工具函数
    │   └── views/             # 页面视图
    ├── .env                   # 环境变量（不提交到Git）
    ├── .env.production       # 生产环境变量
    └── package.json
```

## 主要功能

- ✅ 用户注册/登录（JWT认证）
- ✅ 帖子发布、编辑、删除
- ✅ 帖子列表（分页、分类筛选、排序）
- ✅ 帖子详情（浏览量统计）
- ✅ 评论系统（树形结构）
- ✅ 点赞/取消点赞
- ✅ 收藏/取消收藏
- ✅ 用户个人中心
- ✅ 通知系统（WebSocket实时推送）
- ✅ AI 智能对话（DeepSeek）
- ✅ 文件上传（阿里云OSS）
- ✅ 管理员后台

## 常见问题

### 1. 启动时报错：Could not resolve placeholder 'JWT_SECRET'

**原因：** 环境变量未正确加载

**解决方案：**
- 确保 `.env` 文件存在且包含 `JWT_SECRET` 配置
- 使用 `./start.sh` 脚本启动，它会自动加载环境变量
- 或者在 IDE 中配置环境变量

### 2. 数据库连接失败

**检查项：**
- MySQL 是否已启动
- 数据库 `ai_hub` 是否已创建
- `.env` 中的数据库用户名和密码是否正确

### 3. Redis 连接失败

**检查项：**
- Redis 是否已启动
- Redis 配置是否正确（默认 localhost:6379）

### 4. 前端无法访问后端 API

**检查项：**
- 后端是否在 8080 端口运行
- 前端 `request.js` 中的 baseURL 是否正确
- 浏览器控制台是否有 CORS 错误

## 安全提示

⚠️ **生产环境部署前请务必：**

1. 修改所有默认密码和密钥
2. 使用强密码和长密钥（JWT_SECRET 至少 32 位）
3. 配置 HTTPS
4. 限制 CORS 允许的域名
5. 不要将 `.env` 文件提交到版本控制
6. 定期更新依赖包

## 许可证

MIT License

## 联系方式

如有问题，请提交 Issue 或联系开发者。
