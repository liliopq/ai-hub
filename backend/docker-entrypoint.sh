#!/bin/sh
# ============================================
# AI Hub Backend Docker 启动脚本
# 通过环境变量覆盖 Spring Boot 配置
# ============================================

exec java $JAVA_OPTS -Duser.timezone=Asia/Shanghai -jar app.jar \
    --spring.datasource.url="jdbc:mysql://${DB_HOST:-mysql}:${DB_PORT:-3306}/${DB_NAME:-ai_hub}?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true" \
    --spring.datasource.username="${DB_USERNAME:-root}" \
    --spring.datasource.password="${DB_PASSWORD:-Root@123456}" \
    --spring.data.redis.host="${REDIS_HOST:-redis}" \
    --spring.data.redis.port="${REDIS_PORT:-6379}" \
    --spring.data.redis.password="${REDIS_PASSWORD:-}" \
    --spring.rabbitmq.host="${RABBITMQ_HOST:-rabbitmq}" \
    --spring.rabbitmq.port="${RABBITMQ_PORT:-5672}" \
    --spring.rabbitmq.username="${RABBITMQ_USERNAME:-guest}" \
    --spring.rabbitmq.password="${RABBITMQ_PASSWORD:-guest}" \
    --cors.allowed-origins="${CORS_ORIGINS:-http://localhost}" \
    --spring.jackson.time-zone=Asia/Shanghai
