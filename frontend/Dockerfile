# ============================================
# 阶段 1：Node 构建
# ============================================
FROM node:20-alpine AS builder

WORKDIR /build

# 先复制依赖清单，利用 Docker 缓存加速
COPY package.json package-lock.json ./
RUN npm ci

# 复制源码并构建
COPY . .

# 构建时注入生产环境变量（API 地址通过 Nginx 反代，这里用相对路径即可）
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ============================================
# 阶段 2：Nginx 运行
# ============================================
FROM nginx:1.25-alpine

LABEL maintainer="ai-hub"
LABEL description="AI Hub Community Frontend"

# 移除默认配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制自定义 Nginx 配置
COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制静态资源
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
