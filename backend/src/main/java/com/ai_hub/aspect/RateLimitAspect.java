package com.ai_hub.aspect;

import com.ai_hub.annotation.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;
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
     * 滑动窗口限流 Lua 脚本
     * 原子执行：删除过期记录 -> 统计当前窗口请求数 -> 添加当前请求
     * 返回值：当前窗口内的请求数（包含当前请求）
     */
    private final DefaultRedisScript<Long> rateLimitLuaScript = new DefaultRedisScript<>(
            """
            -- 参数：KEYS[1]=限流key, ARGV[1]=窗口起始时间, ARGV[2]=当前时间, ARGV[3]=当前请求时间戳(member)
            -- 1. 删除过期记录（score <= windowStart）
            redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1])
            -- 2. 统计当前窗口内的请求数（score 在 [windowStart, now] 之间）
            local currentCount = redis.call('ZCOUNT', KEYS[1], ARGV[1], ARGV[2])
            -- 3. 添加当前请求记录
            redis.call('ZADD', KEYS[1], ARGV[2], ARGV[3])
            -- 返回当前窗口内的请求数（包含当前请求）
            return currentCount + 1
            """,
            Long.class
    );

    /**
     * 环绕通知，处理限流逻辑
     */
    @Around("@annotation(com.ai_hub.annotation.RateLimit)")  // 匹配所有方法上的 RateLimit 注解
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
        long now = System.currentTimeMillis();            // score
        long windowStart = now - timeWindow * 1000L;       
        String member = String.valueOf(now);

        // 使用 Lua 脚本原子执行：删除过期记录 -> 统计请求数 -> 添加当前请求
        Long currentCount = redisTemplate.execute(
                rateLimitLuaScript,                       // 执行 Lua 脚本
                Collections.singletonList(key),    
                String.valueOf(windowStart),
                String.valueOf(now),
                member
        );

        // 检查是否超过限流阈值
        if (currentCount != null && currentCount > maxRequests) {
            log.warn("限流触发 - IP: {}, 接口: {}.{}, 当前请求数: {}, 最大允许: {}", 
                    ip, className, methodName, currentCount, maxRequests);
            throw new RuntimeException(message);
        }

        // 设置过期时间（避免内存泄漏）
        redisTemplate.expire(key, timeWindow + 10, TimeUnit.SECONDS);

        log.debug("限流检查通过 - IP: {}, 接口: {}.{}, 当前请求数: {}", 
                ip, className, methodName, currentCount);

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
