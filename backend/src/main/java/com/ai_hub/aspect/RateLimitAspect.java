package com.ai_hub.aspect;

import com.ai_hub.annotation.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 * 基于 Redis + 滑动窗口算法实现接口限流
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 环绕通知，处理限流逻辑
     */
    @Around("@annotation(com.ai_hub.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取限流注解
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return joinPoint.proceed();
        }

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 构建限流 key：类名.方法名:IP地址
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String ip = getClientIp(request);
        String key = String.format("rate_limit:%s.%s:%s", className, methodName, ip);

        // 获取配置参数
        int maxRequests = rateLimit.maxRequests();
        int timeWindow = rateLimit.timeWindow();
        String message = rateLimit.message();

        // 使用 Redis ZSET 实现滑动窗口限流
        long now = System.currentTimeMillis();
        long windowStart = now - timeWindow * 1000L;

        // 移除过期的记录
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // 获取当前窗口内的请求数
        Long currentCount = redisTemplate.opsForZSet().count(key, windowStart, now);

        if (currentCount != null && currentCount >= maxRequests) {
            log.warn("限流触发 - IP: {}, 接口: {}.{}, 当前请求数: {}, 最大允许: {}", 
                    ip, className, methodName, currentCount, maxRequests);
            throw new RuntimeException(message);
        }

        // 添加当前请求记录
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
        
        // 设置过期时间（避免内存泄漏）
        redisTemplate.expire(key, timeWindow + 10, TimeUnit.SECONDS);

        log.debug("限流检查通过 - IP: {}, 接口: {}.{}, 当前请求数: {}", 
                ip, className, methodName, currentCount == null ? 1 : currentCount + 1);

        // 执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个 IP 为客户端真实 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
