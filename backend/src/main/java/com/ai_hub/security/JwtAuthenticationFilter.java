package com.ai_hub.security;

import com.ai_hub.entity.User;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.utils.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 拦截请求，从 HTTP 请求中提取 JWT 并设置安全上下文
 */
@Slf4j                                // 日志
@Component                            // 表示这个类是一个组件
@RequiredArgsConstructor              // 生成构造函数
public class JwtAuthenticationFilter extends OncePerRequestFilter {
//继承 OncePerRequestFilter，确保每个请求只执行一次

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;      // Redis 操作，用于检查 Token 黑名单
    private final UserMapper userMapper;                  // 用户 Mapper，用于校验 Token 版本号

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 检查 Token 是否在黑名单中（如果 Redis 可用）
                if (isRedisAvailable()) {
                    String blacklistKey = "jwt:blacklist:" + jwt;
                    Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        log.warn("Token 已在黑名单中，拒绝访问");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\",\"data\":null}");
                        return;
                    }
                }

                // 校验 Token 版本号：比对 Token 中的版本号与数据库中的版本号
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                Integer tokenVersion = jwtTokenProvider.getTokenVersionFromToken(jwt);
                User user = userMapper.selectById(userId);
                if (user == null) {
                    log.warn("Token 对应的用户不存在，用户ID: {}", userId);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"用户不存在\",\"data\":null}");
                    return;
                }
                int dbTokenVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
                int tokenVersionInToken = tokenVersion != null ? tokenVersion : 0;
                if (tokenVersionInToken != dbTokenVersion) {
                    log.warn("Token 版本号不匹配，用户ID: {}, Token版本: {}, 数据库版本: {}", userId, tokenVersion, dbTokenVersion);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"密码已修改，Token已失效，请重新登录\",\"data\":null}");
                    return;
                }

                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                String role = user.getRole();

                // 创建认证对象（包含用户角色权限）
                var authorities = java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Set authentication for user: {}", username);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查 Redis 是否可用
     *
     * @return true 表示可用，false 表示不可用
     */
    private boolean isRedisAvailable() {
        try {
            // 尝试执行一个简单的操作来检查 Redis 连接
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.warn("Redis 不可用，将跳过 Token 黑名单检查: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从请求中获取 JWT Token
     *
     * @param request HTTP 请求
     * @return JWT Token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
