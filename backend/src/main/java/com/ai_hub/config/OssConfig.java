package com.ai_hub.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {
    
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String domain;
    
    /**
     * 创建OSS客户端Bean
     * 只有当配置了有效的accessKeyId时才创建
     */
    @Bean
    @Conditional(OssClientCondition.class)
    public OSS ossClient() {
        log.info("初始化OSS客户端，endpoint: {}", endpoint);
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
    
    /**
     * 检查OSS配置是否有效
     */
    public boolean isConfigured() {
        return endpoint != null && !endpoint.isEmpty() && 
               accessKeyId != null && !accessKeyId.isEmpty() && 
               accessKeySecret != null && !accessKeySecret.isEmpty();
    }
}
