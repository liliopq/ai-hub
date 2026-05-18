#!/bin/bash

# 加载环境变量
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✅ 已加载 .env 文件中的环境变量"
else
    echo "❌ 错误: .env 文件不存在"
    echo "请复制 .env.example 为 .env 并配置你的阿里云OSS信息"
    exit 1
fi

# 显示当前配置（隐藏敏感信息）
echo "========================================="
echo "当前配置:"
echo "  Database: ${DB_USERNAME:-root}@localhost:3306/ai_hub"
echo "  OSS Endpoint: $ALIYUN_OSS_ENDPOINT"
echo "  OSS Bucket: $ALIYUN_OSS_BUCKET_NAME"
echo "  AccessKey ID: ${ALIYUN_OSS_ACCESS_KEY_ID:0:8}****"
echo "  JWT Secret: ${JWT_SECRET:0:8}****"
echo "  AI API Key: ${AI_API_KEY:0:8}****"
echo "========================================="
echo ""

# 启动Spring Boot应用
echo "🚀 启动 Spring Boot 应用..."
mvn spring-boot:run
