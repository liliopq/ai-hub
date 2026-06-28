# ============================================
# 阶段 1：Maven 构建
# ============================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 先复制 pom.xml，利用 Docker 缓存层加速依赖下载
COPY pom.xml .

# 配置阿里云 Maven 镜像加速（国内环境）
RUN mkdir -p /root/.m2 && echo '<?xml version="1.0" encoding="UTF-8"?>\
<settings>\
  <mirrors>\
    <mirror>\
      <id>aliyunmaven</id>\
      <mirrorOf>central</mirrorOf>\
      <name>Aliyun Maven</name>\
      <url>https://maven.aliyun.com/repository/public</url>\
    </mirror>\
  </mirrors>\
</settings>' > /root/.m2/settings.xml

RUN mvn dependency:go-offline -B

# 复制源代码并打包（跳过测试加速构建）
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================
# 阶段 2：JDK 21 运行（精简镜像）
# ============================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="ai-hub"
LABEL description="AI Hub Community Backend"

WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/*.jar app.jar

# 安装 curl 用于健康检查
RUN apk add --no-cache curl

# 暴露服务端口
EXPOSE 8080

# JVM 参数优化（适配容器环境）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 复制启动脚本
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# 健康检查：每 30s 检查一次，连续 3 次失败则标记为 unhealthy
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/auth/health || exit 1

# 启动命令：通过环境变量覆盖配置
ENTRYPOINT ["/app/docker-entrypoint.sh"]
