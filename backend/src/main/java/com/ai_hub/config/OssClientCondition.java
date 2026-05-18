package com.ai_hub.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * OSS客户端创建条件
 * 只有当配置了有效的accessKeyId时才创建OSS客户端
 */
public class OssClientCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String accessKeyId = context.getEnvironment().getProperty("aliyun.oss.access-key-id");
        return accessKeyId != null && !accessKeyId.isEmpty();
    }
}
